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
import pages.PreviousRegistrationNumbersPage
import play.api.Application
import play.api.i18n.Messages

class PreviousRegistrationNumbersSummarySpec extends SpecBase {

  private val app: Application = applicationBuilder().build()
  implicit val msgs: Messages = messages(app)

  "PreviousRegistrationNumbersSummary.row" - {

    "must display 'not provided' when no answer exists" in {

      val result = PreviousRegistrationNumbersSummary.row(emptyUserAnswers)

      result mustBe defined
      result.get.value.toString must include(msgs("site.notProvided"))
      result.get.actions.get.items.size mustBe 1
    }

    "must display 'not provided' when an empty list is supplied" in {

      val answers =
        emptyUserAnswers
          .set(PreviousRegistrationNumbersPage, Seq.empty[String])
          .success
          .value

      val result = PreviousRegistrationNumbersSummary.row(answers)

      result mustBe defined
      result.get.value.toString must include(msgs("site.notProvided"))
      result.get.actions.get.items.size mustBe 1
    }

    "must display registration numbers separated by <br> and include action when less than 3 numbers" in {

      val numbers = Seq("REG001", "REG002")

      val answers =
        emptyUserAnswers
          .set(PreviousRegistrationNumbersPage, numbers)
          .success
          .value

      val result = PreviousRegistrationNumbersSummary.row(answers).value
      val html = result.value.content.asHtml.toString

      html must include("REG001")
      html must include("REG002")
      html must include("<br/>")

      result.actions.get.items.size mustBe 1
    }

    "must display registration numbers separated by <br> and NOT include action when 3 or more numbers" in {

      val numbers = Seq("REG001", "REG002", "REG003")

      val answers =
        emptyUserAnswers
          .set(PreviousRegistrationNumbersPage, numbers)
          .success
          .value

      val result = PreviousRegistrationNumbersSummary.row(answers).value
      val html = result.value.content.asHtml.toString

      html must include("REG001")
      html must include("REG002")
      html must include("REG003")
      html must include("<br/>")

      result.actions.get.items mustBe empty
    }
  }
}
