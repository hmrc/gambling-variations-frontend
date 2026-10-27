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

import controllers.Execution.trampoline
import controllers.actions.*
import forms.DeclarationSubmittedFormProvider
import models.NormalMode
import navigation.Navigator
import pages.{DeclarationPage, GroupMemberPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.DeclarationView

import javax.inject.Inject
import scala.concurrent.Future

class DeclarationController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: BusinessDetailsDataRequiredAction,
  formProvider: DeclarationSubmittedFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: DeclarationView
) extends FrontendBaseController
    with I18nSupport {

  val form: Form[Boolean] = formProvider()

  def onPageLoad: Action[AnyContent] = (authorise andThen getData andThen requireData) { implicit request =>
    request.userAnswers
      .get(GroupMemberPage)
      .fold(Redirect(routes.SystemErrorController.onPageLoad()))(isGroupMember => Ok(view(form, isGroupMember)))
  }

  def onSubmit: Action[AnyContent] =
    (authorise andThen getData andThen requireData).async { implicit request =>
      for {
        updatedAnswers <- Future.fromTry(request.userAnswers.set(DeclarationPage, true))
        _              <- sessionRepository.set(updatedAnswers)
      } yield Redirect(navigator.nextPage(DeclarationPage, NormalMode, updatedAnswers))
    }
}
