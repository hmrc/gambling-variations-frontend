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
import models.{BusinessDetails, BusinessType, UserAnswers}
import models.requests.{DataRequest, OptionalDataRequest}
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import pages.{BusinessTypePage, GroupMemberPage}
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

class BusinessDetailsDataRequiredActionSpec extends SpecBase with MockitoSugar {

  import BusinessDetailsDataRequiredActionSpec.*

  class Harness(sessionRepository: SessionRepository, gamblingConnector: GamblingConnector)
      extends BusinessDetailsDataRequiredActionImpl(sessionRepository, gamblingConnector) {

    def callRefine[A](request: OptionalDataRequest[A]) =
      refine(request)
  }

  "BusinessDetailsDataRequiredAction" - {

    "when there is NO user answers in cache" - {

      "must populate user answers and return DataRequest" in {

        val request = FakeRequest()

        val sessionRepository = mock[SessionRepository]
        val gamblingConnector = mock[GamblingConnector]

        when(sessionRepository.set(any())) thenReturn Future.successful(true)
        when(gamblingConnector.getBusinessDetails(any())(any()))
          .thenReturn(Future.successful(businessDetails))

        val action = new Harness(sessionRepository, gamblingConnector)

        val result =
          action.callRefine(OptionalDataRequest(request, mgdRegNum, None)).futureValue

        result mustBe a[Right[_, _]]

        val right = result.toOption.get

        right.mgdRegNum mustBe mgdRegNum
        right.userAnswers.id mustBe mgdRegNum

        verify(sessionRepository, times(1)).set(any())
        verify(gamblingConnector, times(1)).getBusinessDetails(any())(any())
      }

      "must redirect to SystemError when session save fails" in {

        val request = FakeRequest()

        val sessionRepository = mock[SessionRepository]
        val gamblingConnector = mock[GamblingConnector]

        when(sessionRepository.set(any())) thenReturn Future.successful(false)
        when(gamblingConnector.getBusinessDetails(any())(any()))
          .thenReturn(Future.successful(businessDetails))

        val action = new Harness(sessionRepository, gamblingConnector)

        val result =
          action.callRefine(OptionalDataRequest(request, mgdRegNum, None)).futureValue

        result mustBe Left(Redirect(controllers.routes.SystemErrorController.onPageLoad()))

        verify(sessionRepository).set(any())
        verify(gamblingConnector).getBusinessDetails(any())(any())
      }

      "must redirect to SystemError when connector fails" in {

        val request = FakeRequest()

        val sessionRepository = mock[SessionRepository]
        val gamblingConnector = mock[GamblingConnector]

        when(gamblingConnector.getBusinessDetails(any())(any()))
          .thenReturn(
            Future.failed(
              UpstreamErrorResponse("boom", INTERNAL_SERVER_ERROR)
            )
          )

        val action = new Harness(sessionRepository, gamblingConnector)

        val result =
          action.callRefine(OptionalDataRequest(request, mgdRegNum, None)).futureValue

        result mustBe Left(Redirect(controllers.routes.SystemErrorController.onPageLoad()))

        verify(gamblingConnector).getBusinessDetails(any())(any())
        verify(sessionRepository, never()).set(any())
      }
    }

    "when user answers already exist" - {

      "must return DataRequest WITHOUT calling connector if BusinessType and GroupMember already present" in {

        val data = Json.obj(
          BusinessTypePage.toString -> BusinessType.Partnership.code,
          GroupMemberPage.toString -> true
        )

        val existing =
          UserAnswers(mgdRegNum, data)

        val request = FakeRequest()

        val sessionRepository = mock[SessionRepository]
        val gamblingConnector = mock[GamblingConnector]

        val action = new Harness(sessionRepository, gamblingConnector)

        val result =
          action.callRefine(OptionalDataRequest(request, mgdRegNum, Some(existing))).futureValue

        result mustBe a[Right[_, _]]

        verify(gamblingConnector, never()).getBusinessDetails(any())(any())
        verify(sessionRepository, never()).set(any())
      }

      "must call connector when BusinessType is missing in existing answers" in {

        val existing =
          UserAnswers(mgdRegNum).set(GroupMemberPage, true).success.value

        val request = FakeRequest()

        val sessionRepository = mock[SessionRepository]
        val gamblingConnector = mock[GamblingConnector]

        when(sessionRepository.set(any())) thenReturn Future.successful(true)
        when(gamblingConnector.getBusinessDetails(any())(any()))
          .thenReturn(Future.successful(businessDetails))

        val action = new Harness(sessionRepository, gamblingConnector)

        val result =
          action.callRefine(OptionalDataRequest(request, mgdRegNum, Some(existing))).futureValue

        result mustBe a[Right[_, _]]

        verify(gamblingConnector).getBusinessDetails(any())(any())
        verify(sessionRepository).set(any())
      }
    }
  }
}

object BusinessDetailsDataRequiredActionSpec {

  val mgdRegNum = "XRM00000000574"

  val businessDetails: BusinessDetails =
    BusinessDetails(
      mgdRegNumber          = mgdRegNum,
      businessType          = Some(BusinessType.Partnership),
      currentlyRegistered   = 1,
      groupReg              = true,
      dateOfRegistration    = Some(LocalDate.of(2026, 1, 1)),
      businessPartnerNumber = None,
      systemDate            = LocalDate.of(2026, 1, 1)
    )
}
