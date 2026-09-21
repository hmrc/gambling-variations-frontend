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
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.JsPath

class PartnerDetailsVatRegistrationNumberYesNoPageSpec extends AnyFreeSpec with Matchers with PartnerDetailsHelper {

  "partners" - {
    "VatRegistrationNumberYesNoPage" - {

      "must have the correct toString" in {
        PartnerDetailsVatRegistrationNumberYesNoPage(businessNumber1).toString mustBe "vatRegistrationNumberYesNo"
      }

      "must have a path corresponding to its name and index" in {
        val expectedPath: JsPath = JsPath \ "partners" \ businessNumber1 \ "vatRegistrationNumberYesNo"

        PartnerDetailsVatRegistrationNumberYesNoPage(businessNumber1).path mustBe expectedPath
      }
    }
  }

  "newPartners" - {
    "VatRegistrationNumberYesNoPage" - {

      "must have the correct toString" in {
        PartnerDetailsVatRegistrationNumberYesNoPage(newPartnersIndex1).toString mustBe "vatRegistrationNumberYesNo"
      }

      "must have a path corresponding to its name and index" in {
        val expectedPath: JsPath = JsPath \ "newPartners" \ newPartnersIndex1 \ "vatRegistrationNumberYesNo"

        PartnerDetailsVatRegistrationNumberYesNoPage(newPartnersIndex1).path mustBe expectedPath
      }
    }
  }
}
