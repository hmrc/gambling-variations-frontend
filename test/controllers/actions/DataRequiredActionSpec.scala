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
import models.BusinessType.{Partnership, Soleproprietor}
import models.requests.{DataRequest, OptionalDataRequest}
import models.{BusinessNameDetails, SoleProprietorNameDetails, UserAnswers}
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.*
import org.scalatest.RecoverMethods
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

class DataRequiredActionSpec extends SpecBase with MockitoSugar with RecoverMethods {

  class Harness(sessionRepository: SessionRepository, gamblingConnector: GamblingConnector)
      extends DataRequiredActionImpl(sessionRepository, gamblingConnector) {
    def callRefine[A](request: OptionalDataRequest[A]): Future[Either[Result, DataRequest[A]]] = refine(request)
  }

  val mgdRegNum = "GAM0000000001"

  "Data Required Action" - {

    "when there is no User Answers in the cache" - {

      "return the request with a populated User Answers with data from the certificate" - {
        "when business" in {

          val request = FakeRequest()
          val sessionRepository = mock[SessionRepository]
          val gamblingConnector = mock[GamblingConnector]
          when(sessionRepository.set(any())) thenReturn Future(true)
          when(gamblingConnector.getBusinessName(any())(any())) thenReturn Future(businessNameModel)
          val action = new Harness(sessionRepository, gamblingConnector)

          val data = Json.obj(
            "businessDetails" -> Json.obj(
              "businessName" -> "Test Business Ltd",
              "tradingName"  -> "Test Trader Ltd",
              "businessType" -> 4
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
          verify(gamblingConnector, times(1)).getBusinessName(any())(any())
        }
        "when sole proprietor" in {

          val request = FakeRequest()
          val sessionRepository = mock[SessionRepository]
          val gamblingConnector = mock[GamblingConnector]
          when(sessionRepository.set(any())) thenReturn Future(true)
          when(gamblingConnector.getBusinessName(any())(any())) thenReturn Future(soleProprietorModel)
          val action = new Harness(sessionRepository, gamblingConnector)

          val data = Json.obj(
            "soleProprietorDetails" -> Json.obj(
              "title"     -> "Mr",
              "firstName" -> "Test",
              "lastName"  -> "Fella"
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
          verify(gamblingConnector, times(1)).getBusinessName(any())(any())
        }
      }

      "redirect to SystemError " - {
        "when User Answers cannot be saved" in {
          val request = FakeRequest()
          val sessionRepository = mock[SessionRepository]
          val gamblingConnector = mock[GamblingConnector]
          when(sessionRepository.set(any())) thenReturn Future(false)
          when(gamblingConnector.getBusinessName(any())(any())) thenReturn Future(soleProprietorModel)
          val action = new Harness(sessionRepository, gamblingConnector)

          val result: Either[Result, DataRequest[AnyContent]] =
            action.callRefine(OptionalDataRequest(request, mgdRegNum, None)).futureValue

          result mustBe Left(Redirect(controllers.routes.SystemErrorController.onPageLoad()))
          verify(sessionRepository, times(1)).set(any())
          verify(gamblingConnector, times(1)).getBusinessName(any())(any())
        }
      }

      "return a failed future when getCertificate throws an exception" in {

        val request = FakeRequest()
        val sessionRepository = mock[SessionRepository]
        val gamblingConnector = mock[GamblingConnector]
        when(sessionRepository.set(any())) thenReturn Future(false)
        when(gamblingConnector.getBusinessName(any())(any())) thenReturn Future.failed(UpstreamErrorResponse("Fail", INTERNAL_SERVER_ERROR))
        val action = new Harness(sessionRepository, gamblingConnector)

        recoverToSucceededIf[RuntimeException] {
          action.callRefine(OptionalDataRequest(request, mgdRegNum, None))
        }

      }

    }

    "when there is no User Answers in the cache" - {

      "return the request with a populated User Answers without call to backend" in {

        val request = FakeRequest()
        val sessionRepository = mock[SessionRepository]
        val gamblingConnector = mock[GamblingConnector]
        when(sessionRepository.set(any())) thenReturn Future(true)
        when(gamblingConnector.getCertificate(any())(any())) thenReturn Future(soleProprietorModel)
        val action = new Harness(sessionRepository, gamblingConnector)

        val data = Json.obj(
          "businessName" -> "Test Business Ltd",
          "tradingName"  -> "Test Trader Ltd"
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
        verify(gamblingConnector, never).getCertificate(any())(any())

      }

    }

    def businessNameModel: BusinessNameDetails = BusinessNameDetails(
      mgdRegNum    = "ABC12345678901",
      businessName = "Test Business Ltd",
      businessType = Partnership,
      tradingName  = Some("Test Trader Ltd"),
      systemDate   = Some(LocalDate.of(1991, 1, 1))
    )
    def soleProprietorModel: SoleProprietorNameDetails = SoleProprietorNameDetails(
      mgdRegNum    = "ABC12345678901",
      title        = "Mr",
      firstName    = "Test",
      middleName   = None,
      lastName     = "Fella",
      systemDate   = Some(LocalDate.of(1991, 1, 1)),
      tradingName  = None,
      businessType = Soleproprietor
    )
  }
}
