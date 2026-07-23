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
import forms.ChangeBusinessNameFormProvider
import models.BusinessType.Soleproprietor
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.*
import play.api.i18n.Messages
import play.api.test.FakeRequest
import views.html.BusinessNameView

class BusinessNameViewSpec extends SpecBase {

  trait Setup {
    val app = applicationBuilder().build()

    val view = app.injector.instanceOf[BusinessNameView]

    implicit val request: play.api.mvc.Request[?] = FakeRequest()

    implicit val messages: Messages =
      app.injector.instanceOf[play.api.i18n.MessagesApi].preferred(request)

    val formProvider = new ChangeBusinessNameFormProvider()
    val form = formProvider(Soleproprietor)

    val businessName = "YouCanBetOnIt"
    val tradingName = "YouCanTradeOnIt"

    val html = view(Soleproprietor, businessName, Some(tradingName), true)(request, messages)

    val doc = Jsoup.parse(html.body)

  }

  "BusinessNameView" - {

    "must render page correctly" in new Setup {

      doc.title             must include(messages("businessName.title"))
      doc.select("h1").text must include(messages("businessName.heading"))

      doc
        .select("p")
        .select(".govuk-body")
        .text() must include(messages("businessName.table.paragraph.soleproprietor"))

      doc
        .select("p")
        .select(".govuk-body")
        .text() must include(messages("businessName.message.requiredToSubmit"))

      doc.body().select(".govuk-caption-l").text must include(messages("changeRegistrationDetails.caption"))

      doc.body().select(".govuk-summary-list__value").text must be(businessName + " " + tradingName)

      doc.select(".govuk-hint").isEmpty mustBe true

    }

  }
}
