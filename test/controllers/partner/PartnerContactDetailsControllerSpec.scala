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
import forms.ContactNumberFormProvider
import models.{ContactNumber, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.partner.PartnerDetailsAddPartnerCompletedPage
import play.api.data.Form
import play.api.inject.bind
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.partner.PartnerContactDetailsView

import scala.concurrent.Future

class PartnerContactDetailsControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new ContactNumberFormProvider()

  val form: Form[ContactNumber] = formProvider("partnerContactDetails")

  lazy val partnerContactDetailsRoute: String = controllers.partner.routes.PartnerContactDetailsController.onPageLoad().url

  val userAnswers: UserAnswers =
    UserAnswers(
      userAnswersId,
      Json.obj(
        "partners" -> Json.arr(
          Json.obj(
            "partnerDetailsMgdRegNumber" -> "XWM00000001762",
            "partnerDetailsCorrespondenceDetailsSection" -> Json.obj(
              "contactNumber" -> Json.obj(
                "phoneNumber"       -> "123456789",
                "mobilePhoneNumber" -> "123456789"
              )
            )
          )
        )
      )
    ).set(PartnerDetailsAddPartnerCompletedPage, false).success.value

  override val emptyUserAnswers = UserAnswers(
    userAnswersId,
    Json.obj(
      "partners" -> Json.arr(
        Json.obj(
          "partnerDetailsMgdRegNumber" -> "XWM00000001762"
        )
      )
    )
  ).set(PartnerDetailsAddPartnerCompletedPage, false).success.value

  "PartnerContactDetails Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, partnerContactDetailsRoute)

        val view = application.injector.instanceOf[PartnerContactDetailsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, partnerContactDetailsRoute)

        val view = application.injector.instanceOf[PartnerContactDetailsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(ContactNumber(Some("123456789"), Some("123456789"))), NormalMode)(request,
                                                                                                                           messages(application)
                                                                                                                          ).toString
      }
    }

    "must redirect to the next page when one number is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, partnerContactDetailsRoute)
            .withFormUrlEncodedBody(("phoneNumber", "123456789"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to the next page when both numbers are submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, partnerContactDetailsRoute)
            .withFormUrlEncodedBody(("phoneNumber", "123456789"), ("mobileNumber", "123456789"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, partnerContactDetailsRoute)
            .withFormUrlEncodedBody(("phoneNumber", "invalid value"))

        val boundForm = form.bind(Map("phoneNumber" -> "invalid value"))

        val view = application.injector.instanceOf[PartnerContactDetailsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must return a Bad Request and errors when valid and invalid data are submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, partnerContactDetailsRoute)
            .withFormUrlEncodedBody(("phoneNumber", "invalid value"), ("mobileNumber", "123456789"))

        val boundForm = form.bind(Map("phoneNumber" -> "invalid value", "mobileNumber" -> "123456789"))

        val view = application.injector.instanceOf[PartnerContactDetailsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must return a Bad Request and errors when no data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, partnerContactDetailsRoute)
            .withFormUrlEncodedBody(("phoneNumber", ""), ("mobileNumber", ""))

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) must include(messages(application)("partnerContactDetails.error.phoneNumber.missing"))
      }
    }

    "must return See Other when no data is present" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, partnerContactDetailsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
      }
    }

  }
}
