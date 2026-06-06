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
import models.{Address, BusinessTradeClass, ContactNumber, CorrespondenceDetails, MgdTradeDetails, UserAnswers}
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

class CorrespondenceDetailsDataRequiredActionSpec extends SpecBase with MockitoSugar {

  import CorrespondenceDetailsDataRequiredActionSpec.*

  class Harness(sessionRepository: SessionRepository, gamblingConnector: GamblingConnector)
      extends CorrespondenceDetailsDataRequiredActionImpl(sessionRepository, gamblingConnector) {
    def callRefine[A](request: OptionalDataRequest[A]): Future[Either[Result, DataRequest[A]]] = refine(request)
  }

  "CorrespondenceDetails DataRequiredAction" - {

    "when there is no User Answers in the cache" - {

      "return the request with a populated User Answers with data from the certificate" in {

        val request = FakeRequest()
        val sessionRepository = mock[SessionRepository]
        val gamblingConnector = mock[GamblingConnector]
        when(sessionRepository.set(any())) thenReturn Future(true)
        when(gamblingConnector.getCorrespondenceDetails(any())(any())) thenReturn Future(correspondenceDetails)
        val action = new Harness(sessionRepository, gamblingConnector)

        val data = Json.obj(
          "correspondenceDetailsSection"        -> Json.obj("mgdRegNum" -> "XWM00000001770"),
          "correspondenceName"            -> "ABC ltd",
          "correspondenceAdditionalName"            -> "XX",
          "correspondenceAddress"               -> Json.obj(
           "address1" -> "add1",
           "address2" -> "add2",
           "address3" -> "add3",
           "address4" -> "add4",
           "postcode" -> "NE11NE",
            "country" -> "UK"
          ),
          "correspondenceAdditionalInformation" -> "Upstairs",
          "correspondenceContactNumber" -> Json.obj(
            "phoneNumber" -> "0123456789",
            "mobilePhoneNumber" -> "0123456780"
          ),
          "correspondenceFaxNumber" -> "0123456799",
          "correspondenceEmail" -> "abc@email.com"
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
        verify(gamblingConnector, times(1)).getCorrespondenceDetails(any())(any())
      }

      "redirect to SystemError " - {
        "User Answers cannot be saved" in {
          val request = FakeRequest()
          val sessionRepository = mock[SessionRepository]
          val gamblingConnector = mock[GamblingConnector]
          when(sessionRepository.set(any())) thenReturn Future(false)
          when(gamblingConnector.getCorrespondenceDetails(any())(any())) thenReturn Future(correspondenceDetails)
          val action = new Harness(sessionRepository, gamblingConnector)

          val result: Either[Result, DataRequest[AnyContent]] =
            action.callRefine(OptionalDataRequest(request, mgdRegNum, None)).futureValue

          result mustBe Left(Redirect(controllers.routes.SystemErrorController.onPageLoad()))
          verify(sessionRepository, times(1)).set(any())
          verify(gamblingConnector, times(1)).getCorrespondenceDetails(any())(any())
        }

        "getMgdTradeDetails throws an exception" in {

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

          val data = Json.obj(
            "correspondenceDetailsSection" -> Json.obj(
              "mgdRegNum" -> "ABC12345678901"
            )
          )

          val result: Either[Result, DataRequest[AnyContent]] =
            action.callRefine(OptionalDataRequest(request, mgdRegNum, Some(UserAnswers(mgdRegNum, data)))).futureValue

          val expected = DataRequest(request, mgdRegNum, UserAnswers(mgdRegNum, data))

          result.map { req =>
            req.request mustBe expected.request
            req.userAnswers.data mustBe expected.userAnswers.data
            req.userAnswers.id mustBe expected.userAnswers.id
          }
          verify(sessionRepository, never).set(any())
          verify(gamblingConnector, never).getMgdTradeDetails(any())(any())

        }
        "with call to backend" in {

          val request = FakeRequest()
          val sessionRepository = mock[SessionRepository]
          val gamblingConnector = mock[GamblingConnector]
          when(sessionRepository.set(any())) thenReturn Future(true)
          when(gamblingConnector.getCorrespondenceDetails(any())(any())) thenReturn Future(correspondenceDetails)
          val action = new Harness(sessionRepository, gamblingConnector)

          val updatedSessionData = Json.obj(
            "businessNameSection" -> Json.obj(
              "mgdRegNum" -> "ABC12345678901"
            ),
            "correspondenceDetailsSection" -> Json.obj("mgdRegNum" -> "XWM00000001770"),
            "correspondenceName" -> "ABC ltd",
            "correspondenceAdditionalName" -> "XX",
            "correspondenceAddress" -> Json.obj(
              "address1" -> "add1",
              "address2" -> "add2",
              "address3" -> "add3",
              "address4" -> "add4",
              "postcode" -> "NE11NE",
              "country" -> "UK"
            ),
            "correspondenceAdditionalInformation" -> "Upstairs",
            "correspondenceContactNumber" -> Json.obj(
              "phoneNumber" -> "0123456789",
              "mobilePhoneNumber" -> "0123456780"
            ),
            "correspondenceFaxNumber" -> "0123456799",
            "correspondenceEmail" -> "abc@email.com"
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

          result.map { req =>
            req.request mustBe expected.request
            req.userAnswers.data mustBe expected.userAnswers.data
            req.userAnswers.id mustBe expected.userAnswers.id
          }
          verify(sessionRepository, times(1)).set(any())
          verify(gamblingConnector, times(1)).getCorrespondenceDetails(any())(any())

        }
      }

    }

  }
}

object CorrespondenceDetailsDataRequiredActionSpec {
  val correspondenceDetails: CorrespondenceDetails = CorrespondenceDetails(
    mgdRegNumber = "XWM00000001770",
    nameLine1    = "ABC ltd",
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
    contactNumber         = Some(ContactNumber(Some("0123456789"), Some("0123456780"))),
    faxNumber             = Some("0123456799"),
    emailAddr             = Some("abc@email.com")
  )
}
