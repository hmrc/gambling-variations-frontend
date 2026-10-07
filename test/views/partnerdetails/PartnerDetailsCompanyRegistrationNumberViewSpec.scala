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

package views.partnerdetails

import base.SpecBase
import forms.partnerdetails.PartnerDetailsCompanyRegistrationNumberFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import views.html.partnerdetails.PartnerDetailsCompanyRegistrationNumberView

class PartnerDetailsCompanyRegistrationNumberViewSpec extends SpecBase {

  private val newPartnersIndex = 0.toString

  trait Setup {
    private val app = applicationBuilder().build()

    val view: PartnerDetailsCompanyRegistrationNumberView =
      app.injector.instanceOf[PartnerDetailsCompanyRegistrationNumberView]

    val form: Form[String] = (new PartnerDetailsCompanyRegistrationNumberFormProvider())()

    val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

    val messages: Messages =
      app.injector.instanceOf[MessagesApi].preferred(request)

    def render(f: Form[String]): Document =
      Jsoup.parse(view(f, newPartnersIndex, NormalMode)(request, messages).body)
  }

  private val fieldName = "value"
  private val companiesHouseUrl = "https://find-and-update.company-information.service.gov.uk/"

  "PartnerDetailsCompanyRegistrationNumberView" - {

    "must render the page correctly" in new Setup {

      val doc: Document = render(form)

      doc.title must include(messages("partnerDetailsCompanyRegistrationNumber.title"))

      doc.select(".govuk-caption-l").text mustEqual messages("changeRegistrationDetails.caption")

      doc.select("h1.govuk-heading-l").text mustBe messages("partnerDetailsCompanyRegistrationNumber.heading")

      doc.select(s"label[for=$fieldName].govuk-label--m").text mustBe
        messages("partnerDetailsCompanyRegistrationNumber.paragraph")

      doc.select(".govuk-hint").text mustBe messages("partnerDetailsCompanyRegistrationNumber.hint")

      doc.select("button.govuk-button").text must include(messages("site.continue"))

      doc.select(".govuk-error-summary").size() mustEqual 0
    }

    "must render the Companies House link opening in a new tab" in new Setup {

      val link = render(form).select(s"p.govuk-body a.govuk-link[href=$companiesHouseUrl]")

      link.size() mustEqual 1
      link.text mustBe messages("partnerDetailsCompanyRegistrationNumber.link")
      link.attr("target") mustBe "_blank"
      link.attr("rel") mustBe "noreferrer noopener"
      link.parents().first().text must startWith(messages("partnerDetailsCompanyRegistrationNumber.p1"))
      link.parents().first().text must endWith(").")
    }

    "must render a 10-character-wide input with the name the form provider binds" in new Setup {

      val input = render(form).select(s"input[name=$fieldName]")

      input.size() mustEqual 1
      input.hasClass("govuk-input--width-10") mustBe true
      input.attr("aria-describedby") must include(s"$fieldName-hint")
    }

    "must post to the controller's onSubmit action" in new Setup {

      render(form).select("form").attr("action") mustEqual
        controllers.partnerdetails.routes.PartnerDetailsCompanyRegistrationNumberController.onSubmit(newPartnersIndex, NormalMode).url
    }

    "must pre-populate the input from the form" in new Setup {

      render(form.fill("SC123456")).select(s"input[name=$fieldName]").attr("value") mustEqual "SC123456"
    }

    "must render the error summary and field error when the value is missing" in new Setup {

      val doc: Document = render(form.bind(Map(fieldName -> "")))

      doc.title must startWith(messages("error.title.prefix"))
      doc.select(".govuk-error-summary").text must
        include(messages("partnerDetailsCompanyRegistrationNumber.error.required"))
      doc.select(".govuk-error-message").text must
        include(messages("partnerDetailsCompanyRegistrationNumber.error.required"))
      doc.select(s".govuk-error-summary a[href=#$fieldName]").size() mustEqual 1
    }
    "must render the invalid characters error" in new Setup {

      render(form.bind(Map(fieldName -> "C@345678"))).select(".govuk-error-message").text must include(
        messages("partnerDetailsCompanyRegistrationNumber.error.invalid.characters")
      )
    }

    "must render the incorrect format error" in new Setup {

      render(form.bind(Map(fieldName -> "ZZ345678"))).select(".govuk-error-message").text must include(
        messages("partnerDetailsCompanyRegistrationNumber.error.invalid.format")
      )
    }
  }
}
