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
import models.{BusinessType, UserAnswers}
import pages.cancellation.RegistrationCancellationConfirmationReferencePage
import pages.{BusinessTypePage, GroupMemberPage}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.cancellation.RegistrationCancellationConfirmationView

class RegistrationCancellationConfirmationControllerSpec extends SpecBase {

  private val cancellationConfirmationRefNumber = "ABCDEFGH"

  override val emptyUserAnswers: UserAnswers =
    super.emptyUserAnswers
      .set(BusinessTypePage, BusinessType.Partnership)
      .success
      .value
      .set(GroupMemberPage, false)
      .success
      .value

  val userAnswersWithReferenceNumber: UserAnswers = emptyUserAnswers
    .set(RegistrationCancellationConfirmationReferencePage, cancellationConfirmationRefNumber)
    .success
    .value

  "RegistrationCancellationConfirmation Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersWithReferenceNumber)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.cancellation.routes.RegistrationCancellationConfirmationController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RegistrationCancellationConfirmationView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view("http://localhost:10400/gambling/", cancellationConfirmationRefNumber)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect when cancellationConfirmationRefNumber page is missing view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.cancellation.routes.RegistrationCancellationConfirmationController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
      }
    }
  }
}
