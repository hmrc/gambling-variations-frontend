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
import models.{BusinessType, RegistrationStatus}
import pages.{BusinessTypePage, GroupMemberPage, RegistrationStatusPage}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.cancellation.RegistrationCancellationLockedView

class RegistrationCancellationLockedControllerSpec extends SpecBase {

  "RegistrationCancellationLocked Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers =
        Some(
          emptyUserAnswers
            .setOriginal(RegistrationStatusPage, RegistrationStatus.Cancelled)
            .success
            .value
            .setOriginal(BusinessTypePage, BusinessType.Soleproprietor)
            .success
            .value
            .setOriginal(GroupMemberPage, true)
            .success
            .value
        )
      ).build()

      running(application) {
        val request = FakeRequest(GET, routes.RegistrationCancellationLockedController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RegistrationCancellationLockedView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view("http://localhost:10400/gambling/", "groupRegistrationCancellationLocked")(request,
                                                                                                                          messages(application)
                                                                                                                         ).toString
      }
    }

    "must redirect and show correct view for a GET when RegistrationStatus is not cancelled" in {

      val application = applicationBuilder(userAnswers =
        Some(
          emptyUserAnswers
            .setOriginal(RegistrationStatusPage, RegistrationStatus.Registered)
            .success
            .value
            .setOriginal(BusinessTypePage, BusinessType.Soleproprietor)
            .success
            .value
            .setOriginal(GroupMemberPage, true)
            .success
            .value
        )
      ).build()

      running(application) {
        val request = FakeRequest(GET, routes.RegistrationCancellationLockedController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.cancellation.routes.CancelRegistrationDateController.onPageLoad().url
      }
    }
  }
}
