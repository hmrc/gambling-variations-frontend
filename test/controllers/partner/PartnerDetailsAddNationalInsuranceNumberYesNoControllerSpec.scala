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
import forms.partner.PartnerDetailsAddNationalInsuranceNumberYesNoFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.partner.PartnerDetailsAddNationalInsuranceNumberYesNoPage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.partner.PartnerDetailsAddNationalInsuranceNumberYesNoView

import scala.concurrent.Future

class PartnerDetailsAddNationalInsuranceNumberYesNoControllerSpec extends SpecBase with MockitoSugar with PartnerDetailsHelper {

  val formProvider = new PartnerDetailsAddNationalInsuranceNumberYesNoFormProvider()
  val form: Form[Boolean] = formProvider()

  lazy val addNinoRoute: String =
    controllers.partner.routes.PartnerDetailsAddNationalInsuranceNumberYesNoController.onPageLoad().url

  val validUserAnswers: UserAnswers = UserAnswers(mgdRegNumber, cleanedData())

  "PartnerDetailsAddNationalInsuranceNumberYesNo Controller" - {

    "onPageLoad" - {

      "must return OK and the correct view for a GET when no previous data exists" in {

        val application = applicationBuilder(userAnswers = Some(validUserAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, addNinoRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[PartnerDetailsAddNationalInsuranceNumberYesNoView]

          status(result) mustBe OK
          contentAsString(result) mustBe view(form, NormalMode)(request, messages(application)).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val userAnswers = validUserAnswers
          .set(PartnerDetailsAddNationalInsuranceNumberYesNoPage(index), true)
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, addNinoRoute)

          val view = application.injector.instanceOf[PartnerDetailsAddNationalInsuranceNumberYesNoView]

          val result = route(application, request).value

          status(result) mustBe OK
          contentAsString(result) mustBe view(form.fill(true), NormalMode)(request, messages(application)).toString
        }
      }

      "must redirect to System Error Page for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, addNinoRoute)

          val result = route(application, request).value

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe controllers.routes.SystemErrorController.onPageLoad().url
        }
      }
    }

    "onSubmit" - {

      def onwardRoute: Call = Call("GET", "/foo")

      "must update UserAnswers and redirect to the next page when valid data is submitted" in {

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
            FakeRequest(POST, addNinoRoute)
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          val expectedAnswers = validUserAnswers
            .set(PartnerDetailsAddNationalInsuranceNumberYesNoPage(index), true)
            .success
            .value

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe onwardRoute.url
          verify(mockSessionRepository).set(expectedAnswers)
        }
      }

      "must return a Bad Request and stay on the page with errors when submitted with no selection" in {

        val mockSessionRepository = mock[SessionRepository]

        val application = applicationBuilder(userAnswers = Some(validUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request =
            FakeRequest(POST, addNinoRoute)
              .withFormUrlEncodedBody(("value", ""))

          val boundForm = form.bind(Map("value" -> ""))

          val view = application.injector.instanceOf[PartnerDetailsAddNationalInsuranceNumberYesNoView]

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
            FakeRequest(POST, addNinoRoute)
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe controllers.routes.SystemErrorController.onPageLoad().url
        }
      }
    }
  }
}
