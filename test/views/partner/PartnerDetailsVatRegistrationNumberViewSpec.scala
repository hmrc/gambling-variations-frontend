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
import forms.partner.PartnerDetailsVatRegistrationNumberFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import views.html.partner.PartnerDetailsVatRegistrationNumberView

class PartnerDetailsVatRegistrationNumberViewSpec extends SpecBase {

  trait Setup {
    private val app = applicationBuilder().build()

    val view: PartnerDetailsVatRegistrationNumberView =
      app.injector.instanceOf[PartnerDetailsVatRegistrationNumberView]

    val form: Form[String] = (new PartnerDetailsVatRegistrationNumberFormProvider())()

    val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

    val messages: Messages =
      app.injector.instanceOf[MessagesApi].preferred(request)

    def render(f: Form[String]): Document =
      Jsoup.parse(view(f, NormalMode)(request, messages).body)
  }

  private val fieldName = "partnerDetailsVatRegistrationNumber"

  "PartnerDetailsVatRegistrationNumberView" - {

    "must render the page correctly" in new Setup {

      val doc: Document = render(form)

      // text
      doc.title must include("What is the partner’s VAT registration number?")

      doc.select(".govuk-caption-l").text mustEqual "Change registration details"

      doc.select(".govuk-fieldset__legend--l").text mustBe "What is the partner’s VAT registration number?"

      doc.select(s"label[for=$fieldName].govuk-visually-hidden").text mustBe
        "What is the partner’s VAT registration number?"

      doc.select(".govuk-body").text must include("You can find it on their VAT registration certificate.")

      doc.select(".govuk-hint").text mustBe
        messages("It must be 9 digits long, and may have GB at the start, for example 123456789 or GB123456789")

      doc.select("button.govuk-button").text must include(messages("site.continue"))

      doc.select(".govuk-error-summary").size() mustEqual 0
    }

    "must render the input with the name the form provider binds" in new Setup {

      val doc: Document = render(form)
      doc.select(s"input[name=$fieldName]").size() mustEqual 1
    }

    "must post to the controller's onSubmit action" in new Setup {
      val doc: Document = render(form)

      doc.select("form").attr("action") mustEqual
        controllers.partner.routes.PartnerDetailsVatRegistrationNumberController.onSubmit().url
    }

    "must pre-populate the input from the form, keeping the value verbatim" in new Setup {

      val doc: Document = render(form.fill("GB353868127"))

      doc.select(s"input[name=$fieldName]").attr("value") mustEqual "GB353868127"
    }

    "must render the error summary and field error when the value is missing" in new Setup {

      val doc: Document = render(form.bind(Map(fieldName -> "")))

      doc.select(".govuk-error-summary").size() mustEqual 1
      doc.select(".govuk-error-summary").text must include("Enter the partner's VAT registration number")
      doc.select(".govuk-error-message").text must include("Enter the partner's VAT registration number")
    }

    "must render the length error for fewer than 9 characters" in new Setup {

      val doc: Document = render(form.bind(Map(fieldName -> "12345678")))

      doc.select(".govuk-error-message").text must include(
        "The VAT registration number must be 9 characters"
      )
    }

    "must render the characters error for a non 1-9 character" in new Setup {

      val doc: Document = render(form.bind(Map(fieldName -> "3538X8127")))

      doc.select(".govuk-error-message").text must include(
        "The VAT registration number must only include numbers 1 to 9"
      )
    }

    "must render the real-VAT number error when the checksum fails" in new Setup {

      // 353868127 is valid; 353868128 fails both the mod-97 and the total + 55 fallback checks
      val doc: Document = render(form.bind(Map(fieldName -> "353868128")))

      doc.select(".govuk-error-message").text must include("Enter a real VAT registration number")
    }
  }

}

