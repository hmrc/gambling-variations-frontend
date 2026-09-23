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
import play.api.libs.json.JsPath

class PartnerDetailsBusinessPartnerNumberPageSpec extends PlaySpec with PartnerDetailsHelper {

  "PartnerDetailsBusinessPartnerNumberPage" must {

    "partners" must {
      "have the correct path" in {
        PartnerDetailsBusinessPartnerNumberPage(businessNumber1).path mustEqual
          (JsPath \ "partners" \ businessNumber1 \ "partnerDetailsBusinessPartnerNumber")
      }

      "have the correct string representation" in {
        PartnerDetailsBusinessPartnerNumberPage(businessNumber1).toString mustEqual "partnerDetailsBusinessPartnerNumber"
      }
    }

    "newPartners" must {
      "have the correct path" in {
        PartnerDetailsBusinessPartnerNumberPage(newPartnersIndex1).path mustEqual
          (JsPath \ "newPartners" \ newPartnersIndex1 \ "partnerDetailsBusinessPartnerNumber")
      }

      "have the correct string representation" in {
        PartnerDetailsBusinessPartnerNumberPage(newPartnersIndex1).toString mustEqual "partnerDetailsBusinessPartnerNumber"
      }
    }
  }
}
