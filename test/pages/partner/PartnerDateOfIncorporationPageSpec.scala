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

package pages.partner

import base.SpecBase
import play.api.libs.json.JsPath

class PartnerDateOfIncorporationPageSpec extends SpecBase {

  "PartnerDateOfIncorporationPage" - {

    "must have the correct path for the first partner" in {
      PartnerDateOfIncorporationPage(0).path mustEqual
        (JsPath \ "partners" \ 0 \ "partnerDateOfIncorporation")
    }

    "must have the correct path for the second partner" in {
      PartnerDateOfIncorporationPage(1).path mustEqual
        (JsPath \ "partners" \ 1 \ "partnerDateOfIncorporation")
    }

    "must have the correct string representation" in {
      PartnerDateOfIncorporationPage(0).toString mustEqual
        "partnerDateOfIncorporation"
    }
  }
}
