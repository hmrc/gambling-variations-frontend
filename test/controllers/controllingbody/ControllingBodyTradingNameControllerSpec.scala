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
import forms.controllingbody.ControllingBodyTradingNameFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.mockito.ArgumentCaptor
import org.scalatestplus.mockito.MockitoSugar
import pages.controllingbody.ControllingBodyTradingNamePage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.controllingbody.ControllingBodyTradingNameView

import scala.concurrent.Future

class ControllingBodyTradingNameControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/trading-name")

  val formProvider = new ControllingBodyTradingNameFormProvider()
  val form = formProvider()

  lazy val controllingBodyTradingNameRoute = routes.ControllingBodyTradingNameController.onPageLoad().url

  "ControllingBodyTradingName Controller" - {

    "render the registration-details caption and page title" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      running(application) {
        val result = route(application, FakeRequest(GET, controllingBodyTradingNameRoute)).value
        status(result) mustEqual OK
        val document = org.jsoup.Jsoup.parse(contentAsString(result))
        document.select("span.govuk-caption-l").text() mustEqual "Change registration details"
        document.title() must include("Change registration details")
      }
    }

    Seq(
      "" -> "Enter the controlling body’s trading name",
      "Trading@Name" -> "The controlling body’s trading name must only include letters a to z, numbers 0 to 9, apostrophes, hyphens, slashes or spaces",
      ("A" * 101) -> "The controlling body’s trading name must be 100 characters or less"
    ).foreach { case (value, expectedError) =>
      s"render the exact error copy: $expectedError" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
        running(application) {
          val request = FakeRequest(POST, controllingBodyTradingNameRoute)
            .withFormUrlEncodedBody("controllingBodyTradingName" -> value)
          val result = route(application, request).value
          status(result) mustEqual BAD_REQUEST
          val document = org.jsoup.Jsoup.parse(contentAsString(result))
          document.select(".govuk-error-summary__list a").text() mustEqual expectedError
          document.select(".govuk-error-message").text() must include(expectedError)
        }
      }
    }

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllingBodyTradingNameRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ControllingBodyTradingNameView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must render the form with its POST route" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      running(application) {
        val url = routes.ControllingBodyTradingNameController.onPageLoad().url
        val request = FakeRequest(GET, url)
        val result = route(application, request).value
        status(result) mustEqual OK
        val doc = org.jsoup.Jsoup.parse(contentAsString(result))
        doc.select("form").attr("action") mustEqual routes.ControllingBodyTradingNameController.onSubmit().url
        val invalid = route(application, FakeRequest(POST, url).withFormUrlEncodedBody("controllingBodyTradingName" -> "")).value
        status(invalid) mustEqual BAD_REQUEST
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(ControllingBodyTradingNamePage, "answer").success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllingBodyTradingNameRoute)

        val view = application.injector.instanceOf[ControllingBodyTradingNameView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill("answer"), NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, controllingBodyTradingNameRoute)
            .withFormUrlEncodedBody(("controllingBodyTradingName", "answer"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, controllingBodyTradingNameRoute)
            .withFormUrlEncodedBody(("controllingBodyTradingName", ""))

        val boundForm = form.bind(Map("controllingBodyTradingName" -> ""))

        val view = application.injector.instanceOf[ControllingBodyTradingNameView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must show an empty form for a GET if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()
      running(application) {
        val request = FakeRequest(GET, controllingBodyTradingNameRoute)
        val result = route(application, request).value
        val view = application.injector.instanceOf[ControllingBodyTradingNameView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must create and save answers for a valid POST if no existing data is found" in {
      val repository = mock[SessionRepository]
      when(repository.set(any())).thenReturn(Future.successful(true))
      val savedAnswers = ArgumentCaptor.forClass(classOf[UserAnswers])
      val application = applicationBuilder(userAnswers = None)
        .overrides(
          bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
          bind[SessionRepository].toInstance(repository)
        )
        .build()

      running(application) {
        val request = FakeRequest(POST, controllingBodyTradingNameRoute)
          .withFormUrlEncodedBody("controllingBodyTradingName" -> "New Trading Name")
        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
        verify(repository).set(savedAnswers.capture())
        savedAnswers.getValue.id mustEqual userAnswersId
        savedAnswers.getValue.get(ControllingBodyTradingNamePage).value mustEqual "New Trading Name"
      }
    }
  }
}
