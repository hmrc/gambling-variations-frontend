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
import forms.returnperiods.{NonStandardReturnPeriodsFormProvider, StandardReturnPeriodsFormProvider}
import models.ChooseReturnPeriods.Jan
import models.{GamblingReturnPeriods, NonStandardReturnPeriodsOptions, NormalMode, StandardReturnPeriodsOptions, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.returnperiods.{GamblingReturnPeriodsPage, NonStandardReturnPeriodsPage, StandardReturnPeriodsPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import viewmodels.checkAnswers.returnperiods.NonStandardReturnPeriodsViewModel
import views.html.returnperiods.{NonStandardReturnPeriodsView, StandardReturnPeriodsView}

import java.time.LocalDate
import scala.concurrent.Future

class ReturnPeriodsControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  lazy val returnPeriodsRoute = routes.ReturnPeriodsController.onPageLoad(NormalMode).url

  "ReturnPeriods Controller" - {

    "Standard Return Periods" - {
      val formProvider = new StandardReturnPeriodsFormProvider()
      val form = formProvider()

      val switchToNonStandard =
        StandardReturnPeriodsOptions.SwitchToNonStandard

      val changeMonthsStandardPeriodCover =
        StandardReturnPeriodsOptions.ChangeMonthsStandardPeriodCover

      val keepStandardReturnPeriod =
        StandardReturnPeriodsOptions.KeepStandardReturnPeriod

      val gamblingReturnPeriods =
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

      val userAnswers =
        UserAnswers(userAnswersId)
          .set(
            GamblingReturnPeriodsPage,
            gamblingReturnPeriods
          )
          .success
          .value

      val answeredUserAnswers =
        userAnswers
          .set(
            StandardReturnPeriodsPage,
            switchToNonStandard
          )
          .success
          .value

      val userAnswersMissingHasExistingNstpValues =
        userAnswers
          .set(
            GamblingReturnPeriodsPage,
            gamblingReturnPeriods.copy(hasExistingNstpValues = None)
          )
          .success
          .value

      val returnPeriodsId =
        Some(Jan.returnPeriodsId.toString)

      def controller(application: play.api.Application) =
        application.injector
          .instanceOf[ReturnPeriodsController]

      "must return OK and the correct view for a GET" in {

        val application =
          applicationBuilder(
            userAnswers = Some(userAnswers)
          ).build()

        running(application) {

          val request =
            FakeRequest(
              GET,
              returnPeriodsRoute
            )

          val result =
            controller(application)
              .onPageLoad(NormalMode)
              .apply(request)

          val view =
            application.injector
              .instanceOf[StandardReturnPeriodsView]

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
              returnPeriodsRoute
            )

          val result =
            controller(application)
              .onPageLoad(NormalMode)
              .apply(request)

          val view =
            application.injector
              .instanceOf[StandardReturnPeriodsView]

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
              returnPeriodsRoute
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
                StandardReturnPeriodsPage,
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
              returnPeriodsRoute
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
                StandardReturnPeriodsPage,
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
              returnPeriodsRoute
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
                StandardReturnPeriodsPage,
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
              returnPeriodsRoute
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
              .instanceOf[StandardReturnPeriodsView]

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
              returnPeriodsRoute
            )

          val boundForm =
            form.bind(Map.empty[String, String])

          val view =
            application.injector
              .instanceOf[StandardReturnPeriodsView]

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
              returnPeriodsRoute
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
              returnPeriodsRoute
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

      "must redirect to the system error page for a GET if HasExistingNstpValues is None" in {

        val application =
          applicationBuilder(
            userAnswers = Some(userAnswersMissingHasExistingNstpValues)
          ).build()

        running(application) {

          val request =
            FakeRequest(
              GET,
              returnPeriodsRoute
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

      "must redirect to the system error page for a POST if HasExistingNstpValues is None" in {

        val application =
          applicationBuilder(
            userAnswers = Some(userAnswersMissingHasExistingNstpValues)
          ).build()

        running(application) {

          val request =
            FakeRequest(
              POST,
              returnPeriodsRoute
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

  ///////////////////////////////

  "NonStandard Return Periods" - {

    val nonStandardFormProvider = new NonStandardReturnPeriodsFormProvider()
    val nonStandardForm = nonStandardFormProvider()

    val date = LocalDate.of(2026, 5, 1)
    val gamblingReturnPeriods = GamblingReturnPeriods(
      mgdRegNumber          = "mgd1",
      returnPeriodsId       = Some(1),
      nstpEndDate1          = Some(date),
      nstpEndDate2          = Some(date.plusMonths(3)),
      nstpEndDate3          = Some(date.plusMonths(6)),
      nstpEndDate4          = Some(date.plusMonths(9)),
      nstpEndDate5          = Some(date.plusMonths(12)),
      nstpEndDate6          = Some(date.plusMonths(15)),
      nstpEndDate7          = Some(date.plusMonths(18)),
      nstpEndDate8          = Some(date.plusMonths(21)),
      isInLastNstp          = Some(true),
      finalPeriodWarning    = Some(false),
      hasExistingNstpValues = Some(true)
    )
    val userAnswersNonStandardReturnPeriods = emptyUserAnswers
      .set(
        GamblingReturnPeriodsPage,
        gamblingReturnPeriods
      )
      .success
      .value

    val userAnswersNonStandardReturnPeriodsMissingIsInLastNstp = emptyUserAnswers
      .set(
        GamblingReturnPeriodsPage,
        gamblingReturnPeriods.copy(isInLastNstp = None)
      )
      .success
      .value

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersNonStandardReturnPeriods)).build()

      running(application) {
        val request = FakeRequest(GET, returnPeriodsRoute)

        val viewModel = NonStandardReturnPeriodsViewModel.from(gamblingReturnPeriods)(messages(application))

        val result = route(application, request).value

        val view = application.injector.instanceOf[NonStandardReturnPeriodsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(nonStandardForm, viewModel.get, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers =
        userAnswersNonStandardReturnPeriods.set(NonStandardReturnPeriodsPage, NonStandardReturnPeriodsOptions.values.head).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, returnPeriodsRoute)

        val viewModel = NonStandardReturnPeriodsViewModel.from(gamblingReturnPeriods)(messages(application))

        val view = application.injector.instanceOf[NonStandardReturnPeriodsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(nonStandardForm.fill(NonStandardReturnPeriodsOptions.values.head), viewModel.get, NormalMode)(
          request,
          messages(application)
        ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswersNonStandardReturnPeriods))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, returnPeriodsRoute)
            .withFormUrlEncodedBody(("value", NonStandardReturnPeriodsOptions.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersNonStandardReturnPeriods)).build()

      running(application) {
        val request =
          FakeRequest(POST, returnPeriodsRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val viewModel = NonStandardReturnPeriodsViewModel.from(gamblingReturnPeriods)(messages(application))

        val boundForm = nonStandardForm.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[NonStandardReturnPeriodsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, viewModel.get, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to SystemError for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, returnPeriodsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.SystemErrorController.onPageLoad().url
      }
    }

    "redirect to SystemError for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, returnPeriodsRoute)
            .withFormUrlEncodedBody(("value", NonStandardReturnPeriodsOptions.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.SystemErrorController.onPageLoad().url
      }
    }



    "must redirect to SystemError for a GET if isInLastNstp is None" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersNonStandardReturnPeriodsMissingIsInLastNstp)).build()

      running(application) {
        val request = FakeRequest(GET, returnPeriodsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.SystemErrorController.onPageLoad().url
      }
    }

    "redirect to SystemError for a POST if isInLastNstp is None" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersNonStandardReturnPeriodsMissingIsInLastNstp)).build()

      running(application) {
        val request =
          FakeRequest(POST, returnPeriodsRoute)
            .withFormUrlEncodedBody(("value", NonStandardReturnPeriodsOptions.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.SystemErrorController.onPageLoad().url
      }
    }
  }
}
