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
import forms.BusinessTradingNameFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.*
import play.api.i18n.Messages
import play.api.test.FakeRequest
import views.html.BusinessEmailAddressView

class BusinessEmailAddressViewSpec extends SpecBase {

  trait Setup {
    val app = applicationBuilder().build()

    val view = app.injector.instanceOf[BusinessEmailAddressView]

    implicit val request: play.api.mvc.Request[?] = FakeRequest()

    implicit val messages: Messages =
      app.injector.instanceOf[play.api.i18n.MessagesApi].preferred(request)

    val formProvider = new BusinessTradingNameFormProvider()
    val form = formProvider()

    val html = view(form, NormalMode)(request, messages)

    val doc = Jsoup.parse(html.body)

  }

  "BusinessEmailAddressView" - {

    "must render page correctly" in new Setup {
      doc.title must include(messages("emailAddress.title"))

      doc.body().select(".govuk-caption-l").text() must include(messages("changeRegistrationDetails.caption"))

      doc.select(".govuk-hint").isEmpty mustBe true

      doc.select("button.govuk-button").text must include(messages("site.continue"))

    }

  }
}
