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

import controllers.actions.{AuthorisedAction, BusinessDetailsDataRequiredAction, DataRetrievalAction}
import controllers.routes
import forms.cancellation.CancelGroupRegistrationDateFormProvider
import models.Mode
import navigation.Navigator
import pages.DateOfRegistrationPage
import pages.cancellation.{CancelRegistrationDatePage, RegistrationCancellationDeclarationPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.cancellation.CancelGroupRegistrationDateView

import java.time.{Clock, LocalDate}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CancelGroupRegistrationDateController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: BusinessDetailsDataRequiredAction,
  formProvider: CancelGroupRegistrationDateFormProvider,
  clock: Clock,
  val controllerComponents: MessagesControllerComponents,
  view: CancelGroupRegistrationDateView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (authorise andThen getData andThen requireData) { implicit request =>
      val registrationAlreadyCancelled = request.userAnswers.get(RegistrationCancellationDeclarationPage).contains(true)

      if (registrationAlreadyCancelled) {
        Redirect(controllers.cancellation.routes.RegistrationCancellationWarningController.onPageLoad().url)
      } else {
        request.userAnswers
          .get(DateOfRegistrationPage)
          .map { dateOfRegistration =>
            val maximumValidDate =
              LocalDate.now(clock).plusDays(14)

            val firstInvalidDate =
              maximumValidDate.plusDays(1)

            val form =
              formProvider(dateOfRegistration, maximumValidDate)

            val middleOfDateRange =
              middleDate(dateOfRegistration, firstInvalidDate)

            val preparedForm =
              request.userAnswers
                .get(CancelRegistrationDatePage)
                .map(form.fill)
                .getOrElse(form)

            Ok(
              view(preparedForm, dateOfRegistration, firstInvalidDate, middleOfDateRange)
            )
          }
          .getOrElse(systemErrorRedirect)
      }
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (authorise andThen getData andThen requireData).async { implicit request =>

      request.userAnswers
        .get(DateOfRegistrationPage)
        .map { dateOfRegistration =>
          val maximumValidDate =
            LocalDate.now(clock).plusDays(14)

          val firstInvalidDate =
            maximumValidDate.plusDays(1)

          val form =
            formProvider(dateOfRegistration, maximumValidDate)

          val middleOfDateRange =
            middleDate(dateOfRegistration, firstInvalidDate)

          form
            .bindFromRequest()
            .fold(
              formWithErrors =>
                Future.successful(
                  BadRequest(
                    view(formWithErrors, dateOfRegistration, firstInvalidDate, middleOfDateRange)
                  )
                ),
              value =>
                for {
                  updatedAnswers <- Future.fromTry(
                                      request.userAnswers.set(CancelRegistrationDatePage, value)
                                    )
                  _ <- sessionRepository.set(updatedAnswers)
                } yield Redirect(
                  navigator.nextPage(CancelRegistrationDatePage, mode, updatedAnswers)
                )
            )

        }
        .getOrElse(Future.successful(systemErrorRedirect))
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
