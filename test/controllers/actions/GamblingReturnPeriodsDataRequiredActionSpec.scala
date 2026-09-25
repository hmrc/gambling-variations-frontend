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
import models.{GamblingReturnPeriods, UserAnswers}
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import pages.GamblingReturnPeriodsPage
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

class GamblingReturnPeriodsDataRequiredActionSpec extends SpecBase with MockitoSugar {

  import GamblingReturnPeriodsDataRequiredActionSpec.*

  class Harness(
    sessionRepository: SessionRepository,
    gamblingConnector: GamblingConnector
  ) extends GamblingReturnPeriodsDataRequiredActionImpl(
        sessionRepository,
        gamblingConnector
      ) {

    def callRefine[A](
      request: OptionalDataRequest[A]
    ): Future[Either[Result, DataRequest[A]]] =
      refine(request)
  }

  "GamblingReturnPeriods DataRequiredAction" - {

    "when there are no User Answers in the cache" - {

      "return the request with populated User Answers from the backend" in {

        val request = FakeRequest()
        val sessionRepository = mock[SessionRepository]
        val gamblingConnector = mock[GamblingConnector]

        when(sessionRepository.set(any())) thenReturn Future(true)
        when(
          gamblingConnector.getGamblingReturnPeriods(any())(any())
        ) thenReturn Future(gamblingReturnPeriods)

        val action =
          new Harness(sessionRepository, gamblingConnector)

        val result: Either[Result, DataRequest[AnyContent]] =
          action
            .callRefine(
              OptionalDataRequest(
                request,
                mgdRegNum,
                None
              )
            )
            .futureValue

        result.map { req =>
          req.request mustBe request
          req.userAnswers.id mustBe mgdRegNum
          req.userAnswers.get(GamblingReturnPeriodsPage) mustBe
            Some(gamblingReturnPeriods)
        }

        verify(sessionRepository, times(1)).set(any())
        verify(gamblingConnector, times(1))
          .getGamblingReturnPeriods(any())(any())
      }

      "redirect to SystemError when User Answers cannot be saved" in {

        val request = FakeRequest()
        val sessionRepository = mock[SessionRepository]
        val gamblingConnector = mock[GamblingConnector]

        when(sessionRepository.set(any())) thenReturn Future(false)
        when(
          gamblingConnector.getGamblingReturnPeriods(any())(any())
        ) thenReturn Future(gamblingReturnPeriods)

        val action =
          new Harness(sessionRepository, gamblingConnector)

        val result: Either[Result, DataRequest[AnyContent]] =
          action
            .callRefine(
              OptionalDataRequest(
                request,
                mgdRegNum,
                None
              )
            )
            .futureValue

        result mustBe Left(
          Redirect(
            controllers.routes.SystemErrorController.onPageLoad()
          )
        )

        verify(sessionRepository, times(1)).set(any())
        verify(gamblingConnector, times(1))
          .getGamblingReturnPeriods(any())(any())
      }

      "redirect to SystemError when getGamblingReturnPeriods throws an exception" in {

        val request = FakeRequest()
        val sessionRepository = mock[SessionRepository]
        val gamblingConnector = mock[GamblingConnector]

        when(
          gamblingConnector.getGamblingReturnPeriods(any())(any())
        ) thenReturn Future.failed(
          UpstreamErrorResponse(
            "Fail",
            INTERNAL_SERVER_ERROR
          )
        )

        val action =
          new Harness(sessionRepository, gamblingConnector)

        val result: Either[Result, DataRequest[AnyContent]] =
          action
            .callRefine(
              OptionalDataRequest(
                request,
                mgdRegNum,
                None
              )
            )
            .futureValue

        result mustBe Left(
          Redirect(
            controllers.routes.SystemErrorController.onPageLoad()
          )
        )

        verify(gamblingConnector, times(1))
          .getGamblingReturnPeriods(any())(any())

        verify(sessionRepository, never()).set(any())
      }
    }

    "when there are User Answers in the cache" - {

      "when Gambling Return Periods already exist" - {

        "return the request without calling the backend" in {

          val request = FakeRequest()
          val sessionRepository = mock[SessionRepository]
          val gamblingConnector = mock[GamblingConnector]

          val existingUserAnswers =
            UserAnswers(
              mgdRegNum,
              Json.obj(
                "gamblingReturnPeriods" -> Json.obj(
                  "mgdRegNumber"          -> mgdRegNum,
                  "returnPeriodsId"       -> 123,
                  "nstpEndDate1"          -> "31-MAR-26",
                  "nstpEndDate2"          -> "30-JUN-26",
                  "nstpEndDate3"          -> "30-SEP-26",
                  "nstpEndDate4"          -> "31-DEC-26",
                  "nstpEndDate5"          -> "31-MAR-27",
                  "nstpEndDate6"          -> "30-JUN-27",
                  "nstpEndDate7"          -> "30-SEP-27",
                  "nstpEndDate8"          -> "31-DEC-27",
                  "isInLastNstp"          -> "false",
                  "finalPeriodWarning"    -> "false",
                  "hasExistingNstpValues" -> "true",
                  "systemDate"            -> "25-SEP-26"
                )
              )
            )

          val action =
            new Harness(sessionRepository, gamblingConnector)

          val result: Either[Result, DataRequest[AnyContent]] =
            action
              .callRefine(
                OptionalDataRequest(
                  request,
                  mgdRegNum,
                  Some(existingUserAnswers)
                )
              )
              .futureValue

          result.map { req =>
            req.request mustBe request
            req.userAnswers.id mustBe existingUserAnswers.id
            req.userAnswers.data mustBe existingUserAnswers.data
          }

          verify(sessionRepository, never()).set(any())

          verify(gamblingConnector, never())
            .getGamblingReturnPeriods(any())(any())
        }
      }

      "when Gambling Return Periods do not exist" - {

        "populate User Answers from the backend and save them" in {

          val request = FakeRequest()
          val sessionRepository = mock[SessionRepository]
          val gamblingConnector = mock[GamblingConnector]

          when(sessionRepository.set(any())) thenReturn Future(true)

          when(
            gamblingConnector.getGamblingReturnPeriods(any())(any())
          ) thenReturn Future(gamblingReturnPeriods)

          val existingUserAnswers =
            UserAnswers(
              mgdRegNum,
              Json.obj(
                "businessNameSection" -> Json.obj(
                  "mgdRegNum" -> "ABC12345678901"
                )
              )
            )

          val action =
            new Harness(sessionRepository, gamblingConnector)

          val result: Either[Result, DataRequest[AnyContent]] =
            action
              .callRefine(
                OptionalDataRequest(
                  request,
                  mgdRegNum,
                  Some(existingUserAnswers)
                )
              )
              .futureValue

          result.map { req =>
            req.request mustBe request
            req.userAnswers.id mustBe mgdRegNum
            req.userAnswers.get(GamblingReturnPeriodsPage) mustBe
              Some(gamblingReturnPeriods)

            req.userAnswers.data mustBe
              existingUserAnswers
                .set(
                  GamblingReturnPeriodsPage,
                  gamblingReturnPeriods
                )
                .get
                .data
          }

          verify(sessionRepository, times(1)).set(any())

          verify(gamblingConnector, times(1))
            .getGamblingReturnPeriods(any())(any())
        }

        "redirect to SystemError when User Answers cannot be saved" in {

          val request = FakeRequest()
          val sessionRepository = mock[SessionRepository]
          val gamblingConnector = mock[GamblingConnector]

          when(sessionRepository.set(any())) thenReturn Future(false)

          when(
            gamblingConnector.getGamblingReturnPeriods(any())(any())
          ) thenReturn Future(gamblingReturnPeriods)

          val existingUserAnswers =
            UserAnswers(
              mgdRegNum,
              Json.obj(
                "businessNameSection" -> Json.obj(
                  "mgdRegNum" -> "ABC12345678901"
                )
              )
            )

          val action =
            new Harness(sessionRepository, gamblingConnector)

          val result: Either[Result, DataRequest[AnyContent]] =
            action
              .callRefine(
                OptionalDataRequest(
                  request,
                  mgdRegNum,
                  Some(existingUserAnswers)
                )
              )
              .futureValue

          result mustBe Left(
            Redirect(
              controllers.routes.SystemErrorController.onPageLoad()
            )
          )

          verify(sessionRepository, times(1)).set(any())

          verify(gamblingConnector, times(1))
            .getGamblingReturnPeriods(any())(any())
        }

        "redirect to SystemError when getGamblingReturnPeriods throws an exception" in {

          val request = FakeRequest()
          val sessionRepository = mock[SessionRepository]
          val gamblingConnector = mock[GamblingConnector]

          when(
            gamblingConnector.getGamblingReturnPeriods(any())(any())
          ) thenReturn Future.failed(
            UpstreamErrorResponse(
              "Fail",
              INTERNAL_SERVER_ERROR
            )
          )

          val existingUserAnswers =
            UserAnswers(
              mgdRegNum,
              Json.obj(
                "businessNameSection" -> Json.obj(
                  "mgdRegNum" -> "ABC12345678901"
                )
              )
            )

          val action =
            new Harness(sessionRepository, gamblingConnector)

          val result: Either[Result, DataRequest[AnyContent]] =
            action
              .callRefine(
                OptionalDataRequest(
                  request,
                  mgdRegNum,
                  Some(existingUserAnswers)
                )
              )
              .futureValue

          result mustBe Left(
            Redirect(
              controllers.routes.SystemErrorController.onPageLoad()
            )
          )

          verify(gamblingConnector, times(1))
            .getGamblingReturnPeriods(any())(any())

          verify(sessionRepository, never()).set(any())
        }
      }
    }
  }
}

object GamblingReturnPeriodsDataRequiredActionSpec {

  val mgdRegNum: String =
    "XWM00000001770"

  val gamblingReturnPeriods: GamblingReturnPeriods =
    GamblingReturnPeriods(
      mgdRegNumber    = mgdRegNum,
      returnPeriodsId = Some(123),
      nstpEndDate1 = Some(
        LocalDate.of(2026, 3, 31)
      ),
      nstpEndDate2 = Some(
        LocalDate.of(2026, 6, 30)
      ),
      nstpEndDate3 = Some(
        LocalDate.of(2026, 9, 30)
      ),
      nstpEndDate4 = Some(
        LocalDate.of(2026, 12, 31)
      ),
      nstpEndDate5 = Some(
        LocalDate.of(2027, 3, 31)
      ),
      nstpEndDate6 = Some(
        LocalDate.of(2027, 6, 30)
      ),
      nstpEndDate7 = Some(
        LocalDate.of(2027, 9, 30)
      ),
      nstpEndDate8 = Some(
        LocalDate.of(2027, 12, 31)
      ),
      isInLastNstp          = Some("false"),
      finalPeriodWarning    = Some("false"),
      hasExistingNstpValues = Some("true"),
      systemDate = Some(
        LocalDate.of(2026, 9, 25)
      )
    )
}
