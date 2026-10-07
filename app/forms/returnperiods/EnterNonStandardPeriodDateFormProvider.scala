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

import forms.mappings.Mappings
import play.api.data.Form
import play.api.data.validation.{Constraint, Invalid, Valid}
import play.api.i18n.Messages

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

class EnterNonStandardPeriodDateFormProvider @Inject() extends Mappings {

  private val longDateFormatter =
    DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH)

  def apply(
    lowerBoundary: LocalDate,
    upperBoundary: LocalDate
  )(implicit messages: Messages): Form[LocalDate] = {

    val outsideBoundariesConstraint =
      Constraint[LocalDate] { date =>
        if (date.isAfter(lowerBoundary) && date.isBefore(upperBoundary)) {
          Valid
        } else {
          Invalid(
            "enterNonStandardPeriodDate.error.outsideBoundaries",
            lowerBoundary.format(longDateFormatter),
            upperBoundary.format(longDateFormatter)
          )
        }
      }

    Form(
      "value" -> localDate(
        invalidKey     = "enterNonStandardPeriodDate.error.invalid",
        allRequiredKey = "enterNonStandardPeriodDate.error.required.all",
        twoRequiredKey = "enterNonStandardPeriodDate.error.required.two",
        requiredKey    = "enterNonStandardPeriodDate.error.required"
      ).verifying(
        outsideBoundariesConstraint
      )
    )
  }
}
