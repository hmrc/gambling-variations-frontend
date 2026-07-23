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
import pages.IsSeasonalBusinessPage
import play.api.Application
import play.api.i18n.Messages
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*
import org.scalatest.matchers.must.Matchers.*
import uk.gov.hmrc.govukfrontend.views.Aliases.Text

class IsSeasonalBusinessSummarySpec extends SpecBase {

  private val app: Application = applicationBuilder().build()
  implicit val messagesApi: Messages = messages(app)

  "IsSeasonalBusinessSummary.row" - {

    "return row with 'Not Provided' when no answer is provided" in {
      val row = IsSeasonalBusinessSummary.row(emptyUserAnswers)

      row.head.value.content mustEqual Text("Not provided")
      row.head.actions.head.toString must include("Change")
    }

    "return YES when true" in {

      val answers =
        emptyUserAnswers
          .set(IsSeasonalBusinessPage, true)
          .success
          .value

      IsSeasonalBusinessSummary.row(answers) mustBe Some(
        SummaryListRowViewModel(
          key   = "checkTradingDetails.seasonalBusiness.checkYourAnswersLabel",
          value = ValueViewModel(messagesApi("site.yes")),
          actions = Seq(
            ActionItemViewModel(
              "site.change",
              routes.SeasonalBusinessController.onPageLoad(CheckMode).url
            ).withVisuallyHiddenText(
              messagesApi("checkTradingDetails.seasonalBusiness.change.hidden")
            )
          )
        )
      )
    }

    "return NO when false" in {

      val answers =
        emptyUserAnswers
          .set(IsSeasonalBusinessPage, false)
          .success
          .value

      IsSeasonalBusinessSummary.row(answers) mustBe Some(
        SummaryListRowViewModel(
          key   = "checkTradingDetails.seasonalBusiness.checkYourAnswersLabel",
          value = ValueViewModel(messagesApi("site.no")),
          actions = Seq(
            ActionItemViewModel(
              "site.change",
              routes.SeasonalBusinessController.onPageLoad(CheckMode).url
            ).withVisuallyHiddenText(
              messagesApi("checkTradingDetails.seasonalBusiness.change.hidden")
            )
          )
        )
      )
    }
  }
}
