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

import controllers.actions.{AuthorisedAction, DataRequiredAction, DataRetrievalAction}
import models.{BusinessNameDetails, BusinessType, SoleProprietorDetails}
import pages.{BusinessDetailsPage, SoleProprietorPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.BusinessNameView

import javax.inject.Inject

class CheckBusinessNameController @Inject() (
  override val messagesApi: MessagesApi,
  val controllerComponents: MessagesControllerComponents,
  authorised: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  view: BusinessNameView
) extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = (authorised andThen getData andThen requireData) { implicit request =>
    val businessNameDetails: Option[BusinessNameDetails] = request.userAnswers.get(BusinessDetailsPage)
    val soleProprietorName: Option[SoleProprietorDetails] = request.userAnswers.get(SoleProprietorPage)

    businessNameDetails map { business =>
      Ok(view(business.businessType.toString, business.businessName, business.tradingName))
    } orElse soleProprietorName.map { soleProprietor =>
      Ok(view(BusinessType.Soleproprietor.toString, soleProprietor.fullName, soleProprietor.tradingName))
    } getOrElse Redirect(routes.SystemErrorController.onPageLoad())
  }
}
