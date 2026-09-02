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
import play.api.i18n.Messages

import java.time.LocalDate
import javax.inject.Inject

class PartnerDetailsAddDateOfJoiningPartnershipFormProvider @Inject() extends Mappings {

  def apply(dateOfJoining: LocalDate)(implicit messages: Messages): Form[LocalDate] = {
    val twoWeeks = dateOfJoining.plusDays(14)

    Form(
      "value" -> localDate(
        invalidKey     = "partnerDetailsAddDateOfJoiningPartnership.error.invalid",
        allRequiredKey = "partnerDetailsAddDateOfJoiningPartnership.error.required.all",
        twoRequiredKey = "partnerDetailsAddDateOfJoiningPartnership.error.required.two",
        requiredKey    = "partnerDetailsAddDateOfJoiningPartnership.error.required"
      ).verifying(
        messages("partnerDetailsAddDateOfJoiningPartnership.error.invalid.range", dateOfJoining, twoWeeks),
        date =>
          (date.isAfter(dateOfJoining) || date.isEqual(dateOfJoining)) &&
            (date.isBefore(twoWeeks) || date.isEqual(twoWeeks))
      )
    )
  }
}
