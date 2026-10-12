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
import forms.controllingbody.ControllingBodyAddTradingNameYesNoFormProvider
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.i18n.Messages
import play.api.test.FakeRequest
import views.html.controllingbody.ControllingBodyAddTradingNameYesNoView

import scala.jdk.CollectionConverters.*

class ControllingBodyAddTradingNameYesNoViewSpec extends SpecBase {

  trait Setup {
    private val app = applicationBuilder().build()

    private val view = app.injector.instanceOf[ControllingBodyAddTradingNameYesNoView]

    implicit private val request: play.api.mvc.Request[?] = FakeRequest()

    implicit val messages: Messages =
      app.injector.instanceOf[play.api.i18n.MessagesApi].preferred(request)

    val form: Form[Boolean] = new ControllingBodyAddTradingNameYesNoFormProvider()()

    def docFor(form: Form[Boolean] = form): Document = {
      val html = view(form)(request, messages)
      Jsoup.parse(html.body)
    }
  }

  "ControllingBodyAddTradingNameYesNoView" - {

    "must render the page correctly" in new Setup {
      val doc: Document = docFor()

      doc.title must include(messages("controllingBodyAddTradingNameYesNo.title"))

      doc.title must include(messages("changeRegistrationDetails.caption"))

      doc.select(".govuk-back-link").text() mustEqual messages("site.back")

      doc.select("span").select(".govuk-caption-l").text() mustEqual messages("changeRegistrationDetails.caption")

      doc.select("h1").select(".govuk-fieldset__heading").text() mustEqual messages("controllingBodyAddTradingNameYesNo.heading")

      doc.select("button.govuk-button").text must include(messages("site.continue"))
    }

    "must render the Yes and No options with nothing selected" in new Setup {
      val doc: Document = docFor()

      val labels: Seq[String] = doc.select(".govuk-radios__item .govuk-label").asScala.map(_.text()).toSeq

      labels mustEqual Seq(messages("site.yes"), messages("site.no"))

      val values: Seq[String] = doc.select(".govuk-radios__input").asScala.map(_.attr("value")).toSeq

      values mustEqual Seq("true", "false")

      doc.select(".govuk-radios__input[checked]") mustBe empty
    }

    Seq(true, false).foreach { answer =>
      s"must select the previously chosen option when the answer is $answer" in new Setup {
        val doc: Document = docFor(form = form.fill(answer))

        doc.select(".govuk-radios__input[checked]").asScala.map(_.attr("value")).toSeq mustEqual Seq(answer.toString)
      }
    }

    "must render an error summary that links to the first option when there are form errors" in new Setup {
      val doc: Document = docFor(form = form.bind(Map("value" -> "")))

      doc.title must startWith(messages("error.title.prefix"))

      doc.select(".govuk-error-summary").size() mustEqual 1

      doc.select(".govuk-error-summary__list a").attr("href") mustEqual "#value"

      doc.getElementById("value").attr("value") mustEqual "true"

      doc.select(".govuk-error-summary__list a").text() mustEqual messages("controllingBodyAddTradingNameYesNo.error.required")

      doc.select(".govuk-error-message").text() must include(messages("controllingBodyAddTradingNameYesNo.error.required"))
    }
  }
}
