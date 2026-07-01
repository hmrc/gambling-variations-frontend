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
import models.{BusinessTradeClass, CheckMode}
import pages.BusinessTradeClassPage
import play.api.i18n.Messages
import play.api.Application
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

class BusinessTradeClassSummarySpec extends SpecBase {

  private val app: Application = applicationBuilder().build()
  implicit val msgs: Messages = messages(app)

  "BusinessTradeClassSummary.row" - {

    "return 'Not Provided' when the question has not been answered" in {
      val row = BusinessTradeClassSummary.row(emptyUserAnswers)
      row.value.value.content.asHtml.toString must include("Not Provided")
    }


    "return the correct row when the question has been answered" in {

      val answers =
        emptyUserAnswers
          .set(BusinessTradeClassPage, BusinessTradeClass.Casino)
          .success
          .value

      BusinessTradeClassSummary.row(answers) mustBe Some(
        SummaryListRowViewModel(
          key = "checkTradingDetails.businessTradeClass.checkYourAnswersLabel",
          value = ValueViewModel(
            msgs("businessTradeClass.casino")
          ),
          actions = Seq(
            ActionItemViewModel(
              "site.change",
              routes.BusinessTradeClassController.onPageLoad(CheckMode).url
            ).withVisuallyHiddenText(
              msgs("checkTradingDetails.businessTradeClass.change.hidden")
            )
          )
        )
      )
    }
  }
}
