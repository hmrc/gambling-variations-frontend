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
import forms.controllingbody.ControllingBodyTradingNameFormProvider
import models.{BusinessType, Mode, UserAnswers}
import navigation.Navigator
import pages.controllingbody.{ControllingBodyBusinessTypePage, ControllingBodySectionPage, ControllingBodyTradingNamePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.controllingbody.ControllingBodyTradingNameView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ControllingBodyTradingNameController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: ControllingBodyDetailsDataRequiredAction,
  formProvider: ControllingBodyTradingNameFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ControllingBodyTradingNameView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  private def controllingBodyType(answers: UserAnswers, registration: String): Option[BusinessType] =
    for {
      _        <- answers.get(ControllingBodySectionPage).filter(_ == registration)
      bodyType <- answers.get(ControllingBodyBusinessTypePage)
    } yield bodyType

  def onPageLoad(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData) { implicit request =>
    controllingBodyType(request.userAnswers, request.mgdRegNum) match {
      case Some(bodyType) =>
        val preparedForm = request.userAnswers.get(ControllingBodyTradingNamePage).fold(form)(form.fill)
        Ok(view(preparedForm, mode, bodyType))
      case None => Redirect(controllers.routes.SystemErrorController.onPageLoad())
    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData).async { implicit request =>
    controllingBodyType(request.userAnswers, request.mgdRegNum) match {
      case Some(bodyType) =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, bodyType))),
            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(ControllingBodyTradingNamePage, value))
                saved          <- sessionRepository.set(updatedAnswers)
              } yield saved match {
                case true  => Redirect(navigator.nextPage(ControllingBodyTradingNamePage, mode, updatedAnswers))
                case false => Redirect(controllers.routes.SystemErrorController.onPageLoad())
              }
          )
      case None => Future.successful(Redirect(controllers.routes.SystemErrorController.onPageLoad()))
    }
  }
}
