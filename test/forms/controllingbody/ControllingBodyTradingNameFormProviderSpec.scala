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
import org.scalacheck.Gen
import play.api.data.FormError

class ControllingBodyTradingNameFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "controllingBodyTradingName.error.required"
  val lengthKey = "controllingBodyTradingName.error.length"
  val maxLength = 100

  val form = new ControllingBodyTradingNameFormProvider()()

  ".controllingBodyTradingName" - {

    val fieldName = "controllingBodyTradingName"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      Gen.choose(1, maxLength).flatMap(length => Gen.listOfN(length, Gen.alphaNumChar).map(_.mkString))
    )

    "not bind valid-character names longer than 100 characters" in {
      forAll(Gen.choose(maxLength + 1, maxLength * 2)) { length =>
        val result = form.bind(Map(fieldName -> ("A" * length))).apply(fieldName)
        result.errors must contain only FormError(fieldName, lengthKey, Seq(maxLength))
      }
    }

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
