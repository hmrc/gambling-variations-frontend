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
import controllers.partner.routes.PartnerRemoveFaxNumberYesNoController
import controllers.routes
import forms.partner.PartnerRemoveFaxNumberYesNoFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import pages.CorrespondenceDetailsChangesPage
import pages.partner.PartnerRemoveFaxNumberYesNoPage
import pages.partnerdetails.PartnerDetailsCorrespondenceDetailsSectionPage
import play.api.data.Form
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.PartnerRemoveFaxNumberYesNoView

import scala.concurrent.Future

class PartnerRemoveFaxNumberYesNoControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  private val formProvider = new PartnerRemoveFaxNumberYesNoFormProvider()
  val form: Form[Boolean] = formProvider()

  lazy val partnerRemoveFaxNumberYesNoRoute: String = PartnerRemoveFaxNumberYesNoController.onPageLoad().url
  private val testFaxNumber = "02071234568"
  private val mgdRegNumber = "XGM00000001761"

  // TODO: This index is hardcoded but it should come from the Partner Details list selection
  private val index: Int = 0

  private def cleanedData(faxNumber: Option[String]) = Json.obj(
    "partners" -> Json.arr(
      Json.obj(
        "partnerDetailsMgdRegNumber"  -> mgdRegNumber,
        "partnerRemoveFaxNumberYesNo" -> true,
        "partnerDetailsBusinessName"  -> "Partner1",
        "partnerDetailsCorrespondenceDetailsSection" -> Json.obj(
          "mgdRegNumber" -> "mgdRegNumber",
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
          "faxNumber" -> faxNumber,
          "emailAddr" -> "a@b.com"
        )
      )
    )
  )

  "PartnerRemoveFaxNumberYesNo Controller" - {

    "must populate the view correctly on a GET when the question has previously been answered" in {
      val userAnswers = UserAnswers("mgdRegNumber", cleanedData(Some(testFaxNumber)))

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, partnerRemoveFaxNumberYesNoRoute)

        val view = application.injector.instanceOf[PartnerRemoveFaxNumberYesNoView]

        val result = route(application, request).value
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode, testFaxNumber)(request, messages(application)).toString
      }
    }

    "must redirect to JourneyRecovery on a GET when correspondence details exist but fax number is None" in {

      val userAnswers = UserAnswers("XGM00000001761", cleanedData(None))

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, partnerRemoveFaxNumberYesNoRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to the next page when valid data is submitted" in {
      val userAnswers = UserAnswers("mgdRegNumber", cleanedData(None))

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, partnerRemoveFaxNumberYesNoRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
      }
    }

    "must redirect to the next page when valid data is submitted (Yes selected)" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val userAnswers = UserAnswers("mgdRegNumber", cleanedData(Some(testFaxNumber)))

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, PartnerRemoveFaxNumberYesNoController.onSubmit().url)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val userAnswers = UserAnswers("mgdRegNumber", cleanedData(Some(testFaxNumber)))

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, PartnerRemoveFaxNumberYesNoController.onSubmit().url)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[PartnerRemoveFaxNumberYesNoView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, testFaxNumber)(request, messages(application)).toString
      }
    }

    "must retain the fax number and redirect when 'No' (false) is submitted" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val userAnswers = UserAnswers("mgdRegNumber", cleanedData(Some(testFaxNumber)))

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, PartnerRemoveFaxNumberYesNoController.onSubmit().url)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        val userAnswersCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(userAnswersCaptor.capture())

        val savedAnswers = userAnswersCaptor.getValue
        savedAnswers.get(PartnerDetailsCorrespondenceDetailsSectionPage(0)).flatMap(_.faxNumber) mustBe Some(testFaxNumber)
        savedAnswers.get(CorrespondenceDetailsChangesPage) mustBe Some(false)
      }
    }

    "must redirect to JourneyRecovery when submitting 'Yes' but correspondence details are missing" in {

      val userAnswers = emptyUserAnswers.set(PartnerRemoveFaxNumberYesNoPage(index), true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, PartnerRemoveFaxNumberYesNoController.onSubmit().url)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.SystemErrorController.onPageLoad().url
      }
    }

    "must redirect to 'there is a problem' error page when submitting 'Yes' (true) but correspondence details section is missing" in {

      val userAnswers = emptyUserAnswers

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, PartnerRemoveFaxNumberYesNoController.onSubmit().url)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual "/gambling-variations/there-is-a-problem-with-the-service"
      }
    }

    "must fail/redirect when sessionRepository.set returns a failure" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.failed(new RuntimeException("Database error"))

      val userAnswers = UserAnswers("mgdRegNumber", cleanedData(Some(testFaxNumber)))

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, PartnerRemoveFaxNumberYesNoController.onSubmit().url)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        intercept[RuntimeException] {
          await(result)
        }
      }
    }
  }
}
