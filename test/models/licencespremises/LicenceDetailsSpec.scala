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

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsError, Json}

import java.time.LocalDate

class LicenceDetailsSpec extends AnyWordSpec with Matchers {

  private val licenceDetails = LicenceDetails(
    mgdRegNumber          = "MGD123456",
    haveGamblingLicenceNo = Some("Yes"),
    gamblingLicenceNo     = Some("123-456789-A-123456-789"),
    heldByLandlord        = Some("No"),
    localAuthority        = Some("Yes"),
    familyEntertainment   = Some("No"),
    clubGaming            = Some("Yes"),
    clubLicence           = Some("No"),
    prizeGaming           = Some("Yes"),
    onPremises            = Some("No"),
    clubPremises          = Some("Yes"),
    regCert               = Some("No"),
    bookmaking            = Some("Yes"),
    bingo                 = Some("No"),
    amusement             = Some("Yes"),
    serveAlcohol          = Some("No"),
    premisesNotCovered    = Some("Yes"),
    systemDate            = Some(LocalDate.now())
  )

  "LicenceDetails" should {

    "write to JSON and read back successfully" in {
      val json = Json.toJson(licenceDetails)

      json.as[LicenceDetails] shouldBe licenceDetails
    }

    "write and read successfully when optional fields are None" in {
      val licenceDetailsWithNone = licenceDetails.copy(
        haveGamblingLicenceNo = None,
        gamblingLicenceNo     = None,
        heldByLandlord        = None,
        localAuthority        = None,
        familyEntertainment   = None,
        clubGaming            = None,
        clubLicence           = None,
        prizeGaming           = None,
        onPremises            = None,
        clubPremises          = None,
        regCert               = None,
        bookmaking            = None,
        bingo                 = None,
        amusement             = None,
        serveAlcohol          = None,
        premisesNotCovered    = None,
        systemDate            = None
      )

      val json = Json.toJson(licenceDetailsWithNone)

      json.as[LicenceDetails] shouldBe licenceDetailsWithNone
    }

    "return a JsError when a required field is missing" in {
      val json = Json.toJson(licenceDetails).as[play.api.libs.json.JsObject] -
        "mgdRegNumber"

      Json.fromJson[LicenceDetails](json) shouldBe a[JsError]
    }

    "return a JsError when a required field has an invalid type" in {
      val json = Json.toJson(licenceDetails).as[play.api.libs.json.JsObject] ++
        Json.obj("mgdRegNumber" -> 123)

      Json.fromJson[LicenceDetails](json) shouldBe a[JsError]
    }

    "return a JsError when an optional field has an invalid type" in {
      val json = Json.toJson(licenceDetails).as[play.api.libs.json.JsObject] ++
        Json.obj("gamblingLicenceNo" -> 123)

      Json.fromJson[LicenceDetails](json) shouldBe a[JsError]
    }
  }
}
