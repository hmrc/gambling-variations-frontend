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
import models.{MgdCertificate, UserAnswers}
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

      "return the request with a populated User Answers with data from the certificate" in {

        val request = FakeRequest()
        val sessionRepository = mock[SessionRepository]
        val gamblingConnector = mock[GamblingConnector]
        when(sessionRepository.set(any())) thenReturn Future(true)
        when(gamblingConnector.getCertificate(any())(any())) thenReturn Future(certificate)
        val action = new Harness(sessionRepository, gamblingConnector)

        val data = Json.obj(
          "businessName" -> "Test Business Ltd",
          "tradingName"  -> "Test Trader Ltd"
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
        verify(gamblingConnector, times(1)).getCertificate(any())(any())
      }

      "redirect to SystemError when User Answers cannot be saved" in {
        val request = FakeRequest()
        val sessionRepository = mock[SessionRepository]
        val gamblingConnector = mock[GamblingConnector]
        when(sessionRepository.set(any())) thenReturn Future(false)
        when(gamblingConnector.getCertificate(any())(any())) thenReturn Future(certificate)
        val action = new Harness(sessionRepository, gamblingConnector)

        val result: Either[Result, DataRequest[AnyContent]] =
          action.callRefine(OptionalDataRequest(request, mgdRegNum, None)).futureValue

        result mustBe Left(Redirect(controllers.routes.SystemErrorController.onPageLoad()))
        verify(sessionRepository, times(1)).set(any())
        verify(gamblingConnector, times(1)).getCertificate(any())(any())
      }

      "return a failed future when getCertificate throws an exception" in {

        val request = FakeRequest()
        val sessionRepository = mock[SessionRepository]
        val gamblingConnector = mock[GamblingConnector]
        when(sessionRepository.set(any())) thenReturn Future(false)
        when(gamblingConnector.getCertificate(any())(any())) thenReturn Future.failed(UpstreamErrorResponse("Fail", INTERNAL_SERVER_ERROR))
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
        when(gamblingConnector.getCertificate(any())(any())) thenReturn Future(certificate)
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

    def certificate: MgdCertificate =
      MgdCertificate(
        mgdRegNumber         = "MGD123",
        registrationDate     = Some(LocalDate.parse("2026-01-01")),
        individualName       = Some("John Doe"),
        businessName         = Some("Test Business Ltd"),
        tradingName          = Some("Test Trader Ltd"),
        repMemName           = None,
        busAddrLine1         = Some("Line 1"),
        busAddrLine2         = Some("Line 2"),
        busAddrLine3         = None,
        busAddrLine4         = None,
        busPostcode          = Some("AB1 2CD"),
        busCountry           = None,
        busAdi               = None,
        repMemLine1          = None,
        repMemLine2          = None,
        repMemLine3          = None,
        repMemLine4          = None,
        repMemPostcode       = None,
        repMemAdi            = None,
        typeOfBusiness       = Some("Corporate Body"), // important: matches controller
        businessTradeClass   = Some(1),
        noOfPartners         = None,
        groupReg             = "N",
        noOfGroupMems        = None,
        dateCertIssued       = Some(LocalDate.parse("2026-01-02")),
        partMembers          = Seq.empty,
        groupMembers         = Seq.empty,
        returnPeriodEndDates = Seq.empty
      )
  }
}
