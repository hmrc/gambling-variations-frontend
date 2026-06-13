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
import controllers.routes
import models.requests.DataRequest
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
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import pages.MgdTradeDetailsSectionPage

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MgdTradeDetailsDataRequiredActionSpec extends SpecBase with MockitoSugar {

  import MgdTradeDetailsDataRequiredActionSpec.*

  class Harness(sessionRepository: SessionRepository, gamblingConnector: GamblingConnector)
      extends MgdTradeDetailsDataRequiredActionImpl(sessionRepository, gamblingConnector) {

    def callRefine(request: DataRequest[AnyContent]) =
      refine(request)
  }

  "MgdTradeDetailsDataRequiredAction" - {

    "when MgdTradeDetailsSectionPage is missing" - {

      "populates UserAnswers from connector and persists them" in {

        val sessionRepository = mock[SessionRepository]
        val gamblingConnector = mock[GamblingConnector]

        when(gamblingConnector.getMgdTradeDetails(any())(any[HeaderCarrier]))
          .thenReturn(Future.successful(mgdTradeDetails))

        when(sessionRepository.set(any()))
          .thenReturn(Future.successful(true))

        val request = FakeRequest()
        val existingUserAnswers = UserAnswers(mgdRegNum, Json.obj()) // Use mgdRegNum here

        val action = new Harness(sessionRepository, gamblingConnector)

        val result =
          action.callRefine(DataRequest(request, mgdRegNum, existingUserAnswers)).futureValue

        result mustBe a[Right[_, _]]
        val Right(dataRequest) = result

        dataRequest.userAnswers.id mustBe mgdRegNum
        dataRequest.userAnswers.get(MgdTradeDetailsSectionPage) mustBe Some(mgdRegNum)

        verify(gamblingConnector, times(1)).getMgdTradeDetails(any())(any[HeaderCarrier])
        verify(sessionRepository, times(1)).set(any())
      }

      "redirects to SystemError when sessionRepository.set returns false" in {

        val sessionRepository = mock[SessionRepository]
        val gamblingConnector = mock[GamblingConnector]

        when(gamblingConnector.getMgdTradeDetails(any())(any[HeaderCarrier]))
          .thenReturn(Future.successful(mgdTradeDetails))

        when(sessionRepository.set(any()))
          .thenReturn(Future.successful(false))

        val request = FakeRequest()
        val action = new Harness(sessionRepository, gamblingConnector)

        val result =
          action
            .callRefine(
              DataRequest(request, mgdRegNum, UserAnswers(mgdRegNum, Json.obj()))
            )
            .futureValue

        result mustBe Left(Redirect(routes.SystemErrorController.onPageLoad()))
        verify(sessionRepository, times(1)).set(any())
      }

      "redirects to SystemError when connector fails" in {

        val sessionRepository = mock[SessionRepository]
        val gamblingConnector = mock[GamblingConnector]

        when(gamblingConnector.getMgdTradeDetails(any())(any[HeaderCarrier]))
          .thenReturn(Future.failed(UpstreamErrorResponse("Fail", INTERNAL_SERVER_ERROR)))

        val request = FakeRequest()
        val action = new Harness(sessionRepository, gamblingConnector)

        val result =
          action
            .callRefine(
              DataRequest(request, mgdRegNum, UserAnswers(mgdRegNum, Json.obj()))
            )
            .futureValue

        result mustBe Left(Redirect(routes.SystemErrorController.onPageLoad()))
        verify(sessionRepository, never).set(any())
      }
    }

    "when MgdTradeDetailsSectionPage is already present" - {

      "returns request immediately and does not call dependencies" in {

        val sessionRepository = mock[SessionRepository]
        val gamblingConnector = mock[GamblingConnector]

        val cachedUserAnswers =
          UserAnswers(mgdRegNum, Json.obj())
            .set(MgdTradeDetailsSectionPage, mgdRegNum)
            .success
            .value

        val request = FakeRequest()
        val action = new Harness(sessionRepository, gamblingConnector)

        val result =
          action.callRefine(DataRequest(request, mgdRegNum, cachedUserAnswers)).futureValue

        result mustBe a[Right[_, _]]
        val Right(dataRequest) = result

        dataRequest.userAnswers mustBe cachedUserAnswers
        verify(sessionRepository, never).set(any())
        verify(gamblingConnector, never).getMgdTradeDetails(any())(any())
      }

      "still allows enrichment if other sections exist but trade details missing" in {

        val sessionRepository = mock[SessionRepository]
        val gamblingConnector = mock[GamblingConnector]

        when(gamblingConnector.getMgdTradeDetails(any())(any[HeaderCarrier]))
          .thenReturn(Future.successful(mgdTradeDetails))

        when(sessionRepository.set(any()))
          .thenReturn(Future.successful(true))

        val partial =
          UserAnswers(mgdRegNum, Json.obj("businessNameSection" -> Json.obj("foo" -> "bar"))) // Use mgdRegNum here

        val request = FakeRequest()

        val action = new Harness(sessionRepository, gamblingConnector)

        val result =
          action.callRefine(DataRequest(request, mgdRegNum, partial)).futureValue

        result mustBe a[Right[_, _]]
        val Right(dataRequest) = result

        // Assert enrichment happened correctly
        dataRequest.userAnswers.id mustBe mgdRegNum
        dataRequest.userAnswers.get(MgdTradeDetailsSectionPage) mustBe Some(mgdRegNum)

        verify(gamblingConnector, times(1)).getMgdTradeDetails(any())(any[HeaderCarrier])
        verify(sessionRepository, times(1)).set(any())
      }
    }
  }
}

object MgdTradeDetailsDataRequiredActionSpec {

  val mgdRegNum = "XRM00000000574"

  val mgdTradeDetails: MgdTradeDetails = MgdTradeDetails(
    mgdRegNumber                     = mgdRegNum,
    isBusinessSeasonal               = Some(true),
    businessTradeClass               = Some(BusinessTradeClass.Casino),
    businessActivityDesc             = Some("Description"),
    previousMgdRegistrationNumbers   = Some(Seq("XWM00000001774", "XDM00000001309", "")),
    associatedMgdRegistrationNumbers = Some(Seq("XXM00000000723", "XQM00000001196", "")),
    systemDate                       = Some(LocalDate.of(2026, 5, 31))
  )
}
