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

package forms.mappings

import generators.Generators
import org.scalacheck.Gen
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.data.{Form, FormError}
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages

import java.time.LocalDate

class DateMappingsSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with Generators with OptionValues with Mappings {

  private implicit val messages: Messages =
    stubMessages()

  private val form = Form(
    "value" -> localDate(
      requiredKey              = "error.required",
      allRequiredKey           = "error.required.all",
      twoRequiredKey           = "error.required.two",
      invalidKey               = "error.invalid",
      requireNumericDateFormat = false
    )
  )

  private val numericForm = Form(
    "value" -> localDate(
      requiredKey    = "error.required",
      allRequiredKey = "error.required.all",
      twoRequiredKey = "error.required.two",
      invalidKey     = "error.invalid"
    )
  )

  private val validData = datesBetween(
    min = LocalDate.of(2000, 1, 1),
    max = LocalDate.of(3000, 1, 1)
  )

  private val invalidField: Gen[String] =
    Gen.alphaStr.map(value => s"$$$value")

  private val missingField: Gen[Option[String]] =
    Gen.option(Gen.const(""))

  private val singleSpace = " "
  private val multipleSpace = "   "
  private val tab = "\t"

  "LocalDate mapping" - {

    "must bind valid dates with months provided as numbers" in {

      forAll(validData -> "valid date") { date =>

        val data = Map(
          "value.day"   -> date.getDayOfMonth.toString,
          "value.month" -> date.getMonthValue.toString,
          "value.year"  -> date.getYear.toString
        )

        val result = form.bind(data)

        result.value.value mustEqual date
        result.errors mustBe empty
      }
    }

    "must bind valid dates with months provided as numbers with leading zeroes" in {

      forAll(validData -> "valid date") { date =>

        val data = Map(
          "value.day"   -> date.getDayOfMonth.toString,
          "value.month" -> f"${date.getMonthValue}%02d",
          "value.year"  -> date.getYear.toString
        )

        val result = form.bind(data)

        result.value.value mustEqual date
        result.errors mustBe empty
      }
    }

    "must bind valid dates with months provided as full names in upper case" in {

      forAll(validData -> "valid date") { date =>

        val data = Map(
          "value.day"   -> date.getDayOfMonth.toString,
          "value.month" -> date.getMonth.toString,
          "value.year"  -> date.getYear.toString
        )

        val result = form.bind(data)

        result.value.value mustEqual date
        result.errors mustBe empty
      }
    }

    "must bind valid dates with months provided as full names in lower case" in {

      forAll(validData -> "valid date") { date =>

        val data = Map(
          "value.day"   -> date.getDayOfMonth.toString,
          "value.month" -> date.getMonth.toString.toLowerCase,
          "value.year"  -> date.getYear.toString
        )

        val result = form.bind(data)

        result.value.value mustEqual date
        result.errors mustBe empty
      }
    }

    "must bind valid dates with months provided as three characters in upper case" in {

      forAll(validData -> "valid date") { date =>

        val data = Map(
          "value.day"   -> date.getDayOfMonth.toString,
          "value.month" -> date.getMonth.toString.take(3),
          "value.year"  -> date.getYear.toString
        )

        val result = form.bind(data)

        result.value.value mustEqual date
        result.errors mustBe empty
      }
    }

    "must bind valid dates with months provided as three characters in lower case" in {

      forAll(validData -> "valid date") { date =>

        val data = Map(
          "value.day"   -> date.getDayOfMonth.toString,
          "value.month" -> date.getMonth.toString.take(3).toLowerCase,
          "value.year"  -> date.getYear.toString
        )

        val result = form.bind(data)

        result.value.value mustEqual date
        result.errors mustBe empty
      }
    }

    "must bind valid dates by removing leading and trailing whitespace" in {

      forAll(validData -> "valid date") { date =>

        val data = Map(
          "value.day"   -> s"$multipleSpace${date.getDayOfMonth}$singleSpace",
          "value.month" -> s"$tab${date.getMonthValue}$multipleSpace",
          "value.year"  -> s"$singleSpace${date.getYear}$tab"
        )

        val result = form.bind(data)

        result.value.value mustEqual date
        result.errors mustBe empty
      }
    }

    "must bind valid dates by removing whitespace between digits" in {

      val data = Map(
        "value.day"   -> s"1${singleSpace}2",
        "value.month" -> s"0${multipleSpace}3",
        "value.year"  -> s"2${singleSpace}0${tab}2${multipleSpace}6"
      )

      val result = form.bind(data)

      result.value.value mustEqual LocalDate.of(2026, 3, 12)
      result.errors mustBe empty
    }

    "must fail to bind a whitespace-only day as a missing day" in {

      val result = form.bind(
        Map(
          "value.day"   -> multipleSpace,
          "value.month" -> "3",
          "value.year"  -> "2026"
        )
      )

      result.errors must contain only FormError(
        "value",
        "error.required",
        List(messages("date.error.day"))
      )
    }

    "must fail to bind an empty date" in {

      val result = form.bind(Map.empty[String, String])

      result.errors must contain only FormError(
        "value",
        "error.required.all",
        List.empty
      )
    }

    "must fail to bind a date with a missing day" in {

      forAll(
        validData    -> "valid date",
        missingField -> "missing field"
      ) { (date, field) =>

        val initialData = Map(
          "value.month" -> date.getMonthValue.toString,
          "value.year"  -> date.getYear.toString
        )

        val data = field.fold(initialData) { value =>
          initialData + ("value.day" -> value)
        }

        val result = form.bind(data)

        result.errors must contain only FormError(
          "value",
          "error.required",
          List(messages("date.error.day"))
        )
      }
    }

    "must fail to bind a date with an invalid day" in {

      forAll(
        validData    -> "valid date",
        invalidField -> "invalid field"
      ) { (date, field) =>

        val data = Map(
          "value.day"   -> field,
          "value.month" -> date.getMonthValue.toString,
          "value.year"  -> date.getYear.toString
        )

        val result = form.bind(data)

        result.errors must contain only FormError(
          "value.day",
          "error.invalid",
          List.empty
        )
      }
    }

    "must fail to bind a date with a missing month" in {

      forAll(
        validData    -> "valid date",
        missingField -> "missing field"
      ) { (date, field) =>

        val initialData = Map(
          "value.day"  -> date.getDayOfMonth.toString,
          "value.year" -> date.getYear.toString
        )

        val data = field.fold(initialData) { value =>
          initialData + ("value.month" -> value)
        }

        val result = form.bind(data)

        result.errors must contain only FormError(
          "value",
          "error.required",
          List(messages("date.error.month"))
        )
      }
    }

    "must fail to bind a date with an invalid month" in {

      forAll(
        validData    -> "valid date",
        invalidField -> "invalid field"
      ) { (date, field) =>

        val data = Map(
          "value.day"   -> date.getDayOfMonth.toString,
          "value.month" -> field,
          "value.year"  -> date.getYear.toString
        )

        val result = form.bind(data)

        result.errors must contain only FormError(
          "value.month",
          "error.invalid",
          List.empty
        )
      }
    }

    "must fail to bind a date with a missing year" in {

      forAll(
        validData    -> "valid date",
        missingField -> "missing field"
      ) { (date, field) =>

        val initialData = Map(
          "value.day"   -> date.getDayOfMonth.toString,
          "value.month" -> date.getMonthValue.toString
        )

        val data = field.fold(initialData) { value =>
          initialData + ("value.year" -> value)
        }

        val result = form.bind(data)

        result.errors must contain only FormError(
          "value",
          "error.required",
          List(messages("date.error.year"))
        )
      }
    }

    "must fail to bind a date with an invalid year" in {

      forAll(
        validData    -> "valid date",
        invalidField -> "invalid field"
      ) { (date, field) =>

        val data = Map(
          "value.day"   -> date.getDayOfMonth.toString,
          "value.month" -> date.getMonthValue.toString,
          "value.year"  -> field
        )

        val result = form.bind(data)

        result.errors must contain only FormError(
          "value.year",
          "error.invalid",
          List.empty
        )
      }
    }

    "must fail to bind a date with a missing day and month" in {

      forAll(
        validData    -> "valid date",
        missingField -> "missing day",
        missingField -> "missing month"
      ) { (date, dayOption, monthOption) =>

        val day = dayOption.fold(Map.empty[String, String]) { value =>
          Map("value.day" -> value)
        }

        val month = monthOption.fold(Map.empty[String, String]) { value =>
          Map("value.month" -> value)
        }

        val data =
          Map("value.year" -> date.getYear.toString) ++ day ++ month

        val result = form.bind(data)

        result.errors must contain only FormError(
          "value",
          "error.required.two",
          List(
            messages("date.error.day"),
            messages("date.error.month")
          )
        )
      }
    }

    "must fail to bind a date with a missing day and year" in {

      forAll(
        validData    -> "valid date",
        missingField -> "missing day",
        missingField -> "missing year"
      ) { (date, dayOption, yearOption) =>

        val day = dayOption.fold(Map.empty[String, String]) { value =>
          Map("value.day" -> value)
        }

        val year = yearOption.fold(Map.empty[String, String]) { value =>
          Map("value.year" -> value)
        }

        val data =
          Map("value.month" -> date.getMonthValue.toString) ++ day ++ year

        val result = form.bind(data)

        result.errors must contain only FormError(
          "value",
          "error.required.two",
          List(
            messages("date.error.day"),
            messages("date.error.year")
          )
        )
      }
    }

    "must fail to bind a date with a missing month and year" in {

      forAll(
        validData    -> "valid date",
        missingField -> "missing month",
        missingField -> "missing year"
      ) { (date, monthOption, yearOption) =>

        val month = monthOption.fold(Map.empty[String, String]) { value =>
          Map("value.month" -> value)
        }

        val year = yearOption.fold(Map.empty[String, String]) { value =>
          Map("value.year" -> value)
        }

        val data =
          Map("value.day" -> date.getDayOfMonth.toString) ++ month ++ year

        val result = form.bind(data)

        result.errors must contain only FormError(
          "value",
          "error.required.two",
          List(
            messages("date.error.month"),
            messages("date.error.year")
          )
        )
      }
    }

    "must return the first invalid field when numeric format is not required" - {

      "when day and month are invalid" in {

        val result = form.bind(
          Map(
            "value.day"   -> "$",
            "value.month" -> "$",
            "value.year"  -> "2026"
          )
        )

        result.errors must contain only FormError(
          "value.day",
          "error.invalid",
          List.empty
        )
      }

      "when day and year are invalid" in {

        val result = form.bind(
          Map(
            "value.day"   -> "$",
            "value.month" -> "3",
            "value.year"  -> "$"
          )
        )

        result.errors must contain only FormError(
          "value.day",
          "error.invalid",
          List.empty
        )
      }

      "when month and year are invalid" in {

        val result = form.bind(
          Map(
            "value.day"   -> "12",
            "value.month" -> "$",
            "value.year"  -> "$"
          )
        )

        result.errors must contain only FormError(
          "value.month",
          "error.invalid",
          List.empty
        )
      }

      "when day, month and year are invalid" in {

        val result = form.bind(
          Map(
            "value.day"   -> "$",
            "value.month" -> "$",
            "value.year"  -> "$"
          )
        )

        result.errors must contain only FormError(
          "value.day",
          "error.invalid",
          List.empty
        )
      }
    }

    "must fail to bind a complete but impossible date" in {

      val data = Map(
        "value.day"   -> "30",
        "value.month" -> "2",
        "value.year"  -> "2018"
      )

      val result = form.bind(data)

      result.errors must contain only FormError(
        "value",
        "error.invalid",
        List.empty
      )
    }

    "when numeric date format is required" - {

      "must accept a valid numeric date" in {

        val result = numericForm.bind(
          Map(
            "value.day"   -> "12",
            "value.month" -> "3",
            "value.year"  -> "2026"
          )
        )

        result.value.value mustEqual LocalDate.of(2026, 3, 12)
        result.errors mustBe empty
      }

      "must accept leading zeroes for day and month" in {

        val result = numericForm.bind(
          Map(
            "value.day"   -> "01",
            "value.month" -> "03",
            "value.year"  -> "2026"
          )
        )

        result.value.value mustEqual LocalDate.of(2026, 3, 1)
        result.errors mustBe empty
      }

      "must accept spaces between numeric date digits" in {

        val result = numericForm.bind(
          Map(
            "value.day"   -> "1 2",
            "value.month" -> "0 3",
            "value.year"  -> "2 0 2 6"
          )
        )

        result.value.value mustEqual LocalDate.of(2026, 3, 12)
        result.errors mustBe empty
      }

      "must identify an invalid day" in {

        val result = numericForm.bind(
          Map(
            "value.day"   -> "$",
            "value.month" -> "3",
            "value.year"  -> "2026"
          )
        )

        result.errors must contain theSameElementsAs Seq(
          FormError(
            "value",
            "error.invalid",
            Seq(messages("date.error.day"))
          ),
          FormError(
            "value.day",
            "error.invalid",
            Seq.empty
          )
        )
      }

      "must identify an invalid month" in {

        val result = numericForm.bind(
          Map(
            "value.day"   -> "12",
            "value.month" -> "$",
            "value.year"  -> "2026"
          )
        )

        result.errors must contain theSameElementsAs Seq(
          FormError(
            "value",
            "error.invalid",
            Seq(messages("date.error.month"))
          ),
          FormError(
            "value.month",
            "error.invalid",
            Seq.empty
          )
        )
      }

      "must identify an invalid year" in {

        val result = numericForm.bind(
          Map(
            "value.day"   -> "12",
            "value.month" -> "3",
            "value.year"  -> "999"
          )
        )

        result.errors must contain theSameElementsAs Seq(
          FormError(
            "value",
            "error.invalid",
            Seq(messages("date.error.year"))
          ),
          FormError(
            "value.year",
            "error.invalid",
            Seq.empty
          )
        )
      }

      "must identify invalid day and month fields" in {

        val result = numericForm.bind(
          Map(
            "value.day"   -> "$",
            "value.month" -> "$",
            "value.year"  -> "2026"
          )
        )

        result.errors must contain theSameElementsAs Seq(
          FormError(
            "value",
            "error.invalid",
            Seq(
              messages("date.error.day"),
              messages("date.error.month")
            )
          ),
          FormError(
            "value.day",
            "error.invalid",
            Seq.empty
          ),
          FormError(
            "value.month",
            "error.invalid",
            Seq.empty
          )
        )
      }

      "must identify invalid day and year fields" in {

        val result = numericForm.bind(
          Map(
            "value.day"   -> "$",
            "value.month" -> "3",
            "value.year"  -> "999"
          )
        )

        result.errors must contain theSameElementsAs Seq(
          FormError(
            "value",
            "error.invalid",
            Seq(
              messages("date.error.day"),
              messages("date.error.year")
            )
          ),
          FormError(
            "value.day",
            "error.invalid",
            Seq.empty
          ),
          FormError(
            "value.year",
            "error.invalid",
            Seq.empty
          )
        )
      }

      "must identify invalid month and year fields" in {

        val result = numericForm.bind(
          Map(
            "value.day"   -> "12",
            "value.month" -> "$",
            "value.year"  -> "999"
          )
        )

        result.errors must contain theSameElementsAs Seq(
          FormError(
            "value",
            "error.invalid",
            Seq(
              messages("date.error.month"),
              messages("date.error.year")
            )
          ),
          FormError(
            "value.month",
            "error.invalid",
            Seq.empty
          ),
          FormError(
            "value.year",
            "error.invalid",
            Seq.empty
          )
        )
      }

      "must identify every invalid date field" in {

        val result = numericForm.bind(
          Map(
            "value.day"   -> "$",
            "value.month" -> "$",
            "value.year"  -> "$"
          )
        )

        result.errors must contain theSameElementsAs Seq(
          FormError(
            "value",
            "error.invalid",
            Seq(
              messages("date.error.day"),
              messages("date.error.month"),
              messages("date.error.year")
            )
          ),
          FormError(
            "value.day",
            "error.invalid",
            Seq.empty
          ),
          FormError(
            "value.month",
            "error.invalid",
            Seq.empty
          ),
          FormError(
            "value.year",
            "error.invalid",
            Seq.empty
          )
        )
      }

      "must accept the minimum four-digit year 1000" in {

        val result = numericForm.bind(
          Map(
            "value.day"   -> "12",
            "value.month" -> "3",
            "value.year"  -> "1000"
          )
        )

        result.value.value mustEqual LocalDate.of(1000, 3, 12)
        result.errors mustBe empty
      }

      "must reject a three-digit year" in {

        val result = numericForm.bind(
          Map(
            "value.day"   -> "12",
            "value.month" -> "3",
            "value.year"  -> "999"
          )
        )

        result.errors.map(_.key) must contain allOf (
          "value",
          "value.year"
        )
      }

      "must reject a four-character year below 1000" in {

        val result = numericForm.bind(
          Map(
            "value.day"   -> "12",
            "value.month" -> "3",
            "value.year"  -> "0999"
          )
        )

        result.errors.map(_.key) must contain allOf (
          "value",
          "value.year"
        )
      }

      "must reject a five-digit year" in {

        val result = numericForm.bind(
          Map(
            "value.day"   -> "12",
            "value.month" -> "3",
            "value.year"  -> "10000"
          )
        )

        result.errors.map(_.key) must contain allOf (
          "value",
          "value.year"
        )
      }

      "must reject a named month" in {

        val result = numericForm.bind(
          Map(
            "value.day"   -> "12",
            "value.month" -> "March",
            "value.year"  -> "2026"
          )
        )

        result.errors.map(_.key) must contain allOf (
          "value",
          "value.month"
        )
      }

      "must reject a complete but impossible numeric date" in {

        val result = numericForm.bind(
          Map(
            "value.day"   -> "30",
            "value.month" -> "2",
            "value.year"  -> "2026"
          )
        )

        result.errors must contain only FormError(
          "value",
          "error.invalid",
          List.empty
        )
      }

      Seq("0", "00", "32", "99").foreach { day =>
        s"must identify the day $day as an invalid day" in {

          val result = numericForm.bind(
            Map(
              "value.day"   -> day,
              "value.month" -> "3",
              "value.year"  -> "2026"
            )
          )

          result.errors must contain theSameElementsAs Seq(
            FormError("value", "error.invalid", Seq(messages("date.error.day"))),
            FormError("value.day", "error.invalid", Seq.empty)
          )
        }
      }

      Seq("0", "00", "13", "99").foreach { month =>
        s"must identify the month $month as an invalid month" in {

          val result = numericForm.bind(
            Map(
              "value.day"   -> "12",
              "value.month" -> month,
              "value.year"  -> "2026"
            )
          )

          result.errors must contain theSameElementsAs Seq(
            FormError("value", "error.invalid", Seq(messages("date.error.month"))),
            FormError("value.month", "error.invalid", Seq.empty)
          )
        }
      }

      Seq(
        ("$", "$", "", Seq("day", "month")),
        ("32", "3", "", Seq("day")),
        ("12", "13", "", Seq("month")),
        ("", "13", "", Seq("month")),
        ("", "3", "999", Seq("year"))
      ).foreach { case (day, month, year, invalidFields) =>
        s"must identify the invalid fields of $day/$month/$year before the missing fields" in {

          val result = numericForm.bind(
            Map(
              "value.day"   -> day,
              "value.month" -> month,
              "value.year"  -> year
            )
          )

          result.errors must contain theSameElementsAs (
            FormError("value", "error.invalid", invalidFields.map(field => messages(s"date.error.$field"))) +:
              invalidFields.map(field => FormError(s"value.$field", "error.invalid", Seq.empty))
          )
        }
      }
    }

    "must unbind a date" in {

      forAll(validData -> "valid date") { date =>

        val filledForm = form.fill(date)

        filledForm("value.day").value.value mustEqual
          date.getDayOfMonth.toString

        filledForm("value.month").value.value mustEqual
          date.getMonthValue.toString

        filledForm("value.year").value.value mustEqual
          date.getYear.toString
      }
    }
  }
}
