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
import models.licencespremises.{LicencesAndPremises, PremisesDetails, PremisesDetailsResponse}
import models.requests.{DataRequest, OptionalDataRequest}
import models.{OriginalAnswers, UserAnswers}
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import pages.licencespremises.LicencesPremisesSection
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

class LicencesPremisesDataRequiredActionSpec extends SpecBase with MockitoSugar {

  import LicencesPremisesDataRequiredActionSpec.*

  class Harness(sessionRepository: SessionRepository, gamblingConnector: GamblingConnector)
      extends LicencesPremisesDataRequiredActionImpl(sessionRepository, gamblingConnector, clock) {
    def callRefine[A](request: OptionalDataRequest[A]): Future[Either[Result, DataRequest[A]]] = refine(request)
  }

  "LicencesPremises DataRequiredAction" - {

    "when there is no User Answers in the cache" - {

      "return the request with User Answers holding the licences and premises as original answers" in {

        val request = FakeRequest()
        val sessionRepository = mock[SessionRepository]
        val gamblingConnector = mock[GamblingConnector]
        when(sessionRepository.set(any())) thenReturn Future(true)
        when(gamblingConnector.getLicencesAndPremises(any())(any())) thenReturn Future(licencesAndPremises)
        val action = new Harness(sessionRepository, gamblingConnector)

        val result: Either[Result, DataRequest[AnyContent]] =
          action.callRefine(OptionalDataRequest(request, mgdRegNum, None)).futureValue

        result.isRight mustBe true
        result.map { req =>
          req.request mustBe request
          req.userAnswers.id mustBe mgdRegNum
          req.userAnswers.original.data mustBe loadedData
          req.userAnswers.original.loaded mustBe Map(LicencesPremisesSection.key -> now)
          req.userAnswers.modified.data mustBe Json.obj()
          req.userAnswers.hasChanges(LicencesPremisesSection) mustBe false
        }
        verify(sessionRepository, times(1)).set(any())
        verify(gamblingConnector, times(1)).getLicencesAndPremises(any())(any())
      }

      "mark the section as loaded when the backend returns no licences or premises" in {

        val request = FakeRequest()
        val sessionRepository = mock[SessionRepository]
        val gamblingConnector = mock[GamblingConnector]
        when(sessionRepository.set(any())) thenReturn Future(true)
        when(gamblingConnector.getLicencesAndPremises(any())(any())) thenReturn Future(emptyLicencesAndPremises)
        val action = new Harness(sessionRepository, gamblingConnector)

        val result: Either[Result, DataRequest[AnyContent]] =
          action.callRefine(OptionalDataRequest(request, mgdRegNum, None)).futureValue

        result.isRight mustBe true
        result.map { req =>
          req.userAnswers.original.data mustBe Json.obj()
          req.userAnswers.isLoaded(LicencesPremisesSection) mustBe true
        }
      }

      "redirect to SystemError" - {

        "when the User Answers cannot be saved" in {

          val request = FakeRequest()
          val sessionRepository = mock[SessionRepository]
          val gamblingConnector = mock[GamblingConnector]
          when(sessionRepository.set(any())) thenReturn Future(false)
          when(gamblingConnector.getLicencesAndPremises(any())(any())) thenReturn Future(licencesAndPremises)
          val action = new Harness(sessionRepository, gamblingConnector)

          val result: Either[Result, DataRequest[AnyContent]] =
            action.callRefine(OptionalDataRequest(request, mgdRegNum, None)).futureValue

          result mustBe Left(Redirect(controllers.routes.SystemErrorController.onPageLoad()))
          verify(sessionRepository, times(1)).set(any())
          verify(gamblingConnector, times(1)).getLicencesAndPremises(any())(any())
        }

        "when getLicencesAndPremises fails" in {

          val request = FakeRequest()
          val sessionRepository = mock[SessionRepository]
          val gamblingConnector = mock[GamblingConnector]
          when(gamblingConnector.getLicencesAndPremises(any())(any())) thenReturn Future.failed(UpstreamErrorResponse("Fail", INTERNAL_SERVER_ERROR))
          val action = new Harness(sessionRepository, gamblingConnector)

          val result: Either[Result, DataRequest[AnyContent]] =
            action.callRefine(OptionalDataRequest(request, mgdRegNum, None)).futureValue

          result mustBe Left(Redirect(controllers.routes.SystemErrorController.onPageLoad()))
          verify(sessionRepository, never).set(any())
        }
      }
    }

    "when there are User Answers in the cache" - {

      "return them without a call to the backend when the section is already loaded" in {

        val request = FakeRequest()
        val sessionRepository = mock[SessionRepository]
        val gamblingConnector = mock[GamblingConnector]
        val action = new Harness(sessionRepository, gamblingConnector)

        val loadedAnswers = UserAnswers(mgdRegNum, OriginalAnswers(loadedData, Map(LicencesPremisesSection.key -> now)))

        val result: Either[Result, DataRequest[AnyContent]] =
          action.callRefine(OptionalDataRequest(request, mgdRegNum, Some(loadedAnswers))).futureValue

        result mustBe Right(DataRequest(request, mgdRegNum, loadedAnswers))
        verify(sessionRepository, never).set(any())
        verify(gamblingConnector, never).getLicencesAndPremises(any())(any())
      }

      "load the section into the original answers, keeping the other answers, when it is not loaded yet" in {

        val request = FakeRequest()
        val sessionRepository = mock[SessionRepository]
        val gamblingConnector = mock[GamblingConnector]
        when(sessionRepository.set(any())) thenReturn Future(true)
        when(gamblingConnector.getLicencesAndPremises(any())(any())) thenReturn Future(licencesAndPremises)
        val action = new Harness(sessionRepository, gamblingConnector)

        val existingUserAnswers = UserAnswers(mgdRegNum, Json.obj("faxNumber" -> "+_+_ hdj39783"))

        val result: Either[Result, DataRequest[AnyContent]] =
          action.callRefine(OptionalDataRequest(request, mgdRegNum, Some(existingUserAnswers))).futureValue

        result.isRight mustBe true
        result.map { req =>
          req.userAnswers.original.data mustBe Json.obj("faxNumber" -> "+_+_ hdj39783") ++ loadedData
          req.userAnswers.isLoaded(LicencesPremisesSection) mustBe true
          req.userAnswers.modified.data mustBe Json.obj()
        }
        verify(sessionRepository, times(1)).set(any())
        verify(gamblingConnector, times(1)).getLicencesAndPremises(any())(any())
      }
    }
  }
}

object LicencesPremisesDataRequiredActionSpec {

  val now: Instant = Instant.parse("2026-11-10T09:00:00Z")
  val clock: Clock = Clock.fixed(now, ZoneOffset.UTC)

  private val premises = PremisesDetails(
    mgdRegNumber = "XGM000001761",
    address1     = Some("1 Test Street"),
    address2     = Some("Testville"),
    address3     = None,
    address4     = None,
    postcode     = Some("AA1 1AA"),
    systemDate   = Some(LocalDate.of(2026, 9, 21))
  )

  val licencesAndPremises: LicencesAndPremises = LicencesAndPremises(
    mgdRegNumber          = "XGM000001761",
    haveGamblingLicenceNo = Some("1"),
    gamblingLicenceNo     = Some("123-456789-A-123456-789"),
    heldByLandlord        = Some("0"),
    localAuthority        = Some("1"),
    familyEntertainment   = Some("0"),
    clubGaming            = Some("1"),
    clubLicence           = Some("0"),
    prizeGaming           = Some("0"),
    onPremises            = Some("1"),
    clubPremises          = Some("0"),
    regCert               = Some("0"),
    bookmaking            = Some("1"),
    bingo                 = Some("0"),
    amusement             = Some("1"),
    serveAlcohol          = Some("0"),
    premisesNotCovered    = Some("1"),
    premisesDetails       = Some(PremisesDetailsResponse(totalRows = Some(1), premises = Seq(premises)))
  )

  // Every item is optional apart from the registration number
  val emptyLicencesAndPremises: LicencesAndPremises = Json.obj("mgdRegNumber" -> "XGM000001761").as[LicencesAndPremises]

  // Every item the backend returns for the section, apart from the registration number, which is the id of the answers
  val loadedData: JsObject = Json.obj(
    "licencesPremisesSection" -> Json.obj(
      "haveGamblingLicenceNo" -> "1",
      "gamblingLicenceNo"     -> "123-456789-A-123456-789",
      "heldByLandlord"        -> "0",
      "localAuthority"        -> "1",
      "familyEntertainment"   -> "0",
      "clubGaming"            -> "1",
      "clubLicence"           -> "0",
      "prizeGaming"           -> "0",
      "onPremises"            -> "1",
      "clubPremises"          -> "0",
      "regCert"               -> "0",
      "bookmaking"            -> "1",
      "bingo"                 -> "0",
      "amusement"             -> "1",
      "serveAlcohol"          -> "0",
      "premisesNotCovered"    -> "1",
      "premisesDetails" -> Json.obj(
        "totalRows" -> 1,
        "premises"  -> Json.arr(Json.toJson(premises))
      )
    )
  )
}
