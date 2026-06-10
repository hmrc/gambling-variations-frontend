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
import models.BusinessType
import org.scalatestplus.mockito.MockitoSugar
import pages.{BusinessNameChangesPage, BusinessTypePage, GroupMemberPage}
import play.api.test.FakeRequest
import play.api.test.Helpers.*

class ChangeRegistrationDetailsControllerSpec extends SpecBase with MockitoSugar {

  "ChangeRegistrationDetailsController" - {

    "must return OK and render the view for a partnership business" in {

      val userAnswers =
        emptyUserAnswers
          .set(GroupMemberPage, false)
          .success
          .value
          .set(BusinessTypePage, BusinessType.Partnership)
          .success
          .value
          .set(BusinessNameChangesPage, true)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {

        val request =
          FakeRequest(GET, routes.ChangeRegistrationDetailsController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual OK
      }
    }

    "must return OK and render view for a group member" in {

      val userAnswers =
        emptyUserAnswers
          .set(GroupMemberPage, true)
          .success
          .value
          .set(BusinessTypePage, BusinessType.Partnership)
          .success
          .value
          .set(BusinessNameChangesPage, false)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {

        val request =
          FakeRequest(GET, routes.ChangeRegistrationDetailsController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual OK
      }
    }

    "must return OK for non-partnership business type" in {

      val userAnswers =
        emptyUserAnswers
          .set(GroupMemberPage, false)
          .success
          .value
          .set(BusinessTypePage, BusinessType.Partnership)
          .success
          .value
          .set(BusinessNameChangesPage, true)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {

        val request =
          FakeRequest(GET, routes.ChangeRegistrationDetailsController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual OK
      }
    }

    "must return OK when business name has not changed" in {

      val userAnswers =
        emptyUserAnswers
          .set(GroupMemberPage, false)
          .success
          .value
          .set(BusinessTypePage, BusinessType.Partnership)
          .success
          .value
          .set(BusinessNameChangesPage, false)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {

        val request =
          FakeRequest(GET, routes.ChangeRegistrationDetailsController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual OK
      }
    }

    "must redirect to SystemErrorController when GroupMemberPage is missing" in {

      val userAnswers =
        emptyUserAnswers
          .set(BusinessTypePage, BusinessType.Partnership)
          .success
          .value
          .set(BusinessNameChangesPage, true)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {

        val request =
          FakeRequest(GET, routes.ChangeRegistrationDetailsController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.SystemErrorController.onPageLoad().url
      }
    }
  }
}
