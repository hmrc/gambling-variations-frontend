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

class BusinessTypeSpec extends AnyWordSpec with Matchers {

  "BusinessType.fromCode" should {

    "return correct BusinessType for valid codes" in {
      BusinessType.fromCode(1) shouldBe Some(BusinessType.Soleproprietor)
      BusinessType.fromCode(2) shouldBe Some(BusinessType.Corporatebody)
      BusinessType.fromCode(3) shouldBe Some(BusinessType.Unincorporatedbody)
      BusinessType.fromCode(4) shouldBe Some(BusinessType.Partnership)
      BusinessType.fromCode(5) shouldBe Some(BusinessType.LimitedLiabilityPartnership)
    }

    "return None for invalid codes" in {
      BusinessType.fromCode(0)  shouldBe None
      BusinessType.fromCode(6)  shouldBe None
      BusinessType.fromCode(-1) shouldBe None
    }
  }

  "BusinessType Reads" should {

    "deserialize valid codes to BusinessType" in {
      Json.fromJson[BusinessType](JsNumber(1)) shouldBe JsSuccess(BusinessType.Soleproprietor)
      Json.fromJson[BusinessType](JsNumber(2)) shouldBe JsSuccess(BusinessType.Corporatebody)
      Json.fromJson[BusinessType](JsNumber(5)) shouldBe JsSuccess(BusinessType.LimitedLiabilityPartnership)
    }

    "fail for invalid codes" in {
      val result = Json.fromJson[BusinessType](JsNumber(99))

      result.isError shouldBe true
      result match {
        case JsError(errors) =>
          errors.head._2.head.message shouldBe "Invalid business type"
        case _ => fail("Expected JsError")
      }
    }

    "fail for non-numeric JSON" in {
      Json.fromJson[BusinessType](JsString("abc")).isError shouldBe true
      Json.fromJson[BusinessType](JsBoolean(true)).isError shouldBe true
    }
  }

  "BusinessType Writes" should {

    "serialize BusinessType to numeric JSON code" in {
      Json.toJson[BusinessType](BusinessType.Soleproprietor)              shouldBe JsNumber(1)
      Json.toJson[BusinessType](BusinessType.Corporatebody)               shouldBe JsNumber(2)
      Json.toJson[BusinessType](BusinessType.LimitedLiabilityPartnership) shouldBe JsNumber(5)
    }
  }

  "BusinessType Format" should {

    "round-trip correctly (write then read)" in
      BusinessType.values.foreach { bt =>
        val json = Json.toJson[BusinessType](bt)
        val result = json.validate[BusinessType]

        result shouldBe JsSuccess(bt)
      }
  }
}
