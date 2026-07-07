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
import connectors.GamblingConnector
import forms.AssociatedRegNumberFormProvider
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.{AssociatedRegNumberPage, AssociatedRegistrationNumbersPage, MgdTradeDetailsSectionPage, TradingDetailsChangesPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.AssociatedRegNumberView

import scala.concurrent.Future

class AssociatedRegNumberControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new AssociatedRegNumberFormProvider()
  val form = formProvider()
  val fieldName = "associatedRegNumber"
  val requiredUserAnswers = emptyUserAnswers.set(MgdTradeDetailsSectionPage, mgdRegNum).success.value

  lazy val associatedRegNumberRoute = routes.AssociatedRegNumberController.onPageLoad(NormalMode).url

  "AssociatedRegNumber Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(requiredUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, associatedRegNumberRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AssociatedRegNumberView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers =
        UserAnswers(userAnswersId)
          .set(MgdTradeDetailsSectionPage, mgdRegNum)
          .success
          .value
          .set(AssociatedRegNumberPage, "answer")
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, associatedRegNumberRoute)

        val view = application.injector.instanceOf[AssociatedRegNumberView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill("answer"), NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(requiredUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, associatedRegNumberRoute)
            .withFormUrlEncodedBody((fieldName, "XRM00000000574"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.AssociatedRegistrationNumbersListController.onPageLoad(NormalMode).url
      }
    }

    "must flag TradingDetailsChangesPage when data added in" in {

      val mockSessionRepository = mock[SessionRepository]
      val savedAnswersCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val userAnswers =
        requiredUserAnswers
          .set(AssociatedRegistrationNumbersPage, Seq("XDM00000001309"))
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
          FakeRequest(POST, associatedRegNumberRoute)
            .withFormUrlEncodedBody((fieldName, "XRM00000000574"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        verify(mockSessionRepository).set(savedAnswersCaptor.capture())
        savedAnswersCaptor.getValue.get(AssociatedRegistrationNumbersPage).value mustEqual Seq(
          "XDM00000001309",
          "XRM00000000574"
        )
        savedAnswersCaptor.getValue.get(TradingDetailsChangesPage).value mustEqual true
      }
    }

    "must add the submitted registration number to associated registration numbers" in {

      val mockSessionRepository = mock[SessionRepository]
      val savedAnswersCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val userAnswers =
        requiredUserAnswers
          .set(AssociatedRegistrationNumbersPage, Seq("XDM00000001309"))
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
          FakeRequest(POST, associatedRegNumberRoute)
            .withFormUrlEncodedBody((fieldName, "XRM00000000574"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        verify(mockSessionRepository).set(savedAnswersCaptor.capture())
        savedAnswersCaptor.getValue.get(AssociatedRegistrationNumbersPage).value mustEqual Seq(
          "XDM00000001309",
          "XRM00000000574"
        )
      }
    }

    "must return a Bad Request when the registration number has already been submitted" in {

      val userAnswers =
        requiredUserAnswers
          .set(AssociatedRegistrationNumbersPage, Seq("XRM00000000574"))
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, associatedRegNumberRoute)
            .withFormUrlEncodedBody((fieldName, "XRM00000000574"))

        val boundForm = form.fill("XRM00000000574").withError(fieldName, "associatedRegNumber.error.duplicate")
        val view = application.injector.instanceOf[AssociatedRegNumberView]
        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(requiredUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, associatedRegNumberRoute)
            .withFormUrlEncodedBody((fieldName, ""))

        val boundForm = form.bind(Map(fieldName -> ""))

        val view = application.injector.instanceOf[AssociatedRegNumberView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to System Error for a GET if no existing data can be loaded" in {

      val mockConnector = mock[GamblingConnector]

      when(mockConnector.getMgdTradeDetails(any())(any()))
        .thenReturn(Future.failed(new RuntimeException("boom")))

      val application =
        applicationBuilder(userAnswers = None)
          .overrides(bind[GamblingConnector].toInstance(mockConnector))
          .build()

      running(application) {
        val request = FakeRequest(GET, associatedRegNumberRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.SystemErrorController.onPageLoad().url
      }
    }

    "must redirect to System Error for a POST if no existing data can be loaded" in {

      val mockConnector = mock[GamblingConnector]

      when(mockConnector.getMgdTradeDetails(any())(any()))
        .thenReturn(Future.failed(new RuntimeException("boom")))

      val application =
        applicationBuilder(userAnswers = None)
          .overrides(bind[GamblingConnector].toInstance(mockConnector))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, associatedRegNumberRoute)
            .withFormUrlEncodedBody((fieldName, "XRM00000000574"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.SystemErrorController.onPageLoad().url
      }
    }
  }
}
