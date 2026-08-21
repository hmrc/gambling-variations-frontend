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

import javax.inject.Inject
import play.api.data.Form
import forms.mappings.Mappings
import forms.partner.PartnerDetailsAddNationalInsuranceNumberFormProvider.*

class PartnerDetailsAddNationalInsuranceNumberFormProvider @Inject() extends Mappings {

  private val length: Int = 9

  def apply(): Form[String] = {
    Form(
      "value" ->
        text("partnerDetailsAddNationalInsuranceNumber.error.required")
          .verifying(
            firstError(
              maxLength(length, "partnerDetailsAddNationalInsuranceNumber.error.length"),
              regexp(ninoFormatRegex, "partnerDetailsAddNationalInsuranceNumber.error.invalidFormat"),
              regexp(ninoCharsRegex, "partnerDetailsAddNationalInsuranceNumber.error.invalidChars"),
              regexp(ninoValidRegex, "partnerDetailsAddNationalInsuranceNumber.error.invalid")
            )
          )
    )
  }
}

object PartnerDetailsAddNationalInsuranceNumberFormProvider {
  private[forms] val ninoFormatRegex: String = """^[A-Za-z]{2}\s?[0-9]{2}\s?[0-9]{2}\s?[0-9]{2}\s?[A-Za-z]$"""
  private[forms] val ninoCharsRegex: String =
    """^[A-CEGHJ-PR-TW-Za-ceghj-pr-tw-z][A-CEGHJ-NPR-TV-Za-ceghj-npr-tv-z]\s?[0-9]{2}\s?[0-9]{2}\s?[0-9]{2}\s?[A-Da-d]$"""
  private[forms] val ninoValidRegex: String =
    """^(?!BG|GB|KN|NK|NT|TN|ZZ)[A-CEGHJ-PR-TW-Z][A-CEGHJ-NPR-TV-Z]\s?[0-9]{2}\s?[0-9]{2}\s?[0-9]{2}\s?[A-D]$"""
}
