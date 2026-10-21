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
import forms.controllingbody.DateOfIncorporationFormProvider
import org.jsoup.Jsoup
import play.api.test.FakeRequest
import play.api.test.Helpers.running
import views.html.controllingbody.DateOfIncorporationView

class DateOfIncorporationViewSpec extends SpecBase {
  "DateOfIncorporationView" - {
    "render the caption, heading, hint and date fields from the design" in {
      val application = applicationBuilder().build()
      running(application) {
        val view = application.injector.instanceOf[DateOfIncorporationView]
        val form = new DateOfIncorporationFormProvider()()(messages(application))
        val document = Jsoup.parse(view(form)(FakeRequest(), messages(application)).toString)

        document.select(".govuk-caption-l").text() mustEqual "Change registration details"
        document.select("h1").text() mustEqual "When was the controlling body’s date of incorporation?"
        document.title() must include("When was the controlling body’s date of incorporation?")
        document.title() must include("Change registration details")
        document.select("#dateOfIncorporation-hint").text() mustEqual "For example, 27 3 2007"
        document.select(".govuk-date-input label").eachText().toArray.toSeq mustEqual Seq("Day", "Month", "Year")
        Seq("day", "month", "year").foreach { part =>
          val input = document.getElementById(s"dateOfIncorporation.$part")
          input.attr("name") mustEqual s"dateOfIncorporation.$part"
          input.attr("value") mustEqual ""
        }
        document.select("form").attr("action") mustEqual
          controllers.controllingbody.routes.DateOfIncorporationController.onSubmit().url
        document.select(".govuk-button").text() mustEqual "Continue"

        val errors = form.bind(Map("dateOfIncorporation.day" -> "", "dateOfIncorporation.month" -> "", "dateOfIncorporation.year" -> ""))
        val errorDocument = Jsoup.parse(view(errors)(FakeRequest(), messages(application)).toString)
        errorDocument.select(".govuk-error-summary").size() mustEqual 1
        errorDocument.select(".govuk-error-summary a").attr("href") mustEqual "#dateOfIncorporation.day"
        errorDocument.select("#dateOfIncorporation-hint").text() mustEqual "For example, 27 3 2007"
      }
    }

    "render the exact validation messages in the summary and beside the date input" in {
      val application = applicationBuilder().build()
      running(application) {
        val view = application.injector.instanceOf[DateOfIncorporationView]
        val form = new DateOfIncorporationFormProvider()()(messages(application))
        val cases = Seq(
          ("", "", "", "Enter a date of incorporation for the controlling body"),
          ("", "3", "2007", "Date of incorporation must include a day"),
          ("27", "", "2007", "Date of incorporation must include a month"),
          ("27", "3", "", "Date of incorporation must include a year"),
          ("", "", "2007", "Date of incorporation must include a day and a month"),
          ("27", "", "", "Date of incorporation must include a month and a year"),
          ("", "3", "", "Date of incorporation must include a day and a year"),
          ("31", "4", "2007", "Date of incorporation must be a real date"),
          ("27", "3", "20077", "Date of incorporation must be a real date"),
          ("001", "3", "2007", "Date of incorporation must be a real date"),
          ("27", "003", "2007", "Date of incorporation must be a real date")
        )
        cases.foreach { case (day, month, year, expected) =>
          val boundForm = form.bind(Map("dateOfIncorporation.day" -> day, "dateOfIncorporation.month" -> month, "dateOfIncorporation.year" -> year))
          val document = Jsoup.parse(view(boundForm)(FakeRequest(), messages(application)).toString)
          withClue(s"Date $day/$month/$year: ") {
            document.select(".govuk-error-summary a").text() mustEqual expected
            document.select(".govuk-error-message").text() mustEqual s"Error: $expected"
            document.select("#dateOfIncorporation-hint").text() mustEqual "For example, 27 3 2007"
          }
        }
      }
    }
  }
}
