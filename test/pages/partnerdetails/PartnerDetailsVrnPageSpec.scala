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


class PartnerDetailsVrnPageSpec extends PlaySpec {

  val Index = 0

  "PartnerDetailsVrnPage" must {

    "have the correct path" in {
      PartnerDetailsVrnPage(Index).path mustEqual (JsPath \ "partners" \ Index \ "partnerDetailsVrn")
    }

    "have the correct toString value" in {

      PartnerDetailsVrnPage(Index).toString mustEqual "partnerDetailsVrn"
    }

    "be able to read and write PartnerDetailsVrnPage values with correct index" in {

      val value1 = "Value1"
      val value2 = "Value2"

      val json = Json.obj(
        "partners" -> Json.arr(
          Json.obj(
            PartnerDetailsVrnPage(Index).toString -> Json.toJson(value1)
          ),
          Json.obj(
            PartnerDetailsVrnPage(Index + 1).toString -> Json.toJson(value2)
          )
        )
      )

      PartnerDetailsVrnPage(Index).path
        .asSingleJson(json)
        .validate[String]
        .get mustEqual value1

      PartnerDetailsVrnPage(Index + 1).path
        .asSingleJson(json)
        .validate[String]
        .get mustEqual value2
    }

  }
}