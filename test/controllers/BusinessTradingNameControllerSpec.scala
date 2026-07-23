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
import forms.BusinessTradingNameFormProvider
import models.{BusinessType, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.{BusinessNameChangesPage, BusinessTypePage, GroupMemberPage, TradingNamePage}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.BusinessTradingNameView

import scala.concurrent.Future

class BusinessTradingNameControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new BusinessTradingNameFormProvider()
  val form = formProvider()

  lazy val businessTradingNameRoute =
    routes.BusinessTradingNameController.onPageLoad(NormalMode).url

  val data = Json.obj(
    BusinessTypePage.toString -> BusinessType.Partnership.code,
    GroupMemberPage.toString  -> false,
    "businessNameSection"     -> Json.obj("mgdRegNum" -> mgdRegNum)
  )

  private val baseUserAnswers =
    UserAnswers(userAnswersId, data)

  "BusinessTradingName Controller" - {

    "must return OK and the correct view for a GET" in {

      val application =
        applicationBuilder(userAnswers = Some(baseUserAnswers)).build()

      running(application) {

        val request = FakeRequest(GET, businessTradingNameRoute)
        val result = route(application, request).value
        val view = application.injector.instanceOf[BusinessTradingNameView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form, NormalMode, BusinessType.Partnership)(request, messages(application)).toString
      }
    }

    "must redirect to access denied when group member is true on GET" in {

      val groupMemberData = Json.obj(
        BusinessTypePage.toString -> BusinessType.Partnership.code,
        GroupMemberPage.toString  -> true,
        "businessNameSection"     -> Json.obj("mgdRegNum" -> mgdRegNum)
      )

      val application =
        applicationBuilder(
          userAnswers = Some(UserAnswers(userAnswersId, groupMemberData))
        ).build()

      running(application) {

        val request = FakeRequest(GET, businessTradingNameRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.AccessDeniedController.onPageLoad().url
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers =
        baseUserAnswers
          .set(TradingNamePage, "answer")
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {

        val request = FakeRequest(GET, businessTradingNameRoute)
        val view = application.injector.instanceOf[BusinessTradingNameView]
        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form.fill("answer"), NormalMode, BusinessType.Partnership)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any()))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(baseUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {

        val request =
          FakeRequest(POST, businessTradingNameRoute)
            .withFormUrlEncodedBody("value" -> "ABC Ltd")

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
          FakeRequest(POST, businessTradingNameRoute)
            .withFormUrlEncodedBody("value" -> "ABC Ltd")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        verify(mockSessionRepository).set(savedAnswersCaptor.capture())
        savedAnswersCaptor.getValue.get(TradingNamePage).value mustEqual "ABC Ltd"
      }
    }

    "must redirect to access denied when group member is true on POST" in {

      val groupMemberData = Json.obj(
        BusinessTypePage.toString -> BusinessType.Partnership.code,
        GroupMemberPage.toString  -> true,
        "businessNameSection"     -> Json.obj("mgdRegNum" -> mgdRegNum)
      )

      val application =
        applicationBuilder(
          userAnswers = Some(UserAnswers(userAnswersId, groupMemberData))
        ).build()

      running(application) {

        val request =
          FakeRequest(POST, businessTradingNameRoute)
            .withFormUrlEncodedBody("value" -> "ABC Ltd")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.AccessDeniedController.onPageLoad().url
      }
    }

    "must flag BusinessNameChangesPage when data changed in" in {

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
          FakeRequest(POST, businessTradingNameRoute)
            .withFormUrlEncodedBody("value" -> "ABC Ltd")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        verify(mockSessionRepository).set(savedAnswersCaptor.capture())
        savedAnswersCaptor.getValue.get(TradingNamePage).value mustEqual "ABC Ltd"
        savedAnswersCaptor.getValue.get(BusinessNameChangesPage).value mustEqual true
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(baseUserAnswers)).build()

      running(application) {

        val request =
          FakeRequest(POST, businessTradingNameRoute)
            .withFormUrlEncodedBody("value" -> "")

        val boundForm = form.bind(Map("value" -> ""))
        val view = application.injector.instanceOf[BusinessTradingNameView]
        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual
          view(boundForm, NormalMode, BusinessType.Partnership)(request, messages(application)).toString
      }
    }

    "must return a Bad Request when invalid characters are submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(baseUserAnswers)).build()

      running(application) {

        val request =
          FakeRequest(POST, businessTradingNameRoute)
            .withFormUrlEncodedBody("value" -> "ABC@Ltd")

        val boundForm = form.bind(Map("value" -> "ABC@Ltd"))
        val view = application.injector.instanceOf[BusinessTradingNameView]
        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual
          view(boundForm, NormalMode, BusinessType.Partnership)(request, messages(application)).toString
      }
    }

  }
}
