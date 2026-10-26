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

package views.cancellation

import base.SpecBase
import forms.cancellation.{ChangeGroupRegistrationNumberFormProvider, ReasonForCancellationFormProvider}
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.test.FakeRequest
import views.html.cancellation.ChangeGroupRegistrationNumberView

class ChangeGroupRegistrationNumberViewSpec extends SpecBase {

  trait Setup {
    private val app = applicationBuilder().build()

    val view = app.injector.instanceOf[ChangeGroupRegistrationNumberView]

    val formProvider = new ChangeGroupRegistrationNumberFormProvider()

    val form = formProvider()

    val request: play.api.mvc.Request[?] = FakeRequest()

    val messages: Messages =
      app.injector
        .instanceOf[play.api.i18n.MessagesApi]
        .preferred(request)
  }

  "ChangeGroupRegistrationNumberView" - {

    "must render page correctly" in new Setup {

      val html = view(form, NormalMode)(request, messages)
      val doc: Document = Jsoup.parse(html.body)

      doc.title must include(
        messages("changeGroupRegistrationNumber.title")
      )

      doc
        .select(".govuk-caption-l")
        .text() must include(
        messages("changeGroupRegistrationNumber.caption")
      )

      doc
        .select(".govuk-label")
        .text mustBe messages("changeGroupRegistrationNumber.heading")


      doc
        .select("button.govuk-button")
        .text must include(
        messages("site.continue")
      )

      doc
        .select(".govuk-error-summary")
        .size() mustEqual 0
    }

    "must render error summary when form has errors" in new Setup {

      val boundForm =
        form.bind(Map("changeGroupRegistrationNumber" -> ""))

      val html = view(boundForm, NormalMode)(request, messages)
      val doc: Document = Jsoup.parse(html.body)

      doc
        .select(".govuk-error-summary")
        .size() mustEqual 1

      doc
        .select(".govuk-error-summary")
        .text must include(
        messages("changeGroupRegistrationNumber.error.required")
      )
    }

  }
}
