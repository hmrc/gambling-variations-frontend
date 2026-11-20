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

import base.{LoadedSectionDetails, SpecBase}
import connectors.GamblingConnector
import models.requests.{DataRequest, OptionalDataRequest}
import models.{Address, ContactDetails, ContactNumber, UserAnswers}
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import pages.correspondencedetails.CorrespondenceDetailsSection
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.libs.json.Json
import play.api.mvc.Results.*
import play.api.mvc.{AnyContent, Result}
import play.api.test.FakeRequest
import repositories.SessionRepository
import uk.gov.hmrc.http.UpstreamErrorResponse

import java.time.{Clock, Instant, ZoneOffset}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CorrespondenceDetailsDataRequiredActionSpec extends SpecBase with MockitoSugar {

  import CorrespondenceDetailsDataRequiredActionSpec.*
  val now: Instant = Instant.parse("2026-11-18T14:30:00Z")
  val clock: Clock = Clock.fixed(now, ZoneOffset.UTC)

  class Harness(sessionRepository: SessionRepository, gamblingConnector: GamblingConnector)
      extends CorrespondenceDetailsDataRequiredActionImpl(sessionRepository, gamblingConnector, clock) {
    def callRefine[A](request: OptionalDataRequest[A]): Future[Either[Result, DataRequest[A]]] = refine(request)
  }

  "CorrespondenceDetails DataRequiredAction" - {

    "when there is no User Answers in the cache" - {

      "return the request with a populated User Answers with data from the certificate" in {

        val request = FakeRequest()
        val sessionRepository = mock[SessionRepository]
        val gamblingConnector = mock[GamblingConnector]
        when(sessionRepository.set(any())) thenReturn Future(true)
        when(gamblingConnector.getCorrespondenceDetails(any())(any())) thenReturn Future(correspondenceDetailsUk)
        val action = new Harness(sessionRepository, gamblingConnector)
        val data = Json.obj(
          "correspondenceDetailsSection" -> Json.obj(
            "correspondenceName"           -> "ABC ltd",
            "correspondenceAdditionalName" -> "XX",
            "iomOrCiFlag"                  -> true,
            "correspondenceAddressUk" -> Json.obj(
              "address1" -> "add1",
              "address2" -> "add2",
              "address3" -> "add3",
              "address4" -> "add4",
              "postcode" -> "NE11NE",
              "country"  -> "UK"
            ),
            "correspondenceAdditionalInformation" -> "Upstairs",
            "correspondenceContactNumber" -> Json.obj(
              "phoneNumber"       -> "0123456789",
              "mobilePhoneNumber" -> "0123456780"
            ),
            "correspondenceFaxNumber" -> "0123456799",
            "correspondenceEmail"     -> "abc@email.com"
          )
        )

        val result: Either[Result, DataRequest[AnyContent]] =
          action.callRefine(OptionalDataRequest(request, mgdRegNum, None)).futureValue

        val expected = DataRequest(request, mgdRegNum, UserAnswers(mgdRegNum, data))

        val req = result.toOption.value
        req.request mustBe expected.request
        req.userAnswers.data mustBe expected.userAnswers.data
        req.userAnswers.id mustBe expected.userAnswers.id

        // Loaded as original answers, nothing asserted, section marked loaded.
        req.userAnswers.original.data mustBe data
        req.userAnswers.modified.data mustBe Json.obj()
        req.userAnswers.original.loaded mustBe Map(CorrespondenceDetailsSection.key -> now)

        verify(sessionRepository, times(1)).set(any())
        verify(gamblingConnector, times(1)).getCorrespondenceDetails(any())(any())
      }

      "redirect to SystemError " - {
        "User Answers cannot be saved" in {
          val request = FakeRequest()
          val sessionRepository = mock[SessionRepository]
          val gamblingConnector = mock[GamblingConnector]
          when(sessionRepository.set(any())) thenReturn Future(false)
          when(gamblingConnector.getCorrespondenceDetails(any())(any())) thenReturn Future(correspondenceDetailsUk)
          val action = new Harness(sessionRepository, gamblingConnector)

          val result: Either[Result, DataRequest[AnyContent]] =
            action.callRefine(OptionalDataRequest(request, mgdRegNum, None)).futureValue

          result mustBe Left(Redirect(controllers.routes.SystemErrorController.onPageLoad()))
          verify(sessionRepository, times(1)).set(any())
          verify(gamblingConnector, times(1)).getCorrespondenceDetails(any())(any())
        }

        "getCorrespondenceDetails throws an exception" in {

          val request = FakeRequest()
          val sessionRepository = mock[SessionRepository]
          val gamblingConnector = mock[GamblingConnector]
          when(sessionRepository.set(any())) thenReturn Future(false)
          when(gamblingConnector.getCorrespondenceDetails(any())(any())) thenReturn Future.failed(
            UpstreamErrorResponse("Fail", INTERNAL_SERVER_ERROR)
          )
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

          val loaded = LoadedSectionDetails(mgdRegNum, Json.obj(), CorrespondenceDetailsSection)

          val result: Either[Result, DataRequest[AnyContent]] =
            action.callRefine(OptionalDataRequest(request, mgdRegNum, Some(loaded))).futureValue

          val expected = DataRequest(request, mgdRegNum, loaded)

          result.map { req =>
            req.request mustBe expected.request
            req.userAnswers.data mustBe expected.userAnswers.data
            req.userAnswers.id mustBe expected.userAnswers.id
          }
          verify(sessionRepository, never).set(any())
          verify(gamblingConnector, never).getCorrespondenceDetails(any())(any())

        }
        "with call to backend" in {

          val request = FakeRequest()
          val sessionRepository = mock[SessionRepository]
          val gamblingConnector = mock[GamblingConnector]
          when(sessionRepository.set(any())) thenReturn Future(true)
          when(gamblingConnector.getCorrespondenceDetails(any())(any())) thenReturn Future(correspondenceDetailsNonUk)
          val action = new Harness(sessionRepository, gamblingConnector)

          val updatedSessionData = Json.obj(
            "businessNameSection" -> Json.obj(
              "mgdRegNum" -> "ABC12345678901"
            ),
            "correspondenceDetailsSection" -> Json.obj(
              "correspondenceName"           -> "ABC ltd",
              "correspondenceAdditionalName" -> "XX",
              "correspondenceAddressNonUk" -> Json.obj(
                "address1" -> "add1",
                "address2" -> "add2",
                "address3" -> "add3",
                "address4" -> "add4",
                "country"  -> "Spain"
              ),
              "correspondenceAdditionalInformation" -> "Upstairs",
              "iomOrCiFlag"                         -> true,
              "correspondenceContactNumber" -> Json.obj(
                "phoneNumber"       -> "0123456789",
                "mobilePhoneNumber" -> "0123456780"
              ),
              "correspondenceFaxNumber" -> "0123456799",
              "correspondenceEmail"     -> "abc@email.com"
            )
          )

          val existingUserAnswers = UserAnswers(mgdRegNum,
                                                Json.obj(
                                                  "businessNameSection" -> Json.obj(
                                                    "mgdRegNum" -> "ABC12345678901"
                                                  )
                                                )
                                               )

          val result: Either[Result, DataRequest[AnyContent]] =
            action.callRefine(OptionalDataRequest(request, mgdRegNum, Some(existingUserAnswers))).futureValue

          val expected = DataRequest(request, mgdRegNum, UserAnswers(mgdRegNum, updatedSessionData))

          val req = result.toOption.value
          req.request mustBe expected.request
          req.userAnswers.data mustBe expected.userAnswers.data
          req.userAnswers.id mustBe expected.userAnswers.id
          req.userAnswers.original.loaded mustBe Map(CorrespondenceDetailsSection.key -> now)

          verify(sessionRepository, times(1)).set(any())
          verify(gamblingConnector, times(1)).getCorrespondenceDetails(any())(any())
        }
      }

    }

  }
}

object CorrespondenceDetailsDataRequiredActionSpec {
  val correspondenceDetailsUk: ContactDetails = ContactDetails(
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
    iomOrCiFlag           = Some(true),
    contactNumber         = Some(ContactNumber(Some("0123456789"), Some("0123456780"))),
    faxNumber             = Some("0123456799"),
    emailAddr             = Some("abc@email.com")
  )

  val correspondenceDetailsNonUk: ContactDetails = ContactDetails(
    mgdRegNumber = "XWM00000001770",
    nameLine1    = Some("ABC ltd"),
    nameLine2    = Some("XX"),
    correspondenceAddress = Some(
      Address(
        "add1",
        Some("add2"),
        Some("add3"),
        Some("add4"),
        None,
        Some("Spain")
      )
    ),
    additionalInformation = Some("Upstairs"),
    iomOrCiFlag           = Some(true),
    contactNumber         = Some(ContactNumber(Some("0123456789"), Some("0123456780"))),
    faxNumber             = Some("0123456799"),
    emailAddr             = Some("abc@email.com")
  )
}
