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
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.{BusinessTypePage, TradingNamePage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.BusinessTradingNameView
import connectors.GamblingConnector
import models.{BusinessNameDetails, BusinessType, NormalMode, UserAnswers}
import scala.concurrent.Future

class BusinessTradingNameControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new BusinessTradingNameFormProvider()
  val form = formProvider()

  lazy val businessTradingNameRoute =
    routes.BusinessTradingNameController.onPageLoad(NormalMode).url

  private val baseUserAnswers =
    emptyUserAnswers.set(BusinessTypePage, BusinessType.Partnership).success.value

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

    "must populate answers from connector when no existing data is found for a GET" in {

      val mockConnector = mock[GamblingConnector]
      val mockSessionRepository = mock[SessionRepository]

      when(mockConnector.getBusinessName(any())(any()))
        .thenReturn(
          Future.successful(
            BusinessNameDetails(
              mgdRegNum    = "safeId",
              businessName = "Test Business Ltd",
              businessType = BusinessType.Partnership,
              tradingName  = None,
              systemDate   = None
            )
          )
        )

      when(mockSessionRepository.set(any()))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = None)
          .overrides(
            bind[GamblingConnector].toInstance(mockConnector),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {

        val request = FakeRequest(GET, businessTradingNameRoute)
        val result = route(application, request).value

        status(result) mustEqual OK
      }
    }

    "must populate answers from connector when no existing data is found for a POST" in {

      val mockConnector = mock[GamblingConnector]
      val mockSessionRepository = mock[SessionRepository]

      when(mockConnector.getBusinessName(any())(any()))
        .thenReturn(
          Future.successful(
            BusinessNameDetails(
              mgdRegNum    = "safeId",
              businessName = "Test Business Ltd",
              businessType = BusinessType.Partnership,
              tradingName  = None,
              systemDate   = None
            )
          )
        )

      when(mockSessionRepository.set(any()))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = None)
          .overrides(
            bind[GamblingConnector].toInstance(mockConnector),
            bind[SessionRepository].toInstance(mockSessionRepository),
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute))
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
  }
}
