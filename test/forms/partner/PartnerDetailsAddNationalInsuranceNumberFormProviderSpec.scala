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

    val validNinoWithSuffix = "AA123456A"
    val validNinoWithoutSuffix = "AA123456"

    "must bind a valid NINO with suffix" in {
      val result = form.bind(Map(fieldName -> validNinoWithSuffix))
      result.errors mustBe empty
      result.value.value mustBe validNinoWithSuffix
    }

    "must bind a valid NINO without suffix" in {
      val result = form.bind(Map(fieldName -> validNinoWithoutSuffix))
      result.errors mustBe empty
      result.value.value mustBe validNinoWithoutSuffix
    }

    "must strip spaces and convert lowercase letters to uppercase upon binding" in {
      val inputWithSpacesAndLowercase = " aa 12 34 56 a "
      val result = form.bind(Map(fieldName -> inputWithSpacesAndLowercase))
      result.errors mustBe empty
      result.value.value mustBe validNinoWithSuffix
    }

    "must fail to bind invalid characters (e.g. symbols like '@')" in {
      val invalidCharsInput = "Q@123456C"
      val result = form.bind(Map(fieldName -> invalidCharsInput))
      result.errors must contain(FormError(fieldName, invalidCharsKey, Seq(ninoCharsRegex)))
    }

    "must fail to bind invalid format starting with digits instead of letters" in {
      val invalidFormatInput = "12QQ3456C"
      val result = form.bind(Map(fieldName -> invalidFormatInput))
      result.errors must contain(FormError(fieldName, invalidFormatKey, Seq(ninoValidRegex)))
    }

    "must fail to bind when the first letter is 'D'" in {
      val ninoStartingWithD = "DA123456A"
      val result = form.bind(Map(fieldName -> ninoStartingWithD))
      result.errors must contain(FormError(fieldName, invalidFormatKey, Seq(ninoValidRegex)))
    }

    "must fail to bind when suffix is not A, B, C, or D" in {
      val ninoWithInvalidSuffix = "AA123456Z"
      val result = form.bind(Map(fieldName -> ninoWithInvalidSuffix))
      result.errors must contain(FormError(fieldName, invalidFormatKey, Seq(ninoValidRegex)))
    }

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
