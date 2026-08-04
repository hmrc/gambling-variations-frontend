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
import models.{Address, ContactNumber, NormalMode, UserAnswers}
import pages.*
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.*

class CheckCorrespondenceDetailsControllerSpec extends SpecBase {

  private val baseAnswers =
    UserAnswers(
      userAnswersId,
      Json.obj(
        "correspondenceDetailsSection" -> Json.obj("mgdRegNum" -> mgdRegNum)
      )
    )

  lazy val checkCorrespondenceDetailsRoute =
    routes.CheckCorrespondenceDetailsController.onPageLoad().url

  "CheckCorrespondenceDetailsController" - {

    "must redirect to Add Corresponding Details when no correspondence details exist" in {

      val application =
        applicationBuilder(userAnswers = Some(baseAnswers)).build()

      running(application) {

        val request =
          FakeRequest(GET, checkCorrespondenceDetailsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.AddCorrespondingDetailsYesNoController
            .onPageLoad(NormalMode)
            .url
      }
    }

    "must return OK when correspondence name exists" in {

      val userAnswers =
        baseAnswers
          .set(CorrespondenceNamePage, "Test Name")
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {

        val request =
          FakeRequest(GET, checkCorrespondenceDetailsRoute)

        val result = route(application, request).value

        status(result) mustEqual OK
      }
    }

    "must return OK when correspondence additional name exists" in {

      val userAnswers =
        baseAnswers
          .set(CorrespondenceAdditionalNamePage, "Additional Name")
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {

        val request =
          FakeRequest(GET, checkCorrespondenceDetailsRoute)

        val result = route(application, request).value

        status(result) mustEqual OK
      }
    }

    "must return OK when correspondence additional information exists" in {

      val userAnswers =
        baseAnswers
          .set(CorrespondenceAdditionalInformationPage, "Additional Information")
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {

        val request =
          FakeRequest(GET, checkCorrespondenceDetailsRoute)

        val result = route(application, request).value

        status(result) mustEqual OK
      }
    }

    "must return OK when correspondence fax number exists" in {

      val userAnswers =
        baseAnswers
          .set(CorrespondenceFaxNumberPage, "01234567890")
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {

        val request =
          FakeRequest(GET, checkCorrespondenceDetailsRoute)

        val result = route(application, request).value

        status(result) mustEqual OK
      }
    }

    "must return OK when correspondence email exists" in {

      val userAnswers =
        baseAnswers
          .set(CorrespondenceEmailPage, "test@test.com")
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {

        val request =
          FakeRequest(GET, checkCorrespondenceDetailsRoute)

        val result = route(application, request).value

        status(result) mustEqual OK
      }
    }

    "must return OK when IOM or Channel Island flag exists" in {

      val userAnswers =
        baseAnswers
          .set(isleMOrChannelFlagPage, "true")
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {

        val request =
          FakeRequest(GET, checkCorrespondenceDetailsRoute)

        val result = route(application, request).value

        status(result) mustEqual OK
      }
    }

    "must return OK when correspondence UK address exists" in {

      val userAnswers =
        baseAnswers
          .set(
            CorrespondenceAddressUkPage,
            Address(
              address1 = "Line 1",
              address2 = Some("Line 2"),
              address3 = Some("Line 3"),
              address4 = None,
              postcode = Some("AA1 1AA"),
              country  = None
            )
          )
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {

        val request =
          FakeRequest(GET, checkCorrespondenceDetailsRoute)

        val result = route(application, request).value

        status(result) mustEqual OK
      }
    }

    "must return OK when correspondence non UK address exists" in {

      val userAnswers =
        baseAnswers
          .set(
            CorrespondenceAddressNonUkPage,
            Address(
              address1 = "Line 1",
              address2 = Some("Line 2"),
              address3 = Some("Line 3"),
              address4 = None,
              postcode = None,
              country  = Some("France")
            )
          )
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {

        val request =
          FakeRequest(GET, checkCorrespondenceDetailsRoute)

        val result = route(application, request).value

        status(result) mustEqual OK
      }
    }

    "must return OK when correspondence phone number exists" in {

      val userAnswers =
        baseAnswers
          .set(
            CorrespondenceContactNumberPage,
            ContactNumber(
              phoneNumber       = Some("01234567890"),
              mobilePhoneNumber = None
            )
          )
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {

        val request =
          FakeRequest(GET, checkCorrespondenceDetailsRoute)

        val result = route(application, request).value

        status(result) mustEqual OK
      }
    }

    "must return OK when correspondence mobile phone number exists" in {

      val userAnswers =
        baseAnswers
          .set(
            CorrespondenceContactNumberPage,
            ContactNumber(
              phoneNumber       = None,
              mobilePhoneNumber = Some("07123456789")
            )
          )
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {

        val request =
          FakeRequest(GET, checkCorrespondenceDetailsRoute)

        val result = route(application, request).value

        status(result) mustEqual OK
      }
    }

    "must redirect to Add Corresponding Details when contact number exists but phone and mobile are empty" in {

      val userAnswers =
        baseAnswers
          .set(
            CorrespondenceContactNumberPage,
            ContactNumber(
              phoneNumber       = None,
              mobilePhoneNumber = None
            )
          )
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {

        val request =
          FakeRequest(GET, checkCorrespondenceDetailsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.AddCorrespondingDetailsYesNoController
            .onPageLoad(NormalMode)
            .url
      }
    }

    "must return OK when correspondence details have been submitted and changed" in {

      val userAnswers =
        baseAnswers
          .set(CorrespondenceNamePage, "Test Name")
          .success
          .value
          .set(CorrespondenceDetailsSubmittedPage, true)
          .success
          .value
          .set(CorrespondenceDetailsChangesPage, true)
          .success
          .value

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {

        val request =
          FakeRequest(GET, checkCorrespondenceDetailsRoute)

        val result = route(application, request).value

        status(result) mustEqual OK
      }
    }

    "must redirect to SystemErrorController when no session exists" in {

      val application =
        applicationBuilder(userAnswers = None).build()

      running(application) {

        val request =
          FakeRequest(GET, checkCorrespondenceDetailsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.SystemErrorController.onPageLoad().url
      }
    }
  }
}
