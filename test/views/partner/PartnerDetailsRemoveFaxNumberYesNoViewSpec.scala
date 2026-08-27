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
import forms.partner.PartnerDetailsRemoveFaxNumberYesNoFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.test.FakeRequest
import play.api.test.Helpers.running
import views.html.partner.PartnerDetailsRemoveFaxNumberYesNoView

class PartnerDetailsRemoveFaxNumberYesNoViewSpec extends SpecBase {

  private val form = new PartnerDetailsRemoveFaxNumberYesNoFormProvider()()

  "PartnerRemoveFaxNumberYesNoView" - {

    "render the page correctly" in {

      val application = applicationBuilder().build()

      running(application) {

        val view = application.injector.instanceOf[PartnerDetailsRemoveFaxNumberYesNoView]

        val html = view(form, NormalMode, "here-goes-nothing!")(FakeRequest(), messages(application))

        val document: Document = Jsoup.parse(html.toString)

        document.title() must include(
          messages(application)(s"partnerDetailsRemoveFaxNumberYesNo.title")
        )

        document.select("h1").select(".govuk-fieldset__heading").text() mustEqual
          messages(application)(s"partnerDetailsRemoveFaxNumberYesNo.heading", "here-goes-nothing!")

        document.getElementById("value").attr("value") mustEqual "true"

        document.getElementById("value-no").attr("value") mustEqual "false"

        document.select(".govuk-button").text() mustEqual
          messages(application)("site.continue")
      }
    }

    "render an error summary when there are form errors" in {

      val application = applicationBuilder().build()

      running(application) {

        val view = application.injector.instanceOf[PartnerDetailsRemoveFaxNumberYesNoView]

        val boundForm = form.bind(Map("value" -> ""))

        val html = view(boundForm, NormalMode, "here-goes-nothing!")(FakeRequest(), messages(application))

        val document: Document = Jsoup.parse(html.toString)

        document.select(".govuk-error-summary").size() mustEqual 1

        document.body().text() must include(
          messages(application)(s"partnerDetailsRemoveFaxNumberYesNo.error.required")
        )
      }
    }
  }
}
