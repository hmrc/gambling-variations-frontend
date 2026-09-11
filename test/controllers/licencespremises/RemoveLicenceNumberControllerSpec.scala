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

package controllers.licencespremises

import base.SpecBase
import forms.licencespremises.RemoveLicenceNumberFormProvider
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.licencespremises.*
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.licencespremises.RemoveLicenceNumberView

import scala.concurrent.Future

class RemoveLicenceNumberControllerSpec extends SpecBase with MockitoSugar {

  private val formProvider = new RemoveLicenceNumberFormProvider()
  private val form = formProvider()

  private val licenceNumber = "123-456789-A-123456-789"

  private lazy val removeLicenceNumberRoute =
    routes.RemoveLicenceNumberController.onPageLoad().url

  private val userAnswersWithLicenceNumber: UserAnswers =
    emptyUserAnswers
      .set(LicencesPremisesSectionPage, userAnswersId)
      .success
      .value
      .set(LicenceNumberPage, licenceNumber)
      .success
      .value

  private val userAnswersWithoutLicenceNumber: UserAnswers =
    emptyUserAnswers
      .set(LicencesPremisesSectionPage, userAnswersId)
      .success
      .value

  "RemoveLicenceNumberController Controller" - {

    "must return OK and the correct view for a GET" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithLicenceNumber))
          .build()

      running(application) {
        val request = FakeRequest(GET, removeLicenceNumberRoute)

        val result = route(application, request).value

        val view =
          application.injector.instanceOf[RemoveLicenceNumberView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form, NormalMode, licenceNumber)(
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
        applicationBuilder(userAnswers = Some(userAnswersWithLicenceNumber))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(
            POST,
            routes.RemoveLicenceNumberController.onSubmit().url
          )
            .withFormUrlEncodedBody("value" -> "true")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithLicenceNumber))
          .build()

      running(application) {
        val request =
          FakeRequest(
            POST,
            routes.RemoveLicenceNumberController.onSubmit().url
          )
            .withFormUrlEncodedBody("value" -> "")

        val boundForm = form.bind(Map("value" -> ""))

        val result = route(application, request).value

        val view =
          application.injector.instanceOf[RemoveLicenceNumberView]

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual
          view(boundForm, NormalMode, licenceNumber)(
            request,
            messages(application)
          ).toString
      }
    }

    "must redirect to Change Registration Details for a POST when LicenceNumberPage is missing" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithoutLicenceNumber))
          .build()

      running(application) {
        val request =
          FakeRequest(
            POST,
            routes.RemoveLicenceNumberController.onSubmit().url
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
        val request = FakeRequest(GET, removeLicenceNumberRoute)

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
            routes.RemoveLicenceNumberController.onSubmit().url
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
        applicationBuilder(userAnswers = Some(userAnswersWithLicenceNumber))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(
            POST,
            routes.RemoveLicenceNumberController.onSubmit().url
          )
            .withFormUrlEncodedBody("value" -> "true")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        verify(mockSessionRepository).set(savedAnswersCaptor.capture())

        val savedAnswers = savedAnswersCaptor.getValue

        savedAnswers.get(RemoveLicenceNumberPage).value mustEqual true
        savedAnswers.get(HaveGamblingLicenceNoPage).value mustEqual "0"
        savedAnswers.get(LicencesPremisesDetailsSubmittedPage).value mustEqual true
        savedAnswers.get(LicencesPremisesDetailsChangesPage).value mustEqual true
        savedAnswers.get(LicenceNumberPage) mustEqual None
      }
    }

    "must update UserAnswers correctly when false is submitted" in {

      val mockSessionRepository = mock[SessionRepository]
      val savedAnswersCaptor =
        ArgumentCaptor.forClass(classOf[UserAnswers])

      val licence = userAnswersWithLicenceNumber.get(LicenceNumberPage).value

      when(mockSessionRepository.set(any[UserAnswers]))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithLicenceNumber))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(
            POST,
            routes.RemoveLicenceNumberController.onSubmit().url
          )
            .withFormUrlEncodedBody("value" -> "false")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        verify(mockSessionRepository).set(savedAnswersCaptor.capture())

        val savedAnswers = savedAnswersCaptor.getValue

        savedAnswers.get(RemoveLicenceNumberPage).value mustEqual false
        savedAnswers.get(LicenceNumberPage).value mustEqual licence
      }
    }
  }
}
