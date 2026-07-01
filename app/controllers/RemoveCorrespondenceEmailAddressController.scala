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
import forms.RemoveCorrespondenceEmailAddressFormProvider

import javax.inject.Inject
import models.Mode
import navigation.Navigator
import utils.FlagsUtil.checkIfChanged
import pages.{CorrespondenceDetailsChangesPage, CorrespondenceEmailPage, RemoveCorrespondenceEmailAddressPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.RemoveCorrespondenceEmailAddressView

import scala.concurrent.{ExecutionContext, Future}

class RemoveCorrespondenceEmailAddressController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: CorrespondenceDetailsDataRequiredAction,
  formProvider: RemoveCorrespondenceEmailAddressFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: RemoveCorrespondenceEmailAddressView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData) { implicit request =>

    val correspondenceEmail = request.userAnswers.get(CorrespondenceEmailPage)
    val preparedForm = request.userAnswers.get(RemoveCorrespondenceEmailAddressPage) match {
      case None        => form
      case Some(value) => form.fill(value)
    }

    correspondenceEmail match {
      case Some(email) =>
        Ok(view(preparedForm, mode, email))

      case None =>
        Redirect(routes.JourneyRecoveryController.onPageLoad())
    }
  }
  def onSubmit(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData).async { implicit request =>

    val correspondenceEmail = request.userAnswers.get(CorrespondenceEmailPage)

    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, correspondenceEmail.getOrElse("")))),
        value =>
          val isChanged =
            checkIfChanged(value, request.userAnswers, RemoveCorrespondenceEmailAddressPage, CorrespondenceDetailsChangesPage)
          val updatedAnswers = for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(RemoveCorrespondenceEmailAddressPage, value))
            cleanedAnswers <- if (value) {
                                Future.fromTry(updatedAnswers.remove(CorrespondenceEmailPage))
                              } else {
                                Future.successful(updatedAnswers)
                              }
            cleanedAnswers <- Future.fromTry(cleanedAnswers.set(CorrespondenceDetailsChangesPage, isChanged))
            _              <- sessionRepository.set(cleanedAnswers)
          } yield cleanedAnswers

          updatedAnswers.map { updatedAnswers =>
            Redirect(navigator.nextPage(RemoveCorrespondenceEmailAddressPage, mode, updatedAnswers))
          }
      )
  }

}
