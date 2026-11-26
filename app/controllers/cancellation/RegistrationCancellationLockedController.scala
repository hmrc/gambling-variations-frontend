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

import config.FrontendAppConfig
import controllers.actions.*
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.cancellation.RegistrationCancellationLockedView

import javax.inject.Inject

//TODO lock uses the same view, a single message maybe needs to be different.
//https://jira.tools.tax.service.gov.uk/browse/DTR-9343
//https://jira.tools.tax.service.gov.uk/browse/DTR-9342
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

  private val groupKey = "groupRegistrationCancellationLocked"
  private val nonGroupKey = "registrationCancellationLocked"

  def onPageLoad(): Action[AnyContent] = show(messageKey = nonGroupKey, isGroup = false)

  def onPageLoadGroup(): Action[AnyContent] = show(messageKey = groupKey, isGroup = true)

  private def show(messageKey: String, isGroup: Boolean): Action[AnyContent] = (authorise andThen getData andThen requireData) { implicit request =>
    checkScreen(request.userAnswers, CurrentScreen.Locked, isGroup)(Ok(view(appConfig.gamblingManagementHomeUrl, messageKey)))
  }
}
