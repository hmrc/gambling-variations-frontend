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

package views.partner

import base.SpecBase
import forms.partner.PartnerEmailAddressFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.test.FakeRequest
import views.html.partner.PartnerEmailAddressView

class PartnerEmailAddressViewSpec extends SpecBase {

  trait Setup {
    private val app = applicationBuilder().build()

    val view = app.injector.instanceOf[PartnerEmailAddressView]

    val formProvider = new PartnerEmailAddressFormProvider()

    val form = formProvider("partnerEmailAddress")

    val request: play.api.mvc.Request[?] = FakeRequest()

    val messages: Messages =
      app.injector
        .instanceOf[play.api.i18n.MessagesApi]
        .preferred(request)
  }

  "PartnerEmailAddressView" - {

    "must render page correctly" in new Setup {

      val html = view(form, NormalMode)(request, messages)
      val doc: Document = Jsoup.parse(html.body)

      doc.title must include(
        messages("partnerEmailAddress.title")
      )

      doc
        .select(".govuk-caption-l")
        .text() must include(
        messages("changeRegistrationDetails.caption")
      )

      doc
        .select(".govuk-label-wrapper")
        .select(".govuk-label--l")
        .text mustBe messages("partnerEmailAddress.heading")

      doc
        .select("input[name=partnerEmailAddress]")
        .size() mustEqual 1

      doc
        .select("input[name=partnerEmailAddress]")
        .attr("type") mustBe "email"

      doc
        .select("input[name=partnerEmailAddress]")
        .attr("autocomplete") mustBe "email"

      doc
        .select("button.govuk-button")
        .text must include(
        messages("site.continue")
      )

      doc
        .select(".govuk-error-summary")
        .size() mustEqual 0
    }

    "must render error summary when form has errors" in new Setup {

      val boundForm =
        form.bind(Map("partnerEmailAddress" -> ""))

      val html = view(boundForm, NormalMode)(request, messages)
      val doc: Document = Jsoup.parse(html.body)

      doc
        .select(".govuk-error-summary")
        .size() mustEqual 1

      doc
        .select(".govuk-error-summary")
        .text must include(
        messages("partnerEmailAddress.error.required")
      )
    }

    "must render error message when email address is invalid" in new Setup {

      val boundForm =
        form.bind(
          Map("partnerEmailAddress" -> "invalid-email")
        )

      val html = view(boundForm, NormalMode)(request, messages)
      val doc: Document = Jsoup.parse(html.body)

      doc
        .select(".govuk-error-message")
        .text must include(
        messages("partnerEmailAddress.error.invalid")
      )
    }

    "must render error message when email address is too long" in new Setup {

      val maxFieldLength = 70

      val tooLong =
        ("a" * (maxFieldLength - 8)) + "@test.com"

      val boundForm =
        form.bind(
          Map("partnerEmailAddress" -> tooLong)
        )

      val html = view(boundForm, NormalMode)(request, messages)
      val doc: Document = Jsoup.parse(html.body)

      doc
        .select(".govuk-error-message")
        .text must include(
        messages("partnerEmailAddress.error.length")
      )
    }
  }
}
