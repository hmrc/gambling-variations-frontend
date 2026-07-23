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
import forms.CorrespondenceUKAddrScreenerFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.must.Matchers.*
import play.api.i18n.Messages
import play.api.test.FakeRequest
import views.html.CorrespondenceUKAddrScreenerView

class CorrespondenceUKAddrScreenerViewSpec extends SpecBase {

  trait Setup {
    private val app = applicationBuilder().build()

    private val view = app.injector.instanceOf[CorrespondenceUKAddrScreenerView]

    implicit private val request: play.api.mvc.Request[?] = FakeRequest()

    implicit val messages: Messages =
      app.injector.instanceOf[play.api.i18n.MessagesApi].preferred(request)

    private val formProvider = new CorrespondenceUKAddrScreenerFormProvider()
    private val form = formProvider()

    private val html = view(form, NormalMode)(request, messages)

    val doc: Document = Jsoup.parse(html.body)

  }

  "CorrespondenceUKAddrScreenerView" - {

    "must render page correctly" in new Setup {

      doc.title must include(messages("correspondenceUKAddrScreener.title"))

      doc.select("span").select(".govuk-caption-l").text() must include(messages("changeRegistrationDetails.caption"))

      doc.select("h1").select(".govuk-fieldset__heading").text() must include(messages("correspondenceUKAddrScreener.heading"))

      doc.select("#correspondenceUKAddrScreener-hint").select(".govuk-hint").text() must include(messages("correspondenceUKAddrScreener.hint"))

      doc.select("button.govuk-button").text must include(messages("site.continue"))

    }

  }
}
