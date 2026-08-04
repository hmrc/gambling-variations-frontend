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
import forms.RemovePreviousRegNumberFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.{ChosenPreviousRegNumberPage, MgdTradeDetailsSectionPage, PreviousRegNumbersUpdatedPage, RemovePreviousRegNumberPage, TradingDetailsChangesPage, UnsubmittedPreviousRegNumbersPage}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.RemovePreviousRegNumberView

import scala.concurrent.Future

class RemovePreviousRegNumberControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new RemovePreviousRegNumberFormProvider()
  val form = formProvider()

  lazy val removePreviousRegNumberRoute = routes.RemovePreviousRegNumberController.onPageLoad(NormalMode).url
  private val prevRegSeq = Some(Seq("XYM00000000", "b", "c"))
  private val baseAnswers =
    UserAnswers(
      userAnswersId,
      Json.obj(
        "previousRegistrationNumbers" -> prevRegSeq,
        "chosenPreviousRegNumber"     -> "XYM00000000",
        "mgdTradeDetailsSection"      -> Json.obj("mgdRegNum" -> userAnswersId)
      )
    )

  private val answersWithChosenPreviousRegNumber =
    emptyUserAnswers
      .set(MgdTradeDetailsSectionPage, "MGD999999")
      .success
      .value
      .set(ChosenPreviousRegNumberPage, "XYM00000000")
      .success
      .value
      .set(UnsubmittedPreviousRegNumbersPage, Seq("XYM00000000", "b", "c"))
      .success
      .value

  "RemovePreviousRegNumber Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, removePreviousRegNumberRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RemovePreviousRegNumberView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, "XYM00000000")(request, messages(application)).toString
      }
    }

    "must populate the view when RemovePreviousRegNumberPage has previously been answered" in {

      val userAnswers =
        answersWithChosenPreviousRegNumber
          .set(RemovePreviousRegNumberPage, true)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {

        val request =
          FakeRequest(GET, removePreviousRegNumberRoute)

        val view =
          application.injector.instanceOf[RemovePreviousRegNumberView]

        val result =
          route(application, request).value

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form.fill(true), NormalMode, "XYM00000000")(request, messages(application)).toString
      }
    }

    "must redirect to System Error on GET when ChosenPreviousRegNumberPage is missing" in {

      val userAnswers =
        emptyUserAnswers
          .set(MgdTradeDetailsSectionPage, "MGD999999")
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {

        val request =
          FakeRequest(GET, removePreviousRegNumberRoute)

        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.SystemErrorController.onPageLoad().url
      }
    }

    "must redirect to ChangeRegistrationDetails when chosen previous registration number is missing on submit" in {

      val userAnswers =
        emptyUserAnswers
          .set(MgdTradeDetailsSectionPage, "MGD999999")
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {

        val request =
          FakeRequest(POST, removePreviousRegNumberRoute)
            .withFormUrlEncodedBody(
              "value" -> "true"
            )

        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.ChangeRegistrationDetailsController.onPageLoad().url
      }
    }

    "must remove chosen previous registration number when user selects yes" in {

      val mockSessionRepository =
        mock[SessionRepository]

      val savedAnswersCaptor =
        ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockSessionRepository.set(any()))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(answersWithChosenPreviousRegNumber))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {

        val request =
          FakeRequest(POST, removePreviousRegNumberRoute)
            .withFormUrlEncodedBody(
              "value" -> "true"
            )

        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER

        verify(mockSessionRepository).set(savedAnswersCaptor.capture())

        val savedAnswers =
          savedAnswersCaptor.getValue

        savedAnswers.get(RemovePreviousRegNumberPage).value mustEqual true
        savedAnswers.get(UnsubmittedPreviousRegNumbersPage).value mustEqual Seq("b", "c")
        savedAnswers.get(PreviousRegNumbersUpdatedPage).value mustEqual true
        savedAnswers.get(TradingDetailsChangesPage).value mustEqual true
      }
    }

    "must not remove previous registration number when user selects no" in {

      val mockSessionRepository =
        mock[SessionRepository]

      val savedAnswersCaptor =
        ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockSessionRepository.set(any()))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(answersWithChosenPreviousRegNumber))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {

        val request =
          FakeRequest(POST, removePreviousRegNumberRoute)
            .withFormUrlEncodedBody(
              "value" -> "false"
            )

        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER

        verify(mockSessionRepository).set(savedAnswersCaptor.capture())

        val savedAnswers =
          savedAnswersCaptor.getValue

        savedAnswers.get(RemovePreviousRegNumberPage).value mustEqual false
        savedAnswers.get(UnsubmittedPreviousRegNumbersPage).value mustEqual Seq("XYM00000000", "b", "c")
        savedAnswers.get(PreviousRegNumbersUpdatedPage).value mustEqual true
        savedAnswers.get(TradingDetailsChangesPage).value mustEqual false
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
          FakeRequest(POST, removePreviousRegNumberRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must update data correctly when submitted in" in {

      val mockSessionRepository = mock[SessionRepository]
      val savedAnswersCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])

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
          FakeRequest(POST, removePreviousRegNumberRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        verify(mockSessionRepository).set(savedAnswersCaptor.capture())
        savedAnswersCaptor.getValue.get(RemovePreviousRegNumberPage).value mustEqual true
      }
    }

    "must flag TradingDetailsChangesPage when data changed in" in {

      val mockSessionRepository = mock[SessionRepository]
      val savedAnswersCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])

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
          FakeRequest(POST, removePreviousRegNumberRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        verify(mockSessionRepository).set(savedAnswersCaptor.capture())
        savedAnswersCaptor.getValue.get(RemovePreviousRegNumberPage).value mustEqual true
        savedAnswersCaptor.getValue.get(TradingDetailsChangesPage).value mustEqual true
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, removePreviousRegNumberRoute)
            .withFormUrlEncodedBody(("value", ""))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
      }
    }

    "must redirect to System Error for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, removePreviousRegNumberRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.SystemErrorController.onPageLoad().url
      }
    }

    "must redirect to System Error for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, removePreviousRegNumberRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.SystemErrorController.onPageLoad().url
      }
    }
  }
}
