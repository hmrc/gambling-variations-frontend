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
import forms.partner.VatRegistrationNumberYesNoFormProvider
import models.NormalMode
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.partner.VatRegistrationNumberYesNoPage
import pages.partnerdetails.PartnerDetailsPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.partner.VatRegistrationNumberYesNoView

import scala.concurrent.Future

class VatRegistrationNumberYesNoControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new VatRegistrationNumberYesNoFormProvider()
  val form = formProvider()

  lazy val vatRegistrationNumberYesNoRoute =
    controllers.partner.routes.VatRegistrationNumberYesNoController.onPageLoad().url

  "VatRegistrationNumberYesNoController Controller" - {

    "must return OK and the correct view for a GET" in {

      val userAnswers =
        emptyUserAnswers
          .set(PartnerDetailsPage(0), "123456789")
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(GET, vatRegistrationNumberYesNoRoute)

        val result =
          route(application, request).value

        val view =
          application.injector.instanceOf[VatRegistrationNumberYesNoView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(
            form,
            NormalMode
          )(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers =
        emptyUserAnswers
          .set(PartnerDetailsPage(0), "123456789")
          .success
          .value
          .set(VatRegistrationNumberYesNoPage(0), true)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(GET, vatRegistrationNumberYesNoRoute)

        val view =
          application.injector.instanceOf[VatRegistrationNumberYesNoView]

        val result =
          route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(
            form.fill(true),
            NormalMode
          )(request, messages(application)).toString
      }
    }

    "must redirect to the next page when true is submitted" in {

      val userAnswers =
        emptyUserAnswers
          .set(PartnerDetailsPage(0), "123456789")
          .success
          .value

      val mockSessionRepository =
        mock[SessionRepository]

      when(mockSessionRepository.set(any()))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, vatRegistrationNumberYesNoRoute)
            .withFormUrlEncodedBody(("vrn", "true"))

        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to the next page when false is submitted" in {

      val userAnswers =
        emptyUserAnswers
          .set(PartnerDetailsPage(0), "123456789")
          .success
          .value

      val mockSessionRepository =
        mock[SessionRepository]

      when(mockSessionRepository.set(any()))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, vatRegistrationNumberYesNoRoute)
            .withFormUrlEncodedBody(("vrn", "false"))

        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val userAnswers =
        emptyUserAnswers
          .set(PartnerDetailsPage(0), "123456789")
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, vatRegistrationNumberYesNoRoute)
            .withFormUrlEncodedBody(("vrn", ""))

        val boundForm =
          form.bind(Map("vrn" -> ""))

        val view =
          application.injector.instanceOf[VatRegistrationNumberYesNoView]

        val result =
          route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual
          view(
            boundForm,
            NormalMode
          )(request, messages(application)).toString
      }
    }

  }
}
