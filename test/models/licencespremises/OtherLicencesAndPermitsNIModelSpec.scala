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

class OtherLicencesAndPermitsNIModelSpec extends SpecBase {
  "OtherLicencesAndPermitsNIModel" - {
    "getSelectedLicencesAndPermits" - {
      "should map page data to a set" in {

        val ua = UserAnswers(
          "id",
          Json.obj(
            "licencesPremisesSection" -> Json.obj(
              "mgdRegNum"    -> "XGM000001761",
              "amusement"    -> "1",
              "bingo"        -> "0",
              "bookmaking"   -> "0",
              "serveAlcohol" -> "1",
              "regCert"      -> "0"
            )
          )
        )

        OtherLicencesAndPermitsNI.getSelectedLicencesAndPermits(ua) mustEqual Set(
          OtherLicencesAndPermitsNI.amusement,
          OtherLicencesAndPermitsNI.serveAlcohol
        )
      }

      "should return noOtherLicencesAndPremises if all are 0" in {

        val ua = UserAnswers(
          "id",
          Json.obj(
            "licencesPremisesSection" -> Json.obj(
              "mgdRegNum"    -> "XGM000001761",
              "amusement"    -> "0",
              "bingo"        -> "0",
              "bookmaking"   -> "0",
              "serveAlcohol" -> "0",
              "regCert"      -> "0"
            )
          )
        )

        OtherLicencesAndPermitsNI.getSelectedLicencesAndPermits(ua) mustEqual Set(
          OtherLicencesAndPermitsNI.noOtherLicencesAndPermits
        )
      }
    }
  }
}
