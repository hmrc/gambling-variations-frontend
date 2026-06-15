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
import forms.AssociatedRegistrationNumbersFormProvider

import javax.inject.Inject
import models.Mode
import navigation.Navigator
import pages.{AddAssociatedRegistrationNumberPage, AssociatedRegistrationNumbersPage, ChosenAssociatedRegNumberPage}
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

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData) { implicit request =>

    val preparedForm = request.userAnswers.get(AddAssociatedRegistrationNumberPage) match {
      case None        => form
      case Some(value) => form.fill(value)
    }
    val associatedRegNumberSeq: Option[Seq[String]] = request.userAnswers.get(AssociatedRegistrationNumbersPage)
    val associatedRegNumberCount: Int = associatedRegNumberSeq match {
      case Some(sequence) => sequence.length
      case None           => 0
    }

    Ok(view(preparedForm, mode, associatedRegNumberSeq, associatedRegNumberCount))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData).async { implicit request =>

    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, request.userAnswers.get(AssociatedRegistrationNumbersPage), 0))),
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(AddAssociatedRegistrationNumberPage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(AddAssociatedRegistrationNumberPage, mode, updatedAnswers))
      )
  }

  def onRedirect(mode: Mode, assocRegNumber: String): Action[AnyContent] =
    (authorise andThen getData andThen requireData).async { implicit request =>
      for {
        updatedAnswers <- Future.fromTry(request.userAnswers.set(ChosenAssociatedRegNumberPage, assocRegNumber))
        _              <- sessionRepository.set(updatedAnswers)
      } yield Redirect(routes.RemoveAssociatedRegNumberController.onPageLoad(mode))
    }

}
