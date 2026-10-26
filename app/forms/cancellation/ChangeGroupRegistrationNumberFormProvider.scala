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

package forms.cancellation

import forms.mappings.{ChecksumConstraints, Mappings}
import play.api.data.Form
import utils.ChecksumValidator

import javax.inject.Inject

//TODO I think I need to add tests, it just slightly different from the original
class ChangeGroupRegistrationNumberFormProvider @Inject() extends Mappings with ChecksumConstraints {

  def apply(): Form[String] =
    Form(
      "changeGroupRegistrationNumber" -> text("changeGroupRegistrationNumber.error.required")
        .transform[String](_.filterNot(_.isWhitespace).toUpperCase, identity)
        .verifying(
          firstError(
            regexp(ChecksumValidator.mgdrnCharactersRegex, "changeGroupRegistrationNumber.error.invalidCharacters"),
            regexp(ChecksumValidator.mgdrnFormatRegex, "changeGroupRegistrationNumber.error.invalidFormat"),
            modulo23Checksum(
              ChecksumValidator.mgdrnFormatRegex,
              ChecksumValidator.mgdrnChecksumWeights,
              ChecksumValidator.mgdrnCheckCharacterIndex,
              ChecksumValidator.mgdrnChecksumLookup,
              "changeGroupRegistrationNumber.error.invalidReference"
            )
          )
        )
    )
}
