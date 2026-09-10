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

import forms.mappings.Mappings
import play.api.data.Form
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}
import play.api.i18n.Messages

import java.time.format.DateTimeFormatter
import java.time.{Clock, LocalDate}
import java.util.Locale
import javax.inject.Inject

class PartnerDeleteDateFormProvider @Inject() (clock: Clock) extends Mappings {

  def apply(
    registrationDate: LocalDate
  )(implicit messages: Messages): Form[LocalDate] = {

    val currentDate = LocalDate.now(clock)

    val latestDate =
      if (registrationDate.isAfter(currentDate)) {
        registrationDate.plusDays(14)
      } else {
        currentDate.plusDays(14)
      }

    Form(
      "value" -> localDate(
        invalidKey     = "partnerDeleteDate.error.invalid",
        allRequiredKey = "partnerDeleteDate.error.required.all",
        twoRequiredKey = "partnerDeleteDate.error.required.two",
        requiredKey    = "partnerDeleteDate.error.required"
      ).verifying(
        beforeLatestDateConstraint(latestDate)
      )
    )
  }

  private def beforeLatestDateConstraint(
    latestDate: LocalDate
  )(implicit messages: Messages): Constraint[LocalDate] =
    Constraint { removeDate =>
      if (removeDate.isAfter(latestDate)) {
        Invalid(
          ValidationError(
            "partnerDeleteDate.error.afterLatestDate",
            formatDate(latestDate.plusDays(1))
          )
        )
      } else {
        Valid
      }
    }

  private def formatDate(date: LocalDate)(implicit messages: Messages): String = {
    val locale = Locale.forLanguageTag(messages.lang.code)
    val formatter = DateTimeFormatter.ofPattern("d MMMM uuuu", locale)

    date.format(formatter)
  }
}
