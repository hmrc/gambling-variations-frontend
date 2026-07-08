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

package views

import base.SpecBase
import forms.CorrespondenceAdditionalNameYesNoFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.*
import play.api.i18n.Messages
import play.api.test.FakeRequest
import views.html.CorrespondenceAdditionalNameYesNoView

class CorrespondenceAdditionalNameYesNoViewSpec extends SpecBase {

  val formProvider = new CorrespondenceAdditionalNameYesNoFormProvider()
  val form = formProvider()

  trait Setup {
    val app = applicationBuilder().build()

    val view = app.injector.instanceOf[CorrespondenceAdditionalNameYesNoView]

    implicit val request: play.api.mvc.Request[?] = FakeRequest()

    implicit val messages: Messages =
      app.injector.instanceOf[play.api.i18n.MessagesApi].preferred(request)
  }

  "CorrespondenceAdditionalNameYesNoView" - {

    "must render the page with correct title" in new Setup {

      val html = view(form, NormalMode)(request, messages)
      val doc = Jsoup.parse(html.body)

      doc.title must include(messages("correspondenceAdditionalNameYesNo.title"))
    }

    "must render the correct heading" in new Setup {

      val html = view(form, NormalMode)(request, messages)
      val doc = Jsoup.parse(html.body)

      doc.select("h1").text must include(
        messages("correspondenceAdditionalNameYesNo.heading")
      )
    }

    "must render the caption" in new Setup {

      val html = view(form, NormalMode)(request, messages)
      val doc = Jsoup.parse(html.body)

      doc.select(".govuk-caption-l").text must include(
        messages("correspondenceAdditionalNameYesNo.caption")
      )
    }

    "must render yes and no radio buttons" in new Setup {

      val html = view(form, NormalMode)(request, messages)
      val doc = Jsoup.parse(html.body)

      doc.select("input[value=true]").size() mustBe 1
      doc.select("input[value=false]").size() mustBe 1
    }

    "must contain continue button" in new Setup {

      val html = view(form, NormalMode)(request, messages)
      val doc = Jsoup.parse(html.body)

      doc.select("button.govuk-button").text must include(
        messages("site.continue")
      )
    }
  }
}
