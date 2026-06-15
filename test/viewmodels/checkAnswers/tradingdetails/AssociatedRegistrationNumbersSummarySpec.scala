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
import pages.AssociatedRegistrationNumbersPage
import play.api.i18n.Messages
import play.api.Application

class AssociatedRegistrationNumbersSummarySpec extends SpecBase {

  private val app: Application = applicationBuilder().build()
  implicit val msgs: Messages = messages(app)

  "AssociatedRegistrationNumbersSummary.row" - {

    "return Some row with 'not provided' when no numbers exist" in {
      val result = AssociatedRegistrationNumbersSummary.row(emptyUserAnswers)

      result mustBe defined
      result.get.value.toString must include(msgs("site.notProvided"))
      result.get.actions.size mustBe 1
    }

    "show a single registration number correctly" in {
      val answers =
        emptyUserAnswers
          .set(AssociatedRegistrationNumbersPage, Seq("123456"))
          .success
          .value

      val result = AssociatedRegistrationNumbersSummary.row(answers)

      result mustBe defined
      result.get.value.toString must include("123456")
      result.get.actions.size mustBe 1
    }

    "show multiple numbers as a bullet list and include action" in {
      val answers =
        emptyUserAnswers
          .set(AssociatedRegistrationNumbersPage, Seq("123", "456"))
          .success
          .value

      val result = AssociatedRegistrationNumbersSummary.row(answers).value
      val html = result.value.content.asHtml.toString

      html must include("<ul")
      html must include("<li>123</li>")
      html must include("<li>456</li>")
      result.actions.size mustBe 1
    }

    "show multiple numbers (three items) as a bullet list and include action" in {
      val answers =
        emptyUserAnswers
          .set(AssociatedRegistrationNumbersPage, Seq("1", "2", "3"))
          .success
          .value

      val result = AssociatedRegistrationNumbersSummary.row(answers).value
      val html = result.value.content.asHtml.toString

      html must include("<ul")
      html must include("<li>1</li>")
      html must include("<li>2</li>")
      html must include("<li>3</li>")
      result.actions.size mustBe 1
    }

    "show multiple numbers (four items) as a bullet list and include action" in {
      val answers =
        emptyUserAnswers
          .set(AssociatedRegistrationNumbersPage, Seq("1", "2", "3", "4"))
          .success
          .value

      val result = AssociatedRegistrationNumbersSummary.row(answers).value
      val html = result.value.content.asHtml.toString

      html must include("<ul")
      html must include("<li>1</li>")
      html must include("<li>2</li>")
      html must include("<li>3</li>")
      html must include("<li>4</li>")
      result.actions.size mustBe 1
    }
  }
}
