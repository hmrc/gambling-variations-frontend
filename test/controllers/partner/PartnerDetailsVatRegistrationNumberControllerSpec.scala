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

package controllers.partner

import base.SpecBase
import controllers.routes
import forms.partner.PartnerDetailsVatRegistrationNumberFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.partnerdetails.PartnerDetailsVrnPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.partner.PartnerDetailsVatRegistrationNumberView

import scala.concurrent.Future

class PartnerDetailsVatRegistrationNumberControllerSpec extends SpecBase with MockitoSugar with PartnerDetailsHelper {

  val formProvider = new PartnerDetailsVatRegistrationNumberFormProvider()
  val form = formProvider()

  def validUserAnswers(vrn: Option[String] = None): UserAnswers = UserAnswers(mgdRegNumber, cleanedData(vrn = vrn))
  val userAnswersNoVrn: UserAnswers = validUserAnswers()
  val userAnswersWithVrn: UserAnswers = validUserAnswers(Some(testVRN))

  val fieldName = "partnerDetailsVatRegistrationNumber"

  lazy val partnerVatRegistrationNumberRoute = controllers.partner.routes.PartnerDetailsVatRegistrationNumberController.onPageLoad().url

  "PartnerDetailsVatRegistrationNumber Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersNoVrn)).build()

      running(application) {
        val request = FakeRequest(GET, partnerVatRegistrationNumberRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PartnerDetailsVatRegistrationNumberView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithVrn)).build()

      running(application) {
        val request = FakeRequest(GET, partnerVatRegistrationNumberRoute)

        val view = application.injector.instanceOf[PartnerDetailsVatRegistrationNumberView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(testVRN), NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswersNoVrn))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, partnerVatRegistrationNumberRoute)
            .withFormUrlEncodedBody((fieldName, testVRN))

        val result = route(application, request).value
        val expectedAnswers = userAnswersNoVrn.set(PartnerDetailsVrnPage(index), testVRN).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
        verify(mockSessionRepository).set(expectedAnswers)
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersNoVrn)).build()

      running(application) {
        val request =
          FakeRequest(POST, partnerVatRegistrationNumberRoute)
            .withFormUrlEncodedBody((fieldName, "xyz"))

        val boundForm = form.bind(Map(fieldName -> "xyz"))

        val view = application.injector.instanceOf[PartnerDetailsVatRegistrationNumberView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to SystemError for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, partnerVatRegistrationNumberRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.SystemErrorController.onPageLoad().url
      }
    }

    "must redirect to SystemError for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, partnerVatRegistrationNumberRoute)
            .withFormUrlEncodedBody((fieldName, "GB353868127"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.SystemErrorController.onPageLoad().url
      }
    }
  }
}
