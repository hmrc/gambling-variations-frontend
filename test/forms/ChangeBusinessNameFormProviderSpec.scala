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
import models.BusinessType
import play.api.data.FormError

class ChangeBusinessNameFormProviderSpec extends StringFieldBehaviours {

  val maxLength = 160

  val businessType: BusinessType = BusinessType.Corporatebody

  val requiredKey = "changeBusinessName.error.required.corporatebody"
  val invalidKey = "changeBusinessName.error.invalid.corporatebody"
  val lengthKey = "changeBusinessName.error.length.corporatebody"

  val formProvider = new ChangeBusinessNameFormProvider()
  val form = formProvider(businessType)

  ".value" - {

    val fieldName = "value"

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    "must not bind strings longer than 160 characters" in {
      val longString = "a" * (maxLength + 1)
      val result = form.bind(Map(fieldName -> longString))

      result.errors must contain only FormError(fieldName, lengthKey, Seq(maxLength))
    }

    "must bind strings up to 160 characters" in {
      val validString = "a" * maxLength
      val result = form.bind(Map(fieldName -> validString))

      result.value.value mustEqual validString
    }

    "must fail when invalid characters are used" in {
      val result = form.bind(Map(fieldName -> "@@@@"))

      result.errors must contain only FormError(fieldName, invalidKey, Seq("^[A-Za-z0-9' -]+$"))
    }
  }
  "a partnership name" - {
    val partnershipForm = formProvider(BusinessType.Partnership)

    "accept every documented character and trim surrounding spaces" in {
      partnershipForm.bind(Map("value" -> "  A-Z 09 & '(),!/  ")).value.value mustEqual "A-Z 09 & '(),!/"
    }

    Seq("", "   ").foreach { value =>
      s"reject an empty name '$value'" in {
        partnershipForm.bind(Map("value" -> value)).errors.map(_.message) must contain("changeBusinessName.error.required.partnership")
      }
    }

    "accept exactly 35 characters" in {
      partnershipForm.bind(Map("value" -> ("A" * 35))).value.value mustEqual ("A" * 35)
    }

    "reject 36 characters using the partnership length message" in {
      partnershipForm.bind(Map("value" -> ("A" * 36))).errors must contain only
        FormError("value", "changeBusinessName.error.length.partnership", Seq(35))
    }

    Seq(".", "@", ":", "_", "[", "]").foreach { character =>
      s"reject the undocumented character $character" in {
        partnershipForm.bind(Map("value" -> s"Name${character}Name")).errors.map(_.message) must contain only
          "changeBusinessName.error.invalid.partnership"
      }
    }
  }

}
