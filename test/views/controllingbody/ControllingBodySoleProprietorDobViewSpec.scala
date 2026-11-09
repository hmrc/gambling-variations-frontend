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

package views.controllingbody

import base.SpecBase
import forms.controllingbody.ControllingBodySoleProprietorDobFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.running
import views.html.controllingbody.ControllingBodySoleProprietorDobView

import java.time.{Clock, LocalDate, ZoneOffset}

class ControllingBodySoleProprietorDobViewSpec extends SpecBase {

  "ControllingBodySoleProprietorDobView" - {

    "render the page correctly" in {

      val application = applicationBuilder().build()

      running(application) {

        implicit val msgs: Messages = messages(application)

        val view =
          application.injector.instanceOf[ControllingBodySoleProprietorDobView]

        val formProvider =
          application.injector.instanceOf[ControllingBodySoleProprietorDobFormProvider]

        val form = formProvider()

        val html = view(
          form,
          NormalMode
        )(FakeRequest(), msgs)

        val document: Document = Jsoup.parse(html.toString)

        document.title() must include(
          msgs("controllingBodySoleProprietorDob.title")
        )

        document.select("h1").text() mustEqual
          msgs("controllingBodySoleProprietorDob.heading")

        document.body().text() must include(
          msgs("changeRegistrationDetails.caption")
        )

        document.body().text() must include(
          msgs("controllingBodySoleProprietorDob.hint")
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
          application.injector.instanceOf[ControllingBodySoleProprietorDobView]

        val formProvider =
          application.injector.instanceOf[ControllingBodySoleProprietorDobFormProvider]

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
          msgs("controllingBodySoleProprietorDob.error.required.all")
        )
      }
    }

    "report invalid fields before missing fields and highlight only the invalid fields" in {

      val application = applicationBuilder().build()

      running(application) {

        implicit val msgs: Messages = messages(application)

        val view =
          application.injector.instanceOf[ControllingBodySoleProprietorDobView]

        val form =
          application.injector.instanceOf[ControllingBodySoleProprietorDobFormProvider].apply()

        Seq(
          ("$$", "me", "", Seq("day", "month")),
          ("32", "3", "", Seq("day")),
          ("2", "13", "", Seq("month")),
          ("2", "13", "1998", Seq("month"))
        ).foreach { case (day, month, year, invalidFields) =>
          val boundForm = form.bind(
            Map(
              "value.day"   -> day,
              "value.month" -> month,
              "value.year"  -> year
            )
          )

          val document: Document = Jsoup.parse(view(boundForm, NormalMode)(FakeRequest(), msgs).toString)

          val highlightedFields =
            Seq("day", "month", "year").filter(part => document.getElementById(s"value.$part").hasClass("govuk-input--error"))

          withClue(s"Date $day/$month/$year: ") {
            document.select(".govuk-error-summary a").size() mustEqual 1
            document.select(".govuk-error-summary a").text() mustEqual msgs("controllingBodySoleProprietorDob.error.invalid")
            document.select(".govuk-error-summary a").attr("href") mustEqual s"#value.${invalidFields.head}"
            document.select(".govuk-error-message").text() mustEqual s"Error: ${msgs("controllingBodySoleProprietorDob.error.invalid")}"
            highlightedFields mustEqual invalidFields
          }
        }
      }
    }

    "render the latest date error once in the error summary" in {

      val today = LocalDate.of(2026, 9, 5)

      val application = applicationBuilder()
        .overrides(bind[Clock].toInstance(Clock.fixed(today.atStartOfDay(ZoneOffset.UTC).toInstant, ZoneOffset.UTC)))
        .build()

      running(application) {

        implicit val msgs: Messages = messages(application)

        val view =
          application.injector.instanceOf[ControllingBodySoleProprietorDobView]

        val form =
          application.injector.instanceOf[ControllingBodySoleProprietorDobFormProvider].apply()

        val boundForm = form.bind(
          Map(
            "value.day"   -> "5",
            "value.month" -> "9",
            "value.year"  -> "2026"
          )
        )

        val document: Document = Jsoup.parse(view(boundForm, NormalMode)(FakeRequest(), msgs).toString)

        document.select(".govuk-error-summary a").size() mustEqual 1
        document.select(".govuk-error-summary a").text() mustEqual "Date of birth must be the same as or before 4 September 2026"
        document.select(".govuk-error-summary a").attr("href") mustEqual "#value.day"
      }
    }
  }
}
