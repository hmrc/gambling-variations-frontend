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
import play.api.libs.json.Json

class BusinessAddressSpec extends AnyWordSpec with Matchers {

  "BusinessAddress" should {

    "read a complete address" in {

      val json =
        Json.obj(
          "mgdRegNumber" -> "MGD123456",
          "adi"          -> "1st floor",
          "address1"     -> "address1",
          "address2"     -> "address2",
          "address3"     -> "address3",
          "address4"     -> "address4",
          "postcode"     -> "L1 8YL",
          "country"      -> "England",
          "iomOrCiFlag"  -> "FALSE"
        )

      val result =
        json.validate[BusinessAddress]

      result.isSuccess shouldBe true

      result.get.mgdRegNumber shouldBe "MGD123456"
      result.get.adi          shouldBe Some("1st floor")
      result.get.address shouldBe Some(
        Address(
          "address1",
          Some("address2"),
          Some("address3"),
          Some("address4"),
          Some("L1 8YL"),
          Some("England")
        )
      )
      result.get.iomOrCiFlag shouldBe Some("FALSE")
    }

    "set address to None when all address fields are missing" in {

      val json =
        Json.obj(
          "mgdRegNumber" -> "MGD123456"
        )

      val result =
        json.validate[BusinessAddress]

      result.isSuccess       shouldBe true
      result.get.adi         shouldBe None
      result.get.address     shouldBe None
      result.get.iomOrCiFlag shouldBe None
    }
  }
}
