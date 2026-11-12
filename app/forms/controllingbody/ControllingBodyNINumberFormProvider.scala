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

import forms.controllingbody.ControllingBodyNINumberFormProvider.{invalidCharsKey, invalidFormatKey, ninoCharsRegex, ninoValidRegex, normaliseNino, requiredKey}
import forms.mappings.Mappings
import play.api.data.Form
import play.api.data.validation.{Constraint, Invalid, Valid}

import javax.inject.Inject

class ControllingBodyNINumberFormProvider @Inject() extends Mappings {

  def apply(): Form[String] = {
    Form(
      "value" ->
        text(requiredKey)
          .transform[String](normaliseNino, identity)
          .verifying(
            Seq(
              Constraint[String](invalidCharsKey) { nino =>
                if (nino.trim.matches(ninoCharsRegex)) Valid else Invalid(invalidCharsKey)
              },
              Constraint[String](invalidFormatKey) { nino =>
                if (nino.matches(ninoValidRegex)) Valid else Invalid(invalidFormatKey)
              }
            )*
          )
    )
  }
}

object ControllingBodyNINumberFormProvider {

  private[forms] val requiredKey = "controllingBodyAddNino.error.required"

  private[forms] val invalidCharsKey = "controllingBodyAddNino.error.invalidChars"
  private[forms] val ninoCharsRegex = """^[A-Za-z0-9]+$"""

  private[forms] val invalidFormatKey = "controllingBodyAddNino.error.invalidFormat"
  private[forms] val ninoValidRegex = """^[ABCE-Zabce-z][ABCE-Zabce-z][0-9][0-9][0-9][0-9][0-9][0-9][A BCD]$"""

  private[forms] val ninoLengthWithoutSuffix = 8

  private[forms] def normaliseNino(input: String): String = {
    val stripped = input.filterNot(_.isWhitespace)
    if (stripped.length == ninoLengthWithoutSuffix) s"$stripped " else stripped
  }
}
