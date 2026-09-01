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

package controllers.partner

import base.SpecBase
import controllers.partner.PartnerUtils.*
import models.UserAnswers
import play.api.libs.json.Json

class PartnerUtilsSpec extends SpecBase {

  "PartnerUtils" - {

    "partnerIndexOffset" - {

      "must return 0 when the 'partners' key is missing from UserAnswers" in {
        emptyUserAnswers.partnerIndexOffset mustBe 0
      }

      "must return 0 when the 'partners' array is empty" in {
        val answers = UserAnswers("id", Json.obj("partners" -> Json.arr()))

        answers.partnerIndexOffset mustBe 0
      }

      "must return 0 when there is exactly 1 partner" in {
        val answers = UserAnswers(
          "id",
          Json.obj(
            "partners" -> Json.arr(
              Json.obj("name" -> "Partner 1")
            )
          )
        )

        answers.partnerIndexOffset mustBe 0
      }

      "must return size - 1 when there are multiple partners" in {
        val answers = UserAnswers(
          "id",
          Json.obj(
            "partners" -> Json.arr(
              Json.obj("name" -> "Partner 1"),
              Json.obj("name" -> "Partner 2"),
              Json.obj("name" -> "Partner 3")
            )
          )
        )

        answers.partnerIndexOffset mustBe 2
      }

      "must return 0 when 'partners' is not a valid JSON array" in {
        val answers = UserAnswers("id", Json.obj("partners" -> "invalid"))

        answers.partnerIndexOffset mustBe 0
      }
    }

    "partnerAt" - {

      "must return None when the 'partners' array is empty or missing" in {
        emptyUserAnswers.partnerAt(0) mustBe None
      }

      "must return Some(JsObject) when a valid index is provided" in {
        val partner1 = Json.obj("name" -> "Partner 1")
        val partner2 = Json.obj("name" -> "Partner 2")

        val answers = UserAnswers(
          "id",
          Json.obj("partners" -> Json.arr(partner1, partner2))
        )

        answers.partnerAt(0).value mustBe partner1
        answers.partnerAt(1).value mustBe partner2
      }

      "must return None when requesting an index out of bounds" in {
        val answers = UserAnswers(
          "id",
          Json.obj(
            "partners" -> Json.arr(
              Json.obj("name" -> "Partner 1")
            )
          )
        )

        answers.partnerAt(1) mustBe None
        answers.partnerAt(-1) mustBe None
      }

      "must filter out non-JsObject elements in the array and lift by valid object index" in {
        val partnerObj = Json.obj("name" -> "Partner 1")

        val answers = UserAnswers(
          "id",
          Json.obj(
            "partners" -> Json.arr(
              "invalid_string_element",
              partnerObj
            )
          )
        )

        answers.partnerAt(0).value mustBe partnerObj
        answers.partnerAt(1) mustBe None
      }
    }
  }
}
