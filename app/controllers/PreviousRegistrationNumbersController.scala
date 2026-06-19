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

package controllers

import controllers.actions.*

import javax.inject.Inject
import forms.PreviousRegistrationNumbersFormProvider
import models.Mode
import models.requests.DataRequest
import navigation.Navigator
import pages.{AddPreviousRegistrationNumberPage, ChosenPreviousRegNumberPage, PreviousRegistrationNumbersPage, UnsubmittedPreviousRegNumbersPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.PreviousRegistrationNumbersView

import scala.concurrent.{ExecutionContext, Future}

class PreviousRegistrationNumbersController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: MgdTradeDetailsDataRequiredAction,
  formProvider: PreviousRegistrationNumbersFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: PreviousRegistrationNumbersView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val MaxPreviousRegistrationNumbers = 3

  private val form = formProvider()

  private case class RegistrationNumbers(
    submitted: Option[Seq[String]],
    unsubmitted: Option[Seq[String]]
  ) {
    val submittedCount: Int = submitted.fold(0)(_.size)
    val unsubmittedCount: Int = unsubmitted.fold(0)(_.size)
    val totalCount: Int = submittedCount + unsubmittedCount
  }

  private def registrationNumbers(
    request: DataRequest[?]
  ): RegistrationNumbers =
    RegistrationNumbers(
      submitted   = request.userAnswers.get(PreviousRegistrationNumbersPage),
      unsubmitted = request.userAnswers.get(UnsubmittedPreviousRegNumbersPage)
    )

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (authorise andThen getData andThen requireData) { implicit request =>

      val preparedForm =
        request.userAnswers
          .get(AddPreviousRegistrationNumberPage)
          .fold(form)(form.fill)

      val regNumbers = registrationNumbers(request)

      Ok(
        view(
          preparedForm,
          mode,
          regNumbers.submitted,
          regNumbers.unsubmitted,
          regNumbers.submittedCount,
          regNumbers.unsubmittedCount
        )
      )
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (authorise andThen getData andThen requireData).async { implicit request =>

      val regNumbers = registrationNumbers(request)

      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            if (regNumbers.totalCount < MaxPreviousRegistrationNumbers) {
              Future.successful(
                BadRequest(
                  view(
                    formWithErrors,
                    mode,
                    regNumbers.submitted,
                    regNumbers.unsubmitted,
                    regNumbers.submittedCount,
                    regNumbers.unsubmittedCount
                  )
                )
              )
            } else {
              Future.successful(Redirect("#"))
            },
          value =>
            for {
              updatedAnswers <- Future.fromTry(
                                  request.userAnswers.set(
                                    AddPreviousRegistrationNumberPage,
                                    value
                                  )
                                )
              _ <- sessionRepository.set(updatedAnswers)
            } yield Redirect(
              navigator.nextPage(
                AddPreviousRegistrationNumberPage,
                mode,
                updatedAnswers
              )
            )
        )
    }

  def onRedirect(mode: Mode, prevRegNumber: String): Action[AnyContent] =
    (authorise andThen getData andThen requireData).async { implicit request =>
      for {
        updatedAnswers <- Future.fromTry(
                            request.userAnswers.set(
                              ChosenPreviousRegNumberPage,
                              prevRegNumber
                            )
                          )
        _ <- sessionRepository.set(updatedAnswers)
      } yield Redirect(
        routes.RemovePreviousRegNumberController.onPageLoad(mode)
      )
    }
}
