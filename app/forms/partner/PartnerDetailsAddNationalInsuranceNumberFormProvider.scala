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

import javax.inject.Inject

class PartnerDetailsAddNationalInsuranceNumberFormProvider @Inject() extends Mappings {

  private val ninoFormatRegex: String = """^[A-Za-z]{2}\s?[0-9]{2}\s?[0-9]{2}\s?[0-9]{2}\s?[A-Za-z\s]$"""

  private val ninoCharsRegex: String = """^[E-ZE-Ze-z][A-Za-z]\s?[0-9]{2}\s?[0-9]{2}\s?[0-9]{2}\s?[B-Db-d\s]$"""

  private val ninoValidRegex: String = """^(?!BG|GB|KN|NK|NT|TN|ZZ)[A-CEGHJ-PR-TW-Z][A-CEGHJ-NPR-TV-Z]\s?[0-9]{2}\s?[0-9]{2}\s?[0-9]{2}\s?[A-D]$"""

  private val length: Int = 9

  def apply(): Form[String] = {
    Form(
      "value" ->
        text("partnerDetailsAddNationalInsuranceNumber.error.required")
          .verifying(
            maxLength(length, "partnerDetailsAddNationalInsuranceNumber.error.length")
          )
          .verifying(
            regexp(ninoFormatRegex, "partnerDetailsAddNationalInsuranceNumber.error.invalidFormat")
          )
          .verifying(
            regexp(ninoCharsRegex, "partnerDetailsAddNationalInsuranceNumber.error.invalidChars")
          )
          .verifying(
            regexp(ninoValidRegex, "partnerDetailsAddNationalInsuranceNumber.error.invalid")
          )
    )
  }
}
