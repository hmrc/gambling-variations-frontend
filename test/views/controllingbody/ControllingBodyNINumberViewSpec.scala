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
import forms.controllingbody.ControllingBodyNINumberFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.test.FakeRequest
import views.html.controllingbody.ControllingBodyNINumberView

class ControllingBodyNINumberViewSpec extends SpecBase {

  "ControllingBodyNINumberView" - {

    "must render the page with the expected content" in new Setup {

      val html = view(form, NormalMode)
      val doc = Jsoup.parse(html.body)

      doc.title must include(messages("controllingBodyAddNino.title"))
      doc.select(".govuk-caption-l").text mustEqual messages("changeRegistrationDetails.caption")
      doc.select("h1").text mustEqual messages("controllingBodyAddNino.heading")

      doc.select("form").attr("action") mustEqual controllers.controllingbody.routes.ControllingBodyNINumberController.onSubmit().url

      doc.select("button").text must include(messages("site.continue"))
    }

    "must render populated values when the form is filled" in new Setup {

      val populatedForm = form.fill(
        "1234"
      )

      val html = view(populatedForm, NormalMode)
      val doc = Jsoup.parse(html.body)

      doc.select("input").attr("value") mustEqual "1234"
    }

    "must render correct error summary when the submitted value is empty" in new Setup {

      val errorForm = form.bind(
        Map(
          "value" -> ""
        )
      )

      val html = view(errorForm, NormalMode)
      val doc = Jsoup.parse(html.body)

      doc.select(".govuk-error-summary").text() must include("Enter the controlling body’s National Insurance number")
    }

    "must render correct error summary when the submitted value is invalid" in new Setup {

      val errorForm = form.bind(
        Map(
          "value" -> "//@:~#"
        )
      )

      val html = view(errorForm, NormalMode)
      val doc = Jsoup.parse(html.body)

      doc.select(".govuk-error-summary").text() must include(
        "The National Insurance number must only include letters a to z, numbers 0 to 9 and spaces"
      )
    }

    "must render correct error summary when the submitted value is too long" in new Setup {
      val sb = new StringBuilder()
      val tooLong = 21
      for (_ <- 1 to tooLong) {
        sb.append(s"a")
      }
      val longString: String = sb.toString()
      val errorForm = form.bind(
        Map(
          "value" -> longString
        )
      )

      val html = view(errorForm, NormalMode)
      val doc = Jsoup.parse(html.body)
      doc.select(".govuk-error-summary").text() must include("The National Insurance number must start with 2 letters, but not D, followed by 6 numbers, with A, B, C, or D as the final character")
    }
  }

  trait Setup {
    val app = applicationBuilder().build()
    val form = new ControllingBodyNINumberFormProvider()()
    val view = app.injector.instanceOf[ControllingBodyNINumberView]

    implicit val request: play.api.mvc.Request[?] = FakeRequest()

    implicit val messages: Messages =
      app.injector
        .instanceOf[play.api.i18n.MessagesApi]
        .preferred(request)
  }
}
