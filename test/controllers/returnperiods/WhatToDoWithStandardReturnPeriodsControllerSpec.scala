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
import controllers.returnperiods.WhatToDoWithStandardReturnPeriodsController
import forms.returnperiods.WhatToDoWithStandardReturnPeriodsFormProvider
import models.ChooseReturnPeriods.Jan
import models.{GamblingReturnPeriods, NormalMode, UserAnswers, WhatToDoWithStandardReturnPeriods}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.returnperiods.{GamblingReturnPeriodsPage, WhatToDoWithStandardReturnPeriodsPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.returnperiods.WhatToDoWithStandardReturnPeriodsView

import scala.concurrent.Future

class WhatToDoWithStandardReturnPeriodsControllerSpec extends SpecBase with MockitoSugar {

  private val onwardRoute =
    Call("GET", "/foo")

  private lazy val whatToDoWithStandardReturnPeriodsRoute =
    controllers.returnperiods.routes.WhatToDoWithStandardReturnPeriodsController
      .onPageLoad(NormalMode)
      .url

  private val formProvider =
    new WhatToDoWithStandardReturnPeriodsFormProvider()

  private val form =
    formProvider()

  private val switchToNonStandard =
    WhatToDoWithStandardReturnPeriods.Switchtononstandard

  private val changeMonthsStandardPeriodCover =
    WhatToDoWithStandardReturnPeriods.Changemonthsstandardperiodcover

  private val keepStandardReturnPeriod =
    WhatToDoWithStandardReturnPeriods.Keepstandardreturnperiod

  private val gamblingReturnPeriods =
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
      finalPeriodWarning    = None,
      hasExistingNstpValues = Some(false)
    )

  private val userAnswers =
    UserAnswers(userAnswersId)
      .set(
        GamblingReturnPeriodsPage,
        gamblingReturnPeriods
      )
      .success
      .value

  private val answeredUserAnswers =
    userAnswers
      .set(
        WhatToDoWithStandardReturnPeriodsPage,
        switchToNonStandard
      )
      .success
      .value

  private val returnPeriodsId =
    Some(Jan.returnPeriodsId.toString)

  private def controller(application: play.api.Application) =
    application.injector
      .instanceOf[WhatToDoWithStandardReturnPeriodsController]

  "WhatToDoWithStandardReturnPeriods Controller" - {

    "must return OK and the correct view for a GET" in {

      val application =
        applicationBuilder(
          userAnswers = Some(userAnswers)
        ).build()

      running(application) {

        val request =
          FakeRequest(
            GET,
            whatToDoWithStandardReturnPeriodsRoute
          )

        val result =
          controller(application)
            .onPageLoad(NormalMode)
            .apply(request)

        val view =
          application.injector
            .instanceOf[WhatToDoWithStandardReturnPeriodsView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(
            form,
            NormalMode,
            returnPeriodsId
          )(
            request,
            messages(application)
          ).toString
      }
    }

    "must populate the view when the question has previously been answered" in {

      val application =
        applicationBuilder(
          userAnswers = Some(answeredUserAnswers)
        ).build()

      running(application) {

        val request =
          FakeRequest(
            GET,
            whatToDoWithStandardReturnPeriodsRoute
          )

        val result =
          controller(application)
            .onPageLoad(NormalMode)
            .apply(request)

        val view =
          application.injector
            .instanceOf[WhatToDoWithStandardReturnPeriodsView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(
            form.fill(switchToNonStandard),
            NormalMode,
            returnPeriodsId
          )(
            request,
            messages(application)
          ).toString
      }
    }

    "must save the selected answer and redirect when Switchtononstandard is submitted" in {

      val mockSessionRepository =
        mock[SessionRepository]

      when(
        mockSessionRepository.set(any())
      ).thenReturn(
        Future.successful(true)
      )

      val application =
        applicationBuilder(
          userAnswers = Some(userAnswers)
        )
          .overrides(
            bind[Navigator].toInstance(
              new FakeNavigator(onwardRoute)
            ),
            bind[SessionRepository].toInstance(
              mockSessionRepository
            )
          )
          .build()

      running(application) {

        val request =
          FakeRequest(
            POST,
            whatToDoWithStandardReturnPeriodsRoute
          ).withFormUrlEncodedBody(
            "value" -> switchToNonStandard.toString
          )

        val result =
          controller(application)
            .onSubmit(NormalMode)
            .apply(request)

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual onwardRoute.url

        val expectedAnswers =
          userAnswers
            .set(
              WhatToDoWithStandardReturnPeriodsPage,
              switchToNonStandard
            )
            .success
            .value

        verify(mockSessionRepository).set(expectedAnswers)
      }
    }

    "must save the selected answer when ChangeMonthsStandardPeriodCover is submitted" in {

      val mockSessionRepository =
        mock[SessionRepository]

      when(
        mockSessionRepository.set(any())
      ).thenReturn(
        Future.successful(true)
      )

      val application =
        applicationBuilder(
          userAnswers = Some(userAnswers)
        )
          .overrides(
            bind[Navigator].toInstance(
              new FakeNavigator(onwardRoute)
            ),
            bind[SessionRepository].toInstance(
              mockSessionRepository
            )
          )
          .build()

      running(application) {

        val request =
          FakeRequest(
            POST,
            whatToDoWithStandardReturnPeriodsRoute
          ).withFormUrlEncodedBody(
            "value" -> changeMonthsStandardPeriodCover.toString
          )

        val result =
          controller(application)
            .onSubmit(NormalMode)
            .apply(request)

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual onwardRoute.url

        val expectedAnswers =
          userAnswers
            .set(
              WhatToDoWithStandardReturnPeriodsPage,
              changeMonthsStandardPeriodCover
            )
            .success
            .value

        verify(mockSessionRepository).set(expectedAnswers)
      }
    }

    "must save the selected answer when Keepstandardreturnperiod is submitted" in {

      val mockSessionRepository =
        mock[SessionRepository]

      when(
        mockSessionRepository.set(any())
      ).thenReturn(
        Future.successful(true)
      )

      val application =
        applicationBuilder(
          userAnswers = Some(userAnswers)
        )
          .overrides(
            bind[Navigator].toInstance(
              new FakeNavigator(onwardRoute)
            ),
            bind[SessionRepository].toInstance(
              mockSessionRepository
            )
          )
          .build()

      running(application) {

        val request =
          FakeRequest(
            POST,
            whatToDoWithStandardReturnPeriodsRoute
          ).withFormUrlEncodedBody(
            "value" -> keepStandardReturnPeriod.toString
          )

        val result =
          controller(application)
            .onSubmit(NormalMode)
            .apply(request)

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual onwardRoute.url

        val expectedAnswers =
          userAnswers
            .set(
              WhatToDoWithStandardReturnPeriodsPage,
              keepStandardReturnPeriod
            )
            .success
            .value

        verify(mockSessionRepository).set(expectedAnswers)
      }
    }

    "must return Bad Request with errors when invalid data is submitted" in {

      val application =
        applicationBuilder(
          userAnswers = Some(userAnswers)
        ).build()

      running(application) {

        val request =
          FakeRequest(
            POST,
            whatToDoWithStandardReturnPeriodsRoute
          ).withFormUrlEncodedBody(
            "value" -> "invalid-value"
          )

        val boundForm =
          form.bind(
            Map(
              "value" -> "invalid-value"
            )
          )

        val view =
          application.injector
            .instanceOf[WhatToDoWithStandardReturnPeriodsView]

        val result =
          controller(application)
            .onSubmit(NormalMode)
            .apply(request)

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(
            boundForm,
            NormalMode,
            returnPeriodsId
          )(
            request,
            messages(application)
          ).toString
      }
    }

    "must return Bad Request when no answer is submitted" in {

      val application =
        applicationBuilder(
          userAnswers = Some(userAnswers)
        ).build()

      running(application) {

        val request =
          FakeRequest(
            POST,
            whatToDoWithStandardReturnPeriodsRoute
          )

        val boundForm =
          form.bind(Map.empty[String, String])

        val view =
          application.injector
            .instanceOf[WhatToDoWithStandardReturnPeriodsView]

        val result =
          controller(application)
            .onSubmit(NormalMode)
            .apply(request)

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(
            boundForm,
            NormalMode,
            returnPeriodsId
          )(
            request,
            messages(application)
          ).toString
      }
    }

    "must redirect to the system error page for a GET if no existing data is found" in {

      val application =
        applicationBuilder(
          userAnswers = None
        ).build()

      running(application) {

        val request =
          FakeRequest(
            GET,
            whatToDoWithStandardReturnPeriodsRoute
          )

        val result =
          controller(application)
            .onPageLoad(NormalMode)
            .apply(request)

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.routes.SystemErrorController.onPageLoad().url
      }
    }

    "must redirect to the system error page for a POST if no existing data is found" in {

      val application =
        applicationBuilder(
          userAnswers = None
        ).build()

      running(application) {

        val request =
          FakeRequest(
            POST,
            whatToDoWithStandardReturnPeriodsRoute
          ).withFormUrlEncodedBody(
            "value" -> switchToNonStandard.toString
          )

        val result =
          controller(application)
            .onSubmit(NormalMode)
            .apply(request)

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.routes.SystemErrorController.onPageLoad().url
      }
    }
  }
}
