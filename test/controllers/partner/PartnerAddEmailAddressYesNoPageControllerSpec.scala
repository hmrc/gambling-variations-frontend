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

package controllers.partner

import base.SpecBase
import controllers.partner.routes.PartnerAddEmailAddressYesNoPageController
import forms.PartnerAddEmailAddressYesNoPageFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.partner.PartnerAddEmailAddressYesNoPagePage
import play.api.data.Form
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.partner.PartnerAddEmailAddressYesNoPageView

import scala.concurrent.Future

class PartnerAddEmailAddressYesNoPageControllerSpec extends SpecBase with MockitoSugar {

  val formProvider: PartnerAddEmailAddressYesNoPageFormProvider = new PartnerAddEmailAddressYesNoPageFormProvider()
  val form: Form[Boolean] = formProvider()
  // TODO: This index is hardcoded but it should come from the Partner Details list selection
  private val index: Int = 0
  private val data = Json.obj("mgdTradeDetailsSection" -> Json.obj("mgdRegNum" -> mgdRegNum))

  private val baseUserAnswers = Some(UserAnswers(userAnswersId, data))

  lazy val partnerAddEmailAddressYesNoPageRoute: String = PartnerAddEmailAddressYesNoPageController.onPageLoad().url

  "PartnerAddEmailAddressYesNoPage Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = baseUserAnswers).build()

      running(application) {
        val request = FakeRequest(GET, partnerAddEmailAddressYesNoPageRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PartnerAddEmailAddressYesNoPageView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(PartnerAddEmailAddressYesNoPagePage(index), true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, partnerAddEmailAddressYesNoPageRoute)

        val view = application.injector.instanceOf[PartnerAddEmailAddressYesNoPageView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      def onwardRoute: Call = Call("GET", "/foo")

      val application =
        applicationBuilder(userAnswers = baseUserAnswers)
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, partnerAddEmailAddressYesNoPageRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = baseUserAnswers).build()

      running(application) {
        val request =
          FakeRequest(POST, partnerAddEmailAddressYesNoPageRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[PartnerAddEmailAddressYesNoPageView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

  }
}
