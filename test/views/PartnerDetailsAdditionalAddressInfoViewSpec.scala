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

package views

import base.SpecBase
import forms.PartnerDetailsAdditionalAddressInfoFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.test.FakeRequest
import views.html.PartnerDetailsAdditionalAddressInfoView

class PartnerDetailsAdditionalAddressInfoViewSpec extends SpecBase {

  trait Setup:
    private val app = applicationBuilder().build()

    val view = app.injector.instanceOf[PartnerDetailsAdditionalAddressInfoView]

    val formProvider = new PartnerDetailsAdditionalAddressInfoFormProvider()

    val form = formProvider()

    val request: play.api.mvc.Request[?] = FakeRequest()

    val messages: Messages =
      app.injector
        .instanceOf[play.api.i18n.MessagesApi]
        .preferred(request)

  "PartnerDetailsAdditionalAddressInfoView" - {
    "must render page correctly" in new Setup {

      val html = view(form, NormalMode)(request, messages)
      val doc: Document = Jsoup.parse(html.body)

      doc.title must include(messages("partnerDetailsAdditionalAddressInfo.title"))

      doc
        .select(".govuk-caption-l")
        .text() must include(messages("changeRegistrationDetails.caption"))

      doc
        .select(".govuk-label-wrapper")
        .select(".govuk-label--l")
        .text mustBe messages("partnerDetailsAdditionalAddressInfo.heading")

      doc.select(".govuk-hint").text must include(
        messages("partnerDetailsAdditionalAddressInfo.hint")
      )

      doc.select("input[name=partnerDetailsAdditionalAddressInfo]").size() mustEqual 1

      doc.select("button.govuk-button").text must include(messages("site.continue"))

      doc.select(".govuk-error-summary").size() mustEqual 0
    }

    "must render error summary when form has errors" in new Setup {

      val boundForm = form.bind(Map("partnerDetailsAdditionalAddressInfo" -> ""))

      val html = view(boundForm, NormalMode)(request, messages)
      val doc: Document = Jsoup.parse(html.body)

      doc.select(".govuk-error-summary").size() mustEqual 1

      doc.select(".govuk-error-summary").text must include(
        messages("partnerDetailsAdditionalAddressInfo.error.required")
      )
    }

    "must render error message when input is too long" in new Setup {

      val maxFieldLength = 100;
      val boundForm =
        form.bind(Map("partnerDetailsAdditionalAddressInfo" -> "a" * (maxFieldLength + 1)))

      val html = view(boundForm, NormalMode)(request, messages)
      val doc: Document = Jsoup.parse(html.body)

      doc.select(".govuk-error-message").text must include(
        messages("partnerDetailsAdditionalAddressInfo.error.length")
      )
    }

  }
}
