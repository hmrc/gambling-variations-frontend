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

import forms.mappings.{ChecksumConstraints, Mappings}
import forms.partner.PartnerDetailsAddUTRFormProvider.*
import play.api.data.Form
import utils.ChecksumValidator

import javax.inject.Inject

class PartnerDetailsAddUTRFormProvider @Inject() extends Mappings with ChecksumConstraints {

  def apply(): Form[String] =
    Form(
      "value" -> text(requiredKey)
        .transform[String](_.trim, identity)
        .verifying(
          regexp(digitsOnlyRegex, invalidCharsKey),
          regexp(lengthRegex, incorrectKey),
          utrChecksum(invalidKey)
        )
    )
}

object PartnerDetailsAddUTRFormProvider {

  // message keys
  private[forms] val requiredKey = "partnerDetailsAddUTR.error.required"
  private[forms] val invalidCharsKey = "partnerDetailsAddUTR.error.invalidChars"
  private[forms] val incorrectKey = "partnerDetailsAddUTR.error.incorrect"
  private[forms] val invalidKey = "partnerDetailsAddUTR.error.invalid"

  // Regex
  private[forms] val lengthRegex = ChecksumValidator.utrFormatRegex
  private[forms] val digitsOnlyRegex = """^\d+$"""
}
