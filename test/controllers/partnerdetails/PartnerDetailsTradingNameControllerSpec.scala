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
import controllers.routes
import forms.partnerdetails.PartnerTradingNameFormProvider
import models.{CheckMode, NormalMode}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.partnerdetails.{PartnerDetailsAddPartnerCompletedPage, PartnerDetailsMgdRegNumberPage, PartnerDetailsTradingNamePage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.partnerdetails.PartnerDetailsTradingNameView

import scala.concurrent.Future

class PartnerDetailsTradingNameControllerSpec extends SpecBase with MockitoSugar {

  val businessNumber: String = "0"
  def onwardRoute = Call("GET", "/foo")

  val formProvider = new PartnerTradingNameFormProvider()
  val form = formProvider()

  lazy val partnerTradingNameRoute =
    controllers.partnerdetails.routes.PartnerDetailsTradingNameController
      .onPageLoad(businessNumber, NormalMode)
      .url

  private val userAnswers =
    emptyUserAnswers
      .set(PartnerDetailsAddPartnerCompletedPage(businessNumber.toInt), false)
      .success
      .value
      .set(PartnerDetailsMgdRegNumberPage(businessNumber), "123456789") // partners
      .success
      .value
      .set(PartnerDetailsTradingNamePage(businessNumber.toInt), "Trading Name")
      .success
      .value
      .set(PartnerDetailsMgdRegNumberPage(businessNumber.toInt), "123456789") // new partners
      .success
      .value

  "PartnerTradingName Controller" - {

    "must return OK and the correct view for a GET" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(GET, partnerTradingNameRoute)

        val result =
          route(application, request).value

        val view =
          application.injector.instanceOf[PartnerDetailsTradingNameView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(
            form.fill("Trading Name"),
            businessNumber,
            NormalMode
          )(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(GET, partnerTradingNameRoute)

        val view =
          application.injector.instanceOf[PartnerDetailsTradingNameView]

        val result =
          route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(
            form.fill("Trading Name"),
            businessNumber,
            NormalMode
          )(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

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
          FakeRequest(POST, partnerTradingNameRoute)
            .withFormUrlEncodedBody(("partnerTradingName", "New Trading Name"))

        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, partnerTradingNameRoute)
            .withFormUrlEncodedBody(("partnerTradingName", ""))

        val boundForm =
          form.bind(Map("partnerTradingName" -> ""))

        val view =
          application.injector.instanceOf[PartnerDetailsTradingNameView]

        val result =
          route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual
          view(
            boundForm,
            businessNumber,
            NormalMode
          )(request, messages(application)).toString
      }
    }

    "must redirect to SystemError for a GET if no existing data is found" in {

      val application =
        applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(GET, partnerTradingNameRoute)

        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          routes.SystemErrorController.onPageLoad().url
      }
    }

    "must redirect to SystemError for a POST if no existing data is found" in {

      val application =
        applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, partnerTradingNameRoute)
            .withFormUrlEncodedBody(("value", "Trading Name"))

        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          routes.SystemErrorController.onPageLoad().url
      }
    }

    "must redirect to SystemError for a GET when trading name is missing" in {

      val userAnswers =
        emptyUserAnswers
          .set(PartnerDetailsMgdRegNumberPage(businessNumber), "123456789") // partners
          .success
          .value
          .set(PartnerDetailsMgdRegNumberPage(businessNumber.toInt), "123456789") // new partners
          .success
          .value
          .set(PartnerDetailsAddPartnerCompletedPage(businessNumber.toInt), false)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(GET, partnerTradingNameRoute)

        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          routes.SystemErrorController.onPageLoad().url
      }
    }

    "must redirect to SystemError for a POST when trading name is missing" in {

      val userAnswers =
        emptyUserAnswers
          .set(PartnerDetailsMgdRegNumberPage(businessNumber), "123456789") // partner
          .success
          .value
          .set(PartnerDetailsMgdRegNumberPage(businessNumber.toInt), "123456789") // new partner
          .success
          .value
          .set(PartnerDetailsAddPartnerCompletedPage(businessNumber.toInt), false)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, partnerTradingNameRoute)
            .withFormUrlEncodedBody(("value", "Trading Name"))

        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          routes.SystemErrorController.onPageLoad().url
      }
    }
  }
}
