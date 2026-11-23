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

package controllers.cancellation

import controllers.actions.*
import models.Mode
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.cancellation.RegistrationCancellationLockedView

import javax.inject.Inject

class RegistrationCancellationLockedController @Inject() (
  override val messagesApi: MessagesApi,
  appConfig: FrontendAppConfig,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: BusinessDetailsDataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: RegistrationCancellationLockedView
) extends FrontendBaseController
    with CancellationEntrypointDecider
    with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData) { implicit request =>
    checkScreen(request.userAnswers, CurrentScreen.Locked)(Ok(view(mode, appConfig.gamblingManagementHomeUrl)))
  }
}
