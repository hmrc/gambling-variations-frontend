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
import utils.DateTimeFormats.formatDate

import java.time.{Clock, LocalDate}
import javax.inject.Inject

class PartnerDateOfJoiningFormProvider @Inject() (clock: Clock) extends Mappings {

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
        invalidKey     = "partnerDateOfJoining.error.invalid",
        allRequiredKey = "partnerDateOfJoining.error.required.all",
        twoRequiredKey = "partnerDateOfJoining.error.required.two",
        requiredKey    = "partnerDateOfJoining.error.required"
      ).verifying(
        beforeLatestDateConstraint(latestDate)
      )
    )
  }

  private def beforeLatestDateConstraint(
    latestDate: LocalDate
  )(implicit messages: Messages): Constraint[LocalDate] =
    Constraint { joiningDate =>
      if (joiningDate.isAfter(latestDate)) {
        Invalid(
          ValidationError(
            "partnerDateOfJoining.error.beforeRegistrationDate",
            formatDate(latestDate.plusDays(1))
          )
        )
      } else {
        Valid
      }
    }

}
