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

package controllers.partnerdetails

import controllers.actions.*
import models.Mode
import pages.BusinessNumberOrIndex
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.PartnerUtils
import viewmodels.checkAnswers.partnerdetails.CheckPartnerDetailsViewModel
import views.html.partnerdetails.PartnerDetailsCheckYourAnswersView

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
  // is new partner -> to use flag!
  private val isNewPartner: Option[Boolean] = Some(false)
  // is submitted -> to use flag!
  private val isSubmitted: Boolean = false

  def onPageLoad(index: String, mode: Mode): Action[AnyContent] = (authorise andThen getData andThen requireData) { implicit request =>
    val newIndex: BusinessNumberOrIndex = PartnerUtils.parseIndex(index, mode)

    val model: CheckPartnerDetailsViewModel = CheckPartnerDetailsViewModel
      .from(request.userAnswers, newIndex, isNewPartner, isSubmitted)

    Ok(view(model, index, mode))
  }
}
