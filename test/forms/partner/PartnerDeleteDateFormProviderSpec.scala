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

package forms.partner

import forms.behaviours.DateBehaviours
import play.api.data.Form
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages

import java.time.{Clock, LocalDate, ZoneOffset}

class PartnerDeleteDateFormProviderSpec extends DateBehaviours {

  private implicit val messages: Messages =
    stubMessages()

  private val today: LocalDate =
    LocalDate.of(2026, 9, 5)

  private val clock: Clock =
    Clock.fixed(
      today.atStartOfDay(ZoneOffset.UTC).toInstant,
      ZoneOffset.UTC
    )

  private val registrationDate: LocalDate =
    LocalDate.of(2020, 1, 1)

  private val form: Form[LocalDate] =
    new PartnerDeleteDateFormProvider(clock)(
      registrationDate
    )

  ".value" - {

    val validData =
      datesBetween(
        min = registrationDate,
        max = today.plusDays(14)
      )

    behave like dateField(
      form,
      "value",
      validData
    )

    behave like mandatoryDateField(
      form,
      "value",
      "partnerDeleteDate.error.required.all"
    )
  }

  "must accept today" in {

    val date = today

    val result =
      form.bind(
        Map(
          "value.day"   -> date.getDayOfMonth.toString,
          "value.month" -> date.getMonthValue.toString,
          "value.year"  -> date.getYear.toString
        )
      )

    result.hasErrors mustBe false
  }

  "must accept the latest permitted date" in {

    val date =
      today.plusDays(14)

    val result =
      form.bind(
        Map(
          "value.day"   -> date.getDayOfMonth.toString,
          "value.month" -> date.getMonthValue.toString,
          "value.year"  -> date.getYear.toString
        )
      )

    result.hasErrors mustBe false
  }

  "must reject a date after the latest permitted date" in {

    val date =
      today.plusDays(15)

    val result =
      form.bind(
        Map(
          "value.day"   -> date.getDayOfMonth.toString,
          "value.month" -> date.getMonthValue.toString,
          "value.year"  -> date.getYear.toString
        )
      )

    result.errors.head.message mustBe
      "partnerDeleteDate.error.afterLatestDate"
  }

  "must use registration date plus 14 days when registration date is in the future" in {

    val futureRegistrationDate =
      today.plusDays(30)

    val futureForm =
      new PartnerDeleteDateFormProvider(clock)(
        futureRegistrationDate
      )

    val date =
      futureRegistrationDate.plusDays(15)

    val result =
      futureForm.bind(
        Map(
          "value.day"   -> date.getDayOfMonth.toString,
          "value.month" -> date.getMonthValue.toString,
          "value.year"  -> date.getYear.toString
        )
      )

    result.errors.head.message mustBe
      "partnerDeleteDate.error.afterLatestDate"
  }

  "must accept registration date plus 14 days when registration date is in the future" in {

    val futureRegistrationDate =
      today.plusDays(30)

    val futureForm =
      new PartnerDeleteDateFormProvider(clock)(
        futureRegistrationDate
      )

    val date =
      futureRegistrationDate.plusDays(14)

    val result =
      futureForm.bind(
        Map(
          "value.day"   -> date.getDayOfMonth.toString,
          "value.month" -> date.getMonthValue.toString,
          "value.year"  -> date.getYear.toString
        )
      )

    result.hasErrors mustBe false
  }
}
