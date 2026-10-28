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

import forms.behaviours.StringFieldBehaviours
import org.scalacheck.Gen
import play.api.data.FormError

class ControllingBodyCountryOfIncorporationFormProviderSpec extends StringFieldBehaviours {

  private val requiredKey = "controllingBodyCountryOfIncorporation.error.required"
  private val lengthKey = "controllingBodyCountryOfIncorporation.error.length"
  private val invalidCharactersKey = "controllingBodyCountryOfIncorporation.error.invalid"

  private val maxLength = 100
  private val controllingBodyCountryOfIncorporationCharactersRegex = "^[a-zA-Z0-9\\-'\\s]+$"

  private val form = new ControllingBodyCountryOfIncorporationFormProvider()()

  ".controllingBodyCountryOfIncorporation" - {

    val fieldName = "controllingBodyCountryOfIncorporation"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      Gen.oneOf(
        "country one",
        "country-dos",
        "country minus four",
        "country sixty",
        "country a million",
        "country thirty five"
      )
    )

    s"max length" in {
      val result = form.bind(Map(fieldName -> ("0" * (maxLength + 1)))).apply(fieldName)

      result.errors must contain(FormError(fieldName, lengthKey, Seq(maxLength)))
    }

    "invalid characters" in {
      val result = form.bind(Map(fieldName -> "&*! & *( ")).apply(fieldName)

      result.errors must contain only FormError(fieldName, invalidCharactersKey, Seq(controllingBodyCountryOfIncorporationCharactersRegex))
    }


    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
