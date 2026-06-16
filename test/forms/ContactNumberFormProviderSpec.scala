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

class ContactNumberFormProviderSpec extends StringFieldBehaviours {

  val form = new ContactNumberFormProvider()("businessContactNumber")

  ".phoneNumber" - {

    val fieldName = "phoneNumber"

    val lengthKey = "businessContactNumber.error.phoneNumber.length"
    val invalidKey = "businessContactNumber.error.phoneNumber.invalid"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      Gen.oneOf(
        "1",
        "123",
        "01632 960 001",
        "07700 900000",
        "01234 567890",
        "01632960001",
        "12345678901234567890"
      )
    )

    "allow empty phone number (handled by cross-field rule)" in {
      val result = form.bind(
        Map(
          "phoneNumber"  -> "",
          "mobileNumber" -> "07700 900000"
        )
      )
      result.errors mustBe empty
    }

    "bind phone numbers with a single digit" in {
      val result = form.bind(
        Map(
          fieldName      -> "1",
          "mobileNumber" -> "07700 900000"
        )
      )

      result.errors mustBe empty
    }

    "bind phone numbers with fewer than 20 digits" in {
      val result = form.bind(
        Map(
          fieldName      -> "123",
          "mobileNumber" -> "07700 900000"
        )
      )

      result.errors mustBe empty
    }

    "bind phone numbers with exactly 20 digits" in {
      val result = form.bind(
        Map(
          fieldName      -> "12345678901234567890",
          "mobileNumber" -> "07700 900000"
        )
      )

      result.errors mustBe empty
    }

    "not bind phone numbers with more than 20 digits" - {
      "no trim" in {
        val result = form.bind(
          Map(
            fieldName      -> "123456789012345678901",
            "mobileNumber" -> "07700 900000"
          )
        )

        result.errors.map(_.message) must contain(lengthKey)
      }
      "trim" in {
        val result = form.bind(
          Map(
            fieldName      -> "12345 67890 12345 67890",
            "mobileNumber" -> "07700 900000"
          )
        )

        result.errors.map(_.message) must contain(lengthKey)
      }
    }

    "not bind invalid characters" in {
      val result = form.bind(
        Map(
          fieldName      -> "abc123",
          "mobileNumber" -> "07700 900000"
        )
      )

      result.errors.map(_.message) must contain(invalidKey)
    }

    "not bind special characters" in {
      val result = form.bind(
        Map(
          fieldName      -> "01632-960-001",
          "mobileNumber" -> "07700 900000"
        )
      )

      result.errors.map(_.message) must contain(invalidKey)
    }
  }

  ".mobileNumber" - {

    val fieldName = "mobileNumber"

    val lengthKey = "businessContactNumber.error.mobileNumber.length"
    val invalidKey = "businessContactNumber.error.mobileNumber.invalid"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      Gen.oneOf(
        "1",
        "123",
        "07700 900000",
        "07123456789",
        "01234 567890",
        "12345678901234567890"
      )
    )

    "allow empty mobile number (handled by cross-field rule)" in {
      val result = form.bind(
        Map(
          "phoneNumber" -> "01632960001",
          fieldName     -> ""
        )
      )

      result.errors mustBe empty
    }

    "bind mobile numbers with a single digit" in {
      val result = form.bind(
        Map(
          "phoneNumber" -> "01632960001",
          fieldName     -> "1"
        )
      )

      result.errors mustBe empty
    }

    "bind mobile numbers with fewer than 10 digits" in {
      val result = form.bind(
        Map(
          "phoneNumber" -> "01632960001",
          fieldName     -> "123"
        )
      )

      result.errors mustBe empty
    }

    "bind mobile numbers with exactly 20 digits" in {
      val result = form.bind(
        Map(
          "phoneNumber" -> "01632960001",
          fieldName     -> "12345678901234567890"
        )
      )

      result.errors mustBe empty
    }

    "not bind mobile numbers with more than 20 digits" - {
      "no trim" in {
        val result = form.bind(
          Map(
            "phoneNumber" -> "01632960001",
            fieldName     -> "123456789012345678901"
          )
        )

        result.errors.map(_.message) must contain(lengthKey)
      }
      "trim" in {
        val result = form.bind(
          Map(
            "phoneNumber" -> "01632960001",
            fieldName     -> "12345 67890 12345 67890"
          )
        )

        result.errors.map(_.message) must contain(lengthKey)
      }
    }

    "not bind invalid characters" in {
      val result = form.bind(
        Map(
          "phoneNumber" -> "01632960001",
          fieldName     -> "mobile123"
        )
      )

      result.errors.map(_.message) must contain(invalidKey)
    }

    "not bind special characters" in {
      val result = form.bind(
        Map(
          "phoneNumber" -> "01632960001",
          fieldName     -> "07700-900000"
        )
      )

      result.errors.map(_.message) must contain(invalidKey)
    }
  }
}
