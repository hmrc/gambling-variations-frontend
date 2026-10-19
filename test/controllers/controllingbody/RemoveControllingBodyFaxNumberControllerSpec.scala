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
import forms.controllingbody.RemoveControllingBodyFaxNumberFormProvider
import models.{CorrespondenceDetails, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.controllingbody.*
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.controllingbody.RemoveControllingBodyFaxNumberView

import scala.concurrent.Future

class RemoveControllingBodyFaxNumberControllerSpec extends SpecBase with MockitoSugar {

  private def onwardRoute = Call("GET", "/foo")

  private val formProvider = new RemoveControllingBodyFaxNumberFormProvider()
  private val form = formProvider()

  private val faxNumber = "020 7844 4444"
  private val emailAddress = "test@example.com"

  private lazy val removeControllingBodyFaxNumberRoute =
    routes.RemoveControllingBodyFaxNumberController.onPageLoad().url

  private lazy val submitRoute =
    routes.RemoveControllingBodyFaxNumberController.onSubmit().url

  private val userAnswersWithoutFaxNumber: UserAnswers =
    emptyUserAnswers
      .set(ControllingBodySectionPage, userAnswersId)
      .success
      .value

  private val userAnswersWithFaxNumber: UserAnswers =
    userAnswersWithoutFaxNumber
      .set(ControllingBodyFaxNumberPage, faxNumber)
      .success
      .value

  private def submitRequest(value: String) =
    FakeRequest(POST, submitRoute).withFormUrlEncodedBody("value" -> value)

  "RemoveControllingBodyFaxNumber Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithFaxNumber)).build()

      running(application) {
        val request = FakeRequest(GET, removeControllingBodyFaxNumberRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RemoveControllingBodyFaxNumberView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form, NormalMode, faxNumber)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers =
        userAnswersWithFaxNumber
          .set(RemoveControllingBodyFaxNumberPage, false)
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, removeControllingBodyFaxNumberRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RemoveControllingBodyFaxNumberView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form.fill(false), NormalMode, faxNumber)(request, messages(application)).toString
      }
    }

    "must show the fax number as stored by the controlling body details loader" in {

      // The loader stores the whole correspondence section as one object, and the page must find the fax number in it
      val correspondenceDetails = CorrespondenceDetails(
        mgdRegNumber          = userAnswersId,
        nameLine1             = None,
        nameLine2             = None,
        correspondenceAddress = None,
        additionalInformation = None,
        iomOrCiFlag           = None,
        contactNumber         = None,
        faxNumber             = Some(faxNumber),
        emailAddr             = Some(emailAddress)
      )

      val userAnswers =
        userAnswersWithoutFaxNumber
          .set(ControllingBodyCorrespondenceSectionPage, correspondenceDetails)
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, removeControllingBodyFaxNumberRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RemoveControllingBodyFaxNumberView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form, NormalMode, faxNumber)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any[UserAnswers])).thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithFaxNumber))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val result = route(application, submitRequest("true")).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when no answer is submitted" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithFaxNumber)).build()

      running(application) {
        val request = submitRequest("")

        val boundForm = form.bind(Map("value" -> ""))

        val result = route(application, request).value

        val view = application.injector.instanceOf[RemoveControllingBodyFaxNumberView]

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual
          view(boundForm, NormalMode, faxNumber)(request, messages(application)).toString
      }
    }

    "must redirect to System Error for a GET if the controlling body has no fax number" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithoutFaxNumber)).build()

      running(application) {
        val request = FakeRequest(GET, removeControllingBodyFaxNumberRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.SystemErrorController.onPageLoad().url
      }
    }

    "must redirect to System Error for a POST if the controlling body has no fax number" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithoutFaxNumber)).build()

      running(application) {
        val result = route(application, submitRequest("true")).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.SystemErrorController.onPageLoad().url
      }
    }

    "must redirect to System Error for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, removeControllingBodyFaxNumberRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.SystemErrorController.onPageLoad().url
      }
    }

    "must redirect to System Error for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val result = route(application, submitRequest("true")).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.SystemErrorController.onPageLoad().url
      }
    }

    "must remove the fax number and flag the section as changed when yes is submitted" in {

      val mockSessionRepository = mock[SessionRepository]
      val savedAnswersCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockSessionRepository.set(any[UserAnswers])).thenReturn(Future.successful(true))

      val answersWithEmailAddress =
        userAnswersWithFaxNumber
          .set(ControllingBodyEmailPage, emailAddress)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(answersWithEmailAddress))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        status(route(application, submitRequest("true")).value) mustEqual SEE_OTHER

        verify(mockSessionRepository).set(savedAnswersCaptor.capture())

        val savedAnswers = savedAnswersCaptor.getValue

        savedAnswers.get(RemoveControllingBodyFaxNumberPage).value mustEqual true
        savedAnswers.get(ControllingBodySubmittedPage).value mustEqual true
        savedAnswers.get(ControllingBodyChangesPage).value mustEqual true
        savedAnswers.get(ControllingBodyFaxNumberPage) mustEqual None
        savedAnswers.get(ControllingBodyEmailPage).value mustEqual emailAddress
      }
    }

    "must keep the fax number and not flag the section as changed when no is submitted" in {

      val mockSessionRepository = mock[SessionRepository]
      val savedAnswersCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockSessionRepository.set(any[UserAnswers])).thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithFaxNumber))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        status(route(application, submitRequest("false")).value) mustEqual SEE_OTHER

        verify(mockSessionRepository).set(savedAnswersCaptor.capture())

        val savedAnswers = savedAnswersCaptor.getValue

        savedAnswers.get(RemoveControllingBodyFaxNumberPage).value mustEqual false
        savedAnswers.get(ControllingBodySubmittedPage).value mustEqual true
        savedAnswers.get(ControllingBodyChangesPage).value mustEqual false
        savedAnswers.get(ControllingBodyFaxNumberPage).value mustEqual faxNumber
      }
    }

    "must keep an earlier change to the section when no is submitted" in {

      val mockSessionRepository = mock[SessionRepository]
      val savedAnswersCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockSessionRepository.set(any[UserAnswers])).thenReturn(Future.successful(true))

      val answersWithEarlierChange =
        userAnswersWithFaxNumber
          .set(ControllingBodyChangesPage, true)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(answersWithEarlierChange))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        status(route(application, submitRequest("false")).value) mustEqual SEE_OTHER

        verify(mockSessionRepository).set(savedAnswersCaptor.capture())

        val savedAnswers = savedAnswersCaptor.getValue

        savedAnswers.get(RemoveControllingBodyFaxNumberPage).value mustEqual false
        savedAnswers.get(ControllingBodySubmittedPage).value mustEqual true
        savedAnswers.get(ControllingBodyChangesPage).value mustEqual true
        savedAnswers.get(ControllingBodyFaxNumberPage).value mustEqual faxNumber
      }
    }
  }
}
