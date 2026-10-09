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
import models.controllingbody.ControlBodyDetails
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

class ControllingBodyDetailsLoadingSpec extends SpecBase with MockitoSugar {
  private class Harness(repository: SessionRepository, connector: GamblingConnector)
      extends ControllingBodyDetailsDataRequiredActionImpl(repository, connector) {
    def run(answers: Option[UserAnswers]) = refine(OptionalDataRequest(FakeRequest(), mgdRegNum, answers))
  }

  private val backendDetails = Json
    .obj(
      "mgdRegNumber"          -> mgdRegNum,
      "typeOfControllingBody" -> 4,
      "businessName"          -> "Backend Partnership"
    )
    .as[ControlBodyDetails]

  private trait Setup {
    val repository = mock[SessionRepository]
    val connector = mock[GamblingConnector]
    when(repository.set(any())) thenReturn Future.successful(true)
    when(connector.getControlBodyDetails(any())(any())) thenReturn Future.successful(backendDetails)
    val action = new Harness(repository, connector)
  }

  "ControllingBodyDetailsLoading" - {
    "fetch the full section once and retain edits on subsequent visits" in new Setup {
      val loaded = action.run(None).futureValue.toOption.value.userAnswers
      val edited = loaded.set(ControllingBodyBusinessNamePage, "Edited name").success.value
      action.run(Some(edited)).futureValue.toOption.value.userAnswers mustBe edited
      verify(connector).getControlBodyDetails(any())(any())
      verify(repository).set(loaded)
    }

    "fetch full details for an old name-only cache while preserving its edits" in new Setup {
      val answers = UserAnswers(mgdRegNum,
                                Json.obj(
                                  "controllingBodyDetails" -> Json.obj(
                                    "loaded"                -> true,
                                    "typeOfControllingBody" -> 4,
                                    "businessName"          -> "Edited name"
                                  )
                                )
                               )
      when(connector.getControlBodyDetails(any())(any())) thenReturn
        Future.successful(ControllingBodyDetailsDataRequiredActionSpec.controlBodyDetails)
      val result = action.run(Some(answers)).futureValue.toOption.value.userAnswers
      result.get(ControllingBodyBusinessNamePage).value mustBe "Edited name"
      result.get(ControllingBodyBusinessTypePage).value mustBe BusinessType.Partnership
      result.get(ControllingBodyCorrespondenceSectionPage).isDefined mustBe true
      verify(connector).getControlBodyDetails(org.mockito.ArgumentMatchers.eq(mgdRegNum))(any())
    }

    "load and cache the name and business type when no session exists" in new Setup {
      val result = action.run(None).futureValue.toOption.value.userAnswers
      result.id mustEqual mgdRegNum
      result.get(ControllingBodyBusinessNamePage).value mustEqual "Backend Partnership"
      result.get(ControllingBodyBusinessTypePage).value mustEqual BusinessType.Partnership
      result.get(ControllingBodySectionPage).value mustBe mgdRegNum
      verify(connector).getControlBodyDetails(org.mockito.ArgumentMatchers.eq(mgdRegNum))(any())
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
      result.get(ControllingBodyBusinessNamePage).value mustBe "Backend Partnership"
      result.get(ControllingBodySoleProprietorPage) mustBe None
      result.get(ControllingBodySectionPage).value mustBe mgdRegNum
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
      val answers = emptyUserAnswers.set(ControllingBodySectionPage, mgdRegNum).success.value
      action.run(Some(answers)).futureValue.toOption.value.userAnswers mustEqual answers
      verifyNoInteractions(repository, connector)
    }

    "allow an absent business name to be displayed as a blank input" in new Setup {
      when(connector.getControlBodyDetails(any())(any())) thenReturn Future.successful(backendDetails.copy(businessName = None))
      val answers = action.run(None).futureValue.toOption.value.userAnswers
      answers.get(ControllingBodyBusinessNamePage) mustBe None
      answers.get(ControllingBodySectionPage).value mustBe mgdRegNum
    }

    "load available sole proprietor fields even when some fields are absent" in new Setup {
      val details =
        backendDetails.copy(typeOfControllingBody = Some(BusinessType.Soleproprietor),
                            solePropFirstName     = Some("Jane"),
                            solePropLastName      = Some("Smith")
                           )
      when(connector.getControlBodyDetails(any())(any())) thenReturn Future.successful(details)
      val answers = action.run(None).futureValue.toOption.value.userAnswers
      answers.get(ControllingBodySoleProprietorPage).value mustEqual SoleProprietorName("", "Jane", None, "Smith")
    }

    "allow a sole proprietor with no name fields" in new Setup {
      when(connector.getControlBodyDetails(any())(any())) thenReturn Future.successful(
        backendDetails.copy(typeOfControllingBody = Some(BusinessType.Soleproprietor))
      )
      action.run(None).futureValue.toOption.value.userAnswers.get(ControllingBodySoleProprietorPage) mustBe None
    }

    "preserve an edited sole proprietor name" in new Setup {
      val name = SoleProprietorName("Ms", "Jane", None, "Jones")
      val answers = emptyUserAnswers.set(ControllingBodySoleProprietorPage, name).success.value
      when(connector.getControlBodyDetails(any())(any())) thenReturn Future.successful(
        backendDetails.copy(typeOfControllingBody = Some(BusinessType.Soleproprietor), solePropFirstName = Some("Old"))
      )
      action.run(Some(answers)).futureValue.toOption.value.userAnswers.get(ControllingBodySoleProprietorPage).value mustEqual name
    }

    "redirect to the service error when the backend fails" in new Setup {
      when(connector.getControlBodyDetails(any())(any())) thenReturn Future.failed(new RuntimeException("Backend unavailable"))
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
