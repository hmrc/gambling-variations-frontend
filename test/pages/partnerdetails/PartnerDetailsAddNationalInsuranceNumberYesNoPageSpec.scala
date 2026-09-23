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
import play.api.libs.json.JsPath

class PartnerDetailsAddNationalInsuranceNumberYesNoPageSpec extends SpecBase with PartnerDetailsHelper {

  "PartnerDetailsAddNationalInsuranceNumberYesNoPage" - {

    "partners" - {
      "must have the correct path" in {
        PartnerDetailsAddNationalInsuranceNumberYesNoPage(
          businessNumber1
        ).path mustEqual (JsPath \ "partners" \ businessNumber1 \ "partnerDetailsAddNationalInsuranceNumberYesNo")
      }

      "must have the correct string representation" in {
        PartnerDetailsAddNationalInsuranceNumberYesNoPage(businessNumber1).toString mustEqual "partnerDetailsAddNationalInsuranceNumberYesNo"
      }
    }

    "newPartners" - {
      "must have the correct path" in {
        PartnerDetailsAddNationalInsuranceNumberYesNoPage(
          newPartnersIndex1
        ).path mustEqual (JsPath \ "newPartners" \ newPartnersIndex1 \ "partnerDetailsAddNationalInsuranceNumberYesNo")
      }

      "must have the correct string representation" in {
        PartnerDetailsAddNationalInsuranceNumberYesNoPage(newPartnersIndex1).toString mustEqual "partnerDetailsAddNationalInsuranceNumberYesNo"
      }
    }

  }
}
