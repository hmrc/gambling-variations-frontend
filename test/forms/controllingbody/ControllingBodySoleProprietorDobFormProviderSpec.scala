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

package forms.controllingbody

import forms.behaviours.DateBehaviours
import play.api.data.{Form, FormError}
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages

import java.time.{Clock, LocalDate, ZoneOffset}

class ControllingBodySoleProprietorDobFormProviderSpec extends DateBehaviours {

  private implicit val messages: Messages = stubMessages()

  private val today: LocalDate =
    LocalDate.of(2026, 9, 5)

  private val clock: Clock =
    Clock.fixed(
      today.atStartOfDay(ZoneOffset.UTC).toInstant,
      ZoneOffset.UTC
    )

  private val form: Form[LocalDate] =
    new ControllingBodySoleProprietorDobFormProvider(clock)()

  ".value" - {

    val validData = datesBetween(
      min = today.minusYears(120),
      max = today.minusDays(1)
    )

    behave like dateField(form, "value", validData)

    behave like mandatoryDateField(
      form,
      "value",
      "controllingBodySoleProprietorDob.error.required.all"
    )
  }

  "must accept yesterday" in {
    val date = today.minusDays(1)

    val result = form.bind(
      Map(
        "value.day"   -> date.getDayOfMonth.toString,
        "value.month" -> date.getMonthValue.toString,
        "value.year"  -> date.getYear.toString
      )
    )

    result.hasErrors mustBe false
  }

  "must accept any date in past" in {
    val date = today.minusYears(120).minusDays(1)

    val result = form.bind(
      Map(
        "value.day"   -> date.getDayOfMonth.toString,
        "value.month" -> date.getMonthValue.toString,
        "value.year"  -> date.getYear.toString
      )
    )

    result.hasErrors mustBe false
  }

  "must reject today" in {
    val date = today

    val result = form.bind(
      Map(
        "value.day"   -> date.getDayOfMonth.toString,
        "value.month" -> date.getMonthValue.toString,
        "value.year"  -> date.getYear.toString
      )
    )

    result.errors.head.message mustBe
      "controllingBodySoleProprietorDob.error.afterLatestDate"
  }

  "must reject a future date" in {
    val date = today.plusDays(1)

    val result = form.bind(
      Map(
        "value.day"   -> date.getDayOfMonth.toString,
        "value.month" -> date.getMonthValue.toString,
        "value.year"  -> date.getYear.toString
      )
    )

    result.errors.head.message mustBe
      "controllingBodySoleProprietorDob.error.afterLatestDate"
  }

  Seq(
    ("$$", "me", "", Seq("day", "month")),
    ("32", "3", "", Seq("day")),
    ("2", "13", "", Seq("month")),
    ("", "13", "1998", Seq("month")),
    ("2", "13", "1998", Seq("month")),
    ("00", "3", "1998", Seq("day")),
    ("2", "March", "1998", Seq("month")),
    ("2", "3", "998", Seq("year")),
    ("2", "3", "98", Seq("year"))
  ).foreach { case (day, month, year, invalidFields) =>
    s"must report $day/$month/$year as not a real date and mark only its invalid fields" in {

      val result = form.bind(
        Map(
          "value.day"   -> day,
          "value.month" -> month,
          "value.year"  -> year
        )
      )

      result.errors must contain theSameElementsAs (
        FormError("value", "controllingBodySoleProprietorDob.error.invalid", invalidFields.map(part => messages(s"date.error.$part"))) +:
          invalidFields.map(part => FormError(s"value.$part", "controllingBodySoleProprietorDob.error.invalid"))
      )
    }
  }
}
