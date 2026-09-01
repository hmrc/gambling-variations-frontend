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
import play.api.libs.json.{JsError, JsObject, Json}

import java.time.LocalDate

class PremisesDetailsSpec extends AnyWordSpec with Matchers {

  private val premisesDetails = PremisesDetails(
    mgdRegNumber = "MGD123456",
    address1     = Some("123 High Street"),
    address2     = Some("Town Centre"),
    address3     = Some("London"),
    address4     = None,
    postcode     = Some("AB1 2CD"),
    systemDate   = Some(LocalDate.of(2026, 8, 26))
  )

  private val premises = Seq(
    PremisesDetails(
      mgdRegNumber = "MGD123456",
      address1     = Some("123 High Street"),
      address2     = Some("Town Centre"),
      address3     = Some("London"),
      address4     = None,
      postcode     = Some("AB1 2CD"),
      systemDate   = None
    ),
    PremisesDetails(
      mgdRegNumber = "MGD654321",
      address1     = Some("456 Main Street"),
      address2     = None,
      address3     = Some("Manchester"),
      address4     = None,
      postcode     = Some("MN1 2AB"),
      systemDate   = None
    )
  )

  private val response = PremisesDetailsResponse(
    totalRows = Some(2),
    premises  = premises
  )

  "PremisesDetails" should {

    "round trip successfully through JSON" in {
      val json = Json.toJson(premisesDetails)

      json.as[PremisesDetails] shouldBe premisesDetails
    }

    "round trip successfully through JSON when optional fields are None" in {
      val premisesDetailsWithNone = PremisesDetails(
        mgdRegNumber = "MGD123456",
        address1     = None,
        address2     = None,
        address3     = None,
        address4     = None,
        postcode     = None,
        systemDate   = None
      )

      val json = Json.toJson(premisesDetailsWithNone)

      json.as[PremisesDetails] shouldBe premisesDetailsWithNone
    }

    "return a JsError when the required mgdRegNumber is missing" in {
      val json: JsObject =
        Json.toJson(premisesDetails).as[JsObject] - "mgdRegNumber"

      Json.fromJson[PremisesDetails](json) shouldBe a[JsError]
    }

    "return a JsError when mgdRegNumber has an invalid type" in {
      val json: JsObject =
        Json.toJson(premisesDetails).as[JsObject] ++
          Json.obj("mgdRegNumber" -> 123)

      Json.fromJson[PremisesDetails](json) shouldBe a[JsError]
    }
  }

  "PremisesDetailsResponse" should {

    "round trip successfully through JSON" in {
      val json = Json.toJson(response)

      json.as[PremisesDetailsResponse] shouldBe response
    }

    "round trip successfully through JSON when totalRows is None" in {
      val responseWithNoTotalRows = response.copy(
        totalRows = None
      )

      val json = Json.toJson(responseWithNoTotalRows)

      json.as[PremisesDetailsResponse] shouldBe responseWithNoTotalRows
    }

    "round trip successfully with an empty premises sequence" in {
      val emptyResponse = PremisesDetailsResponse(
        totalRows = Some(0),
        premises  = Seq.empty
      )

      val json = Json.toJson(emptyResponse)

      json.as[PremisesDetailsResponse] shouldBe emptyResponse
    }

    "return a JsError when premises is missing" in {
      val json: JsObject =
        Json.toJson(response).as[JsObject] - "premises"

      Json.fromJson[PremisesDetailsResponse](json) shouldBe a[JsError]
    }

    "return a JsError when premises has an invalid type" in {
      val json: JsObject =
        Json.toJson(response).as[JsObject] ++
          Json.obj("premises" -> "invalid")

      Json.fromJson[PremisesDetailsResponse](json) shouldBe a[JsError]
    }

    "return a JsError when totalRows has an invalid type" in {
      val json: JsObject =
        Json.toJson(response).as[JsObject] ++
          Json.obj("totalRows" -> "invalid")

      Json.fromJson[PremisesDetailsResponse](json) shouldBe a[JsError]
    }
  }
}
