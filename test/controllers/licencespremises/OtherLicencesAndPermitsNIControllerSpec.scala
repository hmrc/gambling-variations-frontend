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

import forms.licencespremises.OtherLicencesAndPermitsNIFormProvider
import base.SpecBase
import models.licencespremises.OtherLicencesAndPermitsNI
import models.licencespremises.OtherLicencesAndPermitsNI.*
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.ArgumentCaptor
import pages.licencespremises.*
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import viewmodels.OtherLicencesAndPermitsNIViewModel
import views.html.licencespremises.OtherLicencesAndPermitsNIView

import scala.concurrent.Future

class OtherLicencesAndPermitsNIControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  lazy val otherLicencesAndPermitsNIRoute: String =
    controllers.licencespremises.routes.OtherLicencesAndPermitsNIController.onPageLoad().url

  val formProvider = new OtherLicencesAndPermitsNIFormProvider()
  val form = formProvider()
  val blankAnswers = UserAnswers(userAnswersId, Json.obj("licencesPremisesSection" -> Json.obj("mgdRegNum" -> "XGM000001761")))

  private val userAnswers = UserAnswers(
    userAnswersId,
    Json.obj(
      "licencesPremisesSection" -> Json.obj(
        "mgdRegNum"                            -> "XGM000001761",
        "amusement"                            -> "1",
        "bingo"                                -> "0",
        "bookmaking"                           -> "0",
        "serveAlcohol"                         -> "1",
        "regCert"                              -> "0",
        "noOtherLicencesAndPremisesNISelected" -> "0"
      )
    )
  )

  "OtherLicencesAndPermitsNI Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, otherLicencesAndPermitsNIRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[OtherLicencesAndPermitsNIView]
        val preparedForm = form.fill(getSelectedLicencesAndPermits(userAnswers))
        val viewModel = OtherLicencesAndPermitsNIViewModel(preparedForm)(messages(application))

        status(result) mustEqual OK

        contentAsString(result) mustBe view(preparedForm, NormalMode, viewModel)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, otherLicencesAndPermitsNIRoute)
        val view = application.injector.instanceOf[OtherLicencesAndPermitsNIView]

        val result = route(application, request).value
        val preparedForm = form.fill(getSelectedLicencesAndPermits(userAnswers))
        val checkboxes = OtherLicencesAndPermitsNIViewModel(preparedForm)(messages(application))
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(preparedForm, NormalMode, checkboxes)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()
      val set = getSelectedLicencesAndPermits(userAnswers)

      running(application) {
        val request =
          FakeRequest(POST, otherLicencesAndPermitsNIRoute)
            .withFormUrlEncodedBody(form.fill(set).data.toSeq*)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must save correct user answers data" in {

      val mockSessionRepository = mock[SessionRepository]
      val savedAnswersCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()
      val set = getSelectedLicencesAndPermits(userAnswers)

      running(application) {
        val request =
          FakeRequest(POST, otherLicencesAndPermitsNIRoute)
            .withFormUrlEncodedBody(form.fill(set).data.toSeq*)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
        verify(mockSessionRepository).set(savedAnswersCaptor.capture())
        savedAnswersCaptor.getValue.get(LicenceAmusementPage).value mustEqual "1"
        savedAnswersCaptor.getValue.get(LicenceBingoPage).value mustEqual "0"
        savedAnswersCaptor.getValue.get(LicenceBookmakingPage).value mustEqual "0"
        savedAnswersCaptor.getValue.get(LicenceServeAlcoholPage).value mustEqual "1"
        savedAnswersCaptor.getValue.get(LicenceRegCertPage).value mustEqual "0"
        savedAnswersCaptor.getValue.get(NoOtherLicencesAndPermitsNIPage).value mustEqual "0"
      }
    }

    "must set all to 0 if none option submitted" in {

      val mockSessionRepository = mock[SessionRepository]
      val savedAnswersCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()
      val set: Set[OtherLicencesAndPermitsNI] = Seq(noOtherLicencesAndPermits).toSet

      running(application) {
        val request =
          FakeRequest(POST, otherLicencesAndPermitsNIRoute)
            .withFormUrlEncodedBody(form.fill(set).data.toSeq*)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
        verify(mockSessionRepository).set(savedAnswersCaptor.capture())
        savedAnswersCaptor.getValue.get(LicenceAmusementPage).value mustEqual "0"
        savedAnswersCaptor.getValue.get(LicenceBingoPage).value mustEqual "0"
        savedAnswersCaptor.getValue.get(LicenceBookmakingPage).value mustEqual "0"
        savedAnswersCaptor.getValue.get(LicenceServeAlcoholPage).value mustEqual "0"
        savedAnswersCaptor.getValue.get(LicenceRegCertPage).value mustEqual "0"
        savedAnswersCaptor.getValue.get(NoOtherLicencesAndPermitsNIPage).value mustEqual "1"
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(blankAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, otherLicencesAndPermitsNIRoute)
            .withFormUrlEncodedBody(("x", "invalid value"))

        val boundForm = form.bind(Map("x" -> "invalid value"))

        val view = application.injector.instanceOf[OtherLicencesAndPermitsNIView]

        val result = route(application, request).value
        val checkboxes = OtherLicencesAndPermitsNIViewModel(boundForm)(messages(application))

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, checkboxes)(request, messages(application)).toString
      }
    }
  }
}
