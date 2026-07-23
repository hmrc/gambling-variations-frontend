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
import forms.CorrespondenceAddrInfoScreenerFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.must.Matchers.*
import play.api.i18n.Messages
import play.api.test.FakeRequest
import views.html.CorrespondenceAddrInfoScreenerView

class CorrespondenceAddrInfoScreenerViewSpec extends SpecBase {

  trait Setup {
    private val app = applicationBuilder().build()

    private val view = app.injector.instanceOf[CorrespondenceAddrInfoScreenerView]

    implicit private val request: play.api.mvc.Request[?] = FakeRequest()

    implicit val messages: Messages =
      app.injector.instanceOf[play.api.i18n.MessagesApi].preferred(request)

    private val formProvider = new CorrespondenceAddrInfoScreenerFormProvider()
    private val form = formProvider()

    private val html = view(form, NormalMode)(request, messages)

    val doc: Document = Jsoup.parse(html.body)

  }

  "CorrespondenceAddrInfoScreenerView" - {

    "must render page correctly" in new Setup {

      doc.title must include(messages("correspondenceAddrInfoScreener.title"))

      doc.select(".govuk-caption-l").text() must include(messages("changeRegistrationDetails.caption"))
      
      doc
        .select(".govuk-form-group")
        .select(".govuk-fieldset__heading")
        .text mustBe messages("correspondenceAddrInfoScreener.heading")

      doc.select("button.govuk-button").text must include(messages("site.continue"))

    }

  }
}
