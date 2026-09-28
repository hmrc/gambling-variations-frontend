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
import models.{BusinessType, SoleProprietorName, UserAnswers}
import models.controllingbody.ControllingBodyDetails
import models.requests.OptionalDataRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, verifyNoInteractions, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.controllingbody.*
import play.api.libs.json.Json
import play.api.mvc.Results.Redirect
import play.api.test.FakeRequest
import repositories.SessionRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ControllingBodyNameDataRequiredActionSpec extends SpecBase with MockitoSugar {
  private class Harness(repository: SessionRepository, connector: GamblingConnector)
      extends ControllingBodyNameDataRequiredActionImpl(repository, connector) {
    def run(answers: Option[UserAnswers]) = refine(OptionalDataRequest(FakeRequest(), mgdRegNum, answers))
  }

  private val backendDetails = ControllingBodyDetails(mgdRegNum, BusinessType.Partnership, Some("Backend Partnership"))

  private trait Setup {
    val repository = mock[SessionRepository]
    val connector = mock[GamblingConnector]
    when(repository.set(any())) thenReturn Future.successful(true)
    when(connector.getControllingBodyDetails(any())(any())) thenReturn Future.successful(backendDetails)
    val action = new Harness(repository, connector)
  }

  "ControllingBodyNameDataRequiredAction" - {
    "load and cache the name and business type when no session exists" in new Setup {
      val result = action.run(None).futureValue.toOption.value.userAnswers
      result.id mustEqual mgdRegNum
      result.get(ControllingBodyBusinessNamePage).value mustEqual "Backend Partnership"
      result.get(ControllingBodyBusinessTypePage).value mustEqual BusinessType.Partnership
      result.get(ControllingBodyDetailsLoadedPage).value mustBe true
      verify(connector).getControllingBodyDetails(org.mockito.ArgumentMatchers.eq(mgdRegNum))(any())
      verify(repository).set(result)
    }

    "fetch the name when only the business type has been cached" in new Setup {
      val answers = emptyUserAnswers.set(ControllingBodyBusinessTypePage, BusinessType.Partnership).success.value
      action.run(Some(answers)).futureValue.toOption.value.userAnswers.get(ControllingBodyBusinessNamePage).value mustEqual "Backend Partnership"
    }

    "use the selected business type when it differs from the backend type" in new Setup {
      val answers = emptyUserAnswers.set(ControllingBodyBusinessTypePage, BusinessType.Soleproprietor).success.value
      val result = action.run(Some(answers)).futureValue.toOption.value.userAnswers
      result.get(ControllingBodyBusinessTypePage).value mustBe BusinessType.Soleproprietor
      result.get(ControllingBodyBusinessNamePage) mustBe None
      result.get(ControllingBodySoleProprietorPage) mustBe None
      result.get(ControllingBodyDetailsLoadedPage).value mustBe true
    }

    "preserve existing name edits and unrelated registration data during initial loading" in new Setup {
      val answers = UserAnswers(userAnswersId, Json.obj("businessName" -> "Registered Business"))
        .set(ControllingBodyBusinessNamePage, "Edited Partnership")
        .success
        .value
      val result = action.run(Some(answers)).futureValue.toOption.value.userAnswers
      result.get(ControllingBodyBusinessNamePage).value mustEqual "Edited Partnership"
      (result.data \ "businessName").as[String] mustEqual "Registered Business"
    }

    "reuse a loaded session without calling the backend or saving again" in new Setup {
      val answers = emptyUserAnswers.set(ControllingBodyDetailsLoadedPage, true).success.value
      action.run(Some(answers)).futureValue.toOption.value.userAnswers mustEqual answers
      verifyNoInteractions(repository, connector)
    }

    "allow an absent business name to be displayed as a blank input" in new Setup {
      when(connector.getControllingBodyDetails(any())(any())) thenReturn Future.successful(backendDetails.copy(businessName = None))
      val answers = action.run(None).futureValue.toOption.value.userAnswers
      answers.get(ControllingBodyBusinessNamePage) mustBe None
      answers.get(ControllingBodyDetailsLoadedPage).value mustBe true
    }

    "load available sole proprietor fields even when some fields are absent" in new Setup {
      val details =
        ControllingBodyDetails(mgdRegNum, BusinessType.Soleproprietor, solePropFirstName = Some("Jane"), solePropLastName = Some("Smith"))
      when(connector.getControllingBodyDetails(any())(any())) thenReturn Future.successful(details)
      val answers = action.run(None).futureValue.toOption.value.userAnswers
      answers.get(ControllingBodySoleProprietorPage).value mustEqual SoleProprietorName("", "Jane", None, "Smith")
    }

    "allow a sole proprietor with no name fields" in new Setup {
      when(connector.getControllingBodyDetails(any())(any())) thenReturn Future.successful(
        ControllingBodyDetails(mgdRegNum, BusinessType.Soleproprietor)
      )
      action.run(None).futureValue.toOption.value.userAnswers.get(ControllingBodySoleProprietorPage) mustBe None
    }

    "preserve an edited sole proprietor name" in new Setup {
      val name = SoleProprietorName("Ms", "Jane", None, "Jones")
      val answers = emptyUserAnswers.set(ControllingBodySoleProprietorPage, name).success.value
      when(connector.getControllingBodyDetails(any())(any())) thenReturn Future.successful(
        ControllingBodyDetails(mgdRegNum, BusinessType.Soleproprietor, solePropFirstName = Some("Old"))
      )
      action.run(Some(answers)).futureValue.toOption.value.userAnswers.get(ControllingBodySoleProprietorPage).value mustEqual name
    }

    "redirect to the service error when the backend fails" in new Setup {
      when(connector.getControllingBodyDetails(any())(any())) thenReturn Future.failed(new RuntimeException("Backend unavailable"))
      action.run(None).futureValue mustBe Left(Redirect(controllers.routes.SystemErrorController.onPageLoad()))
      verifyNoInteractions(repository)
    }

    "redirect to the service error when caching is unsuccessful" in new Setup {
      when(repository.set(any())) thenReturn Future.successful(false)
      action.run(None).futureValue mustBe Left(Redirect(controllers.routes.SystemErrorController.onPageLoad()))
    }

    "redirect to the service error when caching fails" in new Setup {
      when(repository.set(any())) thenReturn Future.failed(new RuntimeException("Cache unavailable"))
      action.run(None).futureValue mustBe Left(Redirect(controllers.routes.SystemErrorController.onPageLoad()))
    }
  }
}
