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

package controllers.controllingbody

import base.SpecBase
import connectors.GamblingConnector
import models.BusinessType.Corporatebody
import models.UserAnswers
import models.controllingbody.ControlBodyDetails
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.controllingbody.{ControllingBodyBusinessNamePage, ControllingBodyBusinessTypePage, ControllingBodySectionPage}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import viewmodels.checkAnswers.controllingbody.CheckControllingBodyDetailsViewModel
import views.html.controllingbody.CheckControllingBodyDetailsView

import scala.concurrent.Future

class CheckControllingBodyDetailsControllerSpec extends SpecBase with MockitoSugar {

  private lazy val checkControllingBodyDetailsRoute = routes.CheckControllingBodyDetailsController.onPageLoad().url

  private val userAnswers: UserAnswers =
    (for {
      answers <- emptyUserAnswers.set(ControllingBodySectionPage, mgdRegNum)
      answers <- answers.set(ControllingBodyBusinessTypePage, Corporatebody)
      answers <- answers.set(ControllingBodyBusinessNamePage, "BRUCE HOPKINS LIMITED")
    } yield answers).success.value

  private val controlBodyDetails: ControlBodyDetails =
    Json.obj("mgdRegNumber" -> mgdRegNum, "businessName" -> "BRUCE HOPKINS LIMITED", "typeOfControllingBody" -> 2).as[ControlBodyDetails]

  "CheckControllingBodyDetails Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, checkControllingBodyDetailsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckControllingBodyDetailsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(CheckControllingBodyDetailsViewModel.from(userAnswers))(request, messages(application)).toString
      }
    }

    "must load the controlling body details from the backend when they are not in the session yet" in {

      val mockSessionRepository = mock[SessionRepository]
      val mockGamblingConnector = mock[GamblingConnector]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockGamblingConnector.getControlBodyDetails(any())(any())) thenReturn Future.successful(controlBodyDetails)

      val application =
        applicationBuilder(userAnswers = None)
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[GamblingConnector].toInstance(mockGamblingConnector)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, checkControllingBodyDetailsRoute)

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) must include("BRUCE HOPKINS LIMITED")
      }
    }

    "must redirect to the system error page when the controlling body details cannot be loaded" in {

      val mockGamblingConnector = mock[GamblingConnector]

      when(mockGamblingConnector.getControlBodyDetails(any())(any())) thenReturn Future.failed(new RuntimeException("backend unavailable"))

      val application =
        applicationBuilder(userAnswers = None)
          .overrides(bind[GamblingConnector].toInstance(mockGamblingConnector))
          .build()

      running(application) {
        val request = FakeRequest(GET, checkControllingBodyDetailsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.SystemErrorController.onPageLoad().url
      }
    }
  }
}
