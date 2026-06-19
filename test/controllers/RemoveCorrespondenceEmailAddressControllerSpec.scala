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
import forms.RemoveCorrespondenceEmailAddressFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.RemoveCorrespondenceEmailAddressPage
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.RemoveCorrespondenceEmailAddressView

import scala.concurrent.Future

class RemoveCorrespondenceEmailAddressControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new RemoveCorrespondenceEmailAddressFormProvider()
  val form = formProvider()

  lazy val removeCorrespondenceEmailAddressRoute =
    routes.RemoveCorrespondenceEmailAddressController.onPageLoad().url

  private val correspondenceEmail = "test@example.com"

  private val baseAnswers =
    UserAnswers(
      userAnswersId,
      Json.obj(
        "correspondenceDetailsSection" -> Json.obj(
          "mgdRegNum" -> userAnswersId
        ),
        "correspondenceEmail" -> correspondenceEmail
      )
    )

  private val answersWithSectionOnly =
    UserAnswers(
      userAnswersId,
      Json.obj(
        "correspondenceDetailsSection" -> Json.obj(
          "mgdRegNum" -> userAnswersId
        )
      )
    )

  "RemoveCorrespondenceEmailAddressController" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, removeCorrespondenceEmailAddressRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RemoveCorrespondenceEmailAddressView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form, NormalMode, correspondenceEmail)(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET when the page is previously answered" in {

      val answers =
        baseAnswers
          .set(RemoveCorrespondenceEmailAddressPage, true)
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(answers)).build()

      running(application) {
        val request = FakeRequest(GET, removeCorrespondenceEmailAddressRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RemoveCorrespondenceEmailAddressView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form.fill(true), NormalMode, correspondenceEmail)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if correspondence email is missing" in {

      val application = applicationBuilder(userAnswers = Some(answersWithSectionOnly)).build()

      running(application) {
        val request = FakeRequest(GET, removeCorrespondenceEmailAddressRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to the next page when valid data is submitted and value is true" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(baseAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, routes.RemoveCorrespondenceEmailAddressController.onSubmit().url)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to the next page when valid data is submitted and value is false" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(baseAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, routes.RemoveCorrespondenceEmailAddressController.onSubmit().url)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, routes.RemoveCorrespondenceEmailAddressController.onSubmit().url)
            .withFormUrlEncodedBody(("value", ""))

        val result = route(application, request).value

        val view = application.injector.instanceOf[RemoveCorrespondenceEmailAddressView]

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual
          view(form.bind(Map("value" -> "")), NormalMode, correspondenceEmail)(request, messages(application)).toString
      }
    }

    "must redirect to System Error for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, removeCorrespondenceEmailAddressRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.SystemErrorController.onPageLoad().url
      }
    }

    "must redirect to System Error for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, routes.RemoveCorrespondenceEmailAddressController.onSubmit().url)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.SystemErrorController.onPageLoad().url
      }
    }
  }
}
