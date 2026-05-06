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
import forms.RemoveTradeNameFormProvider
import models.{Mode, UserAnswers}
import navigation.Navigator
import pages.{RemoveTradeNamePage, TradingNamePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.RemoveTradeNameView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class RemoveTradeNameController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  formProvider: RemoveTradeNameFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: RemoveTradeNameView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData) { implicit request =>
    request.userAnswers.get(TradingNamePage) map { tradingName =>
      Ok(view(form, mode, tradingName))
    } getOrElse Redirect(routes.CheckBusinessNameController.onPageLoad())
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData) async { implicit request =>

    request.userAnswers.get(TradingNamePage) map { tradingName =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, tradingName))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(updateUserAnswers(request.userAnswers, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(RemoveTradeNamePage, mode, updatedAnswers))
        )
    } getOrElse Future.successful(Redirect(routes.CheckBusinessNameController.onPageLoad()))
  }

  private def updateUserAnswers(userAnswers: UserAnswers, value: Boolean): Try[UserAnswers] = {
    for {
      ua1 <- userAnswers.set(RemoveTradeNamePage, value)
      ua2 <- {
        if (value) {
          ua1.remove(TradingNamePage)
        } else {
          Try(ua1)
        }
      }
    } yield ua2
  }

}
