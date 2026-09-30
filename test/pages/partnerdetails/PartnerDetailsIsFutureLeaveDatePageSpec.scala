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

class PartnerDetailsIsFutureLeaveDatePageSpec extends PlaySpec with PartnerDetailsHelper {

  "partners" must {
    "PartnerDetailsIsFutureLeaveDatePage" must {

      "have the correct path" in {
        PartnerDetailsIsFutureLeaveDatePage(
          businessNumber1
        ).path mustEqual (JsPath \ "partners" \ businessNumber1 \ "partnerDetailsIsFutureLeaveDate")
      }

      "have the correct toString value" in {

        PartnerDetailsIsFutureLeaveDatePage(businessNumber1).toString mustEqual "partnerDetailsIsFutureLeaveDate"
      }

      "be able to read and write PartnerDetailsIsFutureLeaveDatePage values with correct index" in {

        val value1 = 1
        val value2 = 2

        val json = Json.obj(
          "partners" -> Json.obj(
            businessNumber1 -> Json.obj(
              PartnerDetailsIsFutureLeaveDatePage(businessNumber1).toString -> Json.toJson(value1)
            ),
            businessNumber2 -> Json.obj(
              PartnerDetailsIsFutureLeaveDatePage(businessNumber2).toString -> Json.toJson(value2)
            )
          )
        )

        PartnerDetailsIsFutureLeaveDatePage(businessNumber1).path
          .asSingleJson(json)
          .validate[Int]
          .get mustEqual value1

        PartnerDetailsIsFutureLeaveDatePage(businessNumber2).path
          .asSingleJson(json)
          .validate[Int]
          .get mustEqual value2
      }

    }
  }

  "newPartners" must {
    "PartnerDetailsIsFutureLeaveDatePage" must {

      "have the correct path" in {
        PartnerDetailsIsFutureLeaveDatePage(
          newPartnersIndex1
        ).path mustEqual (JsPath \ "newPartners" \ newPartnersIndex1 \ "partnerDetailsIsFutureLeaveDate")
      }

      "have the correct toString value" in {
        PartnerDetailsIsFutureLeaveDatePage(newPartnersIndex1).toString mustEqual "partnerDetailsIsFutureLeaveDate"
      }

      "be able to read and write PartnerDetailsIsFutureLeaveDatePage values with correct index" in {

        val value1 = 1
        val value2 = 2

        val json = Json.obj(
          "newPartners" -> Json.arr(
            Json.obj(
              PartnerDetailsIsFutureLeaveDatePage(newPartnersIndex1).toString -> Json.toJson(value1)
            ),
            Json.obj(
              PartnerDetailsIsFutureLeaveDatePage(newPartnersIndex2).toString -> Json.toJson(value2)
            )
          )
        )

        PartnerDetailsIsFutureLeaveDatePage(newPartnersIndex1).path
          .asSingleJson(json)
          .validate[Int]
          .get mustEqual value1

        PartnerDetailsIsFutureLeaveDatePage(newPartnersIndex2).path
          .asSingleJson(json)
          .validate[Int]
          .get mustEqual value2
      }

    }
  }

}
