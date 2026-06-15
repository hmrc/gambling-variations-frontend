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
import models.requests.DataRequest
import models.{BusinessTradeClass, MgdTradeDetails, UserAnswers}
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import pages.MgdTradeDetailsSectionPage
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.libs.json.Json
import play.api.mvc.Results.Redirect
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import repositories.SessionRepository
import uk.gov.hmrc.http.UpstreamErrorResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CheckTradingDetailsDataRequiredActionSpec extends SpecBase with MockitoSugar {

  import CheckTradingDetailsDataRequiredActionSpec.*

  class Harness(sessionRepository: SessionRepository, gamblingConnector: GamblingConnector)
      extends CheckTradingDetailsDataRequiredActionImpl(sessionRepository, gamblingConnector) {

    def callRefine[A](request: DataRequest[A]) =
      refine(request)
  }

  "CheckTradingDetailsDataRequiredAction" - {

    "when MgdTradeDetailsSectionPage is missing" - {

      "should populate UserAnswers and return updated request" in {

        val request = FakeRequest()
        val sessionRepository = mock[SessionRepository]
        val gamblingConnector = mock[GamblingConnector]

        when(gamblingConnector.getMgdTradeDetails(any())(any()))
          .thenReturn(Future.successful(mgdTradeDetails))

        when(sessionRepository.set(any()))
          .thenReturn(Future.successful(true))

        val action = new Harness(sessionRepository, gamblingConnector)

        val baseUserAnswers =
          UserAnswers(mgdRegNum, Json.obj())

        val result =
          action.callRefine(DataRequest(request, mgdRegNum, baseUserAnswers)).futureValue

        result mustBe a[Right[_, _]]

        verify(gamblingConnector, times(1)).getMgdTradeDetails(any())(any())
        verify(sessionRepository, times(1)).set(any())
      }

      "should redirect when session save fails" in {

        val request = FakeRequest()
        val sessionRepository = mock[SessionRepository]
        val gamblingConnector = mock[GamblingConnector]

        when(gamblingConnector.getMgdTradeDetails(any())(any()))
          .thenReturn(Future.successful(mgdTradeDetails))

        when(sessionRepository.set(any()))
          .thenReturn(Future.successful(false))

        val action = new Harness(sessionRepository, gamblingConnector)

        val result =
          action.callRefine(DataRequest(request, mgdRegNum, UserAnswers(mgdRegNum))).futureValue

        result mustBe Left(Redirect(controllers.routes.SystemErrorController.onPageLoad()))
      }

      "should redirect when connector fails" in {

        val request = FakeRequest()
        val sessionRepository = mock[SessionRepository]
        val gamblingConnector = mock[GamblingConnector]

        when(gamblingConnector.getMgdTradeDetails(any())(any()))
          .thenReturn(Future.failed(UpstreamErrorResponse("error", INTERNAL_SERVER_ERROR)))

        val action = new Harness(sessionRepository, gamblingConnector)

        val result =
          action.callRefine(DataRequest(request, mgdRegNum, UserAnswers(mgdRegNum))).futureValue

        result mustBe Left(Redirect(controllers.routes.SystemErrorController.onPageLoad()))
      }
    }

    "when MgdTradeDetailsSectionPage exists" - {

      "should return request without calling backend" in {

        val request = FakeRequest()
        val sessionRepository = mock[SessionRepository]
        val gamblingConnector = mock[GamblingConnector]

        val existing =
          UserAnswers(mgdRegNum).set(MgdTradeDetailsSectionPage, "XRM00000000574").success.value

        val action = new Harness(sessionRepository, gamblingConnector)

        val result =
          action.callRefine(DataRequest(request, mgdRegNum, existing)).futureValue

        result mustBe a[Right[_, _]]

        verify(gamblingConnector, never()).getMgdTradeDetails(any())(any())
        verify(sessionRepository, never()).set(any())
      }
    }
  }
}

object CheckTradingDetailsDataRequiredActionSpec {

  val mgdTradeDetails: MgdTradeDetails =
    MgdTradeDetails(
      mgdRegNumber         = "XRM00000000574",
      isBusinessSeasonal   = Some(true),
      businessTradeClass   = Some(BusinessTradeClass.Casino),
      businessActivityDesc = Some("Description"),
      previousMgdRegistrationNumbers = Some(
        Seq("XWM00000001774", "XDM00000001309", "")
      ),
      associatedMgdRegistrationNumbers = Some(
        Seq("XXM00000000723", "XQM00000001196", "")
      ),
      systemDate = Some(java.time.LocalDate.of(2026, 5, 31))
    )
}
