/*
 * Copyright 2025 HM Revenue & Customs
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

import models.BusinessType.Corporatebody
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsValue, Json}

import java.time.LocalDate

class BusinessNameDetailsSpec extends AnyWordSpec with Matchers:

  val jsonAsString: String =
    s"""{
       |  "mgdRegNumber": "ABC12345678901",
       |  "businessName": "Test Business Ltd",
       |  "businessType": 2,
       |  "tradingName": "Trading Name",
       |  "systemDate": "${LocalDate.of(1991, 1, 1)}"
       |}""".stripMargin

  val model: BusinessNameDetails = BusinessNameDetails(
    mgdRegNum    = "ABC12345678901",
    businessName = "Test Business Ltd",
    businessType = Corporatebody,
    tradingName  = Some("Trading Name"),
    systemDate   = Some(LocalDate.of(1991, 1, 1))
  )
  val json: JsValue = Json.parse(jsonAsString)

  "BusinessName" should:
    "read JSON correctly" in:
      Json.fromJson[BusinessNameDetails](json).get shouldBe model
