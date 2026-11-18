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

package forms.cancellation

import forms.behaviours.BooleanFieldBehaviours
import play.api.data.FormError

class WarningForCancellationFormProviderSpec extends BooleanFieldBehaviours {

  val requiredKey = "warningForCancellation.error.required"
  val invalidKey = "error.boolean"

  val form = new WarningForCancellationFormProvider()()

  ".value" - {

    val fieldName = "value"

    "bind true" in {
      val result = form.bind(Map(fieldName -> "true"))
      result.value.value mustBe true
      result.errors mustBe empty
    }

    "not bind on false" in {
      val result = form.bind(Map(fieldName -> "false"))
      result.value mustBe None
      result.errors mustBe Seq(FormError(fieldName, requiredKey))
    }

    "not bind non-booleans" in {

      forAll(nonBooleans -> "nonBoolean") { nonBoolean =>
        val result = form.bind(Map(fieldName -> nonBoolean)).apply(fieldName)
        result.errors mustBe Seq(FormError(fieldName, invalidKey))
      }
    }

    "not bind when key is not present at all" in {

      val result = form.bind(emptyForm).apply(fieldName)
      result.errors mustEqual Seq(FormError(fieldName, requiredKey))
    }

    "not bind blank values" in {

      val result = form.bind(Map(fieldName -> "")).apply(fieldName)
      result.errors mustEqual Seq(FormError(fieldName, requiredKey))
    }
  }

}
