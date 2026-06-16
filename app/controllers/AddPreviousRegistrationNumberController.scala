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
import forms.AddPreviousRegistrationNumberFormProvider
import models.{Mode, UserAnswers}
import navigation.Navigator
import pages.{AddPreviousRegistrationNumberPage, ChosenPreviousRegNumberPage, PreviousRegistrationNumbersPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.AddPreviousRegistrationNumberView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class AddPreviousRegistrationNumberController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: MgdTradeDetailsDataRequiredAction,
  formProvider: AddPreviousRegistrationNumberFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AddPreviousRegistrationNumberView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData) { implicit request =>
    val preparedForm = request.userAnswers
      .get(PreviousRegistrationNumbersPage)
      .fold(form)(form.fill)

    Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData).async { implicit request =>
    val currentSequence = request.userAnswers.get(PreviousRegistrationNumbersPage)
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(PreviousRegistrationNumbersPage, currentSequence +: value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(AddPreviousRegistrationNumberPage, mode, updatedAnswers))
      )
  }
}
