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

import models.BusinessType.Soleproprietor
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.*

import java.time.LocalDate

class BusinessNameDetailsResponseSpec extends AnyWordSpec with Matchers {

  val systemDate: LocalDate = LocalDate.of(2024, 1, 15)

  val soleProprietorJson: JsObject = Json.obj(
    "mgdRegNumber"      -> "REG123",
    "solePropTitle"     -> "Mr",
    "solePropFirstName" -> "John",
    "solePropMidName"   -> "James",
    "solePropLastName"  -> "Smith",
    "businessType"      -> BusinessType.Soleproprietor.code,
    "tradingName"       -> "J Smith Trading",
    "systemDate"        -> "2024-01-15"
  )

  val businessJson: JsObject = Json.obj(
    "mgdRegNumber" -> "REG456",
    "businessName" -> "Acme Corp",
    "businessType" -> BusinessType.Corporatebody.code,
    "tradingName"  -> "Acme",
    "systemDate"   -> "2024-01-15"
  )

  "entityNameReads" when {

    "businessType is SoleProprietor" should {

      "parse into a SoleProprietorName" in {
        soleProprietorJson.validate[EntityName].get shouldBe a[SoleProprietorNameDetails]
      }

      "map all fields correctly" in {
        val result = soleProprietorJson.validate[EntityName].get.asInstanceOf[SoleProprietorNameDetails]

        result.mgdRegNum    shouldBe "REG123"
        result.title        shouldBe "Mr"
        result.firstName    shouldBe "John"
        result.middleName   shouldBe Some("James")
        result.lastName     shouldBe "Smith"
        result.businessType shouldBe Soleproprietor
        result.tradingName  shouldBe Some("J Smith Trading")
        result.systemDate   shouldBe Some(systemDate)
      }

      "succeed when optional fields are absent" in {
        val json = soleProprietorJson - "solePropMidName" - "tradingName" - "systemDate"
        val result = json.validate[EntityName].get.asInstanceOf[SoleProprietorNameDetails]

        result.middleName  shouldBe None
        result.tradingName shouldBe None
        result.systemDate  shouldBe None
      }

      "return JsError when mgdRegNumber is missing" in {
        (soleProprietorJson - "mgdRegNumber").validate[EntityName] shouldBe a[JsError]
      }

      "return JsError when solePropTitle is missing" in {
        (soleProprietorJson - "solePropTitle").validate[EntityName] shouldBe a[JsError]
      }

      "return JsError when solePropFirstName is missing" in {
        (soleProprietorJson - "solePropFirstName").validate[EntityName] shouldBe a[JsError]
      }

      "return JsError when solePropLastName is missing" in {
        (soleProprietorJson - "solePropLastName").validate[EntityName] shouldBe a[JsError]
      }

      "return JsError when solePropTitle is the wrong type" in {
        val json = soleProprietorJson ++ Json.obj("solePropTitle" -> 123)
        json.validate[EntityName] shouldBe a[JsError]
      }

      "return JsError when solePropFirstName is the wrong type" in {
        val json = soleProprietorJson ++ Json.obj("solePropFirstName" -> false)
        json.validate[EntityName] shouldBe a[JsError]
      }
    }

    "businessType is not SoleProprietor" should {

      "parse into a BusinessName" in {
        businessJson.validate[EntityName].get shouldBe a[BusinessNameDetails]
      }

      "map all fields correctly" in {
        val result = businessJson.validate[EntityName].get.asInstanceOf[BusinessNameDetails]

        result.mgdRegNum    shouldBe "REG456"
        result.businessName shouldBe "Acme Corp"
        result.businessType shouldBe BusinessType.Corporatebody
        result.tradingName  shouldBe Some("Acme")
        result.systemDate   shouldBe Some(systemDate)
      }

      "succeed when optional fields are absent" in {
        val json = businessJson - "tradingName" - "systemDate"
        val result = json.validate[EntityName].get.asInstanceOf[BusinessNameDetails]

        result.tradingName shouldBe None
        result.systemDate  shouldBe None
      }

      "return JsError when mgdRegNumber is missing" in {
        (businessJson - "mgdRegNumber").validate[EntityName] shouldBe a[JsError]
      }

      "return JsError when businessName is missing" in {
        (businessJson - "businessName").validate[EntityName] shouldBe a[JsError]
      }

      "return JsError when businessName is the wrong type" in {
        val json = businessJson ++ Json.obj("businessName" -> 123)
        json.validate[EntityName] shouldBe a[JsError]
      }

      "parse businessType CorporateBody as BusinessName" in {
        businessJson.validate[EntityName].get shouldBe a[BusinessNameDetails]
      }

      "parse a missing businessType as BusinessName" in {
        val json = businessJson - "businessType"
        json.validate[EntityName] shouldBe a[JsError]
      }
    }
  }

}
