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

class PartnerDetailsRemoveEmailAddressYesNoPageSpec extends SpecBase with PartnerDetailsHelper {

  "partners" - {
    "PartnerDetailsRemoveEmailAddressYesNoPage" - {

      "must have the correct path" in {
        PartnerDetailsRemoveEmailAddressYesNoPage(
          businessNumber1
        ).path mustEqual (JsPath \ "partners" \ businessNumber1 \ "partnerDetailsRemoveEmailAddressYesNo")
      }

      "must have the correct string representation" in {
        PartnerDetailsRemoveEmailAddressYesNoPage(businessNumber1).toString mustEqual "partnerDetailsRemoveEmailAddressYesNo"
      }
    }
  }

  "newPartners" - {
    "PartnerDetailsRemoveEmailAddressYesNoPage" - {

      "must have the correct path" in {
        PartnerDetailsRemoveEmailAddressYesNoPage(
          newPartnersIndex1
        ).path mustEqual (JsPath \ "newPartners" \ newPartnersIndex1 \ "partnerDetailsRemoveEmailAddressYesNo")
      }

      "must have the correct string representation" in {
        PartnerDetailsRemoveEmailAddressYesNoPage(newPartnersIndex1).toString mustEqual "partnerDetailsRemoveEmailAddressYesNo"
      }
    }
  }

}
