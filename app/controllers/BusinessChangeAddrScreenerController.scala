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
import forms.BusinessChangeAddrScreenerFormProvider
import models.Mode
import navigation.Navigator
import pages.businessaddress.{BusinessAddressUkPage, BusinessChangeAddrScreenerPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.BusinessChangeAddrScreenerView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class BusinessChangeAddrScreenerController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: BusinessAddressDataRequiredAction,
  formProvider: BusinessChangeAddrScreenerFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: BusinessChangeAddrScreenerView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData) { implicit request =>

    val preparedForm = request.userAnswers.get(BusinessChangeAddrScreenerPage) match {
      case None        => form
      case Some(value) => form.fill(value)
    }

    val isUkAddress =
      request.userAnswers.get(BusinessAddressUkPage).isDefined

    Ok(view(preparedForm, mode, isUkAddress))

  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData).async { implicit request =>

    val isUkAddress =
      request.userAnswers.get(BusinessAddressUkPage).isDefined

    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, isUkAddress))),
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(BusinessChangeAddrScreenerPage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(BusinessChangeAddrScreenerPage, mode, updatedAnswers))
      )
  }
}
