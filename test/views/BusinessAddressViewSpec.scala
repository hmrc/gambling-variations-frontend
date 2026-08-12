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
import models.{Address, NormalMode, UserAnswers}
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.*
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.test.FakeRequest
import viewmodels.checkAnswers.businessaddress.BusinessAddressViewModel
import views.html.BusinessAddressView

class BusinessAddressViewSpec extends SpecBase {

  trait Setup {
    val app = applicationBuilder().build()

    val view = app.injector.instanceOf[BusinessAddressView]

    implicit val request: play.api.mvc.Request[?] = FakeRequest()

    implicit val messages: Messages =
      app.injector.instanceOf[play.api.i18n.MessagesApi].preferred(request)

  }

  "BusinessAddressView" - {

    "must render page correctly for UK address" in new Setup {

      val ukAddressAnswers: UserAnswers = UserAnswers(
        "id",
        Json.obj(
          "businessAddressSection" -> Json.obj(
            "mgdRegNum" -> "XMY1000002",
            "businessAddressUk" -> Address(
              "address1",
              Some("address2"),
              Some("address3"),
              Some("address4"),
              Some("postcode"),
              Some("country")
            )
          )
        )
      )

      val html = view(ukAddressAnswers, NormalMode, false)(request, messages)

      val doc = Jsoup.parse(html.body)

      doc.title                                     must include(messages("checkBusinessAddress.title"))
      doc.select("h1").text                         must include(messages("checkBusinessAddress.heading"))
      doc.body().select(".govuk-caption-l").text    must include(messages("changeRegistrationDetails.caption"))
      doc.body().select(".govuk-summary-list").text must include("address1")
      doc.body().select(".govuk-summary-list").text must include("address2")
      doc.body().select(".govuk-summary-list").text must include("address3")
      doc.body().select(".govuk-summary-list").text must include("address4")
      doc.body().select(".govuk-summary-list").text must include("postcode")
      doc.body().select(".govuk-summary-list").text must not include "country"
      doc.select(".govuk-hint").text                must include(messages("checkBusinessAddress.hint"))

    }

    "must include change message when flagged" in new Setup {

      val ukAddressAnswers: UserAnswers = UserAnswers(
        "id",
        Json.obj(
          "businessAddressSection" -> Json.obj(
            "mgdRegNum" -> "XMY1000002",
            "businessAddressUk" -> Address(
              "address1",
              Some("address2"),
              Some("address3"),
              Some("address4"),
              Some("postcode"),
              Some("country")
            ),
            "businessAddressChanged" -> true
          )
        )
      )

      val html = view(ukAddressAnswers, NormalMode, true)(request, messages)

      val doc = Jsoup.parse(html.body)

      doc.select(".govuk-body").text must include(messages("checkBusinessAddress.requiredToSubmit"))
    }

  }
}
