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
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{Content, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*
import viewmodels.govuk.all.stringToText

case class PreviousRegNumberViewModel(previousRegNumbers: Option[Seq[String]], unsubmittedPreviousRegNumbers: Option[Seq[String]]) {

  def unsubmittedRegNumbersSummaryListRows(implicit messages: Messages): Seq[SummaryListRow] = {

    unsubmittedPreviousRegNumbers match {
      case Some(newPrevRegNumbers) =>
        for (newPrevReg <- newPrevRegNumbers) yield {
          SummaryListRow(
            key = Key(content = Text(newPrevReg), classes = s"previous-reg-number previous-reg-number-$newPrevReg govuk-!-font-weight-regular"),
            actions = Some(
              Actions(
                classes = "govuk-summary-list__actions govuk-!-width-one-half",
                items = Seq(
                  ActionItem(
                    href               = "#",
                    content            = Text(messages("site.change")),
                    visuallyHiddenText = Some(messages("previousRegistrationNumbers.change.hidden", newPrevReg))
                  ),
                  ActionItem(
                    href               = routes.PreviousRegistrationNumbersController.onRedirect(prevRegNumber = newPrevReg).url,
                    content            = Text(messages("site.remove")),
                    visuallyHiddenText = Some(messages("previousRegistrationNumbers.change.hidden", newPrevReg))
                  )
                )
              )
            )
          )
        }
      case None => Seq.empty
    }
  }

  def submittedRegNumbersSummaryListRows(implicit messages: Messages): Seq[SummaryListRow] = {
    previousRegNumbers match {
      case Some(previousRegNums) =>
        val rows: Seq[SummaryListRow] = for (prevReg <- previousRegNums) yield {
          SummaryListRow(
            key     = Key(content = prevReg, classes = s"previous-reg-number previous-reg-number-$prevReg govuk-!-font-weight-regular"),
            actions = Some(Actions(classes = "govuk-summary-list__actions govuk-!-width-one-half"))
          )
        }
        rows ++ unsubmittedRegNumbersSummaryListRows
      case None =>
        Seq.empty
    }
  }

}
