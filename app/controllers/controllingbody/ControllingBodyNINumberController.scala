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
import forms.controllingbody.ControllingBodyNINumberFormProvider
import models.BusinessType.Soleproprietor
import models.Mode
import navigation.Navigator
import pages.controllingbody.*
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.FlagsUtil.checkIfChanged
import views.html.controllingbody.ControllingBodyNINumberView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ControllingBodyNINumberController @Inject()(
  override val messagesApi: MessagesApi,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  navigator: Navigator,
  requireData: ControllingBodyDetailsDataRequiredAction,
  formProvider: ControllingBodyNINumberFormProvider,
  sessionRepository: SessionRepository,
  val controllerComponents: MessagesControllerComponents,
  view: ControllingBodyNINumberView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData) { implicit request =>
    request.userAnswers.get(ControllingBodyBusinessTypePage) match {
      case Some(Soleproprietor) =>
        val preparedForm = request.userAnswers
          .get(ControllingBodyNINumberPage)
          .fold(form)(form.fill)

        Ok(view(preparedForm, mode))
      case _ => Redirect(controllers.routes.SystemErrorController.onPageLoad())
    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData).async { implicit request =>

    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
        value =>
          val ua = request.userAnswers
          val isChanged: Boolean = checkIfChanged(value, ua, ControllingBodyNINumberPage, ControllingBodyChangesPage)
          for {
            updatedAnswers <- Future.fromTry(ua.set(ControllingBodyNINumberPage, value))
            updatedAnswers <- Future.fromTry(updatedAnswers.set(ControllingBodySubmittedPage, true))
            updatedAnswers <- Future.fromTry(updatedAnswers.set(ControllingBodyChangesPage, isChanged))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(ControllingBodyNINumberPage, mode, updatedAnswers))
      )
  }
}
