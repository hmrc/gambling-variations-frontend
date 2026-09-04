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

package models.licencespremises

import base.SpecBase
import models.UserAnswers
import play.api.libs.json.Json

class OtherLicencesAndPermitsGBModelSpec extends SpecBase {
  "OtherLicencesAndPermitsGBModel" - {
    "getSelectedLicencesAndPermits" - {
      "should map page data to a set" in {

        val ua = UserAnswers(
          "id",
          Json.obj(
            "licencesPremisesSection" -> Json.obj(
              "mgdRegNum"           -> "XGM000001761",
              "clubGaming"          -> "1",
              "clubMachine"         -> "0",
              "clubPremises"        -> "0",
              "familyEntertainment" -> "1",
              "localAuthority"      -> "0",
              "onPremises"          -> "1",
              "prizeGaming"         -> "0"
            )
          )
        )

        OtherLicencesAndPermitsGB.getSelectedLicencesAndPermits(ua) mustEqual Set(
          OtherLicencesAndPermitsGB.clubGaming,
          OtherLicencesAndPermitsGB.familyEntertainment,
          OtherLicencesAndPermitsGB.onPremises
        )

      }
    }
  }
}
