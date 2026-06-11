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
import forms.ChangeEmailAddressFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.EmailAddressPage
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.ChangeEmailAddressView

import scala.concurrent.Future

class ChangeEmailAddressControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new ChangeEmailAddressFormProvider()
  val form = formProvider()

  val noAnswers =
    UserAnswers(
      userAnswersId,
      Json.obj("businessContactDetailsSection" -> Json.obj("mgdRegNum" -> userAnswersId))
    )

  lazy val emailAddressRoute =
    routes.ChangeEmailAddressController.onPageLoad(NormalMode).url

  "ChangeEmailAddress Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(noAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, emailAddressRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ChangeEmailAddressView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view on a GET when the question has previously been answered" in {

      val data = Json.obj(
        "businessContactDetailsSection" -> Json.obj("mgdRegNum" -> userAnswersId),
        EmailAddressPage.toString       -> "validEmail@example.com"
      )

      val userAnswers = UserAnswers(userAnswersId, data)

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, emailAddressRoute)

        val view = application.injector.instanceOf[ChangeEmailAddressView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form.fill("validEmail@example.com"), NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(noAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, emailAddressRoute)
            .withFormUrlEncodedBody(("emailAddress", "validEmail@example.com"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(noAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, emailAddressRoute)
            .withFormUrlEncodedBody(("emailAddress", ""))

        val boundForm = form.bind(Map("emailAddress" -> ""))

        val view = application.injector.instanceOf[ChangeEmailAddressView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual
          view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to SystemError for a GET if no session exists" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, emailAddressRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          routes.SystemErrorController.onPageLoad().url
      }
    }

    "must redirect to SystemError for a POST if no session exists" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, emailAddressRoute)
            .withFormUrlEncodedBody(("emailAddress", "validEmail@example.com"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          routes.SystemErrorController.onPageLoad().url
      }
    }
  }
}
