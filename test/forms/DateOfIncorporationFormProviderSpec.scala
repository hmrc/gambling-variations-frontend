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

import forms.behaviours.DateBehaviours
import forms.controllingbody.DateOfIncorporationFormProvider
import play.api.data.FormError
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages

import java.time.{LocalDate, ZoneOffset}

class DateOfIncorporationFormProviderSpec extends DateBehaviours {

  private implicit val messages: Messages = stubMessages()
  private val form = new DateOfIncorporationFormProvider()()

  ".value" - {

    val validData = datesBetween(
      min = LocalDate.of(2000, 1, 1),
      max = LocalDate.now(ZoneOffset.UTC)
    )

    behave like dateField(form, "value", validData)

    behave like mandatoryDateField(
      form,
      "value",
      "dateOfIncorporation.error.required.all"
    )

    val validDate = Map(
      "value.day"   -> "27",
      "value.month" -> "3",
      "value.year"  -> "2007"
    )

    Seq(
      Seq("day"),
      Seq("month"),
      Seq("year"),
      Seq("day", "month"),
      Seq("month", "year"),
      Seq("day", "year")
    ).foreach { missing =>
      s"reject a date missing ${missing.mkString(" and ")}" in {

        val errorKey =
          if (missing.size == 1) {
            "dateOfIncorporation.error.required"
          } else {
            "dateOfIncorporation.error.required.two"
          }

        val expectedArgs =
          missing.map(part => messages(s"date.error.$part"))

        Seq(
          validDate -- missing.map(part => s"value.$part"),
          validDate ++ missing.map(part => s"value.$part" -> "")
        ).foreach { data =>
          form.bind(data).errors must contain only FormError(
            "value",
            errorKey,
            expectedArgs
          )
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

        val errors =
          form
            .bind(
              Map(
                "value.day"   -> day,
                "value.month" -> month,
                "value.year"  -> year
              )
            )
            .errors

        errors.map(_.message) must contain(
          "dateOfIncorporation.error.invalid"
        )
      }
    }

    Seq(
      "1",
      "99",
      "999",
      "10000",
      "999999999",
      "00002007",
      "+2007",
      "-2007"
    ).foreach { year =>
      s"reject year $year because it does not contain exactly four valid digits" in {

        val errors =
          form
            .bind(
              validDate.updated("value.year", year)
            )
            .errors

        errors.map(_.message) must contain(
          "dateOfIncorporation.error.invalid"
        )

        errors.map(_.key) must contain("value.year")
      }
    }

    Seq("1000", "2007", "9999").foreach { year =>
      s"accept four-digit year $year" in {

        form
          .bind(
            validDate.updated("value.year", year)
          )
          .value
          .value mustEqual
          LocalDate.of(year.toInt, 3, 27)
      }
    }

    Seq("day", "month").foreach { part =>
      Seq(
        "001",
        "000000001",
        "123",
        "+1",
        "-1"
      ).foreach { value =>
        s"reject $part $value because it does not contain one or two digits" in {

          val errors =
            form
              .bind(
                validDate.updated(s"value.$part", value)
              )
              .errors

          errors.map(_.message) must contain(
            "dateOfIncorporation.error.invalid"
          )

          errors.map(_.key) must contain(s"value.$part")
        }
      }
    }

    Seq(
      ("1", "3"),
      ("01", "03"),
      ("27", "12")
    ).foreach { case (day, month) =>
      s"accept day $day and month $month" in {

        form
          .bind(
            validDate ++ Map(
              "value.day"   -> day,
              "value.month" -> month
            )
          )
          .value
          .value mustEqual
          LocalDate.of(2007, month.toInt, day.toInt)
      }
    }

    "accept a valid leap day" in {

      form
        .bind(
          Map(
            "value.day"   -> "29",
            "value.month" -> "2",
            "value.year"  -> "2008"
          )
        )
        .value
        .value mustEqual
        LocalDate.of(2008, 2, 29)
    }
  }
}
