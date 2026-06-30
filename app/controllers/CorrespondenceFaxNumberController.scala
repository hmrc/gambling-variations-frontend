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
import forms.FaxNumberFormProvider

import javax.inject.Inject
import models.Mode
import navigation.Navigator
import utils.FlagsUtil.checkIfChanged
import pages.{CorrespondenceDetailsChangesPage, CorrespondenceDetailsSubmittedPage, CorrespondenceFaxNumberPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.CorrespondenceFaxNumberView

import scala.concurrent.{ExecutionContext, Future}

class CorrespondenceFaxNumberController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: CorrespondenceDetailsDataRequiredAction,
  formProvider: FaxNumberFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: CorrespondenceFaxNumberView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider("correspondenceFaxNumber")

  def onPageLoad(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData) { implicit request =>

    val preparedForm = request.userAnswers.get(CorrespondenceFaxNumberPage) match {
      case None        => form
      case Some(value) => form.fill(value)
    }

    Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData).async { implicit request =>

    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
        value =>
          val isChanged: Boolean = checkIfChanged(value, request.userAnswers, CorrespondenceFaxNumberPage)
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(CorrespondenceFaxNumberPage, value))
            updatedAnswers <- Future.fromTry(updatedAnswers.set(CorrespondenceDetailsSubmittedPage, true))
            updatedAnswers <- Future.fromTry(updatedAnswers.set(CorrespondenceDetailsChangesPage, isChanged))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(CorrespondenceFaxNumberPage, mode, updatedAnswers))
      )
  }
}
