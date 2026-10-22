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
import controllers.controllingbody.routes.ControllingBodyCompanyRegistrationNumberController
import forms.controllingbody.ControllingBodyCompanyRegistrationNumberFormProvider
import models.BusinessType.{Corporatebody, LimitedLiabilityPartnership}
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.controllingbody.*
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.controllingbody.ControllingBodyCompanyRegistrationNumberView

import scala.concurrent.Future

class ControllingBodyCompanyRegistrationNumberControllerSpec extends SpecBase with MockitoSugar {
  val formProvider = new ControllingBodyCompanyRegistrationNumberFormProvider()
  private val form = formProvider()

  private val crn = "12345678"

  private lazy val companyRegistrationNumberRoute: String =
    ControllingBodyCompanyRegistrationNumberController.onPageLoad().url

  private lazy val companyRegistrationNumberSubmitRoute: String =
    ControllingBodyCompanyRegistrationNumberController.onSubmit().url

  private val corporateBodyAnswers: UserAnswers =
    emptyUserAnswers
      .set(ControllingBodySectionPage, userAnswersId)
      .success
      .value
      .set(ControllingBodyBusinessTypePage, Corporatebody)
      .success
      .value
      .set(ControllingBodyIsUkIncorporatedPage, "1")
      .success
      .value

  private val answersWithCrn: UserAnswers =
    corporateBodyAnswers
      .set(ControllingBodySectionPage, userAnswersId)
      .success
      .value
      .set(ControllingBodyCrnPage, crn)
      .success
      .value

  private val llpAnswers: UserAnswers =
    emptyUserAnswers
      .set(ControllingBodySectionPage, userAnswersId)
      .success
      .value
      .set(ControllingBodyBusinessTypePage, LimitedLiabilityPartnership)
      .success
      .value

  "ControllingBodyCompanyRegistrationNumberController" - {

    "must return OK and the correct view for a GET" in {

      val application =
        applicationBuilder(userAnswers = Some(corporateBodyAnswers))
          .build()

      running(application) {
        val request =
          FakeRequest(GET, companyRegistrationNumberRoute)

        val result = route(application, request).value

        val view =
          application.injector
            .instanceOf[ControllingBodyCompanyRegistrationNumberView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form, NormalMode)(
            request,
            messages(application)
          ).toString
      }
    }

    "must pre-populate the form when a CRN already exists" in {

      val application =
        applicationBuilder(userAnswers = Some(answersWithCrn))
          .build()

      running(application) {
        val request =
          FakeRequest(GET, companyRegistrationNumberRoute)

        val result = route(application, request).value

        val view =
          application.injector
            .instanceOf[ControllingBodyCompanyRegistrationNumberView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form.fill(crn), NormalMode)(
            request,
            messages(application)
          ).toString
      }
    }

    "must return OK for a limited liability partnership without UK incorporation data" in {

      val application =
        applicationBuilder(userAnswers = Some(llpAnswers))
          .build()

      running(application) {
        val request =
          FakeRequest(GET, companyRegistrationNumberRoute)

        val result = route(application, request).value

        status(result) mustEqual OK
      }
    }

    "must redirect to System Error for a corporate body not incorporated in the UK" in {

      val answers =
        emptyUserAnswers
          .set(ControllingBodyBusinessTypePage, Corporatebody)
          .success
          .value
          .set(ControllingBodyIsUkIncorporatedPage, "0")
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(answers))
          .build()

      running(application) {
        val request =
          FakeRequest(GET, companyRegistrationNumberRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.routes.SystemErrorController.onPageLoad().url
      }
    }

    "must redirect to System Error when the business type is missing" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .build()

      running(application) {
        val request =
          FakeRequest(GET, companyRegistrationNumberRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.routes.SystemErrorController.onPageLoad().url
      }
    }

    "must redirect to System Error when user answers are missing on GET" in {

      val application =
        applicationBuilder(userAnswers = None)
          .build()

      running(application) {
        val request =
          FakeRequest(GET, companyRegistrationNumberRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.routes.SystemErrorController.onPageLoad().url
      }
    }

    "must return Bad Request when invalid data is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(corporateBodyAnswers))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, companyRegistrationNumberSubmitRoute)
            .withFormUrlEncodedBody("value" -> "")

        val boundForm = form.bind(Map("value" -> ""))

        val result = route(application, request).value

        val view =
          application.injector
            .instanceOf[ControllingBodyCompanyRegistrationNumberView]

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual
          view(boundForm, NormalMode)(
            request,
            messages(application)
          ).toString
      }
    }

    "must redirect after valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any[UserAnswers]))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(corporateBodyAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, companyRegistrationNumberSubmitRoute)
            .withFormUrlEncodedBody("value" -> crn)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
      }
    }

    "must update UserAnswers correctly when a CRN is submitted for the first time" in {

      val mockSessionRepository = mock[SessionRepository]
      val savedAnswersCaptor =
        ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockSessionRepository.set(any[UserAnswers]))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(corporateBodyAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, companyRegistrationNumberSubmitRoute)
            .withFormUrlEncodedBody("value" -> crn)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        verify(mockSessionRepository).set(savedAnswersCaptor.capture())

        val savedAnswers = savedAnswersCaptor.getValue

        savedAnswers.get(ControllingBodyCrnPage).value mustEqual crn
        savedAnswers.get(ControllingBodySubmittedPage).value mustEqual true
        savedAnswers.get(ControllingBodyChangesPage).value mustEqual true
      }
    }

    "must mark changes as false when the submitted CRN has not changed" in {

      val mockSessionRepository = mock[SessionRepository]
      val savedAnswersCaptor =
        ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockSessionRepository.set(any[UserAnswers]))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(answersWithCrn))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, companyRegistrationNumberSubmitRoute)
            .withFormUrlEncodedBody("value" -> crn)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        verify(mockSessionRepository).set(savedAnswersCaptor.capture())

        val savedAnswers = savedAnswersCaptor.getValue

        savedAnswers.get(ControllingBodyCrnPage).value mustEqual crn
        savedAnswers.get(ControllingBodySubmittedPage).value mustEqual true
        savedAnswers.get(ControllingBodyChangesPage).value mustEqual false
      }
    }

    "must mark changes as true when the submitted CRN differs from the existing CRN" in {

      val mockSessionRepository = mock[SessionRepository]
      val savedAnswersCaptor =
        ArgumentCaptor.forClass(classOf[UserAnswers])

      val existingCrn = "87654321"

      val answers =
        corporateBodyAnswers
          .set(ControllingBodyCrnPage, existingCrn)
          .success
          .value

      when(mockSessionRepository.set(any[UserAnswers]))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(answers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, companyRegistrationNumberSubmitRoute)
            .withFormUrlEncodedBody("value" -> crn)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        verify(mockSessionRepository).set(savedAnswersCaptor.capture())

        val savedAnswers = savedAnswersCaptor.getValue

        savedAnswers.get(ControllingBodyCrnPage).value mustEqual crn
        savedAnswers.get(ControllingBodySubmittedPage).value mustEqual true
        savedAnswers.get(ControllingBodyChangesPage).value mustEqual true
      }
    }

    "must redirect to System Error when user answers are missing on POST" in {

      val application =
        applicationBuilder(userAnswers = None)
          .build()

      running(application) {
        val request =
          FakeRequest(POST, companyRegistrationNumberSubmitRoute)
            .withFormUrlEncodedBody("value" -> crn)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.routes.SystemErrorController.onPageLoad().url
      }
    }
  }
}
