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

import java.time.LocalDate

class MgdTradeDetailsSpec extends AnyWordSpec with Matchers {

  "MgdTradeDetails" should {

    "read and write JSON correctly" in {

      val model =
        MgdTradeDetails(
          mgdRegNumber                     = "MGD123456",
          isBusinessSeasonal               = Some(true),
          businessTradeClass               = Some(BusinessTradeClass.Casino),
          businessActivityDesc             = Some("Casino activities"),
          previousMgdRegistrationNumbers   = Some(Seq("MGD111", "MGD222")),
          associatedMgdRegistrationNumbers = Some(Seq("ASS111", "ASS222")),
          systemDate                       = Some(LocalDate.of(2026, 1, 1))
        )

      val json = Json.toJson(model)

      json.as[MgdTradeDetails] shouldBe model
    }
  }

  "MgdTradeDetails.intToBoolean Reads" should {

    "read 1 as true" in {

      JsNumber(1).validate[Boolean](MgdTradeDetails.intToBoolean) shouldBe
        JsSuccess(true)
    }

    "read 0 as false" in {

      JsNumber(0).validate[Boolean](MgdTradeDetails.intToBoolean) shouldBe
        JsSuccess(false)
    }

    "fail for invalid numeric values" in {

      val result =
        JsNumber(2).validate[Boolean](MgdTradeDetails.intToBoolean)

      result.isError shouldBe true
    }

    "fail for non numeric values" in {

      JsString("true")
        .validate[Boolean](MgdTradeDetails.intToBoolean)
        .isError shouldBe true

      JsBoolean(true)
        .validate[Boolean](MgdTradeDetails.intToBoolean)
        .isError shouldBe true
    }
  }

  "MgdTradeDetails.intToBoolean Writes" should {

    "write true as 1" in {

      Json.toJson(true)(MgdTradeDetails.intToBoolean) shouldBe
        JsNumber(1)
    }

    "write false as 0" in {

      Json.toJson(false)(MgdTradeDetails.intToBoolean) shouldBe
        JsNumber(0)
    }
  }

  "MgdTradeDetails format" should {

    "round trip successfully" in {

      val model =
        MgdTradeDetails(
          mgdRegNumber                     = "MGD123456",
          isBusinessSeasonal               = Some(false),
          businessTradeClass               = Some(BusinessTradeClass.Other),
          businessActivityDesc             = Some("Arcade"),
          previousMgdRegistrationNumbers   = Some(Seq("MGD123")),
          associatedMgdRegistrationNumbers = Some(Seq("ASS123")),
          systemDate                       = Some(LocalDate.of(2026, 1, 1))
        )

      val json =
        Json.toJson(model)

      json.validate[MgdTradeDetails] shouldBe
        JsSuccess(model)
    }
  }
}
