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

import controllers.partnerdetails.PartnerDetailsHelper
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{JsPath, Json}

import java.time.LocalDate

class PartnerDetailsDateOfIncorporationSpec extends PlaySpec with PartnerDetailsHelper {

  "partners" must {
    "PartnerDetailsDateOfIncorporation" must {

      "have the correct path" in {
        PartnerDetailsDateOfIncorporation(
          businessNumber1
        ).path mustEqual (JsPath \ "partners" \ businessNumber1 \ "partnerDetailsDateOfIncorporation")
      }

      "have the correct toString value" in {

        PartnerDetailsDateOfIncorporation(businessNumber1).toString mustEqual "partnerDetailsDateOfIncorporation"
      }

      "be able to read and write PartnerDetailsDateOfIncorporation values with correct index" in {

        val value1 = LocalDate.of(2026, 1, 1)
        val value2 = LocalDate.of(2025, 1, 1)

        val json = Json.obj(
          "partners" -> Json.obj(
            businessNumber1 -> Json.obj(
              PartnerDetailsDateOfIncorporation(businessNumber1).toString -> Json.toJson(value1)
            ),
            businessNumber2 -> Json.obj(
              PartnerDetailsDateOfIncorporation(businessNumber2).toString -> Json.toJson(value2)
            )
          )
        )

        PartnerDetailsDateOfIncorporation(businessNumber1).path
          .asSingleJson(json)
          .validate[LocalDate]
          .get mustEqual value1

        PartnerDetailsDateOfIncorporation(businessNumber2).path
          .asSingleJson(json)
          .validate[LocalDate]
          .get mustEqual value2
      }

    }
  }

  "newPartners" must {
    "PartnerDetailsDateOfIncorporation" must {

      "have the correct path" in {
        PartnerDetailsDateOfIncorporation(
          newPartnersIndex1
        ).path mustEqual (JsPath \ "newPartners" \ newPartnersIndex1 \ "partnerDetailsDateOfIncorporation")
      }

      "have the correct toString value" in {

        PartnerDetailsDateOfIncorporation(newPartnersIndex1).toString mustEqual "partnerDetailsDateOfIncorporation"
      }

      "be able to read and write PartnerDetailsDateOfIncorporation values with correct index" in {

        val value1 = LocalDate.of(2026, 1, 1)
        val value2 = LocalDate.of(2025, 1, 1)

        val json = Json.obj(
          "newPartners" -> Json.arr(
            Json.obj(
              PartnerDetailsDateOfIncorporation(newPartnersIndex1).toString -> Json.toJson(value1)
            ),
            Json.obj(
              PartnerDetailsDateOfIncorporation(newPartnersIndex2).toString -> Json.toJson(value2)
            )
          )
        )

        PartnerDetailsDateOfIncorporation(newPartnersIndex1).path
          .asSingleJson(json)
          .validate[LocalDate]
          .get mustEqual value1

        PartnerDetailsDateOfIncorporation(newPartnersIndex2).path
          .asSingleJson(json)
          .validate[LocalDate]
          .get mustEqual value2
      }

    }
  }

}
