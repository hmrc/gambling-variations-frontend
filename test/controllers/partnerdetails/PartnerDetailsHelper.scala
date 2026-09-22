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

package controllers.partnerdetails

import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Call

trait PartnerDetailsHelper {

  val businessNumber1: String = "12345"
  val businessNumber2: String = "123456"
  val newPartnersIndex1: Int = 0
  val newPartnersIndex2: Int = 1
  // TODO delete, and whaeber uses it, should use businessNumber1
//  val index: String = businessNumber1

  val mgdRegNumber: String = "XGM00000001761" // TODO we have two mgdRegNumber, this really has to be sorted, other one is in SpecBase I think
  val testFaxNumber: String = "0123456789"
  val testPhoneNumber: String = "0123456789"
  val testEmailAddress: String = "test@test.com"
  val testNino: String = "SR123456A"
  val testVRN: String = "353868127"
  val testUtr: String = "1121766916"
  val testForeignCorpRef = "FCR-987654"

  lazy val onwardRoute: Call = Call("GET", "/foo")

  // TODO might need for newPartners
  def cleanedData(
    faxNumber: Option[String] = None,
    phoneNumber: Option[String] = None,
    mobilePhoneNumber: Option[String] = None,
    emailAddress: Option[String] = None,
    additionalInformation: Option[String] = Some("ADI123456"),
    nino: Option[String] = None,
    vrn: Option[String] = None,
    utr: Option[String] = None,
    fcr: Option[String] = None
  ): JsObject = Json.obj(
    "partners" -> Json.obj(
      // TODO using businessNumber1 here, I might benefit by putting it into constructor
      businessNumber1 -> Json.obj(
        "partnerDetailsMgdRegNumber"  -> mgdRegNumber,
        "partnerDetailsDateOfJoining" -> "2022-01-15",
        "partnerDetailsDateOfLeaving" -> "2028-12-31",
        "partnerDetailsCorrespondenceDetailsSection" -> Json.obj(
          "mgdRegNumber" -> mgdRegNumber,
          "correspondenceAddress" -> Json.obj(
            "address1" -> "123 High Street",
            "address2" -> "Suite 4",
            "address3" -> "Business Park",
            "address4" -> "London",
            "postcode" -> "SW1A 1AA",
            "country"  -> "GB"
          ),
          "additionalInformation" -> additionalInformation,
          "iomOrCiFlag"           -> "N",
          "contactNumber" -> Json.obj(
            "phoneNumber"       -> phoneNumber,
            "mobilePhoneNumber" -> mobilePhoneNumber
          ),
          "faxNumber" -> faxNumber,
          "emailAddr" -> emailAddress
        ),
        "partnerDetailsDateOfIncorporation"    -> "2020-03-01",
        "partnerDetailsCountryOfIncorporation" -> "GB",
        "partnerDetailsBusinessName"           -> "XYZ Consulting Ltd",
        "partnerDetailsTradingName"            -> "XYZ Consulting",
        "partnerDetailsDateOfBirth"            -> "1985-06-20",
        "partnerDetailsNino"                   -> nino,
        "partnerDetailsUtr"                    -> utr,
        "partnerDetailsVrn"                    -> vrn,
        "partnerDetailsCrn"                    -> "09876543",
        "partnerDetailsForeignCorporateRef"    -> fcr,
        "partnerDetailsIsFutureLeaveDate"      -> 0,
        "partnerDetailsIsFutureJoinDate"       -> 0,
        "partnerDetailsBusinessType"           -> 1
      )
    )
  )

  def cleanedDataNewPartners(
    faxNumber: Option[String] = None,
    phoneNumber: Option[String] = None,
    mobilePhoneNumber: Option[String] = None,
    emailAddress: Option[String] = None,
    additionalInformation: Option[String] = Some("ADI123456"),
    nino: Option[String] = None,
    vrn: Option[String] = None,
    utr: Option[String] = None,
    fcr: Option[String] = None
  ): JsObject = Json.obj(
    "newPartners" -> Json.arr(
      Json.obj(
        "partnerDetailsMgdRegNumber"  -> mgdRegNumber,
        "partnerDetailsDateOfJoining" -> "2022-01-15",
        "partnerDetailsDateOfLeaving" -> "2028-12-31",
        "partnerDetailsCorrespondenceDetailsSection" -> Json.obj(
          "mgdRegNumber" -> mgdRegNumber,
          "correspondenceAddress" -> Json.obj(
            "address1" -> "123 High Street",
            "address2" -> "Suite 4",
            "address3" -> "Business Park",
            "address4" -> "London",
            "postcode" -> "SW1A 1AA",
            "country"  -> "GB"
          ),
          "additionalInformation" -> additionalInformation,
          "iomOrCiFlag"           -> "N",
          "contactNumber" -> Json.obj(
            "phoneNumber"       -> phoneNumber,
            "mobilePhoneNumber" -> mobilePhoneNumber
          ),
          "faxNumber" -> faxNumber,
          "emailAddr" -> emailAddress
        ),
        "partnerDetailsDateOfIncorporation"    -> "2020-03-01",
        "partnerDetailsCountryOfIncorporation" -> "GB",
        "partnerDetailsBusinessName"           -> "XYZ Consulting Ltd",
        "partnerDetailsTradingName"            -> "XYZ Consulting",
        "partnerDetailsDateOfBirth"            -> "1985-06-20",
        "partnerDetailsNino"                   -> nino,
        "partnerDetailsUtr"                    -> utr,
        "partnerDetailsVrn"                    -> vrn,
        "partnerDetailsCrn"                    -> "09876543",
        "partnerDetailsForeignCorporateRef"    -> fcr,
        "partnerDetailsIsFutureLeaveDate"      -> 0,
        "partnerDetailsIsFutureJoinDate"       -> 0,
        "partnerDetailsBusinessType"           -> 1
      )
    )
  )
}
