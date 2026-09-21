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

package controllers.partnerdetails

import base.SpecBase
import forms.partnerdetails.VatRegistrationNumberYesNoFormProvider
import models.NormalMode
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.partnerdetails.{PartnerDetailsAddPartnerCompletedPage, PartnerDetailsMgdRegNumberPage, PartnerDetailsVatRegistrationNumberYesNoPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.partnerdetails.PartnerDetailsVatRegistrationNumberYesNoView

import scala.concurrent.Future

class PartnerDetailsVatRegistrationNumberYesNoControllerSpec extends SpecBase with MockitoSugar {

  val index = 0
  val businessNumber: String = "12345"
  def onwardRoute = Call("GET", "/foo")

  val formProvider = new VatRegistrationNumberYesNoFormProvider()
  val form = formProvider()

  lazy val vatRegistrationNumberYesNoRoute =
    controllers.partnerdetails.routes.PartnerDetailsVatRegistrationNumberYesNoController.onPageLoad(index.toString).url

  private val userAnswersWithoutVatAnswer =
    emptyUserAnswers
      .set(PartnerDetailsAddPartnerCompletedPage(index), false)
      .success
      .value
      .set(PartnerDetailsMgdRegNumberPage(index), "123456789")
      .success
      .value
      .set(PartnerDetailsMgdRegNumberPage(businessNumber), "123456789")
      .success
      .value

  private val userAnswersWithVatAnswer =
    userAnswersWithoutVatAnswer
      .set(PartnerDetailsVatRegistrationNumberYesNoPage(index), true)
      .success
      .value

  "VatRegistrationNumberYesNoController Controller" - {

    "must return OK and the correct view for a GET" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithoutVatAnswer)).build()

      running(application) {
        val request =
          FakeRequest(GET, vatRegistrationNumberYesNoRoute)

        val result =
          route(application, request).value

        val view =
          application.injector.instanceOf[PartnerDetailsVatRegistrationNumberYesNoView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(
            form,
            index.toString,
            NormalMode
          )(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithVatAnswer)).build()

      running(application) {
        val request =
          FakeRequest(GET, vatRegistrationNumberYesNoRoute)

        val view =
          application.injector.instanceOf[PartnerDetailsVatRegistrationNumberYesNoView]

        val result =
          route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(
            form.fill(true),
            index.toString,
            NormalMode
          )(request, messages(application)).toString
      }
    }

    "must redirect to the next page when true is submitted" in {

      val mockSessionRepository =
        mock[SessionRepository]

      when(mockSessionRepository.set(any()))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithoutVatAnswer))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, vatRegistrationNumberYesNoRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        val expectedAnswers =
          userAnswersWithoutVatAnswer
            .set(PartnerDetailsVatRegistrationNumberYesNoPage(index), true)
            .success
            .value
        verify(mockSessionRepository).set(expectedAnswers)
      }
    }

    "must redirect to the next page when false is submitted" in {

      val mockSessionRepository =
        mock[SessionRepository]

      when(mockSessionRepository.set(any()))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithoutVatAnswer))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, vatRegistrationNumberYesNoRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        val expectedAnswers =
          userAnswersWithoutVatAnswer
            .set(PartnerDetailsVatRegistrationNumberYesNoPage(index), false)
            .success
            .value
        verify(mockSessionRepository).set(expectedAnswers)
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithoutVatAnswer)).build()

      running(application) {
        val request =
          FakeRequest(POST, vatRegistrationNumberYesNoRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm =
          form.bind(Map("value" -> ""))

        val view =
          application.injector.instanceOf[PartnerDetailsVatRegistrationNumberYesNoView]

        val result =
          route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual
          view(
            boundForm,
            index.toString,
            NormalMode
          )(request, messages(application)).toString
      }
    }

  }
}
