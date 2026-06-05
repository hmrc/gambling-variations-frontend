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

class ChangeEmailAddressFormProviderSpec extends StringFieldBehaviours {

  private val requiredKey = "emailAddress.error.required"
  private val lengthKey   = "emailAddress.error.length"
  private val invalidKey  = "emailAddress.error.invalid"

  private val maxLength = 70

  private val validEmails = Seq(
    "name@example.com",
    "john_doe-123@test.co.uk",
    "a.b-c_d@example-domain.com",
    "USER_123@test.com"
  )

  private val form = new ChangeEmailAddressFormProvider()()

  ".emailAddress" - {

    val fieldName = "emailAddress"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      Gen.oneOf(validEmails)
    )

    "trim surrounding whitespace" in {
      val result = form.bind(Map(fieldName -> "   name@example.com   ")).value
      result.value mustBe "name@example.com"
    }

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}

