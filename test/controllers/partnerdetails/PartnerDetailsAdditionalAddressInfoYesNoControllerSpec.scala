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
import forms.partnerdetails.PartnerDetailsAdditionalAddressInfoYesNoFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.partnerdetails.PartnerDetailsAdditionalAddressInfoYesNoPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.partnerdetails.PartnerDetailsAdditionalAddressInfoYesNoView

import scala.concurrent.Future

//TODO normalModeOnly - Done - Maybe fix consistency with userAnswers
class PartnerDetailsAdditionalAddressInfoYesNoControllerSpec extends SpecBase with MockitoSugar with PartnerDetailsHelper {

  val formProvider = new PartnerDetailsAdditionalAddressInfoYesNoFormProvider()
  val form = formProvider()
  val partnerDetailsMinimalValidData: UserAnswers = UserAnswers(userAnswersId, minimalValidData)

  lazy val partnerDetailsAdditionalAddressInfoYesNoRouteNewPartners =
    controllers.partnerdetails.routes.PartnerDetailsAdditionalAddressInfoYesNoController.onPageLoad(newPartnersIndex1.toString).url

  "PartnerDetailsAdditionalAddressInfoYesNo Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(partnerDetailsMinimalValidData)).build()

      running(application) {
        val request = FakeRequest(GET, partnerDetailsAdditionalAddressInfoYesNoRouteNewPartners)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PartnerDetailsAdditionalAddressInfoYesNoView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, newPartnersIndex1.toString, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = partnerDetailsMinimalValidData.set(PartnerDetailsAdditionalAddressInfoYesNoPage(newPartnersIndex1), true).success.value
      println(userAnswers)

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, partnerDetailsAdditionalAddressInfoYesNoRouteNewPartners)

        val view = application.injector.instanceOf[PartnerDetailsAdditionalAddressInfoYesNoView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), newPartnersIndex1.toString, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(partnerDetailsMinimalValidData))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, partnerDetailsAdditionalAddressInfoYesNoRouteNewPartners)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(partnerDetailsMinimalValidData)).build()

      running(application) {
        val request =
          FakeRequest(POST, partnerDetailsAdditionalAddressInfoYesNoRouteNewPartners)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[PartnerDetailsAdditionalAddressInfoYesNoView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, newPartnersIndex1.toString, NormalMode)(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = Some(partnerDetailsMinimalValidData)).build()

      running(application) {
        val request = FakeRequest(GET, partnerDetailsAdditionalAddressInfoYesNoRouteNewPartners)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PartnerDetailsAdditionalAddressInfoYesNoView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, newPartnersIndex1.toString, NormalMode)(request, messages(application)).toString
      }
    }

    "must save the correct value to userAnswers when submitted" in {

      val mockSessionRepository = mock[SessionRepository]
      val savedAnswersCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(partnerDetailsMinimalValidData))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, partnerDetailsAdditionalAddressInfoYesNoRouteNewPartners)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        verify(mockSessionRepository).set(savedAnswersCaptor.capture())
        savedAnswersCaptor.getValue.get(PartnerDetailsAdditionalAddressInfoYesNoPage(newPartnersIndex1)).value mustEqual true
      }
    }

    "must redirect to the next page for a POST if no existing data is found" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = None)
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, partnerDetailsAdditionalAddressInfoYesNoRouteNewPartners)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustBe controllers.routes.SystemErrorController.onPageLoad().url
      }
    }
  }
}
