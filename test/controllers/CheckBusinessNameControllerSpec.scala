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
import views.html.BusinessNameView

class CheckBusinessNameControllerSpec extends SpecBase {

  "BusinessName Controller" - {

    "must return OK and the correct view for a GET" - {
      "when sole proprietor" in {

        val data = Json.obj(
          "soleProprietor" -> Json.obj(
            "title"     -> "Mr",
            "firstName" -> "Test",
            "lastName"  -> "Fella"
          )
        )

        val userAnswers = UserAnswers("id", data)

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, routes.CheckBusinessNameController.onPageLoad().url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[BusinessNameView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view("soleproprietor", "Mr Test Fella", None)(request, messages(application)).toString
        }
      }
      "when partnership" in {

        val data = Json.obj(
          "businessName" -> "Test Business Ltd",
          "businessType" -> 4
        )

        val userAnswers = UserAnswers("id", data)

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, routes.CheckBusinessNameController.onPageLoad().url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[BusinessNameView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view("partnership", "Test Business Ltd", None)(request, messages(application)).toString
        }
      }
    }

    "must redirect with an empty set of User Answers" in {

      val application = applicationBuilder(userAnswers = Some(UserAnswers(userAnswersId))).build()

      running(application) {
        val request = FakeRequest(GET, routes.CheckBusinessNameController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.SystemErrorController.onPageLoad().url
      }
    }

  }

}
