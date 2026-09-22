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
import controllers.partnerdetails.routes.PartnerDetailsAddUTRController
import forms.partnerdetails.PartnerDetailsAddUTRFormProvider
import models.{CheckMode, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.partnerdetails.PartnerDetailsUtrPage
import play.api.data.Form
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.partnerdetails.PartnerDetailsAddUTRView

import scala.concurrent.Future

//TODO I think DONE
class PartnerDetailsAddUTRControllerSpec extends SpecBase with MockitoSugar with PartnerDetailsHelper {

  private val formProvider = new PartnerDetailsAddUTRFormProvider()
  val form: Form[String] = formProvider()

  lazy val partnerDetailsAddUTRRouteExistingPartners: String =
    PartnerDetailsAddUTRController.onPageLoad(businessNumber1, CheckMode).url

  lazy val partnerDetailsAddUTRRouteNewPartners: String =
    PartnerDetailsAddUTRController.onPageLoad(newPartnersIndex1.toString, NormalMode).url

  val userAnswersExistingPartners: UserAnswers = userAnswersPartnerDetailsExistingPartners
  val userAnswersNewPartners: UserAnswers = userAnswersPartnerDetailsNewPartners

  "newPartners" - {
    "PartnerDetailsAddUTR Controller" - {

      "onPageLoad" - {

        "must return OK and the correct view for a GET when no previous data exists" in {

          val application = applicationBuilder(userAnswers = Some(userAnswersNewPartners)).build()

          running(application) {
            val request = FakeRequest(GET, partnerDetailsAddUTRRouteNewPartners)

            val result = route(application, request).value

            val view = application.injector.instanceOf[PartnerDetailsAddUTRView]

            status(result) mustBe OK
            contentAsString(result) mustBe view(form, newPartnersIndex1.toString, NormalMode)(request, messages(application)).toString
          }
        }

        "must populate the view correctly on a GET when the question has previously been answered" in {

          val userAnswers = userAnswersNewPartners
            .set(PartnerDetailsUtrPage(newPartnersIndex1), testUtr)
            .success
            .value

          val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

          running(application) {
            val request = FakeRequest(GET, partnerDetailsAddUTRRouteNewPartners)

            val view = application.injector.instanceOf[PartnerDetailsAddUTRView]

            val result = route(application, request).value

            status(result) mustBe OK
            contentAsString(result) mustBe view(form.fill(testUtr), newPartnersIndex1.toString, NormalMode)(request, messages(application)).toString
          }
        }

        "must redirect to System Error Page for a GET if no existing data is found" in {

          val application = applicationBuilder(userAnswers = None).build()

          running(application) {
            val request = FakeRequest(GET, partnerDetailsAddUTRRouteNewPartners)

            val result = route(application, request).value

            status(result) mustBe SEE_OTHER
            redirectLocation(result).value mustBe controllers.routes.SystemErrorController.onPageLoad().url
          }
        }
      }

      "onSubmit" - {

        "must update UserAnswers and redirect to the next page when valid data is submitted" in {

          val mockSessionRepository = mock[SessionRepository]

          when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

          val application =
            applicationBuilder(userAnswers = Some(userAnswersNewPartners))
              .overrides(
                bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()

          running(application) {
            val request =
              FakeRequest(POST, PartnerDetailsAddUTRController.onSubmit(newPartnersIndex1.toString, NormalMode).url)
                .withFormUrlEncodedBody(("value", testUtr))

            val result = route(application, request).value

            val expectedAnswers = userAnswersNewPartners
              .set(PartnerDetailsUtrPage(newPartnersIndex1), testUtr)
              .success
              .value

            status(result) mustBe SEE_OTHER
            redirectLocation(result).value mustBe onwardRoute.url
            verify(mockSessionRepository).set(expectedAnswers)
          }
        }

        "must return a Bad Request and errors when invalid data is submitted" in {

          val mockSessionRepository = mock[SessionRepository]

          val application = applicationBuilder(userAnswers = Some(userAnswersNewPartners))
            .overrides(
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

          running(application) {
            val request =
              FakeRequest(POST, PartnerDetailsAddUTRController.onSubmit(newPartnersIndex1.toString, NormalMode).url)
                .withFormUrlEncodedBody(("value", ""))

            val boundForm = form.bind(Map("value" -> ""))

            val view = application.injector.instanceOf[PartnerDetailsAddUTRView]

            val result = route(application, request).value

            status(result) mustBe BAD_REQUEST
            contentAsString(result) mustBe view(boundForm, newPartnersIndex1.toString, NormalMode)(request, messages(application)).toString
            verify(mockSessionRepository, never()).set(any())
          }
        }

        "must redirect to System Error Page for a POST if no existing data is found" in {

          val application = applicationBuilder(userAnswers = None).build()

          running(application) {
            val request =
              FakeRequest(POST, PartnerDetailsAddUTRController.onSubmit(newPartnersIndex1.toString, NormalMode).url)
                .withFormUrlEncodedBody(("value", testUtr))

            val result = route(application, request).value

            status(result) mustBe SEE_OTHER
            redirectLocation(result).value mustBe controllers.routes.SystemErrorController.onPageLoad().url
          }
        }
      }
    }
  }

  "partners" - {

    "PartnerDetailsAddUTR Controller" - {

      "onPageLoad" - {

        "must return OK and the correct view for a GET when no previous data exists" in {

          val application = applicationBuilder(userAnswers = Some(userAnswersExistingPartners)).build()

          running(application) {
            val request = FakeRequest(GET, partnerDetailsAddUTRRouteExistingPartners)

            val result = route(application, request).value

            val view = application.injector.instanceOf[PartnerDetailsAddUTRView]

            status(result) mustBe OK
            contentAsString(result) mustBe view(form, businessNumber1, CheckMode)(request, messages(application)).toString
          }
        }

        "must populate the view correctly on a GET when the question has previously been answered" in {

          val userAnswers = userAnswersExistingPartners
            .set(PartnerDetailsUtrPage(businessNumber1), testUtr)
            .success
            .value

          val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

          running(application) {
            val request = FakeRequest(GET, partnerDetailsAddUTRRouteExistingPartners)

            val view = application.injector.instanceOf[PartnerDetailsAddUTRView]

            val result = route(application, request).value

            status(result) mustBe OK
            contentAsString(result) mustBe view(form.fill(testUtr), businessNumber1, CheckMode)(request, messages(application)).toString
          }
        }

        "must redirect to System Error Page for a GET if no existing data is found" in {

          val application = applicationBuilder(userAnswers = None).build()

          running(application) {
            val request = FakeRequest(GET, partnerDetailsAddUTRRouteExistingPartners)

            val result = route(application, request).value

            status(result) mustBe SEE_OTHER
            redirectLocation(result).value mustBe controllers.routes.SystemErrorController.onPageLoad().url
          }
        }
      }

      "onSubmit" - {

        "must update UserAnswers and redirect to the next page when valid data is submitted" in {

          val mockSessionRepository = mock[SessionRepository]

          when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

          val application =
            applicationBuilder(userAnswers = Some(userAnswersExistingPartners))
              .overrides(
                bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()

          running(application) {
            val request =
              FakeRequest(POST, PartnerDetailsAddUTRController.onSubmit(businessNumber1, CheckMode).url)
                .withFormUrlEncodedBody(("value", testUtr))

            val result = route(application, request).value

            val expectedAnswers = userAnswersExistingPartners
              .set(PartnerDetailsUtrPage(businessNumber1), testUtr)
              .success
              .value

            status(result) mustBe SEE_OTHER
            redirectLocation(result).value mustBe onwardRoute.url
            verify(mockSessionRepository).set(expectedAnswers)
          }
        }

        "must return a Bad Request and errors when invalid data is submitted" in {

          val mockSessionRepository = mock[SessionRepository]

          val application = applicationBuilder(userAnswers = Some(userAnswersExistingPartners))
            .overrides(
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

          running(application) {
            val request =
              FakeRequest(POST, PartnerDetailsAddUTRController.onSubmit(businessNumber1, CheckMode).url)
                .withFormUrlEncodedBody(("value", ""))

            val boundForm = form.bind(Map("value" -> ""))

            val view = application.injector.instanceOf[PartnerDetailsAddUTRView]

            val result = route(application, request).value

            status(result) mustBe BAD_REQUEST
            contentAsString(result) mustBe view(boundForm, businessNumber1, CheckMode)(request, messages(application)).toString
            verify(mockSessionRepository, never()).set(any())
          }
        }

        "must redirect to System Error Page for a POST if no existing data is found" in {

          val application = applicationBuilder(userAnswers = None).build()

          running(application) {
            val request =
              FakeRequest(POST, PartnerDetailsAddUTRController.onSubmit(businessNumber1, CheckMode).url)
                .withFormUrlEncodedBody(("value", testUtr))

            val result = route(application, request).value

            status(result) mustBe SEE_OTHER
            redirectLocation(result).value mustBe controllers.routes.SystemErrorController.onPageLoad().url
          }
        }
      }
    }

  }
}
