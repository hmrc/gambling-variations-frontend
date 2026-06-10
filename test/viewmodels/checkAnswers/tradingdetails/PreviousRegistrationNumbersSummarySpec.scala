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

import base.SpecBase
import controllers.routes
import models.CheckMode
import pages.PreviousRegistrationNumbersPage
import play.api.Application
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

class PreviousRegistrationNumbersSummarySpec extends SpecBase {

  private val app: Application = applicationBuilder().build()

  implicit val msgs: Messages = messages(app)

  "PreviousRegistrationNumbersSummary.row" - {

    "must display 'not provided' when no answer exists" in {

      PreviousRegistrationNumbersSummary.row(emptyUserAnswers) mustBe Some(
        SummaryListRowViewModel(
          key   = "checkTradingDetails.previousRegistrationNumbers.checkYourAnswersLabel",
          value = ValueViewModel(msgs("site.notProvided")),
          actions = Seq(
            ActionItemViewModel(
              "site.change",
              routes.FaxNumberController.onPageLoad(CheckMode).url
            ).withVisuallyHiddenText(
              msgs("checkTradingDetails.previousRegistrationNumbers.change.hidden")
            )
          )
        )
      )
    }

    "must display 'not provided' when an empty list is supplied" in {

      val answers =
        emptyUserAnswers
          .set(PreviousRegistrationNumbersPage, Seq.empty[String])
          .success
          .value

      PreviousRegistrationNumbersSummary.row(answers) mustBe Some(
        SummaryListRowViewModel(
          key   = "checkTradingDetails.previousRegistrationNumbers.checkYourAnswersLabel",
          value = ValueViewModel(msgs("site.notProvided")),
          actions = Seq(
            ActionItemViewModel(
              "site.change",
              routes.FaxNumberController.onPageLoad(CheckMode).url
            ).withVisuallyHiddenText(
              msgs("checkTradingDetails.previousRegistrationNumbers.change.hidden")
            )
          )
        )
      )
    }

    "must display registration numbers separated by line breaks" in {

      val numbers = Seq("REG001", "REG002", "REG003")

      val answers =
        emptyUserAnswers
          .set(PreviousRegistrationNumbersPage, numbers)
          .success
          .value

      PreviousRegistrationNumbersSummary.row(answers) mustBe Some(
        SummaryListRowViewModel(
          key = "checkTradingDetails.previousRegistrationNumbers.checkYourAnswersLabel",
          value = ValueViewModel(
            HtmlContent(
              HtmlFormat.raw("REG001<br>REG002<br>REG003")
            )
          ),
          actions = Seq(
            ActionItemViewModel(
              "site.change",
              routes.FaxNumberController.onPageLoad(CheckMode).url
            ).withVisuallyHiddenText(
              msgs("checkTradingDetails.previousRegistrationNumbers.change.hidden")
            )
          )
        )
      )
    }
  }
}
