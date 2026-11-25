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
import pages.{BusinessTypePage, DateOfRegistrationPage, GroupMemberPage}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.cancellation.GroupRegistrationCancellationWarningView

import java.time.LocalDate

//TODO move to another class
class GroupRegistrationCancellationWarningControllerSpec extends SpecBase {

  val messageKey: String = "cancelGroupRegistrationWarning"
  val isGroup: Boolean = true

  override val emptyUserAnswers: UserAnswers =
    super.emptyUserAnswers
      .set(DateOfRegistrationPage, LocalDate.now())
      .success
      .value
      .set(GroupMemberPage, isGroup)
      .success
      .value

  "GroupRegistrationCancellationWarning Controller" - {

    "must return OK and the correct view for a GET" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {

        val request =
          FakeRequest(
            GET,
            routes.RegistrationCancellationWarningController.onPageLoad().url
          )

        val result = route(application, request).value

        val view =
          application.injector.instanceOf[GroupRegistrationCancellationWarningView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view("http://localhost:10400/manage-gambling-tax/manage-your-machine-games-duty/")(
            request,
            messages(application)
          ).toString
      }
    }
  }
}
