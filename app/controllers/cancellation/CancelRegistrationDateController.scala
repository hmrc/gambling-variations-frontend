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

import controllers.actions.{AuthenticatedAction, AuthorisedAction, BusinessDetailsDataRequiredAction, DataRetrievalAction}
import controllers.routes
import forms.cancellation.CancelRegistrationDateFormProvider
import models.{Mode, NormalMode}
import navigation.Navigator
import pages.DateOfRegistrationPage
import pages.cancellation.CancelRegistrationDatePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.cancellation.CancelRegistrationDateView

import java.time.{Clock, LocalDate}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

//TODO slight message difference, but key should be enough to handle it
//https://jira.tools.tax.service.gov.uk/browse/DTR-9154
//https://jira.tools.tax.service.gov.uk/browse/DTR-9089
class CancelRegistrationDateController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  authorise: AuthorisedAction,
  authenticate: AuthenticatedAction,
  getData: DataRetrievalAction,
  requireData: BusinessDetailsDataRequiredAction,
  formProvider: CancelRegistrationDateFormProvider,
  clock: Clock,
  val controllerComponents: MessagesControllerComponents,
  view: CancelRegistrationDateView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with CancellationEntrypointDecider
    with I18nSupport {

  // TODO naming
  private val groupKey = "cancelGroupRegistrationDate"
  private val nonGroupKey = "cancelRegistrationDate"

  def onLanding(regNumber: String): Action[AnyContent] =
    authenticate { request =>
      Redirect(controllers.cancellation.routes.CancelRegistrationDateController.onPageLoad(NormalMode))
        .addingToSession(AuthorisedAction.sessionRegNumberKey -> regNumber)(request)
    }

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (authorise andThen getData andThen requireData) { implicit request =>
      checkScreen(request.userAnswers, CurrentScreen.Cancel, false, mode) {
        request.userAnswers
          .get(DateOfRegistrationPage)
          .fold(
            Redirect(routes.SystemErrorController.onPageLoad().url)
          ) { registrationDate =>

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

            preparedForm.get.getDayOfMonth.toString

            Ok(
              view(
                preparedForm,
                mode,
                registrationDate,
                firstInvalidDate,
                middleOfDateRange,
                nonGroupKey,
                false
              )
            )

          }
      }
    }

  // TODO check screen
  def onSubmit(mode: Mode): Action[AnyContent] =
    (authorise andThen getData andThen requireData).async { implicit request =>
      request.userAnswers.get(DateOfRegistrationPage).fold(Future.successful(systemErrorRedirect)) { registrationDate =>
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
                    middleOfDateRange,
                    nonGroupKey,
                    false
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
      }
    }

  ////////////////////////////////

  def onPageLoadGroup(mode: Mode): Action[AnyContent] =
    (authorise andThen getData andThen requireData) { implicit request =>
      checkScreen(request.userAnswers, CurrentScreen.Cancel, true, mode) {
        request.userAnswers.get(DateOfRegistrationPage).fold(systemErrorRedirect) { dateOfRegistration =>
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
            view(preparedForm, mode, dateOfRegistration, firstInvalidDate, middleOfDateRange, groupKey, true)
          )
        }
      }
    }

  // TODO check screen
  def onSubmitGroup(mode: Mode): Action[AnyContent] =
    (authorise andThen getData andThen requireData).async { implicit request =>
      request.userAnswers
        .get(DateOfRegistrationPage)
        .fold(Future.successful(systemErrorRedirect)) { dateOfRegistration =>
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
                    view(formWithErrors, mode, dateOfRegistration, firstInvalidDate, middleOfDateRange, groupKey, true)
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
