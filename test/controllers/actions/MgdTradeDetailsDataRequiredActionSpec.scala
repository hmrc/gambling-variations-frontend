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
import models.{BusinessTradeClass, MgdTradeDetails, UserAnswers}
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

class MgdTradeDetailsDataRequiredActionSpec extends SpecBase with MockitoSugar {

  import MgdTradeDetailsDataRequiredActionSpec.*

  class Harness(sessionRepository: SessionRepository, gamblingConnector: GamblingConnector)
      extends MgdTradeDetailsDataRequiredActionImpl(sessionRepository, gamblingConnector) {
    def callRefine[A](request: OptionalDataRequest[A]): Future[Either[Result, DataRequest[A]]] =
      refine(request)
  }

  "MgdTradeDetails DataRequiredAction" - {

    "when there is no User Answers in the cache" - {

      "return the request with a populated User Answers with data from the certificate" in {

        val request = FakeRequest()
        val sessionRepository = mock[SessionRepository]
        val gamblingConnector = mock[GamblingConnector]

        when(sessionRepository.set(any())) thenReturn Future(true)
        when(gamblingConnector.getMgdTradeDetails(any())(any())) thenReturn Future(mgdTradeDetails)

        val action = new Harness(sessionRepository, gamblingConnector)

        val data = Json.obj(
          "mgdTradeDetailsSection" -> Json.obj("mgdRegNum" -> "XRM00000000574"),
          "isBusinessSeasonal"     -> true,
          "businessTradeClass"     -> 6,
          "otherTradeClass"        -> "Description",
          "previousRegistrationNumbers" ->
            Json.arr("XWM00000001774", "XDM00000001309"),
          "associatedRegistrationNumbers" ->
            Json.arr("XXM00000000723", "XQM00000001196")
        )

        val result =
          action.callRefine(OptionalDataRequest(request, mgdRegNum, None)).futureValue

        val expected = DataRequest(request, mgdRegNum, UserAnswers(mgdRegNum, data))

        result.map { req =>
          req.request mustBe expected.request
          req.userAnswers.data mustBe expected.userAnswers.data
          req.userAnswers.id mustBe expected.userAnswers.id
        }

        verify(sessionRepository, times(1)).set(any())
        verify(gamblingConnector, times(1)).getMgdTradeDetails(any())(any())
      }

      "redirect to SystemError when User Answers cannot be saved" in {

        val request = FakeRequest()
        val sessionRepository = mock[SessionRepository]
        val gamblingConnector = mock[GamblingConnector]

        when(sessionRepository.set(any())) thenReturn Future(false)
        when(gamblingConnector.getMgdTradeDetails(any())(any())) thenReturn Future(mgdTradeDetails)

        val action = new Harness(sessionRepository, gamblingConnector)

        val result =
          action.callRefine(OptionalDataRequest(request, mgdRegNum, None)).futureValue

        result mustBe Left(Redirect(controllers.routes.SystemErrorController.onPageLoad()))

        verify(sessionRepository, times(1)).set(any())
        verify(gamblingConnector, times(1)).getMgdTradeDetails(any())(any())
      }

      "redirect to SystemError when getMgdTradeDetails throws an exception" in {

        val request = FakeRequest()
        val sessionRepository = mock[SessionRepository]
        val gamblingConnector = mock[GamblingConnector]

        when(sessionRepository.set(any())) thenReturn Future(false)
        when(gamblingConnector.getMgdTradeDetails(any())(any())) thenReturn Future.failed(
          UpstreamErrorResponse("Fail", INTERNAL_SERVER_ERROR)
        )

        val action = new Harness(sessionRepository, gamblingConnector)

        val result =
          action.callRefine(OptionalDataRequest(request, mgdRegNum, None)).futureValue

        result mustBe Left(Redirect(controllers.routes.SystemErrorController.onPageLoad()))
      }
    }

    "when there are User Answers in the cache" - {

      "return the request with populated User Answers" - {

        "without call to backend" in {

          val request = FakeRequest()
          val sessionRepository = mock[SessionRepository]
          val gamblingConnector = mock[GamblingConnector]

          val action = new Harness(sessionRepository, gamblingConnector)

          val data = Json.obj(
            "mgdTradeDetailsSection" -> Json.obj(
              "mgdRegNum" -> "ABC12345678901"
            )
          )

          val result =
            action
              .callRefine(
                OptionalDataRequest(request, mgdRegNum, Some(UserAnswers(mgdRegNum, data)))
              )
              .futureValue

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
          when(gamblingConnector.getMgdTradeDetails(any())(any())) thenReturn Future(mgdTradeDetails)

          val action = new Harness(sessionRepository, gamblingConnector)

          val updatedSessionData = Json.obj(
            "businessNameSection" -> Json.obj(
              "mgdRegNum" -> "ABC12345678901"
            ),
            "mgdTradeDetailsSection" -> Json.obj("mgdRegNum" -> "XRM00000000574"),
            "isBusinessSeasonal"     -> true,
            "businessTradeClass"     -> 6,
            "otherTradeClass"        -> "Description",
            "previousRegistrationNumbers" ->
              Json.arr("XWM00000001774", "XDM00000001309"),
            "associatedRegistrationNumbers" ->
              Json.arr("XXM00000000723", "XQM00000001196")
          )

          val existingUserAnswers = UserAnswers(
            mgdRegNum,
            Json.obj(
              "businessNameSection" -> Json.obj(
                "mgdRegNum" -> "ABC12345678901"
              )
            )
          )

          val result =
            action
              .callRefine(
                OptionalDataRequest(request, mgdRegNum, Some(existingUserAnswers))
              )
              .futureValue

          val expected =
            DataRequest(request, mgdRegNum, UserAnswers(mgdRegNum, updatedSessionData))

          result.map { req =>
            req.request mustBe expected.request
            req.userAnswers.data mustBe expected.userAnswers.data
            req.userAnswers.id mustBe expected.userAnswers.id
          }

          verify(sessionRepository, times(1)).set(any())
          verify(gamblingConnector, times(1)).getMgdTradeDetails(any())(any())
        }
      }
    }
  }
}

object MgdTradeDetailsDataRequiredActionSpec {

  val mgdTradeDetails: MgdTradeDetails = MgdTradeDetails(
    mgdRegNumber                     = "XRM00000000574",
    isBusinessSeasonal               = Some(true),
    businessTradeClass               = Some(BusinessTradeClass.Casino),
    businessActivityDesc             = Some("Description"),
    previousMgdRegistrationNumbers   = Some(Seq("XWM00000001774", "XDM00000001309", "")),
    associatedMgdRegistrationNumbers = Some(Seq("XXM00000000723", "XQM00000001196", "")),
    systemDate                       = Some(LocalDate.of(2026, 5, 31))
  )
}
