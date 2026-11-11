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
import models.{BusinessNameDetails, OriginalAnswers, SoleProprietorNameDetails, UserAnswers}
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import pages.businessname.BusinessNameSection
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Results.*
import play.api.mvc.{AnyContent, Result}
import play.api.test.FakeRequest
import repositories.SessionRepository
import uk.gov.hmrc.http.UpstreamErrorResponse

import java.time.{Clock, Instant, LocalDate, ZoneOffset}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class BusinessNameDataRequiredActionSpec extends SpecBase with MockitoSugar {

  import BusinessNameDataRequiredActionSpec.*

  class Harness(sessionRepository: SessionRepository, gamblingConnector: GamblingConnector)
      extends BusinessNameDataRequiredActionImpl(sessionRepository, gamblingConnector, clock) {
    def callRefine[A](request: OptionalDataRequest[A]): Future[Either[Result, DataRequest[A]]] = refine(request)
  }

  "BusinessName DataRequiredAction" - {

    "when there is no User Answers in the cache" - {

      "return the request with a populated User Answers with data from the certificate" - {
        "when business" in {

          val request = FakeRequest()
          val sessionRepository = mock[SessionRepository]
          val gamblingConnector = mock[GamblingConnector]
          when(sessionRepository.set(any())) thenReturn Future(true)
          when(gamblingConnector.getBusinessName(any())(any())) thenReturn Future(businessNameModel)
          val action = new Harness(sessionRepository, gamblingConnector)

          val result: Either[Result, DataRequest[AnyContent]] =
            action.callRefine(OptionalDataRequest(request, mgdRegNum, None)).futureValue

          val expected = DataRequest(request, mgdRegNum, UserAnswers(mgdRegNum, loadedBusinessData))

          result.map { req =>
            req.request mustBe expected.request
            req.userAnswers.original.data mustBe expected.userAnswers.data
            req.userAnswers.original.loaded mustBe Map(BusinessNameSection.key -> now)
            req.userAnswers.modified.data mustBe Json.obj()
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

          val result: Either[Result, DataRequest[AnyContent]] =
            action.callRefine(OptionalDataRequest(request, mgdRegNum, None)).futureValue

          val expected = DataRequest(request, mgdRegNum, UserAnswers(mgdRegNum, loadedSoleProprietorData))

          result.map { req =>
            req.request mustBe expected.request
            req.userAnswers.original.data mustBe expected.userAnswers.data
            req.userAnswers.original.loaded mustBe Map(BusinessNameSection.key -> now)
            req.userAnswers.modified.data mustBe Json.obj()
            req.userAnswers.id mustBe expected.userAnswers.id
          }
          verify(sessionRepository, times(1)).set(any())
          verify(gamblingConnector, times(1)).getBusinessName(any())(any())
        }
      }

      "redirect to SystemError " - {
        "User Answers cannot be saved" in {
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

        "getBusinessName throws an exception" in {

          val request = FakeRequest()
          val sessionRepository = mock[SessionRepository]
          val gamblingConnector = mock[GamblingConnector]
          when(sessionRepository.set(any())) thenReturn Future(false)
          when(gamblingConnector.getBusinessName(any())(any())) thenReturn Future.failed(UpstreamErrorResponse("Fail", INTERNAL_SERVER_ERROR))
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

          val loadedAnswers = UserAnswers(mgdRegNum, OriginalAnswers(loadedSoleProprietorData, Map(BusinessNameSection.key -> now)))

          val result: Either[Result, DataRequest[AnyContent]] =
            action.callRefine(OptionalDataRequest(request, mgdRegNum, Some(loadedAnswers))).futureValue

          val expected = DataRequest(request, mgdRegNum, loadedAnswers)

          result.map { req =>
            req.request mustBe expected.request
            req.userAnswers.data mustBe expected.userAnswers.data
            req.userAnswers.id mustBe expected.userAnswers.id
          }
          verify(sessionRepository, never).set(any())
          verify(gamblingConnector, never).getBusinessName(any())(any())

        }
        "with call to backend" in {

          val request = FakeRequest()
          val sessionRepository = mock[SessionRepository]
          val gamblingConnector = mock[GamblingConnector]
          when(sessionRepository.set(any())) thenReturn Future(true)
          when(gamblingConnector.getBusinessName(any())(any())) thenReturn Future(soleProprietorModel)
          val action = new Harness(sessionRepository, gamblingConnector)

          val updatedSessionData = Json.obj(
            "faxNumber" -> "+_+_ hdj39783"
          ) ++ loadedSoleProprietorData

          val existingUserAnswers = UserAnswers(mgdRegNum,
                                                Json.obj(
                                                  "faxNumber" -> "+_+_ hdj39783"
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
          verify(gamblingConnector, times(1)).getBusinessName(any())(any())

        }
      }

    }

  }
}

object BusinessNameDataRequiredActionSpec {
  val businessNameModel: BusinessNameDetails = BusinessNameDetails(
    mgdRegNum    = "ABC12345678901",
    businessName = "Test Business Ltd",
    businessType = Partnership,
    tradingName  = Some("Test Trader Ltd"),
    systemDate   = Some(LocalDate.of(1991, 1, 1))
  )

  val soleProprietorModel: SoleProprietorNameDetails = SoleProprietorNameDetails(
    mgdRegNum    = "ABC12345678901",
    title        = "Mr",
    firstName    = "Test",
    middleName   = None,
    lastName     = "Fella",
    systemDate   = Some(LocalDate.of(1991, 1, 1)),
    tradingName  = Some("Test Trader"),
    businessType = Soleproprietor
  )

  val now: Instant = Instant.parse("2026-11-02T09:00:00Z")
  val clock: Clock = Clock.fixed(now, ZoneOffset.UTC)

  val loadedBusinessData: JsObject = Json.obj(
    "businessType" -> 4,
    "businessNameSection" -> Json.obj(
      "businessName" -> "Test Business Ltd",
      "tradingName"  -> "Test Trader Ltd"
    )
  )

  val loadedSoleProprietorData: JsObject = Json.obj(
    "businessType" -> 1,
    "businessNameSection" -> Json.obj(
      "soleProprietor" -> Json.obj(
        "title"     -> "Mr",
        "firstName" -> "Test",
        "lastName"  -> "Fella"
      ),
      "tradingName" -> "Test Trader"
    )
  )
}
