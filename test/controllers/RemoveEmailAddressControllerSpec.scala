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

package controllers

import base.SpecBase
import forms.RemoveEmailAddressFormProvider
import models.{NormalMode, UserAnswers}
import navigation.Navigator
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.{BusinessEmailAddressPage, RemoveEmailAddressPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.RemoveEmailAddressView
import org.mockito.Mockito.{verify, when}
import scala.concurrent.Future

class RemoveEmailAddressControllerSpec extends SpecBase with MockitoSugar {

  private val email = "test@test.com"

  private val baseAnswers =
    UserAnswers(userAnswersId)
      .set(BusinessEmailAddressPage, email)
      .success
      .value

  val formProvider = new RemoveEmailAddressFormProvider()
  val form = formProvider()

  lazy val removeEmailRoute =
    routes.RemoveEmailAddressController.onPageLoad(NormalMode).url

  "RemoveEmailAddress Controller" - {

    "must return OK and the correct view for a GET" in {

      val application =
        applicationBuilder(userAnswers = Some(baseAnswers)).build()

      running(application) {

        val request = FakeRequest(GET, removeEmailRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RemoveEmailAddressView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form, NormalMode, email)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers =
        baseAnswers
          .set(RemoveEmailAddressPage, true)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {

        val request = FakeRequest(GET, removeEmailRoute)

        val view = application.injector.instanceOf[RemoveEmailAddressView]

        val result = route(application, request).value

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form.fill(true), NormalMode, email)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]
      val mockNavigator = mock[Navigator]

      when(mockSessionRepository.set(any()))
        .thenReturn(Future.successful(true))

      when(mockNavigator.nextPage(any(), any(), any()))
        .thenReturn(routes.IndexController.onPageLoad())

      val application =
        applicationBuilder(userAnswers = Some(baseAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[Navigator].toInstance(mockNavigator)
          )
          .build()

      running(application) {

        val request =
          FakeRequest(POST, removeEmailRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        verify(mockSessionRepository).set(any())
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(baseAnswers)).build()

      running(application) {

        val request =
          FakeRequest(POST, removeEmailRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[RemoveEmailAddressView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(boundForm, NormalMode, email)(request, messages(application)).toString
      }
    }

    "must redirect to SystemError for GET if email is missing" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {

        val request = FakeRequest(GET, removeEmailRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.SystemErrorController.onPageLoad().url
      }
    }

    "must redirect to SystemError for POST if email is missing" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {

        val request =
          FakeRequest(POST, removeEmailRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.SystemErrorController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for GET when no session exists" in {

      val application =
        applicationBuilder(userAnswers = None).build()

      running(application) {

        val request = FakeRequest(GET, removeEmailRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.SystemErrorController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for POST when no session exists" in {

      val application =
        applicationBuilder(userAnswers = None).build()

      running(application) {

        val request =
          FakeRequest(POST, removeEmailRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.SystemErrorController.onPageLoad().url
      }
    }
  }
}
