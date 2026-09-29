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
import controllers.controllingbody.routes.ControllingBodyBusinessTypeController
import forms.controllingbody.ControllingBodyBusinessTypeFormProvider
import models.BusinessType.{Corporatebody, Soleproprietor}
import models.{BusinessType, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import org.mockito.ArgumentCaptor
import pages.controllingbody.{ControllingBodyBusinessTypePage, ControllingBodyChangesPage, ControllingBodySubmittedPage}
import play.api.data.Form
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.controllingbody.ControllingBodyBusinessTypeView

import java.time.LocalDate
import scala.concurrent.Future

class ControllingBodyBusinessTypeControllerSpec extends SpecBase with MockitoSugar {
  def onwardRoute = Call("GET", "/foo")

  val form: Form[BusinessType] = (new ControllingBodyBusinessTypeFormProvider())()

  lazy val controllingBodyBusinessTypeRoute: String =
    ControllingBodyBusinessTypeController.onPageLoad().url

  private val userAnswers = UserAnswers(
    mgdRegNum,
    Json.obj(
      "controllingBodyDetailsSection" -> Json.obj(
        "mgdRegNum"              -> "ZM1000001",
        "businessPartnerNumber"  -> "12787",
        "dateOfJoining"          -> LocalDate.of(2026, 9, 25),
        "dateOfLeaving"          -> LocalDate.of(2026, 9, 25),
        "solePropTitle"          -> "Mr",
        "solePropFirstName"      -> "Jamie",
        "solePropMiddleName"     -> "T",
        "solePropLastName"       -> "Jamie",
        "businessName"           -> "Sticks And Stones",
        "tradingName"            -> "Jamie T",
        "dateOfBirth"            -> LocalDate.of(1991, 10, 1),
        "nino"                   -> "NINONINO",
        "utr"                    -> 123456789,
        "vrn"                    -> 123456789,
        "crn"                    -> "something else",
        "dateOfIncorporation"    -> LocalDate.of(2026, 9, 25),
        "countryOfIncorporation" -> "UK",
        "foreignCorporateRef"    -> "Nothing here",
        "address1"               -> "123",
        "address2"               -> "Road",
        "address3"               -> "Town",
        "address4"               -> "Albion",
        "postcode"               -> "SA11 1AB",
        "country"                -> "England",
        "adi"                    -> "blessYou",
        "isIomOrCiFlag"          -> "1",
        "phoneNumber"            -> "01111 111111",
        "mobilePhoneNumber"      -> "01111 111111",
        "faxNumber"              -> "01111 111111",
        "emailAddr"              -> "a@b.com",
        "typeOfControllingBody"  -> 1,
        "isRepMemSameAsCb"       -> "1",
        "isUkIncorporated"       -> "1"
      )
    )
  )

  "ControllingBodyBusinessType Controller" - {

    "onPageLoad" - {

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, controllingBodyBusinessTypeRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[ControllingBodyBusinessTypeView]
          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form.fill(Soleproprietor), NormalMode)(request, messages(application)).toString
        }
      }

      "must return OK and the correct view for a GET when no previous data exists" in {

        val ua = userAnswers
          .remove(ControllingBodyBusinessTypePage)
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(ua)).build()

        running(application) {
          val request = FakeRequest(GET, controllingBodyBusinessTypeRoute)

          val view = application.injector.instanceOf[ControllingBodyBusinessTypeView]

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
        }
      }
    }

    "onSubmit" - {

      "must update UserAnswers and redirect to the next page when valid data is submitted" in {

        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        val savedAnswersCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])

        val application =
          applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {

          val request =
            FakeRequest(POST, ControllingBodyBusinessTypeController.onSubmit().url)
              .withFormUrlEncodedBody(("value", Corporatebody.toString))
          val result = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
          verify(mockSessionRepository).set(savedAnswersCaptor.capture())
          savedAnswersCaptor.getValue.get(ControllingBodyBusinessTypePage).value mustEqual BusinessType.Corporatebody
          savedAnswersCaptor.getValue.get(ControllingBodySubmittedPage).value mustEqual true
          savedAnswersCaptor.getValue.get(ControllingBodyChangesPage).value mustEqual true
        }
      }

      "must return BAD_REQUEST and errors when invalid data is submitted" in {

        val mockSessionRepository = mock[SessionRepository]

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request =
            FakeRequest(POST, ControllingBodyBusinessTypeController.onSubmit().url)
              .withFormUrlEncodedBody(("value", ""))

          val boundForm = form.bind(Map("value" -> ""))

          val view = application.injector.instanceOf[ControllingBodyBusinessTypeView]

          val result = route(application, request).value
          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
          verify(mockSessionRepository, never()).set(any())
        }
      }


      "must redirect to SystemError for a POST if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request =
            FakeRequest(POST, ControllingBodyBusinessTypeController.onSubmit().url)
              .withFormUrlEncodedBody(("value", Corporatebody.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.SystemErrorController.onPageLoad().url
        }
      }
    }
  }
}
