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
import forms.RemoveAssociatedRegNumberFormProvider

import javax.inject.Inject
import models.{Mode, UserAnswers}
import navigation.Navigator
import pages.{AssociatedRegistrationNumbersPage, ChosenAssociatedRegNumberPage, RemoveAssociatedRegNumberPage, UnsubmittedAssociatedRegNumbersPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.RemoveAssociatedRegNumberView

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class RemoveAssociatedRegNumberController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: MgdTradeDetailsDataRequiredAction,
  formProvider: RemoveAssociatedRegNumberFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: RemoveAssociatedRegNumberView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData) { implicit request =>

    val preparedForm = request.userAnswers.get(RemoveAssociatedRegNumberPage) match {
      case None        => form
      case Some(value) => form.fill(value)
    }

    request.userAnswers.get(ChosenAssociatedRegNumberPage) match {
      case Some(number) => Ok(view(preparedForm, mode, number))
      case None         => Redirect(routes.SystemErrorController.onPageLoad().url)
    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData).async { implicit request =>
    request.userAnswers.get(ChosenAssociatedRegNumberPage).map { chosenRegNumber =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, chosenRegNumber))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(updateUserAnswers(request.userAnswers, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(RemoveAssociatedRegNumberPage, mode, updatedAnswers))
        )
    } getOrElse Future.successful(Redirect(routes.ChangeRegistrationDetailsController.onPageLoad()))
  }

  private def updateUserAnswers(userAnswers: UserAnswers, value: Boolean): Try[UserAnswers] = {
    for {
      ua1 <- userAnswers.set(RemoveAssociatedRegNumberPage, value)
      ua2 <- {
        if (value) {
          ua1
            .get(ChosenAssociatedRegNumberPage)
            .fold(Try(ua1))(assocRegNo =>
              ua1.get(AssociatedRegistrationNumbersPage).match {
                case Some(assocRegNoSeq) =>
                  val updatedSequence = assocRegNoSeq.filterNot(_ == assocRegNo)
                  ua1.set(AssociatedRegistrationNumbersPage, updatedSequence)
                  ua1.get(UnsubmittedAssociatedRegNumbersPage).match {
                    case Some(unsubmittedSeq) =>
                      val updatedSequence = unsubmittedSeq.filterNot(_ == assocRegNo)
                      ua1.set(UnsubmittedAssociatedRegNumbersPage, updatedSequence)
                    case None => Try(ua1)
                  }
                case None => Try(ua1)
              }
            )
        } else {
          Try(ua1)
        }
      }
    } yield ua2
  }
}
