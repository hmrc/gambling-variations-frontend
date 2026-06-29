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
import pages.{BusinessContactDetailsSubmittedPage, BusinessContactNumberPage, BusinessEmailAddressPage, BusinessFaxNumberPage, ContactDetailsChangesPage}

import javax.inject.Inject
import utils.FlagsUtil.checkFlag
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.CheckContactDetailsView

class CheckContactDetailsController @Inject() (
  override val messagesApi: MessagesApi,
  authorised: AuthorisedAction,
  getData: DataRetrievalAction,
  requireData: BusinessContactDetailsDataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: CheckContactDetailsView
) extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = (authorised andThen getData andThen requireData) { implicit request =>
    val ua = request.userAnswers

    val flag = checkFlag(ua, ContactDetailsChangesPage, BusinessContactDetailsSubmittedPage)
    Ok(
      view(
        ua.get(BusinessContactNumberPage).flatMap(_.phoneNumber),
        ua.get(BusinessContactNumberPage).flatMap(_.mobilePhoneNumber),
        ua.get(BusinessFaxNumberPage),
        ua.get(BusinessEmailAddressPage),
        flag
      )
    )
  }
}
