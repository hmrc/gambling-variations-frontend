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

class PartnerDetailsVrnPageSpec extends PlaySpec with PartnerDetailsHelper {

  "partners" must {
    "PartnerDetailsVrnPage" must {

      "have the correct path" in {
        PartnerDetailsVrnPage(businessNumber1).path mustEqual (JsPath \ "partners" \ businessNumber1 \ "partnerDetailsVrn")
      }

      "have the correct toString value" in {

        PartnerDetailsVrnPage(businessNumber1).toString mustEqual "partnerDetailsVrn"
      }

      "be able to read and write PartnerDetailsVrnPage values with correct index" in {

        val value1 = "Value1"
        val value2 = "Value2"

        val json = Json.obj(
          "partners" -> Json.obj(
            businessNumber1 -> Json.obj(
              PartnerDetailsVrnPage(businessNumber1).toString -> Json.toJson(value1)
            ),
            businessNumber2 -> Json.obj(
              PartnerDetailsVrnPage(businessNumber2).toString -> Json.toJson(value2)
            )
          )
        )

        PartnerDetailsVrnPage(businessNumber1).path
          .asSingleJson(json)
          .validate[String]
          .get mustEqual value1

        PartnerDetailsVrnPage(businessNumber2).path
          .asSingleJson(json)
          .validate[String]
          .get mustEqual value2
      }

    }
  }

  "newPartners" must {
    "PartnerDetailsVrnPage" must {

      "have the correct path" in {
        PartnerDetailsVrnPage(newPartnersIndex1).path mustEqual (JsPath \ "newPartners" \ newPartnersIndex1 \ "partnerDetailsVrn")
      }

      "have the correct toString value" in {

        PartnerDetailsVrnPage(newPartnersIndex1).toString mustEqual "partnerDetailsVrn"
      }

      "be able to read and write PartnerDetailsVrnPage values with correct index" in {

        val value1 = "Value1"
        val value2 = "Value2"

        val json = Json.obj(
          "newPartners" -> Json.arr(
            Json.obj(
              PartnerDetailsVrnPage(newPartnersIndex1).toString -> Json.toJson(value1)
            ),
            Json.obj(
              PartnerDetailsVrnPage(newPartnersIndex2).toString -> Json.toJson(value2)
            )
          )
        )

        PartnerDetailsVrnPage(newPartnersIndex1).path
          .asSingleJson(json)
          .validate[String]
          .get mustEqual value1

        PartnerDetailsVrnPage(newPartnersIndex2).path
          .asSingleJson(json)
          .validate[String]
          .get mustEqual value2
      }

    }
  }

}
