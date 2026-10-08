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

class BusinessAddressAdditionalInfoFormProviderSpec extends StringFieldBehaviours {

  val form = new BusinessAddressAdditionalInfoFormProvider()()

  ".businessAddressAdditionalInfo" - {
    val fieldName = "businessAddressAdditionalInfo"

    val lengthKey = "businessAddressAdditionalInfo.error.length"
    val requiredKey = "businessAddressAdditionalInfo.error.required"
    val invalidKey = "businessAddressAdditionalInfo.error.invalid"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      Gen.oneOf(
        "abc",
        "wordsandwords",
        Seq.fill(100)('A').mkString
      )
    )

    "return error if form is empty" in {
      val result = form.bind(
        Map(
          "businessAddressAdditionalInfo" -> ""
        )
      )
      result.errors.map(_.message) must contain(requiredKey)
    }

    "return error if max length is exceeded" in {
      val result = form.bind(
        Map(
          "businessAddressAdditionalInfo" -> Seq.fill(101)('A').mkString

        )
      )
      result.errors.map(_.message) must contain(lengthKey)
    }

    "return error if invalid data submitted" in {
      val result = form.bind(
        Map(
          "businessAddressAdditionalInfo" -> ">>>><<<<<"

        )
      )
      result.errors.map(_.message) must contain(invalidKey)
    }

  }
}
