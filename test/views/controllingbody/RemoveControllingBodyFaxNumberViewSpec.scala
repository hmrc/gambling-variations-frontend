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
import forms.controllingbody.RemoveControllingBodyFaxNumberFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.must.Matchers.*
import play.api.data.Form
import play.api.i18n.Messages
import play.api.test.FakeRequest
import views.html.controllingbody.RemoveControllingBodyFaxNumberView

class RemoveControllingBodyFaxNumberViewSpec extends SpecBase {

  trait Setup {
    private val app = applicationBuilder().build()

    private val view = app.injector.instanceOf[RemoveControllingBodyFaxNumberView]

    implicit private val request: play.api.mvc.Request[?] = FakeRequest()

    implicit val messages: Messages =
      app.injector.instanceOf[play.api.i18n.MessagesApi].preferred(request)

    val form: Form[Boolean] = new RemoveControllingBodyFaxNumberFormProvider()()

    val faxNumber = "020 7844 4444"

    def render(form: Form[Boolean]): Document =
      Jsoup.parse(view(form, NormalMode, faxNumber)(request, messages).body)
  }

  "RemoveControllingBodyFaxNumberView" - {

    "must render the page correctly" in new Setup {

      val doc: Document = render(form)

      doc.title must include(messages("removeControllingBodyFaxNumber.title"))
      doc.title must include(messages("changeRegistrationDetails.caption"))

      doc.select("span.govuk-caption-l").text() mustEqual messages("changeRegistrationDetails.caption")
      doc.select("legend.govuk-fieldset__legend h1").text() mustEqual messages("removeControllingBodyFaxNumber.heading", faxNumber)

      doc.select("input[type=radio][name=value][value=true]").size() mustEqual 1
      doc.select("input[type=radio][name=value][value=false]").size() mustEqual 1
      doc.select("label[for=value]").text() mustEqual messages("site.yes")
      doc.select("label[for=value-no]").text() mustEqual messages("site.no")

      doc.select("form").attr("action") mustEqual
        controllers.controllingbody.routes.RemoveControllingBodyFaxNumberController.onSubmit().url

      doc.select("button.govuk-button").text mustEqual messages("site.continue")

      doc.select(".govuk-error-summary").size() mustEqual 0
    }

    "must show the error summary and message when nothing was selected" in new Setup {

      val doc: Document = render(form.bind(Map("value" -> "")))

      doc.select(".govuk-error-summary").size() mustEqual 1
      doc.select(".govuk-error-summary__list a").text() mustEqual messages("removeControllingBodyFaxNumber.error.required")
      doc.select(".govuk-error-summary__list a").attr("href") mustEqual "#value"
      doc.select(".govuk-error-message").text() must include(messages("removeControllingBodyFaxNumber.error.required"))
      doc.title                                 must startWith(messages("error.title.prefix"))
    }
  }
}
