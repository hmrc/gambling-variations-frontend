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

package forms.partnerdetails

import forms.mappings.Mappings
import forms.partnerdetails.PartnerDetailsCompanyRegistrationNumberFormProvider.*
import play.api.data.Form

import javax.inject.Inject

class PartnerDetailsCompanyRegistrationNumberFormProvider @Inject() extends Mappings {

  def apply(): Form[String] =
    Form(
      "value" ->
        text(requiredKey)
          .transform[String](_.trim, identity)
          .verifying(
            regexp(allowedCharsRegex, invalidCharactersKey)
          )
          .verifying(regexp(crnRegex, invalidFormatKey))
    )
}

object PartnerDetailsCompanyRegistrationNumberFormProvider {

  private[forms] val allowedCharsRegex = """^[A-Za-z0-9]+$"""
  private[forms] val crnRegex =
    "^(\\d{1,8}|([AaFfIiOoRrSsZz][Cc]|[Cc][Uu]|[Ss][AaEeFfIiRrZz]|[Ee][Ss])\\d{1,6}|([IiSs][Pp]|[Nn][AaFfIiOoPpRrVvZz]|[Rr][Oo])[\\da-zA-Z]{1,6})$"

  private[forms] val requiredKey = "partnerDetailsCompanyRegistrationNumber.error.required"
  private[forms] val invalidCharactersKey = "partnerDetailsCompanyRegistrationNumber.error.invalid.characters"
  private[forms] val invalidFormatKey = "partnerDetailsCompanyRegistrationNumber.error.invalid.format"
}
