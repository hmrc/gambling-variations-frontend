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
import controllers.controllingbody.routes.ControllingBodyChangeScreenerController
import forms.controllingbody.ControllingBodyChangeScreenerFormProvider
import models.controllingbody.ControllingBodyChangeOption
import models.controllingbody.ControllingBodyChangeOption.{KeepSame, ProvideNew}
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.controllingbody.ControllingBodyChangeScreenerPage
import play.api.data.Form
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.controllingbody.ControllingBodyChangeScreenerView

import scala.concurrent.Future

class ControllingBodyChangeScreenerControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute: Call = Call("GET", "/foo")

  val form: Form[ControllingBodyChangeOption] = (new ControllingBodyChangeScreenerFormProvider())()

  lazy val controllingBodyChangeScreenerRoute: String =
    ControllingBodyChangeScreenerController.onPageLoad().url

  private val businessName = "Totally Different Group Holdings Ltd"

  private def userAnswersWith(fields: (String, Json.JsValueWrapper)*): UserAnswers =
    UserAnswers(
      mgdRegNum,
      Json.obj(
        "controllingBodyDetailsSection" -> (Json.obj("mgdRegNum" -> mgdRegNum) ++ Json.obj(fields*))
      )
    )

  private val userAnswers: UserAnswers =
    userAnswersWith("businessName" -> businessName)

  private val soleProprietorAnswers: UserAnswers =
    userAnswersWith(
      "soleProprietor" -> Json.obj(
        "title"      -> "Mrs",
        "firstName"  -> "Jane",
        "middleName" -> "Mary",
        "lastName"   -> "Smith"
      )
    )

  private val noNameAnswers: UserAnswers =
    userAnswersWith()

  "ControllingBodyChangeScreener Controller" - {

    "onPageLoad" - {

      "must return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, controllingBodyChangeScreenerRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[ControllingBodyChangeScreenerView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, NormalMode, businessName)(request, messages(application)).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val answers = userAnswers.set(ControllingBodyChangeScreenerPage, ProvideNew).success.value

        val application = applicationBuilder(userAnswers = Some(answers)).build()

        running(application) {
          val request = FakeRequest(GET, controllingBodyChangeScreenerRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[ControllingBodyChangeScreenerView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form.fill(ProvideNew), NormalMode, businessName)(request, messages(application)).toString
        }
      }

      "must show the sole proprietor's name when the controlling body has no business name" in {

        val application = applicationBuilder(userAnswers = Some(soleProprietorAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, controllingBodyChangeScreenerRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[ControllingBodyChangeScreenerView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, NormalMode, "Mrs Jane Mary Smith")(request, messages(application)).toString
        }
      }

      "must redirect to SystemError for a GET when the controlling body has no name" in {

        val application = applicationBuilder(userAnswers = Some(noNameAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, controllingBodyChangeScreenerRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.SystemErrorController.onPageLoad().url
        }
      }

      "must redirect to SystemError for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, controllingBodyChangeScreenerRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.SystemErrorController.onPageLoad().url
        }
      }
    }

    "onSubmit" - {

      "must save the answer and redirect to the next page when valid data is submitted" in {

        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        val savedAnswersCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])

        val application =
          applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, ControllingBodyChangeScreenerController.onSubmit().url)
              .withFormUrlEncodedBody(("value", KeepSame.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
          verify(mockSessionRepository).set(savedAnswersCaptor.capture())
          savedAnswersCaptor.getValue.get(ControllingBodyChangeScreenerPage).value mustEqual KeepSame
        }
      }

      "must return BAD_REQUEST and errors when invalid data is submitted" in {

        val mockSessionRepository = mock[SessionRepository]

        val application =
          applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, ControllingBodyChangeScreenerController.onSubmit().url)
              .withFormUrlEncodedBody(("value", ""))

          val boundForm = form.bind(Map("value" -> ""))

          val view = application.injector.instanceOf[ControllingBodyChangeScreenerView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, NormalMode, businessName)(request, messages(application)).toString
          verify(mockSessionRepository, never()).set(any())
        }
      }

      "must redirect to SystemError for a POST when the controlling body has no name" in {

        val mockSessionRepository = mock[SessionRepository]

        val application =
          applicationBuilder(userAnswers = Some(noNameAnswers))
            .overrides(
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, ControllingBodyChangeScreenerController.onSubmit().url)
              .withFormUrlEncodedBody(("value", KeepSame.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.SystemErrorController.onPageLoad().url
          verify(mockSessionRepository, never()).set(any())
        }
      }

      "must redirect to SystemError for a POST if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request =
            FakeRequest(POST, ControllingBodyChangeScreenerController.onSubmit().url)
              .withFormUrlEncodedBody(("value", KeepSame.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.SystemErrorController.onPageLoad().url
        }
      }
    }
  }
}
