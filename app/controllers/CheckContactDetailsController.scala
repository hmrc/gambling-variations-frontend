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
import pages.{BusinessContactNumberPage, BusinessEmailAddressPage, FaxNumberPage, IsFlaggedPage}

import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.BusinessContactDetailsView

class CheckContactDetailsController @Inject() (
  override val messagesApi: MessagesApi,
  authorised: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: BusinessContactDetailsView
) extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = (authorised andThen getData andThen requireData) { implicit request =>
    val flag = request.userAnswers.get(IsFlaggedPage).getOrElse(false)
    val contactDetailsView: Option[Result] = for {
      businessContactNumber <- request.userAnswers.get(BusinessContactNumberPage)
      faxNumber             <- request.userAnswers.get(FaxNumberPage)
      businessEmailAddress  <- request.userAnswers.get(BusinessEmailAddressPage)
    } yield {
      Ok(
        view(businessContactNumber.phoneNumber.getOrElse(""), businessContactNumber.mobileNumber.getOrElse(""), faxNumber, businessEmailAddress, flag)
      )
    }
    contactDetailsView getOrElse Redirect(routes.SystemErrorController.onPageLoad())
  }
}
