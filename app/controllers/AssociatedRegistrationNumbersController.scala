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
import forms.AssociatedRegistrationNumbersFormProvider
import models.Mode
import models.requests.DataRequest
import navigation.Navigator
import pages.{AddAssociatedRegistrationNumberPage, AssociatedRegNumbersUpdatedPage, AssociatedRegistrationNumbersPage, ChosenAssociatedRegNumberPage, UnsubmittedAssociatedRegNumbersPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.AssociatedRegistrationNumbersView

import scala.concurrent.{ExecutionContext, Future}

class AssociatedRegistrationNumbersController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: MgdTradeDetailsDataRequiredAction,
  formProvider: AssociatedRegistrationNumbersFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AssociatedRegistrationNumbersView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val MaxAssociatedRegistrationNumbers = 3

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
      submitted   = request.userAnswers.get(AssociatedRegistrationNumbersPage),
      unsubmitted = request.userAnswers.get(UnsubmittedAssociatedRegNumbersPage)
    )

  private def associatedRegNumbersUpdated(request: DataRequest[?]): Boolean =
    request.userAnswers.get(AssociatedRegNumbersUpdatedPage).fold(false)(_ => true)

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (authorise andThen getData andThen requireData) { implicit request =>

      val preparedForm =
        request.userAnswers
          .get(AddAssociatedRegistrationNumberPage)
          .fold(form)(form.fill)

      val regNumbers = registrationNumbers(request)
      val regNumbersUpdated = associatedRegNumbersUpdated(request)
      
        Ok(
          view(
            preparedForm,
            mode,
            regNumbers.submitted,
            regNumbers.unsubmitted,
            regNumbers.submittedCount,
            regNumbers.unsubmittedCount,
            regNumbersUpdated
          )
        )
      }
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (authorise andThen getData andThen requireData).async { implicit request =>

      val regNumbers = registrationNumbers(request)
      val regNumbersUpdated = associatedRegNumbersUpdated(request)

      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            if (regNumbers.totalCount < MaxAssociatedRegistrationNumbers) {
              Future.successful(
                BadRequest(
                  view(
                    formWithErrors,
                    mode,
                    regNumbers.submitted,
                    regNumbers.unsubmitted,
                    regNumbers.submittedCount,
                    regNumbers.unsubmittedCount,
                    regNumbersUpdated
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
                                    AddAssociatedRegistrationNumberPage,
                                    value
                                  )
                                )
              _ <- sessionRepository.set(updatedAnswers)
            } yield Redirect(
              navigator.nextPage(
                AddAssociatedRegistrationNumberPage,
                mode,
                updatedAnswers
              )
            )
        )
    }

  def onRedirect(mode: Mode, assocRegNumber: String): Action[AnyContent] =
    (authorise andThen getData andThen requireData).async { implicit request =>
      for {
        updatedAnswers <- Future.fromTry(
                            request.userAnswers.set(
                              ChosenAssociatedRegNumberPage,
                              assocRegNumber
                            )
                          )
        _ <- sessionRepository.set(updatedAnswers)
      } yield Redirect(
        routes.RemoveAssociatedRegNumberController.onPageLoad(mode)
      )
    }
}
