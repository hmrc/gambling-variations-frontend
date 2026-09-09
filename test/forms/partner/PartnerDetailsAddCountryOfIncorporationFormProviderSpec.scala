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
import forms.partner.PartnerDetailsAddCountryOfIncorporationFormProvider.*
import org.scalacheck.Gen
import play.api.data.FormError

class PartnerDetailsAddCountryOfIncorporationFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "partnerDetailsAddCountryOfIncorporation.error.required"
  val lengthKey = "partnerDetailsAddCountryOfIncorporation.error.length"
  val invalidKey = "partnerDetailsAddCountryOfIncorporation.error.invalid"

  val form = new PartnerDetailsAddCountryOfIncorporationFormProvider()()

  ".value" - {

    val fieldName = "value"

    // Generator restricted strictly to characters matching countryRegex
    val validCountryGen: Gen[String] = for {
      length <- Gen.chooseNum(1, maxStringLength)
      chars  <- Gen.listOfN(length, Gen.oneOf(('A' to 'Z') ++ ('a' to 'z') ++ ('0' to '9') ++ Seq(' ', '-', '\'')))
    } yield chars.mkString

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validCountryGen
    )

    "must fail to bind a string exceeding 100 characters" in {
      val invalidLength = "A" * 101
      val result = form.bind(Map(fieldName -> invalidLength))
      result.errors must contain(FormError(fieldName, lengthKey, Seq(maxStringLength)))
    }

    "must fail to bind invalid characters" in {
      val invalidCharacters = "United States of America!"
      val result = form.bind(Map(fieldName -> invalidCharacters))
      result.errors must contain(FormError(fieldName, invalidKey, Seq(countryRegex)))
    }

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
