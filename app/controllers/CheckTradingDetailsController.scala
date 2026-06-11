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
import pages.GroupMemberPage

import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.*
import viewmodels.checkAnswers.tradingdetails.*
import viewmodels.govuk.summarylist.*
import views.html.CheckTradingDetailsView

class CheckTradingDetailsController @Inject() (
  override val messagesApi: MessagesApi,
  authorised: AuthorisedAction,
  getData: DataRetrievalAction,
  mgdTradDetailsRequireData: MgdTradeDetailsDataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  view: CheckTradingDetailsView
) extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] =
    (authorised andThen getData andThen mgdTradDetailsRequireData) { implicit request =>

      val isGroupMember =
        request.userAnswers.get(GroupMemberPage).getOrElse(false)

      val tradeClassRows =
        if (isGroupMember) {
          Nil
        } else {
          Seq(
            BusinessTradeClassSummary.row(request.userAnswers),
            OtherTradeClassSummary.row(request.userAnswers)
          ).flatten
        }

      val listTradClass = SummaryListViewModel(tradeClassRows)

      val list = SummaryListViewModel(
        rows = Seq(
          IsSeasonalBusinessSummary.row(request.userAnswers)
        ).flatten
      )

      val previousMgdRows =
        if (isGroupMember) {
          Nil
        } else {
          Seq(
            PreviousRegistrationNumbersSummary.row(request.userAnswers)
          ).flatten
        }

      val associatedMgdRows =
        if (isGroupMember) {
          Nil
        } else {
          Seq(
            AssociatedRegistrationNumbersSummary.row(request.userAnswers)
          ).flatten
        }

      val listPreviousMgdRegNo =
        SummaryListViewModel(previousMgdRows)

      val listAssociatedMgdRegNo =
        SummaryListViewModel(associatedMgdRows)

      Ok(view(listTradClass, list, listPreviousMgdRegNo, listAssociatedMgdRegNo))
    }
}
