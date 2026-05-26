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

package pages

import models.BusinessContactNumber
import play.api.libs.json.JsPath
import play.api.libs.json.Json
import org.scalatestplus.play.PlaySpec

class BusinessContactNumberPageSpec extends PlaySpec {

  "BusinessContactNumberPage" must {

    "have the correct path" in {

      BusinessContactNumberPage.path mustEqual (JsPath \ "businessContactNumber")
    }

    "have the correct toString value" in {

      BusinessContactNumberPage.toString mustEqual "businessContactNumber"
    }

    "be able to read and write BusinessContactNumber values" in {

      val value = BusinessContactNumber(
        phoneNumber  = "01632 960 001",
        mobileNumber = "07700 900000"
      )

      val json = Json.obj(
        BusinessContactNumberPage.toString -> Json.toJson(value)
      )

      BusinessContactNumberPage.path
        .asSingleJson(json)
        .validate[BusinessContactNumber]
        .get mustEqual value
    }
  }
}
