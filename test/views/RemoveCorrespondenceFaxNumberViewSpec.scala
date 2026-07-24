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
import forms.RemoveCorrespondenceFaxNumberFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.test.FakeRequest
import views.html.RemoveCorrespondenceFaxNumberView

class RemoveCorrespondenceFaxNumberViewSpec extends SpecBase {

  "RemoveCorrespondenceFaxNumberView" - {

    "must render page correctly" in new Setup {

      val html = view(form, NormalMode, faxNumber)
      val doc = Jsoup.parse(html.body)

      doc.title must include(messages("removeCorrespondenceFaxNumber.title"))

      doc.select("h1").text must include(
        messages("removeCorrespondenceFaxNumber.heading", faxNumber)
      )

      doc.select("legend").text must include(
        messages("removeCorrespondenceFaxNumber.heading", faxNumber)
      )
      doc.select("span").select(".govuk-caption-l").text() must include(messages("changeRegistrationDetails.caption"))

      doc.select("button").text must include(
        messages("site.continue")
      )

      doc.select("input[type=radio]").size() mustEqual 2
    }

    "must render error summary when form has errors" in new Setup {

      val boundForm = form.bind(Map("value" -> ""))

      val html = view(boundForm, NormalMode, faxNumber)
      val doc = Jsoup.parse(html.body)

      doc.select(".govuk-error-summary").size() mustEqual 1
    }

  }

  trait Setup {
    val app = applicationBuilder().build()

    val view = app.injector.instanceOf[RemoveCorrespondenceFaxNumberView]

    val formProvider = new RemoveCorrespondenceFaxNumberFormProvider()

    val form = formProvider()

    val faxNumber = "01234567890"

    implicit val request: play.api.mvc.Request[?] = FakeRequest()

    implicit val messages: Messages =
      app.injector
        .instanceOf[play.api.i18n.MessagesApi]
        .preferred(request)

  }
}
