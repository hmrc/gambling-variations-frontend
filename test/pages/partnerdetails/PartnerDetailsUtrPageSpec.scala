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

class PartnerDetailsUtrPageSpec extends PlaySpec with PartnerDetailsHelper {

  "partners" must {
    "PartnerDetailsUtrPage" must {

      "have the correct path" in {
        PartnerDetailsUtrPage(businessNumber1).path mustEqual (JsPath \ "partners" \ businessNumber1 \ "partnerDetailsUtr")
      }

      "have the correct toString value" in {

        PartnerDetailsUtrPage(businessNumber1).toString mustEqual "partnerDetailsUtr"
      }

      "be able to read and write PartnerDetailsUtrPage values with correct index" in {

        val value1 = "Value1"
        val value2 = "Value2"

        val json = Json.obj(
          "partners" -> Json.obj(
            businessNumber1 -> Json.obj(
              PartnerDetailsUtrPage(businessNumber1).toString -> Json.toJson(value1)
            ),
            businessNumber2 -> Json.obj(
              PartnerDetailsUtrPage(businessNumber2).toString -> Json.toJson(value2)
            )
          )
        )

        PartnerDetailsUtrPage(businessNumber1).path
          .asSingleJson(json)
          .validate[String]
          .get mustEqual value1

        PartnerDetailsUtrPage(businessNumber2).path
          .asSingleJson(json)
          .validate[String]
          .get mustEqual value2
      }

    }
  }

  "newPartners" must {
    "PartnerDetailsUtrPage" must {

      "have the correct path" in {
        PartnerDetailsUtrPage(newPartnersIndex1).path mustEqual (JsPath \ "newPartners" \ newPartnersIndex1 \ "partnerDetailsUtr")
      }

      "have the correct toString value" in {

        PartnerDetailsUtrPage(newPartnersIndex1).toString mustEqual "partnerDetailsUtr"
      }

      "be able to read and write PartnerDetailsUtrPage values with correct index" in {

        val value1 = "Value1"
        val value2 = "Value2"

        val json = Json.obj(
          "newPartners" -> Json.arr(
            Json.obj(
              PartnerDetailsUtrPage(newPartnersIndex1).toString -> Json.toJson(value1)
            ),
            Json.obj(
              PartnerDetailsUtrPage(newPartnersIndex2).toString -> Json.toJson(value2)
            )
          )
        )

        PartnerDetailsUtrPage(newPartnersIndex1).path
          .asSingleJson(json)
          .validate[String]
          .get mustEqual value1

        PartnerDetailsUtrPage(newPartnersIndex2).path
          .asSingleJson(json)
          .validate[String]
          .get mustEqual value2
      }

    }
  }

}
