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

class PartnerSoleProprietorDobFormProvider @Inject() (clock: Clock) extends Mappings {

  def apply()(implicit messages: Messages): Form[LocalDate] = {

    val today = LocalDate.now(clock)
    val earliestDate = today.minusYears(120)
    val latestDate = today.minusDays(1)

    Form(
      "value" -> localDate(
        invalidKey     = "partnerSoleProprietorDob.error.invalid",
        allRequiredKey = "partnerSoleProprietorDob.error.required.all",
        twoRequiredKey = "partnerSoleProprietorDob.error.required.two",
        requiredKey    = "partnerSoleProprietorDob.error.required"
      ).verifying(
        dateOfBirthConstraint(earliestDate, latestDate)
      )
    )
  }

  private def dateOfBirthConstraint(
    earliestDate: LocalDate,
    latestDate: LocalDate
  )(implicit messages: Messages): Constraint[LocalDate] =
    Constraint { dateOfBirth =>
      if (dateOfBirth.isBefore(earliestDate)) {
        Invalid(
          ValidationError(
            "partnerSoleProprietorDob.error.beforeEarliestDate",
            formatDate(earliestDate)
          )
        )
      } else if (dateOfBirth.isAfter(latestDate)) {
        Invalid(
          ValidationError(
            "partnerSoleProprietorDob.error.afterLatestDate",
            formatDate(latestDate)
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
