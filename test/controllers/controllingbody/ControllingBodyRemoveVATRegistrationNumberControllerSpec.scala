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
import controllers.routes
import forms.controllingbody.ControllingBodyRemoveVATRegistrationNumberFormProvider
import models.UserAnswers
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.controllingbody.ControllingBodyRemoveVATRegistrationNumberPage
import play.api.inject.bind
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.controllingbody.ControllingBodyRemoveVATRegistrationNumberView

import scala.concurrent.Future

class ControllingBodyRemoveVATRegistrationNumberControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new ControllingBodyRemoveVATRegistrationNumberFormProvider()
  val form = formProvider()

  lazy val controllingBodyRemoveVATRegistrationNumberRoute: String =
    controllers.controllingbody.routes.ControllingBodyRemoveVATRegistrationNumberController.onPageLoad().url

  private val requiredAnswer = Json.obj(
    "controllingBodyDetails" -> Json.obj("mgdRegNum" -> mgdRegNum, "vrn" -> "123456789")
  )

  "ControllingBodyRemoveVATRegistrationNumber Controller" - {

    Seq(GET, POST).foreach { method =>
      s"must redirect to SystemError for $method when the VAT number is missing" in {
        val answers = UserAnswers("id", Json.obj("controllingBodyDetails" -> Json.obj("mgdRegNum" -> mgdRegNum)))
        val application = applicationBuilder(userAnswers = Some(answers)).build()
        running(application) {
          val result = route(application, FakeRequest(method, controllingBodyRemoveVATRegistrationNumberRoute)).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.SystemErrorController.onPageLoad().url
        }
      }
    }

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(UserAnswers("id", requiredAnswer))).build()

      running(application) {
        val request = FakeRequest(GET, controllingBodyRemoveVATRegistrationNumberRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ControllingBodyRemoveVATRegistrationNumberView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, "123456789")(request, messages(application)).toString
      }
    }

    Seq(true, false).foreach { answer =>
      s"must prepopulate the saved answer $answer" in {
        val answers = UserAnswers("id", requiredAnswer).set(ControllingBodyRemoveVATRegistrationNumberPage, answer).success.value
        val application = applicationBuilder(userAnswers = Some(answers)).build()
        running(application) {
          val request = FakeRequest(GET, controllingBodyRemoveVATRegistrationNumberRoute)
          val result = route(application, request).value
          val view = application.injector.instanceOf[ControllingBodyRemoveVATRegistrationNumberView]
          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form.fill(answer), "123456789")(request, messages(application)).toString
        }
      }
    }

    Seq(true, false).foreach { answer =>
      s"must save $answer and redirect to the placeholder destination" in {
        val repository = mock[SessionRepository]
        when(repository.set(any())) thenReturn Future.successful(true)
        val application = applicationBuilder(userAnswers = Some(UserAnswers("id", requiredAnswer)))
          .overrides(bind[SessionRepository].toInstance(repository))
          .build()
        running(application) {
          val result = route(
            application,
            FakeRequest(POST, controllingBodyRemoveVATRegistrationNumberRoute)
              .withFormUrlEncodedBody("removeVATRegistrationNumberYesNo" -> answer.toString)
          ).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual "#"
          org.mockito.Mockito
            .verify(repository)
            .set(
              org.mockito.ArgumentMatchers.argThat[UserAnswers](answers =>
                answers.get(ControllingBodyRemoveVATRegistrationNumberPage).contains(answer)
              )
            )
        }
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(UserAnswers("id", requiredAnswer)))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, controllingBodyRemoveVATRegistrationNumberRoute)
            .withFormUrlEncodedBody(("removeVATRegistrationNumberYesNo", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(UserAnswers("id", requiredAnswer))).build()

      running(application) {
        val request =
          FakeRequest(POST, controllingBodyRemoveVATRegistrationNumberRoute)
            .withFormUrlEncodedBody(("removeVATRegistrationNumberYesNo", ""))

        val boundForm = form.bind(Map("removeVATRegistrationNumberYesNo" -> ""))

        val view = application.injector.instanceOf[ControllingBodyRemoveVATRegistrationNumberView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, "123456789")(request, messages(application)).toString
      }
    }

    "must redirect to SystemError for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, controllingBodyRemoveVATRegistrationNumberRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.SystemErrorController.onPageLoad().url
      }
    }

    "must redirect to SystemError for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, controllingBodyRemoveVATRegistrationNumberRoute)
            .withFormUrlEncodedBody(("removeVATRegistrationNumberYesNo", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.SystemErrorController.onPageLoad().url
      }
    }
  }
}
