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
import forms.RemoveAssociatedRegNumberFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.test.FakeRequest
import play.api.test.Helpers.running
import views.html.RemoveAssociatedRegNumberView

class RemoveAssociatedRegNumberViewSpec extends SpecBase {

  private val form = new RemoveAssociatedRegNumberFormProvider()()
  private val application = applicationBuilder().build()
  val view = application.injector.instanceOf[RemoveAssociatedRegNumberView]

  "RemoveAssociatedRegNumberView" - {

    "render the page correctly" in {
      running(application) {
        val regNumber = "reg-number"
        val html = view(form, NormalMode, regNumber)(FakeRequest(), messages(application))
        val document: Document = Jsoup.parse(html.toString)

        document.title() must include(
          messages(application)("removeAssociatedRegNumber.title")
        )

        document.select(".govuk-fieldset__heading").text() mustEqual
          messages(application)("removeAssociatedRegNumber.heading", regNumber)

        document.body().text() must include(
          messages(application)("site.continue")
        )

      }
    }

    "render an error summary when there are form errors" in {
      running(application) {
        val boundForm = form.bind(Map("removeCorrespondenceDetailsYesNo" -> ""))
        val html = view(boundForm, NormalMode, "reg")(FakeRequest(), messages(application))
        val document: Document = Jsoup.parse(html.toString)

        document.select(".govuk-error-summary").size() mustEqual 1

        document.body().text() must include(
          messages(application)("Select yes if you want to remove the associated registration number")
        )
      }
    }
  }
}
