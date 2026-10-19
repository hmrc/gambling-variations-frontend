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
import forms.controllingbody.ControllingBodyChangeScreenerFormProvider
import models.NormalMode
import models.controllingbody.ControllingBodyChangeOption
import models.controllingbody.ControllingBodyChangeOption.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.data.Form
import play.api.i18n.Messages
import play.api.test.FakeRequest
import views.html.controllingbody.ControllingBodyChangeScreenerView

import scala.jdk.CollectionConverters.*

class ControllingBodyChangeScreenerViewSpec extends SpecBase {

  trait Setup {
    private val app = applicationBuilder().build()

    private val view = app.injector.instanceOf[ControllingBodyChangeScreenerView]

    implicit private val request: play.api.mvc.Request[?] = FakeRequest()

    implicit val messages: Messages =
      app.injector.instanceOf[play.api.i18n.MessagesApi].preferred(request)

    val form: Form[ControllingBodyChangeOption] = new ControllingBodyChangeScreenerFormProvider()()

    val controllingBodyName = "Totally Different Group Holdings Ltd"

    def docFor(form: Form[ControllingBodyChangeOption] = form, name: String = controllingBodyName): Document = {
      val html = view(form, NormalMode, name)(request, messages)
      Jsoup.parse(html.body)
    }
  }

  "ControllingBodyChangeScreenerView" - {

    "must render the page correctly" in new Setup {
      val doc: Document = docFor()

      doc.title must include(messages("controllingBodyChangeScreener.title"))

      doc.title must include(messages("changeRegistrationDetails.caption"))

      doc.select("span").select(".govuk-caption-l").text() mustEqual messages("changeRegistrationDetails.caption")

      doc.select("h1").select(".govuk-fieldset__heading").text() mustEqual messages("controllingBodyChangeScreener.heading")

      doc.select("button.govuk-button").text must include(messages("site.continue"))
    }

    "must show the current controlling body between the heading and the options" in new Setup {
      val doc: Document = docFor()

      doc.select(".govuk-fieldset .govuk-hint p.govuk-body").text() mustEqual
        messages("controllingBodyChangeScreener.p1", controllingBodyName)
    }

    "must escape the controlling body name" in new Setup {
      val doc: Document = docFor(name = "<b>Evil & Co</b>")

      doc.select(".govuk-hint b") mustBe empty

      doc.select(".govuk-hint").text() must include("<b>Evil & Co</b>")
    }

    "must render the three options with an 'or' divider before the last option" in new Setup {
      val doc: Document = docFor()

      val labels: Seq[String] = doc.select(".govuk-radios__item .govuk-label").asScala.map(_.text()).toSeq

      labels mustEqual Seq(
        messages("controllingBodyChangeScreener.editDetails"),
        messages("controllingBodyChangeScreener.provideNew"),
        messages("controllingBodyChangeScreener.keepSame")
      )

      val values: Seq[String] = doc.select(".govuk-radios__input").asScala.map(_.attr("value")).toSeq

      values mustEqual Seq(EditDetails.toString, ProvideNew.toString, KeepSame.toString)

      doc.select(".govuk-radios__divider").text() mustEqual messages("site.or")

      doc.select(".govuk-radios > *").asScala.map(_.className()).toSeq mustEqual Seq(
        "govuk-radios__item",
        "govuk-radios__item",
        "govuk-radios__divider",
        "govuk-radios__item"
      )
    }

    "must select the previously chosen option" in new Setup {
      val doc: Document = docFor(form = form.fill(KeepSame))

      doc.select(".govuk-radios__input[checked]").asScala.map(_.attr("value")).toSeq mustEqual Seq(KeepSame.toString)
    }

    "must render an error summary that links to the first option when there are form errors" in new Setup {
      val doc: Document = docFor(form = form.bind(Map("value" -> "")))

      doc.title must startWith(messages("error.title.prefix"))

      doc.select(".govuk-error-summary").size() mustEqual 1

      doc.select(".govuk-error-summary__list a").attr("href") mustEqual "#value"

      doc.getElementById("value").attr("value") mustEqual EditDetails.toString

      doc.select(".govuk-error-message").text() must include(messages("controllingBodyChangeScreener.error.required"))
    }
  }
}
