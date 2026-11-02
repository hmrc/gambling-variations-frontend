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

package views.cancellation

import base.SpecBase
import forms.cancellation.CancelRegistrationDateFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.test.FakeRequest
import views.html.cancellation.CancelRegistrationDateView

import java.time.LocalDate

class CancelRegistrationDateViewSpec extends SpecBase {

  trait Setup {

    private val app =
      applicationBuilder().build()

    val view =
      app.injector.instanceOf[CancelRegistrationDateView]

    val registrationDate =
      LocalDate.of(2007, 3, 20)

    val maximumValidDate =
      LocalDate.of(2026, 11, 4)

    val firstInvalidDate =
      LocalDate.of(2026, 11, 5)

    val middleOfDateRange =
      LocalDate.of(2017, 1, 27)

    val request: play.api.mvc.Request[?] =
      FakeRequest()

    implicit val messages: Messages =
      app.injector
        .instanceOf[play.api.i18n.MessagesApi]
        .preferred(request)

    val formProvider =
      new CancelRegistrationDateFormProvider()

    val form =
      formProvider(
        registrationDate,
        maximumValidDate
      )

    def document(
      formToRender: play.api.data.Form[LocalDate] = form
    ): Document = {

      val html =
        view(
          formToRender,
          NormalMode,
          registrationDate,
          firstInvalidDate,
          middleOfDateRange
        )(
          request,
          messages
        )

      Jsoup.parse(html.body)
    }
  }

  "CancelRegistrationDateView" - {

    "must render page correctly" in new Setup {

      val doc =
        document()

      doc.title must include(
        messages("cancelRegistrationDate.title")
      )

      doc
        .select(".govuk-caption-l")
        .text() must include(
        messages("cancelRegistrationDate.caption")
      )

      doc
        .select(".govuk-fieldset__heading")
        .text() mustBe
        messages("cancelRegistrationDate.heading")

      doc
        .select("button.govuk-button")
        .text() must include(
        messages("site.continue")
      )

      doc
        .select(".govuk-error-summary")
        .size() mustEqual 0
    }

    "must render the dynamic hint with registration date, first invalid date and example date" in new Setup {

      val doc =
        document()

      doc
        .select(".govuk-hint")
        .text() mustBe
        messages(
          "cancelRegistrationDate.hint",
          "20 March 2007",
          "5 November 2026",
          "27 1 2017"
        )
    }

    "must render the registration date in full month format in the hint" in new Setup {

      val doc =
        document()

      doc
        .select(".govuk-hint")
        .text() must include(
        "20 March 2007"
      )
    }

    "must render the first invalid date in full month format in the hint" in new Setup {

      val doc =
        document()

      doc
        .select(".govuk-hint")
        .text() must include(
        "5 November 2026"
      )
    }

    "must render the middle date in numerical format in the hint" in new Setup {

      val doc =
        document()

      doc
        .select(".govuk-hint")
        .text() must include(
        "27 1 2017"
      )
    }

    "must render day, month and year date inputs" in new Setup {

      val doc =
        document()

      doc
        .select("#value\\.day")
        .size() mustEqual 1

      doc
        .select("#value\\.month")
        .size() mustEqual 1

      doc
        .select("#value\\.year")
        .size() mustEqual 1
    }

    "must submit to the correct route" in new Setup {

      val doc =
        document()

      doc
        .select("form")
        .attr("action") mustBe
        controllers.cancellation.routes.CancelRegistrationDateController
          .onSubmit(NormalMode)
          .url
    }

    "must render error summary when form has errors" in new Setup {

      val boundForm =
        form.bind(
          Map(
            "value.day"   -> "",
            "value.month" -> "",
            "value.year"  -> ""
          )
        )

      val doc =
        document(boundForm)

      doc
        .select(".govuk-error-summary")
        .size() mustEqual 1

      doc
        .select(".govuk-error-summary")
        .text() must include(
        messages(
          "cancelRegistrationDate.error.required.all"
        )
      )
    }

    "must render outside boundaries error" in new Setup {

      val invalidDate =
        registrationDate.minusDays(1)

      val boundForm =
        form.bind(
          Map(
            "value.day" ->
              invalidDate.getDayOfMonth.toString,
            "value.month" ->
              invalidDate.getMonthValue.toString,
            "value.year" ->
              invalidDate.getYear.toString
          )
        )

      val doc =
        document(boundForm)

      doc
        .select(".govuk-error-summary")
        .size() mustEqual 1

      doc
        .select(".govuk-error-summary")
        .text() must include(
        messages(
          "cancelRegistrationDate.error.outsideBoundaries",
          "20 March 2007",
          "5 November 2026"
        )
      )
    }

    "must link the error summary to the day input" in new Setup {

      val boundForm =
        form.bind(
          Map(
            "value.day"   -> "",
            "value.month" -> "",
            "value.year"  -> ""
          )
        )

      val doc =
        document(boundForm)

      doc
        .select(".govuk-error-summary a")
        .attr("href") mustBe
        "#value.day"
    }
  }
}
