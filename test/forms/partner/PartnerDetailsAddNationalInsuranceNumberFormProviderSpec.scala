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

package forms.partner

import forms.behaviours.StringFieldBehaviours
import forms.partner.PartnerDetailsAddNationalInsuranceNumberFormProvider.*
import play.api.data.{Form, FormError}

class PartnerDetailsAddNationalInsuranceNumberFormProviderSpec extends StringFieldBehaviours {

  val form: Form[String] = (new PartnerDetailsAddNationalInsuranceNumberFormProvider())()

  ".value" - {

    val fieldName = "value"

    val validNino9Chars = "AA123456A"
    val validNino8Chars = "AA123456"

    "must bind a valid 9-character NINO with suffix" in {
      val result = form.bind(Map(fieldName -> validNino9Chars))
      result.errors mustBe empty
      result.value.value mustBe validNino9Chars
    }

    "must bind a valid 8-character NINO (missing suffix) by appending space for regex check" in {
      val result = form.bind(Map(fieldName -> validNino8Chars))
      result.errors mustBe empty
      result.value.value mustBe validNino8Chars
    }

    "must strip whitespace and convert to uppercase before running validations" in {
      val inputWithWhitespace = " a a 1 2 3 4 5 6 "
      val result = form.bind(Map(fieldName -> inputWithWhitespace))
      result.errors mustBe empty
      result.value.value mustBe validNino8Chars
    }

    "must fail to bind invalid characters (e.g. '@')" in {
      val invalidCharsInput = "AA12345@A"
      val result = form.bind(Map(fieldName -> invalidCharsInput))
      result.errors must contain(FormError(fieldName, invalidCharsKey, Seq(ninoCharsRegex)))
    }

    "must fail to bind when starting with digits" in {
      val invalidFormatInput = "12AA3456A"
      val result = form.bind(Map(fieldName -> invalidFormatInput))
      result.errors must contain(FormError(fieldName, invalidFormatKey))
    }

    "must fail to bind when first character is 'D' (or 'd')" in {
      val ninoStartingWithD = "DA123456A"
      val result = form.bind(Map(fieldName -> ninoStartingWithD))
      result.errors must contain(FormError(fieldName, invalidFormatKey))
    }

    "must fail to bind when second character is 'D' (or 'd')" in {
      val ninoSecondCharIsD = "ad123456A"
      val result = form.bind(Map(fieldName -> ninoSecondCharIsD))
      result.errors must contain(FormError(fieldName, invalidFormatKey))
    }

    "must fail to bind when 9-character NINO has invalid suffix (not A, B, C, D, or space)" in {
      val ninoWithInvalidSuffix = "AA123456Z"
      val result = form.bind(Map(fieldName -> ninoWithInvalidSuffix))
      result.errors must contain(FormError(fieldName, invalidFormatKey))
    }

    "must fail to bind when string length is insufficient even with padding (e.g. 7 chars)" in {
      val shortNino = "AA12345"
      val result = form.bind(Map(fieldName -> shortNino))
      result.errors must contain(FormError(fieldName, invalidFormatKey))
    }

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
