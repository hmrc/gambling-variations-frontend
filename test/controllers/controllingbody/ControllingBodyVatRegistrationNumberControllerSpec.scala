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
import controllers.controllingbody.routes.ControllingBodyVatRegistrationNumberController
import forms.controllingbody.ControllingBodyVatRegistrationNumberFormProvider
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.controllingbody.*
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.controllingbody.ControllingBodyVatRegistrationNumberView

import scala.concurrent.Future

class ControllingBodyVatRegistrationNumberControllerSpec extends SpecBase with MockitoSugar {

  private val form = new ControllingBodyVatRegistrationNumberFormProvider()()

  private val vrn = "353868127"

  private lazy val vatRegistrationNumberRoute: String =
    ControllingBodyVatRegistrationNumberController.onPageLoad().url

  private val loadedAnswers: UserAnswers =
    emptyUserAnswers.set(ControllingBodySectionPage, userAnswersId).success.value

  private val answersWithVrn: UserAnswers =
    loadedAnswers.set(ControllingBodyVrnPage, vrn).success.value

  private def submitAndCapture(answers: UserAnswers, value: String): UserAnswers = {

    val mockSessionRepository = mock[SessionRepository]
    val savedAnswersCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])

    when(mockSessionRepository.set(any[UserAnswers])).thenReturn(Future.successful(true))

    val application =
      applicationBuilder(userAnswers = Some(answers))
        .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
        .build()

    running(application) {
      val request = FakeRequest(POST, vatRegistrationNumberRoute).withFormUrlEncodedBody("value" -> value)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.IndexController.onPageLoad().url

      verify(mockSessionRepository).set(savedAnswersCaptor.capture())
      savedAnswersCaptor.getValue
    }
  }

  "ControllingBodyVatRegistrationNumberController" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(loadedAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, vatRegistrationNumberRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ControllingBodyVatRegistrationNumberView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view with the saved VAT registration number on a GET" in {

      val application = applicationBuilder(userAnswers = Some(answersWithVrn)).build()

      running(application) {
        val request = FakeRequest(GET, vatRegistrationNumberRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ControllingBodyVatRegistrationNumberView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(vrn), NormalMode)(request, messages(application)).toString
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(loadedAnswers)).build()

      running(application) {
        val request = FakeRequest(POST, vatRegistrationNumberRoute).withFormUrlEncodedBody("value" -> "353868128")

        val boundForm = form.bind(Map("value" -> "353868128"))

        val result = route(application, request).value

        val view = application.injector.instanceOf[ControllingBodyVatRegistrationNumberView]

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must save a new VAT registration number without spaces and flag the change" in {

      val savedAnswers = submitAndCapture(loadedAnswers, " 353 868 127 ")

      savedAnswers.get(ControllingBodyVrnPage).value mustEqual vrn
      savedAnswers.get(ControllingBodySubmittedPage).value mustEqual true
      savedAnswers.get(ControllingBodyChangesPage).value mustEqual true
    }

    "must not flag a change when the submitted VAT registration number is the saved one" in {

      val savedAnswers = submitAndCapture(answersWithVrn, vrn)

      savedAnswers.get(ControllingBodyVrnPage).value mustEqual vrn
      savedAnswers.get(ControllingBodySubmittedPage).value mustEqual true
      savedAnswers.get(ControllingBodyChangesPage).value mustEqual false
    }

    "must flag a change when the submitted VAT registration number differs from the saved one" in {

      val savedAnswers = submitAndCapture(answersWithVrn, "239088635")

      savedAnswers.get(ControllingBodyVrnPage).value mustEqual "239088635"
      savedAnswers.get(ControllingBodyChangesPage).value mustEqual true
    }

    "must keep an earlier change when the submitted VAT registration number is the saved one" in {

      val answers = answersWithVrn.set(ControllingBodyChangesPage, true).success.value

      submitAndCapture(answers, vrn).get(ControllingBodyChangesPage).value mustEqual true
    }

    "must redirect to System Error for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, vatRegistrationNumberRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.SystemErrorController.onPageLoad().url
      }
    }

    "must redirect to System Error for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(POST, vatRegistrationNumberRoute).withFormUrlEncodedBody("value" -> vrn)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.SystemErrorController.onPageLoad().url
      }
    }
  }
}
