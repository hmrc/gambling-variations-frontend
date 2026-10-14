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
import forms.controllingbody.RemoveControllingBodyTradeNameFormProvider
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
import views.html.controllingbody.RemoveControllingBodyTradeNameView

import scala.concurrent.Future

class RemoveControllingBodyTradeNameControllerSpec extends SpecBase with MockitoSugar {

  private val formProvider = new RemoveControllingBodyTradeNameFormProvider()
  private val form = formProvider()

  private val tradingName = "Test Trading Name"

  private lazy val removeControllingBodyTradeNameRoute =
    routes.RemoveControllingBodyTradeNameController.onPageLoad().url

  private val userAnswersWithTradingName: UserAnswers =
    emptyUserAnswers
      .set(ControllingBodySectionPage, userAnswersId)
      .success
      .value
      .set(ControllingBodyTradingNamePage, tradingName)
      .success
      .value

  private val userAnswersWithoutTradingName: UserAnswers =
    emptyUserAnswers
      .set(ControllingBodySectionPage, userAnswersId)
      .success
      .value

  "RemoveControllingBodyTradeNameController Controller" - {

    "must return OK and the correct view for a GET" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithTradingName))
          .build()

      running(application) {
        val request =
          FakeRequest(GET, removeControllingBodyTradeNameRoute)

        val result = route(application, request).value

        val view =
          application.injector.instanceOf[RemoveControllingBodyTradeNameView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form, NormalMode, tradingName)(
            request,
            messages(application)
          ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any[UserAnswers]))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithTradingName))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(
            POST,
            routes.RemoveControllingBodyTradeNameController
              .onSubmit()
              .url
          )
            .withFormUrlEncodedBody("value" -> "true")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithTradingName))
          .build()

      running(application) {
        val request =
          FakeRequest(
            POST,
            routes.RemoveControllingBodyTradeNameController
              .onSubmit()
              .url
          )
            .withFormUrlEncodedBody("value" -> "")

        val boundForm = form.bind(Map("value" -> ""))

        val result = route(application, request).value

        val view =
          application.injector.instanceOf[RemoveControllingBodyTradeNameView]

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual
          view(boundForm, NormalMode, tradingName)(
            request,
            messages(application)
          ).toString
      }
    }

    "must redirect to System Error for a GET if no existing trading name is found" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithoutTradingName))
          .build()

      running(application) {
        val request =
          FakeRequest(GET, removeControllingBodyTradeNameRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.routes.SystemErrorController.onPageLoad().url
      }
    }

    "must redirect to System Error for a POST if no existing trading name is found" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithoutTradingName))
          .build()

      running(application) {
        val request =
          FakeRequest(
            POST,
            routes.RemoveControllingBodyTradeNameController
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

    "must redirect to System Error for a GET if no existing data is found" in {

      val application =
        applicationBuilder(userAnswers = None)
          .build()

      running(application) {
        val request =
          FakeRequest(GET, removeControllingBodyTradeNameRoute)

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
            routes.RemoveControllingBodyTradeNameController
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

    "must update UserAnswers correctly when true is submitted" in {

      val mockSessionRepository = mock[SessionRepository]
      val savedAnswersCaptor =
        ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockSessionRepository.set(any[UserAnswers]))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithTradingName))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(
            POST,
            routes.RemoveControllingBodyTradeNameController
              .onSubmit()
              .url
          )
            .withFormUrlEncodedBody("value" -> "true")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        verify(mockSessionRepository).set(savedAnswersCaptor.capture())

        val savedAnswers = savedAnswersCaptor.getValue

        savedAnswers.get(RemoveControllingBodyTradeNamePage).value mustEqual true
        savedAnswers.get(ControllingBodySubmittedPage).value mustEqual true
        savedAnswers.get(ControllingBodyChangesPage).value mustEqual true
        savedAnswers.get(ControllingBodyTradingNamePage) mustEqual None
      }
    }

    "must update UserAnswers correctly when false is submitted" in {

      val mockSessionRepository = mock[SessionRepository]
      val savedAnswersCaptor =
        ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockSessionRepository.set(any[UserAnswers]))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithTradingName))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(
            POST,
            routes.RemoveControllingBodyTradeNameController
              .onSubmit()
              .url
          )
            .withFormUrlEncodedBody("value" -> "false")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        verify(mockSessionRepository).set(savedAnswersCaptor.capture())

        val savedAnswers = savedAnswersCaptor.getValue

        savedAnswers.get(RemoveControllingBodyTradeNamePage).value mustEqual false
        savedAnswers.get(ControllingBodySubmittedPage).value mustEqual true
        savedAnswers.get(ControllingBodyChangesPage).value mustEqual false
        savedAnswers.get(ControllingBodyTradingNamePage).value mustEqual tradingName
      }
    }

    "must keep an earlier change to the section when false is submitted" in {

      val mockSessionRepository = mock[SessionRepository]
      val savedAnswersCaptor =
        ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockSessionRepository.set(any[UserAnswers]))
        .thenReturn(Future.successful(true))

      val answersWithEarlierChange =
        userAnswersWithTradingName
          .set(ControllingBodyChangesPage, true)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(answersWithEarlierChange))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(
            POST,
            routes.RemoveControllingBodyTradeNameController
              .onSubmit()
              .url
          )
            .withFormUrlEncodedBody("value" -> "false")

        status(route(application, request).value) mustEqual SEE_OTHER

        verify(mockSessionRepository).set(savedAnswersCaptor.capture())

        val savedAnswers = savedAnswersCaptor.getValue

        savedAnswers.get(ControllingBodySubmittedPage).value mustEqual true
        savedAnswers.get(ControllingBodyChangesPage).value mustEqual false
        savedAnswers.get(RemoveControllingBodyTradeNamePage).value mustEqual false
        savedAnswers.get(ControllingBodyTradingNamePage).value mustEqual tradingName
      }
    }
  }
}
