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
import forms.CorrespondenceChangeAddrScreenerFormProvider
import models.{Address, Mode}
import navigation.Navigator
import pages.{CorrespondenceAddressUkPage, CorrespondenceChangeAddrScreenerPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.CorrespondenceChangeAddrScreenerView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CorrespondenceChangeAddrScreenerController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: CorrespondenceDetailsDataRequiredAction,
  formProvider: CorrespondenceChangeAddrScreenerFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: CorrespondenceChangeAddrScreenerView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData) { implicit request =>

    val preparedForm = request.userAnswers.get(CorrespondenceChangeAddrScreenerPage) match {
      case None        => form
      case Some(value) => form.fill(value)
    }

    val isUkAddress =
      request.userAnswers
        .get(CorrespondenceAddressUkPage)
        .exists(_.country.isEmpty)

    Ok(view(preparedForm, mode, isUkAddress))

  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData).async { implicit request =>

    val isUkAddress =
      request.userAnswers
        .get(CorrespondenceAddressUkPage)
        .exists(_.country.isEmpty)

    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, isUkAddress))),
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(CorrespondenceChangeAddrScreenerPage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(CorrespondenceChangeAddrScreenerPage, mode, updatedAnswers))
      )
  }
}
