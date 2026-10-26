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

import forms.AssociatedRegNumberFormProvider
import forms.behaviours.StringFieldBehaviours
import org.scalatestplus.mockito.MockitoSugar
import play.api.data.FormError
import utils.ChecksumValidator

class ChangeGroupRegistrationNumberFormProviderSpec extends StringFieldBehaviours with MockitoSugar {

  val requiredKey = "changeGroupRegistrationNumber.error.required"
  val invalidCharactersKey = "changeGroupRegistrationNumber.error.invalidCharacters"
  val invalidFormatKey = "changeGroupRegistrationNumber.error.invalidFormat"
  val invalidReferenceKey = "changeGroupRegistrationNumber.error.invalidReference"

  val form = new ChangeGroupRegistrationNumberFormProvider()()

  ".changeGroupRegistrationNumber" - {

    val fieldName = "changeGroupRegistrationNumber"

    "bind a valid associated registration number" in {
      val result = form.bind(Map(fieldName -> "XRM00000000574"))

      result.value.value mustEqual "XRM00000000574"
    }

    "remove whitespaces and uppercase a valid associated registration number" in {
      val result = form.bind(Map(fieldName -> " xrm 0000 0000574 "))

      result.value.value mustEqual "XRM00000000574"
    }

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    "not bind an associated registration number with invalid characters" in {
      val result = form.bind(Map(fieldName -> "@@@00001234567")).apply(fieldName)

      result.errors must contain only FormError(fieldName, invalidCharactersKey, Seq("^[A-Z0-9]+$"))
    }

    "not bind an associated registration number that does not start with X" in {
      val result = form.bind(Map(fieldName -> "MAX6666444555")).apply(fieldName)

      result.errors must contain only FormError(fieldName, invalidFormatKey, Seq(ChecksumValidator.mgdrnFormatRegex))
    }

    "not bind an associated registration number that does not have M as the third character" in {
      val result = form.bind(Map(fieldName -> "XAX00001234567")).apply(fieldName)

      result.errors must contain only FormError(fieldName, invalidFormatKey, Seq(ChecksumValidator.mgdrnFormatRegex))
    }

    "not bind an associated registration number with an excluded check character" in {
      val result = form.bind(Map(fieldName -> "XIM00000000574")).apply(fieldName)

      result.errors must contain only FormError(fieldName, invalidFormatKey, Seq(ChecksumValidator.mgdrnFormatRegex))
    }

    "not bind an associated registration number with an invalid checksum" in {
      val result = form.bind(Map(fieldName -> "XAM00001234567")).apply(fieldName)

      result.errors must contain only FormError(fieldName, invalidReferenceKey)
    }
  }

//  val requiredKey = "changeGroupRegistrationNumber.error.required"
//  val lengthKey = "changeGroupRegistrationNumber.error.length"
//  val maxLength = 14
//
//  val form = new ChangeGroupRegistrationNumberFormProvider()()
//
//  ".value" - {
//
//    val fieldName = "value"
//
//    behave like fieldThatBindsValidData(
//      form,
//      fieldName,
//      stringsWithMaxLength(maxLength)
//    )
//
//    behave like fieldWithMaxLength(
//      form,
//      fieldName,
//      maxLength   = maxLength,
//      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
//    )
//
//    behave like mandatoryField(
//      form,
//      fieldName,
//      requiredError = FormError(fieldName, requiredKey)
//    )
//  }
}
