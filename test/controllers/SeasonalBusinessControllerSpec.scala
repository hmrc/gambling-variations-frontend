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
import forms.SeasonalBusinessFormProvider
import models.{BusinessType, NormalMode, UserAnswers}
import navigation.FakeNavigator
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.{BusinessTypePage, GroupMemberPage, IsSeasonalBusinessPage, MgdTradeDetailsSectionPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.SeasonalBusinessView
import navigation.Navigator
import scala.concurrent.Future

class SeasonalBusinessControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new SeasonalBusinessFormProvider()
  val form = formProvider()

  lazy val seasonalBusinessRoute =
    routes.SeasonalBusinessController.onPageLoad(NormalMode).url

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
      .set(IsSeasonalBusinessPage, true)
      .success
      .value

  "SeasonalBusiness Controller" - {

    "must return OK and render page for a GET" in {

      val application =
        applicationBuilder(userAnswers = Some(baseUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, seasonalBusinessRoute)

        val result = route(application, request).value
        val view = application.injector.instanceOf[SeasonalBusinessView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on GET when answer exists" in {

      val application =
        applicationBuilder(userAnswers = Some(answeredUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, seasonalBusinessRoute)

        val result = route(application, request).value
        val view = application.injector.instanceOf[SeasonalBusinessView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form.fill(true), NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to next page when valid data is submitted" in {

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
          FakeRequest(POST, seasonalBusinessRoute)
            .withFormUrlEncodedBody("isSeasonalBusiness" -> "true")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(onwardRoute.url)
      }
    }

    "must return BAD REQUEST when invalid data is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(baseUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, seasonalBusinessRoute)
            .withFormUrlEncodedBody("isSeasonalBusiness" -> "")

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        val view = application.injector.instanceOf[SeasonalBusinessView]

        contentAsString(result) mustEqual
          view(form.bind(Map("isSeasonalBusiness" -> "")), NormalMode)(
            request,
            messages(application)
          ).toString
      }
    }

    "must redirect to System Error page when no user answers on GET" in {

      val application =
        applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, seasonalBusinessRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(routes.SystemErrorController.onPageLoad().url)
      }
    }

    "must redirect to System Error page when no user answers on POST" in {

      val application =
        applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, seasonalBusinessRoute)
            .withFormUrlEncodedBody("isSeasonalBusiness" -> "true")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(routes.SystemErrorController.onPageLoad().url)
      }
    }
  }
}
