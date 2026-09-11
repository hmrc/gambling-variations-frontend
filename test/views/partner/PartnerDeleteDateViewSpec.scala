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
import forms.partner.PartnerDeleteDateFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.running
import views.html.partner.PartnerDeleteDateView

import java.time.{Clock, LocalDate, ZoneOffset}

class PartnerDeleteDateViewSpec extends SpecBase {

  private val today: LocalDate =
    LocalDate.of(2026, 9, 5)

  private val clock: Clock =
    Clock.fixed(
      today.atStartOfDay(ZoneOffset.UTC).toInstant,
      ZoneOffset.UTC
    )

  private val registrationDate: LocalDate =
    LocalDate.of(2020, 1, 1)

  private val formattedRegistrationDate: String =
    "1 January 2020"

  private val latestDate: String =
    "19 September 2026"

  private val partnerName: String =
    "Test Trading Name"

  "PartnerDeleteDateView" - {

    "render the page correctly" in {

      val application =
        applicationBuilder()
          .overrides(
            bind[Clock].toInstance(clock)
          )
          .build()

      running(application) {

        implicit val msgs: Messages =
          messages(application)

        val view =
          application.injector
            .instanceOf[PartnerDeleteDateView]

        val formProvider =
          application.injector
            .instanceOf[PartnerDeleteDateFormProvider]

        val form =
          formProvider(registrationDate)

        val html =
          view(
            form,
            NormalMode,
            formattedRegistrationDate,
            latestDate,
            partnerName
          )(
            FakeRequest(),
            msgs
          )

        val document: Document =
          Jsoup.parse(html.toString)

        document.title() must include(
          msgs("partnerDeleteDate.title")
        )

        document.select("h1").text() must include(
          msgs("partnerDeleteDate.heading")
        )

        document.body().text() must include(
          msgs("changeRegistrationDetails.caption")
        )

        document.body().text() must include(
          msgs("partnerDeleteDate.p1")
        )

        document.body().text() must include(
          msgs(
            "partnerDeleteDate.h2",
            partnerName
          )
        )

        document.body().text() must include(
          msgs(
            "partnerDeleteDate.hint",
            latestDate
          )
        )

        document.body().text() must include(
          partnerName
        )

        document.body().text() must include(
          latestDate
        )

        document.body().text() must include(
          msgs("site.continue")
        )

        document
          .select("form")
          .attr("method") mustEqual
          "POST"

        document
          .select("form")
          .attr("action") mustEqual
          controllers.partner.routes.PartnerDeleteDateController
            .onSubmit()
            .url

        Option(
          document.getElementById("value.day")
        ).isDefined mustBe true

        Option(
          document.getElementById("value.month")
        ).isDefined mustBe true

        Option(
          document.getElementById("value.year")
        ).isDefined mustBe true

        val hint =
          document.getElementById("value-hint")

        Option(hint).isDefined mustBe true

        hint.text() mustEqual
          msgs(
            "partnerDeleteDate.hint",
            latestDate
          )

        document
          .select("button.govuk-button")
          .text() mustEqual
          msgs("site.continue")

        document
          .select(".govuk-error-summary")
          .size() mustEqual 0
      }
    }

    "render an error summary when there are form errors" in {

      val application =
        applicationBuilder()
          .overrides(
            bind[Clock].toInstance(clock)
          )
          .build()

      running(application) {

        implicit val msgs: Messages =
          messages(application)

        val view =
          application.injector
            .instanceOf[PartnerDeleteDateView]

        val formProvider =
          application.injector
            .instanceOf[PartnerDeleteDateFormProvider]

        val form =
          formProvider(registrationDate)

        val boundForm =
          form.bind(
            Map(
              "value.day"   -> "",
              "value.month" -> "",
              "value.year"  -> ""
            )
          )

        val html =
          view(
            boundForm,
            NormalMode,
            formattedRegistrationDate,
            latestDate,
            partnerName
          )(
            FakeRequest(),
            msgs
          )

        val document: Document =
          Jsoup.parse(html.toString)

        document
          .select(".govuk-error-summary")
          .size() mustEqual 1

        document
          .select(".govuk-error-summary__title")
          .text() mustEqual
          msgs("error.summary.title")

        document.body().text() must include(
          msgs(
            "partnerDeleteDate.error.required.all"
          )
        )

        document
          .select(".govuk-error-summary a")
          .attr("href") mustEqual
          "#value.day"

        val fieldError =
          document.getElementById("value-error")

        Option(fieldError).isDefined mustBe true

        fieldError.text() must include(
          msgs(
            "partnerDeleteDate.error.required.all"
          )
        )
      }
    }

    "render a previously entered date" in {

      val application =
        applicationBuilder()
          .overrides(
            bind[Clock].toInstance(clock)
          )
          .build()

      running(application) {

        implicit val msgs: Messages =
          messages(application)

        val view =
          application.injector
            .instanceOf[PartnerDeleteDateView]

        val formProvider =
          application.injector
            .instanceOf[PartnerDeleteDateFormProvider]

        val existingDate =
          LocalDate.of(2026, 9, 10)

        val form =
          formProvider(registrationDate)
            .fill(existingDate)

        val html =
          view(
            form,
            NormalMode,
            formattedRegistrationDate,
            latestDate,
            partnerName
          )(
            FakeRequest(),
            msgs
          )

        val document: Document =
          Jsoup.parse(html.toString)

        document
          .getElementById("value.day")
          .attr("value") mustEqual
          "10"

        document
          .getElementById("value.month")
          .attr("value") mustEqual
          "9"

        document
          .getElementById("value.year")
          .attr("value") mustEqual
          "2026"
      }
    }
  }
}
