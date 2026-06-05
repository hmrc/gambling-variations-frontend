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
import models.{BusinessContactDetails, UserAnswers}
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

class BusinessContactDetailsDataRequiredActionSpec extends SpecBase with MockitoSugar {

  import BusinessContactDetailsDataRequiredActionSpec.*

  class Harness(sessionRepository: SessionRepository, gamblingConnector: GamblingConnector)
      extends BusinessContactDetailsDataRequiredActionImpl(sessionRepository, gamblingConnector) {
    def callRefine[A](request: OptionalDataRequest[A]): Future[Either[Result, DataRequest[A]]] = refine(request)
  }

  val mgdRegNum = "GAM0000000001"

  "BusinessContactDetails DataRequiredAction" - {

    "when there is no User Answers in the cache" - {

      "return the request with a populated User Answers with data from the certificate" in {

        val request = FakeRequest()
        val sessionRepository = mock[SessionRepository]
        val gamblingConnector = mock[GamblingConnector]
        when(sessionRepository.set(any())) thenReturn Future(true)
        when(gamblingConnector.getBusinessContactDetails(any())(any())) thenReturn Future(businessContactDetailsModel)
        val action = new Harness(sessionRepository, gamblingConnector)

        val data = Json.obj(
          "businessContactDetailsSection" -> Json.obj("mgdRegNum" -> "ABC12345678901"),
          "faxNumber"                     -> "+_+_ hdj39783",
          "businessContactNumber" -> Json.obj(
            "phoneNumber"  -> "+44 8903928171",
            "mobileNumber" -> "+44 8903928171"
          ),
          "businessEmailAddress" -> "a@b.com"
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
        verify(gamblingConnector, times(1)).getBusinessContactDetails(any())(any())
      }

      "redirect to SystemError " - {
        "User Answers cannot be saved" in {
          val request = FakeRequest()
          val sessionRepository = mock[SessionRepository]
          val gamblingConnector = mock[GamblingConnector]
          when(sessionRepository.set(any())) thenReturn Future(false)
          when(gamblingConnector.getBusinessContactDetails(any())(any())) thenReturn Future(businessContactDetailsModel)
          val action = new Harness(sessionRepository, gamblingConnector)

          val result: Either[Result, DataRequest[AnyContent]] =
            action.callRefine(OptionalDataRequest(request, mgdRegNum, None)).futureValue

          result mustBe Left(Redirect(controllers.routes.SystemErrorController.onPageLoad()))
          verify(sessionRepository, times(1)).set(any())
          verify(gamblingConnector, times(1)).getBusinessContactDetails(any())(any())
        }

        "getBusinessContactDetails throws an exception" in {

          val request = FakeRequest()
          val sessionRepository = mock[SessionRepository]
          val gamblingConnector = mock[GamblingConnector]
          when(sessionRepository.set(any())) thenReturn Future(false)
          when(gamblingConnector.getBusinessContactDetails(any())(any())) thenReturn Future.failed(
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
            "businessContactDetailsSection" -> Json.obj(
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
          verify(gamblingConnector, never).getBusinessContactDetails(any())(any())

        }
        "with call to backend" in {

          val request = FakeRequest()
          val sessionRepository = mock[SessionRepository]
          val gamblingConnector = mock[GamblingConnector]
          when(sessionRepository.set(any())) thenReturn Future(true)
          when(gamblingConnector.getBusinessContactDetails(any())(any())) thenReturn Future(businessContactDetailsModel)
          val action = new Harness(sessionRepository, gamblingConnector)

          val updatedSessionData = Json.obj(
            "businessNameSection" -> Json.obj(
              "mgdRegNum" -> "ABC12345678901"
            ),
            "businessContactDetailsSection" -> Json.obj(
              "mgdRegNum" -> "ABC12345678901"
            ),
            "faxNumber" -> "+_+_ hdj39783",
            "businessContactNumber" -> Json.obj(
              "phoneNumber"  -> "+44 8903928171",
              "mobileNumber" -> "+44 8903928171"
            ),
            "businessEmailAddress" -> "a@b.com"
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
          verify(gamblingConnector, times(1)).getBusinessContactDetails(any())(any())

        }
      }

    }

  }
}

object BusinessContactDetailsDataRequiredActionSpec {
  val businessContactDetailsModel: BusinessContactDetails = BusinessContactDetails(
    mgdRegNumber      = "ABC12345678901",
    phoneNumber       = Some("+44 8903928171"),
    mobilePhoneNumber = Some("+44 8903928171"),
    faxNumber         = Some("+_+_ hdj39783"),
    emailAddr         = Some("a@b.com"),
    systemDate        = Some(LocalDate.of(1991, 1, 1))
  )
}
