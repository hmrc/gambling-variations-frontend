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
import models.BusinessTradeClass
import pages.*
import play.api.test.FakeRequest
import play.api.test.Helpers.*

class CheckTradingDetailsControllerSpec extends SpecBase {

  private val filledUserAnswers =
    emptyUserAnswers
      .set(MgdTradeDetailsSectionPage, "MGD999999")
      .success
      .value
      .set(GroupMemberPage, false)
      .success
      .value
      .set(BusinessTradeClassPage, BusinessTradeClass.Casino)
      .success
      .value
      .set(IsSeasonalBusinessPage, true)
      .success
      .value
      .set(PreviousRegistrationNumbersPage, Seq("MGD123", "MGD456"))
      .success
      .value
      .set(AssociatedRegistrationNumbersPage, Seq("ASS789"))
      .success
      .value

  "CheckTradingDetailsController" - {

    "must return OK for a GET request" in {

      val application =
        applicationBuilder(userAnswers = Some(filledUserAnswers)).build()

      running(application) {

        val request =
          FakeRequest(GET, routes.CheckTradingDetailsController.onPageLoad().url)

        val result =
          route(application, request).value

        status(result) mustEqual OK
      }
    }

    "must show trade class section when user is NOT a group member" in {

      val application =
        applicationBuilder(userAnswers = Some(filledUserAnswers)).build()

      running(application) {

        val request =
          FakeRequest(GET, routes.CheckTradingDetailsController.onPageLoad().url)

        val content =
          contentAsString(route(application, request).value)

        content must include("Trade class")
        content must include("Casino")
      }
    }

    "must show all sections when user is NOT a group member" in {

      val application =
        applicationBuilder(userAnswers = Some(filledUserAnswers)).build()

      running(application) {

        val request =
          FakeRequest(GET, routes.CheckTradingDetailsController.onPageLoad().url)

        val content =
          contentAsString(route(application, request).value)

        content must include("Trade class")
        content must include("Seasonal business")
        content must include("Yes")

        content must include("Previous MGD registration numbers")
        content must include("MGD123")
        content must include("MGD456")

        content must include("Associated MGD registration numbers")
        content must include("ASS789")
      }
    }

    "must hide trade class and MGD registration sections when user IS a group member" in {

      val groupMemberUserAnswers =
        filledUserAnswers
          .set(GroupMemberPage, true)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(groupMemberUserAnswers)).build()

      running(application) {

        val request =
          FakeRequest(GET, routes.CheckTradingDetailsController.onPageLoad().url)

        val content =
          contentAsString(route(application, request).value)

        content must not include "Trade class"
        content must not include "Description of business activity"
        content must not include "Casino"

        content must not include "Previous MGD registration numbers"
        content must not include "MGD123"
        content must not include "MGD456"

        content must not include "Associated MGD registration numbers"
        content must not include "ASS789"

        content must include("Seasonal business")
      }
    }
  }
}
