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
import forms.BusinessNonUKAddressFormProvider
import models.Mode
import navigation.Navigator
import pages.{BusinessAddressChangesPage, BusinessAddressIsleMOrChannelFlagPage, BusinessAddressNonUkPage, BusinessAddressSubmittedPage, BusinessAddressUkPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.FlagsUtil.checkIfChanged
import views.html.BusinessNonUKAddressView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BusinessNonUKAddressController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: BusinessAddressDataRequiredAction,
  formProvider: BusinessNonUKAddressFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: BusinessNonUKAddressView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (authorise andThen getData andThen requireData) { implicit request =>

      val preparedForm = request.userAnswers
        .get(BusinessAddressNonUkPage)
        .fold(form)(form.fill)

      Ok(view(preparedForm, mode))
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (authorise andThen getData andThen requireData).async { implicit request =>

      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
          value => {
            val ua = request.userAnswers
            val isChanged: Boolean = checkIfChanged(value, ua, BusinessAddressNonUkPage, BusinessAddressChangesPage)

            for {
              updatedAnswers <- Future.fromTry(ua.set(BusinessAddressNonUkPage, value))
              updatedAnswers <- Future.fromTry(updatedAnswers.remove(BusinessAddressUkPage))
              updatedAnswers <- Future.fromTry(updatedAnswers.set(BusinessAddressIsleMOrChannelFlagPage, "false"))
              updatedAnswers <- Future.fromTry(updatedAnswers.set(BusinessAddressSubmittedPage, true))
              updatedAnswers <- Future.fromTry(updatedAnswers.set(BusinessAddressChangesPage, isChanged))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(
              navigator.nextPage(
                BusinessAddressNonUkPage,
                mode,
                updatedAnswers
              )
            )
          }
        )
    }
}
