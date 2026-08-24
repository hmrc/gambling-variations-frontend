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
import forms.partner.PartnerDetailsAddNationalInsuranceNumberFormProvider.*
import org.scalacheck.Gen
import play.api.data.FormError

class PartnerDetailsAddNationalInsuranceNumberFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "partnerDetailsAddNino.error.required"
  val lengthKey = "partnerDetailsAddNino.error.length"
  val invalidFormatKey = "partnerDetailsAddNino.error.invalidFormat"
  val invalidCharsKey = "partnerDetailsAddNino.error.invalidChars"
  val invalidKey = "partnerDetailsAddNino.error.invalid"
  val maxLength = 9

  val form = new PartnerDetailsAddNationalInsuranceNumberFormProvider()()

  ".value" - {

    val fieldName = "value"

    val validNinoGen: Gen[String] = for {
      firstChar  <- Gen.oneOf('A' to 'Z').filterNot(c => Seq('D', 'F', 'I', 'Q', 'U', 'V').contains(c))
      secondChar <- Gen.oneOf('A' to 'Z').filterNot(c => Seq('D', 'F', 'I', 'O', 'Q', 'U', 'V').contains(c))
      digits     <- Gen.listOfN(6, Gen.numChar).map(_.mkString)
      suffix     <- Gen.oneOf('A', 'B', 'C', 'D')
      prefix = s"$firstChar$secondChar"
      if !Seq("BG", "GB", "KN", "NK", "NT", "TN", "ZZ").contains(prefix)
    } yield s"$prefix$digits$suffix"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      validNinoGen
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength   = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    "fail to bind when format is invalid" in {
      val invalidFormatInputs = Seq("123456789", "QQ1234567", "QQAAAAAAA")

      for (input <- invalidFormatInputs) {
        val result = form.bind(Map(fieldName -> input)).apply(fieldName)
        result.errors must contain(FormError(fieldName, invalidFormatKey, Seq(ninoFormatRegex)))
      }
    }

    "fail to bind when prefix uses invalid characters" in {
      val invalidCharInputs = Seq("DA123456A", "FA123456A", "IA123456A", "QA123456A", "UA123456A", "VA123456A")

      for (input <- invalidCharInputs) {
        val result = form.bind(Map(fieldName -> input)).apply(fieldName)
        result.errors must contain(FormError(fieldName, invalidCharsKey, Seq(ninoCharsRegex)))
      }
    }

    "fail to bind when NINO is structurally well-formed but not a real/valid prefix" in {
      val disallowedPrefixes = Seq("BG123456A", "GB123456A", "KN123456A", "NK123456A", "NT123456A", "TN123456A", "ZZ123456A")

      for (input <- disallowedPrefixes) {
        val result = form.bind(Map(fieldName -> input)).apply(fieldName)
        result.errors must contain(FormError(fieldName, invalidKey, Seq(ninoValidRegex)))
      }
    }
  }
}
