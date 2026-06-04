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

class BusinessContactDetailsSpec extends AnyWordSpec with Matchers {
  "BusinessContactDetails.format" should {

    "serialise BusinessContactDetails to JSON" in {

      val model = BusinessContactDetails(
        mgdRegNumber      = "123456789",
        phoneNumber       = Some("0700000000"),
        mobilePhoneNumber = Some("0700000001"),
        faxNumber         = Some("0700000002"),
        emailAddr         = Some("a@b.com"),
        systemDate        = Some(LocalDate.of(2026, 5, 27))
      )

      val expectedJson = Json.parse(
        """
          |{
          |  "mgdRegNumber": "123456789",
          |  "phoneNumber": "0700000000",
          |  "mobilePhoneNumber": "0700000001",
          |  "faxNumber": "0700000002",
          |  "emailAddr": "a@b.com",
          |  "systemDate": "2026-05-27"
          |}
          |""".stripMargin
      )

      Json.toJson(model) shouldBe expectedJson
    }

    "deserialise JSON to BusinessContactDetails" in {

      val json = Json.parse(
        """
          |{
          |  "mgdRegNumber": "123456789",
          |  "phoneNumber": "0700000000",
          |  "mobilePhoneNumber": "0700000001",
          |  "faxNumber": "0700000002",
          |  "emailAddr": "a@b.com",
          |  "systemDate": "2026-05-27"
          |}
          |""".stripMargin
      )

      val expectedModel = BusinessContactDetails(
        mgdRegNumber      = "123456789",
        phoneNumber       = Some("0700000000"),
        mobilePhoneNumber = Some("0700000001"),
        faxNumber         = Some("0700000002"),
        emailAddr         = Some("a@b.com"),
        systemDate        = Some(LocalDate.of(2026, 5, 27))
      )

      json.validate[BusinessContactDetails] shouldBe JsSuccess(expectedModel)
    }

    "handle optional fields when absent" in {

      val model = BusinessContactDetails(
        mgdRegNumber      = "123456789",
        phoneNumber       = None,
        mobilePhoneNumber = None,
        faxNumber         = None,
        emailAddr         = None,
        systemDate        = None
      )

      val json = Json.toJson(model)

      json.validate[BusinessContactDetails] shouldBe JsSuccess(model)
    }
  }

}
