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

package controllers.businesscontactdetails

import controllers.actions.*
import controllers.routes
import forms.EmailAddressFormProvider
import models.Mode
import navigation.Navigator
import pages.GroupMemberPage
import pages.contactdetails.*
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.FlagsUtil.checkIfChanged
import views.html.businesscontactdetails.BusinessEmailAddressView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BusinessEmailAddressController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  sessionData: BusinessContactDetailsSessionDataAction,
  submittedData: BusinessContactDetailsSubmittedDataAction,
  formProvider: EmailAddressFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: BusinessEmailAddressView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider("emailAddress")

  def onPageLoad(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen submittedData andThen sessionData) { implicit request =>
    request.submittedAnswers.get(GroupMemberPage) match {
      case Some(true) =>
        Redirect(routes.AccessDeniedController.onPageLoad())
      case Some(false) =>
        val preparedForm = request.submittedAnswers
          .get(BusinessEmailAddressPage)
          .fold(form)(form.fill)

        Ok(view(preparedForm, mode))
      case None =>
        Redirect(routes.SystemErrorController.onPageLoad())
    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen submittedData andThen sessionData).async { implicit request =>
    request.submittedAnswers.get(GroupMemberPage) match {
      case Some(true) =>
        Future.successful(Redirect(routes.AccessDeniedController.onPageLoad()))
      case Some(false) =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
            value =>
              val isChanged: Boolean =
                checkIfChanged(value, request.submittedAnswers, BusinessEmailAddressPage, ContactDetailsChangesPage)
              for {
                updatedAnswers <- Future.fromTry(request.submittedAnswers.set(BusinessEmailAddressPage, value))
                updatedAnswers <- Future.fromTry(updatedAnswers.set(BusinessContactDetailsSubmittedPage, true))
                updatedAnswers <- Future.fromTry(updatedAnswers.set(ContactDetailsChangesPage, isChanged))
                _              <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(BusinessEmailAddressPage, mode, updatedAnswers))
          )
      case None =>
        Future.successful(Redirect(routes.SystemErrorController.onPageLoad()))
    }
  }
}
