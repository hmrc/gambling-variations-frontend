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

package controllers.businessname

import base.SpecBase
import controllers.routes
import models.BusinessType.Soleproprietor
import models.{BusinessType, SoleProprietorName}
import pages.*
import pages.businessname.{BusinessNamePage, SoleProprietorPage}
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.businessname.BusinessNameView

class CheckBusinessNameControllerSpec extends SpecBase {

  "BusinessName Controller" - {

    val noAnswers = LoadedBusinessName(userAnswersId, Json.obj())

    "must return OK and the correct view for a GET" - {

      "with the expected URL" in {
        controllers.businessname.routes.CheckBusinessNameController.onPageLoad().url must endWith("/change-registration-details/business-name/check")
      }

      "when sole proprietor" in {

        val userAnswers =
          LoadedBusinessName(userAnswersId, Json.obj())
            .set(GroupMemberPage, false)
            .success
            .value
            .setOriginal(
              SoleProprietorPage,
              SoleProprietorName(
                "Mr",
                "Test",
                None,
                "Fella"
              )
            )
            .success
            .value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, controllers.businessname.routes.CheckBusinessNameController.onPageLoad().url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[BusinessNameView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual
            view(
              Soleproprietor,
              "Mr Test Fella",
              None,
              false
            )(request, messages(application)).toString
        }
      }

      "when partnership" in {

        val userAnswers =
          LoadedBusinessName(userAnswersId, Json.obj())
            .set(GroupMemberPage, false)
            .success
            .value
            .setOriginal(BusinessNamePage, "Test Business Ltd")
            .success
            .value
            .setOriginal(BusinessTypePage, BusinessType.Partnership)
            .success
            .value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, controllers.businessname.routes.CheckBusinessNameController.onPageLoad().url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[BusinessNameView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual
            view(
              BusinessType.Partnership,
              "Test Business Ltd",
              None,
              false
            )(request, messages(application)).toString
        }
      }
    }

    "must redirect to access denied when group member is true" in {

      val userAnswers =
        LoadedBusinessName(userAnswersId, Json.obj())
          .set(GroupMemberPage, true)
          .success
          .value
          .setOriginal(BusinessNamePage, "Test Business Ltd")
          .success
          .value
          .setOriginal(BusinessTypePage, BusinessType.Partnership)
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.businessname.routes.CheckBusinessNameController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.AccessDeniedController.onPageLoad().url
      }
    }

    "must redirect with an empty set of User Answers" in {

      val application = applicationBuilder(userAnswers = Some(noAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.businessname.routes.CheckBusinessNameController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.SystemErrorController.onPageLoad().url
      }
    }
  }
}
