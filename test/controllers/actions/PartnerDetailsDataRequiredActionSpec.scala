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

package controllers.actions

import base.SpecBase
import connectors.GamblingConnector
import controllers.businesscontactdetails.LoadedPartnerDetails
import models.requests.{DataRequest, OptionalDataRequest}
import models.{Address, BusinessPartnerDetails, BusinessType, ContactNumber, CorrespondenceDetails, PartnersDetails, SoleProprietorName, SoleProprietorPartnerDetails, UserAnswers}
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import pages.partnerdetails.{ExistingPartnerDetailsSection, NewPartnerDetailsSection}
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.libs.json.Json
import play.api.mvc.Results.*
import play.api.mvc.{AnyContent, Result}
import play.api.test.FakeRequest
import repositories.SessionRepository
import uk.gov.hmrc.http.UpstreamErrorResponse

import java.time.{Clock, Instant, LocalDate, ZoneOffset}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.postfixOps

class PartnerDetailsDataRequiredActionSpec extends SpecBase with MockitoSugar {

  import PartnerDetailsDataRequiredActionSpec.*

  class Harness(sessionRepository: SessionRepository, gamblingConnector: GamblingConnector)
      extends PartnerDetailsDataRequiredActionImpl(sessionRepository, gamblingConnector, clock) {
    def callRefine[A](request: OptionalDataRequest[A]): Future[Either[Result, DataRequest[A]]] = refine(request)
  }

  "BusinessAddress DataRequiredAction" - {

    "when there is no User Answers in the cache" - {

      "return the request with a populated User Answers with data from the backend" - {
        "when the is data in cache" in {

          val request = FakeRequest()
          val sessionRepository = mock[SessionRepository]
          val gamblingConnector = mock[GamblingConnector]
          when(sessionRepository.set(any())) thenReturn Future(true)
          when(gamblingConnector.getPartnersDetails(any())(any())) thenReturn Future(partnersDetailsBusinessName)
          val action = new Harness(sessionRepository, gamblingConnector)

          val data = Json.obj(
            "partnersSection" -> Json.obj(
              "0100049899" -> Json.obj(
                "partnerDetailsMgdRegNumber"          -> "XWM00000001762",
                "partnerDetailsBusinessPartnerNumber" -> "0100049899",
                "partnerDetailsDateOfJoining"         -> "2022-01-15",
                "partnerDetailsDateOfLeaving"         -> "2028-12-31",
                "partnerDetailsCorrespondenceDetailsSection" -> Json.obj(
                  "mgdRegNumber" -> "XWM00000001762",
                  "correspondenceAddress" -> Json.obj(
                    "address1" -> "123 High Street",
                    "address2" -> "Suite 4",
                    "address3" -> "Business Park",
                    "address4" -> "London",
                    "postcode" -> "SW1A 1AA",
                    "country"  -> "GB"
                  ),
                  "additionalInformation" -> "ADI123456",
                  "iomOrCiFlag"           -> "N",
                  "contactNumber" -> Json.obj(
                    "phoneNumber"       -> "02071234567",
                    "mobilePhoneNumber" -> "07700123456"
                  ),
                  "faxNumber" -> "02071234568",
                  "emailAddr" -> "john.doe@example.com"
                ),
                "partnerDetailsDateOfIncorporation"    -> "2020-03-01",
                "partnerDetailsCountryOfIncorporation" -> "GB",
                "partnerDetailsBusinessName"           -> "XYZ Consulting Ltd",
                "partnerDetailsTradingName"            -> "XYZ Consulting",
                "partnerDetailsDateOfBirth"            -> "1985-06-20",
                "partnerDetailsNino"                   -> "AB123456C",
                "partnerDetailsUtr"                    -> "1234567890",
                "partnerDetailsVrn"                    -> "GB123456789",
                "partnerDetailsCrn"                    -> "09876543",
                "partnerDetailsForeignCorporateRef"    -> "FCR-987654",
                "partnerDetailsIsFutureLeaveDate"      -> false,
                "partnerDetailsIsFutureJoinDate"       -> false,
                "partnerDetailsBusinessType"           -> 2
              )
            )
          )

          val result: Either[Result, DataRequest[AnyContent]] =
            action.callRefine(OptionalDataRequest(request, mgdRegNum, None)).futureValue

          val expected = DataRequest(request, mgdRegNum, UserAnswers(mgdRegNum, data))

          result.map { req =>
            req.request mustBe expected.request
            req.userAnswers.data mustBe expected.userAnswers.data
            req.userAnswers.id mustBe expected.userAnswers.id
          }
          verify(sessionRepository, times(1)).set(any())
          verify(gamblingConnector, times(1)).getPartnersDetails(any())(any())
        }

      }

      "redirect to SystemError " - {
        "User Answers cannot be saved" in {
          val request = FakeRequest()
          val sessionRepository = mock[SessionRepository]
          val gamblingConnector = mock[GamblingConnector]
          when(sessionRepository.set(any())) thenReturn Future(false)
          when(gamblingConnector.getPartnersDetails(any())(any())) thenReturn Future(partnersDetailsBusinessName)
          val action = new Harness(sessionRepository, gamblingConnector)

          val result: Either[Result, DataRequest[AnyContent]] =
            action.callRefine(OptionalDataRequest(request, mgdRegNum, None)).futureValue

          result mustBe Left(Redirect(controllers.routes.SystemErrorController.onPageLoad()))
          verify(sessionRepository, times(1)).set(any())
          verify(gamblingConnector, times(1)).getPartnersDetails(any())(any())
        }

        "getParternsDetails throws an exception" in {

          val request = FakeRequest()
          val sessionRepository = mock[SessionRepository]
          val gamblingConnector = mock[GamblingConnector]
          when(sessionRepository.set(any())) thenReturn Future(false)
          when(gamblingConnector.getPartnersDetails(any())(any())) thenReturn Future.failed(UpstreamErrorResponse("Fail", INTERNAL_SERVER_ERROR))
          val action = new Harness(sessionRepository, gamblingConnector)

          val result: Either[Result, DataRequest[AnyContent]] =
            action.callRefine(OptionalDataRequest(request, mgdRegNum, None)).futureValue

          result mustBe Left(Redirect(controllers.routes.SystemErrorController.onPageLoad()))

        }
      }

    }

    "when there are User Answers in the cache" - {

      "return the request with a populated User Answers" - {
        "without call to backend" in {

          val request = FakeRequest()
          val sessionRepository = mock[SessionRepository]
          val gamblingConnector = mock[GamblingConnector]
          val action = new Harness(sessionRepository, gamblingConnector)

          val minimalData = Json.obj(
            "partnersSection" -> Json.obj(
              "0100049899" -> Json.obj(
                "partnerDetailsMgdRegNumber" -> "XWM00000001762"
              )
            ),
            "systemDate" -> "2026-07-30"
          )

          val minimalDataWithSections = LoadedPartnerDetails.apply(mgdRegNum, minimalData, ExistingPartnerDetailsSection, NewPartnerDetailsSection)

          val result: Either[Result, DataRequest[AnyContent]] =
            action.callRefine(OptionalDataRequest(request, mgdRegNum, Some(minimalDataWithSections))).futureValue

          val expected = DataRequest(request, mgdRegNum, minimalDataWithSections)

          result.map { req =>
            req.request mustBe expected.request
            req.userAnswers.data mustBe expected.userAnswers.data
            req.userAnswers.id mustBe expected.userAnswers.id
          }
          verify(sessionRepository, never).set(any())
          verify(gamblingConnector, never).getPartnersDetails(any())(any())

        }

        "with call to backend" in {

          val request = FakeRequest()
          val sessionRepository = mock[SessionRepository]
          val gamblingConnector = mock[GamblingConnector]
          when(sessionRepository.set(any())) thenReturn Future(true)
          when(gamblingConnector.getPartnersDetails(any())(any())) thenReturn Future(partnersDetailsBusinessName)
          val action = new Harness(sessionRepository, gamblingConnector)

          val sentSessionData = Json.obj(
            // No partners = has to call backend
            "systemDate" -> "2026-07-30"
          )

          val expectedResponse = Json.obj(
            "partnersSection" -> Json.obj(
              "0100049899" -> Json.obj(
                "partnerDetailsMgdRegNumber"          -> "XWM00000001762",
                "partnerDetailsBusinessPartnerNumber" -> "0100049899",
                "partnerDetailsDateOfJoining"         -> "2022-01-15",
                "partnerDetailsDateOfLeaving"         -> "2028-12-31",
                "partnerDetailsCorrespondenceDetailsSection" -> Json.obj(
                  "mgdRegNumber" -> "XWM00000001762",
                  "correspondenceAddress" -> Json.obj(
                    "address1" -> "123 High Street",
                    "address2" -> "Suite 4",
                    "address3" -> "Business Park",
                    "address4" -> "London",
                    "postcode" -> "SW1A 1AA",
                    "country"  -> "GB"
                  ),
                  "additionalInformation" -> "ADI123456",
                  "iomOrCiFlag"           -> "N",
                  "contactNumber" -> Json.obj(
                    "phoneNumber"       -> "02071234567",
                    "mobilePhoneNumber" -> "07700123456"
                  ),
                  "faxNumber" -> "02071234568",
                  "emailAddr" -> "john.doe@example.com"
                ),
                "partnerDetailsDateOfIncorporation"    -> "2020-03-01",
                "partnerDetailsCountryOfIncorporation" -> "GB",
                "partnerDetailsBusinessName"           -> "XYZ Consulting Ltd",
                "partnerDetailsTradingName"            -> "XYZ Consulting",
                "partnerDetailsDateOfBirth"            -> "1985-06-20",
                "partnerDetailsNino"                   -> "AB123456C",
                "partnerDetailsUtr"                    -> "1234567890",
                "partnerDetailsVrn"                    -> "GB123456789",
                "partnerDetailsCrn"                    -> "09876543",
                "partnerDetailsForeignCorporateRef"    -> "FCR-987654",
                "partnerDetailsIsFutureLeaveDate"      -> false,
                "partnerDetailsIsFutureJoinDate"       -> false,
                "partnerDetailsBusinessType"           -> 2
              )
            ),
            "systemDate" -> "2026-07-30"
          )

          val existingUserAnswers = UserAnswers(mgdRegNum, sentSessionData)

          val result: Either[Result, DataRequest[AnyContent]] =
            action.callRefine(OptionalDataRequest(request, mgdRegNum, Some(existingUserAnswers))).futureValue

          val expected = DataRequest(request, mgdRegNum, UserAnswers(mgdRegNum, expectedResponse))

          result.map { req =>
            req.request mustBe expected.request
            req.userAnswers.data mustBe expected.userAnswers.data
            req.userAnswers.id mustBe expected.userAnswers.id
          }
          verify(sessionRepository, times(1)).set(any())
          verify(gamblingConnector, times(1)).getPartnersDetails(any())(any())

        }
      }
    }

    "when setBusinessDetails checks BusinessType" - {

      "return User Answers with SoleProprietor details where BusinessType is 1" in {
        val request = FakeRequest()
        val sessionRepository = mock[SessionRepository]
        val gamblingConnector = mock[GamblingConnector]
        when(sessionRepository.set(any())) thenReturn Future(true)
        when(gamblingConnector.getPartnersDetails(any())(any())) thenReturn Future(partnersDetailsSoleProprietor)
        val action = new Harness(sessionRepository, gamblingConnector)

        val data = Json.obj(
          "partnersSection" -> Json.obj(
            "0100049899" -> Json.obj(
              "partnerDetailsMgdRegNumber"          -> "XWM00000001762",
              "partnerDetailsBusinessPartnerNumber" -> "0100049899",
              "partnerDetailsDateOfJoining"         -> "2022-01-15",
              "partnerDetailsDateOfLeaving"         -> "2028-12-31",
              "partnerDetailsCorrespondenceDetailsSection" -> Json.obj(
                "mgdRegNumber" -> "XWM00000001762",
                "correspondenceAddress" -> Json.obj(
                  "address1" -> "123 High Street",
                  "address2" -> "Suite 4",
                  "address3" -> "Business Park",
                  "address4" -> "London",
                  "postcode" -> "SW1A 1AA",
                  "country"  -> "GB"
                ),
                "additionalInformation" -> "ADI123456",
                "iomOrCiFlag"           -> "N",
                "contactNumber" -> Json.obj(
                  "phoneNumber"       -> "02071234567",
                  "mobilePhoneNumber" -> "07700123456"
                ),
                "faxNumber" -> "02071234568",
                "emailAddr" -> "john.doe@example.com"
              ),
              "partnerDetailsSoleProprietor" -> Json.obj(
                "title"      -> "Mr",
                "firstName"  -> "John",
                "middleName" -> "Michael",
                "lastName"   -> "Doe"
              ),
              "partnerDetailsDateOfIncorporation"    -> "2020-03-01",
              "partnerDetailsCountryOfIncorporation" -> "GB",
              "partnerDetailsTradingName"            -> "XYZ Consulting",
              "partnerDetailsDateOfBirth"            -> "1985-06-20",
              "partnerDetailsNino"                   -> "AB123456C",
              "partnerDetailsUtr"                    -> "1234567890",
              "partnerDetailsVrn"                    -> "GB123456789",
              "partnerDetailsCrn"                    -> "09876543",
              "partnerDetailsForeignCorporateRef"    -> "FCR-987654",
              "partnerDetailsIsFutureLeaveDate"      -> false,
              "partnerDetailsIsFutureJoinDate"       -> false,
              "partnerDetailsBusinessType"           -> 1
            )
          )
        )

        val result: Either[Result, DataRequest[AnyContent]] =
          action.callRefine(OptionalDataRequest(request, mgdRegNum, None)).futureValue

        val expected = DataRequest(request, mgdRegNum, UserAnswers(mgdRegNum, data))

        result.map { req =>
          req.request mustBe expected.request
          req.userAnswers.data mustBe expected.userAnswers.data
          req.userAnswers.id mustBe expected.userAnswers.id
        }
        verify(sessionRepository, times(1)).set(any())
        verify(gamblingConnector, times(1)).getPartnersDetails(any())(any())
      }

      "return User Answers with SoleProprietor details where BusinessType is 1 and middleName is missing" in {
        val request = FakeRequest()
        val sessionRepository = mock[SessionRepository]
        val gamblingConnector = mock[GamblingConnector]
        when(sessionRepository.set(any())) thenReturn Future(true)
        when(gamblingConnector.getPartnersDetails(any())(any())) thenReturn Future(
          partnersDetailsSoleProprietor.copy(
            partners = partnersDetailsSoleProprietor.partners.map(e =>
              e.asInstanceOf[SoleProprietorPartnerDetails]
                .copy(
                  soleProprietorName = e.asInstanceOf[SoleProprietorPartnerDetails].soleProprietorName.copy(middleName = None)
                )
            )
          )
        )
        val action = new Harness(sessionRepository, gamblingConnector)

        val data = Json.obj(
          "partnersSection" -> Json.obj(
            "0100049899" -> Json.obj(
              "partnerDetailsMgdRegNumber"          -> "XWM00000001762",
              "partnerDetailsBusinessPartnerNumber" -> "0100049899",
              "partnerDetailsDateOfJoining"         -> "2022-01-15",
              "partnerDetailsDateOfLeaving"         -> "2028-12-31",
              "partnerDetailsCorrespondenceDetailsSection" -> Json.obj(
                "mgdRegNumber" -> "XWM00000001762",
                "correspondenceAddress" -> Json.obj(
                  "address1" -> "123 High Street",
                  "address2" -> "Suite 4",
                  "address3" -> "Business Park",
                  "address4" -> "London",
                  "postcode" -> "SW1A 1AA",
                  "country"  -> "GB"
                ),
                "additionalInformation" -> "ADI123456",
                "iomOrCiFlag"           -> "N",
                "contactNumber" -> Json.obj(
                  "phoneNumber"       -> "02071234567",
                  "mobilePhoneNumber" -> "07700123456"
                ),
                "faxNumber" -> "02071234568",
                "emailAddr" -> "john.doe@example.com"
              ),
              "partnerDetailsSoleProprietor" -> Json.obj(
                "title"     -> "Mr",
                "firstName" -> "John",
                "lastName"  -> "Doe"
              ),
              "partnerDetailsDateOfIncorporation"    -> "2020-03-01",
              "partnerDetailsCountryOfIncorporation" -> "GB",
              "partnerDetailsTradingName"            -> "XYZ Consulting",
              "partnerDetailsDateOfBirth"            -> "1985-06-20",
              "partnerDetailsNino"                   -> "AB123456C",
              "partnerDetailsUtr"                    -> "1234567890",
              "partnerDetailsVrn"                    -> "GB123456789",
              "partnerDetailsCrn"                    -> "09876543",
              "partnerDetailsForeignCorporateRef"    -> "FCR-987654",
              "partnerDetailsIsFutureLeaveDate"      -> false,
              "partnerDetailsIsFutureJoinDate"       -> false,
              "partnerDetailsBusinessType"           -> 1
            )
          )
        )

        val result: Either[Result, DataRequest[AnyContent]] =
          action.callRefine(OptionalDataRequest(request, mgdRegNum, None)).futureValue

        val expected = DataRequest(request, mgdRegNum, UserAnswers(mgdRegNum, data))

        result.map { req =>
          req.request mustBe expected.request
          req.userAnswers.data mustBe expected.userAnswers.data
          req.userAnswers.id mustBe expected.userAnswers.id
        }
        verify(sessionRepository, times(1)).set(any())
        verify(gamblingConnector, times(1)).getPartnersDetails(any())(any())
      }

      "return User Answers with Business Name where BusinessType is NOT 1" in {
        val request = FakeRequest()
        val sessionRepository = mock[SessionRepository]
        val gamblingConnector = mock[GamblingConnector]
        when(sessionRepository.set(any())) thenReturn Future(true)
        when(gamblingConnector.getPartnersDetails(any())(any())) thenReturn Future(partnersDetailsBusinessName)
        val action = new Harness(sessionRepository, gamblingConnector)

        val data = Json.obj(
          "partnersSection" -> Json.obj(
            "0100049899" -> Json.obj(
              "partnerDetailsMgdRegNumber"          -> "XWM00000001762",
              "partnerDetailsBusinessPartnerNumber" -> "0100049899",
              "partnerDetailsDateOfJoining"         -> "2022-01-15",
              "partnerDetailsDateOfLeaving"         -> "2028-12-31",
              "partnerDetailsCorrespondenceDetailsSection" -> Json.obj(
                "mgdRegNumber" -> "XWM00000001762",
                "correspondenceAddress" -> Json.obj(
                  "address1" -> "123 High Street",
                  "address2" -> "Suite 4",
                  "address3" -> "Business Park",
                  "address4" -> "London",
                  "postcode" -> "SW1A 1AA",
                  "country"  -> "GB"
                ),
                "additionalInformation" -> "ADI123456",
                "iomOrCiFlag"           -> "N",
                "contactNumber" -> Json.obj(
                  "phoneNumber"       -> "02071234567",
                  "mobilePhoneNumber" -> "07700123456"
                ),
                "faxNumber" -> "02071234568",
                "emailAddr" -> "john.doe@example.com"
              ),
              "partnerDetailsBusinessName"           -> "XYZ Consulting Ltd",
              "partnerDetailsDateOfIncorporation"    -> "2020-03-01",
              "partnerDetailsCountryOfIncorporation" -> "GB",
              "partnerDetailsTradingName"            -> "XYZ Consulting",
              "partnerDetailsDateOfBirth"            -> "1985-06-20",
              "partnerDetailsNino"                   -> "AB123456C",
              "partnerDetailsUtr"                    -> "1234567890",
              "partnerDetailsVrn"                    -> "GB123456789",
              "partnerDetailsCrn"                    -> "09876543",
              "partnerDetailsForeignCorporateRef"    -> "FCR-987654",
              "partnerDetailsIsFutureLeaveDate"      -> false,
              "partnerDetailsIsFutureJoinDate"       -> false,
              "partnerDetailsBusinessType"           -> 2
            )
          )
        )

        val result: Either[Result, DataRequest[AnyContent]] =
          action.callRefine(OptionalDataRequest(request, mgdRegNum, None)).futureValue

        val expected = DataRequest(request, mgdRegNum, UserAnswers(mgdRegNum, data))

        result.map { req =>
          req.request mustBe expected.request
          req.userAnswers.data mustBe expected.userAnswers.data
          req.userAnswers.id mustBe expected.userAnswers.id
        }
        verify(sessionRepository, times(1)).set(any())
        verify(gamblingConnector, times(1)).getPartnersDetails(any())(any())
      }

      partnersDetailsSoleProprietorMissingData.foreach { case (propName, partnersDetailsMissingData) =>
        s"return User Answers WITHOUT SoleProprietor details where BusinessType is 1 and SoleProp.$propName is missing" in {
          val request = FakeRequest()
          val sessionRepository = mock[SessionRepository]
          val gamblingConnector = mock[GamblingConnector]
          when(sessionRepository.set(any())) thenReturn Future(true)
          when(gamblingConnector.getPartnersDetails(any())(any())) thenReturn Future(partnersDetailsMissingData)
          val action = new Harness(sessionRepository, gamblingConnector)

          val data = Json.obj(
            "partnersSection" -> Json.obj(
              "0100049899" -> Json.obj(
                "partnerDetailsMgdRegNumber"          -> "XWM00000001762",
                "partnerDetailsBusinessPartnerNumber" -> "0100049899",
                "partnerDetailsDateOfJoining"         -> "2022-01-15",
                "partnerDetailsDateOfLeaving"         -> "2028-12-31",
                "partnerDetailsCorrespondenceDetailsSection" -> Json.obj(
                  "mgdRegNumber" -> "XWM00000001762",
                  "correspondenceAddress" -> Json.obj(
                    "address1" -> "123 High Street",
                    "address2" -> "Suite 4",
                    "address3" -> "Business Park",
                    "address4" -> "London",
                    "postcode" -> "SW1A 1AA",
                    "country"  -> "GB"
                  ),
                  "additionalInformation" -> "ADI123456",
                  "iomOrCiFlag"           -> "N",
                  "contactNumber" -> Json.obj(
                    "phoneNumber"       -> "02071234567",
                    "mobilePhoneNumber" -> "07700123456"
                  ),
                  "faxNumber" -> "02071234568",
                  "emailAddr" -> "john.doe@example.com"
                ),
                "partnerDetailsSoleProprietor" -> Json.obj(
                  "title"      -> (if propName != "title" then "Mr" else ""),
                  "firstName"  -> (if propName != "firstName" then "John" else ""),
                  "middleName" -> "Michael",
                  "lastName"   -> (if propName != "lastName" then "Doe" else "")
                ),
                "partnerDetailsDateOfIncorporation"    -> "2020-03-01",
                "partnerDetailsCountryOfIncorporation" -> "GB",
                "partnerDetailsTradingName"            -> "XYZ Consulting",
                "partnerDetailsDateOfBirth"            -> "1985-06-20",
                "partnerDetailsNino"                   -> "AB123456C",
                "partnerDetailsUtr"                    -> "1234567890",
                "partnerDetailsVrn"                    -> "GB123456789",
                "partnerDetailsCrn"                    -> "09876543",
                "partnerDetailsForeignCorporateRef"    -> "FCR-987654",
                "partnerDetailsIsFutureLeaveDate"      -> false,
                "partnerDetailsIsFutureJoinDate"       -> false,
                "partnerDetailsBusinessType"           -> 1
              )
            )
          )

          val result: Either[Result, DataRequest[AnyContent]] =
            action.callRefine(OptionalDataRequest(request, mgdRegNum, None)).futureValue

          val expected = DataRequest(request, mgdRegNum, UserAnswers(mgdRegNum, data))

          result.map { req =>
            req.request mustBe expected.request
            req.userAnswers.data mustBe expected.userAnswers.data
            req.userAnswers.id mustBe expected.userAnswers.id
          }
          verify(sessionRepository, times(1)).set(any())
          verify(gamblingConnector, times(1)).getPartnersDetails(any())(any())
        }
      }
    }

  }
}

object PartnerDetailsDataRequiredActionSpec {

  val now: Instant = Instant.parse("2026-11-02T09:00:00Z")
  val clock: Clock = Clock.fixed(now, ZoneOffset.UTC)

  val partnerDetailsBusiness = BusinessPartnerDetails(
    mgdRegNumber           = "XWM00000001762",
    businessPartnerNumber  = "0100049899",
    dateOfJoining          = Some(LocalDate.parse("2022-01-15")),
    dateOfLeaving          = Some(LocalDate.parse("2028-12-31")),
    businessName           = "XYZ Consulting Ltd",
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
    businessType      = BusinessType.Corporatebody // NOT 1
  )

  val partnerDetailsSoleProprietor = SoleProprietorPartnerDetails(
    mgdRegNumber          = "XWM00000001762",
    businessPartnerNumber = "0100049899",
    dateOfJoining         = Some(LocalDate.parse("2022-01-15")),
    dateOfLeaving         = Some(LocalDate.parse("2028-12-31")),
    soleProprietorName = SoleProprietorName(
      title      = "Mr",
      firstName  = "John",
      middleName = Some("Michael"),
      lastName   = "Doe"
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
    businessType      = BusinessType.Soleproprietor // Explicitly 1
  )

  val partnersDetailsBusinessName = PartnersDetails(
    partners = Seq(partnerDetailsBusiness)
  )

  val partnersDetailsSoleProprietor = PartnersDetails(
    partners = Seq(partnerDetailsSoleProprietor)
  )

  val partnersDetailsSoleProprietorMissingData: Seq[(String, PartnersDetails)] = Seq(
    ("title",
     partnersDetailsSoleProprietor.copy(partners =
       partnersDetailsSoleProprietor.partners.map(e =>
         e.asInstanceOf[SoleProprietorPartnerDetails]
           .copy(
             soleProprietorName = e.asInstanceOf[SoleProprietorPartnerDetails].soleProprietorName.copy(title = "")
           )
       )
     )
    ),
    ("firstName",
     partnersDetailsSoleProprietor.copy(partners =
       partnersDetailsSoleProprietor.partners.map(e =>
         e.asInstanceOf[SoleProprietorPartnerDetails]
           .copy(
             soleProprietorName = e.asInstanceOf[SoleProprietorPartnerDetails].soleProprietorName.copy(firstName = "")
           )
       )
     )
    ),
    ("lastName",
     partnersDetailsSoleProprietor.copy(partners =
       partnersDetailsSoleProprietor.partners.map(e =>
         e.asInstanceOf[SoleProprietorPartnerDetails]
           .copy(
             soleProprietorName = e.asInstanceOf[SoleProprietorPartnerDetails].soleProprietorName.copy(lastName = "")
           )
       )
     )
    )
  )

}
