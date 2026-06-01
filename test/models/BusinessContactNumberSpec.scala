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

import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{JsSuccess, Json}

class BusinessContactNumberSpec extends PlaySpec {

  "BusinessContactNumber" must {

    "serialise to JSON" in {

      val model = BusinessContactNumber(
        phoneNumber  = Some("01632 960 001"),
        mobileNumber = Some("07700 900000")
      )

      val expectedJson = Json.obj(
        "phoneNumber"  -> "01632 960 001",
        "mobileNumber" -> "07700 900000"
      )

      Json.toJson(model) mustEqual expectedJson
    }

    "deserialise from JSON" in {

      val json = Json.obj(
        "phoneNumber"  -> "01632 960 001",
        "mobileNumber" -> "07700 900000"
      )

      val expectedModel = BusinessContactNumber(
        phoneNumber  = Some("01632 960 001"),
        mobileNumber = Some("07700 900000")
      )

      json.validate[BusinessContactNumber] mustEqual JsSuccess(expectedModel)
    }
  }
}
