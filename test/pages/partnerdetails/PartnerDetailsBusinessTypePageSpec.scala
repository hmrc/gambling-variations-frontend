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
import models.BusinessType
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{JsPath, Json}

class PartnerDetailsBusinessTypePageSpec extends PlaySpec with PartnerDetailsHelper {
  "partners" must {
    "PartnerDetailsBusinessTypePage" must {

      "have the correct path" in {
        PartnerDetailsBusinessTypePage(businessNumber1).path mustEqual (JsPath \ "partners" \ businessNumber1 \ "partnerDetailsBusinessType")
      }

      "have the correct toString value" in {
        PartnerDetailsBusinessTypePage(businessNumber1).toString mustEqual "partnerDetailsBusinessType"
      }

      "be able to read and write PartnerDetailsBusinessTypePage values with correct index" in {

        val value1 = BusinessType.Soleproprietor
        val value2 = BusinessType.Unincorporatedbody

        val json = Json.obj(
          "partners" -> Json.obj(
            businessNumber1 -> Json.obj(
              PartnerDetailsBusinessTypePage(businessNumber1).toString -> Json.toJson(value1.code)
            ),
            businessNumber2 -> Json.obj(
              PartnerDetailsBusinessTypePage(businessNumber2).toString -> Json.toJson(value2.code)
            )
          )
        )

        PartnerDetailsBusinessTypePage(businessNumber1).path
          .asSingleJson(json)
          .validate[BusinessType]
          .get mustEqual value1

        PartnerDetailsBusinessTypePage(businessNumber2).path
          .asSingleJson(json)
          .validate[BusinessType]
          .get mustEqual value2
      }

    }
  }

  "newPartners" must {
    "PartnerDetailsBusinessTypePage" must {

      "have the correct path" in {
        PartnerDetailsBusinessTypePage(newPartnersIndex1).path mustEqual (JsPath \ "newPartners" \ newPartnersIndex1 \ "partnerDetailsBusinessType")
      }

      "have the correct toString value" in {
        PartnerDetailsBusinessTypePage(newPartnersIndex1).toString mustEqual "partnerDetailsBusinessType"
      }

      "be able to read and write PartnerDetailsBusinessTypePage values with correct index" in {

        val value1 = BusinessType.Soleproprietor
        val value2 = BusinessType.Unincorporatedbody

        val json = Json.obj(
          "newPartners" -> Json.arr(
            Json.obj(
              PartnerDetailsBusinessTypePage(newPartnersIndex1).toString -> Json.toJson(value1.code)
            ),
            Json.obj(
              PartnerDetailsBusinessTypePage(newPartnersIndex2).toString -> Json.toJson(value2.code)
            )
          )
        )

        PartnerDetailsBusinessTypePage(newPartnersIndex1).path
          .asSingleJson(json)
          .validate[BusinessType]
          .get mustEqual value1

        PartnerDetailsBusinessTypePage(newPartnersIndex2).path
          .asSingleJson(json)
          .validate[BusinessType]
          .get mustEqual value2
      }

    }
  }

}
