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

package forms

import java.time.{LocalDate, ZoneOffset}
import forms.behaviours.DateBehaviours
import forms.controllingbody.DateOfIncorporationFormProvider
import play.api.data.FormError
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages

class DateOfIncorporationFormProviderSpec extends DateBehaviours {

  private implicit val messages: Messages = stubMessages()
  private val form = new DateOfIncorporationFormProvider()()

  ".dateOfIncorporation" - {

    val validData = datesBetween(
      min = LocalDate.of(2000, 1, 1),
      max = LocalDate.now(ZoneOffset.UTC)
    )

    behave like dateField(form, "dateOfIncorporation", validData)

    behave like mandatoryDateField(form, "dateOfIncorporation", "dateOfIncorporation.error.required.all")

    val validDate = Map("dateOfIncorporation.day" -> "27", "dateOfIncorporation.month" -> "3", "dateOfIncorporation.year" -> "2007")
    Seq(
      Seq("day"),
      Seq("month"),
      Seq("year"),
      Seq("day", "month"),
      Seq("month", "year"),
      Seq("day", "year")
    ).foreach { missing =>
      s"reject a date missing ${missing.mkString(" and ")}" in {
        val errorKey = if (missing.size == 1) "dateOfIncorporation.error.required" else "dateOfIncorporation.error.required.two"
        val expectedArgs = missing.map(part => messages(s"date.error.$part"))
        Seq(
          validDate -- missing.map(part => s"dateOfIncorporation.$part"),
          validDate ++ missing.map(part => s"dateOfIncorporation.$part" -> "")
        ).foreach { data =>
          form.bind(data).errors must contain only FormError("dateOfIncorporation", errorKey, expectedArgs)
        }
      }
    }

    Seq(
      ("31", "4", "2007"),
      ("29", "2", "2007"),
      ("0", "3", "2007"),
      ("27", "13", "2007"),
      ("abc", "3", "2007"),
      ("27", "abc", "2007"),
      ("27", "3", "abc"),
      ("1.5", "3", "2007")
    ).foreach { case (day, month, year) =>
      s"reject the invalid date $day/$month/$year" in {
        form.bind(Map("dateOfIncorporation.day" -> day, "dateOfIncorporation.month" -> month, "dateOfIncorporation.year" -> year)).errors must
          contain only FormError("dateOfIncorporation", "dateOfIncorporation.error.invalid")
      }
    }

    "accept a valid leap day" in {
      form
        .bind(Map("dateOfIncorporation.day" -> "29", "dateOfIncorporation.month" -> "2", "dateOfIncorporation.year" -> "2008"))
        .value
        .value mustEqual
        LocalDate.of(2008, 2, 29)
    }

  }
}
