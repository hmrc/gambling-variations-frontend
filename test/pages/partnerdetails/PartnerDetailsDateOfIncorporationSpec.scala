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

package pages.partnerdetails

import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{JsPath, Json}

import java.time.LocalDate

class PartnerDetailsDateOfIncorporationSpec extends PlaySpec {

  val Index = 0

  "PartnerDetailsDateOfIncorporation" must {

    "have the correct path" in {
      PartnerDetailsDateOfIncorporation(Index).path mustEqual (JsPath \ "partners" \ Index \ "partnerDetailsDateOfIncorporation")
    }

    "have the correct toString value" in {

      PartnerDetailsDateOfIncorporation(Index).toString mustEqual "partnerDetailsDateOfIncorporation"
    }

    "be able to read and write PartnerDetailsDateOfIncorporation values with correct index" in {

      val value1 = LocalDate.of(2026, 1, 1)
      val value2 = LocalDate.of(2025, 1, 1)

      val json = Json.obj(
        "partners" -> Json.arr(
          Json.obj(
            PartnerDetailsDateOfIncorporation(Index).toString -> Json.toJson(value1)
          ),
          Json.obj(
            PartnerDetailsDateOfIncorporation(Index + 1).toString -> Json.toJson(value2)
          )
        )
      )

      PartnerDetailsDateOfIncorporation(Index).path
        .asSingleJson(json)
        .validate[LocalDate]
        .get mustEqual value1

      PartnerDetailsDateOfIncorporation(Index + 1).path
        .asSingleJson(json)
        .validate[LocalDate]
        .get mustEqual value2
    }

  }
}
