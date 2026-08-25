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
import forms.partner.PartnerDetailsAddNationalInsuranceNumberFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.partner.PartnerDetailsAddNationalInsuranceNumberView

class PartnerDetailsAddNationalInsuranceNumberViewSpec extends SpecBase {

  trait Setup {
    private val app = applicationBuilder().build()

    val view: PartnerDetailsAddNationalInsuranceNumberView =
      app.injector.instanceOf[PartnerDetailsAddNationalInsuranceNumberView]

    val form: Form[String] = (new PartnerDetailsAddNationalInsuranceNumberFormProvider())()

    val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

    val messages: Messages =
      app.injector
        .instanceOf[play.api.i18n.MessagesApi]
        .preferred(request)
  }

  "PartnerDetailsAddNationalInsuranceNumberView" - {

    "must render page correctly" in new Setup {

      val html: HtmlFormat.Appendable = view(form, NormalMode)(request, messages)
      val doc: Document = Jsoup.parse(html.body)

      doc.title must include(
        messages("partnerDetailsAddNino.title")
      )

      doc
        .select("span")
        .select(".govuk-caption-l")
        .text() mustEqual messages("changeRegistrationDetails.caption")

      doc
        .select(".govuk-fieldset__legend--l")
        .text mustBe messages("partnerDetailsAddNino.heading")

      doc
        .select("label[for=value].govuk-visually-hidden")
        .text() mustBe messages("partnerDetailsAddNino.heading")

      doc
        .select(".govuk-hint")
        .text mustBe messages("partnerDetailsAddNino.hint")

      doc
        .select("input[name=value]")
        .size() mustEqual 1

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

      val boundForm: Form[String] = form.bind(Map("value" -> ""))

      val html: HtmlFormat.Appendable = view(boundForm, NormalMode)(request, messages)
      val doc: Document = Jsoup.parse(html.body)

      doc
        .select(".govuk-error-summary")
        .size() mustEqual 1

      doc
        .select(".govuk-error-summary")
        .text must include(
        messages("partnerDetailsAddNino.error.required")
      )
    }

    "must render error message when input fails length check" in new Setup {

      val tooLongInput = "SR123A"

      val boundForm: Form[String] = form.bind(Map("value" -> tooLongInput))

      val html: HtmlFormat.Appendable = view(boundForm, NormalMode)(request, messages)
      val doc: Document = Jsoup.parse(html.body)

      doc
        .select(".govuk-error-message")
        .text must include(
        messages("partnerDetailsAddNino.error.length")
      )
    }

    "must render error message when National Insurance number format is invalid" in new Setup {

      val invalidFormatInput = "QQAAAAAAA"

      val boundForm: Form[String] = form.bind(Map("value" -> invalidFormatInput))

      val html: HtmlFormat.Appendable = view(boundForm, NormalMode)(request, messages)
      val doc: Document = Jsoup.parse(html.body)

      doc
        .select(".govuk-error-message")
        .text must include(
        messages("partnerDetailsAddNino.error.invalidFormat")
      )
    }

    "must render error message when prefix contains invalid characters" in new Setup {

      val invalidCharsInput = "SR123456A!"

      val boundForm: Form[String] = form.bind(Map("value" -> invalidCharsInput))

      val html: HtmlFormat.Appendable = view(boundForm, NormalMode)(request, messages)
      val doc: Document = Jsoup.parse(html.body)

      doc
        .select(".govuk-error-message")
        .text must include(
        messages("partnerDetailsAddNino.error.invalidChars")
      )
    }

    "must render error message when National Insurance number is an administrative or disallowed prefix" in new Setup {

      val disallowedPrefixInput = "DD123456A"

      val boundForm: Form[String] = form.bind(Map("value" -> disallowedPrefixInput))

      val html: HtmlFormat.Appendable = view(boundForm, NormalMode)(request, messages)
      val doc: Document = Jsoup.parse(html.body)

      doc
        .select(".govuk-error-message")
        .text must include(
        messages("partnerDetailsAddNino.error.invalid")
      )
    }
  }
}
