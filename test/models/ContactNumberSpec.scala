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
import play.api.libs.json.{JsSuccess, Json}

class ContactNumberSpec extends AnyFreeSpec with Matchers {

  "ContactNumber" - {

    "when all fields are present" - {

      val model = ContactNumber(
        phoneNumber       = Some("02071234567"),
        mobilePhoneNumber = Some("07700123456")
      )

      val json = Json.obj(
        "phoneNumber"       -> "02071234567",
        "mobilePhoneNumber" -> "07700123456"
      )

      "must read from JSON correctly" in {
        json.validate[ContactNumber] mustBe JsSuccess(model)
      }

      "must write to JSON correctly" in {
        Json.toJson(model) mustBe json
      }
    }

    "when optional fields are empty or missing" - {

      val model = ContactNumber(
        phoneNumber       = None,
        mobilePhoneNumber = None
      )

      val json = Json.obj()

      "must read from JSON correctly when fields are absent" in {
        json.validate[ContactNumber] mustBe JsSuccess(model)
      }

      "must write to JSON correctly omitting empty options" in {
        Json.toJson(model) mustBe json
      }
    }
  }
}
