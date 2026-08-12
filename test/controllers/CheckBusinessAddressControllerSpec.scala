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
import models.{Address, NormalMode, UserAnswers}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import play.api.libs.json.Json
import views.html.BusinessAddressView

class CheckBusinessAddressControllerSpec extends SpecBase {

  "CheckBusinessAddress Controller" - {

    "must return OK and the correct view for a GET when address is present" in {

      val addressOnlyUa: UserAnswers = UserAnswers(
        "id",
        Json.obj(
          "businessAddressSection" -> Json.obj("mgdRegNum" -> "XMY1000002",
                                               "businessAddressUk" -> Address(
                                                 "abc",
                                                 Some("abc"),
                                                 Some("abc"),
                                                 Some("abc"),
                                                 Some("abc"),
                                                 Some("abc")
                                               )
                                              )
        )
      )

      val application = applicationBuilder(userAnswers = Some(addressOnlyUa)).build()

      running(application) {
        val request = FakeRequest(GET, routes.CheckBusinessAddressController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[BusinessAddressView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(ua = addressOnlyUa, mode = NormalMode, showChangeMessage = false)(request,
                                                                                                                 messages(application)
                                                                                                                ).toString
      }
    }

    "must redirect when address is not present" in {

      val regOnly = Json.obj(
        "businessAddressSection" -> Json.obj(
          "mgdRegNum" -> "XMY1000001"
        )
      )

      val basicAnswers = UserAnswers("id", regOnly)

      val application = applicationBuilder(userAnswers = Some(basicAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.CheckBusinessAddressController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[BusinessAddressView]

        status(result) mustEqual SEE_OTHER
      }
    }
    "must return SystemErrorController when Business Address section is empty" in {

      val data = Json.obj("businessContactDetailsSection" -> Json.obj())

      val userAnswers = UserAnswers("id-number", data)

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(GET, routes.CheckBusinessAddressController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          routes.SystemErrorController.onPageLoad().url
      }
    }
  }
}
