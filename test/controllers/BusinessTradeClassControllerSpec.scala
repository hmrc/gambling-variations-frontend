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

package controllers

import base.SpecBase
import forms.BusinessTradeClassFormProvider
import models.{BusinessTradeClass, BusinessType, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.{BusinessTradeClassPage, BusinessTypePage, GroupMemberPage, MgdTradeDetailsSectionPage, TradingDetailsChangesPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.BusinessTradeClassView
import play.api.mvc.Call

import scala.concurrent.Future

class BusinessTradeClassControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  lazy val businessTradeClassRoute =
    routes.BusinessTradeClassController.onPageLoad(NormalMode).url

  val formProvider = new BusinessTradeClassFormProvider()
  val form = formProvider()

  private val baseUserAnswers =
    UserAnswers(userAnswersId)
      .set(MgdTradeDetailsSectionPage, mgdRegNum)
      .success
      .value
      .set(GroupMemberPage, false)
      .success
      .value
      .set(BusinessTypePage, BusinessType.Soleproprietor)
      .success
      .value

  private val answeredUserAnswers =
    baseUserAnswers
      .set(BusinessTradeClassPage, BusinessTradeClass.Casino)
      .success
      .value

  "BusinessTradeClass Controller" - {

    "must return OK and render page for a GET" in {

      val application =
        applicationBuilder(userAnswers = Some(baseUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, businessTradeClassRoute)

        val result = route(application, request).value
        val view = application.injector.instanceOf[BusinessTradeClassView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on GET when answer exists" in {

      val application =
        applicationBuilder(userAnswers = Some(answeredUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, businessTradeClassRoute)

        val result = route(application, request).value
        val view = application.injector.instanceOf[BusinessTradeClassView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form.fill(BusinessTradeClass.Casino), NormalMode)(
            request,
            messages(application)
          ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(baseUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, businessTradeClassRoute)
            .withFormUrlEncodedBody(
              "value" -> BusinessTradeClass.values.head.toString
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must update data correctly when submitted in" in {

      val mockSessionRepository = mock[SessionRepository]
      val savedAnswersCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(baseUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, businessTradeClassRoute)
            .withFormUrlEncodedBody(
              "value" -> BusinessTradeClass.values.head.toString
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        verify(mockSessionRepository).set(savedAnswersCaptor.capture())
        savedAnswersCaptor.getValue.get(BusinessTradeClassPage).value.toString mustEqual "adultGamingCentre"
      }
    }

    "must flag TradingDetailsChangesPage when data changed in" in {

      val mockSessionRepository = mock[SessionRepository]
      val savedAnswersCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(baseUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, businessTradeClassRoute)
            .withFormUrlEncodedBody(
              "value" -> BusinessTradeClass.values.head.toString
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        verify(mockSessionRepository).set(savedAnswersCaptor.capture())
        savedAnswersCaptor.getValue.get(BusinessTradeClassPage).value.toString mustEqual "adultGamingCentre"
        savedAnswersCaptor.getValue.get(TradingDetailsChangesPage).value mustEqual true
      }
    }

    "must return BAD REQUEST when invalid data is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(baseUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, businessTradeClassRoute)
            .withFormUrlEncodedBody("value" -> "invalid value")

        val result = route(application, request).value
        val view = application.injector.instanceOf[BusinessTradeClassView]

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(form.bind(Map("value" -> "invalid value")), NormalMode)(
            request,
            messages(application)
          ).toString
      }
    }

    "must redirect to System Error page when no user answers on GET" in {

      val application =
        applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, businessTradeClassRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.SystemErrorController.onPageLoad().url
      }
    }

    "must redirect to System Error page when no user answers on POST" in {

      val application =
        applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, businessTradeClassRoute)
            .withFormUrlEncodedBody(
              "value" -> BusinessTradeClass.values.head.toString
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.SystemErrorController.onPageLoad().url
      }
    }
  }
}
