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
import forms.controllingbody.ControllingBodyForeignCorpRefFormProvider
import models.BusinessType.Corporatebody
import models.{BusinessType, Mode}
import navigation.Navigator
import pages.controllingbody.*
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.FlagsUtil.checkIfChanged
import views.html.controllingbody.ControllingBodyForeignCorpRefView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ControllingBodyForeignCorpRefController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: ControllingBodyDetailsDataRequiredAction,
  formProvider: ControllingBodyForeignCorpRefFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ControllingBodyForeignCorpRefView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form: Form[String] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData) { implicit request =>

    val incorporatedInUK = request.userAnswers.get(ControllingBodyIsUkIncorporatedPage)
    val businessType = request.userAnswers.get(ControllingBodyBusinessTypePage)

    (businessType, incorporatedInUK) match {
      case (Some(Corporatebody), Some("0")) =>
        val preparedForm = request.userAnswers.get(ControllingBodyForeignCorpRefPage) match {
          case None        => form
          case Some(value) => form.fill(value)
        }
        Ok(view(preparedForm, mode))

      case _ =>
        Redirect(controllers.routes.SystemErrorController.onPageLoad())
    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData).async { implicit request =>

    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
        value =>
          val ua = request.userAnswers
          val isChanged = checkIfChanged(value, ua, ControllingBodyForeignCorpRefPage, ControllingBodyChangesPage)
          for {
            updatedAnswers              <- Future.fromTry(request.userAnswers.set(ControllingBodyForeignCorpRefPage, value))
            updatedAnswersWithSubmitted <- Future.fromTry(updatedAnswers.set(ControllingBodySubmittedPage, true))
            finalAnswers                <- Future.fromTry(updatedAnswersWithSubmitted.set(ControllingBodyChangesPage, isChanged))
            _                           <- sessionRepository.set(finalAnswers)
          } yield Redirect(navigator.nextPage(ControllingBodyForeignCorpRefPage, mode, finalAnswers))
      )
  }
}
