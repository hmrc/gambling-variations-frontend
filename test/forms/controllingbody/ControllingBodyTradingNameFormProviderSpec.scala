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

    "accept the punctuation listed in the error copy" in {
      form.bind(Map(fieldName -> "O'Brien / Trading-1")).value mustBe Some("O'Brien / Trading-1")
    }

    "use the trading-name error for invalid characters" in {
      form.bind(Map(fieldName -> "Trading@Name")).errors must contain only
        FormError(fieldName, "controllingBodyTradingName.error.invalid", Seq(ControllingBodyTradingNameFormProvider.tradingNameRegex))
    }

    "replace multiple consecutive spaces between characters with a single space" in {
      Seq(
        "ABC    123    Trading"   -> "ABC 123 Trading",
        "ABC\t\t123\t\tTrading"   -> "ABC 123 Trading",
        "ABC\t\n123\n\n\tTrading" -> "ABC 123 Trading",
        "AB\t\n123"               -> "AB 123"
      ).foreach { case (input, expected) =>
        val bound = form.bind(Map(fieldName -> input))

        bound.value.value mustBe expected
        bound.errors mustBe empty
      }
    }

    "trim leading and trailing spaces" in {
      val input = "  ABC Trading  "
      val expected = "ABC Trading"

      val result = form.bind(Map(fieldName -> input))

      result.value mustBe Some(expected)
    }

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

}
