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

package forms

import javax.inject.Inject

import forms.mappings.Mappings
import play.api.data.Form
import play.api.data.Forms.*
import models.PartnerContactDetails

class PartnerContactDetailsFormProvider @Inject() extends Mappings {

  private val phoneRegex = """^[0-9]{1,20}$""".r

  def apply(): Form[PartnerContactDetails] = Form(
    mapping(
      "phoneNumber" -> optional(
        text("partnerContactDetails.error.phoneNumber.required")
          .verifying(maxLength(20, "partnerContactDetails.error.phoneNumber.length"))
          .verifying("partnerContactDetails.error.phoneNumber.invalid", value => value.trim.isEmpty || phoneRegex.matches(value.trim))
      ),
      "mobilePhoneNumber" -> optional(
        text("partnerContactDetails.error.mobilePhoneNumber.required")
          .verifying(maxLength(20, "partnerContactDetails.error.mobilePhoneNumber.length"))
          .verifying("partnerContactDetails.error.mobilePhoneNumber.invalid", value => value.trim.isEmpty || phoneRegex.matches(value.trim))
      )
    )((a, b) => PartnerContactDetails.apply(a, b))(x => Some((x.phoneNumber, x.mobilePhoneNumber)))
      .verifying(
        "partnerContactDetails.error.count",
        contact => {
          contact.phoneNumber.nonEmpty || contact.mobilePhoneNumber.nonEmpty
        }
      )
  )
}
