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

package controllers.licencespremises

import base.SpecBase
import forms.licencespremises.LicenceDetailsLandlordLicenceYesNoFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.licencespremises.LicenceDetailsLandlordLicenceYesNoPage
import play.api.data.Form
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.licencespremises.LicenceDetailsLandlordLicenceYesNoView

import scala.concurrent.Future

class LicenceDetailsLandlordLicenceYesNoControllerSpec extends SpecBase with MockitoSugar {

  val formProvider = new LicenceDetailsLandlordLicenceYesNoFormProvider()
  val form: Form[Boolean] = formProvider()

  lazy val onwardRoute: Call = Call("GET", "/foo")

  lazy val addRoute: String =
    controllers.licencespremises.routes.LicenceDetailsLandlordLicenceYesNoController.onPageLoad().url

  val noAnswers =
    UserAnswers(
      userAnswersId,
      Json.obj("licencesPremisesSection" -> Json.obj("mgdRegNum" -> userAnswersId))
    )

  "LicenceDetailsLandlordLicenceYesNoController Controller" - {

    "onPageLoad" - {

      "must return OK and the correct view for a GET when no previous data exists" in {

        val application = applicationBuilder(userAnswers = Some(noAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, addRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[LicenceDetailsLandlordLicenceYesNoView]

          status(result) mustBe OK
          contentAsString(result) mustBe view(form, NormalMode)(request, messages(application)).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val userAnswers = noAnswers
          .set(LicenceDetailsLandlordLicenceYesNoPage, true)
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, addRoute)

          val view = application.injector.instanceOf[LicenceDetailsLandlordLicenceYesNoView]

          val result = route(application, request).value

          status(result) mustBe OK
          contentAsString(result) mustBe view(form.fill(true), NormalMode)(request, messages(application)).toString
        }
      }

      "must redirect to System Error Page for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, addRoute)

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
          applicationBuilder(userAnswers = Some(noAnswers))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, addRoute)
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          val expectedAnswers = noAnswers
            .set(LicenceDetailsLandlordLicenceYesNoPage, true)
            .success
            .value

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe onwardRoute.url
          verify(mockSessionRepository).set(expectedAnswers)
        }
      }

      "must return a Bad Request and stay on the page with errors when submitted with no selection" in {

        val mockSessionRepository = mock[SessionRepository]

        val application = applicationBuilder(userAnswers = Some(noAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request =
            FakeRequest(POST, addRoute)
              .withFormUrlEncodedBody(("value", ""))

          val boundForm = form.bind(Map("value" -> ""))

          val view = application.injector.instanceOf[LicenceDetailsLandlordLicenceYesNoView]

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
            FakeRequest(POST, addRoute)
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe controllers.routes.SystemErrorController.onPageLoad().url
        }
      }
    }
  }
}
