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

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Actions, Key, SummaryListRow}

case class PreviousRegNumbersViewModel(previousRegNumbers: Option[Seq[String]]) {

  def summaryList(implicit messages: Messages): Seq[SummaryListRow] = {
    previousRegNumbers match {
      case Some(previousRegNums) =>
        val rows: Seq[SummaryListRow] = for (prevReg <- previousRegNums) yield {
          SummaryListRow(
            key     = Key(content = Text(prevReg), classes = s"previous-reg-number previous-reg-number-$prevReg govuk-!-font-weight-regular"),
            actions = Some(Actions(classes = "govuk-summary-list__actions govuk-!-width-one-half"))
          )
        }
        rows
      case None =>
        Seq.empty
    }
  }

}
