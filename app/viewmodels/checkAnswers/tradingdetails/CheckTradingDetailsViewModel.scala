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

import models.{BusinessTradeClass, UserAnswers}
import pages.{BusinessTradeClassPage, OtherTradeClassPage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import viewmodels.govuk.all.ValueViewModel
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

    if (isGroupMember) {
      val seasonalOnly =
        Seq(IsSeasonalBusinessSummary.row(userAnswers)).flatten

      return CheckTradingDetailsViewModel(
        list          = SummaryListViewModel(seasonalOnly),
        previousMgd   = SummaryListViewModel(Nil),
        associatedMgd = SummaryListViewModel(Nil)
      )
    }

    val tradeClassRow =
      BusinessTradeClassSummary.row(userAnswers).map { row =>
        val tc = userAnswers.get(BusinessTradeClassPage)

        val fixedValue =
          tc match {
            case None => messages("site.notProvided")
            case Some(BusinessTradeClass.Other) =>
              messages("businessTradeClass.other")
            case Some(value) =>
              messages(s"businessTradeClass.$value")
          }

        row.copy(value = ValueViewModel(Text(fixedValue)))
      }

    val otherTradeClassRow =
      OtherTradeClassSummary.row(userAnswers).map { row =>
        val descOpt = userAnswers.get(OtherTradeClassPage)

        val fixedValue: String = descOpt.filter(_.nonEmpty).getOrElse(messages("site.notProvided"))

        row.copy(value = ValueViewModel(Text(fixedValue)))
      }

    val seasonalRow =
      IsSeasonalBusinessSummary.row(userAnswers)

    val tradeClassRows =
      Seq(tradeClassRow, otherTradeClassRow, seasonalRow).flatten

    val previousMgdRows =
      Seq(PreviousRegistrationNumbersSummary.row(userAnswers)).flatten

    val associatedMgdRows =
      Seq(AssociatedRegistrationNumbersSummary.row(userAnswers)).flatten

    CheckTradingDetailsViewModel(
      list          = SummaryListViewModel(tradeClassRows),
      previousMgd   = SummaryListViewModel(previousMgdRows),
      associatedMgd = SummaryListViewModel(associatedMgdRows)
    )
  }
}
