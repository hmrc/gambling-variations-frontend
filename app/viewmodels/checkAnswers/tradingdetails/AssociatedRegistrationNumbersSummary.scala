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
import models.{CheckMode, UserAnswers}
import pages.AssociatedRegistrationNumbersPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object AssociatedRegistrationNumbersSummary {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    Some {

      val numbers =
        answers.get(AssociatedRegistrationNumbersPage).getOrElse(Seq.empty)

      val value = numbers.size match {

        case 0 =>
          ValueViewModel(messages("site.notProvided"))

        case 1 =>
          ValueViewModel(numbers.head)

        case 2 | 3 =>
          ValueViewModel(
            HtmlContent(
              HtmlFormat.raw(
                s"""<ul class="govuk-list govuk-list--bullet">
                   |${numbers.map(n => s"<li>$n</li>").mkString}
                   |</ul>""".stripMargin
              )
            )
          )

        case _ =>
          ValueViewModel(numbers.mkString(", "))
      }

      val actions = Seq(
        ActionItemViewModel(
          "site.change",
          routes.BusinessTradeClassController.onPageLoad(CheckMode).url
        ).withVisuallyHiddenText(
          messages("checkTradingDetails.associatedRegistrationNumbers.change.hidden")
        )
      )

      SummaryListRowViewModel(
        key     = "checkTradingDetails.associatedRegistrationNumbers.checkYourAnswersLabel",
        value   = value,
        actions = actions
      )
    }
}
