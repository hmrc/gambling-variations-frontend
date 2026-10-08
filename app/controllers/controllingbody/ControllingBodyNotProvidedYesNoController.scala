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

package controllers.controllingbody

import controllers.actions.*
import controllers.routes
import forms.controllingbody.ControllingBodyNotProvidedYesNoFormProvider
import models.Mode
import navigation.Navigator
import pages.controllingbody.ControllingBodyBusinessNamePage
import pages.controllingbody.ControllingBodyNotProvidedYesNoPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.controllingbody.ControllingBodyNotProvidedYesNoView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ControllingBodyNotProvidedYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: ControllingBodyDetailsDataRequiredAction,
  formProvider: ControllingBodyNotProvidedYesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ControllingBodyNotProvidedYesNoView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (authorise andThen getData andThen requireData) { implicit request =>

      val businessName = request.userAnswers.get(ControllingBodyBusinessNamePage).getOrElse("")
      val preparedForm = request.userAnswers.get(ControllingBodyNotProvidedYesNoPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode, businessName))
    }

  def onSubmit(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData).async { implicit request =>
    request.userAnswers
      .get(ControllingBodyBusinessNamePage)
      .map { businessName =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, businessName))),
            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(ControllingBodyNotProvidedYesNoPage, value))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(ControllingBodyNotProvidedYesNoPage, mode, updatedAnswers))
          )
      }
      .getOrElse {
        Future.successful(Redirect(routes.SystemErrorController.onPageLoad()))
      }
  }
}
