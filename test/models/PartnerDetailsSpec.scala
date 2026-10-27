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
import play.api.libs.json.{JsError, JsSuccess, Json, __}

import java.time.LocalDate

class PartnerDetailsSpec extends AnyWordSpec with Matchers {

  val partnerDetailsBusiness = BusinessPartnerDetails(
    mgdRegNumber           = "XWM00000001762",
    businessPartnerNumber  = "0100049899",
    dateOfJoining          = Some(LocalDate.parse("2022-01-15")),
    dateOfLeaving          = Some(LocalDate.parse("2028-12-31")),
    businessName           = Some("XYZ Consulting Ltd"),
    tradingName            = Some("XYZ Consulting"),
    dateOfBirth            = Some(LocalDate.parse("1985-06-20")),
    nino                   = Some("AB123456C"),
    utr                    = Some("1234567890"),
    vrn                    = Some("GB123456789"),
    crn                    = Some("09876543"),
    dateOfIncorporation    = Some(LocalDate.parse("2020-03-01")),
    countryOfIncorporation = Some("GB"),
    foreignCorporateRef    = Some("FCR-987654"),
    correspondenceDetails = Some(
      CorrespondenceDetails(
        mgdRegNumber = "XWM00000001762",
        nameLine1    = None,
        nameLine2    = None,
        correspondenceAddress = Some(
          Address(
            address1 = "123 High Street",
            address2 = Some("Suite 4"),
            address3 = Some("Business Park"),
            address4 = Some("London"),
            postcode = Some("SW1A 1AA"),
            country  = Some("GB")
          )
        ),
        additionalInformation = Some("ADI123456"),
        iomOrCiFlag           = Some(false),
        contactNumber = Some(
          ContactNumber(
            phoneNumber       = Some("02071234567"),
            mobilePhoneNumber = Some("07700123456")
          )
        ),
        faxNumber = Some("02071234568"),
        emailAddr = Some("john.doe@example.com")
      )
    ),
    isFutureLeaveDate = Some(false),
    isFutureJoinDate  = Some(false),
    businessType      = BusinessType.Corporatebody
  )

  val partnerDetailsSoleProprietor = SoleProprietorPartnerDetails(
    mgdRegNumber          = "XWM00000001762",
    businessPartnerNumber = "0100049899",
    dateOfJoining         = Some(LocalDate.parse("2022-01-15")),
    dateOfLeaving         = Some(LocalDate.parse("2028-12-31")),
    soleProprietorName = Some(
      SoleProprietorName(
        title      = "Mr",
        firstName  = "John",
        middleName = Some("Michael"),
        lastName   = "Doe"
      )
    ),
    tradingName            = Some("XYZ Consulting"),
    dateOfBirth            = Some(LocalDate.parse("1985-06-20")),
    nino                   = Some("AB123456C"),
    utr                    = Some("1234567890"),
    vrn                    = Some("GB123456789"),
    crn                    = Some("09876543"),
    dateOfIncorporation    = Some(LocalDate.parse("2020-03-01")),
    countryOfIncorporation = Some("GB"),
    foreignCorporateRef    = Some("FCR-987654"),
    correspondenceDetails = Some(
      CorrespondenceDetails(
        mgdRegNumber = "XWM00000001762",
        nameLine1    = None,
        nameLine2    = None,
        correspondenceAddress = Some(
          Address(
            address1 = "123 High Street",
            address2 = Some("Suite 4"),
            address3 = Some("Business Park"),
            address4 = Some("London"),
            postcode = Some("SW1A 1AA"),
            country  = Some("GB")
          )
        ),
        additionalInformation = Some("ADI123456"),
        iomOrCiFlag           = Some(false),
        contactNumber = Some(
          ContactNumber(
            phoneNumber       = Some("02071234567"),
            mobilePhoneNumber = Some("07700123456")
          )
        ),
        faxNumber = Some("02071234568"),
        emailAddr = Some("john.doe@example.com")
      )
    ),
    isFutureLeaveDate = Some(false),
    isFutureJoinDate  = Some(false),
    businessType      = BusinessType.Soleproprietor
  )

  val partnersDetailsBusinessName = PartnersDetails(
    partners   = Seq(partnerDetailsBusiness),
    systemDate = Some(LocalDate.of(2026, 7, 30))
  )

  val partnersDetailsSoleProprietor = PartnersDetails(
    partners   = Seq(partnerDetailsSoleProprietor),
    systemDate = Some(LocalDate.of(2026, 7, 30))
  )

  private val solePropPartnerJson = Json.obj(
    "mgdRegNumber"           -> "XWM00000001762",
    "businessPartnerNumber"  -> "0100049899",
    "dateOfJoining"          -> "2022-01-15",
    "dateOfLeaving"          -> "2028-12-31",
    "solePropTitle"          -> "Mr",
    "solePropFirstName"      -> "John",
    "solePropMiddleName"     -> "Michael",
    "solePropLastName"       -> "Doe",
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
    "businessType"           -> 1
  )

  private val businessPartnerJson = Json.obj(
    "mgdRegNumber"           -> "XWM00000001762",
    "businessPartnerNumber"  -> "0100049899",
    "dateOfJoining"          -> "2022-01-15",
    "dateOfLeaving"          -> "2028-12-31",
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

  private val businessPartnerFullJson = Json.obj(
    "partners"   -> Json.arr(businessPartnerJson),
    "systemDate" -> "2026-07-30"
  )

  private val solePropPartnerFullJson = Json.obj(
    "partners"   -> Json.arr(solePropPartnerJson),
    "systemDate" -> "2026-07-30"
  )

  "PartnersDetails" should {

    "read complete partner details correctly" in {
      businessPartnerFullJson.validate[PartnersDetails] shouldBe JsSuccess(partnersDetailsBusinessName)
      solePropPartnerFullJson.validate[PartnersDetails] shouldBe JsSuccess(partnersDetailsSoleProprietor)
    }

    "read partner details with only required fields present" in {
      val minJsonSoleProp = Json.obj(
        "partners" -> Json.arr(
          Json.obj("mgdRegNumber" -> "XWM00000001762", "businessType" -> 1, "businessPartnerNumber" -> "1234567890")
        )
      )

      val minJsonBusiness = Json.obj(
        "partners" -> Json.arr(
          Json.obj("mgdRegNumber" -> "XWM00000001762", "businessType" -> 2, "businessPartnerNumber" -> "1234567890")
        )
      )

      val businessExpectedModel = PartnersDetails(
        Seq(
          BusinessPartnerDetails(
            mgdRegNumber           = "XWM00000001762",
            businessPartnerNumber  = "1234567890",
            dateOfJoining          = None,
            dateOfLeaving          = None,
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
            correspondenceDetails = Some(
              CorrespondenceDetails(
                mgdRegNumber          = "XWM00000001762",
                nameLine1             = None,
                nameLine2             = None,
                correspondenceAddress = None,
                additionalInformation = None,
                iomOrCiFlag           = None,
                contactNumber         = None,
                faxNumber             = None,
                emailAddr             = None
              )
            ),
            isFutureLeaveDate = None,
            isFutureJoinDate  = None,
            businessType      = BusinessType.Corporatebody
          )
        ),
        None
      )

      val solePropExpectedModel = PartnersDetails(
        Seq(
          SoleProprietorPartnerDetails(
            mgdRegNumber           = "XWM00000001762",
            businessPartnerNumber  = "1234567890",
            dateOfJoining          = None,
            dateOfLeaving          = None,
            soleProprietorName     = None,
            tradingName            = None,
            dateOfBirth            = None,
            nino                   = None,
            utr                    = None,
            vrn                    = None,
            crn                    = None,
            dateOfIncorporation    = None,
            countryOfIncorporation = None,
            foreignCorporateRef    = None,
            correspondenceDetails = Some(
              CorrespondenceDetails(
                mgdRegNumber          = "XWM00000001762",
                nameLine1             = None,
                nameLine2             = None,
                correspondenceAddress = None,
                additionalInformation = None,
                iomOrCiFlag           = None,
                contactNumber         = None,
                faxNumber             = None,
                emailAddr             = None
              )
            ),
            isFutureLeaveDate = None,
            isFutureJoinDate  = None,
            businessType      = BusinessType.Soleproprietor
          )
        ),
        None
      )

      minJsonSoleProp.validate[PartnersDetails] shouldBe JsSuccess(solePropExpectedModel)
      minJsonBusiness.validate[PartnersDetails] shouldBe JsSuccess(businessExpectedModel)
    }

    "read partner details fails if businessType is missing or incorrect" in {
      val minJsonMissingBusinessType = Json.obj(
        "partners" -> Json.arr(
          Json.obj("mgdRegNumber" -> "XWM00000001762", "businessPartnerNumber" -> "1234567890")
        )
      )

      val minJsonIncorrectBusinessType = Json.obj(
        "partners" -> Json.arr(
          Json.obj("mgdRegNumber" -> "XWM00000001762", "businessPartnerNumber" -> "12345678900", "businessType" -> 100)
        )
      )

      minJsonMissingBusinessType.validate[PartnersDetails] shouldBe JsError(
        __ \ "partners" \ 0,
        "Invalid business type structure: expected businessType to contain a valid structure for PartnerDetails."
      )
      minJsonIncorrectBusinessType.validate[PartnersDetails] shouldBe JsError(
        __ \ "partners" \ 0 \ "businessType",
        "Invalid business type"
      )
    }

    "read partner details fails if mgdRegNumber is missing" in {
      val minJsonMissingRegNumber = Json.obj(
        "partners" -> Json.arr(
          Json.obj("businessPartnerNumber" -> "12345678900", "businessType" -> 1)
        )
      )

      minJsonMissingRegNumber.validate[PartnersDetails] shouldBe JsError(
        __ \ "partners" \ 0 \ "mgdRegNumber",
        "error.path.missing"
      )
    }

    "read partner details fails if partnerBusinessNumber is missing" in {
      val minJsonMissingBusinessNumber = Json.obj(
        "partners" -> Json.arr(
          Json.obj("mgdRegNumber" -> "XWM00000001762", "businessType" -> 1)
        )
      )

      minJsonMissingBusinessNumber.validate[PartnersDetails] shouldBe JsError(
        __ \ "partners" \ 0 \ "businessPartnerNumber",
        "error.path.missing"
      )
    }
  }
}
