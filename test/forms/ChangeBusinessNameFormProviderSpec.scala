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

  val requiredKey = "changeBusinessName.error.required.corporateBody"
  val invalidKey  = "changeBusinessName.error.invalid.corporateBody"
  val lengthKey   = "changeBusinessName.error.length.corporateBody"

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
}

