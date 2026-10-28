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
import forms.controllingbody.ControllingBodyUTRFormProvider
import controllers.controllingbody.routes.ControllingBodyUTRController
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.controllingbody.{ControllingBodyChangesPage, ControllingBodySectionPage, ControllingBodySubmittedPage, ControllingBodyUtrPage}
import play.api.data.Form
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.controllingbody.ControllingBodyUTRView

import scala.concurrent.Future

class ControllingBodyUTRControllerSpec extends SpecBase with MockitoSugar {

  private val formProvider = new ControllingBodyUTRFormProvider()
  val form: Form[String] = formProvider()

  private val utr = "1121766916"

  private lazy val utrRoute: String =
    ControllingBodyUTRController.onPageLoad().url

  private lazy val utrSubmitRoute: String =
    ControllingBodyUTRController.onSubmit().url

  private val userAnswers: UserAnswers =
    emptyUserAnswers
      .set(ControllingBodySectionPage, userAnswersId)
      .success
      .value

  private val answersWithUtr: UserAnswers =
    userAnswers
      .set(ControllingBodyUtrPage, utr)
      .success
      .value

  "ControllingBodyUTR Controller" - {

    "must return OK and the correct view for a GET" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .build()

      running(application) {
        val request =
          FakeRequest(GET, utrRoute)

        val result = route(application, request).value

        val view =
          application.injector
            .instanceOf[ControllingBodyUTRView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form, NormalMode)(
            request,
            messages(application)
          ).toString
      }
    }

    "must pre-populate the form when a UTR already exists" in {

      val application =
        applicationBuilder(userAnswers = Some(answersWithUtr))
          .build()

      running(application) {
        val request =
          FakeRequest(GET, utrRoute)

        val result = route(application, request).value

        val view =
          application.injector
            .instanceOf[ControllingBodyUTRView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form.fill(utr), NormalMode)(
            request,
            messages(application)
          ).toString
      }
    }

    "must redirect to System Error when user answers are missing on GET" in {

      val application =
        applicationBuilder(userAnswers = None)
          .build()

      running(application) {
        val request =
          FakeRequest(GET, utrRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.routes.SystemErrorController.onPageLoad().url
      }
    }

    "must return Bad Request when invalid data is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(answersWithUtr))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, utrSubmitRoute)
            .withFormUrlEncodedBody("value" -> "")

        val boundForm = form.bind(Map("value" -> ""))

        val result = route(application, request).value

        val view =
          application.injector
            .instanceOf[ControllingBodyUTRView]

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual
          view(boundForm, NormalMode)(
            request,
            messages(application)
          ).toString
      }
    }

    "must redirect after valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any[UserAnswers]))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, utrSubmitRoute)
            .withFormUrlEncodedBody("value" -> utr)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
      }
    }

    "must update UserAnswers correctly when a UTR is submitted for the first time" in {

      val mockSessionRepository = mock[SessionRepository]
      val savedAnswersCaptor =
        ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockSessionRepository.set(any[UserAnswers]))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, utrSubmitRoute)
            .withFormUrlEncodedBody("value" -> utr)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        verify(mockSessionRepository).set(savedAnswersCaptor.capture())

        val savedAnswers = savedAnswersCaptor.getValue

        savedAnswers.get(ControllingBodyUtrPage).value mustEqual utr
        savedAnswers.get(ControllingBodySubmittedPage).value mustEqual true
        savedAnswers.get(ControllingBodyChangesPage).value mustEqual true
      }
    }

    "must mark changes as false when the submitted UTR has not changed" in {

      val mockSessionRepository = mock[SessionRepository]
      val savedAnswersCaptor =
        ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockSessionRepository.set(any[UserAnswers]))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(answersWithUtr))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, utrSubmitRoute)
            .withFormUrlEncodedBody("value" -> utr)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        verify(mockSessionRepository).set(savedAnswersCaptor.capture())

        val savedAnswers = savedAnswersCaptor.getValue

        savedAnswers.get(ControllingBodyUtrPage).value mustEqual utr
        savedAnswers.get(ControllingBodySubmittedPage).value mustEqual true
        savedAnswers.get(ControllingBodyChangesPage).value mustEqual false
      }
    }

    "must mark changes as true when the submitted UTR differs from the existing UTR" in {

      val mockSessionRepository = mock[SessionRepository]
      val savedAnswersCaptor =
        ArgumentCaptor.forClass(classOf[UserAnswers])

      val existingUtr = "9876543210"

      val answers =
        userAnswers
          .set(ControllingBodyUtrPage, existingUtr)
          .success
          .value

      when(mockSessionRepository.set(any[UserAnswers]))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(answers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, utrSubmitRoute)
            .withFormUrlEncodedBody("value" -> utr)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        verify(mockSessionRepository).set(savedAnswersCaptor.capture())

        val savedAnswers = savedAnswersCaptor.getValue

        savedAnswers.get(ControllingBodyUtrPage).value mustEqual utr
        savedAnswers.get(ControllingBodySubmittedPage).value mustEqual true
        savedAnswers.get(ControllingBodyChangesPage).value mustEqual true
      }
    }

    "must redirect to System Error when user answers are missing on POST" in {

      val application =
        applicationBuilder(userAnswers = None)
          .build()

      running(application) {
        val request =
          FakeRequest(POST, utrSubmitRoute)
            .withFormUrlEncodedBody("value" -> utr)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.routes.SystemErrorController.onPageLoad().url
      }
    }
  }
}
