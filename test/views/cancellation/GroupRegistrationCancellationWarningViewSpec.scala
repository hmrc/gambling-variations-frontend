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

package views.cancellation

import base.SpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.test.FakeRequest
import views.html.cancellation.GroupRegistrationCancellationWarningView

class GroupRegistrationCancellationWarningViewSpec extends SpecBase {

  trait Setup {

    private val app = applicationBuilder().build()

    val view =
      app.injector.instanceOf[GroupRegistrationCancellationWarningView]

    val request: play.api.mvc.Request[?] = FakeRequest()

    val gamblingManagementHomeUrl = "http://localhost:10400/manage-gambling-tax/manage-your-machine-games-duty/"

    val messages: Messages =
      app.injector
        .instanceOf[play.api.i18n.MessagesApi]
        .preferred(request)
  }

  "GroupRegistrationCancellationWarningView" - {

    "must render page correctly" in new Setup {

      val html =
        view(gamblingManagementHomeUrl)(request, messages)

      val doc: Document =
        Jsoup.parse(html.body)

      doc.title must include(
        messages("groupRegistrationCancellationWarning.title")
      )

      doc
        .select(".govuk-caption-l")
        .text mustBe messages(
        "groupRegistrationCancellationWarning.caption"
      )

      doc
        .select("h1.govuk-heading-l")
        .text mustBe messages(
        "groupRegistrationCancellationWarning.heading"
      )

      doc
        .select("p.govuk-body")
        .first()
        .text() mustBe messages(
        "groupRegistrationCancellationWarning.p1"
      )

      val linkContainer =
        doc.select("p.change-registration-page-link").first()

      linkContainer must not be null

      linkContainer.hasClass("govuk-!-margin-bottom-8") mustBe true

      val link = linkContainer.select("a.govuk-link").first()

      link must not be null

      link.text() mustBe messages(
        "groupRegistrationCancellationWarning.link"
      )

      link.attr("href") mustBe gamblingManagementHomeUrl

      doc
        .select(".govuk-error-summary")
        .size() mustEqual 0
    }
  }
}
