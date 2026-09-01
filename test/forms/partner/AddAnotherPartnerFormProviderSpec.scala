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

package forms.partner

import base.SpecBase
import play.api.data.Form

class AddAnotherPartnerFormProviderSpec extends SpecBase {

  private val requiredError =
    "partnerDetails.addAnotherPartner.error.required"

  private val formProvider =
    new AddAnotherPartnerFormProvider()

  private val form =
    formProvider(requiredError)

  "AddAnotherPartnerFormProvider" - {

    "must create a form" in {
      form mustBe a[Form[Boolean]]
    }

    "must bind true" in {
      val result =
        form.bind(
          Map(
            "value" -> "true"
          )
        )

      result.hasErrors mustEqual false
      result.value mustBe Some(true)
    }

    "must bind false" in {
      val result =
        form.bind(
          Map(
            "value" -> "false"
          )
        )

      result.hasErrors mustEqual false
      result.value mustBe Some(false)
    }

    "must return an error when value is empty" in {
      val result =
        form.bind(
          Map(
            "value" -> ""
          )
        )

      result.hasErrors mustEqual true
      result.error("value").value.message mustEqual requiredError
    }

    "must return an error when value is missing" in {
      val result =
        form.bind(Map.empty)

      result.hasErrors mustEqual true
      result.error("value").value.message mustEqual requiredError
    }
  }
}
