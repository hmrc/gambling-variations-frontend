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

import base.SpecBase
import controllers.partnerdetails.PartnerDetailsHelper
import play.api.libs.json.{JsPath, Json}

class PartnerDetailsSubmittedPageSpec extends SpecBase with PartnerDetailsHelper {

  "PartnerDetailsSubmittedPage" - {

    "partners" - {
      "have the correct path" in {
        PartnerDetailsSubmittedPage(businessNumber1).path mustEqual
          (JsPath \ "partners" \ businessNumber1 \ "partnerDetailsSection" \ "submitted")
      }

      "have the correct toString value" in {
        PartnerDetailsSubmittedPage(businessNumber1).toString mustEqual "submitted"
      }

      "be able to read and write values with correct index" in {
        val json = Json.obj(
          "partners" -> Json.obj(
            businessNumber1 -> Json.obj(
              "partnerDetailsSection" -> Json.obj(PartnerDetailsSubmittedPage(businessNumber1).toString -> Json.toJson(true))
            )
          )
        )

        PartnerDetailsSubmittedPage(businessNumber1).path.asSingleJson(json).validate[Boolean].get mustEqual true
      }
    }

    "newPartners" - {
      "have the correct path" in {
        PartnerDetailsSubmittedPage(newPartnersIndex1).path mustEqual
          (JsPath \ "newPartners" \ newPartnersIndex1 \ "partnerDetailsSection" \ "submitted")
      }

      "be able to read and write values with correct index" in {
        val json = Json.obj(
          "newPartners" -> Json.arr(
            Json.obj("partnerDetailsSection" -> Json.obj(PartnerDetailsSubmittedPage(newPartnersIndex1).toString -> Json.toJson(true)))
          )
        )

        PartnerDetailsSubmittedPage(newPartnersIndex1).path.asSingleJson(json).validate[Boolean].get mustEqual true
      }
    }
  }
}
