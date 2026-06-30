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
import forms.EmailAddressFormProvider
import models.Mode
import navigation.Navigator
import utils.FlagsUtil.checkIfChanged
import pages.{CorrespondenceDetailsChangesPage, CorrespondenceDetailsSubmittedPage, CorrespondenceEmailPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.CorrespondenceEmailAddressView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CorrespondenceEmailAddressController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: CorrespondenceDetailsDataRequiredAction,
  formProvider: EmailAddressFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: CorrespondenceEmailAddressView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider("correspondenceEmailAddress")

  def onPageLoad(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData) { implicit request =>
    val preparedForm = request.userAnswers
      .get(CorrespondenceEmailPage)
      .fold(form)(form.fill)

    Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData).async { implicit request =>

    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
        value =>
          val isChanged: Boolean = checkIfChanged(value, request.userAnswers, CorrespondenceEmailPage)

          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(CorrespondenceEmailPage, value))
            updatedAnswers <- Future.fromTry(updatedAnswers.set(CorrespondenceDetailsSubmittedPage, true))
            updatedAnswers <- Future.fromTry(updatedAnswers.set(CorrespondenceDetailsChangesPage, isChanged))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(CorrespondenceEmailPage, mode, updatedAnswers))
      )
  }
}
