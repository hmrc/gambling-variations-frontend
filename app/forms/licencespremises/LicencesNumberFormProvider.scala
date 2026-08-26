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

package forms.licencespremises

import forms.mappings.Mappings
import play.api.data.Form

import javax.inject.Inject

class LicencesNumberFormProvider @Inject() extends Mappings {

  private val allowedCharactersRegex = "^[A-Z0-9-]+$"

  private val licenceNumberRegex =
    "^([0-9]{3})-([0-9]{6})-([A-Z])-([0-9]{6})-([0-9]{3})$"

  def apply(): Form[String] =
    Form(
      "gamblingLicenceNo" -> text("licencesNumber.error.required")
        .transform[String](_.replace(" ", ""), identity)
        .verifying(
          "licencesNumber.error.required",
          _.nonEmpty
        )
        .verifying(
          "licencesNumber.error.invalidCharacters",
          _.matches(allowedCharactersRegex)
        )
        .verifying(
          "licencesNumber.error.invalidFormat",
          _.matches(licenceNumberRegex)
        )
    )
}
