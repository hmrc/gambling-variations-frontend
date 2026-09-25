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
import models.controllingbody.ControllingBody
import models.requests.{DataRequest, OptionalDataRequest}
import models.{Address, ContactNumber, CorrespondenceDetails, SoleProprietorName, UserAnswers}
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import pages.controllingbody.ControllingBodySectionPage
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.libs.json.Json
import play.api.mvc.Results.*
import play.api.mvc.{AnyContent, Result}
import play.api.test.FakeRequest
import repositories.SessionRepository
import uk.gov.hmrc.http.UpstreamErrorResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ControllingBodyDataRequiredActionSpec extends SpecBase with MockitoSugar {

  import ControllingBodyDataRequiredActionSpec.*

  class Harness(sessionRepository: SessionRepository, gamblingConnector: GamblingConnector)
      extends ControllingBodyDataRequiredActionImpl(sessionRepository, gamblingConnector) {
    def callRefine[A](request: OptionalDataRequest[A]): Future[Either[Result, DataRequest[A]]] = refine(request)
  }

  "ControllingBody DataRequiredAction" - {

    "when there is no User Answers in the cache" - {

      "return the request with a populated User Answers with data from the backend" in {

        val request = FakeRequest()
        val sessionRepository = mock[SessionRepository]
        val gamblingConnector = mock[GamblingConnector]

        when(sessionRepository.set(any())) thenReturn Future(true)
        when(gamblingConnector.getControllingBody(any())(any())) thenReturn Future(controllingBody)

        val action = new Harness(sessionRepository, gamblingConnector)

        val data = Json.obj(
          "controllingBodySection" -> Json.obj(
            "mgdRegNum"             -> "XGM00000001761",
            "businessPartnerNumber" -> "0100053091",
            "dateOfJoining"         -> "2013-02-01",
            "dateOfLeaving"         -> "2023-03-01",
            "correspondenceSection" -> Json.obj(
              "mgdRegNumber" -> "XGM00000001761",
              "correspondenceAddress" -> Json.obj(
                "address1" -> "Address 1",
                "address2" -> "Address 2",
                "address3" -> "Address 3",
                "address4" -> "Address 4",
                "postcode" -> "postcode",
                "country"  -> "Spain"
              ),
              "additionalInformation" -> "adi",
              "iomOrCiFlag"           -> "0",
              "contactNumber" -> Json.obj(
                "phoneNumber"       -> "phoneNumber",
                "mobilePhoneNumber" -> "mobilePhoneNumber"
              ),
              "faxNumber" -> "faxNumber",
              "emailAddr" -> "emailAddr"
            ),
            "dateOfIncorporation"    -> "2020-02-15",
            "countryOfIncorporation" -> "Spain",
            "foreignCorporateRef"    -> "foreignCorporateRef",
            "dateOfBirth"            -> "1998-06-24",
            "nino"                   -> "AB123456C",
            "utr"                    -> "5202020208",
            "vrn"                    -> "127207785",
            "crn"                    -> "12345678",
            "businessName"           -> "BRUCE HOPKINS LIMITED",
            "tradingName"            -> "Trading name 1",
            "typeOfControllingBody"  -> "corporatebody",
            "isRepMemSameAsCb"       -> "0",
            "isUkIncorporated"       -> "0",
            "soleProprietor" -> Json.obj(
              "title"      -> "Mx",
              "firstName"  -> "solePropFirstName",
              "middleName" -> "solePropMiddleName",
              "lastName"   -> "solePropLastName"
            )
          )
        )

        val result: Either[Result, DataRequest[AnyContent]] =
          action.callRefine(OptionalDataRequest(request, mgdRegNum, None)).futureValue

        val expected =
          DataRequest(request, mgdRegNum, UserAnswers(mgdRegNum, data))

        result.map { req =>
          req.request mustBe expected.request
          req.userAnswers.data mustBe expected.userAnswers.data
          req.userAnswers.id mustBe expected.userAnswers.id
        }

        verify(sessionRepository, times(1)).set(any())
        verify(gamblingConnector, times(1)).getControllingBody(any())(any())
      }

      "redirect to SystemError" - {

        "when User Answers cannot be saved" in {

          val request = FakeRequest()
          val sessionRepository = mock[SessionRepository]
          val gamblingConnector = mock[GamblingConnector]

          when(sessionRepository.set(any())) thenReturn Future(false)
          when(gamblingConnector.getControllingBody(any())(any())) thenReturn Future(controllingBody)

          val action = new Harness(sessionRepository, gamblingConnector)

          val result: Either[Result, DataRequest[AnyContent]] =
            action.callRefine(OptionalDataRequest(request, mgdRegNum, None)).futureValue

          result mustBe Left(Redirect(controllers.routes.SystemErrorController.onPageLoad()))

          verify(sessionRepository, times(1)).set(any())
          verify(gamblingConnector, times(1)).getControllingBody(any())(any())
        }

        "when getControllingBody throws an exception" in {

          val request = FakeRequest()
          val sessionRepository = mock[SessionRepository]
          val gamblingConnector = mock[GamblingConnector]

          when(gamblingConnector.getControllingBody(any())(any())) thenReturn Future.failed(
            UpstreamErrorResponse("Fail", INTERNAL_SERVER_ERROR)
          )

          val action = new Harness(sessionRepository, gamblingConnector)

          val result: Either[Result, DataRequest[AnyContent]] =
            action.callRefine(OptionalDataRequest(request, mgdRegNum, None)).futureValue

          result mustBe Left(Redirect(controllers.routes.SystemErrorController.onPageLoad()))

          verify(sessionRepository, never()).set(any())
          verify(gamblingConnector, times(1)).getControllingBody(any())(any())
        }
      }
    }

    "when there are User Answers in the cache" - {

      "return the request with a populated User Answers" - {

        "without call to backend" in {

          val request = FakeRequest()
          val sessionRepository = mock[SessionRepository]
          val gamblingConnector = mock[GamblingConnector]

          val data = Json.obj(
            "controllingBodySection" -> Json.obj(
              "mgdRegNum" -> "XGM00000001761"
            )
          )

          val userAnswers = UserAnswers(mgdRegNum, data)

          val action = new Harness(sessionRepository, gamblingConnector)

          val result: Either[Result, DataRequest[AnyContent]] =
            action.callRefine(OptionalDataRequest(request, mgdRegNum, Some(userAnswers))).futureValue

          val expected = DataRequest(request, mgdRegNum, userAnswers)

          result.map { req =>
            req.request mustBe expected.request
            req.userAnswers.data mustBe expected.userAnswers.data
            req.userAnswers.id mustBe expected.userAnswers.id
          }

          verify(sessionRepository, never()).set(any())
          verify(gamblingConnector, never()).getControllingBody(any())(any())
        }

        "with call to backend" in {

          val request = FakeRequest()
          val sessionRepository = mock[SessionRepository]
          val gamblingConnector = mock[GamblingConnector]

          when(sessionRepository.set(any())) thenReturn Future(true)
          when(gamblingConnector.getControllingBody(any())(any())) thenReturn Future(controllingBody)

          val existingUserAnswers =
            UserAnswers(mgdRegNum,
                        Json.obj(
                          "businessNameSection" -> Json.obj(
                            "mgdRegNum" -> "ABC12345678901"
                          )
                        )
                       )

          val action = new Harness(sessionRepository, gamblingConnector)

          val result: Either[Result, DataRequest[AnyContent]] =
            action.callRefine(OptionalDataRequest(request, mgdRegNum, Some(existingUserAnswers))).futureValue

          result match {
            case Right(req) =>
              req.request mustBe request
              req.userAnswers.id mustBe mgdRegNum

              req.userAnswers.get(ControllingBodySectionPage) mustBe
                Some("XGM00000001761")

            case Left(result) =>
              fail(s"Expected Right but got Left($result)")
          }

          verify(sessionRepository, times(1)).set(any())
          verify(gamblingConnector, times(1)).getControllingBody(any())(any())
        }
      }
    }
  }
}

object ControllingBodyDataRequiredActionSpec {

  val controllingBody: ControllingBody =
    ControllingBody(
      mgdRegNumber           = "XGM00000001761",
      businessPartnerNumber  = Some("0100053091"),
      dateOfJoining          = Some(java.time.LocalDate.parse("2013-02-01")),
      dateOfLeaving          = Some(java.time.LocalDate.parse("2023-03-01")),
      solePropTitle          = Some("Mx"),
      solePropFirstName      = Some("solePropFirstName"),
      solePropMiddleName     = Some("solePropMiddleName"),
      solePropLastName       = Some("solePropLastName"),
      businessName           = Some("BRUCE HOPKINS LIMITED"),
      tradingName            = Some("Trading name 1"),
      dateOfBirth            = Some(java.time.LocalDate.parse("1998-06-24")),
      nino                   = Some("AB123456C"),
      utr                    = Some(5202020208L),
      vrn                    = Some(127207785L),
      crn                    = Some("12345678"),
      dateOfIncorporation    = Some(java.time.LocalDate.parse("2020-02-15")),
      countryOfIncorporation = Some("Spain"),
      foreignCorporateRef    = Some("foreignCorporateRef"),
      address1               = Some("Address 1"),
      address2               = Some("Address 2"),
      address3               = Some("Address 3"),
      address4               = Some("Address 4"),
      postcode               = Some("postcode"),
      country                = Some("Spain"),
      adi                    = Some("adi"),
      isIomOrCiFlag          = Some("0"),
      phoneNumber            = Some("phoneNumber"),
      mobilePhoneNumber      = Some("mobilePhoneNumber"),
      faxNumber              = Some("faxNumber"),
      emailAddr              = Some("emailAddr"),
      typeOfControllingBody  = Some(models.BusinessType.Corporatebody),
      isRepMemSameAsCb       = Some("0"),
      isUkIncorporated       = Some("0")
    )

  val correspondenceDetails: CorrespondenceDetails =
    CorrespondenceDetails(
      mgdRegNumber = "XGM00000001761",
      nameLine1    = None,
      nameLine2    = None,
      correspondenceAddress = Some(
        Address(
          address1 = "Address 1",
          address2 = Some("Address 2"),
          address3 = Some("Address 3"),
          address4 = Some("Address 4"),
          postcode = Some("postcode"),
          country  = Some("Spain")
        )
      ),
      additionalInformation = Some("adi"),
      iomOrCiFlag           = Some("0"),
      contactNumber = Some(
        ContactNumber(
          phoneNumber       = Some("phoneNumber"),
          mobilePhoneNumber = Some("mobilePhoneNumber")
        )
      ),
      faxNumber = Some("faxNumber"),
      emailAddr = Some("emailAddr")
    )

  val soleProprietorName: SoleProprietorName =
    SoleProprietorName(
      title      = "Mx",
      firstName  = "solePropFirstName",
      middleName = Some("solePropMiddleName"),
      lastName   = "solePropLastName"
    )
}
