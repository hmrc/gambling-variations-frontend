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
import views.html.cancellation.{ChangeGroupRegistrationNumberView, RegistrationCancellationConfirmationView}

class RegistrationCancellationConfirmationViewSpec extends SpecBase {

  trait Setup {
    private val app = applicationBuilder().build()

    val view = app.injector.instanceOf[RegistrationCancellationConfirmationView]

    val formProvider = new ChangeGroupRegistrationNumberFormProvider()

    val form = formProvider()

    val request: play.api.mvc.Request[?] = FakeRequest()

    val redirectUrl = "url"
    val referenceNumber = "XYC123525"

    val messages: Messages =
      app.injector
        .instanceOf[play.api.i18n.MessagesApi]
        .preferred(request)
  }

  "RegistrationCancellationConfirmationView" - {

    "must render page correctly" in new Setup {

      val html = view(redirectUrl, referenceNumber)(request, messages)
      val doc: Document = Jsoup.parse(html.body)

      doc.title must include(
        messages("registrationCancellationConfirmation.title")
      )

      doc
        .select(".govuk-heading-m")
        .text mustBe messages("registrationCancellationConfirmation.heading")

      doc
        .select(".govuk-body")
        .get(0)
        .text mustBe messages("registrationCancellationConfirmation.p1")

      doc
        .select(".govuk-body")
        .get(1)
        .text mustBe messages("registrationCancellationConfirmation.p2")

      doc
        .select(".govuk-panel__body")
        .text mustBe messages("registrationCancellationConfirmation.panel.p1") + " " + referenceNumber

      doc
        .select(".govuk-error-summary")
        .size() mustEqual 0
    }

  }
}
