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

import forms.FaxNumberFormProvider
import forms.behaviours.StringFieldBehaviours
import org.scalacheck.Gen
import play.api.data.{Form, FormError}

class ChangePartnerFaxNumberFormProviderSpec extends StringFieldBehaviours {

  private val requiredKey = "partnerDetailsFaxNumber.error.required"
  private val lengthKey = "partnerDetailsFaxNumber.error.length"
  private val invalidCharactersKey = "partnerDetailsFaxNumber.error.invalid.characters"

  private val maxLength = 20
  private val faxNumberCharactersRegex: String = "^[0-9 ]+$"

  val form: Form[String] = new FaxNumberFormProvider()("partnerDetailsFaxNumber")

  ".value" - {

    val fieldName = "faxNumber"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      Gen.oneOf(
        "01632960001",
        "01632 960 001",
        "07700900982",
        "07700 900 982",
        "01234567890123456789",
        "12345678901234567890"
      )
    )

    s"not bind strings longer than $maxLength characters" in {
      val result = form.bind(Map(fieldName -> ("0" * (maxLength + 1)))).apply(fieldName)

      result.errors must contain(FormError(fieldName, lengthKey, Seq(maxLength)))
    }

    "not bind invalid characters" in {
      val result = form.bind(Map(fieldName -> "01632-960001")).apply(fieldName)

      result.errors must contain only FormError(fieldName, invalidCharactersKey, Seq(faxNumberCharactersRegex))
    }

    "bind length and invalid character errors together" in {
      val result = form.bind(Map(fieldName -> (("0" * (maxLength + 1)) + "-"))).apply(fieldName)

      result.errors mustBe Seq(
        FormError(fieldName, lengthKey, Seq(maxLength)),
        FormError(fieldName, invalidCharactersKey, Seq(faxNumberCharactersRegex))
      )
    }

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
