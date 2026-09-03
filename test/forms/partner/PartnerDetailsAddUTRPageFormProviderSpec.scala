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

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class PartnerDetailsAddUTRPageFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "partnerDetailsAddUTRPage.error.required"
  val invalidCharsKey = "partnerDetailsAddUTRPage.error.invalidChars"
  val incorrectKey = "partnerDetailsAddUTRPage.error.incorrect"
  val invalidKey = "partnerDetailsAddUTRPage.error.invalid"
  val maxLength = 10

  val form = new PartnerDetailsAddUTRPageFormProvider()()

  ".value" - {

    val fieldName = "value"

    val validUtr = "1121766916"

    "must bind valid UTR with correct Modulo 11 checksum" in {
      val result = form.bind(Map(fieldName -> validUtr))
      result.errors mustBe empty
      result.value.value mustBe validUtr
    }

    "must fail to bind non-numeric characters" in {
      val result = form.bind(Map(fieldName -> "12345ABCDE"))
      result.errors must contain(FormError(fieldName, invalidCharsKey, Seq("""^\d+$""")))
    }

    "must fail to bind string exceeding 10 characters" in {
      val result = form.bind(Map(fieldName -> "112176691612"))
      result.errors must contain(FormError(fieldName, incorrectKey, Seq(maxLength)))
    }

    "must fail to bind 10-digit string with invalid checksum" in {
      val result = form.bind(Map(fieldName -> "1234567890"))
      result.errors must contain(FormError(fieldName, invalidKey))
    }

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
