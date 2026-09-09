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
import controllers.partner.routes.PartnerDetailsAddNationalInsuranceNumberController
import forms.partner.PartnerDetailsAddNationalInsuranceNumberFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.partnerdetails.PartnerDetailsNinoPage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.partner.PartnerDetailsAddNationalInsuranceNumberView

import scala.concurrent.Future

class PartnerDetailsAddNationalInsuranceNumberControllerSpec extends SpecBase with MockitoSugar with PartnerDetailsHelper {

  private val formProvider = new PartnerDetailsAddNationalInsuranceNumberFormProvider()
  val form: Form[String] = formProvider()

  lazy val ninoRoute: String =
    PartnerDetailsAddNationalInsuranceNumberController.onPageLoad().url

  val validUserAnswers: UserAnswers = UserAnswers(mgdRegNumber, cleanedData())

  val validNino9Chars = "AA123456A"
  val validNino8Chars = "AA123456"

  "PartnerDetailsAddNationalInsuranceNumber Controller" - {

    "onPageLoad" - {

      "must return OK and the correct view for a GET when no previous data exists" in {

        val application = applicationBuilder(userAnswers = Some(validUserAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, ninoRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[PartnerDetailsAddNationalInsuranceNumberView]

          status(result) mustBe OK
          contentAsString(result) mustBe view(form, NormalMode)(request, messages(application)).toString
        }
      }

      "must populate the view correctly on a GET when a 9-character NINO has previously been answered" in {

        val userAnswers = validUserAnswers
          .set(PartnerDetailsNinoPage(index), validNino9Chars)
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, ninoRoute)

          val view = application.injector.instanceOf[PartnerDetailsAddNationalInsuranceNumberView]

          val result = route(application, request).value

          status(result) mustBe OK
          contentAsString(result) mustBe view(form.fill(validNino9Chars), NormalMode)(request, messages(application)).toString
        }
      }

      "must strip the trailing underscore when populating the view for an 8-character NINO" in {

        val storedNinoWithUnderscore = s"${validNino8Chars}_"

        val userAnswers = validUserAnswers
          .set(PartnerDetailsNinoPage(index), storedNinoWithUnderscore)
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, ninoRoute)

          val view = application.injector.instanceOf[PartnerDetailsAddNationalInsuranceNumberView]

          val result = route(application, request).value

          status(result) mustBe OK
          contentAsString(result) mustBe view(form.fill(validNino8Chars), NormalMode)(request, messages(application)).toString
        }
      }

      "must redirect to System Error Page for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, ninoRoute)

          val result = route(application, request).value

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe controllers.routes.SystemErrorController.onPageLoad().url
        }
      }
    }

    "onSubmit" - {

      "must save unmodified 9-character NINO to UserAnswers and redirect when valid 9-char NINO is submitted" in {

        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        val application =
          applicationBuilder(userAnswers = Some(validUserAnswers))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, PartnerDetailsAddNationalInsuranceNumberController.onSubmit().url)
              .withFormUrlEncodedBody(("value", validNino9Chars))

          val result = route(application, request).value

          val expectedAnswers = validUserAnswers
            .set(PartnerDetailsNinoPage(index), validNino9Chars)
            .success
            .value

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe onwardRoute.url
          verify(mockSessionRepository).set(expectedAnswers)
        }
      }

      "must append an underscore to an 8-character NINO before saving to UserAnswers and redirect" in {

        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        val application =
          applicationBuilder(userAnswers = Some(validUserAnswers))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, PartnerDetailsAddNationalInsuranceNumberController.onSubmit().url)
              .withFormUrlEncodedBody(("value", validNino8Chars))

          val result = route(application, request).value

          val expectedAnswers = validUserAnswers
            .set(PartnerDetailsNinoPage(index), s"${validNino8Chars}_")
            .success
            .value

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe onwardRoute.url
          verify(mockSessionRepository).set(expectedAnswers)
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {

        val mockSessionRepository = mock[SessionRepository]

        val application = applicationBuilder(userAnswers = Some(validUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request =
            FakeRequest(POST, PartnerDetailsAddNationalInsuranceNumberController.onSubmit().url)
              .withFormUrlEncodedBody(("value", "12QQ3456C"))

          val boundForm = form.bind(Map("value" -> "12QQ3456C"))

          val view = application.injector.instanceOf[PartnerDetailsAddNationalInsuranceNumberView]

          val result = route(application, request).value

          status(result) mustBe BAD_REQUEST
          contentAsString(result) mustBe view(boundForm, NormalMode)(request, messages(application)).toString
          verify(mockSessionRepository, never()).set(any())
        }
      }

      "must redirect to System Error Page for a POST if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request =
            FakeRequest(POST, PartnerDetailsAddNationalInsuranceNumberController.onSubmit().url)
              .withFormUrlEncodedBody(("value", validNino9Chars))

          val result = route(application, request).value

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe controllers.routes.SystemErrorController.onPageLoad().url
        }
      }
    }
  }
}
