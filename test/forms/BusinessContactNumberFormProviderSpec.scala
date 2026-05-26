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

class BusinessContactNumberFormProviderSpec extends StringFieldBehaviours {

  val form = new BusinessContactNumberFormProvider()()

  ".phoneNumber" - {

    val fieldName = "phoneNumber"
    val requiredKey = "businessContactNumber.error.PhoneNumber.required"
    val lengthKey = "businessContactNumber.error.PhoneNumber.length"
    val invalidKey = "businessContactNumber.error.PhoneNumber.invalid"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      Gen.oneOf(
        "01632 960 001",
        "07700900000",
        "1234567890",
        "01234 567 890"
      )
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    "not bind strings longer than 20 characters" in {

      val result = form.bind(
        Map(
          fieldName      -> "123456789012345678901",
          "mobileNumber" -> "07700900000"
        )
      )

      result.errors.map(_.message) must contain(lengthKey)
    }

    "not bind invalid characters" in {

      val result = form.bind(
        Map(
          fieldName      -> "abc123",
          "mobileNumber" -> "07700900000"
        )
      )

      result.errors.map(_.message) must contain(invalidKey)
    }

    "not bind special characters" in {

      val result = form.bind(
        Map(
          fieldName      -> "01632-960-001",
          "mobileNumber" -> "07700900000"
        )
      )

      result.errors.map(_.message) must contain(invalidKey)
    }
  }

  ".mobileNumber" - {

    val fieldName = "mobileNumber"
    val lengthKey = "businessContactNumber.error.MobileNumber.length"
    val invalidKey = "businessContactNumber.error.MobileNumber.invalid"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      Gen.oneOf(
        "07700 900000",
        "07123456789",
        "01234 567890"
      )
    )

    "allow empty mobile number" in {

      val result = form.bind(
        Map(
          "phoneNumber" -> "01632960001",
          fieldName     -> ""
        )
      )

      result.errors mustBe empty
    }

    "not bind strings longer than 20 characters" in {

      val result = form.bind(
        Map(
          "phoneNumber" -> "01632960001",
          fieldName     -> "123456789012345678901"
        )
      )

      result.errors.map(_.message) must contain(lengthKey)
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
