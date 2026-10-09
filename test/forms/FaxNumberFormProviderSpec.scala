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
import org.scalacheck.Gen
import play.api.data.FormError

class FaxNumberFormProviderSpec extends StringFieldBehaviours {

  private val requiredKey = "faxNumber.error.required"
  private val lengthKey = "faxNumber.error.length"
  private val invalidCharactersKey = "faxNumber.error.invalid.characters"

  private val maxLength = 20
  private val faxNumberCharactersRegex = "^[0-9 ]+$"

  private val form = new FaxNumberFormProvider()("faxNumber")

  ".faxNumber" - {

    val fieldName = "faxNumber"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      Gen.oneOf(
        "01632960001",
        "01632 960 001",
        "07700900982",
        "07700 900 982",
        "01234567890123456789",
        "12345678901234567890"
      )
    )

    "bind fax numbers with a single space when the total character count is exactly 20" in {
      val result = form.bind(Map(fieldName -> "0123456789 012345678")).apply(fieldName) // 19 digits + 1 space = 20 chars

      result.errors mustBe empty
    }

    s"not bind fax numbers with a single space when the total character count exceeds $maxLength" in {
      val result = form.bind(Map(fieldName -> "0123456789 0123456789")).apply(fieldName) // 20 digits + 1 space = 21 chars

      result.errors must contain(FormError(fieldName, lengthKey, Seq(maxLength)))
    }

    "bind fax numbers with multiple consecutive spaces collapsed to a single space, when the resulting count is exactly 20" in {
      val result =
        form.bind(Map(fieldName -> "123456789012345678   9")).apply(fieldName) // 18 digits + (3 spaces collapsed to 1) + 1 digit = 20 aftercollapse

      result.errors mustBe empty
    }

    s"not bind strings longer than $maxLength characters with no spaces" in {
      val result = form.bind(Map(fieldName -> ("0" * (maxLength + 1)))).apply(fieldName)

      result.errors must contain(FormError(fieldName, lengthKey, Seq(maxLength)))
    }

    "not bind invalid characters" in {
      val result = form.bind(Map(fieldName -> "01632-960001")).apply(fieldName)

      result.errors must contain only FormError(fieldName, invalidCharactersKey, Seq(faxNumberCharactersRegex))
    }

    "bind length and invalid character errors together" in {
      val result = form.bind(Map(fieldName -> (("0" * (maxLength + 1)) + "-"))).apply(fieldName)

      result.errors mustBe Seq(
        FormError(fieldName, lengthKey, Seq(maxLength)),
        FormError(fieldName, invalidCharactersKey, Seq(faxNumberCharactersRegex))
      )
    }

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
