/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers.cancellation

import connectors.GamblingConnector
import controllers.actions.{AuthorisedAction, BusinessDetailsDataRequiredAction, DataRetrievalAction}
import controllers.routes
import forms.cancellation.CancelRegistrationDateFormProvider
import models.Mode
import navigation.Navigator
import pages.cancellation.CancelRegistrationDatePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.cancellation.CancelRegistrationDateView

import java.time.{Clock, LocalDate}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CancelRegistrationDateController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: BusinessDetailsDataRequiredAction,
  formProvider: CancelRegistrationDateFormProvider,
  gamblingConnector: GamblingConnector,
  clock: Clock,
  val controllerComponents: MessagesControllerComponents,
  view: CancelRegistrationDateView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (authorise andThen getData andThen requireData).async { implicit request =>
      gamblingConnector
        .getBusinessDetails(request.mgdRegNum)
        .map { businessDetails =>
          businessDetails.dateOfRegistration match {
            case Some(registrationDate) =>
              val maximumValidDate =
                LocalDate.now(clock).plusDays(14)

              val firstInvalidDate =
                maximumValidDate.plusDays(1)

              val form =
                formProvider(registrationDate, maximumValidDate)

              val middleOfDateRange =
                middleDate(registrationDate, firstInvalidDate)

              val preparedForm =
                request.userAnswers
                  .get(CancelRegistrationDatePage)
                  .map(form.fill)
                  .getOrElse(form)

              Ok(
                view(
                  preparedForm,
                  mode,
                  registrationDate,
                  firstInvalidDate,
                  middleOfDateRange
                )
              )

            case None =>
              systemErrorRedirect
          }
        }
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (authorise andThen getData andThen requireData).async { implicit request =>
      gamblingConnector
        .getBusinessDetails(request.mgdRegNum)
        .flatMap { businessDetails =>
          businessDetails.dateOfRegistration match {
            case Some(registrationDate) =>
              val maximumValidDate =
                LocalDate.now(clock).plusDays(14)

              val firstInvalidDate =
                maximumValidDate.plusDays(1)

              val form =
                formProvider(registrationDate, maximumValidDate)

              val middleOfDateRange =
                middleDate(registrationDate, firstInvalidDate)

              form
                .bindFromRequest()
                .fold(
                  formWithErrors =>
                    Future.successful(
                      BadRequest(
                        view(
                          formWithErrors,
                          mode,
                          registrationDate,
                          firstInvalidDate,
                          middleOfDateRange
                        )
                      )
                    ),
                  value =>
                    for {
                      updatedAnswers <- Future.fromTry(
                                          request.userAnswers.set(
                                            CancelRegistrationDatePage,
                                            value
                                          )
                                        )
                      _ <- sessionRepository.set(updatedAnswers)
                    } yield Redirect(
                      navigator.nextPage(
                        CancelRegistrationDatePage,
                        mode,
                        updatedAnswers
                      )
                    )
                )

            case None =>
              Future.successful(systemErrorRedirect)
          }
        }
    }

  private def systemErrorRedirect: Result =
    Redirect(routes.SystemErrorController.onPageLoad())

  private def middleDate(
    startDate: LocalDate,
    endDate: LocalDate
  ): LocalDate = {
    val daysBetween =
      java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate)

    startDate.plusDays(daysBetween / 2)
  }

}
