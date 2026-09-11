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

class PartnerDetailsForeignCorporateReferencePageSpec extends PlaySpec {

  private val Index = 0

  "PartnerDetailsForeignCorporateReferencePage" must {

    "have the correct path" in {
      PartnerDetailsForeignCorporateReferencePage(Index).path mustEqual (JsPath \ "partners" \ Index \ "partnerDetailsForeignCorporateReference")
    }

    "have the correct toString value" in {

      PartnerDetailsForeignCorporateReferencePage(Index).toString mustEqual "partnerDetailsForeignCorporateReference"
    }

    "be able to read and write PartnerDetailsForeignCorporateReferencePage values with correct index" in {

      val value1 = "ForeignCorpRef-1"
      val value2 = "ForeignCorpRef-2"

      val json = Json.obj(
        "partners" -> Json.arr(
          Json.obj(
            PartnerDetailsForeignCorporateReferencePage(Index).toString -> Json.toJson(value1)
          ),
          Json.obj(
            PartnerDetailsForeignCorporateReferencePage(Index + 1).toString -> Json.toJson(value2)
          )
        )
      )

      PartnerDetailsForeignCorporateReferencePage(Index).path
        .asSingleJson(json)
        .validate[String]
        .get mustEqual value1

      PartnerDetailsForeignCorporateReferencePage(Index + 1).path
        .asSingleJson(json)
        .validate[String]
        .get mustEqual value2
    }

  }
}
