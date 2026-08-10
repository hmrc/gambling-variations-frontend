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
import models.requests.{DataRequest, OptionalDataRequest}
import models.{PartnerDetails, PartnersDetails, UserAnswers}
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.libs.json.Json
import play.api.mvc.Results.*
import play.api.mvc.{AnyContent, Result}
import play.api.test.FakeRequest
import repositories.SessionRepository
import uk.gov.hmrc.http.UpstreamErrorResponse

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PartnerDetailsDataRequiredActionSpec extends SpecBase with MockitoSugar {

  import PartnerDetailsDataRequiredActionSpec.*

  class Harness(sessionRepository: SessionRepository, gamblingConnector: GamblingConnector)
      extends PartnerDetailsDataRequiredActionImpl(sessionRepository, gamblingConnector) {
    def callRefine[A](request: OptionalDataRequest[A]): Future[Either[Result, DataRequest[A]]] = refine(request)
  }

  "BusinessAddress DataRequiredAction" - {

    "when there is no User Answers in the cache" - {

      "return the request with a populated User Answers with data from the certificate" - {
        "when the is data in cache" in {

          val request = FakeRequest()
          val sessionRepository = mock[SessionRepository]
          val gamblingConnector = mock[GamblingConnector]
          when(sessionRepository.set(any())) thenReturn Future(true)
          when(gamblingConnector.getPartnersDetails(any())(any())) thenReturn Future(partnersDetails)
          val action = new Harness(sessionRepository, gamblingConnector)

          val data = Json.obj(
            "partners" -> Json.arr(
              Json.obj(
                "partnerDetailsMgdRegNumber"  -> "XWM00000001762",
                "partnerDetailsDateOfJoining" -> "2022-01-15",
                "partnerDetailsDateOfLeaving" -> "2028-12-31",
                "partnerDetailsSoleProprietor" -> Json.obj(
                  "title"      -> "Mr",
                  "firstName"  -> "John",
                  "middleName" -> "Michael",
                  "lastName"   -> "Doe"
                ),
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
                "partnerDetailsIsFutureLeaveDate"      -> 0,
                "partnerDetailsIsFutureJoinDate"       -> 0,
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
          when(gamblingConnector.getPartnersDetails(any())(any())) thenReturn Future(partnersDetails)
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
            "partners" -> Json.arr(
              Json.obj(
                "partnerDetailsMgdRegNumber" -> "XWM00000001762"
              )
            ),
            "systemDate" -> "2026-07-30"
          )

          val result: Either[Result, DataRequest[AnyContent]] =
            action.callRefine(OptionalDataRequest(request, mgdRegNum, Some(UserAnswers(mgdRegNum, minimalData)))).futureValue

          val expected = DataRequest(request, mgdRegNum, UserAnswers(mgdRegNum, minimalData))

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
          when(gamblingConnector.getPartnersDetails(any())(any())) thenReturn Future(partnersDetails)
          val action = new Harness(sessionRepository, gamblingConnector)

          val sentSessionData = Json.obj(
            "partners"   -> Json.arr(),
            "systemDate" -> "2026-07-30"
          )

          val expectedResponse = Json.obj(
            "partners" -> Json.arr(
              Json.obj(
                "partnerDetailsMgdRegNumber"  -> "XWM00000001762",
                "partnerDetailsDateOfJoining" -> "2022-01-15",
                "partnerDetailsDateOfLeaving" -> "2028-12-31",
                "partnerDetailsSoleProprietor" -> Json.obj(
                  "title"      -> "Mr",
                  "firstName"  -> "John",
                  "middleName" -> "Michael",
                  "lastName"   -> "Doe"
                ),
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
                "partnerDetailsIsFutureLeaveDate"      -> 0,
                "partnerDetailsIsFutureJoinDate"       -> 0,
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

  }
}

object PartnerDetailsDataRequiredActionSpec {

  val partnerDetails = PartnerDetails(
    mgdRegNumber           = "XWM00000001762",
    dateOfJoining          = Some(LocalDate.parse("2022-01-15")),
    dateOfLeaving          = Some(LocalDate.parse("2028-12-31")),
    solePropTitle          = Some("Mr"),
    solePropFirstName      = Some("John"),
    solePropMiddleName     = Some("Michael"),
    solePropLastName       = Some("Doe"),
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
    emailAddress           = Some("john.doe@example.com"),
    isFutureLeaveDate      = Some(0),
    isFutureJoinDate       = Some(0),
    businessType           = Some(2)
  )

  val partnersDetails = PartnersDetails(
    partners   = Seq(partnerDetails),
    systemDate = Some(LocalDate.of(2026, 7, 30))
  )

}
