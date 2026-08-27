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

package connectors

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import models.BusinessType.Unincorporatedbody
import models.licencespremises.{LicencesAndPremises, PremisesDetails, PremisesDetailsResponse}
import models.{Address, BusinessAddress, BusinessContactDetails, BusinessDetails, BusinessNameDetails, BusinessTradeClass, ContactNumber, CorrespondenceDetails, MgdCertificate, MgdTradeDetails, PartnerDetails, PartnersDetails}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}

import java.time.LocalDate
import scala.concurrent.ExecutionContext

class GamblingConnectorISpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  import GamblingConnectorISpec.*

  given ExecutionContext = ExecutionContext.global
  given HeaderCarrier = HeaderCarrier()

  private val wireMockServer = new WireMockServer(0)

  override def beforeAll(): Unit = {
    wireMockServer.start()
    configureFor("localhost", wireMockServer.port())
  }

  override def afterAll(): Unit =
    wireMockServer.stop()

  private lazy val app =
    new GuiceApplicationBuilder()
      .configure(
        "microservice.services.gambling.protocol" -> "http",
        "microservice.services.gambling.host"     -> "localhost",
        "microservice.services.gambling.port"     -> wireMockServer.port()
      )
      .build()

  private lazy val connector =
    app.injector.instanceOf[GamblingConnector]

  "GamblingConnector.getParterDetails" should {

    "return partnerDetails when backend returns 200" in {

      val jsonAsString: String =
        """
          |{"partners":[{
          |  "mgdRegNumber": "XWM00000001762",
          |  "dateOfJoining": "2022-01-15",
          |  "dateOfLeaving": "2028-12-31",
          |  "businessName": "XYZ Consulting Ltd",
          |  "tradingName": "XYZ Consulting",
          |  "dateOfBirth": "1985-06-20",
          |  "nino": "AB123456C",
          |  "utr": "1234567890",
          |  "vrn": "GB123456789",
          |  "crn": "09876543",
          |  "dateOfIncorporation": "2020-03-01",
          |  "countryOfIncorporation": "GB",
          |  "foreignCorporateRef": "FCR-987654",
          |  "address1": "123 High Street",
          |  "address2": "Suite 4",
          |  "address3": "Business Park",
          |  "address4": "London",
          |  "postcode": "SW1A 1AA",
          |  "country": "GB",
          |  "adi": "ADI123456",
          |  "iomOrCiFlag": "N",
          |  "phoneNumber": "02071234567",
          |  "mobilePhoneNumber": "07700123456",
          |  "faxNumber": "02071234568",
          |  "emailAddr": "john.doe@example.com",
          |  "isFutureLeaveDate": 0,
          |  "isFutureJoinDate": 0,
          |  "businessType": 2
          |}],
          |"systemDate": "2026-07-30"
          |}
          |""".stripMargin

      wireMockServer.stubFor(
        get(urlEqualTo(s"/gambling/partner-details/mgd/$mgdRegNumber"))
          .willReturn(okJson(jsonAsString))
      )

      connector.getPartnersDetails(mgdRegNumber).map { result =>
        result mustBe partnersDetailsBusinessName
      }
    }

    "return UpstreamErrorResponse when backend returns 404" in {

      wireMockServer.stubFor(
        get(urlEqualTo(s"/gambling/partner-details/mgd/$mgdRegNumber"))
          .willReturn(aResponse().withStatus(404))
      )

      recoverToSucceededIf[UpstreamErrorResponse] {
        connector.getPartnersDetails(mgdRegNumber)
      }
    }

    "return UpstreamErrorResponse when backend returns 500" in {

      wireMockServer.stubFor(
        get(urlEqualTo(s"/gambling/partner-details/mgd/$mgdRegNumber"))
          .willReturn(serverError())
      )

      recoverToSucceededIf[UpstreamErrorResponse] {
        connector.getPartnersDetails(mgdRegNumber)
      }
    }

  }

  "GamblingConnector.getBusinessDetails" should {

    "return business details when backend returns 200" ignore {

      val jsonAsString: String =
        s"""{
           |  "mgdRegNumber": "$mgdRegNumber",
           |  "businessType": 3,
           |  "currentlyRegistered": 1,
           |  "groupReg": false,
           |  "dateOfRegistration": "2020-01-01",
           |  "businessPartnerNumber": "XB1234567890",
           |  "systemDate": "2026-01-01"
           |}""".stripMargin

      wireMockServer.stubFor(
        get(urlEqualTo(s"/gambling/business-details/mgd/$mgdRegNumber"))
          .willReturn(okJson(jsonAsString))
      )

      connector.getBusinessDetails(mgdRegNumber).futureValue mustBe businessDetails
    }

    "return UpstreamErrorResponse when backend returns 404" in {

      wireMockServer.stubFor(
        get(urlEqualTo(s"/gambling/business-details/mgd/$mgdRegNumber"))
          .willReturn(aResponse().withStatus(404))
      )

      recoverToSucceededIf[UpstreamErrorResponse] {
        connector.getBusinessDetails(mgdRegNumber)
      }
    }

    "return UpstreamErrorResponse when backend returns 500" in {

      wireMockServer.stubFor(
        get(urlEqualTo(s"/gambling/business-details/mgd/$mgdRegNumber"))
          .willReturn(serverError())
      )

      recoverToSucceededIf[UpstreamErrorResponse] {
        connector.getBusinessDetails(mgdRegNumber)
      }
    }

    "throw RuntimeException when backend returns invalid JSON" in {

      val invalidJson =
        """{
          |  "invalidField": "invalidValue"
          |}""".stripMargin

      wireMockServer.stubFor(
        get(urlEqualTo(s"/gambling/business-details/mgd/$mgdRegNumber"))
          .willReturn(okJson(invalidJson))
      )

      recoverToSucceededIf[RuntimeException] {
        connector.getBusinessDetails(mgdRegNumber)
      }
    }

    "throw RuntimeException when business type code is invalid" in {

      val invalidBusinessTypeJson =
        s"""{
           |  "mgdRegNumber": "$mgdRegNumber",
           |  "businessType": 999,
           |  "currentlyRegistered": 1,
           |  "groupReg": false,
           |  "systemDate": "2026-01-01"
           |}""".stripMargin

      wireMockServer.stubFor(
        get(urlEqualTo(s"/gambling/business-details/mgd/$mgdRegNumber"))
          .willReturn(okJson(invalidBusinessTypeJson))
      )

      recoverToSucceededIf[RuntimeException] {
        connector.getBusinessDetails(mgdRegNumber)
      }
    }

  }

  "GamblingConnector.getBusinessName" should {

    "return businessName when backend returns 200" in {

      val jsonAsString: String =
        s"""{
           |  "mgdRegNumber": "ABC12345678901",
           |  "businessName": "Test Business Ltd",
           |  "businessType": 3,
           |  "tradingName": "Trading Name",
           |  "systemDate": "${LocalDate.of(1991, 1, 1)}"
           |}""".stripMargin

      wireMockServer.stubFor(
        get(urlEqualTo(s"/gambling/business-name/mgd/$mgdRegNumber"))
          .willReturn(okJson(jsonAsString))
      )

      connector.getBusinessName(mgdRegNumber).futureValue mustBe businessName
    }

    "return NotFound when backend returns UpstreamErrorResponse" in {

      wireMockServer.stubFor(
        get(urlEqualTo(s"/gambling/business-name/mgd/$mgdRegNumber"))
          .willReturn(aResponse().withStatus(404))
      )

      recoverToSucceededIf[UpstreamErrorResponse] {
        connector.getBusinessName(mgdRegNumber)
      }
    }

    "return Left(UnexpectedError) when backend returns 500" in {

      wireMockServer.stubFor(
        get(urlEqualTo(s"/gambling/business-name/mgd/$mgdRegNumber"))
          .willReturn(serverError())
      )

      recoverToSucceededIf[UpstreamErrorResponse] {
        connector.getBusinessName(mgdRegNumber)
      }
    }

  }

  "GamblingConnector.getCertificate" should {

    "return certificate when backend returns 200" in {

      val jsonAsString: String =
        s"""{
           |  "mgdRegNumber": "$mgdRegNumber",
           |  "businessName": "Test Business Ltd",
           |  "tradingName": "Test Trader Ltd",
           |  "groupReg": "N",
           |  "groupMembers": [],
           |  "partMembers": [],
           |  "returnPeriodEndDates": []
           |}""".stripMargin

      wireMockServer.stubFor(
        get(urlEqualTo(s"/gambling/certificate/mgd/$mgdRegNumber"))
          .willReturn(okJson(jsonAsString))
      )

      connector.getCertificate(mgdRegNumber).futureValue mustBe certificate
    }

    "return NotFound when backend returns UpstreamErrorResponse" in {

      wireMockServer.stubFor(
        get(urlEqualTo(s"/gambling/certificate/mgd/$mgdRegNumber"))
          .willReturn(aResponse().withStatus(404))
      )

      recoverToSucceededIf[UpstreamErrorResponse] {
        connector.getCertificate(mgdRegNumber)
      }
    }

    "return Left(UnexpectedError) when backend returns 500" in {

      wireMockServer.stubFor(
        get(urlEqualTo(s"/gambling/certificate/mgd/$mgdRegNumber"))
          .willReturn(serverError())
      )

      recoverToSucceededIf[UpstreamErrorResponse] {
        connector.getCertificate(mgdRegNumber)
      }
    }

  }

  "GamblingConnector.getBusinessContactDetails" should {

    "return businessContactDetails when backend returns 200" in {

      val jsonAsString: String =
        s"""{
           |  "mgdRegNumber": "ABC12345678901",
           |  "phoneNumber": "+44 8903928171",
           |  "mobilePhoneNumber": "+44 8903928171",
           |  "faxNumber": "+_+_ hdj39783",
           |  "emailAddr": "a@b.com",
           |  "systemDate": "${LocalDate.of(1991, 1, 1)}"
           |}""".stripMargin

      wireMockServer.stubFor(
        get(urlEqualTo(s"/gambling/business-contact-details/mgd/$mgdRegNumber"))
          .willReturn(okJson(jsonAsString))
      )

      connector.getBusinessContactDetails(mgdRegNumber).futureValue mustBe businessContactDetails
    }

    "return NotFound when backend returns UpstreamErrorResponse" in {

      wireMockServer.stubFor(
        get(urlEqualTo(s"/gambling/business-contact-details/mgd/$mgdRegNumber"))
          .willReturn(aResponse().withStatus(404))
      )

      recoverToSucceededIf[UpstreamErrorResponse] {
        connector.getBusinessContactDetails(mgdRegNumber)
      }
    }

  }

  "GamblingConnector.getMgdTradeDetails" should {

    "return mgdTradeDetails when backend returns 200" in {

      val jsonAsString: String =
        s"""{
           |  "mgdRegNumber": "XRM00000000574",
           |  "isBusinessSeasonal": 1,
           |  "businessTradeClass": 6,
           |  "businessActivityDesc": "Description",
           |  "previousMgdRegistrationNumbers": ["XWM00000001774", "XDM00000001309", ""],
           |  "associatedMgdRegistrationNumbers": ["XXM00000000723", "XQM00000001196", ""],
           |  "systemDate": "2026-05-31"
           |
           |}""".stripMargin

      wireMockServer.stubFor(
        get(urlEqualTo(s"/gambling/mgd-and-trade-details/mgd/$mgdRegNumber"))
          .willReturn(okJson(jsonAsString))
      )

      connector.getMgdTradeDetails(mgdRegNumber).futureValue mustBe mgdTradeDetails
    }

    "return NotFound when backend returns UpstreamErrorResponse" in {

      wireMockServer.stubFor(
        get(urlEqualTo(s"/gambling/mgd-and-trade-details/mgd/$mgdRegNumber"))
          .willReturn(aResponse().withStatus(404))
      )

      recoverToSucceededIf[UpstreamErrorResponse] {
        connector.getMgdTradeDetails(mgdRegNumber)
      }
    }

  }

  "GamblingConnector.getCorrespondenceDetails" should {

    "return correspondenceDetails when backend returns 200" in {

      val jsonAsString: String =
        s"""{
           |  "mgdRegNumber": "XWM00000001770",
           |  "nameLine1": "ABC ltd",
           |  "nameLine2": "XX" ,
           |  "phoneNumber" : "0123456789",
           |  "mobilePhoneNumber" : "0123456780",
           |  "faxNumber" : "0123456799",
           |  "emailAddr" : "abc@email.com",
           |  "adi": "Upstairs",
           |  "address1" : "add1",
           |  "address2" : "add2",
           |  "address3" : "add3",
           |  "address4" : "add4",
           |  "postcode" : "NE11NE",
           |  "country"   : "UK",
           |  "iomOrCiFlag" : "true",
           |  "systemDate" : "2026-06-26"
           |}""".stripMargin

      wireMockServer.stubFor(
        get(urlEqualTo(s"/gambling/correspondence-details/mgd/$mgdRegNumber"))
          .willReturn(okJson(jsonAsString))
      )

      connector.getCorrespondenceDetails(mgdRegNumber).futureValue mustBe correspondenceDetails
    }

    "return NotFound when backend returns UpstreamErrorResponse" in {

      wireMockServer.stubFor(
        get(urlEqualTo(s"/gambling/correspondence-details/mgd/$mgdRegNumber"))
          .willReturn(aResponse().withStatus(404))
      )

      recoverToSucceededIf[UpstreamErrorResponse] {
        connector.getCorrespondenceDetails(mgdRegNumber)
      }
    }

  }

  "GamblingConnector.getBusinessAddress" should {

    "return businessAddress when backend returns 200" in {

      val jsonAsString: String =
        s"""{
           |  "mgdRegNumber": "XRM00000000574",
           |  "adi": "1st floor",
           |  "address1": "address1",
           |  "address2": "address2",
           |  "address3": "address3",
           |  "address4": "address4",
           |  "postcode": "L1 8YL",
           |  "country": "England",
           |  "iomOrCiFlag": "FALSE",
           |  "systemDate": "2026-05-31"
           |}""".stripMargin

      wireMockServer.stubFor(
        get(urlEqualTo(s"/gambling/business-address/mgd/$mgdRegNumber"))
          .willReturn(okJson(jsonAsString))
      )

      connector.getBusinessAddress(mgdRegNumber).futureValue mustBe businessAddress
    }

    "return UpstreamErrorResponse when backend returns 404" in {

      wireMockServer.stubFor(
        get(urlEqualTo(s"/gambling/business-address/mgd/$mgdRegNumber"))
          .willReturn(aResponse().withStatus(404))
      )

      recoverToSucceededIf[UpstreamErrorResponse] {
        connector.getBusinessAddress(mgdRegNumber)
      }
    }

    "return UpstreamErrorResponse when backend returns 500" in {

      wireMockServer.stubFor(
        get(urlEqualTo(s"/gambling/business-address/mgd/$mgdRegNumber"))
          .willReturn(serverError())
      )

      recoverToSucceededIf[UpstreamErrorResponse] {
        connector.getBusinessAddress(mgdRegNumber)
      }
    }

  }

  "GamblingConnector.getLicencesAndPremises" should {

    "return LicencesAndPremises when backend returns 200" in {

      val jsonAsString: String =
        s"""{
           |  "mgdRegNumber": "XEM00000001335",
           |  "haveGamblingLicenceNo": "1",
           |  "gamblingLicenceNo": "123-456789-A-123456-789",
           |  "heldByLandlord": "1",
           |  "localAuthority": "1",
           |  "familyEntertainment": "1",
           |  "clubGaming": "0",
           |  "clubLicence": "1",
           |  "prizeGaming": "0",
           |  "onPremises": "1",
           |  "clubPremises": "0",
           |  "regCert": "0",
           |  "bookmaking": "0",
           |  "bingo": "0",
           |  "amusement": "0",
           |  "serveAlcohol": "0",
           |  "premisesNotCovered": "0",
           |  "premisesDetails": {
           |    "totalRows": 2,
           |    "premises": [
           |      {
           |        "mgdRegNumber": "XGM00000001762",
           |        "address1": "123 Road",
           |        "address2": "Somewhere",
           |        "address3": "A Place",
           |        "address4": "Earth",
           |        "postcode": "SM12 0NL",
           |        "systemDate": ${LocalDate.of(2023, 4, 1)}
           |      },
           |      {
           |        "mgdRegNumber": "XGM00000001763",
           |        "address1": "345 Road",
           |        "address2": "SomewhereElse",
           |        "address3": "ANOTHERPlace",
           |        "address4": "Earth II",
           |        "postcode": "SM12 1MO",
           |        "systemDate": ${LocalDate.of(2023,4,1)}
           |      }
           |    ]
           |  }
           |}""".stripMargin

      wireMockServer.stubFor(
        get(urlEqualTo(s"/gambling/licences-and-premises-details/mgd/$mgdRegNumber"))
          .willReturn(okJson(jsonAsString))
      )

      connector.getLicencesAndPremises(mgdRegNumber).futureValue mustBe licencesAndPremisesResponse
    }

    "return UpstreamErrorResponse when backend returns 404" in {

      wireMockServer.stubFor(
        get(urlEqualTo(s"/gambling/licences-and-premises-details/mgd/$mgdRegNumber"))
          .willReturn(aResponse().withStatus(404))
      )

      recoverToSucceededIf[UpstreamErrorResponse] {
        connector.getLicencesAndPremises(mgdRegNumber)
      }
    }

    "return UpstreamErrorResponse when backend returns 500" in {

      wireMockServer.stubFor(
        get(urlEqualTo(s"/gambling/licences-and-premises-details/mgd/$mgdRegNumber"))
          .willReturn(serverError())
      )

      recoverToSucceededIf[UpstreamErrorResponse] {
        connector.getLicencesAndPremises(mgdRegNumber)
      }
    }

  }
}

object GamblingConnectorISpec {

  private val mgdRegNumber = "XWM00000001762"

  val businessContactDetails: BusinessContactDetails = BusinessContactDetails(
    mgdRegNumber      = "ABC12345678901",
    phoneNumber       = Some("+44 8903928171"),
    mobilePhoneNumber = Some("+44 8903928171"),
    faxNumber         = Some("+_+_ hdj39783"),
    emailAddr         = Some("a@b.com"),
    systemDate        = Some(LocalDate.of(1991, 1, 1))
  )

  val businessName: BusinessNameDetails = BusinessNameDetails(
    mgdRegNum    = "ABC12345678901",
    businessName = "Test Business Ltd",
    businessType = Unincorporatedbody,
    tradingName  = Some("Trading Name"),
    systemDate   = Some(LocalDate.of(1991, 1, 1))
  )

  val mgdTradeDetails: MgdTradeDetails = MgdTradeDetails(
    mgdRegNumber         = "XRM00000000574",
    isBusinessSeasonal   = Some(true),
    businessTradeClass   = Some(BusinessTradeClass.Casino),
    businessActivityDesc = Some("Description"),
    previousMgdRegistrationNumbers = Some(
      Seq(
        "XWM00000001774",
        "XDM00000001309",
        ""
      )
    ),
    associatedMgdRegistrationNumbers = Some(
      Seq(
        "XXM00000000723",
        "XQM00000001196",
        ""
      )
    ),
    systemDate = Some(LocalDate.of(2026, 5, 31))
  )

  val businessDetails: BusinessDetails =
    BusinessDetails(
      mgdRegNumber          = mgdRegNumber,
      businessType          = Some(Unincorporatedbody),
      currentlyRegistered   = 1,
      groupReg              = false,
      dateOfRegistration    = Some(LocalDate.of(2020, 1, 1)),
      businessPartnerNumber = Some("XB1234567890"),
      systemDate            = LocalDate.of(2026, 1, 1)
    )

  val certificate: MgdCertificate =
    MgdCertificate(
      mgdRegNumber         = mgdRegNumber,
      registrationDate     = None,
      individualName       = None,
      businessName         = Some("Test Business Ltd"),
      tradingName          = Some("Test Trader Ltd"),
      repMemName           = None,
      busAddrLine1         = None,
      busAddrLine2         = None,
      busAddrLine3         = None,
      busAddrLine4         = None,
      busPostcode          = None,
      busCountry           = None,
      busAdi               = None,
      repMemLine1          = None,
      repMemLine2          = None,
      repMemLine3          = None,
      repMemLine4          = None,
      repMemPostcode       = None,
      repMemAdi            = None,
      typeOfBusiness       = None, // important: matches controller
      businessTradeClass   = None,
      noOfPartners         = None,
      groupReg             = false,
      noOfGroupMems        = None,
      dateCertIssued       = None,
      partMembers          = Seq.empty,
      groupMembers         = Seq.empty,
      returnPeriodEndDates = Seq.empty
    )

  val correspondenceDetails: CorrespondenceDetails = CorrespondenceDetails(
    mgdRegNumber = "XWM00000001770",
    nameLine1    = Some("ABC ltd"),
    nameLine2    = Some("XX"),
    correspondenceAddress = Some(
      Address(
        "add1",
        Some("add2"),
        Some("add3"),
        Some("add4"),
        Some("NE11NE"),
        Some("UK")
      )
    ),
    additionalInformation = Some("Upstairs"),
    iomOrCiFlag           = Some("true"),
    contactNumber         = Some(ContactNumber(Some("0123456789"), Some("0123456780"))),
    faxNumber             = Some("0123456799"),
    emailAddr             = Some("abc@email.com")
  )

  val businessAddress: BusinessAddress = BusinessAddress(
    mgdRegNumber = "XRM00000000574",
    adi          = Some("1st floor"),
    address = Some(
      Address(
        "address1",
        Some("address2"),
        Some("address3"),
        Some("address4"),
        Some("L1 8YL"),
        Some("England")
      )
    ),
    iomOrCiFlag = Some("FALSE")
  )

  val partnersDetailsBusinessName = PartnersDetails(
    partners = Seq(
      PartnerDetails(
        mgdRegNumber           = "XWM00000001762",
        dateOfJoining          = Some(LocalDate.parse("2022-01-15")),
        dateOfLeaving          = Some(LocalDate.parse("2028-12-31")),
        solePropTitle          = None,
        solePropFirstName      = None,
        solePropMiddleName     = None,
        solePropLastName       = None,
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
    ),
    systemDate = Some(LocalDate.of(2026, 7, 30))
  )

  val premisesDetails: Seq[PremisesDetails] = Seq(
    PremisesDetails(
      mgdRegNumber = "XGM00000001762",
      address1     = Some("123 Road"),
      address2     = Some("Somewhere"),
      address3     = Some("A Place"),
      address4     = Some("Earth"),
      postcode     = Some("SM12 0NL"),
      systemDate   = Some(LocalDate.of(2023, 4, 1))
    ),
    PremisesDetails(
      mgdRegNumber = "XGM00000001763",
      address1     = Some("345 Road"),
      address2     = Some("SomewhereElse"),
      address3     = Some("ANOTHERPlace"),
      address4     = Some("Earth II"),
      postcode     = Some("SM12 1MO"),
      systemDate   = Some(LocalDate.of(2023, 4, 1))
    )
  )

  val premisesDetailsResponse: PremisesDetailsResponse = PremisesDetailsResponse(
    totalRows = Some(2),
    premises  = premisesDetails
  )

  val licencesAndPremisesResponse: LicencesAndPremises = LicencesAndPremises(
    mgdRegNumber          = mgdRegNumber,
    haveGamblingLicenceNo = Some("1"),
    gamblingLicenceNo     = Some("123-456789-A-123456-789"),
    heldByLandlord        = Some("1"),
    localAuthority        = Some("1"),
    familyEntertainment   = Some("1"),
    clubGaming            = Some("0"),
    clubLicence           = Some("1"),
    prizeGaming           = Some("0"),
    onPremises            = Some("1"),
    clubPremises          = Some("0"),
    regCert               = Some("0"),
    bookmaking            = Some("0"),
    bingo                 = Some("0"),
    amusement             = Some("0"),
    serveAlcohol          = Some("0"),
    premisesNotCovered    = Some("0"),
    premisesDetails       = Some(premisesDetailsResponse),
  )
}
