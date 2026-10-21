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

package forms.partnerdetails

import forms.behaviours.StringFieldBehaviours
import org.scalacheck.Gen
import play.api.data.FormError

class PartnerEmailAddressFormProviderSpec extends StringFieldBehaviours {

  private val requiredKey = "partnerEmailAddress.error.required"
  private val lengthKey = "partnerEmailAddress.error.length"
  private val invalidKey = "partnerEmailAddress.error.invalid"

  private val maxLength = 70
  private val maxPartLength = 64

  private val form = new PartnerEmailAddressFormProvider()("partnerEmailAddress")

  ".partnerEmailAddress" - {

    val fieldName = "partnerEmailAddress"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      Gen.oneOf(
        "name@example.com",
        "john_doe-123@test.co.uk",
        "a.b-c_d@example-domain.com",
        "USER_123@test.com"
      )
    )

    "trim surrounding whitespace" in {
      val result = form.bind(Map(fieldName -> "   name@example.com   "))

      result.value mustBe Some("name@example.com")
    }

    "accept an email address with a total length of 70 characters" in {
      val email = ("a" * maxPartLength) + "@b.com"

      email.length mustBe maxLength

      val result = form.bind(Map(fieldName -> email))

      result.errors mustBe empty
      result.value mustBe Some(email)
    }

    "reject an email address with a total length greater than 70 characters" in {
      val email = ("a" * (maxPartLength + 1)) + "@b.com"

      email.length mustBe maxLength + 1

      val result = form.bind(Map(fieldName -> email))

      result.errors must contain(
        FormError(fieldName, lengthKey, Seq(maxLength))
      )
    }

    "reject a local part longer than 64 characters" in {
      val email = ("a" * (maxPartLength + 1)) + "@b.c"

      email.length mustBe 69

      val result = form.bind(Map(fieldName -> email))

      result.errors must contain(
        FormError(fieldName, invalidKey)
      )
    }

    "reject a domain part longer than 64 characters" in {
      val email = "a@" + ("b" * 65)

      email.length mustBe 67

      val result = form.bind(Map(fieldName -> email))

      result.errors must contain(
        FormError(fieldName, invalidKey)
      )
    }

    "accept an email address with a domain part of 64 characters" in {
      val email = "a@" + ("b" * 64)

      email.length mustBe 66

      val result = form.bind(Map(fieldName -> email))

      result.errors mustBe empty
      result.value mustBe Some(email)
    }

    "reject invalid email formats" in {
      val invalids = Seq(
        "plainaddress",
        "missingatsign.com",
        "missingdomain@",
        "@missinglocal.com",
        "bad!chars@test.com",
        "two@@ats.com",
        "spaces are not allowed@test.com",
        ".john@gmail.com",
        "john.@gmail.com",
        "..john@gmail.com",
        "john..smith@gmail.com",
        "john@.gmail.com",
        "john@gmail.",
        "john@-gmail.com",
        "john@gmail..com",
        "john@_",
        "john@---"
      )

      invalids.foreach { email =>
        withClue(s"Email: '$email'") {
          val result = form.bind(Map(fieldName -> email))

          result.errors must contain(
            FormError(fieldName, invalidKey)
          )
        }
      }
    }

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
