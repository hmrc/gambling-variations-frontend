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
import forms.partner.PartnerDetailsForeignCorporateReferenceFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import views.html.partnerdetails.PartnerDetailsForeignCorporateReferenceView

class PartnerDetailsForeignCorporateReferenceViewSpec extends SpecBase {

  private val fieldName = "value"
  private val prefix = "partnerDetailsForeignCorporateReference"
  private val validValue = "ABC-123 456"
  private val newPartnersIndex = 0.toString

  trait Setup {
    private val app = applicationBuilder().build()

    val view: PartnerDetailsForeignCorporateReferenceView =
      app.injector.instanceOf[PartnerDetailsForeignCorporateReferenceView]

    val form: Form[String] = new PartnerDetailsForeignCorporateReferenceFormProvider()()

    val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

    val messages: Messages =
      app.injector.instanceOf[MessagesApi].preferred(request)

    def render(f: Form[String]): Document =
      Jsoup.parse(view(f, newPartnersIndex, NormalMode)(request, messages).body)
  }

  "PartnerDetailsForeignCorporateReferenceView" - {

    "must render the page correctly" in new Setup {

      val doc: Document = render(form)

      // text
      doc.title must include(messages(s"$prefix.title"))

      doc.select(".govuk-caption-l").text mustEqual messages("changeRegistrationDetails.caption")

      doc.select("h1").text mustBe messages(s"$prefix.heading")
      doc.select(s"label[for=$fieldName].govuk-label--l").text mustBe messages(s"$prefix.heading")

      doc.select("button.govuk-button").text must include(messages("site.continue"))

      doc.select(".govuk-hint").size() mustEqual 0
      doc.select(".govuk-error-summary").size() mustEqual 0

    }

    "must render the input with the name the form provider binds, at fixed width of 20 chars" in new Setup {

      val doc: Document = render(form)

      doc.select(s"input[name=$fieldName]").size() mustEqual 1
      doc.select(s"input[name=$fieldName]").hasClass("govuk-input--width-20") mustBe true

    }

    "must post to the controller's onSubmit action" in new Setup {

      val doc: Document = render(form)

      doc.select("form").attr("action") mustEqual
        controllers.partnerdetails.routes.PartnerDetailsForeignCorporateReferenceController.onSubmit(newPartnersIndex, NormalMode).url
    }

    "must pre-populate the input from the form, keeping the value verbatim" in new Setup {

      val doc: Document = render(form.fill(validValue))

      doc.select(s"input[name=$fieldName]").attr("value") mustEqual validValue
      doc.select(".govuk-error-summary").size() mustEqual 0
    }

    "must render without errors for a valid submission" in new Setup {
      val doc = render(form.bind(Map(fieldName -> validValue)))
      doc.select(".govuk-error-summary").size() mustEqual 0
      doc.select(".govuk-error-message").size() mustEqual 0
    }

    "must render the error summary and field error when the value is missing" in new Setup {

      val doc: Document = render(form.bind(Map(fieldName -> "")))

      doc.select(".govuk-error-summary").size() mustEqual 1
      doc.select(".govuk-error-summary").text must include(messages(s"$prefix.error.required"))
      doc.select(".govuk-error-message").text must include(messages(s"$prefix.error.required"))
    }

    "must render the length error when the value is more than 100 characters" in new Setup {

      val doc: Document = render(form.bind(Map(fieldName -> ("a" * 101))))

      doc.select(".govuk-error-message").text must include(messages(s"$prefix.error.length"))
      doc.select(".govuk-error-summary").size() mustEqual 1
    }

    "must render the invalid-characters error for a disallowed character" in new Setup {

      val doc: Document = render(form.bind(Map(fieldName -> "abc$123")))

      doc.select(".govuk-error-message").text must include(messages(s"$prefix.error.invalid"))
      doc.select(".govuk-error-summary").size() mustEqual 1
    }

  }

}
