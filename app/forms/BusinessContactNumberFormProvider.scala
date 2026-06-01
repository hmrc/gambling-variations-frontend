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
import models.BusinessContactNumber
import play.api.data.Form
import play.api.data.Forms.*
import play.api.data.validation.*

class BusinessContactNumberFormProvider @Inject() extends Mappings {

  private val MaxLength = 20
  private val AllowedCharsRegex = "^[0-9 ]+$"
  private val DigitsOnlyRegex = "^[0-9]{10,11}$"

  private def isValidFormat(number: String): Boolean =
    number.replaceAll(" ", "").matches(DigitsOnlyRegex)

  private val phoneConstraint: Constraint[String] =
    Constraint { value =>
      val trimmed = value.trim

      if (trimmed.isEmpty) Valid
      else if (trimmed.length > MaxLength) Invalid("businessContactNumber.error.phoneNumber.length")
      else if (!trimmed.matches(AllowedCharsRegex)) Invalid("businessContactNumber.error.phoneNumber.invalid")
      else if (!isValidFormat(trimmed)) Invalid("businessContactNumber.error.phoneNumber.invalidFormat")
      else Valid
    }

  private val mobileConstraint: Constraint[String] =
    Constraint { value =>
      val trimmed = value.trim

      if (trimmed.isEmpty) Valid
      else if (trimmed.length > MaxLength) Invalid("businessContactNumber.error.mobileNumber.length")
      else if (!trimmed.matches(AllowedCharsRegex)) Invalid("businessContactNumber.error.mobileNumber.invalid")
      else if (!isValidFormat(trimmed)) Invalid("businessContactNumber.error.mobileNumber.invalidFormat")
      else Valid
    }

  def apply(): Form[BusinessContactNumber] =
    Form(
      mapping(
        "phoneNumber" ->
          optional(
            text()
              .transform(_.trim, identity)
              .verifying(phoneConstraint)
          ),
        "mobileNumber" ->
          optional(
            text()
              .transform(_.trim, identity)
              .verifying(mobileConstraint)
          )
      )((phone: Option[String], mobile: Option[String]) => BusinessContactNumber(phone, mobile))((b: BusinessContactNumber) =>
        Some((b.phoneNumber, b.mobileNumber))
      )
//        .verifying(
//          Constraint[BusinessContactNumber] { data =>
//            val phone = data.phoneNumber.getOrElse("").trim
//            val mobile = data.mobileNumber.getOrElse("").trim
//
//            if (phone.isEmpty && mobile.isEmpty)
//              Invalid("businessContactNumber.error.contactRequired")
//            else Valid
//          }
//        )
    )
}
