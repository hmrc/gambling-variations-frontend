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
import forms.partner.PartnerDetailsAddNationalInsuranceNumberFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.partnerdetails.PartnerDetailsNinoPage
import play.api.data.Form
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.partner.PartnerDetailsAddNationalInsuranceNumberView

import scala.concurrent.Future

class PartnerDetailsAddNationalInsuranceNumberControllerSpec extends SpecBase with MockitoSugar with PartnerDetailsHelper {

  val form: Form[String] = (new PartnerDetailsAddNationalInsuranceNumberFormProvider())()

  lazy val partnerDetailsAddNationalInsuranceNumberRoute: String =
    controllers.partner.routes.PartnerDetailsAddNationalInsuranceNumberController.onPageLoad().url

  def validUserAnswers(nino: Option[String] = None): UserAnswers = UserAnswers(mgdRegNumber, cleanedData(nino = nino))
  val userAnswersNoNino: UserAnswers = validUserAnswers()
  val userAnswersWithNino: UserAnswers = validUserAnswers(Some(testNino))

  "PartnerDetailsAddNationalInsuranceNumber Controller" - {

    "onPageLoad" - {

      "must return OK and the correct view for a GET when no previous answer exists" in {

        val application = applicationBuilder(userAnswers = Some(userAnswersNoNino)).build()

        running(application) {
          val request = FakeRequest(GET, partnerDetailsAddNationalInsuranceNumberRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[PartnerDetailsAddNationalInsuranceNumberView]

          status(result) mustBe OK
          contentAsString(result) mustBe view(form, NormalMode)(request, messages(application)).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val application = applicationBuilder(userAnswers = Some(userAnswersWithNino)).build()

        running(application) {
          val request = FakeRequest(GET, partnerDetailsAddNationalInsuranceNumberRoute)

          val view = application.injector.instanceOf[PartnerDetailsAddNationalInsuranceNumberView]

          val result = route(application, request).value

          status(result) mustBe OK
          contentAsString(result) mustBe view(form.fill(testNino), NormalMode)(request, messages(application)).toString
        }
      }

      "must redirect to SystemErrorController for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, partnerDetailsAddNationalInsuranceNumberRoute)

          val result = route(application, request).value

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe routes.SystemErrorController.onPageLoad().url
        }
      }
    }

    "onSubmit" - {

      "must update UserAnswers for both pages and redirect when valid data is submitted" in {

        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        val application =
          applicationBuilder(userAnswers = Some(userAnswersNoNino))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, partnerDetailsAddNationalInsuranceNumberRoute)
              .withFormUrlEncodedBody(("value", testNino))

          val result = route(application, request).value

          val expectedAnswers = userAnswersNoNino
            .set(PartnerDetailsNinoPage(index), testNino)
            .success
            .value
            .set(PartnerDetailsNinoPage(index), testNino)
            .success
            .value

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe onwardRoute.url
          verify(mockSessionRepository).set(expectedAnswers)
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {

        val mockSessionRepository = mock[SessionRepository]

        val application = applicationBuilder(userAnswers = Some(userAnswersNoNino))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request =
            FakeRequest(POST, partnerDetailsAddNationalInsuranceNumberRoute)
              .withFormUrlEncodedBody(("value", ""))

          val boundForm = form.bind(Map("value" -> ""))

          val view = application.injector.instanceOf[PartnerDetailsAddNationalInsuranceNumberView]

          val result = route(application, request).value

          status(result) mustBe BAD_REQUEST
          contentAsString(result) mustBe view(boundForm, NormalMode)(request, messages(application)).toString
          verify(mockSessionRepository, never()).set(any())
        }
      }

      "must redirect to SystemErrorController for a POST if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request =
            FakeRequest(POST, partnerDetailsAddNationalInsuranceNumberRoute)
              .withFormUrlEncodedBody(("value", testNino))

          val result = route(application, request).value

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe routes.SystemErrorController.onPageLoad().url
        }
      }
    }
  }
}
