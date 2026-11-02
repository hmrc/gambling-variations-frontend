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
import models.cancellation.ReasonForCancellation
import pages.cancellation.{CancelRegistrationDatePage, ChangeGroupRegistrationNumberPage, ReasonForCancellationPage}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import viewmodels.checkAnswers.cancellation.CheckRegistrationCancellationDetailsViewModel
import views.html.cancellation.CheckRegistrationCancellationDetailsView

import java.time.LocalDate

class CheckRegistrationCancellationDetailsControllerSpec extends SpecBase {

  "CheckRegistrationCancellationDetailsController" - {

    "must return OK and the correct view for a GET" in {

      val userAnswers =
        emptyUserAnswers
          .set(CancelRegistrationDatePage, LocalDate.of(2026, 2, 2))
          .success
          .value
          .set(
            ReasonForCancellationPage,
            ReasonForCancellation.BusinessIsPartOfTheGroup
          )
          .success
          .value
          .set(
            ChangeGroupRegistrationNumberPage,
            "XWM000000001770"
          )
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {

        val request =
          FakeRequest(
            GET,
            controllers.cancellation.routes.CheckRegistrationCancellationDetailsController
              .onPageLoad()
              .url
          )

        val result = route(application, request).value

        val view =
          application.injector
            .instanceOf[CheckRegistrationCancellationDetailsView]

        implicit val msgs = messages(application)

        val expectedSummaryList =
          CheckRegistrationCancellationDetailsViewModel.summaryList(userAnswers)

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(expectedSummaryList)(request, msgs).toString
      }
    }
  }
}
