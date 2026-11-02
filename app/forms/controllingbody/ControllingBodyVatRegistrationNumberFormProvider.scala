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

import forms.controllingbody.ControllingBodyVatRegistrationNumberFormProvider.*
import forms.mappings.{ChecksumConstraints, Mappings}
import play.api.data.Form

import javax.inject.Inject

class ControllingBodyVatRegistrationNumberFormProvider @Inject() extends Mappings with ChecksumConstraints {

  def apply(): Form[String] =
    Form(
      "value" ->
        text(requiredKey)
          .transform[String](_.filterNot(_.isWhitespace), identity)
          .verifying(
            firstError(
              regexp(zeroToNineRegex, invalidCharactersKey),
              fixedLength(vrnLength, invalidLengthKey),
              vatChecksum(invalidKey)
            )
          )
    )
}

object ControllingBodyVatRegistrationNumberFormProvider {

  private[forms] val vrnLength = 9
  private[forms] val zeroToNineRegex = "^[0-9]+$"

  private[forms] val requiredKey = "controllingBodyVatRegNumber.error.required"
  private[forms] val invalidCharactersKey = "controllingBodyVatRegNumber.error.invalid.characters"
  private[forms] val invalidLengthKey = "controllingBodyVatRegNumber.error.length"
  private[forms] val invalidKey = "controllingBodyVatRegNumber.error.invalid"
}
