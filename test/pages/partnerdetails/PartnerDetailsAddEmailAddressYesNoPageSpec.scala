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

class PartnerDetailsAddEmailAddressYesNoPageSpec extends SpecBase with PartnerDetailsHelper {

  "PartnerDetailsAddEmailAddressYesNoPage" - {

    "partners" - {
      "have the correct path" in {
        PartnerDetailsAddEmailAddressYesNoPage(businessNumber1).path mustEqual
          (JsPath \ "partners" \ businessNumber1 \ "partnerDetailsAddEmailAddressYesNo")
      }

      "have the correct toString value" in {
        PartnerDetailsAddEmailAddressYesNoPage(businessNumber1).toString mustEqual "partnerDetailsAddEmailAddressYesNo"
      }

      "be able to read and write values with correct index" in {
        val json = Json.obj(
          "partners" -> Json.obj(
            businessNumber1 -> Json.obj(PartnerDetailsAddEmailAddressYesNoPage(businessNumber1).toString -> Json.toJson(true)),
            businessNumber2 -> Json.obj(PartnerDetailsAddEmailAddressYesNoPage(businessNumber2).toString -> Json.toJson(false))
          )
        )

        PartnerDetailsAddEmailAddressYesNoPage(businessNumber1).path.asSingleJson(json).validate[Boolean].get mustEqual true
        PartnerDetailsAddEmailAddressYesNoPage(businessNumber2).path.asSingleJson(json).validate[Boolean].get mustEqual false
      }
    }

    "newPartners" - {
      "have the correct path" in {
        PartnerDetailsAddEmailAddressYesNoPage(newPartnersIndex1).path mustEqual
          (JsPath \ "newPartners" \ newPartnersIndex1 \ "partnerDetailsAddEmailAddressYesNo")
      }

      "be able to read and write values with correct index" in {
        val json = Json.obj(
          "newPartners" -> Json.arr(
            Json.obj(PartnerDetailsAddEmailAddressYesNoPage(newPartnersIndex1).toString -> Json.toJson(true)),
            Json.obj(PartnerDetailsAddEmailAddressYesNoPage(newPartnersIndex2).toString -> Json.toJson(false))
          )
        )

        PartnerDetailsAddEmailAddressYesNoPage(newPartnersIndex1).path.asSingleJson(json).validate[Boolean].get mustEqual true
        PartnerDetailsAddEmailAddressYesNoPage(newPartnersIndex2).path.asSingleJson(json).validate[Boolean].get mustEqual false
      }
    }
  }
}
