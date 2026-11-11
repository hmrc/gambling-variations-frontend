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

package controllers.businessname

import base.SpecBase
import controllers.routes
import forms.RemoveTradeNameFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.GroupMemberPage
import pages.businessname.{BusinessNameChangesPage, BusinessNameSection, RemoveTradeNamePage, TradingNamePage}
import play.api.inject.bind
import play.api.libs.json.{JsNull, Json}
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.businessname.RemoveTradeNameView

import scala.concurrent.Future

class RemoveTradeNameControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new RemoveTradeNameFormProvider()
  val form = formProvider()
  val tradingName = "Test Trader"

  val data = Json.obj(
    GroupMemberPage.toString -> false,
    "businessNameSection"    -> Json.obj("tradingName" -> "Test Trader")
  )

  val noAnswers = Json.obj(GroupMemberPage.toString -> false)

  lazy val removeTradeNameRoute = controllers.businessname.routes.RemoveTradeNameController.onPageLoad().url

  "RemoveTradeName Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(LoadedBusinessName("id", data))).build()

      running(application) {
        val request = FakeRequest(GET, removeTradeNameRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RemoveTradeNameView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, tradingName)(request, messages(application)).toString
      }
    }

    "must redirect to access denied when group member is true on GET" in {

      val groupMemberData = Json.obj(
        GroupMemberPage.toString -> true,
        "businessNameSection"    -> Json.obj("tradingName" -> "Test Trader")
      )

      val application =
        applicationBuilder(userAnswers = Some(LoadedBusinessName("id", groupMemberData))).build()

      running(application) {
        val request = FakeRequest(GET, removeTradeNameRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.AccessDeniedController.onPageLoad().url
      }
    }

    "must redirect to system error when GroupMemberPage is missing on GET" in {

      val noGroupMemberData = Json.obj("businessNameSection" -> Json.obj("tradingName" -> "Test Trader"))

      val application =
        applicationBuilder(userAnswers = Some(LoadedBusinessName("id", noGroupMemberData))).build()

      running(application) {
        val request = FakeRequest(GET, removeTradeNameRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.SystemErrorController.onPageLoad().url
      }
    }

    "must redirect" - {
      "to Check Business Name when no Trading Name exists" - {
        "when GET" in {

          val application = applicationBuilder(userAnswers = Some(LoadedBusinessName(userAnswersId, noAnswers))).build()

          running(application) {
            val request = FakeRequest(GET, removeTradeNameRoute)

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.businessname.routes.CheckBusinessNameController.onPageLoad().url
          }
        }

        "when POST" in {

          val application =
            applicationBuilder(userAnswers = Some(LoadedBusinessName(userAnswersId, noAnswers))).build()

          running(application) {
            val request =
              FakeRequest(POST, removeTradeNameRoute)
                .withFormUrlEncodedBody(("value", "true"))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.businessname.routes.CheckBusinessNameController.onPageLoad().url
          }
        }
      }

      "must redirect to system error when GroupMemberPage is missing on POST" in {

        val noGroupMemberData = Json.obj("businessNameSection" -> Json.obj("tradingName" -> "Test Trader"))

        val application =
          applicationBuilder(userAnswers = Some(LoadedBusinessName("id", noGroupMemberData))).build()

        running(application) {
          val request =
            FakeRequest(POST, removeTradeNameRoute)
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.SystemErrorController.onPageLoad().url
        }
      }

      "must redirect to access denied when group member is true on POST" in {

        val groupMemberData = Json.obj(
          GroupMemberPage.toString -> true,
          "businessNameSection"    -> Json.obj("tradingName" -> "Test Trader")
        )

        val application =
          applicationBuilder(userAnswers = Some(LoadedBusinessName("id", groupMemberData))).build()

        running(application) {
          val request =
            FakeRequest(POST, removeTradeNameRoute)
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.AccessDeniedController.onPageLoad().url
        }
      }

      "to the next page when valid data is submitted" in {

        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(LoadedBusinessName("id", data)))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, removeTradeNameRoute)
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }
    }

    "must update data correctly when submitted in" in {

      val mockSessionRepository = mock[SessionRepository]
      val savedAnswersCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(LoadedBusinessName("id", data)))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, removeTradeNameRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        verify(mockSessionRepository).set(savedAnswersCaptor.capture())
        savedAnswersCaptor.getValue.get(RemoveTradeNamePage).value mustEqual true
      }
    }

    "must assert the removal, so that the trading name loaded from the backend does not show again" in {

      val mockSessionRepository = mock[SessionRepository]
      val savedAnswersCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(LoadedBusinessName("id", data)))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, removeTradeNameRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        verify(mockSessionRepository).set(savedAnswersCaptor.capture())

        val savedAnswers = savedAnswersCaptor.getValue
        savedAnswers.get(TradingNamePage) mustBe None
        savedAnswers.original.get[String](TradingNamePage.path).value mustEqual tradingName
        savedAnswers.changes(BusinessNameSection) mustBe Json.obj(
          "businessNameSection" -> Json.obj("tradingName" -> JsNull)
        )
      }
    }

    "must flag BusinessNameChangesPage when data changed in" in {

      val mockSessionRepository = mock[SessionRepository]
      val savedAnswersCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(LoadedBusinessName("id", data)))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, removeTradeNameRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        verify(mockSessionRepository).set(savedAnswersCaptor.capture())
        savedAnswersCaptor.getValue.get(RemoveTradeNamePage).value mustEqual true
        savedAnswersCaptor.getValue.get(BusinessNameChangesPage).value mustEqual true
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(LoadedBusinessName("id", data))).build()

      running(application) {
        val request =
          FakeRequest(POST, removeTradeNameRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[RemoveTradeNameView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, tradingName)(request, messages(application)).toString
      }
    }
  }
}
