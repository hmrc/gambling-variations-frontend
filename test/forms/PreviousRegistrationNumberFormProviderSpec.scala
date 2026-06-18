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

package forms

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class PreviousRegistrationNumberFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "previousRegNumber.error.required"
  val invalidCharactersKey = "previousRegNumber.error.invalid.characters"
  val invalidFormatKey = "previousRegNumber.error.invalid.format"

  val form = new PreviousRegistrationNumberFormProvider()()

  ".previousRegNumber" - {

    val fieldName = "previousRegNumber"

    "bind a valid previous registration number" in {
      val result = form.bind(Map(fieldName -> "XGM00001234567"))

      result.value.value mustEqual "XGM00001234567"
    }

    "remove whitespaces and uppercase a valid previous registration number" in {
      val result = form.bind(Map(fieldName -> " xgm 0000 1234567 "))

      result.value.value mustEqual "XGM00001234567"
    }

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    "not bind an previous registration number with invalid characters" in {
      val result = form.bind(Map(fieldName -> "@@@00001234567")).apply(fieldName)

      result.errors must contain only FormError(fieldName, invalidCharactersKey, Seq("^[A-Z0-9]+$"))
    }

    "not bind an previous registration number that does not start with X" in {
      val result = form.bind(Map(fieldName -> "MAX6666444555")).apply(fieldName)

      result.errors must contain only FormError(fieldName, invalidFormatKey, Seq("^X[A-Z]M000[0-9]{8}$"))
    }

    "not bind an previous registration number that does not have M as the third character" in {
      val result = form.bind(Map(fieldName -> "XAX00001234567")).apply(fieldName)

      result.errors must contain only FormError(fieldName, invalidFormatKey, Seq("^X[A-Z]M000[0-9]{8}$"))
    }
  }
}