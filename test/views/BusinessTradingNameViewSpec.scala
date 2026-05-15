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
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.*
import play.api.i18n.Messages
import play.api.test.FakeRequest
import models.{BusinessType, NormalMode}
import views.html.BusinessTradingNameView
import forms.BusinessTradingNameFormProvider

class BusinessTradingNameViewSpec extends SpecBase {

  val formProvider = new BusinessTradingNameFormProvider()
  val form = formProvider()

  trait Setup {
    val app = applicationBuilder().build()

    val view = app.injector.instanceOf[BusinessTradingNameView]

    implicit val request: play.api.mvc.Request[?] = FakeRequest()

    implicit val messages: Messages =
      app.injector.instanceOf[play.api.i18n.MessagesApi].preferred(request)
  }

  "BusinessTradingNameView" - {

    "must render page without hint when business type is not SoleProprietor" in new Setup {

      val html = view(form, NormalMode, BusinessType.Partnership)(request, messages)
      val doc = Jsoup.parse(html.body)

      doc.title must include(messages("businessTradingName.title"))

      doc.select("h1").text must include(messages("businessTradingName.heading"))

      doc.select(".govuk-hint").isEmpty mustBe true
    }

    "must render page with hint when business type is SoleProprietor" in new Setup {

      val html = view(form, NormalMode, BusinessType.Soleproprietor)(request, messages)
      val doc = Jsoup.parse(html.body)

      doc.title must include(messages("businessTradingName.title"))

      doc.select("h1").text must include(messages("businessTradingName.heading"))

      doc.select(".govuk-hint").text must include(messages("businessTradingName.hint"))
    }

    "must contain continue button" in new Setup {

      val html = view(form, NormalMode, BusinessType.Soleproprietor)(request, messages)
      val doc = Jsoup.parse(html.body)

      doc.select("button.govuk-button").text must include(messages("site.continue"))
    }
  }
}
