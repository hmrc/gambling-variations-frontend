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
import forms.RemoveAssociatedRegNumberFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.RemoveAssociatedRegNumberView

import scala.concurrent.Future

class RemoveAssociatedRegNumberControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new RemoveAssociatedRegNumberFormProvider()
  val form = formProvider()

  lazy val removeAssociatedRegNumberRoute = routes.RemoveAssociatedRegNumberController.onPageLoad(NormalMode).url
  private val assocRegSeq = Some(Seq("XYM00000000", "b", "c"))
  private val baseAnswers =
    UserAnswers(
      userAnswersId,
      Json.obj(
        "associatedRegistrationNumbers" -> assocRegSeq,
        "chosenAssociatedRegNumber"     -> "XYM00000000",
        "mgdTradeDetailsSection"        -> Json.obj("mgdRegNum" -> userAnswersId)
      )
    )

  "RemoveAssociatedRegNumber Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, removeAssociatedRegNumberRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RemoveAssociatedRegNumberView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, "XYM00000000")(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

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
          FakeRequest(POST, removeAssociatedRegNumberRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, removeAssociatedRegNumberRoute)
            .withFormUrlEncodedBody(("value", ""))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
      }
    }

    "must redirect to System Error for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, removeAssociatedRegNumberRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.SystemErrorController.onPageLoad().url
      }
    }

    "must redirect to System Error for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, removeAssociatedRegNumberRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.SystemErrorController.onPageLoad().url
      }
    }
  }
}
