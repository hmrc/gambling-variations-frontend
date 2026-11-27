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
import forms.cancellation.RegistrationCancellationWarningFormProvider
import models.{BusinessType, UserAnswers}
import org.scalatestplus.mockito.MockitoSugar
import pages.cancellation.{CancelRegistrationDatePage, RegistrationCancellationCancelAgainYesNoPage, RegistrationCancellationDeclarationPage}
import pages.{BusinessTypePage, DateOfRegistrationPage, GroupMemberPage}
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
  private val nonGroupMessageKey: String = "cancelRegistrationWarning"
  private val groupMessageKey: String = "cancelGroupRegistrationWarning"
  private val nonGroupFlag: Boolean = false
  private val groupFlag: Boolean = true

  private val formProvider = new RegistrationCancellationWarningFormProvider()

  private def form = formProvider()

  private lazy val registrationCancellationWarningRouteNonGroup =
    controllers.cancellation.routes.RegistrationCancellationWarningController.onPageLoad().url

  private lazy val registrationCancellationWarningRouteGroup =
    controllers.cancellation.routes.RegistrationCancellationWarningController.onPageLoadGroup().url

  override val emptyUserAnswers: UserAnswers = UserAnswers(userAnswersId)
    .setOriginal(RegistrationCancellationDeclarationPage, currentDate)
    .success
    .value
    .setOriginal(DateOfRegistrationPage, currentDate)
    .success
    .value
    .setOriginal(CancelRegistrationDatePage, currentDate)
    .success
    .value
    .setOriginal(GroupMemberPage, nonGroupFlag)
    .success
    .value
    .setOriginal(BusinessTypePage, BusinessType.Soleproprietor)
    .success
    .value

  val emptyUserAnswersGroup: UserAnswers = emptyUserAnswers
    .setOriginal(GroupMemberPage, groupFlag)
    .success
    .value

  private def getRequestNonGroup(): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, registrationCancellationWarningRouteNonGroup)

  private def getRequestGroup(): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, registrationCancellationWarningRouteGroup)

  "RegistrationCancellationWarning Controller" - {

    "nonGroup" - {

      "onPageLoad" - {

        "must return OK and the correct view for a GET" in {

          val application = applicationBuilder(Some(emptyUserAnswers)).build()

          running(application) {
            val request = getRequestNonGroup()

            val result = route(application, request).value

            val view = application.injector.instanceOf[RegistrationCancellationWarningView]

            status(result) mustEqual OK
            contentAsString(result) mustEqual
              view(form, currentDateFormatted, nonGroupMessageKey)(request, messages(application)).toString
          }
        }

        "must redirect when CancelRegistrationDate is not present" in {
          val userAnswers = emptyUserAnswers.remove(RegistrationCancellationDeclarationPage).success.value

          val application = applicationBuilder(Some(userAnswers)).build()

          running(application) {
            val result = route(application, getRequestNonGroup()).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual
              controllers.cancellation.routes.CancelRegistrationDateController.onPageLoad().url
          }
        }

        "must populate the view when the question has previously been answered" in {

          val userAnswers = emptyUserAnswers.set(RegistrationCancellationCancelAgainYesNoPage, true).success.value

          val application = applicationBuilder(Some(userAnswers)).build()

          running(application) {
            val request = getRequestNonGroup()

            val view = application.injector.instanceOf[RegistrationCancellationWarningView]

            val result = route(application, getRequestNonGroup()).value

            status(result) mustEqual OK
            contentAsString(result) mustEqual
              view(form.fill(true), currentDateFormatted, nonGroupMessageKey)(request, messages(application)).toString
          }
        }

        "must redirect when the registration cancellation value does not exist" in {

          val userAnswers = emptyUserAnswers.remove(RegistrationCancellationDeclarationPage).success.value

          val application = applicationBuilder(Some(userAnswers)).build()

          running(application) {
            val result = route(application, getRequestNonGroup()).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual
              controllers.cancellation.routes.CancelRegistrationDateController.onPageLoad().url
          }
        }

        "must redirect when the registration cancellation value is false" in {

          val userAnswers = emptyUserAnswers.remove(RegistrationCancellationDeclarationPage).success.value

          val application = applicationBuilder(Some(userAnswers)).build()

          running(application) {
            val result = route(application, getRequestNonGroup()).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual
              controllers.cancellation.routes.CancelRegistrationDateController.onPageLoad().url
          }
        }
      }

      "onSubmit" - {

        "must return a Ok when valid data is submitted" in {
          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

          running(application) {
            val request =
              FakeRequest(POST, registrationCancellationWarningRouteNonGroup)
                .withFormUrlEncodedBody(("value", "true"))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
          }
        }

        "must redirect to SystemError for a GET if CancelRegistrationDate is not found" in {

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.remove(GroupMemberPage).success.value)).build()

          running(application) {
            val request = FakeRequest(GET, registrationCancellationWarningRouteNonGroup)

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.routes.SystemErrorController.onPageLoad().url
          }
        }

        "must return a Bad Request and errors when invalid data is submitted" in {

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

          running(application) {
            val request =
              FakeRequest(POST, registrationCancellationWarningRouteNonGroup)
                .withFormUrlEncodedBody(("invalid value", "invalid value"))

            val boundForm = form.bind(Map("invalid value" -> "invalid value"))

            val view = application.injector.instanceOf[RegistrationCancellationWarningView]

            val result = route(application, request).value

            status(result) mustEqual BAD_REQUEST
            contentAsString(result) mustEqual view(boundForm, currentDateFormatted, nonGroupMessageKey)(request, messages(application)).toString
          }
        }

        "must redirect to SystemError for a GET if no existing data is found" in {

          val application = applicationBuilder(userAnswers = None).build()

          running(application) {
            val request = FakeRequest(GET, registrationCancellationWarningRouteNonGroup)

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.routes.SystemErrorController.onPageLoad().url
          }
        }

        "must redirect to SystemError for a POST if no existing data is found" in {

          val application = applicationBuilder(userAnswers = None).build()

          running(application) {
            val request =
              FakeRequest(POST, registrationCancellationWarningRouteNonGroup)
                .withFormUrlEncodedBody(("value", "answer"))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.routes.SystemErrorController.onPageLoad().url
          }
        }

      }
    }

    "group" - {

      "onPageLoad" - {

        "must return OK and the correct view for a GET" in {

          val application = applicationBuilder(Some(emptyUserAnswersGroup)).build()

          running(application) {
            val request = getRequestGroup()

            val result = route(application, request).value

            val view = application.injector.instanceOf[RegistrationCancellationWarningView]

            status(result) mustEqual OK
            contentAsString(result) mustEqual
              view(form, currentDateFormatted, groupMessageKey)(request, messages(application)).toString
          }
        }

        "must redirect when CancelRegistrationDate is not present" in {
          val userAnswers = emptyUserAnswersGroup.remove(RegistrationCancellationDeclarationPage).success.value

          val application = applicationBuilder(Some(userAnswers)).build()

          running(application) {
            val result = route(application, getRequestGroup()).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual
              controllers.cancellation.routes.CancelRegistrationDateController.onPageLoadGroup().url
          }
        }

        "must populate the view when the question has previously been answered" in {

          val userAnswers = emptyUserAnswersGroup.set(RegistrationCancellationCancelAgainYesNoPage, true).success.value

          val application = applicationBuilder(Some(userAnswers)).build()

          running(application) {
            val request = getRequestGroup()

            val view = application.injector.instanceOf[RegistrationCancellationWarningView]

            val result = route(application, getRequestGroup()).value

            status(result) mustEqual OK
            contentAsString(result) mustEqual
              view(form.fill(true), currentDateFormatted, groupMessageKey)(request, messages(application)).toString
          }
        }

        "must redirect when the registration cancellation value does not exist" in {

          val userAnswers = emptyUserAnswersGroup.remove(RegistrationCancellationDeclarationPage).success.value

          val application = applicationBuilder(Some(userAnswers)).build()

          running(application) {
            val result = route(application, getRequestGroup()).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual
              controllers.cancellation.routes.CancelRegistrationDateController.onPageLoadGroup().url
          }
        }
      }

      "onSubmit" - {

        "must return a Ok when valid data is submitted" in {
          val application = applicationBuilder(userAnswers = Some(emptyUserAnswersGroup)).build()

          running(application) {
            val request =
              FakeRequest(POST, registrationCancellationWarningRouteGroup)
                .withFormUrlEncodedBody(("value", "true"))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
          }
        }

        "must redirect to SystemError for a GET if CancelRegistrationDate is not found" in {

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswersGroup.remove(GroupMemberPage).success.value)).build()

          running(application) {
            val request = FakeRequest(GET, registrationCancellationWarningRouteGroup)

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.routes.SystemErrorController.onPageLoad().url
          }
        }

        "must return a Bad Request and errors when invalid data is submitted" in {

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswersGroup)).build()

          running(application) {
            val request =
              FakeRequest(POST, registrationCancellationWarningRouteGroup)
                .withFormUrlEncodedBody(("invalid value", "invalid value"))

            val boundForm = form.bind(Map("invalid value" -> "invalid value"))

            val view = application.injector.instanceOf[RegistrationCancellationWarningView]

            val result = route(application, request).value

            status(result) mustEqual BAD_REQUEST
            contentAsString(result) mustEqual view(boundForm, currentDateFormatted, groupMessageKey)(request, messages(application)).toString
          }
        }

        "must redirect to SystemError for a GET if no existing data is found" in {

          val application = applicationBuilder(userAnswers = None).build()

          running(application) {
            val request = FakeRequest(GET, registrationCancellationWarningRouteGroup)

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.routes.SystemErrorController.onPageLoad().url
          }
        }

        "must redirect to SystemError for a POST if no existing data is found" in {

          val application = applicationBuilder(userAnswers = None).build()

          running(application) {
            val request =
              FakeRequest(POST, registrationCancellationWarningRouteGroup)
                .withFormUrlEncodedBody(("value", "answer"))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.routes.SystemErrorController.onPageLoad().url
          }
        }

      }
    }

  }
}
