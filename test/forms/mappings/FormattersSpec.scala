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

package forms.mappings

import models.BusinessType
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.data.FormError

class FormattersSpec extends AnyWordSpec with Matchers with Formatters {

  "stringFormatter" should {

    val formatter = stringFormatter("required")

    "bind a valid string" in {
      formatter.bind("name", Map("name" -> "John")) shouldBe Right("John")
    }

    "fail when value is missing" in {
      formatter.bind("name", Map.empty) shouldBe
        Left(Seq(FormError("name", "required")))
    }

    "fail when value is blank" in {
      formatter.bind("name", Map("name" -> "  ")) shouldBe
        Left(Seq(FormError("name", "required")))
    }

    "unbind a string value" in {
      formatter.unbind("name", "John") shouldBe Map("name" -> "John")
    }
  }

  "booleanFormatter" should {

    val formatter = booleanFormatter("required", "invalid")

    "bind true" in {
      formatter.bind("active", Map("active" -> "true")) shouldBe Right(true)
    }

    "bind false" in {
      formatter.bind("active", Map("active" -> "false")) shouldBe Right(false)
    }

    "fail for invalid boolean values" in {
      formatter.bind("active", Map("active" -> "yes")) shouldBe
        Left(Seq(FormError("active", "invalid")))
    }

    "fail when value is missing" in {
      formatter.bind("active", Map.empty) shouldBe
        Left(Seq(FormError("active", "required")))
    }

    "unbind a boolean value" in {
      formatter.unbind("active", true) shouldBe Map("active" -> "true")
    }
  }

  "intFormatter" should {

    val formatter = intFormatter(
      requiredKey    = "required",
      wholeNumberKey = "wholeNumber",
      nonNumericKey  = "nonNumeric"
    )

    "bind an integer" in {
      formatter.bind("amount", Map("amount" -> "123")) shouldBe Right(123)
    }

    "bind an integer containing commas" in {
      formatter.bind("amount", Map("amount" -> "1,234")) shouldBe Right(1234)
    }

    "fail when decimal is supplied" in {
      formatter.bind("amount", Map("amount" -> "12.50")) shouldBe
        Left(Seq(FormError("amount", "wholeNumber")))
    }

    "fail when non numeric value is supplied" in {
      formatter.bind("amount", Map("amount" -> "abc")) shouldBe
        Left(Seq(FormError("amount", "nonNumeric")))
    }

    "fail when value is missing" in {
      formatter.bind("amount", Map.empty) shouldBe
        Left(Seq(FormError("amount", "required")))
    }

    "return nonNumeric error when number is too large for Int" in {
      formatter.bind(
        "amount",
        Map("amount" -> "999999999999999999999999999999")
      ) shouldBe
        Left(Seq(FormError("amount", "nonNumeric")))
    }

    "unbind an integer value" in {
      formatter.unbind("amount", 123) shouldBe Map("amount" -> "123")
    }
  }

  "enumerableFormatter" should {

    val formatter =
      enumerableFormatter[BusinessType]("required", "invalid")

    "bind a valid enum value" in {
      formatter.bind(
        "businessType",
        Map("businessType" -> "soleproprietor")
      ) shouldBe Right(BusinessType.Soleproprietor)
    }

    "fail for an invalid enum value" in {
      formatter.bind(
        "businessType",
        Map("businessType" -> "unknown")
      ) shouldBe Left(Seq(FormError("businessType", "invalid")))
    }

    "fail when value is missing" in {
      formatter.bind(
        "businessType",
        Map.empty
      ) shouldBe Left(Seq(FormError("businessType", "required")))
    }

    "unbind an enum value" in {
      formatter.unbind(
        "businessType",
        BusinessType.Partnership
      ) shouldBe Map("businessType" -> "partnership")
    }
  }

  "currencyFormatter" should {

    val formatter = currencyFormatter(
      requiredKey       = "required",
      invalidNumericKey = "invalidNumeric",
      nonNumericKey     = "nonNumeric"
    )

    "bind a whole number" in {
      formatter.bind("amount", Map("amount" -> "100")) shouldBe
        Right(BigDecimal(100))
    }

    "bind a decimal value" in {
      formatter.bind("amount", Map("amount" -> "100.50")) shouldBe
        Right(BigDecimal("100.50"))
    }

    "bind values containing pound signs, commas and spaces" in {
      formatter.bind("amount", Map("amount" -> "£1,234.56")) shouldBe
        Right(BigDecimal("1234.56"))
    }

    "fail for more than two decimal places" in {
      formatter.bind("amount", Map("amount" -> "10.123")) shouldBe
        Left(Seq(FormError("amount", "invalidNumeric")))
    }

    "fail for non numeric values" in {
      formatter.bind("amount", Map("amount" -> "abc")) shouldBe
        Left(Seq(FormError("amount", "nonNumeric")))
    }

    "fail when value is missing" in {
      formatter.bind("amount", Map.empty) shouldBe
        Left(Seq(FormError("amount", "required")))
    }

    "unbind a currency value" in {
      formatter.unbind("amount", BigDecimal("99.99")) shouldBe
        Map("amount" -> "99.99")
    }
  }
}
