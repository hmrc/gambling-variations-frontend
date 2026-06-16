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

package viewmodels.checkAnswers.tradingdetails

import models.UserAnswers
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.govuk.summarylist.SummaryListViewModel

case class CheckTradingDetailsViewModel(
  list: SummaryList,
  previousMgd: SummaryList,
  associatedMgd: SummaryList
)

object CheckTradingDetailsViewModel {

  def from(
    userAnswers: UserAnswers,
    isGroupMember: Boolean
  )(implicit messages: Messages): CheckTradingDetailsViewModel = {

    val tradeClassRows =
      (
        if (isGroupMember) Nil
        else
          Seq(
            BusinessTradeClassSummary.row(userAnswers),
            OtherTradeClassSummary.row(userAnswers)
          ).flatten
      ) ++ Seq(
        IsSeasonalBusinessSummary.row(userAnswers)
      ).flatten

    val previousMgdRows =
      if (isGroupMember) Nil
      else
        Seq(
          PreviousRegistrationNumbersSummary.row(userAnswers)
        ).flatten

    val associatedMgdRows =
      if (isGroupMember) Nil
      else
        Seq(
          AssociatedRegistrationNumbersSummary.row(userAnswers)
        ).flatten

    CheckTradingDetailsViewModel(
      list          = SummaryListViewModel(tradeClassRows),
      previousMgd   = SummaryListViewModel(previousMgdRows),
      associatedMgd = SummaryListViewModel(associatedMgdRows)
    )
  }
}
