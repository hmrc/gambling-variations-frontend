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

import controllers.routes
import models.{BusinessTradeClass, CheckMode, UserAnswers}
import pages.{BusinessTradeClassPage, OtherTradeClassPage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object OtherTradeClassSummary {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] = {

    val tradeClassOpt = answers.get(BusinessTradeClassPage)

    tradeClassOpt match {
      case Some(BusinessTradeClass.Other) =>
        val descOpt = answers.get(OtherTradeClassPage)

        val descMissing = descOpt.forall(_.trim.isEmpty)
        if (descMissing) {
          None
        } else {
          Some(
            SummaryListRowViewModel(
              key   = "checkTradingDetails.otherBusinessTradeClassDescription.checkYourAnswersLabel",
              value = ValueViewModel(descOpt.get),
              actions = Seq(
                ActionItemViewModel(
                  "site.change",
                  routes.BusinessTradeClassController.onPageLoad(CheckMode).url
                ).withVisuallyHiddenText(
                  messages("checkTradingDetails.otherBusinessTradeClassDescription.change.hidden")
                )
              )
            )
          )
        }

      case _ =>
        None
    }
  }

}
