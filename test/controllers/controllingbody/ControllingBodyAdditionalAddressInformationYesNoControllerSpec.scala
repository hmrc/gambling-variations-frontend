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
import forms.controllingbody.ControllingBodyAdditionalAddressInformationYesNoFormProvider
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.controllingbody.{ControllingBodyAdditionalAddressInformationYesNoPage, ControllingBodySectionPage, ControllingBodySubmittedPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.controllingbody.ControllingBodyAdditionalAddressInformationYesNoView

import scala.concurrent.Future

class ControllingBodyAdditionalAddressInformationYesNoControllerSpec
  extends SpecBase
    with MockitoSugar {

  private val formProvider =
    new ControllingBodyAdditionalAddressInformationYesNoFormProvider()
  private val form = formProvider()

  private lazy val controllingBodyAdditionalAddressInformationYesNoRoute =
    routes.ControllingBodyAdditionalAddressInformationYesNoController
      .onPageLoad()
      .url

  private val userAnswers =
    emptyUserAnswers
      .set(ControllingBodySectionPage, userAnswersId)
      .success
      .value

  private val userAnswersWithAnswer =
    userAnswers
      .set(ControllingBodyAdditionalAddressInformationYesNoPage, true)
      .success
      .value

  "ControllingBodyAdditionalAddressInformationYesNoController" - {

    "must return OK and the correct view for a GET when no answer exists" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .build()

      running(application) {
        val request =
          FakeRequest(GET, controllingBodyAdditionalAddressInformationYesNoRoute)

        val result = route(application, request).value

        val view =
          application.injector
            .instanceOf[ControllingBodyAdditionalAddressInformationYesNoView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form,NormalMode)(
            request,
            messages(application)
          ).toString
      }
    }

    "must return OK and the correct view for a GET with an existing answer" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithAnswer))
          .build()

      running(application) {
        val request =
          FakeRequest(GET, controllingBodyAdditionalAddressInformationYesNoRoute)

        val result = route(application, request).value

        val view =
          application.injector
            .instanceOf[ControllingBodyAdditionalAddressInformationYesNoView]

        val filledForm =
          form.fill(true)

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(filledForm,NormalMode)(
            request,
            messages(application)
          ).toString
      }
    }

    "must redirect to the next page when valid true data is submitted" in {

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
          FakeRequest(
            POST,
            routes.ControllingBodyAdditionalAddressInformationYesNoController
              .onSubmit()
              .url
          )
            .withFormUrlEncodedBody("value" -> "true")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
      }
    }

    "must redirect to the next page when valid false data is submitted" in {

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
          FakeRequest(
            POST,
            routes.ControllingBodyAdditionalAddressInformationYesNoController
              .onSubmit()
              .url
          )
            .withFormUrlEncodedBody("value" -> "false")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .build()

      running(application) {
        val request =
          FakeRequest(
            POST,
            routes.ControllingBodyAdditionalAddressInformationYesNoController
              .onSubmit()
              .url
          )
            .withFormUrlEncodedBody("value" -> "")

        val boundForm =
          form.bind(Map("value" -> ""))

        val result = route(application, request).value

        val view =
          application.injector
            .instanceOf[ControllingBodyAdditionalAddressInformationYesNoView]

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual
          view(boundForm,NormalMode)(
            request,
            messages(application)
          ).toString
      }
    }

    "must update UserAnswers correctly when true is submitted" in {

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
          FakeRequest(
            POST,
            routes.ControllingBodyAdditionalAddressInformationYesNoController
              .onSubmit()
              .url
          )
            .withFormUrlEncodedBody("value" -> "true")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        verify(mockSessionRepository).set(savedAnswersCaptor.capture())

        val savedAnswers =
          savedAnswersCaptor.getValue

        savedAnswers
          .get(ControllingBodyAdditionalAddressInformationYesNoPage)
          .value mustEqual true

        savedAnswers
          .get(ControllingBodySubmittedPage)
          .value mustEqual true
      }
    }

    "must update UserAnswers correctly when false is submitted" in {

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
          FakeRequest(
            POST,
            routes.ControllingBodyAdditionalAddressInformationYesNoController
              .onSubmit()
              .url
          )
            .withFormUrlEncodedBody("value" -> "false")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        verify(mockSessionRepository).set(savedAnswersCaptor.capture())

        val savedAnswers =
          savedAnswersCaptor.getValue

        savedAnswers
          .get(ControllingBodyAdditionalAddressInformationYesNoPage)
          .value mustEqual false

        savedAnswers
          .get(ControllingBodySubmittedPage)
          .value mustEqual true
      }
    }

    "must redirect to System Error for a GET if no existing data is found" in {

      val application =
        applicationBuilder(userAnswers = None)
          .build()

      running(application) {
        val request =
          FakeRequest(GET, controllingBodyAdditionalAddressInformationYesNoRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.routes.SystemErrorController.onPageLoad().url
      }
    }

    "must redirect to System Error for a POST if no existing data is found" in {

      val application =
        applicationBuilder(userAnswers = None)
          .build()

      running(application) {
        val request =
          FakeRequest(
            POST,
            routes.ControllingBodyAdditionalAddressInformationYesNoController
              .onSubmit()
              .url
          )
            .withFormUrlEncodedBody("value" -> "true")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.routes.SystemErrorController.onPageLoad().url
      }
    }
  }
}