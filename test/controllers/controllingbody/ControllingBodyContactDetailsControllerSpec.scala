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

package controllers.controllingbody

import base.SpecBase
import controllers.controllingbody.routes.ControllingBodyContactDetailsController
import forms.ContactNumberFormProvider
import models.{ContactNumber, CorrespondenceDetails, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.controllingbody.{ControllingBodyChangesPage, ControllingBodyContactDetailsPage, ControllingBodyCorrespondenceSectionPage, ControllingBodySectionPage, ControllingBodySubmittedPage}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.controllingbody.ControllingBodyContactDetailsView

import scala.concurrent.Future

class ControllingBodyContactDetailsControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute: Call = Call("GET", "/foo")

  val formProvider = new ContactNumberFormProvider()
  val form = formProvider("controllingBodyContactDetails")

  private val phoneNumber = "123456789"
  private val mobilePhoneNumber = "4546"

  private lazy val controllingBodyContactDetailsRoute: String =
    ControllingBodyContactDetailsController.onPageLoad().url

  val userAnswersWithoutContactDetails: UserAnswers =
    UserAnswers("id", Json.obj())
      .set(ControllingBodySectionPage, userAnswersId)
      .success
      .value
      .set(
        ControllingBodyCorrespondenceSectionPage,
        CorrespondenceDetails(
          mgdRegNumber          = "",
          nameLine1             = None,
          nameLine2             = None,
          correspondenceAddress = None,
          additionalInformation = None,
          iomOrCiFlag           = None,
          contactNumber         = None,
          faxNumber             = None,
          emailAddr             = None
        )
      )
      .success
      .value

  val userAnswersWithContactDetails: UserAnswers =
    userAnswersWithoutContactDetails
      .set(
        ControllingBodyContactDetailsPage,
        ContactNumber(
          phoneNumber       = None,
          mobilePhoneNumber = Some(mobilePhoneNumber)
        )
      )
      .success
      .value

  "ControllingBodyContactDetails Controller" - {

    "must return OK and the correct view for a GET" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithoutContactDetails)).build()

      running(application) {
        val request =
          FakeRequest(GET, controllingBodyContactDetailsRoute)

        val view =
          application.injector.instanceOf[ControllingBodyContactDetailsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithContactDetails)).build()

      running(application) {
        val request =
          FakeRequest(GET, controllingBodyContactDetailsRoute)

        val view =
          application.injector.instanceOf[ControllingBodyContactDetailsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(
            form.fill(
              ContactNumber(
                phoneNumber       = None,
                mobilePhoneNumber = Some(mobilePhoneNumber)
              )
            ),
            NormalMode
          )(request, messages(application)).toString
      }
    }

    "must redirect to the next page when one number is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any()))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithoutContactDetails))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, controllingBodyContactDetailsRoute)
            .withFormUrlEncodedBody(
              ("phoneNumber", phoneNumber)
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must redirect to the next page when both numbers are submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any()))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithoutContactDetails))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, controllingBodyContactDetailsRoute)
            .withFormUrlEncodedBody(
              ("phoneNumber", phoneNumber),
              ("mobileNumber", mobilePhoneNumber)
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithoutContactDetails)).build()

      running(application) {
        val request =
          FakeRequest(POST, controllingBodyContactDetailsRoute)
            .withFormUrlEncodedBody(
              ("phoneNumber", "invalid value")
            )

        val boundForm =
          form.bind(Map("phoneNumber" -> "invalid value"))

        val view =
          application.injector.instanceOf[ControllingBodyContactDetailsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual
          view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must return a Bad Request and errors when valid and invalid data are submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithoutContactDetails)).build()

      running(application) {
        val request =
          FakeRequest(POST, controllingBodyContactDetailsRoute)
            .withFormUrlEncodedBody(
              ("phoneNumber", "invalid value"),
              ("mobileNumber", mobilePhoneNumber)
            )

        val boundForm =
          form.bind(
            Map(
              "phoneNumber"  -> "invalid value",
              "mobileNumber" -> mobilePhoneNumber
            )
          )

        val view =
          application.injector.instanceOf[ControllingBodyContactDetailsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual
          view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must return a Bad Request and errors when no data is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithoutContactDetails)).build()

      running(application) {
        val request =
          FakeRequest(POST, controllingBodyContactDetailsRoute)
            .withFormUrlEncodedBody(
              ("phoneNumber", ""),
              ("mobileNumber", "")
            )

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) must include(
          messages(application)(
            "controllingBodyContactDetails.error.phoneNumber.required"
          )
        )
      }
    }

    "must return See Other when no user answers are present" in {

      val application =
        applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(GET, controllingBodyContactDetailsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
      }
    }

    "must update UserAnswers correctly when contact details are submitted for the first time" in {

      val mockSessionRepository = mock[SessionRepository]
      val savedAnswersCaptor =
        ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockSessionRepository.set(any[UserAnswers]))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithoutContactDetails))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, controllingBodyContactDetailsRoute)
            .withFormUrlEncodedBody(
              "mobileNumber" -> mobilePhoneNumber
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        verify(mockSessionRepository).set(savedAnswersCaptor.capture())

        val savedAnswers = savedAnswersCaptor.getValue

        savedAnswers.get(ControllingBodyContactDetailsPage).value mustEqual
          ContactNumber(
            phoneNumber       = None,
            mobilePhoneNumber = Some(mobilePhoneNumber)
          )
        savedAnswers.get(ControllingBodySubmittedPage).value mustEqual true
        savedAnswers.get(ControllingBodyChangesPage).value mustEqual true
      }
    }

    "must mark changes as false when the submitted contact details have not changed" in {

      val mockSessionRepository = mock[SessionRepository]
      val savedAnswersCaptor =
        ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockSessionRepository.set(any[UserAnswers]))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithContactDetails))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, controllingBodyContactDetailsRoute)
            .withFormUrlEncodedBody(
              "mobileNumber" -> mobilePhoneNumber
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        verify(mockSessionRepository).set(savedAnswersCaptor.capture())

        val savedAnswers = savedAnswersCaptor.getValue

        savedAnswers.get(ControllingBodyContactDetailsPage).value mustEqual
          ContactNumber(
            phoneNumber       = None,
            mobilePhoneNumber = Some(mobilePhoneNumber)
          )
        savedAnswers.get(ControllingBodySubmittedPage).value mustEqual true
        savedAnswers.get(ControllingBodyChangesPage).value mustEqual false
      }
    }

    "must mark changes as true when the submitted contact details differ from the existing contact details" in {

      val mockSessionRepository = mock[SessionRepository]
      val savedAnswersCaptor =
        ArgumentCaptor.forClass(classOf[UserAnswers])

      val existingMobileNumber = "987654321"

      val answersWithDifferentContactDetails =
        userAnswersWithoutContactDetails
          .set(
            ControllingBodyContactDetailsPage,
            ContactNumber(
              phoneNumber       = None,
              mobilePhoneNumber = Some(existingMobileNumber)
            )
          )
          .success
          .value

      when(mockSessionRepository.set(any[UserAnswers]))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(answersWithDifferentContactDetails))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, controllingBodyContactDetailsRoute)
            .withFormUrlEncodedBody(
              "mobileNumber" -> mobilePhoneNumber
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        verify(mockSessionRepository).set(savedAnswersCaptor.capture())

        val savedAnswers = savedAnswersCaptor.getValue

        savedAnswers.get(ControllingBodyContactDetailsPage).value mustEqual
          ContactNumber(
            phoneNumber       = None,
            mobilePhoneNumber = Some(mobilePhoneNumber)
          )
        savedAnswers.get(ControllingBodySubmittedPage).value mustEqual true
        savedAnswers.get(ControllingBodyChangesPage).value mustEqual true
      }
    }

    "must redirect to System Error when user answers are missing on POST" in {

      val application =
        applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, controllingBodyContactDetailsRoute)
            .withFormUrlEncodedBody(
              "mobileNumber" -> mobilePhoneNumber
            )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.routes.SystemErrorController.onPageLoad().url
      }
    }
  }
}
