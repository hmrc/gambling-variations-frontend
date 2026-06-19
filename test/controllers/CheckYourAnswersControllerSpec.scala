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
import pages.{BusinessFaxNumberPage, IsSeasonalBusinessPage}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import viewmodels.checkAnswers.{FaxNumberSummary, SeasonalBusinessSummary}
import viewmodels.govuk.SummaryListFluency
import views.html.CheckYourAnswersView

class CheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency {

  "Check Your Answers Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckYourAnswersView]
        val list = SummaryListViewModel(Seq.empty)
        val continueUrl = routes.ChangeRegistrationDetailsController.onPageLoad().url

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list, continueUrl)(request, messages(application)).toString
      }
    }

    "must show the business fax number when it has been provided" in {

      val userAnswers = emptyUserAnswers.set(BusinessFaxNumberPage, "01632 960 001").success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckYourAnswersView]
        val list = SummaryListViewModel(
          Seq(FaxNumberSummary.row(userAnswers)(messages(application)).value)
        )
        val continueUrl = routes.ChangeRegistrationDetailsController.onPageLoad().url

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list, continueUrl)(request, messages(application)).toString
      }
    }

    "must show whether this is a seasonal business when it has been provided" in {

      val userAnswers = emptyUserAnswers.set(IsSeasonalBusinessPage, true).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckYourAnswersView]
        val list = SummaryListViewModel(
          Seq(SeasonalBusinessSummary.row(userAnswers)(messages(application)).value)
        )
        val continueUrl = routes.ChangeRegistrationDetailsController.onPageLoad().url

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list, continueUrl)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
