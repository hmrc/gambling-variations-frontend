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

import models.BusinessType
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{JsPath, Json}

class PartnerDetailsBusinessTypePageSpec extends PlaySpec {

  val Index = 0

  "PartnerDetailsBusinessTypePage" must {

    "have the correct path" in {
      PartnerDetailsBusinessTypePage(Index).path mustEqual (JsPath \ "partners" \ Index \ "partnerDetailsBusinessType")
    }

    "have the correct toString value" in {
      PartnerDetailsBusinessTypePage(Index).toString mustEqual "partnerDetailsBusinessType"
    }

    "be able to read and write PartnerDetailsBusinessNamePage values with correct index" in {

      val value1 = BusinessType.Soleproprietor
      val value2 = BusinessType.Unincorporatedbody

      val json = Json.obj(
        "partners" -> Json.arr(
          Json.obj(
            PartnerDetailsBusinessTypePage(Index).toString -> Json.toJson(value1.code)
          ),
          Json.obj(
            PartnerDetailsBusinessTypePage(Index + 1).toString -> Json.toJson(value2.code)
          )
        )
      )

      PartnerDetailsBusinessTypePage(Index).path
        .asSingleJson(json)
        .validate[BusinessType]
        .get mustEqual value1

      PartnerDetailsBusinessTypePage(Index + 1).path
        .asSingleJson(json)
        .validate[BusinessType]
        .get mustEqual value2
    }

  }
}
