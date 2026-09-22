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
import controllers.partnerdetails.routes.PartnerDetailsBusinessTypeController
import forms.partnerdetails.PartnerDetailsBusinessTypeFormProvider
import models.BusinessType.{Corporatebody, Soleproprietor}
import models.{BusinessType, CheckMode, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.partnerdetails.PartnerDetailsAddPartnerCompletedPage
import pages.partnerdetails.PartnerDetailsBusinessTypePage
import play.api.data.Form
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.partnerdetails.PartnerDetailsBusinessTypeView

import scala.concurrent.Future

//TODO almost done - I think navigator problem
class PartnerDetailsBusinessTypeControllerSpec extends SpecBase with MockitoSugar with PartnerDetailsHelper {

  val form: Form[BusinessType] = (new PartnerDetailsBusinessTypeFormProvider())()

  lazy val partnerDetailsBusinessTypeRouteExistingPartners: String =
    PartnerDetailsBusinessTypeController.onPageLoad(businessNumber1, CheckMode).url

  lazy val partnerDetailsBusinessTypeRouteNewPartners: String =
    PartnerDetailsBusinessTypeController.onPageLoad(newPartnersIndex1.toString, NormalMode).url

  val validUserAnswersExistingPartners: UserAnswers =
    userAnswersPartnerDetailsExistingPartners
//      .set(PartnerDetailsAddPartnerCompletedPage(newPartnersIndex1), false) // TODO for businessNumber, this should not be relevant I think
//      .success
//      .value

  val validUserAnswersNewPartners: UserAnswers =
    userAnswersPartnerDetailsNewPartners
//      .set(PartnerDetailsAddPartnerCompletedPage(newPartnersIndex1), false) // TODO for businessNumber, this should not be relevant I think
//      .success
//      .value

  "newPartners" - {
    "PartnerDetailsBusinessType Controller" - {

      "onPageLoad" - {

        "must populate the view correctly on a GET when the question has previously been answered" in {

          val application = applicationBuilder(userAnswers = Some(validUserAnswersNewPartners)).build()

          running(application) {
            val request = FakeRequest(GET, partnerDetailsBusinessTypeRouteNewPartners)

            val result = route(application, request).value

            val view = application.injector.instanceOf[PartnerDetailsBusinessTypeView]

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(form.fill(Soleproprietor), newPartnersIndex1.toString, NormalMode)(request, messages(application)).toString
          }
        }

        "must return OK and the correct view for a GET when no previous data exists" in {

          val userAnswers = validUserAnswersExistingPartners
            .remove(PartnerDetailsBusinessTypePage(newPartnersIndex1))
            .success
            .value

          val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

          running(application) {
            val request = FakeRequest(GET, partnerDetailsBusinessTypeRouteNewPartners)

            val view = application.injector.instanceOf[PartnerDetailsBusinessTypeView]

            val result = route(application, request).value

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(form, newPartnersIndex1.toString, NormalMode)(request, messages(application)).toString
          }
        }

        "must redirect to SystemError for a GET if no existing data is found" in {

          val application = applicationBuilder(userAnswers = None).build()

          running(application) {
            val request = FakeRequest(GET, partnerDetailsBusinessTypeRouteNewPartners)

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.routes.SystemErrorController.onPageLoad().url
          }
        }
      }

      "onSubmit" - {
        // TODO this test seems more relevant for newPartners, I think
        "must update UserAnswers and redirect to the next page when valid data is submitted" in {

          val mockSessionRepository = mock[SessionRepository]

          when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

          val application =
            applicationBuilder(userAnswers = Some(validUserAnswersNewPartners))
              .overrides(
                bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()

          running(application) {
            val request =
              FakeRequest(POST, PartnerDetailsBusinessTypeController.onSubmit(newPartnersIndex1.toString, NormalMode).url)
                .withFormUrlEncodedBody(("value", Corporatebody.toString))

            val result = route(application, request).value

            val expectedAnswers = validUserAnswersNewPartners
              .set(PartnerDetailsBusinessTypePage(newPartnersIndex1.toString), Corporatebody)
              .success
              .value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual onwardRoute.url
            verify(mockSessionRepository).set(expectedAnswers)
          }
        }

        "must return BAD_REQUEST and errors when invalid data is submitted" in {

          val mockSessionRepository = mock[SessionRepository]

          val application = applicationBuilder(userAnswers = Some(validUserAnswersNewPartners))
            .overrides(
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

          running(application) {
            val request =
              FakeRequest(POST, PartnerDetailsBusinessTypeController.onSubmit(newPartnersIndex1.toString, NormalMode).url)
                .withFormUrlEncodedBody(("value", ""))

            val boundForm = form.bind(Map("value" -> ""))

            val view = application.injector.instanceOf[PartnerDetailsBusinessTypeView]

            val result = route(application, request).value

            status(result) mustEqual BAD_REQUEST
            contentAsString(result) mustEqual view(boundForm, newPartnersIndex1.toString, NormalMode)(request, messages(application)).toString
            verify(mockSessionRepository, never()).set(any())
          }
        }

        "must redirect to SystemError for a POST if no existing data is found" in {

          val application = applicationBuilder(userAnswers = None).build()

          running(application) {
            val request =
              FakeRequest(POST, PartnerDetailsBusinessTypeController.onSubmit(newPartnersIndex1.toString, NormalMode).url)
                .withFormUrlEncodedBody(("value", Corporatebody.toString))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.routes.SystemErrorController.onPageLoad().url
          }
        }
      }
    }
  }

  "partners" - {

    "PartnerDetailsBusinessType Controller" - {

      "onPageLoad" - {

        "must populate the view correctly on a GET when the question has previously been answered" in {

          val application = applicationBuilder(userAnswers = Some(validUserAnswersExistingPartners)).build()

          running(application) {
            val request = FakeRequest(GET, partnerDetailsBusinessTypeRouteExistingPartners)

            val result = route(application, request).value

            val view = application.injector.instanceOf[PartnerDetailsBusinessTypeView]

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(form.fill(Soleproprietor), businessNumber1, CheckMode)(request, messages(application)).toString
          }
        }

        "must return OK and the correct view for a GET when no previous data exists" in {

          val userAnswers = validUserAnswersExistingPartners
            .remove(PartnerDetailsBusinessTypePage(businessNumber1))
            .success
            .value

          val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

          running(application) {
            val request = FakeRequest(GET, partnerDetailsBusinessTypeRouteExistingPartners)

            val view = application.injector.instanceOf[PartnerDetailsBusinessTypeView]

            val result = route(application, request).value

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(form, businessNumber1, CheckMode)(request, messages(application)).toString
          }
        }

        "must redirect to SystemError for a GET if no existing data is found" in {

          val application = applicationBuilder(userAnswers = None).build()

          running(application) {
            val request = FakeRequest(GET, partnerDetailsBusinessTypeRouteExistingPartners)

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.routes.SystemErrorController.onPageLoad().url
          }
        }
      }

      "onSubmit" - {
        // TODO this test seems more relevant for newPartners, I think
        "must update UserAnswers and redirect to the next page when valid data is submitted" in {

          val mockSessionRepository = mock[SessionRepository]

          when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

          val application =
            applicationBuilder(userAnswers = Some(validUserAnswersExistingPartners))
              .overrides(
                bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()

          running(application) {
            val request =
              FakeRequest(POST, PartnerDetailsBusinessTypeController.onSubmit(businessNumber1, CheckMode).url)
                .withFormUrlEncodedBody(("value", Corporatebody.toString))

            val result = route(application, request).value

            val expectedAnswers = validUserAnswersExistingPartners
              .set(PartnerDetailsBusinessTypePage(businessNumber1), Corporatebody)
              .success
              .value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual onwardRoute.url
            verify(mockSessionRepository).set(expectedAnswers)
          }
        }

        "must return BAD_REQUEST and errors when invalid data is submitted" in {

          val mockSessionRepository = mock[SessionRepository]

          val application = applicationBuilder(userAnswers = Some(validUserAnswersExistingPartners))
            .overrides(
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

          running(application) {
            val request =
              FakeRequest(POST, PartnerDetailsBusinessTypeController.onSubmit(businessNumber1, CheckMode).url)
                .withFormUrlEncodedBody(("value", ""))

            val boundForm = form.bind(Map("value" -> ""))

            val view = application.injector.instanceOf[PartnerDetailsBusinessTypeView]

            val result = route(application, request).value

            status(result) mustEqual BAD_REQUEST
            contentAsString(result) mustEqual view(boundForm, businessNumber1, CheckMode)(request, messages(application)).toString
            verify(mockSessionRepository, never()).set(any())
          }
        }

        "must redirect to SystemError for a POST if no existing data is found" in {

          val application = applicationBuilder(userAnswers = None).build()

          running(application) {
            val request =
              FakeRequest(POST, PartnerDetailsBusinessTypeController.onSubmit(businessNumber1, CheckMode).url)
                .withFormUrlEncodedBody(("value", Corporatebody.toString))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.routes.SystemErrorController.onPageLoad().url
          }
        }
      }
    }

  }
}
