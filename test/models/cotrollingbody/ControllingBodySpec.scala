/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package models.cotrollingbody

import models.BusinessType
import models.controllingbody.ControllingBody
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json

import java.time.LocalDate

class ControllingBodySpec extends AnyWordSpec with Matchers {

  "Control Body Details JSON format" should {

    "serialize to JSON when defined" in {

      val json = Json.toJson(controllingBodyResponse)

      json shouldBe Json.obj(
        "mgdRegNumber"           -> "XGM00000001761",
        "businessPartnerNumber"  -> "0100053091",
        "dateOfJoining"          -> LocalDate.of(2013, 2, 1),
        "dateOfLeaving"          -> LocalDate.of(2023, 3, 1),
        "solePropTitle"          -> "Mx",
        "solePropFirstName"      -> "solePropFirstName",
        "solePropMiddleName"     -> "solePropMiddleName",
        "solePropLastName"       -> "solePropLastName",
        "businessName"           -> "BRUCE HOPKINS LIMITED",
        "tradingName"            -> "Trading name 1",
        "dateOfBirth"            -> LocalDate.of(1998, 6, 24),
        "nino"                   -> "AB123456C",
        "utr"                    -> 5202020208L,
        "vrn"                    -> 127207785L,
        "crn"                    -> "12345678",
        "dateOfIncorporation"    -> LocalDate.of(2020, 2, 15),
        "countryOfIncorporation" -> "Spain",
        "foreignCorporateRef"    -> "foreignCorporateRef",
        "address1"               -> "Address 1",
        "address2"               -> "Address 2",
        "address3"               -> "Address 3",
        "address4"               -> "Address 4",
        "postcode"               -> "postcode",
        "country"                -> "Spain",
        "adi"                    -> "adi",
        "isIomOrCiFlag"          -> "0",
        "phoneNumber"            -> "phoneNumber",
        "mobilePhoneNumber"      -> "mobilePhoneNumber",
        "faxNumber"              -> "faxNumber",
        "emailAddr"              -> "emailAddr",
        "typeOfControllingBody"  -> 1,
        "isRepMemSameAsCb"       -> "0",
        "isUkIncorporated"       -> "0"
      )
    }

    "deserialize JSON" in {

      val json = Json.obj(
        "mgdRegNumber"           -> "XGM00000001761",
        "businessPartnerNumber"  -> "0100053091",
        "dateOfJoining"          -> LocalDate.of(2013, 2, 1),
        "dateOfLeaving"          -> LocalDate.of(2023, 3, 1),
        "solePropTitle"          -> "Mx",
        "solePropFirstName"      -> "solePropFirstName",
        "solePropMiddleName"     -> "solePropMiddleName",
        "solePropLastName"       -> "solePropLastName",
        "businessName"           -> "BRUCE HOPKINS LIMITED",
        "tradingName"            -> "Trading name 1",
        "dateOfBirth"            -> LocalDate.of(1998, 6, 24),
        "nino"                   -> "AB123456C",
        "utr"                    -> 5202020208L,
        "vrn"                    -> 127207785L,
        "crn"                    -> "12345678",
        "dateOfIncorporation"    -> LocalDate.of(2020, 2, 15),
        "countryOfIncorporation" -> "Spain",
        "foreignCorporateRef"    -> "foreignCorporateRef",
        "address1"               -> "Address 1",
        "address2"               -> "Address 2",
        "address3"               -> "Address 3",
        "address4"               -> "Address 4",
        "postcode"               -> "postcode",
        "country"                -> "Spain",
        "adi"                    -> "adi",
        "isIomOrCiFlag"          -> "0",
        "phoneNumber"            -> "phoneNumber",
        "mobilePhoneNumber"      -> "mobilePhoneNumber",
        "faxNumber"              -> "faxNumber",
        "emailAddr"              -> "emailAddr",
        "typeOfControllingBody"  -> 1,
        "isRepMemSameAsCb"       -> "0",
        "isUkIncorporated"       -> "0"
      )

      val result = json.as[ControllingBody]

      result shouldBe controllingBodyResponse
    }

    "deserialize missing optional fields as None" in {

      val json = Json.obj(
        "mgdRegNumber" -> "XRM00000000574"
      )

      val result = json.as[ControllingBody]

      result shouldBe ControllingBody(
        mgdRegNumber           = "XRM00000000574",
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
        isIomOrCiFlag          = None,
        phoneNumber            = None,
        mobilePhoneNumber      = None,
        faxNumber              = None,
        emailAddr              = None,
        typeOfControllingBody  = None,
        isRepMemSameAsCb       = None,
        isUkIncorporated       = None
      )
    }
  }
  val controllingBodyResponse: ControllingBody =
    ControllingBody(
      mgdRegNumber           = "XGM00000001761",
      businessPartnerNumber  = Some("0100053091"),
      dateOfJoining          = Some(LocalDate.of(2013, 2, 1)),
      dateOfLeaving          = Some(LocalDate.of(2023, 3, 1)),
      solePropTitle          = Some("Mx"),
      solePropFirstName      = Some("solePropFirstName"),
      solePropMiddleName     = Some("solePropMiddleName"),
      solePropLastName       = Some("solePropLastName"),
      businessName           = Some("BRUCE HOPKINS LIMITED"),
      tradingName            = Some("Trading name 1"),
      dateOfBirth            = Some(LocalDate.of(1998, 6, 24)),
      nino                   = Some("AB123456C"),
      utr                    = Some(5202020208L),
      vrn                    = Some(127207785L),
      crn                    = Some("12345678"),
      dateOfIncorporation    = Some(LocalDate.of(2020, 2, 15)),
      countryOfIncorporation = Some("Spain"),
      foreignCorporateRef    = Some("foreignCorporateRef"),
      address1               = Some("Address 1"),
      address2               = Some("Address 2"),
      address3               = Some("Address 3"),
      address4               = Some("Address 4"),
      postcode               = Some("postcode"),
      country                = Some("Spain"),
      adi                    = Some("adi"),
      isIomOrCiFlag          = Some("0"),
      phoneNumber            = Some("phoneNumber"),
      mobilePhoneNumber      = Some("mobilePhoneNumber"),
      faxNumber              = Some("faxNumber"),
      emailAddr              = Some("emailAddr"),
      typeOfControllingBody  = Some(BusinessType.Soleproprietor),
      isRepMemSameAsCb       = Some("0"),
      isUkIncorporated       = Some("0")
    )
}
