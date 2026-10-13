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

package forms.returnperiods

import forms.behaviours.DateBehaviours
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages

import java.time.LocalDate

class EnterNonStandardPeriodDateFormProviderSpec extends DateBehaviours {

  private implicit val messages: Messages = stubMessages()

  private val lowerBoundary = LocalDate.of(2026, 1, 1)
  private val upperBoundary = LocalDate.of(2026, 2, 1)

  private val form =
    new EnterNonStandardPeriodDateFormProvider()(lowerBoundary, upperBoundary)

  private def dateData(date: LocalDate): Map[String, String] =
    Map(
      "value.day"   -> date.getDayOfMonth.toString,
      "value.month" -> date.getMonthValue.toString,
      "value.year"  -> date.getYear.toString
    )

  ".value" - {

    "must bind a valid date strictly between the boundaries" in {
      val date = LocalDate.of(2026, 1, 15)
      val result = form.bind(dateData(date))

      result.errors mustBe empty
      result.value.value mustBe date
    }

    "must reject a date equal to the lower boundary" in {
      val result = form.bind(dateData(lowerBoundary))

      result.errors.map(_.message) must contain(
        "enterNonStandardPeriodDate.error.outsideBoundaries"
      )
    }

    "must reject a date equal to the upper boundary" in {
      val result = form.bind(dateData(upperBoundary))

      result.errors.map(_.message) must contain(
        "enterNonStandardPeriodDate.error.outsideBoundaries"
      )
    }

    "must reject a date before the lower boundary" in {
      val result = form.bind(dateData(lowerBoundary.minusDays(1)))

      result.errors.map(_.message) must contain(
        "enterNonStandardPeriodDate.error.outsideBoundaries"
      )
    }

    "must reject a date after the upper boundary" in {
      val result = form.bind(dateData(upperBoundary.plusDays(1)))

      result.errors.map(_.message) must contain(
        "enterNonStandardPeriodDate.error.outsideBoundaries"
      )
    }

    "must reject an invalid date" in {
      val result = form.bind(
        Map(
          "value.day"   -> "not-a-day",
          "value.month" -> "1",
          "value.year"  -> "2026"
        )
      )

      result.errors.map(_.message) must contain(
        "enterNonStandardPeriodDate.error.invalid"
      )
    }

    "must reject a missing date" in {
      val result = form.bind(Map.empty[String, String])

      result.errors.map(_.message) must contain(
        "enterNonStandardPeriodDate.error.required.all"
      )
    }
  }
}
