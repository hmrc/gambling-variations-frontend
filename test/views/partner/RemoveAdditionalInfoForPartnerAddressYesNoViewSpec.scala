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
import forms.partner.RemoveAdditionalInfoForPartnerAddressYesNoFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.test.FakeRequest
import views.html.partner.RemoveAdditionalInfoForPartnerAddressYesNoView

class RemoveAdditionalInfoForPartnerAddressYesNoViewSpec extends SpecBase {

  trait Setup:
    private val app = applicationBuilder().build()

    val view = app.injector.instanceOf[RemoveAdditionalInfoForPartnerAddressYesNoView]

    val formProvider = new RemoveAdditionalInfoForPartnerAddressYesNoFormProvider()

    val form = formProvider()

    val request: play.api.mvc.Request[?] = FakeRequest()

    val messages: Messages =
      app.injector
        .instanceOf[play.api.i18n.MessagesApi]
        .preferred(request)

  "RemoveAdditionalInfoForPartnerAddressYesNoView" - {

    "must render page correctly" in new Setup {

      val html = view(form, NormalMode)(request, messages)
      val doc: Document = Jsoup.parse(html.body)

      doc.title must include(
        messages("removeAdditionalInfoForPartnerAddressYesNo.title")
      )

      doc
        .select(".govuk-fieldset__heading")
        .text mustBe
        messages("removeAdditionalInfoForPartnerAddressYesNo.heading")

      doc.select("input[name=value]").size() mustEqual 2

      doc.select("input[name=value][value=true]").size() mustEqual 1

      doc.select("input[name=value][value=false]").size() mustEqual 1

      doc
        .select("label[for=value]")
        .text mustBe messages("site.yes")

      doc
        .select("label[for=value-no]")
        .text mustBe messages("site.no")

      doc
        .select("button.govuk-button")
        .text must include(messages("site.continue"))

      doc
        .select("form")
        .attr("method") mustBe "POST"

      doc
        .select("form")
        .attr("action") mustBe
        controllers.partner.routes.RemoveAdditionalInfoForPartnerAddressYesNoController
          .onSubmit()
          .url

      doc
        .select("form")
        .attr("autocomplete") mustBe "off"

      doc.select(".govuk-error-summary").size() mustEqual 0
    }

    "must render error summary when form has errors" in new Setup {

      val boundForm = form.bind(Map("value" -> ""))

      val html = view(boundForm, NormalMode)(request, messages)
      val doc: Document = Jsoup.parse(html.body)

      doc.select(".govuk-error-summary").size() mustEqual 1

      doc
        .select(".govuk-error-summary")
        .text must include(
        messages("removeAdditionalInfoForPartnerAddressYesNo.error.required")
      )
    }

    "must render error message when form has errors" in new Setup {

      val boundForm = form.bind(Map("value" -> ""))

      val html = view(boundForm, NormalMode)(request, messages)
      val doc: Document = Jsoup.parse(html.body)

      doc
        .select(".govuk-error-message")
        .text must include(
        messages("removeAdditionalInfoForPartnerAddressYesNo.error.required")
      )
    }
  }
}
