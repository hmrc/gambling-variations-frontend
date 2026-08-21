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
import forms.partner.PartnerDetailsRemoveNationalInsuranceNumberYesNoFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.partner.PartnerDetailsRemoveNationalInsuranceNumberYesNoPage
import pages.partnerdetails.PartnerDetailsNinoPage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.partner.PartnerDetailsRemoveNationalInsuranceNumberYesNoView

import scala.concurrent.Future

class PartnerDetailsRemoveNationalInsuranceNumberYesNoControllerSpec extends SpecBase with MockitoSugar with PartnerDetailsHelper {

  val formProvider = new PartnerDetailsRemoveNationalInsuranceNumberYesNoFormProvider()
  val form: Form[Boolean] = formProvider()

  lazy val removeNinoRoute: String =
    controllers.partner.routes.PartnerDetailsRemoveNationalInsuranceNumberYesNoController.onPageLoad().url

  val validUserAnswers: UserAnswers = UserAnswers(mgdRegNumber, cleanedData(nino = Some(testNino)))

  "PartnerDetailsRemoveNationalInsuranceNumberYesNo Controller" - {

    "onPageLoad" - {

      "must return OK and the correct view for a GET when no previous answer exists" in {

        val application = applicationBuilder(userAnswers = Some(validUserAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, removeNinoRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[PartnerDetailsRemoveNationalInsuranceNumberYesNoView]

          status(result) mustBe OK
          contentAsString(result) mustBe view(form, NormalMode, testNino)(request, messages(application)).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val userAnswers = validUserAnswers
          .set(PartnerDetailsRemoveNationalInsuranceNumberYesNoPage(index), true)
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, removeNinoRoute)

          val view = application.injector.instanceOf[PartnerDetailsRemoveNationalInsuranceNumberYesNoView]

          val result = route(application, request).value

          status(result) mustBe OK
          contentAsString(result) mustBe view(form.fill(true), NormalMode, testNino)(request, messages(application)).toString
        }
      }

      "must redirect to JourneyRecovery for a GET if PartnerDetailsNinoPage is missing" in {

        val userAnswersNoNino = emptyUserAnswers

        val application = applicationBuilder(userAnswers = Some(userAnswersNoNino)).build()

        running(application) {
          val request = FakeRequest(GET, removeNinoRoute)

          val result = route(application, request).value

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe controllers.routes.SystemErrorController.onPageLoad().url
        }
      }

      "must redirect to JourneyRecovery for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, removeNinoRoute)

          val result = route(application, request).value

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe controllers.routes.SystemErrorController.onPageLoad().url
        }
      }
    }

    "onSubmit" - {

      def onwardRoute: Call = Call("GET", "/foo")

      "must remove NINO, update UserAnswers and redirect when 'Yes' (true) is submitted" in {

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
            FakeRequest(POST, removeNinoRoute)
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          val expectedAnswers = validUserAnswers
            .remove(PartnerDetailsNinoPage(index))
            .success
            .value
            .set(PartnerDetailsRemoveNationalInsuranceNumberYesNoPage(index), true)
            .success
            .value

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe onwardRoute.url
          verify(mockSessionRepository).set(expectedAnswers)
        }
      }

      "must NOT remove NINO, update UserAnswers and redirect when 'No' (false) is submitted" in {

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
            FakeRequest(POST, removeNinoRoute)
              .withFormUrlEncodedBody(("value", "false"))

          val result = route(application, request).value

          val expectedAnswers = validUserAnswers
            .set(PartnerDetailsRemoveNationalInsuranceNumberYesNoPage(index), false)
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
            FakeRequest(POST, removeNinoRoute)
              .withFormUrlEncodedBody(("value", ""))

          val boundForm = form.bind(Map("value" -> ""))

          val view = application.injector.instanceOf[PartnerDetailsRemoveNationalInsuranceNumberYesNoView]

          val result = route(application, request).value

          status(result) mustBe BAD_REQUEST
          contentAsString(result) mustBe view(boundForm, NormalMode, testNino)(request, messages(application)).toString
          verify(mockSessionRepository, never()).set(any())
        }
      }

      "must return Bad Request and stay on the same page with form errors when submitted with no selection" in {

        val mockSessionRepository = mock[SessionRepository]

        val application = applicationBuilder(userAnswers = Some(validUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request =
            FakeRequest(POST, removeNinoRoute)
              .withFormUrlEncodedBody(("value", ""))

          val boundForm = form.bind(Map("value" -> ""))

          val view = application.injector.instanceOf[PartnerDetailsRemoveNationalInsuranceNumberYesNoView]

          val result = route(application, request).value

          status(result) mustBe BAD_REQUEST
          contentAsString(result) mustBe view(boundForm, NormalMode, testNino)(request, messages(application)).toString
          verify(mockSessionRepository, never()).set(any())
        }
      }
    }
  }
}
