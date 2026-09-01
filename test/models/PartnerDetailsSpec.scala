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

package models

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsSuccess, Json}

import java.time.LocalDate

class PartnerDetailsSpec extends AnyWordSpec with Matchers {

  private val partnerModel = PartnerDetails(
    mgdRegNumber           = "XWM00000001762",
    businessPartnerNumber  = Some("0100049899"),
    dateOfJoining          = Some(LocalDate.of(2022, 1, 15)),
    dateOfLeaving          = Some(LocalDate.of(2028, 12, 31)),
    solePropTitle          = Some("Mr"),
    solePropFirstName      = Some("John"),
    solePropMiddleName     = Some("Michael"),
    solePropLastName       = Some("Doe"),
    businessName           = Some("XYZ Consulting Ltd"),
    tradingName            = Some("XYZ Consulting"),
    dateOfBirth            = Some(LocalDate.of(1985, 6, 20)),
    nino                   = Some("AB123456C"),
    utr                    = Some("1234567890"),
    vrn                    = Some("GB123456789"),
    crn                    = Some("09876543"),
    dateOfIncorporation    = Some(LocalDate.of(2020, 3, 1)),
    countryOfIncorporation = Some("GB"),
    foreignCorporateRef    = Some("FCR-987654"),
    address1               = Some("123 High Street"),
    address2               = Some("Suite 4"),
    address3               = Some("Business Park"),
    address4               = Some("London"),
    postcode               = Some("SW1A 1AA"),
    country                = Some("GB"),
    adi                    = Some("ADI123456"),
    iomOrCiFlag            = Some("N"),
    phoneNumber            = Some("02071234567"),
    mobilePhoneNumber      = Some("07700123456"),
    faxNumber              = Some("02071234568"),
    emailAddr              = Some("john.doe@example.com"),
    isFutureLeaveDate      = Some(0),
    isFutureJoinDate       = Some(0),
    businessType           = Some(2)
  )

  private val partnersDetailsModel = PartnersDetails(
    partners   = Seq(partnerModel),
    systemDate = Some(LocalDate.of(2026, 7, 30))
  )

  private val partnerJson = Json.obj(
    "mgdRegNumber"           -> "XWM00000001762",
    "businessPartnerNumber"  -> "0100049899",
    "dateOfJoining"          -> "2022-01-15",
    "dateOfLeaving"          -> "2028-12-31",
    "solePropTitle"          -> "Mr",
    "solePropFirstName"      -> "John",
    "solePropMiddleName"     -> "Michael",
    "solePropLastName"       -> "Doe",
    "businessName"           -> "XYZ Consulting Ltd",
    "tradingName"            -> "XYZ Consulting",
    "dateOfBirth"            -> "1985-06-20",
    "nino"                   -> "AB123456C",
    "utr"                    -> "1234567890",
    "vrn"                    -> "GB123456789",
    "crn"                    -> "09876543",
    "dateOfIncorporation"    -> "2020-03-01",
    "countryOfIncorporation" -> "GB",
    "foreignCorporateRef"    -> "FCR-987654",
    "address1"               -> "123 High Street",
    "address2"               -> "Suite 4",
    "address3"               -> "Business Park",
    "address4"               -> "London",
    "postcode"               -> "SW1A 1AA",
    "country"                -> "GB",
    "adi"                    -> "ADI123456",
    "iomOrCiFlag"            -> "N",
    "phoneNumber"            -> "02071234567",
    "mobilePhoneNumber"      -> "07700123456",
    "faxNumber"              -> "02071234568",
    "emailAddr"              -> "john.doe@example.com",
    "isFutureLeaveDate"      -> 0,
    "isFutureJoinDate"       -> 0,
    "businessType"           -> 2
  )

  private val fullJson = Json.obj(
    "partners"   -> Json.arr(partnerJson),
    "systemDate" -> "2026-07-30"
  )

  "PartnersDetails" should {

    "read complete partner details correctly" in {
      fullJson.validate[PartnersDetails] shouldBe JsSuccess(partnersDetailsModel)
    }

    "write complete partner details correctly" in {
      Json.toJson(partnersDetailsModel) shouldBe fullJson
    }

    "read partner details with only required fields present" in {
      val minJson = Json.obj(
        "partners" -> Json.arr(
          Json.obj("mgdRegNumber" -> "XWM00000001762")
        )
      )

      val expectedModel = PartnersDetails(
        partners = Seq(
          PartnerDetails(
            mgdRegNumber           = "XWM00000001762",
            businessPartnerNumber  = None,
            dateOfJoining          = None,
            dateOfLeaving          = None,
            solePropTitle          = None,
            solePropFirstName      = None,
            solePropMiddleName     = None,
            solePropLastName       = None,
            businessName           = None,
            tradingName            = None,
            dateOfBirth            = None,
            nino                   = None,
            utr                    = None,
            vrn                    = None,
            crn                    = None,
            dateOfIncorporation    = None,
            countryOfIncorporation = None,
            foreignCorporateRef    = None,
            address1               = None,
            address2               = None,
            address3               = None,
            address4               = None,
            postcode               = None,
            country                = None,
            adi                    = None,
            iomOrCiFlag            = None,
            phoneNumber            = None,
            mobilePhoneNumber      = None,
            faxNumber              = None,
            emailAddr              = None,
            isFutureLeaveDate      = None,
            isFutureJoinDate       = None,
            businessType           = None
          )
        ),
        systemDate = None
      )

      minJson.validate[PartnersDetails] shouldBe JsSuccess(expectedModel)
    }
  }
}
