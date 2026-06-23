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
import forms.AssociatedRegistrationNumbersFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.AddAssociatedRegistrationNumberPage
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.AssociatedRegistrationNumbersView

import scala.concurrent.Future

class AssociatedRegistrationNumbersControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new AssociatedRegistrationNumbersFormProvider()
  val form = formProvider()

  val data = Json.obj(
    "mgdTradeDetailsSection" -> Json.obj("mgdRegNum" -> mgdRegNum),
    "associatedRegistrationNumbers" -> Json.arr(
      "XHM00000199",
      "ZIU00001218",
      "GTT28881666"
    )
  )

  private val baseUserAnswers =
    UserAnswers(userAnswersId, data)

  lazy val associatedRegistrationNumbersRoute = routes.AssociatedRegistrationNumbersController.onPageLoad(NormalMode).url

  "AssociatedRegistrationNumbers Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(baseUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, associatedRegistrationNumbersRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AssociatedRegistrationNumbersView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form, NormalMode, Some(Seq("XHM00000199", "ZIU00001218", "GTT28881666")), 3, false)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = baseUserAnswers.set(AddAssociatedRegistrationNumberPage, true).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, associatedRegistrationNumbersRoute)

        val view = application.injector.instanceOf[AssociatedRegistrationNumbersView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form, NormalMode, Some(Seq("XHM00000199", "ZIU00001218", "GTT28881666")), 3, false)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(baseUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, associatedRegistrationNumbersRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val twoNumbers = Json.obj(
        "mgdTradeDetailsSection" -> Json.obj("mgdRegNum" -> mgdRegNum),
        "associatedRegistrationNumbers" -> Json.arr(
          "XHM00000199",
          "ZIU00001218"
        )
      )

      val twoAnswers =
        UserAnswers(userAnswersId, twoNumbers)

      val application = applicationBuilder(userAnswers = Some(twoAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, associatedRegistrationNumbersRoute)
            .withFormUrlEncodedBody(("value", ""))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, associatedRegistrationNumbersRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.SystemErrorController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, associatedRegistrationNumbersRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.SystemErrorController.onPageLoad().url
      }
    }
  }
}
