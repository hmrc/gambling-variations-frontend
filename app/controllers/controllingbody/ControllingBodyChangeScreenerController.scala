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
import forms.controllingbody.ControllingBodyChangeScreenerFormProvider
import models.controllingbody.ControllingBodyChangeOption
import models.{Mode, UserAnswers}
import navigation.Navigator
import pages.controllingbody.{ControllingBodyBusinessNamePage, ControllingBodyChangeScreenerPage, ControllingBodySoleProprietorPage}
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.controllingbody.ControllingBodyChangeScreenerView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

/** CB-INT-NSM: asks what the user wants to do with a controlling body that is not the same as the representative member. */
class ControllingBodyChangeScreenerController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: ControllingBodyDetailsDataRequiredAction,
  formProvider: ControllingBodyChangeScreenerFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ControllingBodyChangeScreenerView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  val form: Form[ControllingBodyChangeOption] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData) { implicit request =>
    controllingBodyName(request.userAnswers) match {
      case Some(name) =>
        val preparedForm = request.userAnswers.get(ControllingBodyChangeScreenerPage) match {
          case None        => form
          case Some(value) => form.fill(value)
        }

        Ok(view(preparedForm, mode, name))

      case None =>
        redirectToSystemError(request.userAnswers)
    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData).async { implicit request =>
    controllingBodyName(request.userAnswers) match {
      case Some(name) =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, name))),
            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(ControllingBodyChangeScreenerPage, value))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(ControllingBodyChangeScreenerPage, mode, updatedAnswers))
          )

      case None =>
        Future.successful(redirectToSystemError(request.userAnswers))
    }
  }

  /** The business name is shown, or the sole proprietor's name when the controlling body does not have a business name. */
  private def controllingBodyName(userAnswers: UserAnswers): Option[String] =
    userAnswers
      .get(ControllingBodyBusinessNamePage)
      .orElse(userAnswers.get(ControllingBodySoleProprietorPage).map(_.fullName))

  private def redirectToSystemError(userAnswers: UserAnswers): Result = {
    logger.warn(s"Controlling body name not found in User Answers for id ${userAnswers.id}")
    Redirect(routes.SystemErrorController.onPageLoad())
  }
}
