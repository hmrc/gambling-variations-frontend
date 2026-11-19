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

package models

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.*
import play.api.mvc.JavascriptLiteral

class RegistrationStatusSpec extends AnyWordSpec with Matchers {

  "RegistrationStatus.fromCode" should {

    "return correct RegistrationStatus for valid codes" in {
      RegistrationStatus.fromCode(0) shouldBe Some(RegistrationStatus.Cancelled)
      RegistrationStatus.fromCode(1) shouldBe Some(RegistrationStatus.Registered)
      RegistrationStatus.fromCode(2) shouldBe Some(RegistrationStatus.ToBeRegistered)
    }

    "return None for invalid codes" in {
      RegistrationStatus.fromCode(9)  shouldBe None
      RegistrationStatus.fromCode(6)  shouldBe None
      RegistrationStatus.fromCode(-1) shouldBe None
    }
  }

  "RegistrationStatus.values" should {

    "contain all business types in the expected order" in {
      RegistrationStatus.values shouldBe Seq(
        RegistrationStatus.Cancelled,
        RegistrationStatus.Registered,
        RegistrationStatus.ToBeRegistered
      )
    }
  }

  "RegistrationStatus.enumerable" should {

    "find values by their string representation" in {
      RegistrationStatus.enumerable.withName("cancelled")      shouldBe Some(RegistrationStatus.Cancelled)
      RegistrationStatus.enumerable.withName("registered")     shouldBe Some(RegistrationStatus.Registered)
      RegistrationStatus.enumerable.withName("toBeRegistered") shouldBe Some(RegistrationStatus.ToBeRegistered)
    }

    "return None for an unknown value" in {
      RegistrationStatus.enumerable.withName("unknown") shouldBe None
    }
  }

  "RegistrationStatus.jsLiteral" should {

    "convert a business type to its string representation" in {
      val jsLiteral = implicitly[JavascriptLiteral[RegistrationStatus]]

      jsLiteral.to(RegistrationStatus.Cancelled)      shouldBe "cancelled"
      jsLiteral.to(RegistrationStatus.Registered)     shouldBe "registered"
      jsLiteral.to(RegistrationStatus.ToBeRegistered) shouldBe "toBeRegistered"
    }
  }

  "RegistrationStatus Reads" should {

    "deserialize valid codes to RegistrationStatus" in {
      Json.fromJson[RegistrationStatus](JsNumber(0)) shouldBe JsSuccess(RegistrationStatus.Cancelled)
      Json.fromJson[RegistrationStatus](JsNumber(1)) shouldBe JsSuccess(RegistrationStatus.Registered)
      Json.fromJson[RegistrationStatus](JsNumber(2)) shouldBe JsSuccess(RegistrationStatus.ToBeRegistered)
    }

    "fail for invalid codes" in {
      val result = Json.fromJson[RegistrationStatus](JsNumber(99))

      result.isError shouldBe true
      result match {
        case JsError(errors) =>
          errors.head._2.head.message shouldBe "Invalid business type"
        case _ => fail("Expected JsError")
      }
    }

    "fail for non-numeric JSON" in {
      Json.fromJson[RegistrationStatus](JsString("abc")).isError shouldBe true
      Json.fromJson[RegistrationStatus](JsBoolean(true)).isError shouldBe true
    }
  }

  "RegistrationStatus Writes" should {

    "serialize RegistrationStatus to numeric JSON code" in {
      Json.toJson[RegistrationStatus](RegistrationStatus.Cancelled)      shouldBe JsNumber(0)
      Json.toJson[RegistrationStatus](RegistrationStatus.Registered)     shouldBe JsNumber(1)
      Json.toJson[RegistrationStatus](RegistrationStatus.ToBeRegistered) shouldBe JsNumber(2)
    }
  }

  "RegistrationStatus Format" should {

    "round-trip correctly (write then read)" in
      RegistrationStatus.values.foreach { bt =>
        val json = Json.toJson[RegistrationStatus](bt)
        val result = json.validate[RegistrationStatus]

        result shouldBe JsSuccess(bt)
      }
  }
}
