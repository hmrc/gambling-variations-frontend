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

package controllers

import base.SpecBase
import models.UserAnswers
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.CheckContactDetailsView
import pages.GroupMemberPage

class CheckContactDetailsControllerSpec extends SpecBase {

  "CheckContactDetails Controller" - {

    "must redirect to access denied when group member is true" in {

      val data = Json.obj(
        "businessContactDetailsSection" -> Json.obj("mgdRegNum" -> mgdRegNum),
        "businessContactNumber" -> Json.obj(
          "phoneNumber"       -> "07000000000",
          "mobilePhoneNumber" -> "07000000000"
        ),
        "businessFaxNumber"      -> "07000000000",
        "businessEmailAddress"   -> "a@b.com",
        GroupMemberPage.toString -> true
      )

      val userAnswers = UserAnswers("id-number", data)

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(GET, routes.CheckContactDetailsController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          routes.AccessDeniedController.onPageLoad().url
      }
    }

    "must return OK and the correct view for a GET" in {

      val data = Json.obj(
        "businessContactDetailsSection" -> Json.obj("mgdRegNum" -> mgdRegNum),
        "businessContactNumber"         -> Json.obj("phoneNumber" -> "07000000000", "mobilePhoneNumber" -> "07000000000"),
        "businessFaxNumber"             -> "07000000000",
        "businessEmailAddress"          -> "a@b.com",
        GroupMemberPage.toString        -> false,
        "flag"                          -> false
      )

      val userAnswers = UserAnswers("id-number", data)
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.CheckContactDetailsController.onPageLoad().url)

        val result = route(application, request).value
        val view = application.injector.instanceOf[CheckContactDetailsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(Some("07000000000"), Some("07000000000"), Some("07000000000"), Some("a@b.com"), false)(
          request,
          messages(application)
        ).toString
      }
    }

    "must return SystemErrorController when GroupMemberPage is empty" in {

      val data = Json.obj(
        "businessContactDetailsSection" -> Json.obj("mgdRegNum" -> mgdRegNum),
        "businessContactNumber" -> Json.obj(
          "phoneNumber" -> "07000000000",
          "mobilePhoneNumber" -> "07000000000"
        ),
        "businessFaxNumber" -> "07000000000",
        "businessEmailAddress" -> "a@b.com",
      )

      val userAnswers = UserAnswers("id-number", data)

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(GET, routes.CheckContactDetailsController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          routes.SystemErrorController.onPageLoad().url
      }
    }

  }
}
