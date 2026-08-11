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
import models.{Address, BusinessAddress, UserAnswers}
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

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class BusinessAddressDataRequiredActionSpec extends SpecBase with MockitoSugar {

  import BusinessAddressDataRequiredActionSpec.*

  class Harness(sessionRepository: SessionRepository, gamblingConnector: GamblingConnector)
      extends BusinessAddressDataRequiredActionImpl(sessionRepository, gamblingConnector) {
    def callRefine[A](request: OptionalDataRequest[A]): Future[Either[Result, DataRequest[A]]] = refine(request)
  }

  "BusinessAddress DataRequiredAction" - {

    "when there is no User Answers in the cache" - {

      "return the request with a populated User Answers with data from the certificate" - {
        "when the address has a postcode" in {

          val request = FakeRequest()
          val sessionRepository = mock[SessionRepository]
          val gamblingConnector = mock[GamblingConnector]
          when(sessionRepository.set(any())) thenReturn Future(true)
          when(gamblingConnector.getBusinessAddress(any())(any())) thenReturn Future(businessAddressUk)
          val action = new Harness(sessionRepository, gamblingConnector)

          val data = Json.obj(
            "businessAddressSection" -> Json.obj(
              "mgdRegNum" -> "XRM00000000574",
              "businessAddressUk" -> Json.obj(
                "address1" -> "address1",
                "address2" -> "address2",
                "address3" -> "address3",
                "address4" -> "address4",
                "postcode" -> "L1 8YL",
                "country"  -> "England"
              ),
              "businessAddressAdditionalInformation" -> "1st floor",
              "businessAddressIomOrCiFlag"           -> "false"
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
          verify(gamblingConnector, times(1)).getBusinessAddress(any())(any())
        }

        "when the address has no postcode" in {

          val request = FakeRequest()
          val sessionRepository = mock[SessionRepository]
          val gamblingConnector = mock[GamblingConnector]
          when(sessionRepository.set(any())) thenReturn Future(true)
          when(gamblingConnector.getBusinessAddress(any())(any())) thenReturn Future(businessAddressNonUk)
          val action = new Harness(sessionRepository, gamblingConnector)

          val data = Json.obj(
            "businessAddressSection" -> Json.obj(
              "mgdRegNum" -> "XRM00000000574",
              "businessAddressNonUk" -> Json.obj(
                "address1" -> "address1",
                "address2" -> "address2",
                "address3" -> "address3",
                "address4" -> "address4",
                "country"  -> "Spain"
              ),
              "businessAddressAdditionalInformation" -> "1st floor",
              "businessAddressIomOrCiFlag"           -> "false"
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
          verify(gamblingConnector, times(1)).getBusinessAddress(any())(any())
        }

        "when the address is Isle of Man" in {

          val request = FakeRequest()
          val sessionRepository = mock[SessionRepository]
          val gamblingConnector = mock[GamblingConnector]
          when(sessionRepository.set(any())) thenReturn Future(true)
          when(gamblingConnector.getBusinessAddress(any())(any())) thenReturn Future(businessAddressIom)
          val action = new Harness(sessionRepository, gamblingConnector)

          val data = Json.obj(
            "businessAddressSection" -> Json.obj(
              "mgdRegNum" -> "XRM00000000574",
              "businessAddressNonUk" -> Json.obj(
                "address1" -> "address1",
                "address2" -> "address2",
                "address3" -> "address3",
                "address4" -> "address4",
                "country"  -> "Isle of Man"
              ),
              "businessAddressAdditionalInformation" -> "1st floor",
              "businessAddressIomOrCiFlag"           -> "true"
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
          verify(gamblingConnector, times(1)).getBusinessAddress(any())(any())
        }
      }

      "redirect to SystemError " - {
        "User Answers cannot be saved" in {
          val request = FakeRequest()
          val sessionRepository = mock[SessionRepository]
          val gamblingConnector = mock[GamblingConnector]
          when(sessionRepository.set(any())) thenReturn Future(false)
          when(gamblingConnector.getBusinessAddress(any())(any())) thenReturn Future(businessAddressUk)
          val action = new Harness(sessionRepository, gamblingConnector)

          val result: Either[Result, DataRequest[AnyContent]] =
            action.callRefine(OptionalDataRequest(request, mgdRegNum, None)).futureValue

          result mustBe Left(Redirect(controllers.routes.SystemErrorController.onPageLoad()))
          verify(sessionRepository, times(1)).set(any())
          verify(gamblingConnector, times(1)).getBusinessAddress(any())(any())
        }

        "getBusinessAddress throws an exception" in {

          val request = FakeRequest()
          val sessionRepository = mock[SessionRepository]
          val gamblingConnector = mock[GamblingConnector]
          when(sessionRepository.set(any())) thenReturn Future(false)
          when(gamblingConnector.getBusinessAddress(any())(any())) thenReturn Future.failed(UpstreamErrorResponse("Fail", INTERNAL_SERVER_ERROR))
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
            "businessAddressSection" -> Json.obj(
              "mgdRegNum" -> "XRM00000000574"
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
          verify(gamblingConnector, never).getBusinessAddress(any())(any())

        }
        "with call to backend" in {

          val request = FakeRequest()
          val sessionRepository = mock[SessionRepository]
          val gamblingConnector = mock[GamblingConnector]
          when(sessionRepository.set(any())) thenReturn Future(true)
          when(gamblingConnector.getBusinessAddress(any())(any())) thenReturn Future(businessAddressUk)
          val action = new Harness(sessionRepository, gamblingConnector)

          val updatedSessionData = Json.obj(
            "businessNameSection" -> Json.obj(
              "mgdRegNum" -> "ABC12345678901"
            ),
            "businessAddressSection" -> Json.obj(
              "mgdRegNum" -> "XRM00000000574",
              "businessAddressUk" -> Json.obj(
                "address1" -> "address1",
                "address2" -> "address2",
                "address3" -> "address3",
                "address4" -> "address4",
                "postcode" -> "L1 8YL",
                "country"  -> "England"
              ),
              "businessAddressAdditionalInformation" -> "1st floor",
              "businessAddressIomOrCiFlag"           -> "false"
            )
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
          verify(gamblingConnector, times(1)).getBusinessAddress(any())(any())

        }
      }

    }

  }
}

object BusinessAddressDataRequiredActionSpec {
  val businessAddressUk: BusinessAddress = BusinessAddress(
    mgdRegNumber = "XRM00000000574",
    adi          = Some("1st floor"),
    address = Some(
      Address(
        "address1",
        Some("address2"),
        Some("address3"),
        Some("address4"),
        Some("L1 8YL"),
        Some("England")
      )
    ),
    iomOrCiFlag = Some("false")
  )

  val businessAddressNonUk: BusinessAddress = BusinessAddress(
    mgdRegNumber = "XRM00000000574",
    adi          = Some("1st floor"),
    address = Some(
      Address(
        "address1",
        Some("address2"),
        Some("address3"),
        Some("address4"),
        None,
        Some("Spain")
      )
    ),
    iomOrCiFlag = Some("false")
  )

  val businessAddressIom: BusinessAddress = BusinessAddress(
    mgdRegNumber = "XRM00000000574",
    adi          = Some("1st floor"),
    address = Some(
      Address(
        "address1",
        Some("address2"),
        Some("address3"),
        Some("address4"),
        None,
        Some("Isle of Man")
      )
    ),
    iomOrCiFlag = Some("true")
  )

}
