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
import forms.ContactNumberFormProvider
import models.{ContactNumber, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.CorrespondenceContactNumberPage
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.CorrespondenceContactNumberView

import scala.concurrent.Future

class CorrespondenceContactNumberControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new ContactNumberFormProvider()
  val form = formProvider("correspondenceContactNumber")

  val data = Json.obj(
    CorrespondenceContactNumberPage.toString -> Json.obj(
      "phoneNumber"       -> "01632 960 001",
      "mobilePhoneNumber" -> "07700900000"
    ),
    "correspondenceDetailsSection" -> Json.obj("mgdRegNum" -> userAnswersId)
  )

  val noAnswers = Json.obj("correspondenceDetailsSection" -> Json.obj("mgdRegNum" -> userAnswersId))

  lazy val correspondenceContactNumberRoute =
    routes.CorrespondenceContactNumberController.onPageLoad(NormalMode).url

  val userAnswers =
    UserAnswers(
      userAnswersId,
      data
    )

  "CorrespondenceContact Controller" - {

    "must return OK and the correct view for a GET" in {

      val application =
        applicationBuilder(userAnswers = Some(UserAnswers(userAnswersId, noAnswers))).build()

      running(application) {

        val request = FakeRequest(GET, correspondenceContactNumberRoute)

        val view = application.injector.instanceOf[CorrespondenceContactNumberView]

        val result = route(application, request).value

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when previously answered" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {

        val request = FakeRequest(GET, correspondenceContactNumberRoute)

        val view = application.injector.instanceOf[CorrespondenceContactNumberView]

        val result = route(application, request).value

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(
            form.fill(
              ContactNumber(
                Some("01632 960 001"),
                Some("07700900000")
              )
            ),
            NormalMode
          )(request, messages(application)).toString
      }
    }

    "must redirect to next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any()))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(UserAnswers(userAnswersId, noAnswers)))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {

        val request =
          FakeRequest(POST, correspondenceContactNumberRoute)
            .withFormUrlEncodedBody(
              ("phoneNumber", "01632 960 001"),
              ("mobileNumber", "07700900000")
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return Bad Request and errors when invalid data is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(UserAnswers(userAnswersId, noAnswers))).build()

      running(application) {

        val request =
          FakeRequest(POST, correspondenceContactNumberRoute)
            .withFormUrlEncodedBody(
              ("phoneNumber", "invalid"),
              ("mobileNumber", "invalid")
            )

        val boundForm =
          form.bind(
            Map(
              "phoneNumber"  -> "invalid",
              "mobileNumber" -> "invalid"
            )
          )

        val view = application.injector.instanceOf[CorrespondenceContactNumberView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no data" in {

      val application =
        applicationBuilder(userAnswers = None).build()

      running(application) {

        val request = FakeRequest(GET, correspondenceContactNumberRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.SystemErrorController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no data" in {

      val application =
        applicationBuilder(userAnswers = None).build()

      running(application) {

        val request =
          FakeRequest(POST, correspondenceContactNumberRoute)
            .withFormUrlEncodedBody(
              ("phoneNumber", "01632 960 001"),
              ("mobileNumber", "07700900000")
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.SystemErrorController.onPageLoad().url
      }
    }
  }
}
