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
import forms.controllingbody.RemoveControllingBodyEmailFormProvider
import models.{Address, ContactNumber, CorrespondenceDetails, NormalMode, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.controllingbody.*
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.controllingbody.RemoveControllingBodyEmailView

import scala.concurrent.Future

class RemoveControllingBodyEmailControllerSpec extends SpecBase with MockitoSugar {

  private val formProvider = new RemoveControllingBodyEmailFormProvider()
  private val form = formProvider()

  private val email = "abc@def.com"

  private lazy val removeControllingBodyEmailRoute =
    routes.RemoveControllingBodyEmailController.onPageLoad().url

  private val correspondenceDetails = CorrespondenceDetails(
    mgdRegNumber = userAnswersId,
    nameLine1    = None,
    nameLine2    = None,
    correspondenceAddress = Some(
      Address(
        address1 = "Address 1",
        address2 = Some("Address 2"),
        address3 = Some("Address 3"),
        address4 = Some("Address 4"),
        postcode = Some("postcode"),
        country  = Some("Spain")
      )
    ),
    additionalInformation = Some("adi"),
    iomOrCiFlag           = Some("0"),
    contactNumber = Some(
      ContactNumber(
        phoneNumber       = Some("phoneNumber"),
        mobilePhoneNumber = Some("mobilePhoneNumber")
      )
    ),
    faxNumber = Some("faxNumber"),
    emailAddr = Some("emailAddr")
  )

  private val userAnswersWithEmail: UserAnswers =
    emptyUserAnswers
      .set(ControllingBodySectionPage, userAnswersId)
      .success
      .value
      .set(ControllingBodyCorrespondenceSectionPage, correspondenceDetails)
      .success
      .value
      .set(ControllingBodyEmailPage, email)
      .success
      .value

  private val userAnswersWithoutEmail: UserAnswers =
    emptyUserAnswers
      .set(ControllingBodySectionPage, userAnswersId)
      .success
      .value

  "RemoveControllingBodyEmailController Controller" - {

    "must return OK and the correct view for a GET" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithEmail))
          .build()

      running(application) {
        val request =
          FakeRequest(GET, removeControllingBodyEmailRoute)

        val result = route(application, request).value

        val view =
          application.injector.instanceOf[RemoveControllingBodyEmailView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form, NormalMode, email)(
            request,
            messages(application)
          ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any[UserAnswers]))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithEmail))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(
            POST,
            routes.RemoveControllingBodyEmailController
              .onSubmit()
              .url
          )
            .withFormUrlEncodedBody("value" -> "true")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithEmail))
          .build()

      running(application) {
        val request =
          FakeRequest(
            POST,
            routes.RemoveControllingBodyEmailController
              .onSubmit()
              .url
          )
            .withFormUrlEncodedBody("value" -> "")

        val boundForm = form.bind(Map("value" -> ""))

        val result = route(application, request).value

        val view =
          application.injector.instanceOf[RemoveControllingBodyEmailView]

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual
          view(boundForm, NormalMode, email)(
            request,
            messages(application)
          ).toString
      }
    }

    "must redirect to System Error for a GET if no existing email is found" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithoutEmail))
          .build()

      running(application) {
        val request =
          FakeRequest(GET, removeControllingBodyEmailRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.routes.SystemErrorController.onPageLoad().url
      }
    }

    "must redirect to System Error for a POST if no existing email is found" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithoutEmail))
          .build()

      running(application) {
        val request =
          FakeRequest(
            POST,
            routes.RemoveControllingBodyEmailController
              .onSubmit()
              .url
          )
            .withFormUrlEncodedBody("value" -> "true")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.routes.SystemErrorController.onPageLoad().url
      }
    }

    "must redirect to System Error for a GET if no existing data is found" in {

      val application =
        applicationBuilder(userAnswers = None)
          .build()

      running(application) {
        val request =
          FakeRequest(GET, removeControllingBodyEmailRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.routes.SystemErrorController.onPageLoad().url
      }
    }

    "must redirect to System Error for a POST if no existing data is found" in {

      val application =
        applicationBuilder(userAnswers = None)
          .build()

      running(application) {
        val request =
          FakeRequest(
            POST,
            routes.RemoveControllingBodyEmailController
              .onSubmit()
              .url
          )
            .withFormUrlEncodedBody("value" -> "true")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.routes.SystemErrorController.onPageLoad().url
      }
    }

    "must update UserAnswers correctly when true is submitted" in {

      val mockSessionRepository = mock[SessionRepository]
      val savedAnswersCaptor =
        ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockSessionRepository.set(any[UserAnswers]))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithEmail))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(
            POST,
            routes.RemoveControllingBodyEmailController
              .onSubmit()
              .url
          )
            .withFormUrlEncodedBody("value" -> "true")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        verify(mockSessionRepository).set(savedAnswersCaptor.capture())

        val savedAnswers = savedAnswersCaptor.getValue

        savedAnswers.get(RemoveControllingBodyEmailPage).value mustEqual true
        savedAnswers.get(ControllingBodySubmittedPage).value mustEqual true
        savedAnswers.get(ControllingBodyChangesPage).value mustEqual true
        savedAnswers.get(ControllingBodyEmailPage) mustEqual None
      }
    }

    "must update UserAnswers correctly when false is submitted" in {

      val mockSessionRepository = mock[SessionRepository]
      val savedAnswersCaptor =
        ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockSessionRepository.set(any[UserAnswers]))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithEmail))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(
            POST,
            routes.RemoveControllingBodyEmailController
              .onSubmit()
              .url
          )
            .withFormUrlEncodedBody("value" -> "false")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        verify(mockSessionRepository).set(savedAnswersCaptor.capture())

        val savedAnswers = savedAnswersCaptor.getValue

        savedAnswers.get(RemoveControllingBodyEmailPage).value mustEqual false
        savedAnswers.get(ControllingBodySubmittedPage).value mustEqual true
        savedAnswers.get(ControllingBodyChangesPage).value mustEqual false
        savedAnswers.get(ControllingBodyEmailPage).value mustEqual email
      }
    }

    "must keep an earlier change to the section when false is submitted" in {

      val mockSessionRepository = mock[SessionRepository]
      val savedAnswersCaptor =
        ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockSessionRepository.set(any[UserAnswers]))
        .thenReturn(Future.successful(true))

      val answersWithEarlierChange =
        userAnswersWithEmail
          .set(ControllingBodyChangesPage, true)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(answersWithEarlierChange))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(
            POST,
            routes.RemoveControllingBodyEmailController
              .onSubmit()
              .url
          )
            .withFormUrlEncodedBody("value" -> "false")

        status(route(application, request).value) mustEqual SEE_OTHER

        verify(mockSessionRepository).set(savedAnswersCaptor.capture())

        val savedAnswers = savedAnswersCaptor.getValue

        savedAnswers.get(ControllingBodySubmittedPage).value mustEqual true
        savedAnswers.get(ControllingBodyChangesPage).value mustEqual false
        savedAnswers.get(RemoveControllingBodyEmailPage).value mustEqual false
        savedAnswers.get(ControllingBodyEmailPage).value mustEqual email
      }
    }
  }
}
