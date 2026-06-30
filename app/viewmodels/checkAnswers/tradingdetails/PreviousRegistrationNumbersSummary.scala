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
import models.{NormalMode, UserAnswers}
import pages.PreviousRegistrationNumbersListPage
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

object PreviousRegistrationNumbersSummary {

  def row(answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    Some {

      val numbers =
        answers.get(PreviousRegistrationNumbersListPage).getOrElse(Seq.empty)

      val amount = numbers.length
      val maxAmount = 3

      val value =
        numbers match {
          case Seq() =>
            ValueViewModel(messages("site.notProvided"))

          case Seq(singleNumber) =>
            ValueViewModel(singleNumber)

          case multipleNumbers =>
            ValueViewModel(
              HtmlContent(
                HtmlFormat.raw(
                  multipleNumbers.mkString("<br/>")
                )
              )
            )
        }

      val hasNumbers = numbers.nonEmpty

      val route =
        if (hasNumbers) {
          routes.PreviousRegistrationNumbersListController.onPageLoad(NormalMode).url
        } else {
          routes.PreviousRegistrationNumberController.onPageLoad(NormalMode).url
        }

      val actions = if (amount >= maxAmount) { Seq.empty }
      else {
        Seq(
          ActionItemViewModel(
            "site.change",
            route
          ).withVisuallyHiddenText(
            messages("checkTradingDetails.previousRegistrationNumbers.change.hidden")
          )
        )
      }

      SummaryListRowViewModel(
        key     = "checkTradingDetails.previousRegistrationNumbers.checkYourAnswersLabel",
        value   = value,
        actions = actions
      )
    }
}
