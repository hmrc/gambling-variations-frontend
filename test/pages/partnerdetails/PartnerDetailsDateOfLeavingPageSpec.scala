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

class PartnerDetailsDateOfLeavingPageSpec extends PlaySpec with PartnerDetailsHelper {

  "partners" must {
    "PartnerDetailsDateOfLeavingPage" must {

      "have the correct path" in {
        PartnerDetailsDateOfLeavingPage(businessNumber1).path mustEqual (JsPath \ "partners" \ businessNumber1 \ "partnerDetailsDateOfLeaving")
      }

      "have the correct toString value" in {

        PartnerDetailsDateOfLeavingPage(businessNumber1).toString mustEqual "partnerDetailsDateOfLeaving"
      }

      "be able to read and write PartnerDetailsDateOfLeavingPage values with correct index" in {

        val value1 = LocalDate.of(2026, 1, 1)
        val value2 = LocalDate.of(2025, 1, 1)

        val json = Json.obj(
          "partners" -> Json.obj(
            businessNumber1 -> Json.obj(
              PartnerDetailsDateOfLeavingPage(businessNumber1).toString -> Json.toJson(value1)
            ),
            businessNumber2 -> Json.obj(
              PartnerDetailsDateOfLeavingPage(businessNumber2).toString -> Json.toJson(value2)
            )
          )
        )

        PartnerDetailsDateOfLeavingPage(businessNumber1).path
          .asSingleJson(json)
          .validate[LocalDate]
          .get mustEqual value1

        PartnerDetailsDateOfLeavingPage(businessNumber2).path
          .asSingleJson(json)
          .validate[LocalDate]
          .get mustEqual value2
      }

    }
  }

  "newPartners" must {
    "PartnerDetailsDateOfLeavingPage" must {

      "have the correct path" in {
        PartnerDetailsDateOfLeavingPage(newPartnersIndex1).path mustEqual (JsPath \ "newPartners" \ newPartnersIndex1 \ "partnerDetailsDateOfLeaving")
      }

      "have the correct toString value" in {

        PartnerDetailsDateOfLeavingPage(newPartnersIndex1).toString mustEqual "partnerDetailsDateOfLeaving"
      }

      "be able to read and write PartnerDetailsDateOfLeavingPage values with correct index" in {

        val value1 = LocalDate.of(2026, 1, 1)
        val value2 = LocalDate.of(2025, 1, 1)

        val json = Json.obj(
          "newPartners" -> Json.arr(
            Json.obj(
              PartnerDetailsDateOfLeavingPage(newPartnersIndex1).toString -> Json.toJson(value1)
            ),
            Json.obj(
              PartnerDetailsDateOfLeavingPage(newPartnersIndex2).toString -> Json.toJson(value2)
            )
          )
        )

        PartnerDetailsDateOfLeavingPage(newPartnersIndex1).path
          .asSingleJson(json)
          .validate[LocalDate]
          .get mustEqual value1

        PartnerDetailsDateOfLeavingPage(newPartnersIndex2).path
          .asSingleJson(json)
          .validate[LocalDate]
          .get mustEqual value2
      }

    }
  }

}
