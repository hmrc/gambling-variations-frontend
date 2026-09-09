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

import forms.licencespremises.OtherLicencesAndPermitsGBFormProvider
import base.SpecBase
import models.licencespremises.OtherLicencesAndPermitsGB
import models.licencespremises.OtherLicencesAndPermitsGB.*
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
import viewmodels.OtherLicencesAndPermitsViewModel
import views.html.licencespremises.OtherLicencesAndPermitsGBView

import scala.concurrent.Future

class OtherLicencesAndPermitsGBControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  lazy val otherLicencesAndPermitsGBRoute: String =
    controllers.licencespremises.routes.OtherLicencesAndPermitsGBController.onPageLoad().url

  val formProvider = new OtherLicencesAndPermitsGBFormProvider()
  val form = formProvider()
  val blankAnswers = UserAnswers(userAnswersId, Json.obj("licencesPremisesSection" -> Json.obj("mgdRegNum" -> "XGM000001761")))

  private val userAnswers = UserAnswers(
    userAnswersId,
    Json.obj(
      "licencesPremisesSection" -> Json.obj(
        "mgdRegNum"                            -> "XGM000001761",
        "clubGaming"                           -> "1",
        "clubMachine"                          -> "0",
        "clubPremises"                         -> "0",
        "familyEntertainment"                  -> "1",
        "localAuthority"                       -> "0",
        "onPremises"                           -> "1",
        "prizeGaming"                          -> "0",
        "noOtherLicencesAndPremisesGBSelected" -> "0"
      )
    )
  )

  "OtherLicencesAndPermitsGB Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, otherLicencesAndPermitsGBRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[OtherLicencesAndPermitsGBView]
        val preparedForm = form.fill(getSelectedLicencesAndPermits(userAnswers))
        val viewModel = OtherLicencesAndPermitsViewModel(preparedForm)(messages(application))

        status(result) mustEqual OK

        contentAsString(result) mustBe view(preparedForm, NormalMode, viewModel)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, otherLicencesAndPermitsGBRoute)
        val view = application.injector.instanceOf[OtherLicencesAndPermitsGBView]

        val result = route(application, request).value
        val preparedForm = form.fill(getSelectedLicencesAndPermits(userAnswers))
        val checkboxes = OtherLicencesAndPermitsViewModel(preparedForm)(messages(application))
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
          FakeRequest(POST, otherLicencesAndPermitsGBRoute)
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
          FakeRequest(POST, otherLicencesAndPermitsGBRoute)
            .withFormUrlEncodedBody(form.fill(set).data.toSeq*)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
        verify(mockSessionRepository).set(savedAnswersCaptor.capture())
        savedAnswersCaptor.getValue.get(LicenceClubGamingPage).value mustEqual "1"
        savedAnswersCaptor.getValue.get(ClubLicencePage).value mustEqual "0"
        savedAnswersCaptor.getValue.get(LicenceClubPremisesPage).value mustEqual "0"
        savedAnswersCaptor.getValue.get(LicenceFamilyEntertainmentPage).value mustEqual "1"
        savedAnswersCaptor.getValue.get(LicenceLocalAuthorityPage).value mustEqual "0"
        savedAnswersCaptor.getValue.get(LicenceOnPremisesPage).value mustEqual "1"
        savedAnswersCaptor.getValue.get(LicencePrizeGamingPage).value mustEqual "0"
        savedAnswersCaptor.getValue.get(NoOtherLicencesAndPermitsGBPage).value mustEqual "0"
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
      val set: Set[OtherLicencesAndPermitsGB] = Seq(noOtherLicencesAndPermits).toSet

      running(application) {
        val request =
          FakeRequest(POST, otherLicencesAndPermitsGBRoute)
            .withFormUrlEncodedBody(form.fill(set).data.toSeq*)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
        verify(mockSessionRepository).set(savedAnswersCaptor.capture())
        savedAnswersCaptor.getValue.get(LicenceClubGamingPage).value mustEqual "0"
        savedAnswersCaptor.getValue.get(ClubLicencePage).value mustEqual "0"
        savedAnswersCaptor.getValue.get(LicenceClubPremisesPage).value mustEqual "0"
        savedAnswersCaptor.getValue.get(LicenceFamilyEntertainmentPage).value mustEqual "0"
        savedAnswersCaptor.getValue.get(LicenceLocalAuthorityPage).value mustEqual "0"
        savedAnswersCaptor.getValue.get(LicenceOnPremisesPage).value mustEqual "0"
        savedAnswersCaptor.getValue.get(LicencePrizeGamingPage).value mustEqual "0"
        savedAnswersCaptor.getValue.get(NoOtherLicencesAndPermitsGBPage).value mustEqual "1"
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(blankAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, otherLicencesAndPermitsGBRoute)
            .withFormUrlEncodedBody(("x", "invalid value"))

        val boundForm = form.bind(Map("x" -> "invalid value"))

        val view = application.injector.instanceOf[OtherLicencesAndPermitsGBView]

        val result = route(application, request).value
        val checkboxes = OtherLicencesAndPermitsViewModel(boundForm)(messages(application))

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, checkboxes)(request, messages(application)).toString
      }
    }
  }
}
