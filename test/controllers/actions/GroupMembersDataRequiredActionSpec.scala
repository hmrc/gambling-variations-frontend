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
import models.groupmembers.{GroupMember, GroupMembers}
import models.requests.{DataRequest, OptionalDataRequest}
import models.{OriginalAnswers, UserAnswers}
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import pages.groupmembers.{GroupMemberBusinessNamePage, GroupMemberDateOfLeavingPage, GroupMembersSection}
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

class GroupMembersDataRequiredActionSpec extends SpecBase with MockitoSugar {

  import GroupMembersDataRequiredActionSpec.*

  class Harness(sessionRepository: SessionRepository, gamblingConnector: GamblingConnector)
      extends GroupMembersDataRequiredActionImpl(sessionRepository, gamblingConnector, clock) {
    def callRefine[A](request: OptionalDataRequest[A]): Future[Either[Result, DataRequest[A]]] = refine(request)
  }

  "GroupMembers DataRequiredAction" - {

    "when there is no User Answers in the cache" - {

      "return the request with User Answers holding every group member as original answers" in {

        val request = FakeRequest()
        val sessionRepository = mock[SessionRepository]
        val gamblingConnector = mock[GamblingConnector]
        when(sessionRepository.set(any())) thenReturn Future(true)
        when(gamblingConnector.getGroupMembers(any())(any())) thenReturn Future(groupMembers)
        val action = new Harness(sessionRepository, gamblingConnector)

        val result: Either[Result, DataRequest[AnyContent]] =
          action.callRefine(OptionalDataRequest(request, mgdRegNum, None)).futureValue

        result.isRight mustBe true
        result.map { req =>
          req.request mustBe request
          req.userAnswers.id mustBe mgdRegNum
          req.userAnswers.original.data mustBe loadedData
          req.userAnswers.original.loaded mustBe Map(GroupMembersSection.key -> now)
          req.userAnswers.modified.data mustBe Json.obj()
          req.userAnswers.hasChanges(GroupMembersSection) mustBe false
        }
        verify(sessionRepository, times(1)).set(any())
        verify(gamblingConnector, times(1)).getGroupMembers(any())(any())
      }

      "keep a member who has left the group" in {

        val request = FakeRequest()
        val sessionRepository = mock[SessionRepository]
        val gamblingConnector = mock[GamblingConnector]
        when(sessionRepository.set(any())) thenReturn Future(true)
        when(gamblingConnector.getGroupMembers(any())(any())) thenReturn Future(groupMembers)
        val action = new Harness(sessionRepository, gamblingConnector)

        val result: Either[Result, DataRequest[AnyContent]] =
          action.callRefine(OptionalDataRequest(request, mgdRegNum, None)).futureValue

        result.isRight mustBe true
        result.map { req =>
          req.userAnswers.get(GroupMemberBusinessNamePage(formerMember.businessPartnerNumber)) mustBe formerMember.businessName
          req.userAnswers.get(GroupMemberDateOfLeavingPage(formerMember.businessPartnerNumber)) mustBe formerMember.dateOfLeaving
        }
      }

      "mark the section as loaded when the backend returns no group members" in {

        val request = FakeRequest()
        val sessionRepository = mock[SessionRepository]
        val gamblingConnector = mock[GamblingConnector]
        when(sessionRepository.set(any())) thenReturn Future(true)
        when(gamblingConnector.getGroupMembers(any())(any())) thenReturn Future(GroupMembers(Nil))
        val action = new Harness(sessionRepository, gamblingConnector)

        val result: Either[Result, DataRequest[AnyContent]] =
          action.callRefine(OptionalDataRequest(request, mgdRegNum, None)).futureValue

        result.isRight mustBe true
        result.map { req =>
          req.userAnswers.original.data mustBe Json.obj()
          req.userAnswers.isLoaded(GroupMembersSection) mustBe true
        }
      }

      "redirect to SystemError" - {

        "when the User Answers cannot be saved" in {

          val request = FakeRequest()
          val sessionRepository = mock[SessionRepository]
          val gamblingConnector = mock[GamblingConnector]
          when(sessionRepository.set(any())) thenReturn Future(false)
          when(gamblingConnector.getGroupMembers(any())(any())) thenReturn Future(groupMembers)
          val action = new Harness(sessionRepository, gamblingConnector)

          val result: Either[Result, DataRequest[AnyContent]] =
            action.callRefine(OptionalDataRequest(request, mgdRegNum, None)).futureValue

          result mustBe Left(Redirect(controllers.routes.SystemErrorController.onPageLoad()))
          verify(sessionRepository, times(1)).set(any())
          verify(gamblingConnector, times(1)).getGroupMembers(any())(any())
        }

        "when getGroupMembers fails" in {

          val request = FakeRequest()
          val sessionRepository = mock[SessionRepository]
          val gamblingConnector = mock[GamblingConnector]
          when(gamblingConnector.getGroupMembers(any())(any())) thenReturn Future.failed(UpstreamErrorResponse("Fail", INTERNAL_SERVER_ERROR))
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

        val loadedAnswers = UserAnswers(mgdRegNum, OriginalAnswers(loadedData, Map(GroupMembersSection.key -> now)))

        val result: Either[Result, DataRequest[AnyContent]] =
          action.callRefine(OptionalDataRequest(request, mgdRegNum, Some(loadedAnswers))).futureValue

        result mustBe Right(DataRequest(request, mgdRegNum, loadedAnswers))
        verify(sessionRepository, never).set(any())
        verify(gamblingConnector, never).getGroupMembers(any())(any())
      }

      "load the section into the original answers, keeping the other answers, when it is not loaded yet" in {

        val request = FakeRequest()
        val sessionRepository = mock[SessionRepository]
        val gamblingConnector = mock[GamblingConnector]
        when(sessionRepository.set(any())) thenReturn Future(true)
        when(gamblingConnector.getGroupMembers(any())(any())) thenReturn Future(groupMembers)
        val action = new Harness(sessionRepository, gamblingConnector)

        val existingUserAnswers = UserAnswers(mgdRegNum, Json.obj("faxNumber" -> "+_+_ hdj39783"))

        val result: Either[Result, DataRequest[AnyContent]] =
          action.callRefine(OptionalDataRequest(request, mgdRegNum, Some(existingUserAnswers))).futureValue

        result.isRight mustBe true
        result.map { req =>
          req.userAnswers.original.data mustBe Json.obj("faxNumber" -> "+_+_ hdj39783") ++ loadedData
          req.userAnswers.isLoaded(GroupMembersSection) mustBe true
          req.userAnswers.modified.data mustBe Json.obj()
        }
        verify(sessionRepository, times(1)).set(any())
        verify(gamblingConnector, times(1)).getGroupMembers(any())(any())
      }
    }
  }
}

object GroupMembersDataRequiredActionSpec {

  val now: Instant = Instant.parse("2026-11-20T09:00:00Z")
  val clock: Clock = Clock.fixed(now, ZoneOffset.UTC)

  val currentMember: GroupMember = GroupMember(
    mgdRegNumber          = "XGM00000001772",
    businessPartnerNumber = "0100001772",
    businessName          = Some("Current Member Ltd"),
    crn                   = Some("12345678"),
    foreignCorporateRef   = Some("FCR123"),
    isRepMemberOfGroup    = Some("1"),
    dateOfJoining         = Some(LocalDate.of(2025, 5, 31)),
    dateOfLeaving         = None,
    emailAddr             = Some("test@example.com"),
    systemDate            = Some(LocalDate.of(2026, 11, 18))
  )

  val formerMember: GroupMember = GroupMember(
    mgdRegNumber          = "XGM00000001772",
    businessPartnerNumber = "0200001772",
    businessName          = Some("Former Member Ltd"),
    crn                   = None,
    foreignCorporateRef   = None,
    isRepMemberOfGroup    = Some("0"),
    dateOfJoining         = Some(LocalDate.of(2013, 2, 1)),
    dateOfLeaving         = Some(LocalDate.of(2023, 3, 1)),
    emailAddr             = None,
    systemDate            = Some(LocalDate.of(2026, 11, 18))
  )

  val groupMembers: GroupMembers = GroupMembers(Seq(currentMember, formerMember))

  // Every item the backend returns for each member, apart from the registration number, which is the id of the
  // answers, the business partner number, which is the key of the member, and the system date
  val loadedData: JsObject = Json.obj(
    "groupMembersSection" -> Json.obj(
      "0100001772" -> Json.obj(
        "businessName"        -> "Current Member Ltd",
        "crn"                 -> "12345678",
        "foreignCorporateRef" -> "FCR123",
        "isRepMemberOfGroup"  -> "1",
        "dateOfJoining"       -> "2025-05-31",
        "emailAddr"           -> "test@example.com"
      ),
      "0200001772" -> Json.obj(
        "businessName"       -> "Former Member Ltd",
        "isRepMemberOfGroup" -> "0",
        "dateOfJoining"      -> "2013-02-01",
        "dateOfLeaving"      -> "2023-03-01"
      )
    )
  )
}
