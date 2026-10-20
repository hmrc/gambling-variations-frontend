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

package views.returnperiods

import base.SpecBase
import forms.returnperiods.EnterNonStandardPeriodDateFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.api.test.Helpers.running
import views.html.returnperiods.EnterNonStandardPeriodDateView

import java.time.LocalDate

class EnterNonStandardPeriodDateViewSpec extends SpecBase {

  private val periodNumber = 1
  private val numberOfPeriods = 8

  private val periodEnd = LocalDate.of(2026, 1, 16)
  private val lowerBoundary = periodEnd.minusDays(16)
  private val upperBoundary = periodEnd.plusDays(16)

  private val periodMonthYear = "January 2026"
  private val formattedLowerBoundary = "31 December 2025"
  private val formattedUpperBoundary = "1 February 2026"
  private val formattedExampleDate = "31 1 2026"

  "EnterNonStandardPeriodDateView" - {

    "render the page correctly" in {

      val application = applicationBuilder().build()

      running(application) {

        implicit val msgs: Messages = messages(application)

        val view =
          application.injector
            .instanceOf[EnterNonStandardPeriodDateView]

        val formProvider =
          application.injector
            .instanceOf[EnterNonStandardPeriodDateFormProvider]

        val form =
          formProvider(lowerBoundary, upperBoundary)

        val request = FakeRequest()

        val html =
          view(
            form,
            NormalMode,
            periodNumber,
            numberOfPeriods,
            periodEnd,
            lowerBoundary,
            upperBoundary
          )(
            request,
            msgs
          )

        val document: Document = Jsoup.parse(html.toString)

        document.title() must include(periodMonthYear)

        document
          .select("h1")
          .text() must include(periodMonthYear)

        document.body().text() must include(
          msgs("changeRegistrationDetails.caption")
        )

        document.body().text() must include(formattedLowerBoundary)
        document.body().text() must include(formattedUpperBoundary)
        document.body().text() must include(formattedExampleDate)

        document.body().text() must include(msgs("site.continue"))

        document
          .select("form")
          .attr("method") mustEqual "POST"

        document
          .select("form")
          .attr("action") mustEqual
          controllers.returnperiods.routes.EnterNonStandardPeriodDateController
            .onSubmit(periodNumber, NormalMode)
            .url

        document
          .select("form")
          .attr("autocomplete") mustEqual "off"

        Option(document.getElementById("value.day")).isDefined mustBe true
        Option(document.getElementById("value.month")).isDefined mustBe true
        Option(document.getElementById("value.year")).isDefined mustBe true

        val expectedHint = msgs(
          "enterNonStandardPeriodDate.hint",
          formattedLowerBoundary,
          formattedUpperBoundary,
          formattedExampleDate
        ).replaceAll("\\s+", " ").trim

        val actualHint =
          document
            .getElementById("value-hint")
            .text()
            .replaceAll("\\s+", " ")
            .trim

        actualHint mustEqual expectedHint

        document
          .select("button.govuk-button")
          .text() mustEqual msgs("site.continue")

        document
          .select(".govuk-error-summary")
          .size() mustEqual 0
      }
    }

    "render an error summary when there are form errors" in {

      val application = applicationBuilder().build()

      running(application) {

        implicit val msgs: Messages = messages(application)

        val view =
          application.injector
            .instanceOf[EnterNonStandardPeriodDateView]

        val formProvider =
          application.injector
            .instanceOf[EnterNonStandardPeriodDateFormProvider]

        val form =
          formProvider(lowerBoundary, upperBoundary)

        val boundForm =
          form.bind(
            Map(
              "value.day"   -> "",
              "value.month" -> "",
              "value.year"  -> ""
            )
          )

        val request = FakeRequest()

        val html =
          view(
            boundForm,
            NormalMode,
            periodNumber,
            numberOfPeriods,
            periodEnd,
            lowerBoundary,
            upperBoundary
          )(
            request,
            msgs
          )

        val document: Document = Jsoup.parse(html.toString)

        document
          .select(".govuk-error-summary")
          .size() mustEqual 1

        document
          .select(".govuk-error-summary__title")
          .text() mustEqual msgs("error.summary.title")

        document.body().text() must include(
          msgs("enterNonStandardPeriodDate.error.required.all")
        )

        document
          .select(".govuk-error-summary a")
          .attr("href") mustEqual "#value.day"

        val fieldError = document.getElementById("value-error")

        Option(fieldError).isDefined mustBe true

        fieldError.text() must include(
          msgs("enterNonStandardPeriodDate.error.required.all")
        )
      }
    }

    "render a previously entered date" in {

      val application = applicationBuilder().build()

      running(application) {

        implicit val msgs: Messages = messages(application)

        val view =
          application.injector
            .instanceOf[EnterNonStandardPeriodDateView]

        val formProvider =
          application.injector
            .instanceOf[EnterNonStandardPeriodDateFormProvider]

        val existingDate = LocalDate.of(2026, 1, 15)

        val form =
          formProvider(lowerBoundary, upperBoundary)
            .fill(existingDate)

        val request = FakeRequest()

        val html =
          view(
            form,
            NormalMode,
            periodNumber,
            numberOfPeriods,
            periodEnd,
            lowerBoundary,
            upperBoundary
          )(
            request,
            msgs
          )

        val document: Document = Jsoup.parse(html.toString)

        document
          .getElementById("value.day")
          .attr("value") mustEqual "15"

        document
          .getElementById("value.month")
          .attr("value") mustEqual "1"

        document
          .getElementById("value.year")
          .attr("value") mustEqual "2026"
      }
    }
  }
}
