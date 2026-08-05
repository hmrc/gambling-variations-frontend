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
import forms.BusinessAddressAdditionalInfoFormProvider
import models.BusinessType.Soleproprietor
import models.{NormalMode, SoleProprietorName}
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.test.FakeRequest
import views.html.BusinessAddressAdditionalInfoView

class BusinessAddressAdditionalInfoViewSpec extends SpecBase {

  "BusinessAddressAdditionalInfoView" - {

    "must render the page with the expected content" in new Setup {

      val html = view(form, NormalMode)
      val doc = Jsoup.parse(html.body)

      doc.title must include(messages("businessAddressAdditionalInfo.title"))
      doc.select(".govuk-caption-l").text mustEqual messages("changeRegistrationDetails.caption")
      doc.select(".govuk-hint").text mustEqual messages("businessAddressAdditionalInfo.hint")
      doc.select("h1").text mustEqual messages("businessAddressAdditionalInfo.heading")

      doc.select("form").attr("action") mustEqual controllers.routes.BusinessAddressAdditionalInfoController.onSubmit(NormalMode).url

      doc.select("button").text must include(messages("site.continue"))
    }

    "must render populated values when the form is filled" in new Setup {

      val populatedForm = form.fill(
        "ajklasjlkjsalkjslkdjflksdjflk"
      )

      val html = view(populatedForm, NormalMode)
      val doc = Jsoup.parse(html.body)

      doc.select("input").attr("value") mustEqual "ajklasjlkjsalkjslkdjflksdjflk"
    }

    "must render correct error summary when the submitted value is empty" in new Setup {

      val errorForm = form.bind(
        Map(
          "businessAddressAdditionalInfo" -> ""
        )
      )

      val html = view(errorForm, NormalMode)
      val doc = Jsoup.parse(html.body)

      doc.select(".govuk-error-summary").text() must include ("Enter additional information for your business address")
    }

    "must render correct error summary when the submitted value is invalid" in new Setup {

      val errorForm = form.bind(
        Map(
          "businessAddressAdditionalInfo" -> "//@:~#"
        )
      )

      val html = view(errorForm, NormalMode)
      val doc = Jsoup.parse(html.body)

      doc.select(".govuk-error-summary").text() must include("The additional information must only include letters a to z, " +
        "numbers 0 to 9, apostrophes, hyphens or spaces")
    }

    "must render correct error summary when the submitted value is too long" in new Setup {
      val sb = new StringBuilder()
      val tooLong = 101
      for(_ <- 1 to tooLong) {
        sb.append(s"a")
      }
      val longString: String = sb.toString()
      val errorForm = form.bind(
        Map(
          "businessAddressAdditionalInfo" -> longString
        )
      )

      val html = view(errorForm, NormalMode)
      val doc = Jsoup.parse(html.body)
      doc.select(".govuk-error-summary").text() must include("The additional information must be 100 characters or less")
    }
  }

  trait Setup {
    val app = applicationBuilder().build()
    val form = new BusinessAddressAdditionalInfoFormProvider()()
    val view = app.injector.instanceOf[BusinessAddressAdditionalInfoView]

    implicit val request: play.api.mvc.Request[?] = FakeRequest()

    implicit val messages: Messages =
      app.injector
        .instanceOf[play.api.i18n.MessagesApi]
        .preferred(request)
  }
}
