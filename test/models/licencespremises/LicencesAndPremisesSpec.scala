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

class LicencesAndPremisesSpec extends AnyWordSpec with Matchers {

  private val premisesDetails = PremisesDetails(
    mgdRegNumber = "MGD123456",
    address1     = Some("123 High Street"),
    address2     = Some("Town Centre"),
    address3     = Some("London"),
    address4     = None,
    postcode     = Some("AB1 2CD"),
    systemDate   = Some(LocalDate.of(2026, 8, 26))
  )

  private val licencesAndPremises = LicencesAndPremises(
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
    premisesDetails = Some(
      PremisesDetailsResponse(
        totalRows = Some(1),
        premises  = Seq(premisesDetails)
      )
    )
  )

  "LicencesAndPremises" should {

    "round trip successfully through JSON" in {
      val json = Json.toJson(licencesAndPremises)

      json.as[LicencesAndPremises] shouldBe licencesAndPremises
    }

    "round trip successfully through JSON when optional fields are None" in {
      val licencesAndPremisesWithNone = LicencesAndPremises(
        mgdRegNumber          = "MGD123456",
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
        premisesDetails       = None
      )

      val json = Json.toJson(licencesAndPremisesWithNone)

      json.as[LicencesAndPremises] shouldBe licencesAndPremisesWithNone
    }

    "return a JsError when the required mgdRegNumber is missing" in {
      val json = Json.toJson(licencesAndPremises).as[play.api.libs.json.JsObject] -
        "mgdRegNumber"

      Json.fromJson[LicencesAndPremises](json) shouldBe a[JsError]
    }

    "return a JsError when the required mgdRegNumber has an invalid type" in {
      val json = Json.toJson(licencesAndPremises).as[play.api.libs.json.JsObject] ++
        Json.obj("mgdRegNumber" -> 123)

      Json.fromJson[LicencesAndPremises](json) shouldBe a[JsError]
    }

    "return a JsError when premisesDetails has an invalid type" in {
      val json = Json.toJson(licencesAndPremises).as[play.api.libs.json.JsObject] ++
        Json.obj("premisesDetails" -> "invalid")

      Json.fromJson[LicencesAndPremises](json) shouldBe a[JsError]
    }
  }
}
