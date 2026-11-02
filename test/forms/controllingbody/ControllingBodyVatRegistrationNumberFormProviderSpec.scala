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
import forms.controllingbody.ControllingBodyVatRegistrationNumberFormProvider.{vrnLength, zeroToNineRegex}
import play.api.data.FormError

class ControllingBodyVatRegistrationNumberFormProviderSpec extends StringFieldBehaviours {

  private val requiredKey = "controllingBodyVatRegNumber.error.required"
  private val invalidCharactersKey = "controllingBodyVatRegNumber.error.invalid.characters"
  private val lengthKey = "controllingBodyVatRegNumber.error.length"
  private val invalidKey = "controllingBodyVatRegNumber.error.invalid"

  private val form = new ControllingBodyVatRegistrationNumberFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    "must bind a valid VAT registration number" in {
      Seq("353868127", "239088635").foreach { input =>
        val result = form.bind(Map(fieldName -> input)).apply(fieldName)
        result.value.value mustBe input
        result.errors mustBe empty
      }
    }

    "must remove spaces and other whitespace before validation" in {
      Seq(" 353868127 ", "353 868 127", "3 5 3 8 6 8 1 2 7", "353\t868\t127", "353868127\n").foreach { input =>
        val bound = form.bind(Map(fieldName -> input))
        bound.value.value mustBe "353868127"
        bound.errors mustBe empty
      }
    }

    "must report only the required error for a value of whitespace only" in {
      form.bind(Map(fieldName -> "   ")).apply(fieldName).errors mustEqual Seq(FormError(fieldName, requiredKey))
    }

    "must report only the invalid characters error when the value has characters other than 0 to 9" in {
      Seq("GB353868127", "3538X8127", "353-868-127").foreach { input =>
        form.bind(Map(fieldName -> input)).apply(fieldName).errors mustEqual
          Seq(FormError(fieldName, invalidCharactersKey, Seq(zeroToNineRegex)))
      }
    }

    "must report only the length error when the value is too short or too long" in {
      Seq("12345678", "1234567891", "35 38 68 12").foreach { input =>
        form.bind(Map(fieldName -> input)).apply(fieldName).errors mustEqual
          Seq(FormError(fieldName, lengthKey, Seq(vrnLength)))
      }
    }

    "must report the real VAT registration number error only when the other validation has passed" in {
      Seq("353868128", "123406789", "100000000").foreach { input =>
        form.bind(Map(fieldName -> input)).apply(fieldName).errors mustEqual Seq(FormError(fieldName, invalidKey))
      }
    }

    "must unbind a VAT registration number unchanged" in {
      form.fill("353868127").data mustBe Map(fieldName -> "353868127")
    }
  }
}
