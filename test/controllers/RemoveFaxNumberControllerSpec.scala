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
import forms.RemoveFaxNumberFormProvider
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.{FaxNumberPage, RemoveFaxNumberPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.RemoveFaxNumberView

import scala.concurrent.Future

class RemoveFaxNumberControllerSpec extends SpecBase with MockitoSugar {

  private val faxNumber = "123456789"

  private val baseAnswers =
    UserAnswers(userAnswersId)
      .set(FaxNumberPage, faxNumber)
      .success
      .value

  val formProvider = new RemoveFaxNumberFormProvider()
  val form = formProvider()

  lazy val removeFaxNumberRoute =
    routes.RemoveFaxNumberController.onPageLoad(NormalMode).url

  "RemoveFaxNumber Controller" - {

    "must return OK and the correct view for a GET" in {

      val application =
        applicationBuilder(userAnswers = Some(baseAnswers)).build()

      running(application) {

        val request = FakeRequest(GET, removeFaxNumberRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RemoveFaxNumberView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form, NormalMode, faxNumber)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers =
        baseAnswers
          .set(RemoveFaxNumberPage, true)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {

        val request = FakeRequest(GET, removeFaxNumberRoute)

        val view = application.injector.instanceOf[RemoveFaxNumberView]

        val result = route(application, request).value

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form.fill(true), NormalMode, faxNumber)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any()))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(baseAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {

        val request =
          FakeRequest(POST, removeFaxNumberRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(baseAnswers)).build()

      running(application) {

        val request =
          FakeRequest(POST, removeFaxNumberRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[RemoveFaxNumberView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(boundForm, NormalMode, faxNumber)(request, messages(application)).toString
      }
    }

    "must redirect to CheckBusinessNameController for a GET if fax number is missing" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {

        val request = FakeRequest(GET, removeFaxNumberRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.CheckBusinessNameController.onPageLoad().url
      }
    }

    "must redirect to CheckBusinessNameController for a POST if fax number is missing" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {

        val request =
          FakeRequest(POST, removeFaxNumberRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.CheckBusinessNameController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for GET when no session exists" in {

      val application =
        applicationBuilder(userAnswers = None).build()

      running(application) {

        val request = FakeRequest(GET, removeFaxNumberRoute)

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
          FakeRequest(POST, removeFaxNumberRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.SystemErrorController.onPageLoad().url
      }
    }
  }
}
