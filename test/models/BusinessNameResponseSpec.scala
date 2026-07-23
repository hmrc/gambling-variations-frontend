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

import java.time.LocalDate

class BusinessNameResponseSpec extends AnyWordSpec with Matchers {

  "BusinessNameResponse" should {

    "read and write JSON correctly" in {

      val model =
        BusinessNameResponse(
          mgdRegNumber      = "MGD123456",
          solePropTitle     = Some("Mr"),
          solePropFirstName = Some("John"),
          solePropMidName   = Some("A"),
          solePropLastName  = Some("Smith"),
          businessName      = Some("John Smith Trading"),
          businessType      = Some(BusinessType.Soleproprietor),
          tradingName       = Some("Trading Name"),
          systemDate        = Some(LocalDate.of(2026, 1, 1))
        )

      val json = Json.toJson(model)

      json.as[BusinessNameResponse] shouldBe model
    }

    "read EntityName as SoleProprietorNameDetails when business type is Soleproprietor" in {

      val json =
        Json.obj(
          "businessType"      -> 1,
          "mgdRegNumber"      -> "ABC12345678901",
          "solePropTitle"     -> "Mr",
          "solePropFirstName" -> "John",
          "solePropMidName"   -> "A",
          "solePropLastName"  -> "Smith",
          "tradingName"       -> "Trading Name",
          "systemDate"        -> "2025-01-01"
        )

      val result =
        BusinessNameResponse.entityNameReads.reads(json)

      result.isSuccess shouldBe true
      result.get       shouldBe a[SoleProprietorNameDetails]
    }

    "read EntityName as BusinessNameDetails when business type is Corporatebody" in {

      val json =
        Json.obj(
          "businessType" -> 2,
          "mgdRegNumber" -> "ABC12345678901",
          "businessName" -> "Test Business Ltd",
          "tradingName"  -> "Trading Name",
          "systemDate"   -> "2025-01-01"
        )

      val result =
        BusinessNameResponse.entityNameReads.reads(json)

      result.isSuccess shouldBe true
      result.get       shouldBe a[BusinessNameDetails]
    }

    "fall through to BusinessNameDetails reads when business type is missing" in {

      val json =
        Json.obj(
          "businessName" -> "Test Business Ltd"
        )

      val result =
        BusinessNameResponse.entityNameReads.reads(json)

      result.isError shouldBe true
    }

    "expose soleProprietorReads" in {

      BusinessNameResponse.soleProprietorReads should not be null
    }

    "expose businessNameDetailsReads" in {

      BusinessNameResponse.businessNameDetailsReads should not be null
    }
  }
}
