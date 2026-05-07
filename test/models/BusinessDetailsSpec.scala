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
import play.api.libs.json.{JsSuccess, Json}

import java.time.LocalDate

class BusinessDetailsSpec extends AnyWordSpec with Matchers {

  "BusinessDetails.format" should {

    "serialise BusinessDetails to JSON" in {

      val model = BusinessDetails(
        mgdRegNumber          = "123456789",
        businessType          = Some(ApiBusinessType.Partnership),
        currentlyRegistered   = 1,
        groupReg              = true,
        dateOfRegistration    = Some(LocalDate.of(2024, 1, 15)),
        businessPartnerNumber = Some("X1234567890"),
        systemDate            = LocalDate.of(2026, 5, 7)
      )

      val expectedJson = Json.parse(
        """
          |{
          |  "mgdRegNumber": "123456789",
          |  "businessType": 4,
          |  "currentlyRegistered": 1,
          |  "groupReg": true,
          |  "dateOfRegistration": "2024-01-15",
          |  "businessPartnerNumber": "X1234567890",
          |  "systemDate": "2026-05-07"
          |}
          |""".stripMargin
      )

      Json.toJson(model) shouldBe expectedJson
    }

    "deserialise JSON to BusinessDetails" in {

      val json = Json.parse(
        """
          |{
          |  "mgdRegNumber": "123456789",
          |  "businessType": 4,
          |  "currentlyRegistered": 1,
          |  "groupReg": true,
          |  "dateOfRegistration": "2024-01-15",
          |  "businessPartnerNumber": "X1234567890",
          |  "systemDate": "2026-05-07"
          |}
          |""".stripMargin
      )

      val expectedModel = BusinessDetails(
        mgdRegNumber          = "123456789",
        businessType          = Some(ApiBusinessType.Partnership),
        currentlyRegistered   = 1,
        groupReg              = true,
        dateOfRegistration    = Some(LocalDate.of(2024, 1, 15)),
        businessPartnerNumber = Some("X1234567890"),
        systemDate            = LocalDate.of(2026, 5, 7)
      )

      json.validate[BusinessDetails] shouldBe JsSuccess(expectedModel)
    }

    "handle optional fields when absent" in {

      val model = BusinessDetails(
        mgdRegNumber          = "123456789",
        businessType          = None,
        currentlyRegistered   = 0,
        groupReg              = false,
        dateOfRegistration    = None,
        businessPartnerNumber = None,
        systemDate            = LocalDate.of(2026, 5, 7)
      )

      val json = Json.toJson(model)

      json.validate[BusinessDetails] shouldBe JsSuccess(model)
    }
  }
}
