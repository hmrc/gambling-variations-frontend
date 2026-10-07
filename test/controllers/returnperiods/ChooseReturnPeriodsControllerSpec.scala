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
import forms.returnperiods.ChooseReturnPeriodsFormProvider
import models.ChooseReturnPeriods.{Feb, Jan, Mar}
import models.{CheckMode, GamblingReturnPeriods, NormalMode, ReturnPeriodsVariant, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.returnperiods.{ChooseReturnPeriodsPage, GamblingReturnPeriodsPage, HasExistingNstpValuesPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.returnperiods.ChooseReturnPeriodsView

import scala.concurrent.Future

class ChooseReturnPeriodsControllerSpec extends SpecBase with MockitoSugar {

  private val onwardRoute =
    Call("GET", "/foo")
  lazy val chooseReturnPeriodsNormalRoute =
    controllers.returnperiods.routes.ChooseReturnPeriodsController.onPageLoad(NormalMode).url

  lazy val chooseReturnPeriodsCheckRoute =
    controllers.returnperiods.routes.ChooseReturnPeriodsController.onPageLoad(CheckMode).url

  private val formProvider =
    new ChooseReturnPeriodsFormProvider()

  private val standardGamblingReturnPeriods =
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

  private val nonStandardGamblingReturnPeriods =
    standardGamblingReturnPeriods.copy(
      returnPeriodsId       = Some(Feb.returnPeriodsId),
      hasExistingNstpValues = Some(true)
    )

  private val standardUserAnswers =
    UserAnswers(userAnswersId)
      .set(
        GamblingReturnPeriodsPage,
        standardGamblingReturnPeriods
      )
      .success
      .value

  private val nonStandardUserAnswers =
    UserAnswers(userAnswersId)
      .set(
        GamblingReturnPeriodsPage,
        nonStandardGamblingReturnPeriods
      )
      .success
      .value

  private def controller(application: play.api.Application) =
    application.injector
      .instanceOf[ChooseReturnPeriodsController]

  "ChooseReturnPeriods Controller" - {

    "must return OK and the correct view for a GET in NormalMode" in {

      val application =
        applicationBuilder(
          userAnswers = Some(standardUserAnswers)
        ).build()

      running(application) {

        val request =
          FakeRequest(
            GET,
            chooseReturnPeriodsNormalRoute
          )

        val result =
          controller(application)
            .onPageLoad(NormalMode)
            .apply(request)

        val view =
          application.injector
            .instanceOf[ChooseReturnPeriodsView]

        val form =
          formProvider(
            ReturnPeriodsVariant.Standard.errorMessageKey
          )

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(
            form.fill(Jan),
            NormalMode,
            ReturnPeriodsVariant.Standard
          )(
            request,
            messages(application)
          ).toString
      }
    }

    "must return OK and use the NonStandard variant for a GET" in {

      val application =
        applicationBuilder(
          userAnswers = Some(nonStandardUserAnswers)
        ).build()

      running(application) {

        val request =
          FakeRequest(
            GET,
            chooseReturnPeriodsNormalRoute
          )

        val result =
          controller(application)
            .onPageLoad(NormalMode)
            .apply(request)

        val view =
          application.injector
            .instanceOf[ChooseReturnPeriodsView]

        val form =
          formProvider(
            ReturnPeriodsVariant.NonStandard.errorMessageKey
          )

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(
            form.fill(Feb),
            NormalMode,
            ReturnPeriodsVariant.NonStandard
          )(
            request,
            messages(application)
          ).toString
      }
    }

    "must populate the view from ChooseReturnPeriodsPage when previously answered" in {

      val userAnswers =
        standardUserAnswers
          .set(
            ChooseReturnPeriodsPage,
            Mar.returnPeriodsId
          )
          .success
          .value

      val application =
        applicationBuilder(
          userAnswers = Some(userAnswers)
        ).build()

      running(application) {

        val request =
          FakeRequest(
            GET,
            chooseReturnPeriodsNormalRoute
          )

        val result =
          controller(application)
            .onPageLoad(NormalMode)
            .apply(request)

        val view =
          application.injector
            .instanceOf[ChooseReturnPeriodsView]

        val form =
          formProvider(
            ReturnPeriodsVariant.Standard.errorMessageKey
          )

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(
            form.fill(Mar),
            NormalMode,
            ReturnPeriodsVariant.Standard
          )(
            request,
            messages(application)
          ).toString
      }
    }

    "must use the return period from GamblingReturnPeriodsPage when ChooseReturnPeriodsPage is not populated" in {

      val application =
        applicationBuilder(
          userAnswers = Some(standardUserAnswers)
        ).build()

      running(application) {

        val request =
          FakeRequest(
            GET,
            chooseReturnPeriodsNormalRoute
          )

        val result =
          controller(application)
            .onPageLoad(NormalMode)
            .apply(request)

        val view =
          application.injector
            .instanceOf[ChooseReturnPeriodsView]

        val form =
          formProvider(
            ReturnPeriodsVariant.Standard.errorMessageKey
          )

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(
            form.fill(Jan),
            NormalMode,
            ReturnPeriodsVariant.Standard
          )(
            request,
            messages(application)
          ).toString
      }
    }

    "must leave the form empty when GamblingReturnPeriodsPage has no return period" in {

      val gamblingReturnPeriods =
        standardGamblingReturnPeriods.copy(
          returnPeriodsId = None
        )

      val userAnswers =
        UserAnswers(userAnswersId)
          .set(
            GamblingReturnPeriodsPage,
            gamblingReturnPeriods
          )
          .success
          .value

      val application =
        applicationBuilder(
          userAnswers = Some(userAnswers)
        ).build()

      running(application) {

        val request =
          FakeRequest(
            GET,
            chooseReturnPeriodsNormalRoute
          )

        val result =
          controller(application)
            .onPageLoad(NormalMode)
            .apply(request)

        val view =
          application.injector
            .instanceOf[ChooseReturnPeriodsView]

        val form =
          formProvider(
            ReturnPeriodsVariant.Standard.errorMessageKey
          )

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(
            form,
            NormalMode,
            ReturnPeriodsVariant.Standard
          )(
            request,
            messages(application)
          ).toString
      }
    }

    "must leave the form empty when GamblingReturnPeriodsPage contains an unknown return period ID" in {

      val gamblingReturnPeriods =
        standardGamblingReturnPeriods.copy(
          returnPeriodsId = Some(999)
        )

      val userAnswers =
        UserAnswers(userAnswersId)
          .set(
            GamblingReturnPeriodsPage,
            gamblingReturnPeriods
          )
          .success
          .value

      val application =
        applicationBuilder(
          userAnswers = Some(userAnswers)
        ).build()

      running(application) {

        val request =
          FakeRequest(
            GET,
            chooseReturnPeriodsNormalRoute
          )

        val result =
          controller(application)
            .onPageLoad(NormalMode)
            .apply(request)

        val view =
          application.injector
            .instanceOf[ChooseReturnPeriodsView]

        val form =
          formProvider(
            ReturnPeriodsVariant.Standard.errorMessageKey
          )

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(
            form,
            NormalMode,
            ReturnPeriodsVariant.Standard
          )(
            request,
            messages(application)
          ).toString
      }
    }

    "must redirect to the system error page when hasExistingNstpValues is missing" in {

      val gamblingReturnPeriods =
        standardGamblingReturnPeriods.copy(
          hasExistingNstpValues = None
        )

      val userAnswers =
        UserAnswers(userAnswersId)
          .set(
            GamblingReturnPeriodsPage,
            gamblingReturnPeriods
          )
          .success
          .value

      val application =
        applicationBuilder(
          userAnswers = Some(userAnswers)
        ).build()

      running(application) {

        val request =
          FakeRequest(
            GET,
            chooseReturnPeriodsNormalRoute
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

    "must return OK and use the CheckMode route for a GET" in {

      val application =
        applicationBuilder(
          userAnswers = Some(standardUserAnswers)
        ).build()

      running(application) {

        val request =
          FakeRequest(
            GET,
            "/change-registration-details/return-periods/change-select"
          )

        val result =
          controller(application)
            .onPageLoad(models.CheckMode)
            .apply(request)

        val view =
          application.injector
            .instanceOf[ChooseReturnPeriodsView]

        val form =
          formProvider(
            ReturnPeriodsVariant.Standard.errorMessageKey
          )

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(
            form.fill(Jan),
            models.CheckMode,
            ReturnPeriodsVariant.Standard
          )(
            request,
            messages(application)
          ).toString
      }
    }

    "must save the selected return period and redirect in NormalMode" in {

      val mockSessionRepository =
        mock[SessionRepository]

      when(
        mockSessionRepository.set(any())
      ).thenReturn(
        Future.successful(true)
      )

      val application =
        applicationBuilder(
          userAnswers = Some(standardUserAnswers)
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
            chooseReturnPeriodsNormalRoute
          ).withFormUrlEncodedBody(
            "value" -> Feb.toString
          )

        val result =
          controller(application)
            .onSubmit(NormalMode)
            .apply(request)

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual onwardRoute.url

        val expectedAnswers =
          standardUserAnswers
            .set(
              ChooseReturnPeriodsPage,
              Feb.returnPeriodsId
            )
            .success
            .value
            .set(
              HasExistingNstpValuesPage,
              false
            )
            .success
            .value

        verify(mockSessionRepository).set(expectedAnswers)
      }
    }

    "must save true for HasExistingNstpValuesPage for a NonStandard return period" in {

      val mockSessionRepository =
        mock[SessionRepository]

      when(
        mockSessionRepository.set(any())
      ).thenReturn(
        Future.successful(true)
      )

      val application =
        applicationBuilder(
          userAnswers = Some(nonStandardUserAnswers)
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
            chooseReturnPeriodsNormalRoute
          ).withFormUrlEncodedBody(
            "value" -> Mar.toString
          )

        val result =
          controller(application)
            .onSubmit(NormalMode)
            .apply(request)

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual onwardRoute.url

        val expectedAnswers =
          nonStandardUserAnswers
            .set(
              ChooseReturnPeriodsPage,
              Mar.returnPeriodsId
            )
            .success
            .value
            .set(
              HasExistingNstpValuesPage,
              true
            )
            .success
            .value

        verify(mockSessionRepository).set(expectedAnswers)
      }
    }

    "must return Bad Request with errors when invalid data is submitted" in {

      val application =
        applicationBuilder(
          userAnswers = Some(standardUserAnswers)
        ).build()

      running(application) {

        val request =
          FakeRequest(
            POST,
            chooseReturnPeriodsNormalRoute
          ).withFormUrlEncodedBody(
            "value" -> "invalid-value"
          )

        val form =
          formProvider(
            ReturnPeriodsVariant.Standard.errorMessageKey
          )

        val boundForm =
          form.bind(
            Map(
              "value" -> "invalid-value"
            )
          )

        val view =
          application.injector
            .instanceOf[ChooseReturnPeriodsView]

        val result =
          controller(application)
            .onSubmit(NormalMode)
            .apply(request)

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(
            boundForm,
            NormalMode,
            ReturnPeriodsVariant.Standard
          )(
            request,
            messages(application)
          ).toString
      }
    }

    "must use CheckMode for a valid POST" in {

      val mockSessionRepository =
        mock[SessionRepository]

      when(
        mockSessionRepository.set(any())
      ).thenReturn(
        Future.successful(true)
      )

      val application =
        applicationBuilder(
          userAnswers = Some(standardUserAnswers)
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
            chooseReturnPeriodsCheckRoute
          ).withFormUrlEncodedBody(
            "value" -> Mar.toString
          )

        val result =
          controller(application)
            .onSubmit(models.CheckMode)
            .apply(request)

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual onwardRoute.url

        val expectedAnswers =
          standardUserAnswers
            .set(
              ChooseReturnPeriodsPage,
              Mar.returnPeriodsId
            )
            .success
            .value
            .set(
              HasExistingNstpValuesPage,
              false
            )
            .success
            .value

        verify(mockSessionRepository).set(expectedAnswers)
      }
    }

    "must redirect to the system error page when hasExistingNstpValues is missing on POST" in {

      val gamblingReturnPeriods =
        standardGamblingReturnPeriods.copy(
          hasExistingNstpValues = None
        )

      val userAnswers =
        UserAnswers(userAnswersId)
          .set(
            GamblingReturnPeriodsPage,
            gamblingReturnPeriods
          )
          .success
          .value

      val application =
        applicationBuilder(
          userAnswers = Some(userAnswers)
        ).build()

      running(application) {

        val request =
          FakeRequest(
            POST,
            chooseReturnPeriodsNormalRoute
          ).withFormUrlEncodedBody(
            "value" -> Feb.toString
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
