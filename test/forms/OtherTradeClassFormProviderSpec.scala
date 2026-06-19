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

class OtherTradeClassFormProviderSpec extends StringFieldBehaviours {

  private val requiredKey = "otherTradeClass.error.required"
  private val invalidKey = "otherTradeClass.error.invalid"

  private val otherTradeClassRegex =
    "^[a-zA-Z0-9\\- '\\s]+$"

  private val form = new OtherTradeClassFormProvider()()

  ".otherTradeClass" - {

    val fieldName = "otherTradeClass"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      Gen.oneOf(
        "valid example 1",
        "another val1d example",
        "a third ''' valid example",
        "they-never-expect-a-4th-valid-example"
      )
    )

    "not bind invalid formats" in {
      val invalids = Seq(
        "invalidCharacter$",
        "£$%",
        "#1@#*"
      )

      invalids.foreach { otherTradeClass =>
        val result = form.bind(Map(fieldName -> otherTradeClass)).apply(fieldName)
        result.errors must contain only FormError(fieldName, invalidKey, Seq(otherTradeClassRegex))
      }
    }

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
