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
import forms.partner.PartnerDetailsForeignCorporateReferenceFormProvider.*
import org.scalacheck.Gen
import play.api.data.FormError

class PartnerDetailsForeignCorporateReferenceFormProviderSpec extends StringFieldBehaviours {

  private val form = new PartnerDetailsForeignCorporateReferenceFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      Gen.oneOf(
        "XYZ123",
        "John O'Doe Corp",
        "Foreign-Ref 99",
        "Company's Reference",
        "-XYZ123",
        "'XYZ123",
        "Z"
      )
    )

    "bind strings that have maximum length with no errors" in {
      val validLongString = "A" * maxLength

      val result = form.bind(
        Map(fieldName -> validLongString)
      )

      result.errors mustBe empty
    }

    "fail to bind values that are greater than the maximum length" in {
      val tooLongString = "A" * (maxLength + 1)

      val result = form.bind(
        Map(fieldName -> tooLongString)
      )

      result.errors mustBe Seq(
        FormError(fieldName, maxLengthKey, Seq(maxLength))
      )
    }

    "fail to bind values greater than max length with an invalid character, errors ordered " in {
      val tooLongWithInvalidChar = "A" * (maxLength - 1) + "@B"

      val result = form.bind(
        Map(fieldName -> tooLongWithInvalidChar)
      )

      result.errors mustBe Seq(
        FormError(fieldName, invalidKey, Seq(refNumberRegex)),
        FormError(fieldName, maxLengthKey, Seq(maxLength))
      )
    }

    "fail to bind invalid characters" in {
      val invalidValues = Seq("Test@Ref", "Hello#World", "Ref!123", "Company$123")
      invalidValues.foreach { value =>
        val result = form.bind(Map(fieldName -> value))
        result.errors must contain(FormError(fieldName, invalidKey, Seq("^[A-Za-z 0-9-']+$")))
      }
    }

    "bind valid ref number ignoring leading and trailing spaces" in {
      Seq("   abcd1234", "abcd1234   ", "   abcd1234   ").foreach { input =>
        val bound = form.bind(Map(fieldName -> input))
        bound.value.value mustBe "abcd1234"
        bound.errors mustBe empty
      }
    }
  }
}
