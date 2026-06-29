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
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.{AddAssociatedRegistrationNumberPage, AssociatedRegNumberPage, AssociatedRegNumberSubmittedPage, ChosenAssociatedRegNumberPage}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.AssociatedRegistrationNumbersView

import scala.concurrent.Future

class AssociatedRegistrationNumbersControllerSpec extends SpecBase with MockitoSugar {

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

  lazy val associatedRegistrationNumbersRoute =
    routes.AssociatedRegistrationNumbersController.onPageLoad().url

  "AssociatedRegistrationNumbers Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(baseUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, associatedRegistrationNumbersRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AssociatedRegistrationNumbersView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(
            form,
            NormalMode,
            Some(Seq("XHM00000199", "ZIU00001218", "GTT28881666")),
            3,
            false
          )(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers =
        baseUserAnswers
          .set(AddAssociatedRegistrationNumberPage, true)
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, associatedRegistrationNumbersRoute)

        val view = application.injector.instanceOf[AssociatedRegistrationNumbersView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(
            form.fill(true),
            NormalMode,
            Some(Seq("XHM00000199", "ZIU00001218", "GTT28881666")),
            3,
            false
          )(request, messages(application)).toString
      }
    }

    "must redirect to AssociatedRegNumber page when true is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(baseUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, associatedRegistrationNumbersRoute)
            .withFormUrlEncodedBody(form.fill(true).data.toSeq*)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.CheckTradingDetailsController.onPageLoad().url
      }
    }

    "must redirect to CheckTradingDetails page when false is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(baseUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, associatedRegistrationNumbersRoute)
            .withFormUrlEncodedBody(form.fill(false).data.toSeq*)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.CheckTradingDetailsController.onPageLoad().url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted and there are fewer than 3 associated registration numbers" in {

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
        val request = FakeRequest(POST, associatedRegistrationNumbersRoute)

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
      }
    }

    "must redirect to SystemError for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, associatedRegistrationNumbersRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.SystemErrorController.onPageLoad().url
      }
    }

    "must return OK and show submitted flag as true for a GET when AssociatedRegNumberSubmittedPage is true" in {

      val userAnswers =
        baseUserAnswers
          .set(AssociatedRegNumberSubmittedPage, true)
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, associatedRegistrationNumbersRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AssociatedRegistrationNumbersView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(
            form,
            NormalMode,
            Some(Seq("XHM00000199", "ZIU00001218", "GTT28881666")),
            3,
            true
          )(request, messages(application)).toString
      }
    }

    "must return OK and show count as 0 for a GET when there are no associated registration numbers" in {

      val dataWithoutAssociatedNumbers = Json.obj(
        "mgdTradeDetailsSection" -> Json.obj("mgdRegNum" -> mgdRegNum)
      )

      val userAnswers = UserAnswers(userAnswersId, dataWithoutAssociatedNumbers)

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, associatedRegistrationNumbersRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AssociatedRegistrationNumbersView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(
            form,
            NormalMode,
            None,
            0,
            false
          )(request, messages(application)).toString
      }
    }

    "must redirect to # when invalid data is submitted and there are already 3 associated registration numbers" in {

      val application = applicationBuilder(userAnswers = Some(baseUserAnswers)).build()

      running(application) {
        val request = FakeRequest(POST, associatedRegistrationNumbersRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.CheckTradingDetailsController.onPageLoad().url
      }
    }

    "must set the chosen associated registration number and redirect to remove page" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(baseUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val assocRegNumber = "XHM00000199"

        val request =
          FakeRequest(
            GET,
            routes.AssociatedRegistrationNumbersController.onRedirect(assocRegNumber).url
          )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.RemoveAssociatedRegNumberController.onPageLoad(NormalMode).url

        val captor = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(captor.capture())

        captor.getValue.get(ChosenAssociatedRegNumberPage).value mustEqual assocRegNumber
      }
    }

    "must set the chosen associated registration number and redirect to associated registration number page when changing" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(baseUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val assocRegNumber = "ZIU00001218"

        val request =
          FakeRequest(
            GET,
            routes.AssociatedRegistrationNumbersController.onChangeRedirect(assocRegNumber).url
          )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.AssociatedRegNumberController.onPageLoad().url

        val captor = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(captor.capture())

        captor.getValue.get(ChosenAssociatedRegNumberPage).value mustEqual assocRegNumber
      }
    }

    "must remove AssociatedRegNumberPage and ChosenAssociatedRegNumberPage when valid data is submitted and redirect to associated registration number page" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val twoNumbers = Json.obj(
        "mgdTradeDetailsSection" -> Json.obj("mgdRegNum" -> mgdRegNum),
        "associatedRegistrationNumbers" -> Json.arr(
          "XHM00000199",
          "ZIU00001218"
        )
      )

      val userAnswers =
        UserAnswers(userAnswersId, twoNumbers)
          .set(AssociatedRegNumberPage, "XHM00000199")
          .success
          .value
          .set(ChosenAssociatedRegNumberPage, "ZIU00001218")
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, associatedRegistrationNumbersRoute)
            .withFormUrlEncodedBody(form.fill(true).data.toSeq*)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          routes.AssociatedRegNumberController.onPageLoad().url

        val captor = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(captor.capture())

        val savedAnswers = captor.getValue

        savedAnswers.get(AddAssociatedRegistrationNumberPage).value mustEqual true
        savedAnswers.get(AssociatedRegNumberPage)       must not be defined
        savedAnswers.get(ChosenAssociatedRegNumberPage) must not be defined
      }
    }

    "must redirect to SystemError for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, associatedRegistrationNumbersRoute)
            .withFormUrlEncodedBody(form.fill(true).data.toSeq*)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.SystemErrorController.onPageLoad().url
      }
    }
  }
}
