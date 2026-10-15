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

package controllers.returnperiods

import base.SpecBase
import models.ChooseReturnPeriods.{Feb, Jan}
import models.{GamblingReturnPeriods, UserAnswers}
import pages.returnperiods.{ChooseReturnPeriodsPage, GamblingReturnPeriodsPage}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import viewmodels.checkAnswers.returnperiods.CheckReturnPeriodsViewModel
import views.html.returnperiods.CheckReturnPeriodsView

class CheckReturnPeriodsControllerSpec extends SpecBase {

  private lazy val checkReturnPeriodsRoute =
    controllers.returnperiods.routes.CheckReturnPeriodsController.onPageLoad().url

  private lazy val continueRoute =
    controllers.returnperiods.routes.CheckReturnPeriodsController.onContinue().url

  private val standardReturnPeriods =
    GamblingReturnPeriods(
      mgdRegNumber          = mgdRegNum,
      returnPeriodsId       = Some(Jan.returnPeriodsId),
      nstpEndDate1          = None,
      nstpEndDate2          = None,
      nstpEndDate3          = None,
      nstpEndDate4          = None,
      nstpEndDate5          = None,
      nstpEndDate6          = None,
      nstpEndDate7          = None,
      nstpEndDate8          = None,
      isInLastNstp          = None,
      finalPeriodWarning    = Some(false),
      hasExistingNstpValues = Some(false)
    )

  private def answersWith(returnPeriods: GamblingReturnPeriods): UserAnswers =
    UserAnswers(userAnswersId).set(GamblingReturnPeriodsPage, returnPeriods).success.value

  "CheckReturnPeriods Controller" - {

    "must return OK and the correct view for a GET with standard return periods" in {

      val application = applicationBuilder(userAnswers = Some(answersWith(standardReturnPeriods))).build()

      running(application) {
        val request = FakeRequest(GET, checkReturnPeriodsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckReturnPeriodsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(CheckReturnPeriodsViewModel(Some(Jan), showAppliesAfterCurrentPeriod = true))(request, messages(application)).toString
      }
    }

    "must show the return periods the user has chosen on a GET" in {

      val userAnswers = answersWith(standardReturnPeriods).set(ChooseReturnPeriodsPage, Feb.returnPeriodsId).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, checkReturnPeriodsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckReturnPeriodsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(CheckReturnPeriodsViewModel(Some(Feb), showAppliesAfterCurrentPeriod = true))(request, messages(application)).toString
      }
    }

    "must redirect to the change registration details page for a GET with non-standard return periods" in {

      val application =
        applicationBuilder(userAnswers = Some(answersWith(standardReturnPeriods.copy(hasExistingNstpValues = Some(true))))).build()

      running(application) {
        val request = FakeRequest(GET, checkReturnPeriodsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ChangeRegistrationDetailsController.onPageLoad().url
      }
    }

    "must return OK with an add return periods link for a GET when no return periods have been set" in {

      val application =
        applicationBuilder(userAnswers = Some(answersWith(standardReturnPeriods.copy(returnPeriodsId = None)))).build()

      running(application) {
        val request = FakeRequest(GET, checkReturnPeriodsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckReturnPeriodsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(CheckReturnPeriodsViewModel(None, showAppliesAfterCurrentPeriod = true))(request, messages(application)).toString
        contentAsString(result) must include("Add return periods ending")
      }
    }

    "must redirect to the change registration details page for a GET when the type of return periods is not known" in {

      val application =
        applicationBuilder(userAnswers = Some(answersWith(standardReturnPeriods.copy(hasExistingNstpValues = None)))).build()

      running(application) {
        val request = FakeRequest(GET, checkReturnPeriodsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ChangeRegistrationDetailsController.onPageLoad().url
      }
    }

    "must redirect to the change registration details page when Continue is selected" in {

      val application = applicationBuilder(userAnswers = Some(answersWith(standardReturnPeriods))).build()

      running(application) {
        val request = FakeRequest(POST, continueRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ChangeRegistrationDetailsController.onPageLoad().url
      }
    }
  }
}
