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
import forms.controllingbody.ControllingBodyForeignCorpRefFormProvider.*
import org.scalacheck.Gen
import play.api.data.FormError

class ControllingBodyForeignCorpRefFormProviderSpec extends StringFieldBehaviours {

  private val form = new ControllingBodyForeignCorpRefFormProvider()()

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
        "Z",
        "12 34",
        "123 456"
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

    "fail to bind values greater than max length with an invalid character, errors ordered" in {
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

        result.errors must contain(
          FormError(fieldName, invalidKey, Seq(refNumberRegex))
        )
      }
    }

    "bind valid ref number ignoring leading and trailing spaces" in {
      Seq(
        "   abcd1234",
        "abcd1234   ",
        "   abcd1234   "
      ).foreach { input =>
        val bound = form.bind(Map(fieldName -> input))

        bound.value.value mustBe "abcd1234"
        bound.errors mustBe empty
      }
    }

    "replace multiple consecutive spaces between digits with a single space" in {
      Seq(
        "12  34"      -> "12 34",
        "12   34"     -> "12 34",
        "123    456"  -> "123 456",
        "  12   34  " -> "12 34"
      ).foreach { case (input, expected) =>
        val bound = form.bind(Map(fieldName -> input))

        bound.value.value mustBe expected
        bound.errors mustBe empty
      }
    }

    "preserve a single space between digits" in {
      val input = "12 34"

      val bound = form.bind(Map(fieldName -> input))

      bound.value.value mustBe input
      bound.errors mustBe empty
    }

    "preserve multiple spaces when they are not between digits" in {
      val input = "AB  123"

      val bound = form.bind(Map(fieldName -> input))

      bound.value.value mustBe input
      bound.errors mustBe empty
    }

    "validate the normalized value against the maximum length" in {
      val input = "1" + (" " * 3) + ("2" * 97)

      val bound = form.bind(Map(fieldName -> input))

      bound.value.value mustBe ("1 " + ("2" * 97))
      bound.errors mustBe empty
    }
  }
}
