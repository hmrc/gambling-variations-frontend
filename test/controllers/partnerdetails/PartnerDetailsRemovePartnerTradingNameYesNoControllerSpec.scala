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
import forms.partnerdetails.RemovePartnerTradingNameYesNoFormProvider
import models.{CheckMode, NormalMode}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.partnerdetails.{PartnerDetailsAddPartnerCompletedPage, PartnerDetailsMgdRegNumberPage, PartnerDetailsRemovePartnerTradingNameYesNoPage, PartnerDetailsTradingNamePage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.partnerdetails.PartnerDetailsRemovePartnerTradingNameYesNoView

import javax.cache.annotation.CacheKey
import scala.concurrent.Future

//TODO done
class PartnerDetailsRemovePartnerTradingNameYesNoControllerSpec extends SpecBase with MockitoSugar with PartnerDetailsHelper {

  val formProvider = new RemovePartnerTradingNameYesNoFormProvider()
  val form = formProvider()

  lazy val removePartnerTradingNameYesNoRouteNewPartners =
    controllers.partnerdetails.routes.PartnerDetailsRemovePartnerTradingNameYesNoController.onPageLoad(newPartnersIndex1.toString, NormalMode).url

  lazy val removePartnerTradingNameYesNoRouteExistingPartners =
    controllers.partnerdetails.routes.PartnerDetailsRemovePartnerTradingNameYesNoController.onPageLoad(businessNumber1, CheckMode).url

  private val userAnswersWithoutRemoveAnswerNewPartners =
    userAnswersPartnerDetailsMinimalValidData
      .set(PartnerDetailsAddPartnerCompletedPage(newPartnersIndex1), false)
      .success
      .value
      .set(PartnerDetailsMgdRegNumberPage(newPartnersIndex1), "123456789")
      .success
      .value
      .set(PartnerDetailsTradingNamePage(newPartnersIndex1), "Trading Name")
      .success
      .value

  private val userAnswersWithoutRemoveAnswerExistingPartners =
    userAnswersPartnerDetailsMinimalValidData
      .set(PartnerDetailsMgdRegNumberPage(businessNumber1), "123456789")
      .success
      .value
      .set(PartnerDetailsTradingNamePage(businessNumber1), "Trading Name")
      .success
      .value

  private val userAnswersWithRemoveAnswerNewPartners =
    userAnswersWithoutRemoveAnswerNewPartners
      .set(PartnerDetailsRemovePartnerTradingNameYesNoPage(newPartnersIndex1), true)
      .success
      .value

  private val userAnswersWithRemoveAnswerExistingPartners =
    userAnswersWithoutRemoveAnswerExistingPartners
      .set(PartnerDetailsRemovePartnerTradingNameYesNoPage(businessNumber1), true)
      .success
      .value

  "partners" - {

    "RemovePartnerTradingNameYesNo Controller" - {

      "must return OK and the correct view for a GET" in {

        val application =
          applicationBuilder(userAnswers = Some(userAnswersWithoutRemoveAnswerExistingPartners)).build()

        running(application) {
          val request =
            FakeRequest(GET, removePartnerTradingNameYesNoRouteExistingPartners)

          val result =
            route(application, request).value

          val view =
            application.injector.instanceOf[PartnerDetailsRemovePartnerTradingNameYesNoView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual
            view(
              form,
              businessNumber1,
              CheckMode,
              "Trading Name"
            )(request, messages(application)).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val application =
          applicationBuilder(userAnswers = Some(userAnswersWithRemoveAnswerExistingPartners)).build()

        running(application) {
          val request =
            FakeRequest(GET, removePartnerTradingNameYesNoRouteExistingPartners)

          val view =
            application.injector.instanceOf[PartnerDetailsRemovePartnerTradingNameYesNoView]

          val result =
            route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual
            view(
              form.fill(true),
              businessNumber1,
              CheckMode,
              "Trading Name"
            )(request, messages(application)).toString
        }
      }

      "must redirect to the next page when true is submitted" in {

        val mockSessionRepository =
          mock[SessionRepository]

        when(mockSessionRepository.set(any()))
          .thenReturn(Future.successful(true))

        val application =
          applicationBuilder(userAnswers = Some(userAnswersWithoutRemoveAnswerExistingPartners))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, removePartnerTradingNameYesNoRouteExistingPartners)
              .withFormUrlEncodedBody(("value", "true"))

          val result =
            route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      "must redirect to the next page when false is submitted" in {

        val mockSessionRepository =
          mock[SessionRepository]

        when(mockSessionRepository.set(any()))
          .thenReturn(Future.successful(true))

        val application =
          applicationBuilder(userAnswers = Some(userAnswersWithoutRemoveAnswerExistingPartners))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, removePartnerTradingNameYesNoRouteExistingPartners)
              .withFormUrlEncodedBody(("value", "false"))

          val result =
            route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {

        val application =
          applicationBuilder(userAnswers = Some(userAnswersWithoutRemoveAnswerExistingPartners)).build()

        running(application) {
          val request =
            FakeRequest(POST, removePartnerTradingNameYesNoRouteExistingPartners)
              .withFormUrlEncodedBody(("value", ""))

          val boundForm =
            form.bind(Map("value" -> ""))

          val view =
            application.injector.instanceOf[PartnerDetailsRemovePartnerTradingNameYesNoView]

          val result =
            route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual
            view(
              boundForm,
              businessNumber1,
              CheckMode,
              "Trading Name"
            )(request, messages(application)).toString
        }
      }

      "must redirect to SystemError for a GET if no existing data is found" in {

        val application =
          applicationBuilder(userAnswers = None).build()

        running(application) {
          val request =
            FakeRequest(GET, removePartnerTradingNameYesNoRouteExistingPartners)

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
            FakeRequest(POST, removePartnerTradingNameYesNoRouteExistingPartners)
              .withFormUrlEncodedBody(("value", "true"))

          val result =
            route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            routes.SystemErrorController.onPageLoad().url
        }
      }

      "must redirect to SystemError for a GET when trading name is missing" in {

        val application =
          applicationBuilder(
            userAnswers = Some(emptyUserAnswers)
          ).build()

        running(application) {
          val request =
            FakeRequest(GET, removePartnerTradingNameYesNoRouteExistingPartners)

          val result =
            route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            routes.SystemErrorController.onPageLoad().url
        }
      }

      "must redirect to SystemError for a POST when trading name is missing" in {

        val application =
          applicationBuilder(
            userAnswers = Some(emptyUserAnswers)
          ).build()

        running(application) {
          val request =
            FakeRequest(POST, removePartnerTradingNameYesNoRouteExistingPartners)
              .withFormUrlEncodedBody(("value", "true"))

          val result =
            route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            routes.SystemErrorController.onPageLoad().url
        }
      }
    }

  }

  "newPartners" - {

    "RemovePartnerTradingNameYesNo Controller" - {

      "must return OK and the correct view for a GET" in {

        val application =
          applicationBuilder(userAnswers = Some(userAnswersWithoutRemoveAnswerNewPartners)).build()

        running(application) {
          val request =
            FakeRequest(GET, removePartnerTradingNameYesNoRouteNewPartners)

          val result =
            route(application, request).value

          val view =
            application.injector.instanceOf[PartnerDetailsRemovePartnerTradingNameYesNoView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual
            view(
              form,
              newPartnersIndex1.toString,
              NormalMode,
              "Trading Name"
            )(request, messages(application)).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val application =
          applicationBuilder(userAnswers = Some(userAnswersWithRemoveAnswerNewPartners)).build()

        running(application) {
          val request =
            FakeRequest(GET, removePartnerTradingNameYesNoRouteNewPartners)

          val view =
            application.injector.instanceOf[PartnerDetailsRemovePartnerTradingNameYesNoView]

          val result =
            route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual
            view(
              form.fill(true),
              newPartnersIndex1.toString,
              NormalMode,
              "Trading Name"
            )(request, messages(application)).toString
        }
      }

      "must redirect to the next page when true is submitted" in {

        val mockSessionRepository =
          mock[SessionRepository]

        when(mockSessionRepository.set(any()))
          .thenReturn(Future.successful(true))

        val application =
          applicationBuilder(userAnswers = Some(userAnswersWithoutRemoveAnswerNewPartners))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, removePartnerTradingNameYesNoRouteNewPartners)
              .withFormUrlEncodedBody(("value", "true"))

          val result =
            route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      "must redirect to the next page when false is submitted" in {

        val mockSessionRepository =
          mock[SessionRepository]

        when(mockSessionRepository.set(any()))
          .thenReturn(Future.successful(true))

        val application =
          applicationBuilder(userAnswers = Some(userAnswersWithoutRemoveAnswerNewPartners))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, removePartnerTradingNameYesNoRouteNewPartners)
              .withFormUrlEncodedBody(("value", "false"))

          val result =
            route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {

        val application =
          applicationBuilder(userAnswers = Some(userAnswersWithoutRemoveAnswerNewPartners)).build()

        running(application) {
          val request =
            FakeRequest(POST, removePartnerTradingNameYesNoRouteNewPartners)
              .withFormUrlEncodedBody(("value", ""))

          val boundForm =
            form.bind(Map("value" -> ""))

          val view =
            application.injector.instanceOf[PartnerDetailsRemovePartnerTradingNameYesNoView]

          val result =
            route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual
            view(
              boundForm,
              newPartnersIndex1.toString,
              NormalMode,
              "Trading Name"
            )(request, messages(application)).toString
        }
      }

      "must redirect to SystemError for a GET if no existing data is found" in {

        val application =
          applicationBuilder(userAnswers = None).build()

        running(application) {
          val request =
            FakeRequest(GET, removePartnerTradingNameYesNoRouteNewPartners)

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
            FakeRequest(POST, removePartnerTradingNameYesNoRouteNewPartners)
              .withFormUrlEncodedBody(("value", "true"))

          val result =
            route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            routes.SystemErrorController.onPageLoad().url
        }
      }

      "must redirect to SystemError for a GET when trading name is missing" in {

        val application =
          applicationBuilder(
            userAnswers = Some(emptyUserAnswers)
          ).build()

        running(application) {
          val request =
            FakeRequest(GET, removePartnerTradingNameYesNoRouteNewPartners)

          val result =
            route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            routes.SystemErrorController.onPageLoad().url
        }
      }

      "must redirect to SystemError for a POST when trading name is missing" in {

        val application =
          applicationBuilder(
            userAnswers = Some(emptyUserAnswers)
          ).build()

        running(application) {
          val request =
            FakeRequest(POST, removePartnerTradingNameYesNoRouteNewPartners)
              .withFormUrlEncodedBody(("value", "true"))

          val result =
            route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            routes.SystemErrorController.onPageLoad().url
        }
      }
    }

  }
}
