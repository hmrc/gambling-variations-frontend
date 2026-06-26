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
import forms.AssociatedRegNumberFormProvider

import javax.inject.Inject
import models.{Mode, UserAnswers}
import navigation.Navigator
import pages.*
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.AssociatedRegNumberView

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class AssociatedRegNumberController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: MgdTradeDetailsDataRequiredAction,
  formProvider: AssociatedRegNumberFormProvider,
  navigator: Navigator,
  val controllerComponents: MessagesControllerComponents,
  view: AssociatedRegNumberView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form: Form[String] = formProvider()
  private val fieldName = "associatedRegNumber"
  
  def onPageLoad(mode: Mode): Action[AnyContent] =
    (authorise andThen getData andThen requireData) { implicit request =>

      val preparedForm =
        request.userAnswers
          .get(ChosenAssociatedRegNumberPage)
          .orElse(request.userAnswers.get(AssociatedRegNumberPage)) match {
          case Some(value) => form.fill(value)
          case None        => form
        }

      Ok(view(preparedForm, mode))
    }


  def onSubmit(mode: Mode): Action[AnyContent] =
    (authorise andThen getData andThen requireData).async { implicit request =>

      val existingList =
        request.userAnswers.get(AssociatedRegistrationNumbersPage).getOrElse(Seq.empty)

      val maybeEditing =
        request.userAnswers.get(ChosenAssociatedRegNumberPage)

      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
          associatedRegNumber => {

            val isDuplicate =
              maybeEditing match {
                case Some(oldValue) =>
                  existingList.exists(v => v == associatedRegNumber && v != oldValue)
                case None =>
                  existingList.contains(associatedRegNumber)
              }

            if (isDuplicate) {
              Future.successful(
                BadRequest(
                  view(
                    form
                      .fill(associatedRegNumber)
                      .withError(fieldName, "associatedRegNumber.error.duplicate"),
                    mode
                  )
                )
              )
            } else {

              val updatedList = maybeEditing match {
                
                case Some(oldValue) =>
                  existingList.map {
                    case v if v == oldValue => associatedRegNumber
                    case v                  => v
                  }
                  
                case None =>
                  existingList :+ associatedRegNumber
              }

              for {
                updatedAnswers <- Future.fromTry(
                                    request.userAnswers.set(
                                      AssociatedRegNumberPage,
                                      associatedRegNumber
                                    )
                                  )
                updatedAnswers <- Future.fromTry(
                                    updatedAnswers.set(
                                      AssociatedRegistrationNumbersPage,
                                      updatedList
                                    )
                                  )
                updatedAnswers <- Future.fromTry(
                                    updatedAnswers.set(
                                      AssociatedRegNumberSubmittedPage,
                                      true
                                    )
                                  )
                _ <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(AssociatedRegNumberPage, mode, updatedAnswers))

            }
          }
        )
    }
}
