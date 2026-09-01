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
import forms.partner.PartnerDetailsBusinessTypeFormProvider
import models.{BusinessType, NormalMode}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.test.FakeRequest
import play.api.test.Helpers.running
import views.html.partner.PartnerDetailsBusinessTypeView

class PartnerDetailsBusinessTypeViewSpec extends SpecBase {

  private val form = new PartnerDetailsBusinessTypeFormProvider()()

  "PartnerDetailsBusinessTypeView" - {

    "render the page correctly" in {

      val application = applicationBuilder().build()

      running(application) {

        val view = application.injector.instanceOf[PartnerDetailsBusinessTypeView]

        val html = view(form, NormalMode)(FakeRequest(), messages(application))

        val document: Document = Jsoup.parse(html.toString)

        document.title() must include(
          messages(application)("partnerDetailsBusinessType.title")
        )

        document.select("h1").select(".govuk-fieldset__heading").text() mustEqual
          messages(application)("partnerDetailsBusinessType.heading")

        document.select("span").select(".govuk-caption-l").text() mustEqual
          messages(application)("changeRegistrationDetails.caption")

        BusinessType.values.zipWithIndex.foreach { case (option, index) =>
          val radioInput = document.getElementById(s"value_$index")
          val label = document.select(s"label[for=${radioInput.id()}]")

          radioInput.attr("value") mustEqual option.toString
          label.text() mustEqual messages(application)(s"businessType.${option.toString}")
        }

        document.select(".govuk-button").text() mustEqual
          messages(application)("site.continue")
      }
    }

    "render an error summary when there are form errors" in {

      val application = applicationBuilder().build()

      running(application) {

        val view = application.injector.instanceOf[PartnerDetailsBusinessTypeView]

        val boundForm = form.bind(Map("value" -> ""))

        val html = view(
          boundForm,
          NormalMode
        )(FakeRequest(), messages(application))

        val document: Document = Jsoup.parse(html.toString)

        document.select(".govuk-error-summary").size() mustEqual 1

        document.body().text() must include(
          messages(application)("partnerDetailsBusinessType.error.required")
        )
      }
    }
  }
}
