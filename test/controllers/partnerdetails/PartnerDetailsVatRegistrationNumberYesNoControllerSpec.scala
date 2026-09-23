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
import models.{NormalMode, UserAnswers}
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

//TODO normalModeOnly - Done - Maybe fix consistency with userAnswers
class PartnerDetailsVatRegistrationNumberYesNoControllerSpec extends SpecBase with MockitoSugar with PartnerDetailsHelper {

  val formProvider = new VatRegistrationNumberYesNoFormProvider()
  val form = formProvider()

  lazy val vatRegistrationNumberYesNoRouteNewPartners =
    controllers.partnerdetails.routes.PartnerDetailsVatRegistrationNumberYesNoController.onPageLoad(newPartnersIndex1.toString).url

  private val userAnswersWithoutVatAnswerNewPartners =
    UserAnswers(userAnswersId, minimalValidData)
      .set(PartnerDetailsAddPartnerCompletedPage(newPartnersIndex1), false)
      .success
      .value
      .set(PartnerDetailsMgdRegNumberPage(newPartnersIndex1), "123456789")
      .success
      .value
      .set(PartnerDetailsMgdRegNumberPage(newPartnersIndex1), "123456789")
      .success
      .value

  private val userAnswersWithVatAnswerNewPartners =
    userAnswersWithoutVatAnswerNewPartners
      .set(PartnerDetailsVatRegistrationNumberYesNoPage(newPartnersIndex1), true)
      .success
      .value

  "VatRegistrationNumberYesNoController Controller" - {

    "must return OK and the correct view for a GET" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithoutVatAnswerNewPartners)).build()

      running(application) {
        val request =
          FakeRequest(GET, vatRegistrationNumberYesNoRouteNewPartners)

        val result =
          route(application, request).value

        val view =
          application.injector.instanceOf[PartnerDetailsVatRegistrationNumberYesNoView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(
            form,
            newPartnersIndex1.toString,
            NormalMode
          )(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithVatAnswerNewPartners)).build()

      running(application) {
        val request =
          FakeRequest(GET, vatRegistrationNumberYesNoRouteNewPartners)

        val view =
          application.injector.instanceOf[PartnerDetailsVatRegistrationNumberYesNoView]

        val result =
          route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(
            form.fill(true),
            newPartnersIndex1.toString,
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
        applicationBuilder(userAnswers = Some(userAnswersWithoutVatAnswerNewPartners))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, vatRegistrationNumberYesNoRouteNewPartners)
            .withFormUrlEncodedBody(("value", "true"))

        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        val expectedAnswers =
          userAnswersWithoutVatAnswerNewPartners
            .set(PartnerDetailsVatRegistrationNumberYesNoPage(newPartnersIndex1), true)
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
        applicationBuilder(userAnswers = Some(userAnswersWithoutVatAnswerNewPartners))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, vatRegistrationNumberYesNoRouteNewPartners)
            .withFormUrlEncodedBody(("value", "false"))

        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url

        val expectedAnswers =
          userAnswersWithoutVatAnswerNewPartners
            .set(PartnerDetailsVatRegistrationNumberYesNoPage(newPartnersIndex1), false)
            .success
            .value
        verify(mockSessionRepository).set(expectedAnswers)
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithoutVatAnswerNewPartners)).build()

      running(application) {
        val request =
          FakeRequest(POST, vatRegistrationNumberYesNoRouteNewPartners)
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
            newPartnersIndex1.toString,
            NormalMode
          )(request, messages(application)).toString
      }
    }

  }
}
