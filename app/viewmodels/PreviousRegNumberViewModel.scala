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

package viewmodels

import controllers.routes
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*
import viewmodels.govuk.all.stringToText

case class PreviousRegNumberViewModel(previousRegNumbers: Option[Seq[String]]) {

  def summaryList(implicit messages: Messages): Seq[SummaryListRow] = previousRegNumberSummaryListRows

  private def previousRegNumberSummaryListRows(implicit messages: Messages): Seq[SummaryListRow] = {
    previousRegNumbers match {
      case Some(previousRegNums) =>
        val rows: Seq[SummaryListRow] = for (prevReg <- previousRegNums) yield {
          SummaryListRow(
            key = Key(content = prevReg, classes = s"previous-reg-number previous-reg-number-$prevReg govuk-!-font-weight-regular"),
            actions = Some(
              Actions(
                items = Seq(
                  ActionItem(
                    href               = "#",
                    content            = "site.change",
                    visuallyHiddenText = Some(messages("previousRegistrationNumbers.change.hidden", prevReg))
                  ),
                  ActionItem(
                    href               = routes.PreviousRegistrationNumbersController.onRedirect(prevRegNumber = prevReg).url,
                    content            = "site.remove",
                    visuallyHiddenText = Some(messages("previousRegistrationNumbers.change.hidden", prevReg))
                  )
                ),
                classes = "govuk-summary-list__actions govuk-!-width-one-half"
              )
            )
          )
        }
        rows
      case None =>
        Seq.empty
    }
  }
}
