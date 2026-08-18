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
import controllers.routes.JourneyRecoveryController
import forms.partner.PartnerDetailsRemoveEmailAddressYesNoFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import pages.partner.PartnerDetailsRemoveEmailAddressYesNoPage
import play.api.data.Form
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.partner.PartnerDetailsRemoveEmailAddressYesNoView

import scala.concurrent.Future

class PartnerDetailsRemoveEmailAddressYesNoControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute: Call = Call("GET", "/foo")

  private val formProvider = new PartnerDetailsRemoveEmailAddressYesNoFormProvider()
  val form: Form[Boolean] = formProvider()

  // TODO: This index is hardcoded but it should come from the Partner Details list selection
  private val index: Int = 0

  private val testEmail = "john.doe@example.com"
  private val mgdRegNumber = "XGM00000001761"
  lazy val partnerDetailsRemoveEmailAddressYesNoRoute: String =
    controllers.partner.routes.PartnerDetailsRemoveEmailAddressYesNoController.onPageLoad().url

  private def cleanedData(emailAddr: Option[String]) = Json.obj(
    "partners" -> Json.arr(
      Json.obj(
        "partnerDetailsMgdRegNumber" -> mgdRegNumber,
        "partnerDetailsBusinessName" -> "Partner1",
        "partnerDetailsCorrespondenceDetailsSection" -> Json.obj(
          "mgdRegNumber" -> mgdRegNumber,
          "correspondenceAddress" -> Json.obj(
            "address1" -> "Flat 1",
            "address2" -> "10 Market Road",
            "address3" -> "Felling",
            "address4" -> "Gateshead",
            "postcode" -> "NE8 1ZZ",
            "country"  -> "UK"
          ),
          "contactNumber" -> Json.obj(
            "phoneNumber"       -> "0798765",
            "mobilePhoneNumber" -> "7093434765"
          ),
          "faxNumber" -> "02071234568",
          "emailAddr" -> emailAddr
        )
      )
    )
  )

  "PartnerDetailsRemoveEmailAddressYesNo Controller" - {

    "onPageLoad" - {

      "must return OK and the correct view for a GET when email address exists in UserAnswers" in {
        val userAnswers = UserAnswers(mgdRegNumber, cleanedData(Some(testEmail)))

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, partnerDetailsRemoveEmailAddressYesNoRoute)
          val result = route(application, request).value
          val view = application.injector.instanceOf[PartnerDetailsRemoveEmailAddressYesNoView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, NormalMode, testEmail)(request, messages(application)).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {
        val baseAnswers = UserAnswers(mgdRegNumber, cleanedData(Some(testEmail)))

        val userAnswers = baseAnswers
          .set(PartnerDetailsRemoveEmailAddressYesNoPage(index), true)
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, partnerDetailsRemoveEmailAddressYesNoRoute)
          val result = route(application, request).value
          val view = application.injector.instanceOf[PartnerDetailsRemoveEmailAddressYesNoView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form.fill(true), NormalMode, testEmail)(request, messages(application)).toString
        }
      }

      "must redirect to JourneyRecovery on a GET when correspondence details exist but emailAddr is None" in {
        val userAnswers = UserAnswers(mgdRegNumber, cleanedData(None))

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, partnerDetailsRemoveEmailAddressYesNoRoute)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual JourneyRecoveryController.onPageLoad().url

        }
      }

      "must redirect to JourneyRecovery when no user answers are found" in {
        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, partnerDetailsRemoveEmailAddressYesNoRoute)
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.SystemErrorController.onPageLoad().url

        }
      }
    }

    "onSubmit" - {

      "must remove the email address and redirect to next page when 'Yes' (true) is submitted" in {
        val mockSessionRepository = mock[SessionRepository]
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val userAnswers = UserAnswers(mgdRegNumber, cleanedData(Some(testEmail)))

        val application =
          applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, routes.PartnerDetailsRemoveEmailAddressYesNoController.onSubmit().url)
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      "must retain the email address and redirect when 'No' (false) is submitted" in {
        val mockSessionRepository = mock[SessionRepository]
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val userAnswers = UserAnswers(mgdRegNumber, cleanedData(Some(testEmail)))

        val application =
          applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, routes.PartnerDetailsRemoveEmailAddressYesNoController.onSubmit().url)
              .withFormUrlEncodedBody(("value", "false"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      "must return BAD_REQUEST and errors when invalid data is submitted" in {
        val userAnswers = UserAnswers(mgdRegNumber, cleanedData(Some(testEmail)))

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request =
            FakeRequest(POST, routes.PartnerDetailsRemoveEmailAddressYesNoController.onSubmit().url)
              .withFormUrlEncodedBody(("value", ""))

          val boundForm = form.bind(Map("value" -> ""))
          val view = application.injector.instanceOf[PartnerDetailsRemoveEmailAddressYesNoView]
          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, NormalMode, testEmail)(request, messages(application)).toString
        }
      }

      "must redirect to 'there is a problem' error page when submitting 'Yes' (true) but correspondence details section is missing" in {
        val userAnswers = emptyUserAnswers

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request =
            FakeRequest(POST, routes.PartnerDetailsRemoveEmailAddressYesNoController.onSubmit().url)
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual "/gambling-variations/there-is-a-problem-with-the-service"
        }
      }
    }
  }
}
