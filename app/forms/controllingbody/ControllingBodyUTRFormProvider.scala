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

import forms.mappings.{ChecksumConstraints, Mappings}
import forms.controllingbody.ControllingBodyUTRFormProvider.*
import play.api.data.Form
import play.api.data.validation.{Constraint, Invalid, ValidationError}
import utils.ChecksumValidator

import javax.inject.Inject

class ControllingBodyUTRFormProvider @Inject() extends Mappings with ChecksumConstraints {

  private val utrConstraint: Constraint[String] =
    Constraint("constraints.utr") { value =>
      if (!digitsOnlyRegex.r.matches(value)) {
        Invalid(ValidationError(invalidCharsKey, digitsOnlyRegex))
      } else if (value.length != 10) {
        Invalid(ValidationError(invalidLengthKey, lengthRegex))
      } else {
        utrChecksum(invalidKey).apply(value)
      }
    }
    
  def apply(): Form[String] =
    Form(
      "value" -> text(requiredKey)
        .transform[String](_.trim, identity)
        .verifying(utrConstraint)
    )
}

object ControllingBodyUTRFormProvider {

  // message keys
  private[forms] val requiredKey = "controllingBodyUTR.error.required"
  private[forms] val invalidCharsKey = "controllingBodyUTR.error.invalidChars"
  private[forms] val invalidLengthKey = "controllingBodyUTR.error.length"
  private[forms] val invalidKey = "controllingBodyUTR.error.invalid"

  // Regex
  private[forms] val lengthRegex = ChecksumValidator.utrFormatRegex
  private[forms] val digitsOnlyRegex = """^\d+$"""
}
