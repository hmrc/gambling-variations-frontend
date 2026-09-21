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
import forms.partnerdetails.PartnerDetailsAdditionalAddressInfoFormProvider
import models.{CheckMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.partnerdetails.PartnerDetailsAdditionalAddressInfoPage
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.partnerdetails.PartnerDetailsAdditionalAddressInfoView

import scala.concurrent.Future

class PartnerDetailsAdditionalAddressInfoControllerSpec extends SpecBase with MockitoSugar /*with PartnerDetailsHelper*/ {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new PartnerDetailsAdditionalAddressInfoFormProvider()
  val form = formProvider()
  val businessNumber1 = "12345"

  val noAnswers =
    UserAnswers(
      userAnswersId,
      Json.obj(
        "partners" -> Json.obj(
          businessNumber1 -> Json.obj(
            "partnerDetailsMgdRegNumber" -> businessNumber1
          )
        )
//        "partnerDetailsSection" -> Json.obj(
//          "partnerDetailsMgdRegNumber" -> userAnswersId
//        )
      )
    )

  lazy val nameRoute =
    controllers.partnerdetails.routes.PartnerDetailsAdditionalAddressInfoController.onPageLoad(businessNumber1, CheckMode).url

  "PartnerDetailsAdditionalAddressInfo Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(noAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, nameRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PartnerDetailsAdditionalAddressInfoView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form, businessNumber1, CheckMode)(request, messages(application)).toString
      }
    }

    "must populate the view on a GET when the question has previously been answered" in {

//      val data = Json.obj(
//        "partnerDetailsSection"                          -> Json.obj("mgdRegNum" -> userAnswersId),
//        PartnerDetailsAdditionalAddressInfoPage.toString -> "validName"
//      )
//      val data = userAnswers.set(PartnerDetailsAdditionalAddressInfoPage(businessNumber1), "validaName").get

      val userAnswers = noAnswers.set(PartnerDetailsAdditionalAddressInfoPage(businessNumber1), "validName").get // UserAnswers(userAnswersId, data)

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, nameRoute)

        val view = application.injector.instanceOf[PartnerDetailsAdditionalAddressInfoView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form.fill("validName"), businessNumber1, CheckMode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any()))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(noAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {

        val request =
          FakeRequest(POST, nameRoute)
            .withFormUrlEncodedBody("partnerDetailsAdditionalAddressInfo" -> "valid name")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(noAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, nameRoute)
            .withFormUrlEncodedBody(("partnerDetailsAdditionalAddressInfo", ""))

        val boundForm = form.bind(Map("partnerDetailsAdditionalAddressInfo" -> ""))

        val view = application.injector.instanceOf[PartnerDetailsAdditionalAddressInfoView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual
          view(boundForm, businessNumber1, CheckMode)(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = Some(noAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, nameRoute)

        val result = route(application, request).value
        val view = application.injector.instanceOf[PartnerDetailsAdditionalAddressInfoView]
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, businessNumber1, CheckMode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page for a POST if no existing data is found" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(noAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, nameRoute)
            .withFormUrlEncodedBody(("partnerDetailsAdditionalAddressInfo", "validName"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }
  }
}
