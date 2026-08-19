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
import forms.RemoveBusinessAddressAddInfoFormProvider
import models.Mode
import navigation.Navigator
import pages.{BusinessAddressAdditionalInformationPage, BusinessAddressChangesPage, BusinessAddressSubmittedPage, RemoveBusinessAddressAddInfoPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.RemoveBusinessAddressAddInfoView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RemoveBusinessAddressAddInfoController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: BusinessAddressDataRequiredActionImpl,
  formProvider: RemoveBusinessAddressAddInfoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: RemoveBusinessAddressAddInfoView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData) { implicit request =>
    val addressAddInfo = request.userAnswers.get(BusinessAddressAdditionalInformationPage).getOrElse("")
    val preparedForm = request.userAnswers.get(RemoveBusinessAddressAddInfoPage) match {
      case None        => form
      case Some(value) => form.fill(value)
    }

    Ok(view(preparedForm, mode, addressAddInfo))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData).async { implicit request =>
    val addressAddInfo = request.userAnswers.get(BusinessAddressAdditionalInformationPage).getOrElse("")

    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, addressAddInfo))),
        value =>
          val ua = request.userAnswers
          for {
            updatedAnswers <- Future.fromTry(ua.set(RemoveBusinessAddressAddInfoPage, value))
            updatedAnswers <- Future.fromTry(updatedAnswers.set(BusinessAddressSubmittedPage, true))
            updatedAnswers <- if (value) {
                                Future.fromTry(updatedAnswers.remove(BusinessAddressAdditionalInformationPage))
                              } else {
                                Future.successful(updatedAnswers)
                              }
            updatedAnswers <- Future.fromTry(updatedAnswers.set(BusinessAddressChangesPage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(RemoveBusinessAddressAddInfoPage, mode, updatedAnswers))
      )
  }
}
