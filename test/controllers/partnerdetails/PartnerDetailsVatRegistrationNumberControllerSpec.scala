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

package controllers.partnerdetails

import base.SpecBase
import controllers.routes
import forms.partnerdetails.PartnerDetailsVatRegistrationNumberFormProvider
import models.{CheckMode, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.partnerdetails.PartnerDetailsVrnPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.partnerdetails.PartnerDetailsVatRegistrationNumberView

import scala.concurrent.Future

//TODO done
class PartnerDetailsVatRegistrationNumberControllerSpec extends SpecBase with MockitoSugar with PartnerDetailsHelper {

  val formProvider = new PartnerDetailsVatRegistrationNumberFormProvider()
  val form = formProvider()

  def validUserAnswers(vrn: Option[String] = None): UserAnswers = UserAnswers(mgdRegNumber, cleanedDataExistingPartners(vrn = vrn))

  val userAnswersNoVrnNewPartners: UserAnswers = UserAnswers(mgdRegNumber, cleanedDataNewPartners())
  val userAnswersWithVrnNewPartners: UserAnswers = userAnswersNoVrnNewPartners.set(PartnerDetailsVrnPage(newPartnersIndex1), testVRN).success.value

  val userAnswersNoVrnExistingPartners: UserAnswers = UserAnswers(mgdRegNumber, cleanedDataExistingPartners())
  val userAnswersWithVrnExistingPartners: UserAnswers = userAnswersNoVrnNewPartners.set(PartnerDetailsVrnPage(businessNumber1), testVRN).success.value

  val fieldName = "partnerDetailsVatRegistrationNumber"

  lazy val partnerVatRegistrationNumberRouteNewPartners =
    controllers.partnerdetails.routes.PartnerDetailsVatRegistrationNumberController.onPageLoad(newPartnersIndex1.toString, NormalMode).url

  lazy val partnerVatRegistrationNumberRouteExistingPartners =
    controllers.partnerdetails.routes.PartnerDetailsVatRegistrationNumberController.onPageLoad(businessNumber1, CheckMode).url


  "partners" - {

    "PartnerDetailsVatRegistrationNumber Controller" - {

      "must return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(userAnswersNoVrnNewPartners)).build()

        running(application) {
          val request = FakeRequest(GET, partnerVatRegistrationNumberRouteExistingPartners)

          val result = route(application, request).value

          val view = application.injector.instanceOf[PartnerDetailsVatRegistrationNumberView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, businessNumber1, CheckMode)(request, messages(application)).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val application = applicationBuilder(userAnswers = Some(userAnswersWithVrnExistingPartners)).build()

        running(application) {
          val request = FakeRequest(GET, partnerVatRegistrationNumberRouteExistingPartners)

          val view = application.injector.instanceOf[PartnerDetailsVatRegistrationNumberView]

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form.fill(testVRN), businessNumber1, CheckMode)(request, messages(application)).toString
        }
      }

      "must redirect to the next page when valid data is submitted" in {

        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(userAnswersNoVrnExistingPartners))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, partnerVatRegistrationNumberRouteExistingPartners)
              .withFormUrlEncodedBody((fieldName, testVRN))

          val result = route(application, request).value
          val expectedAnswers = userAnswersNoVrnExistingPartners.set(PartnerDetailsVrnPage(businessNumber1), testVRN).success.value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
          verify(mockSessionRepository).set(expectedAnswers)
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {

        val application = applicationBuilder(userAnswers = Some(userAnswersNoVrnExistingPartners)).build()

        running(application) {
          val request =
            FakeRequest(POST, partnerVatRegistrationNumberRouteExistingPartners)
              .withFormUrlEncodedBody((fieldName, "xyz"))

          val boundForm = form.bind(Map(fieldName -> "xyz"))

          val view = application.injector.instanceOf[PartnerDetailsVatRegistrationNumberView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, businessNumber1, CheckMode)(request, messages(application)).toString
        }
      }

      "must redirect to SystemError for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, partnerVatRegistrationNumberRouteExistingPartners)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.SystemErrorController.onPageLoad().url
        }
      }

      "must redirect to SystemError for a POST if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request =
            FakeRequest(POST, partnerVatRegistrationNumberRouteExistingPartners)
              .withFormUrlEncodedBody((fieldName, "GB353868127"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.SystemErrorController.onPageLoad().url
        }
      }
    }

  }


  "newPartners" - {

    "PartnerDetailsVatRegistrationNumber Controller" - {

      "must return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(userAnswersNoVrnNewPartners)).build()

        running(application) {
          val request = FakeRequest(GET, partnerVatRegistrationNumberRouteNewPartners)

          val result = route(application, request).value

          val view = application.injector.instanceOf[PartnerDetailsVatRegistrationNumberView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, newPartnersIndex1.toString, NormalMode)(request, messages(application)).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val application = applicationBuilder(userAnswers = Some(userAnswersWithVrnNewPartners)).build()

        running(application) {
          val request = FakeRequest(GET, partnerVatRegistrationNumberRouteNewPartners)

          val view = application.injector.instanceOf[PartnerDetailsVatRegistrationNumberView]

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form.fill(testVRN), newPartnersIndex1.toString, NormalMode)(request, messages(application)).toString
        }
      }

      "must redirect to the next page when valid data is submitted" in {

        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(userAnswersNoVrnNewPartners))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, partnerVatRegistrationNumberRouteNewPartners)
              .withFormUrlEncodedBody((fieldName, testVRN))

          val result = route(application, request).value
          val expectedAnswers = userAnswersNoVrnNewPartners.set(PartnerDetailsVrnPage(newPartnersIndex1), testVRN).success.value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
          verify(mockSessionRepository).set(expectedAnswers)
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {

        val application = applicationBuilder(userAnswers = Some(userAnswersNoVrnNewPartners)).build()

        running(application) {
          val request =
            FakeRequest(POST, partnerVatRegistrationNumberRouteNewPartners)
              .withFormUrlEncodedBody((fieldName, "xyz"))

          val boundForm = form.bind(Map(fieldName -> "xyz"))

          val view = application.injector.instanceOf[PartnerDetailsVatRegistrationNumberView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, newPartnersIndex1.toString, NormalMode)(request, messages(application)).toString
        }
      }

      "must redirect to SystemError for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, partnerVatRegistrationNumberRouteNewPartners)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.SystemErrorController.onPageLoad().url
        }
      }

      "must redirect to SystemError for a POST if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request =
            FakeRequest(POST, partnerVatRegistrationNumberRouteNewPartners)
              .withFormUrlEncodedBody((fieldName, "GB353868127"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.SystemErrorController.onPageLoad().url
        }
      }
    }

  }
}
