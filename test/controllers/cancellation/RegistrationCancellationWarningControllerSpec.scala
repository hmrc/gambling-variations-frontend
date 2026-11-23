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

package controllers.cancellation

import base.SpecBase
import controllers.routes
import forms.cancellation.RegistrationCancellationWarningFormProvider
import models.{BusinessType, NormalMode, UserAnswers}
import org.scalatestplus.mockito.MockitoSugar
import pages.cancellation.{RegistrationCancellationCancelAgainYesNoPage, RegistrationCancellationDatePage, RegistrationCancellationDeclarationPage}
import pages.{BusinessTypePage, GroupMemberPage}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.cancellation.RegistrationCancellationWarningView

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class RegistrationCancellationWarningControllerSpec extends SpecBase with MockitoSugar {

  private val dateFormatter =
    DateTimeFormatter.ofPattern("d MMMM yyyy")

  private val currentDate = LocalDate.of(2026, 11, 3)

  private val currentDateFormatted = dateFormatter.format(currentDate)
  private val isGroupFlag: Boolean = false

  private val formProvider = new RegistrationCancellationWarningFormProvider()

  private def form = formProvider()

  private lazy val registrationCancellationWarningRoute =
    controllers.cancellation.routes.RegistrationCancellationWarningController.onPageLoad().url

  override val emptyUserAnswers: UserAnswers = UserAnswers(userAnswersId)
    .setOriginal(RegistrationCancellationDeclarationPage, true)
    .success
    .value
    .setOriginal(RegistrationCancellationDatePage, currentDate)
    .success
    .value
    .setOriginal(GroupMemberPage, isGroupFlag)
    .success
    .value
    .setOriginal(BusinessTypePage, BusinessType.Soleproprietor)
    .success
    .value

  private def getRequest(): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, registrationCancellationWarningRoute)

  "RegistrationCancellationWarning Controller" - {

    "onPageLoad" - {

      "must return OK and the correct view for a GET" in {

        val application = applicationBuilder(Some(emptyUserAnswers)).build()

        running(application) {
          val request = getRequest()

          val result = route(application, request).value

          val view = application.injector.instanceOf[RegistrationCancellationWarningView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual
            view(form, currentDateFormatted, isGroupFlag, NormalMode)(request, messages(application)).toString
        }
      }

      "must populate the view when the question has previously been answered" in {

        val userAnswers = emptyUserAnswers.set(RegistrationCancellationCancelAgainYesNoPage, true).success.value

        val application = applicationBuilder(Some(userAnswers)).build()

        running(application) {
          val request = getRequest()

          val view = application.injector.instanceOf[RegistrationCancellationWarningView]

          val result = route(application, getRequest()).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual
            view(form.fill(true), currentDateFormatted, isGroupFlag, NormalMode)(request, messages(application)).toString
        }
      }

      "must redirect when the registration cancellation value does not exist" in {

        val userAnswers = emptyUserAnswers.remove(RegistrationCancellationDeclarationPage).success.value

        val application = applicationBuilder(Some(userAnswers)).build()

        running(application) {
          val result = route(application, getRequest()).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.cancellation.routes.CancelRegistrationDateController.onPageLoad(NormalMode).url
        }
      }

      "must redirect when the registration cancellation value is false" in {

        val userAnswers = emptyUserAnswers.set(RegistrationCancellationDeclarationPage, false).success.value

        val application = applicationBuilder(Some(userAnswers)).build()

        running(application) {
          val result = route(application, getRequest()).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            controllers.cancellation.routes.CancelRegistrationDateController.onPageLoad(NormalMode).url
        }
      }
    }

    "onSubmit" - {

      "must return a Ok when valid data is submitted" in {
        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request =
            FakeRequest(POST, registrationCancellationWarningRoute)
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request =
            FakeRequest(POST, registrationCancellationWarningRoute)
              .withFormUrlEncodedBody(("invalid value", "invalid value"))

          val boundForm = form.bind(Map("invalid value" -> "invalid value"))

          val view = application.injector.instanceOf[RegistrationCancellationWarningView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, currentDateFormatted, isGroupFlag, NormalMode)(request, messages(application)).toString
        }
      }

      "must redirect to SystemError for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, registrationCancellationWarningRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.SystemErrorController.onPageLoad().url
        }
      }

      "must redirect to SystemError for a POST if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request =
            FakeRequest(POST, registrationCancellationWarningRoute)
              .withFormUrlEncodedBody(("value", "answer"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.SystemErrorController.onPageLoad().url
        }
      }

    }
  }
}
