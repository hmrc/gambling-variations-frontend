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
import forms.BusinessChangeAddrScreenerFormProvider
import models.BusinessChangeAddrOption.EditCurrentAddress
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.BusinessChangeAddrScreenerPage
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.BusinessChangeAddrScreenerView

import scala.concurrent.Future

class BusinessChangeAddrScreenerControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new BusinessChangeAddrScreenerFormProvider()
  val form = formProvider()

  lazy val BusinessChangeAddrScreenerRoute = routes.BusinessChangeAddrScreenerController.onPageLoad().url

  val noAnswers =
    UserAnswers(
      userAnswersId,
      Json.obj("businessAddressSection" -> Json.obj("mgdRegNum" -> userAnswersId))
    )

  "BusinessChangeAddrScreener Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(noAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, BusinessChangeAddrScreenerRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[BusinessChangeAddrScreenerView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, false)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val data = Json.obj(
        "businessAddressSection"                -> Json.obj("mgdRegNum" -> userAnswersId),
        BusinessChangeAddrScreenerPage.toString -> EditCurrentAddress.toString
      )

      val userAnswers = UserAnswers(userAnswersId, data)

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, BusinessChangeAddrScreenerRoute)

        val view = application.injector.instanceOf[BusinessChangeAddrScreenerView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(EditCurrentAddress), NormalMode, false)(request, messages(application)).toString
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
          FakeRequest(POST, BusinessChangeAddrScreenerRoute)
            .withFormUrlEncodedBody(("businessChangeAddrScreener", "editCurrentAddress"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(noAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, BusinessChangeAddrScreenerRoute)
            .withFormUrlEncodedBody(("businessChangeAddrScreener", ""))

        val boundForm = form.bind(Map("businessChangeAddrScreener" -> ""))

        val view = application.injector.instanceOf[BusinessChangeAddrScreenerView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, false)(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = Some(noAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, BusinessChangeAddrScreenerRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[BusinessChangeAddrScreenerView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, false)(request, messages(application)).toString
      }
    }

    "must redirect to the next page for a POST if no existing data is found" in {

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
          FakeRequest(POST, BusinessChangeAddrScreenerRoute)
            .withFormUrlEncodedBody(("businessChangeAddrScreener", "editCurrentAddress"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }
  }
}
