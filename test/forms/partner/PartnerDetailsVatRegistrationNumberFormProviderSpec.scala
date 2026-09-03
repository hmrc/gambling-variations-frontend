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
import forms.partner.PartnerDetailsVatRegistrationNumberFormProvider.{lengthRegex, oneToNineRegex}
import play.api.data.FormError

class PartnerDetailsVatRegistrationNumberFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "partnerDetailsVatRegistrationNumber.error.required"
  val lengthKey = "partnerDetailsVatRegistrationNumber.error.length"
  val realKey = "partnerDetailsVatRegistrationNumber.error.invalid"
  val digitsOnly = "partnerDetailsVatRegistrationNumber.error.invalid.characters"

  val form = new PartnerDetailsVatRegistrationNumberFormProvider()()

  ".value" - {

    val fieldName = "partnerDetailsVatRegistrationNumber"

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    "bind a bare valid 9-digit VAT registration number" in {
      val result = form.bind(Map(fieldName -> "353868127")).apply(fieldName)
      result.value.value mustBe "353868127"
      result.errors mustBe empty
    }

    "bind a GB-prefixed VAT valid registration number" in {
      val validInputs = Seq("GB353868127", "gb353868127", "Gb353868127", "gB353868127")
      for (input <- validInputs) {
        val result = form.bind(Map(fieldName -> input)).apply(fieldName)
        result.errors mustBe empty
      }
    }

    "bind a checksum-valid VAT valid registration number ignoring leading and trailing spaces" in {
      val validInputs = Seq("   GB353868127", "GB353868127   ", "   GB353868127   ")
      for (input <- validInputs) {
        val result = form.bind(Map(fieldName -> input)).apply(fieldName)
        result.errors mustBe empty
      }
    }

    "fail to bind a non-digit char in a 9 chars number" in {
      val result = form.bind(Map(fieldName -> "3538X8127")).apply(fieldName)
      result.errors must contain(FormError(fieldName, digitsOnly, Seq(oneToNineRegex)))
    }

    "fail to bind a 9-digits containing a 0, reporting the characters and real-VAT number errors but not the length error" in {
      val invalidInputs = Seq("123406789", "100000000")
      for (input <- invalidInputs) {
        val result = form.bind(Map(fieldName -> input)).apply(fieldName)
        result.errors                must contain(FormError(fieldName, digitsOnly, Seq(oneToNineRegex)))
        result.errors                must contain(FormError(fieldName, realKey))
        result.errors.map(_.message) must not contain lengthKey
      }
    }

    "fail to bind fewer than 9 digits with no GB prefix" in {
      val result = form.bind(Map(fieldName -> "12345678")).apply(fieldName)
      result.errors must contain(FormError(fieldName, lengthKey, Seq(lengthRegex)))
    }

    "fail to bind more than 9 digits with no GB prefix, reporting length and real-VAT number error" in {
      val result = form.bind(Map(fieldName -> "1234567891")).apply(fieldName)
      result.errors                must contain(FormError(fieldName, lengthKey, Seq(lengthRegex)))
      result.errors                must contain(FormError(fieldName, realKey))
      result.errors.map(_.message) must not contain digitsOnly
    }

    "fail to bind a non GB prefix with valid 9-digit VAT registration number" in {
      val invalidInputs = Seq("XY353868127", "xY353868127", "Xy353868127", "xy353868127")
      for (input <- invalidInputs) {
        val result = form.bind(Map(fieldName -> input)).apply(fieldName)
        result.errors must contain(FormError(fieldName, realKey))
        result.errors must contain(FormError(fieldName, digitsOnly, Seq(oneToNineRegex)))
      }
    }

    "fail to bind 9 digits non-valid VAT number with no GB prefix" in {
      // increment valid VAT number to make invalid: 353868127 + 1
      val result = form.bind(Map(fieldName -> "353868128")).apply(fieldName)
      result.errors mustEqual Seq(FormError(fieldName, realKey))
    }

  }
}
