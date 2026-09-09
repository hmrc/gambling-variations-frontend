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

package views.partner

import base.SpecBase
import forms.partner.PartnerSoleProprietorDobFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.api.test.Helpers.running
import views.html.partner.PartnerSoleProprietorDobView

class PartnerSoleProprietorDobViewSpec extends SpecBase {

  "PartnerSoleProprietorDobView" - {

    "render the page correctly" in {

      val application = applicationBuilder().build()

      running(application) {

        implicit val msgs: Messages = messages(application)

        val view =
          application.injector.instanceOf[PartnerSoleProprietorDobView]

        val formProvider =
          application.injector.instanceOf[PartnerSoleProprietorDobFormProvider]

        val form = formProvider()

        val html = view(
          form,
          NormalMode
        )(FakeRequest(), msgs)

        val document: Document = Jsoup.parse(html.toString)

        document.title() must include(
          msgs("partnerSoleProprietorDob.title")
        )

        document.select("h1").text() mustEqual
          msgs("partnerSoleProprietorDob.heading")

        document.body().text() must include(
          msgs("changeRegistrationDetails.caption")
        )

        document.body().text() must include(
          msgs("partnerSoleProprietorDob.hint")
        )

        document.body().text() must include(
          msgs("site.continue")
        )
      }
    }

    "render an error summary when there are form errors" in {

      val application = applicationBuilder().build()

      running(application) {

        implicit val msgs: Messages = messages(application)

        val view =
          application.injector.instanceOf[PartnerSoleProprietorDobView]

        val formProvider =
          application.injector.instanceOf[PartnerSoleProprietorDobFormProvider]

        val form = formProvider()

        val boundForm = form.bind(
          Map(
            "value.day"   -> "",
            "value.month" -> "",
            "value.year"  -> ""
          )
        )

        val html = view(
          boundForm,
          NormalMode
        )(FakeRequest(), msgs)

        val document: Document = Jsoup.parse(html.toString)

        document.select(".govuk-error-summary").size() mustEqual 1

        document.body().text() must include(
          msgs("partnerSoleProprietorDob.error.required.all")
        )
      }
    }
  }
}
