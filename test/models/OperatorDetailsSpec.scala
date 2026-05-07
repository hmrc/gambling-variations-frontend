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

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.Json
import java.time.LocalDate

class OperatorDetailsSpec extends AnyFreeSpec with Matchers {

  private val sample = OperatorDetails(
    mgdRegNumber       = "MGD123",
    solePropName       = Some("John Doe"),
    solePropTitle      = Some("Mr"),
    solePropFirstName  = Some("John"),
    solePropMiddleName = Some("M"),
    solePropLastName   = Some("Doe"),
    tradingName        = Some("JD Trading"),
    businessName       = Some("JD Ltd"),
    businessType       = Some(1),
    adi                = Some("ADI123"),
    address1           = Some("123 Street"),
    address2           = Some("Area"),
    address3           = Some("City"),
    address4           = Some("County"),
    postcode           = Some("AB12 3CD"),
    country            = Some("UK"),
    abroadSig          = Some("N"),
    agentOwnRef        = Some("REF001"),
    systemDate         = Some(LocalDate.of(2026, 5, 7))
  )

  ".OperatorDetails JSON" - {

    "must serialize to JSON correctly" in {
      val json = Json.toJson(sample)
      (json \ "mgdRegNumber").as[String] mustBe "MGD123"
      (json \ "solePropName").as[String] mustBe "John Doe"
      (json \ "systemDate").as[String] mustBe "2026-05-07"
    }

    "must deserialize from JSON correctly" in {
      val jsonString =
        """
          |{
          | "mgdRegNumber": "MGD123",
          | "solePropName": "John Doe",
          | "solePropTitle": "Mr",
          | "solePropFirstName": "John",
          | "solePropMiddleName": "M",
          | "solePropLastName": "Doe",
          | "tradingName": "JD Trading",
          | "businessName": "JD Ltd",
          | "businessType": 1,
          | "adi": "ADI123",
          | "address1": "123 Street",
          | "address2": "Area",
          | "address3": "City",
          | "address4": "County",
          | "postcode": "AB12 3CD",
          | "country": "UK",
          | "abroadSig": "N",
          | "agentOwnRef": "REF001",
          | "systemDate": "2026-05-07"
          |}
          |""".stripMargin

      val parsed = Json.parse(jsonString).as[OperatorDetails]
      parsed mustBe sample
    }

    "must handle optional fields being None" in {
      val minimal = OperatorDetails(
        mgdRegNumber       = "MGD999",
        solePropName       = None,
        solePropTitle      = None,
        solePropFirstName  = None,
        solePropMiddleName = None,
        solePropLastName   = None,
        tradingName        = None,
        businessName       = None,
        businessType       = None,
        adi                = None,
        address1           = None,
        address2           = None,
        address3           = None,
        address4           = None,
        postcode           = None,
        country            = None,
        abroadSig          = None,
        agentOwnRef        = None,
        systemDate         = None
      )

      val json = Json.toJson(minimal)
      (json \ "mgdRegNumber").as[String] mustBe "MGD999"
      (json \ "systemDate").toOption mustBe None

      val roundTrip = Json.fromJson[OperatorDetails](json).get
      roundTrip mustBe minimal
    }
  }
}
