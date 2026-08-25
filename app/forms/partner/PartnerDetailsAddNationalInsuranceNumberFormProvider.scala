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
import forms.partner.PartnerDetailsAddNationalInsuranceNumberFormProvider.*
import play.api.data.Form
import play.api.data.validation.Constraint

import javax.inject.Inject

class PartnerDetailsAddNationalInsuranceNumberFormProvider @Inject() extends Mappings {

  def apply(): Form[String] = {
    Form(
      "value" ->
        text(requiredKey)
          .transform[String](_.filterNot(_.isWhitespace).toUpperCase, identity)
          .verifying(
            Seq(
              regexp(ninoCharsRegex, invalidCharsKey),
              regexp(ninoFormatRegex, invalidFormatKey),
              regexp(ninoLengthRegex, lengthKey),
              regexp(ninoValidRegex, invalidKey)
            )*
          )
    )
  }
}

object PartnerDetailsAddNationalInsuranceNumberFormProvider {

  private[forms] val requiredKey = "partnerDetailsAddNino.error.required"
  private[forms] val invalidCharsKey = "partnerDetailsAddNino.error.invalidChars"
  private[forms] val invalidFormatKey = "partnerDetailsAddNino.error.invalidFormat"
  private[forms] val lengthKey = "partnerDetailsAddNino.error.length"
  private[forms] val invalidKey = "partnerDetailsAddNino.error.invalid"

  private[forms] val ninoCharsRegex = """^[A-Z0-9]+$"""

  private[forms] val ninoFormatRegex = """^[A-Z]{2}[0-9]+[A-Z]$"""

  private[forms] val ninoLengthRegex = """^[A-Z0-9]{9}$"""

  private[forms] val ninoValidRegex =
    """^(?!BG|GB|KN|NK|NT|TN|ZZ)[A-CEGHJ-PR-TW-Z][A-CEGHJ-NPR-TV-Z][0-9]{6}[A-D]$"""
}
