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
import models.BusinessType
import pages.cancellation.CancelRegistrationDatePage
import pages.{BusinessTypePage, GroupMemberPage}
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import viewmodels.checkAnswers.cancellation.CheckGroupRegistrationCancellationDetailsViewModel
import views.html.cancellation.CheckGroupRegistrationCancellationDetailsView

import java.time.LocalDate

class CheckGroupRegistrationCancellationDetailsControllerSpec extends SpecBase {

  "CheckGroupRegistrationCancellationDetailsController" - {

    "must return OK and the correct view for a GET" in {

      val userAnswers =
        emptyUserAnswers
          .set(BusinessTypePage, BusinessType.Corporatebody)
          .success
          .value
          .set(GroupMemberPage, true)
          .success
          .value
          .set(CancelRegistrationDatePage, LocalDate.of(2026, 11, 20))
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(GET, routes.CheckGroupRegistrationCancellationDetailsController.onPageLoad().url)

        val result = route(application, request).value
        val view = application.injector.instanceOf[CheckGroupRegistrationCancellationDetailsView]

        given msgs: Messages = messages(application)

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(CheckGroupRegistrationCancellationDetailsViewModel.summaryList(userAnswers))(request, msgs).toString
      }
    }
  }
}
