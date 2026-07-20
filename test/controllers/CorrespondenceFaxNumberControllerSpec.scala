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
import forms.FaxNumberFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import pages.*
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.CorrespondenceFaxNumberView

class CorrespondenceFaxNumberControllerSpec extends SpecBase {

  private val formProvider = new FaxNumberFormProvider()
  private val form = formProvider("correspondenceFaxNumber")

  private val validAnswer = "01234567890"

  private val baseAnswers =
    emptyUserAnswers
      .set(CorrespondenceDetailsSectionPage, mgdRegNum)
      .success
      .value

  private lazy val onPageLoadRoute =
    routes.CorrespondenceFaxNumberController.onPageLoad(NormalMode).url

  private lazy val onSubmitRoute =
    routes.CorrespondenceFaxNumberController.onSubmit(NormalMode).url

  "CorrespondenceFaxNumberController" - {

    "GET onPageLoad" - {

      "must return OK and the correct view for a GET" in {

        val application =
          applicationBuilder(userAnswers = Some(baseAnswers)).build()

        running(application) {

          val request =
            FakeRequest(GET, onPageLoadRoute)

          val view =
            application.injector.instanceOf[CorrespondenceFaxNumberView]

          val result =
            route(application, request).value

          status(result) mustEqual OK

          Jsoup.parse(contentAsString(result)).text() mustEqual
            Jsoup
              .parse(
                view(form, NormalMode)(request, messages(application)).toString
              )
              .text()
        }
      }

      "must populate the view when the answer already exists" in {

        val userAnswers =
          baseAnswers
            .set(CorrespondenceFaxNumberPage, validAnswer)
            .success
            .value

        val application =
          applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {

          val request =
            FakeRequest(GET, onPageLoadRoute)

          val result =
            route(application, request).value

          status(result) mustEqual OK

          contentAsString(result) must include(validAnswer)
        }
      }

      "must redirect when no existing answers are found" in {

        val application =
          applicationBuilder(userAnswers = None).build()

        running(application) {

          val request =
            FakeRequest(GET, onPageLoadRoute)

          val result =
            route(application, request).value

          status(result) mustEqual SEE_OTHER
        }
      }
    }

    "POST onSubmit" - {

      "must redirect to Check Correspondence Details when valid data is submitted" in {

        val application =
          applicationBuilder(userAnswers = Some(baseAnswers)).build()

        running(application) {

          val request =
            FakeRequest(POST, onSubmitRoute)
              .withFormUrlEncodedBody(
                "faxNumber" -> validAnswer
              )

          val result =
            route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            routes.CheckCorrespondenceDetailsController.onPageLoad().url
        }
      }

      "must return BAD_REQUEST when invalid data is submitted" in {

        val application =
          applicationBuilder(userAnswers = Some(baseAnswers)).build()

        running(application) {

          val request =
            FakeRequest(POST, onSubmitRoute)
              .withFormUrlEncodedBody(
                "faxNumber" -> ""
              )

          val result =
            route(application, request).value

          status(result) mustEqual BAD_REQUEST
        }
      }

      "must redirect to Check Correspondence Details when answer changes from existing value" in {

        val userAnswers =
          baseAnswers
            .set(CorrespondenceFaxNumberPage, "00000000000")
            .success
            .value

        val application =
          applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {

          val request =
            FakeRequest(POST, onSubmitRoute)
              .withFormUrlEncodedBody(
                "faxNumber" -> validAnswer
              )

          val result =
            route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            routes.CheckCorrespondenceDetailsController.onPageLoad().url
        }
      }

      "must redirect to Check Correspondence Details when answer remains unchanged" in {

        val userAnswers =
          baseAnswers
            .set(CorrespondenceFaxNumberPage, validAnswer)
            .success
            .value

        val application =
          applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {

          val request =
            FakeRequest(POST, onSubmitRoute)
              .withFormUrlEncodedBody(
                "faxNumber" -> validAnswer
              )

          val result =
            route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            routes.CheckCorrespondenceDetailsController.onPageLoad().url
        }
      }

      "must redirect when no existing answers are found" in {

        val application =
          applicationBuilder(userAnswers = None).build()

        running(application) {

          val request =
            FakeRequest(POST, onSubmitRoute)
              .withFormUrlEncodedBody(
                "faxNumber" -> validAnswer
              )

          val result =
            route(application, request).value

          status(result) mustEqual SEE_OTHER
        }
      }
    }
  }
}
