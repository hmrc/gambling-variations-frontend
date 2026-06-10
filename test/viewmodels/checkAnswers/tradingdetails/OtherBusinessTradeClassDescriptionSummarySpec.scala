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
import pages.OtherBusinessTradeClassDescriptionPage
import play.api.Application
import play.api.i18n.Messages
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

class OtherBusinessTradeClassDescriptionSummarySpec extends SpecBase {

  private val app: Application = applicationBuilder().build()

  implicit val msgs: Messages = messages(app)

  "OtherBusinessTradeClassDescriptionSummary.row" - {

    "must return None when no answer exists" in {

      OtherBusinessTradeClassDescriptionSummary.row(emptyUserAnswers) mustBe None
    }

    "must return a row when an answer exists" in {

      val description = "Mobile gaming arcade operator"

      val answers =
        emptyUserAnswers
          .set(OtherBusinessTradeClassDescriptionPage, description)
          .success
          .value

      OtherBusinessTradeClassDescriptionSummary.row(answers) mustBe Some(
        SummaryListRowViewModel(
          key   = "checkTradingDetails.otherBusinessTradeClassDescription.checkYourAnswersLabel",
          value = ValueViewModel(description),
          actions = Seq(
            ActionItemViewModel(
              "site.change",
              routes.FaxNumberController.onPageLoad(CheckMode).url
            ).withVisuallyHiddenText(
              msgs("checkTradingDetails.otherBusinessTradeClassDescription.change.hidden")
            )
          )
        )
      )
    }
  }
}
