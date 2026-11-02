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

package views.controllingbody

import base.SpecBase
import forms.controllingbody.ControllingBodyVatRegistrationNumberFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import views.html.controllingbody.ControllingBodyVatRegistrationNumberView

class ControllingBodyVatRegistrationNumberViewSpec extends SpecBase {

  trait Setup {
    private val app = applicationBuilder().build()

    val view: ControllingBodyVatRegistrationNumberView =
      app.injector.instanceOf[ControllingBodyVatRegistrationNumberView]

    val form: Form[String] = (new ControllingBodyVatRegistrationNumberFormProvider())()

    val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

    val messages: Messages =
      app.injector.instanceOf[MessagesApi].preferred(request)

    def render(f: Form[String]): Document =
      Jsoup.parse(view(f, NormalMode)(request, messages).body)
  }

  private val fieldName = "value"

  "ControllingBodyVatRegistrationNumberView" - {

    "must render the page correctly" in new Setup {

      val doc: Document = render(form)

      doc.title mustEqual
        s"${messages("controllingBodyVatRegNumber.title")} - ${messages("changeRegistrationDetails.caption")} - ${messages("service.name")} - GOV.UK"

      doc.select(".govuk-caption-l").text mustEqual messages("changeRegistrationDetails.caption")

      doc.select("h1.govuk-heading-l").text mustBe messages("controllingBodyVatRegNumber.heading")

      doc.select("p.govuk-body").text must include(messages("controllingBodyVatRegNumber.paragraph"))

      doc.select(s"label[for=$fieldName].govuk-visually-hidden").text mustBe
        messages("controllingBodyVatRegNumber.heading")

      doc.select(".govuk-hint").text mustBe messages("controllingBodyVatRegNumber.hint")

      doc.select("button.govuk-button").text must include(messages("site.continue"))

      doc.select(".govuk-error-summary").size() mustEqual 0
    }

    "must render a 10-character-wide input with the name the form provider binds" in new Setup {

      val input = render(form).select(s"input[name=$fieldName]")

      input.size() mustEqual 1
      input.hasClass("govuk-input--width-10") mustBe true
      input.attr("aria-describedby") must include(s"$fieldName-hint")
    }

    "must post to the controller's onSubmit action" in new Setup {

      render(form).select("form").attr("action") mustEqual
        controllers.controllingbody.routes.ControllingBodyVatRegistrationNumberController.onSubmit().url
    }

    "must pre-populate the input from the form" in new Setup {

      render(form.fill("353868127")).select(s"input[name=$fieldName]").attr("value") mustEqual "353868127"
    }

    "must render the error summary and field error when the value is missing" in new Setup {

      val doc: Document = render(form.bind(Map(fieldName -> "")))

      doc.title                               must startWith(messages("error.title.prefix"))
      doc.select(".govuk-error-summary").text must include(messages("controllingBodyVatRegNumber.error.required"))
      doc.select(".govuk-error-message").text must include(messages("controllingBodyVatRegNumber.error.required"))
      doc.select(s".govuk-error-summary a[href=#$fieldName]").size() mustEqual 1
    }

    "must render the invalid characters error" in new Setup {

      render(form.bind(Map(fieldName -> "GB3538681"))).select(".govuk-error-message").text must include(
        messages("controllingBodyVatRegNumber.error.invalid.characters")
      )
    }

    "must render the length error" in new Setup {

      render(form.bind(Map(fieldName -> "12345678"))).select(".govuk-error-message").text must include(
        messages("controllingBodyVatRegNumber.error.length")
      )
    }

    "must render the real VAT registration number error" in new Setup {

      render(form.bind(Map(fieldName -> "353868128"))).select(".govuk-error-message").text must include(
        messages("controllingBodyVatRegNumber.error.invalid")
      )
    }
  }
}
