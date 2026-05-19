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

class BusinessTradingNameFormProviderSpec extends StringFieldBehaviours {

  private val requiredKey = "businessTradingName.error.required"
  private val lengthKey = "businessTradingName.error.length"
  private val invalidKey = "businessTradingName.error.invalid"

  private val maxLength = 100

  private val form = new BusinessTradingNameFormProvider()()

  private val businessNameRegex = "^[a-zA-Z0-9\\- '\\s]+$"

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      Gen.oneOf(
        "ABC Ltd",
        "O'Brien Services",
        "Shop-24",
        "Test Business 123"
      )
    )

    "bind strings longer than maximum length with length error" in {

      val validLongString = "A" * (maxLength + 1)

      val result = form.bind(
        Map(fieldName -> validLongString)
      )

      result.errors must contain only
        FormError(fieldName, lengthKey, Seq(maxLength))
    }

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    "not bind invalid characters" in {

      val invalidValues = Seq(
        "Test@Ltd",
        "Hello#World",
        "Business!",
        "Company$123"
      )

      invalidValues.foreach { value =>

        val result = form.bind(
          Map(fieldName -> value)
        )

        result.errors must contain(
          FormError(fieldName, invalidKey, Seq(businessNameRegex))
        )
      }
    }
  }
}
