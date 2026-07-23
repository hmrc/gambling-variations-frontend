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
import forms.RemoveFaxNumberFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.{BusinessContactDetailsSubmittedPage, BusinessFaxNumberPage, ContactDetailsChangesPage, GroupMemberPage, RemoveFaxNumberPage}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.RemoveFaxNumberView

import scala.concurrent.Future

class RemoveFaxNumberControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  private val faxNumber = "123456789"

  private val baseAnswers =
    UserAnswers(
      userAnswersId,
      Json.obj(
        GroupMemberPage.toString        -> false,
        BusinessFaxNumberPage.toString  -> faxNumber,
        "businessContactDetailsSection" -> Json.obj("mgdRegNum" -> mgdRegNum)
      )
    )

  private val answersWithoutFaxNumber =
    UserAnswers(
      userAnswersId,
      Json.obj(
        GroupMemberPage.toString        -> false,
        "businessContactDetailsSection" -> Json.obj("mgdRegNum" -> mgdRegNum)
      )
    )

  private val answersWithoutGroupMember =
    UserAnswers(
      userAnswersId,
      Json.obj(
        BusinessFaxNumberPage.toString  -> faxNumber,
        "businessContactDetailsSection" -> Json.obj("mgdRegNum" -> mgdRegNum)
      )
    )

  val formProvider = new RemoveFaxNumberFormProvider()
  val form = formProvider()

  lazy val removeFaxNumberRoute =
    routes.RemoveFaxNumberController.onPageLoad(NormalMode).url

  "RemoveFaxNumber Controller" - {

    "must return OK and the correct view for a GET" in {

      val application =
        applicationBuilder(userAnswers = Some(baseAnswers)).build()

      running(application) {

        val request = FakeRequest(GET, removeFaxNumberRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RemoveFaxNumberView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form, NormalMode, faxNumber)(request, messages(application)).toString
      }
    }

    "must redirect to SystemError on GET when BusinessFaxNumberPage is missing" in {

      val application =
        applicationBuilder(userAnswers = Some(answersWithoutFaxNumber)).build()

      running(application) {

        val request =
          FakeRequest(GET, removeFaxNumberRoute)

        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.SystemErrorController.onPageLoad().url
      }
    }

    "must redirect to access denied when group member is true on GET" in {

      val userAnswers =
        UserAnswers(
          userAnswersId,
          Json.obj(
            GroupMemberPage.toString        -> true,
            BusinessFaxNumberPage.toString  -> faxNumber,
            "businessContactDetailsSection" -> Json.obj("mgdRegNum" -> mgdRegNum)
          )
        )

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {

        val request = FakeRequest(GET, removeFaxNumberRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.AccessDeniedController.onPageLoad().url
      }
    }

    "must redirect to SystemError on POST when BusinessFaxNumberPage is missing" in {

      val application =
        applicationBuilder(userAnswers = Some(answersWithoutFaxNumber)).build()

      running(application) {

        val request =
          FakeRequest(POST, removeFaxNumberRoute)
            .withFormUrlEncodedBody(
              "value" -> "true"
            )

        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.SystemErrorController.onPageLoad().url
      }
    }

    "must redirect to SystemError when GroupMemberPage is missing on GET" in {

      val application =
        applicationBuilder(userAnswers = Some(answersWithoutGroupMember)).build()

      running(application) {

        val request =
          FakeRequest(GET, removeFaxNumberRoute)

        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.SystemErrorController.onPageLoad().url
      }
    }

    "must keep BusinessFaxNumberPage and set submitted flag when user selects no" in {

      val mockSessionRepository = mock[SessionRepository]
      val savedAnswersCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockSessionRepository.set(any()))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(baseAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {

        val request =
          FakeRequest(POST, removeFaxNumberRoute)
            .withFormUrlEncodedBody(
              "value" -> "false"
            )

        val result =
          route(application, request).value

        status(result) mustEqual SEE_OTHER

        verify(mockSessionRepository).set(savedAnswersCaptor.capture())

        val savedAnswers =
          savedAnswersCaptor.getValue

        savedAnswers.get(RemoveFaxNumberPage) mustBe Some(false)
        savedAnswers.get(BusinessFaxNumberPage) mustBe Some(faxNumber)
        savedAnswers.get(BusinessContactDetailsSubmittedPage) mustBe Some(true)
        savedAnswers.get(ContactDetailsChangesPage) mustBe Some(false)
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers =
        baseAnswers
          .set(RemoveFaxNumberPage, true)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {

        val request = FakeRequest(GET, removeFaxNumberRoute)

        val view = application.injector.instanceOf[RemoveFaxNumberView]

        val result = route(application, request).value

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(form.fill(true), NormalMode, faxNumber)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any()))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(baseAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {

        val request =
          FakeRequest(POST, removeFaxNumberRoute)
            .withFormUrlEncodedBody(("value", "true"))

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
        applicationBuilder(userAnswers = Some(baseAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {

        val request =
          FakeRequest(POST, removeFaxNumberRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        verify(mockSessionRepository).set(savedAnswersCaptor.capture())
        savedAnswersCaptor.getValue.get(RemoveFaxNumberPage).value mustEqual true
      }
    }

    "must flag ContactDetailsChangesPage when data changed in" in {

      val mockSessionRepository = mock[SessionRepository]
      val savedAnswersCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(baseAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {

        val request =
          FakeRequest(POST, removeFaxNumberRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        verify(mockSessionRepository).set(savedAnswersCaptor.capture())
        savedAnswersCaptor.getValue.get(RemoveFaxNumberPage).value mustEqual true
        savedAnswersCaptor.getValue.get(ContactDetailsChangesPage).value mustEqual true
      }
    }
    "must redirect to access denied when group member is true on POST" in {

      val userAnswers =
        UserAnswers(
          userAnswersId,
          Json.obj(
            GroupMemberPage.toString        -> true,
            BusinessFaxNumberPage.toString  -> faxNumber,
            "businessContactDetailsSection" -> Json.obj("mgdRegNum" -> mgdRegNum)
          )
        )

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {

        val request =
          FakeRequest(POST, removeFaxNumberRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.AccessDeniedController.onPageLoad().url
      }
    }

    "must redirect to system error when GroupMemberPage is missing on POST" in {

      val userAnswers =
        UserAnswers(
          userAnswersId,
          Json.obj(
            BusinessFaxNumberPage.toString  -> faxNumber,
            "businessContactDetailsSection" -> Json.obj("mgdRegNum" -> mgdRegNum)
          )
        )

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {

        val request =
          FakeRequest(POST, removeFaxNumberRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.SystemErrorController.onPageLoad().url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(baseAnswers)).build()

      running(application) {

        val request =
          FakeRequest(POST, removeFaxNumberRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[RemoveFaxNumberView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(boundForm, NormalMode, faxNumber)(request, messages(application)).toString
      }
    }

    "must redirect to CheckBusinessNameController for a GET if fax number is missing" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {

        val request = FakeRequest(GET, removeFaxNumberRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.SystemErrorController.onPageLoad().url
      }
    }

    "must redirect to CheckBusinessNameController for a POST if fax number is missing" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {

        val request =
          FakeRequest(POST, removeFaxNumberRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.SystemErrorController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for GET when no session exists" in {

      val application =
        applicationBuilder(userAnswers = None).build()

      running(application) {

        val request = FakeRequest(GET, removeFaxNumberRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.SystemErrorController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for POST when no session exists" in {

      val application =
        applicationBuilder(userAnswers = None).build()

      running(application) {

        val request =
          FakeRequest(POST, removeFaxNumberRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.SystemErrorController.onPageLoad().url
      }
    }
  }
}
