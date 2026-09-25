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

package controllers.partner

import controllers.actions.*
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.partner.CheckPartnerDetailsViewModel
import views.html.partner.PartnerDetailsCheckYourAnswersView

import javax.inject.Inject

class PartnerDetailsCheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  authorise: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: PartnerDetailsDataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: PartnerDetailsCheckYourAnswersView
) extends FrontendBaseController
    with I18nSupport {

  // TODO: Interim solutions

  // index -> will be refactored with the indexing ticket
  private val index: Int = utils.PartnerUtils.interimIndex
  // is new partner -> to use flag!
  private val isNewPartner: Option[Boolean] = Some(true)
  // is submitted -> to use flag!
  private val isSubmitted: Boolean = true

  def onPageLoad: Action[AnyContent] = (authorise andThen getData andThen requireData) { implicit request =>

    val model = CheckPartnerDetailsViewModel
      .from(request.userAnswers, index, isNewPartner, isSubmitted)

    Ok(view(model))
  }
}
