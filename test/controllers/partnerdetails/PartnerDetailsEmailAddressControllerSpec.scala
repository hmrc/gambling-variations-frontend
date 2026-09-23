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
import forms.partnerdetails.PartnerEmailAddressFormProvider
import models.{CheckMode, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.partnerdetails.PartnerDetailsEmailAddressPage
import play.api.data.Form
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.partnerdetails.PartnerDetailsEmailAddressView

import scala.concurrent.Future

//TODO done
class PartnerDetailsEmailAddressControllerSpec extends SpecBase with MockitoSugar with PartnerDetailsHelper {

  val formProvider = new PartnerEmailAddressFormProvider()
  val form: Form[String] = formProvider("partnerEmailAddress")

  lazy val partnerEmailAddressRouteExistingPartners: String =
    controllers.partnerdetails.routes.PartnerDetailsEmailAddressController.onPageLoad(businessNumber1, CheckMode).url
    
  lazy val partnerEmailAddressRouteNewPartners: String =
    controllers.partnerdetails.routes.PartnerDetailsEmailAddressController.onPageLoad(newPartnersIndex1.toString, NormalMode).url
    
  val minimalUserAnswersNewPartners: UserAnswers = userAnswersPartnerDetailsNewPartners
  
  val minimalUserAnswersExistingPartners: UserAnswers = userAnswersPartnerDetailsExistingPartners

  "newPartners" - {

    "PartnerEmailAddress Controller" - {

      "must return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(minimalUserAnswersNewPartners)).build()

        running(application) {
          val request = FakeRequest(GET, partnerEmailAddressRouteNewPartners)

          val result = route(application, request).value

          val view = application.injector.instanceOf[PartnerDetailsEmailAddressView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual
            view(form, newPartnersIndex1.toString, NormalMode)(request, messages(application)).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val userAnswers = minimalUserAnswersNewPartners
          .set(PartnerDetailsEmailAddressPage(newPartnersIndex1), "validEmail@example.com")
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, partnerEmailAddressRouteNewPartners)

          val view = application.injector.instanceOf[PartnerDetailsEmailAddressView]

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual
            view(
              form.fill("validEmail@example.com"),
              newPartnersIndex1.toString,
              NormalMode
            )(request, messages(application)).toString
        }
      }

      "must redirect to the next page when valid data is submitted" in {

        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(minimalUserAnswersNewPartners))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, partnerEmailAddressRouteNewPartners)
              .withFormUrlEncodedBody(
                ("partnerEmailAddress", "validEmail@example.com")
              )

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {

        val application = applicationBuilder(userAnswers = Some(minimalUserAnswersNewPartners)).build()

        running(application) {
          val request =
            FakeRequest(POST, partnerEmailAddressRouteNewPartners)
              .withFormUrlEncodedBody(
                ("partnerEmailAddress", "")
              )

          val boundForm =
            form.bind(Map("partnerEmailAddress" -> ""))

          val view = application.injector.instanceOf[PartnerDetailsEmailAddressView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual
            view(boundForm, newPartnersIndex1.toString, NormalMode)(request, messages(application)).toString
        }
      }

      "must save the correct value to userAnswers when submitted" in {

        val mockSessionRepository = mock[SessionRepository]
        val savedAnswersCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(minimalUserAnswersNewPartners))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, partnerEmailAddressRouteNewPartners)
              .withFormUrlEncodedBody(
                ("partnerEmailAddress", "validEmail@example.com")
              )

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          verify(mockSessionRepository).set(savedAnswersCaptor.capture())

          savedAnswersCaptor.getValue
            .get(PartnerDetailsEmailAddressPage(newPartnersIndex1))
            .value mustEqual "validEmail@example.com"
        }
      }

      "must return OK and the correct view for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = Some(minimalUserAnswersNewPartners)).build()

        running(application) {
          val request = FakeRequest(GET, partnerEmailAddressRouteNewPartners)

          val result = route(application, request).value

          val view = application.injector.instanceOf[PartnerDetailsEmailAddressView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual
            view(form, newPartnersIndex1.toString, NormalMode)(request, messages(application)).toString
        }
      }

      "must redirect to the next page for a POST if no existing data is found" in {

        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(minimalUserAnswersNewPartners))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, partnerEmailAddressRouteNewPartners)
              .withFormUrlEncodedBody(
                ("partnerEmailAddress", "validEmail@example.com")
              )

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }
    }
  }
    
  "partners" - {

    "PartnerEmailAddress Controller" - {

      "must return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(minimalUserAnswersExistingPartners)).build()

        running(application) {
          val request = FakeRequest(GET, partnerEmailAddressRouteExistingPartners)

          val result = route(application, request).value

          val view = application.injector.instanceOf[PartnerDetailsEmailAddressView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual
            view(form, businessNumber1, CheckMode)(request, messages(application)).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val userAnswers = minimalUserAnswersExistingPartners
          .set(PartnerDetailsEmailAddressPage(businessNumber1), "validEmail@example.com")
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, partnerEmailAddressRouteExistingPartners)

          val view = application.injector.instanceOf[PartnerDetailsEmailAddressView]

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual
            view(
              form.fill("validEmail@example.com"),
              businessNumber1,
              CheckMode
            )(request, messages(application)).toString
        }
      }

      "must redirect to the next page when valid data is submitted" in {

        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(minimalUserAnswersExistingPartners))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, partnerEmailAddressRouteExistingPartners)
              .withFormUrlEncodedBody(
                ("partnerEmailAddress", "validEmail@example.com")
              )

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {

        val application = applicationBuilder(userAnswers = Some(minimalUserAnswersExistingPartners)).build()

        running(application) {
          val request =
            FakeRequest(POST, partnerEmailAddressRouteExistingPartners)
              .withFormUrlEncodedBody(
                ("partnerEmailAddress", "")
              )

          val boundForm =
            form.bind(Map("partnerEmailAddress" -> ""))

          val view = application.injector.instanceOf[PartnerDetailsEmailAddressView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual
            view(boundForm, businessNumber1, CheckMode)(request, messages(application)).toString
        }
      }

      "must save the correct value to userAnswers when submitted" in {

        val mockSessionRepository = mock[SessionRepository]
        val savedAnswersCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(minimalUserAnswersExistingPartners))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, partnerEmailAddressRouteExistingPartners)
              .withFormUrlEncodedBody(
                ("partnerEmailAddress", "validEmail@example.com")
              )

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          verify(mockSessionRepository).set(savedAnswersCaptor.capture())

          savedAnswersCaptor.getValue
            .get(PartnerDetailsEmailAddressPage(businessNumber1))
            .value mustEqual "validEmail@example.com"
        }
      }

      "must return OK and the correct view for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = Some(minimalUserAnswersExistingPartners)).build()

        running(application) {
          val request = FakeRequest(GET, partnerEmailAddressRouteExistingPartners)

          val result = route(application, request).value

          val view = application.injector.instanceOf[PartnerDetailsEmailAddressView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual
            view(form, businessNumber1, CheckMode)(request, messages(application)).toString
        }
      }

      "must redirect to the next page for a POST if no existing data is found" in {

        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(minimalUserAnswersExistingPartners))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, partnerEmailAddressRouteExistingPartners)
              .withFormUrlEncodedBody(
                ("partnerEmailAddress", "validEmail@example.com")
              )

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }
    }   
  }

}
