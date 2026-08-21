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

package controllers.partner

import play.api.libs.json.{JsObject, Json}

trait PartnerDetailsHelper {

  val index: Int = 0
  val mgdRegNumber: String = "XGM00000001761"
  val testFaxNumber: String = "0123456789"
  val testPhoneNumber: String = "0123456789"
  val testEmailAddress: String = "test@test.com"
  val testNino: String = "SR123456A"

  def cleanedData(
    faxNumber: Option[String] = None,
    phoneNumber: Option[String] = None,
    mobilePhoneNumber: Option[String] = None,
    emailAddress: Option[String] = None,
    additionalInformation: Option[String] = Some("ADI123456"),
    nino: Option[String] = None
  ): JsObject = Json.obj(
    "partners" -> Json.arr(
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
        "partnerDetailsUtr"                    -> "1234567890",
        "partnerDetailsVrn"                    -> "GB123456789",
        "partnerDetailsCrn"                    -> "09876543",
        "partnerDetailsForeignCorporateRef"    -> "FCR-987654",
        "partnerDetailsIsFutureLeaveDate"      -> 0,
        "partnerDetailsIsFutureJoinDate"       -> 0,
        "partnerDetailsBusinessType"           -> 2
      )
    )
  )

}
