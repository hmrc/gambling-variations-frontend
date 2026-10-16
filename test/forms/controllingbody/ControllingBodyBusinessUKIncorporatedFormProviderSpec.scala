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
import play.api.data.FormError

class ControllingBodyBusinessUKIncorporatedFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "controllingBodyBusinessUKIncorporated.error.required"
  val invalidKey = "error.boolean"

  val form = new ControllingBodyBusinessUKIncorporatedFormProvider()()

  ".value" - {

    val fieldName = "value"

    "transform true to 1" in {
      form.bind(Map(fieldName -> "true")).value mustBe Some("1")
    }

    "transform false to 0" in {
      form.bind(Map(fieldName -> "false")).value mustBe Some("0")
    }

    "reject values other than true and false" in {
      form.bind(Map(fieldName -> "other")).errors must contain(
        FormError(fieldName, invalidKey)
      )
    }

    "reject an empty value" in {
      form.bind(Map(fieldName -> "")).errors must contain(
        FormError(fieldName, requiredKey)
      )
    }

    "reject a missing value" in {
      form.bind(Map.empty[String, String]).errors must contain(
        FormError(fieldName, requiredKey)
      )
    }

    "format 1 as true when filling the form" in {
      form.fill("1").data must contain("value" -> "true")
    }

    "format 0 as false when filling the form" in {
      form.fill("0").data must contain("value" -> "false")
    }
  }
}
