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
import forms.licencespremises.RemovePremisesDetailsYesNoFormProvider
import models.licencespremises.{PremisesDetails, PremisesDetailsResponse}
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.licencespremises.{LicencesPremisesDetailsChangesPage, LicencesPremisesSectionPage, PremisesDetailsPage, RemovePremisesDetailsYesNoPage}
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.licencespremises.RemovePremisesDetailsYesNoView

import scala.concurrent.Future

class RemovePremisesDetailsYesNoControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute: Call = Call("GET", "/foo")

  val formProvider = new RemovePremisesDetailsYesNoFormProvider()
  val form: Form[Boolean] = formProvider()

  private val premisesDetails = PremisesDetailsResponse(
    totalRows = Some(1),
    premises = Seq(
      PremisesDetails(
        mgdRegNumber = userAnswersId,
        address1 = Some("1 Test Street"),
        address2 = None,
        address3 = None,
        address4 = None,
        postcode = Some("AA1 1AA"),
        systemDate = None
      )
    )
  )

  val userAnswersWithPremisesDetails: UserAnswers =
    emptyUserAnswers
      .set(LicencesPremisesSectionPage, userAnswersId)
      .success
      .value
      .set(PremisesDetailsPage, premisesDetails)
      .success
      .value

  lazy val removePremisesDetailsYesNoRoute: String =
    routes.RemovePremisesDetailsYesNoController.onPageLoad().url

  "RemovePremisesDetailsYesNoController Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithPremisesDetails)).build()

      running(application) {
        val request = FakeRequest(GET, removePremisesDetailsYesNoRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RemovePremisesDetailsYesNoView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view on a GET when the question has previously been answered" in {

      val userAnswers =
        userAnswersWithPremisesDetails
          .set(RemovePremisesDetailsYesNoPage, true)
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, removePremisesDetailsYesNoRoute)

        val view = application.injector.instanceOf[RemovePremisesDetailsYesNoView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form.fill(true), NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithPremisesDetails))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, removePremisesDetailsYesNoRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must remove the premises details and flag the section as changed when the user selects yes" in {

      val mockSessionRepository = mock[SessionRepository]
      val savedAnswersCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithPremisesDetails))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, removePremisesDetailsYesNoRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        verify(mockSessionRepository).set(savedAnswersCaptor.capture())
        savedAnswersCaptor.getValue.get(RemovePremisesDetailsYesNoPage).value mustEqual true
        savedAnswersCaptor.getValue.get(LicencesPremisesDetailsChangesPage).value mustEqual true
        savedAnswersCaptor.getValue.get(PremisesDetailsPage) mustBe None
      }
    }

    "must keep the premises details and not flag the section as changed when the user selects no" in {

      val mockSessionRepository = mock[SessionRepository]
      val savedAnswersCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithPremisesDetails))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, removePremisesDetailsYesNoRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        verify(mockSessionRepository).set(savedAnswersCaptor.capture())
        savedAnswersCaptor.getValue.get(RemovePremisesDetailsYesNoPage).value mustEqual false
        savedAnswersCaptor.getValue.get(LicencesPremisesDetailsChangesPage).value mustEqual false
        savedAnswersCaptor.getValue.get(PremisesDetailsPage).value mustEqual premisesDetails
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithPremisesDetails)).build()

      running(application) {
        val request =
          FakeRequest(POST, removePremisesDetailsYesNoRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[RemovePremisesDetailsYesNoView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual
          view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to SystemError for a GET if no session exists" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, removePremisesDetailsYesNoRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.routes.SystemErrorController.onPageLoad().url
      }
    }

    "must redirect to SystemError for a POST if no session exists" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, removePremisesDetailsYesNoRoute)
            .withFormUrlEncodedBody(("value", ""))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.routes.SystemErrorController.onPageLoad().url
      }
    }
  }
}
