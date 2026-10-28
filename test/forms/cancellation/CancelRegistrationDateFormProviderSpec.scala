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

package forms.cancellation

import forms.behaviours.DateBehaviours
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages

import java.time.LocalDate

class CancelRegistrationDateFormProviderSpec extends DateBehaviours {

  private implicit val messages: Messages = stubMessages()

  private val registrationDate =
    LocalDate.of(2007, 3, 20)

  private val maximumValidDate =
    LocalDate.of(2026, 11, 4)

  private val firstInvalidDate =
    LocalDate.of(2026, 11, 5)

  private val form =
    new CancelRegistrationDateFormProvider()(
      registrationDate,
      maximumValidDate
    )

  private def dateData(date: LocalDate): Map[String, String] =
    Map(
      "value.day"   -> date.getDayOfMonth.toString,
      "value.month" -> date.getMonthValue.toString,
      "value.year"  -> date.getYear.toString
    )

  ".value" - {

    "must bind a valid date between the boundaries" in {
      val date = LocalDate.of(2017, 1, 27)

      val result = form.bind(dateData(date))

      result.errors mustBe empty
      result.value.value mustBe date
    }

    "must bind the registration date" in {
      val result = form.bind(dateData(registrationDate))

      result.errors mustBe empty
      result.value.value mustBe registrationDate
    }

    "must bind the maximum valid date" in {
      val result = form.bind(dateData(maximumValidDate))

      result.errors mustBe empty
      result.value.value mustBe maximumValidDate
    }

    "must reject a date before the registration date" in {
      val result =
        form.bind(dateData(registrationDate.minusDays(1)))

      result.errors.map(_.message) must contain(
        "cancelRegistrationDate.error.outsideBoundaries"
      )
    }

    "must reject the first invalid date" in {
      val result =
        form.bind(dateData(firstInvalidDate))

      result.errors.map(_.message) must contain(
        "cancelRegistrationDate.error.outsideBoundaries"
      )
    }

    "must reject a date after the first invalid date" in {
      val result =
        form.bind(dateData(firstInvalidDate.plusDays(1)))

      result.errors.map(_.message) must contain(
        "cancelRegistrationDate.error.outsideBoundaries"
      )
    }

    "must provide the formatted date boundaries as error arguments when the date is before registration" in {
      val result =
        form.bind(dateData(registrationDate.minusDays(1)))

      val error =
        result.errors
          .find(
            _.message == "cancelRegistrationDate.error.outsideBoundaries"
          )
          .value

      error.args mustBe Seq(
        "20 March 2007",
        "5 November 2026"
      )
    }

    "must provide the formatted date boundaries as error arguments when the date is after the maximum valid date" in {
      val result =
        form.bind(dateData(firstInvalidDate))

      val error =
        result.errors
          .find(
            _.message == "cancelRegistrationDate.error.outsideBoundaries"
          )
          .value

      error.args mustBe Seq(
        "20 March 2007",
        "5 November 2026"
      )
    }

    "must reject an invalid date" in {
      val result =
        form.bind(
          Map(
            "value.day"   -> "not-a-day",
            "value.month" -> "3",
            "value.year"  -> "2026"
          )
        )

      result.errors.map(_.message) must contain(
        "cancelRegistrationDate.error.invalid"
      )
    }

    "must reject a missing date" in {
      val result =
        form.bind(Map.empty[String, String])

      result.errors.map(_.message) must contain(
        "cancelRegistrationDate.error.required.all"
      )
    }

    "must reject a date with only the day provided" in {
      val result =
        form.bind(
          Map(
            "value.day"   -> "20",
            "value.month" -> "",
            "value.year"  -> ""
          )
        )

      result.errors.map(_.message) must contain(
        "cancelRegistrationDate.error.required.two"
      )
    }

    "must reject a date with one component missing" in {
      val result =
        form.bind(
          Map(
            "value.day"   -> "20",
            "value.month" -> "3",
            "value.year"  -> ""
          )
        )

      result.errors.map(_.message) must contain(
        "cancelRegistrationDate.error.required"
      )
    }
  }
}
