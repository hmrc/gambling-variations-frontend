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
import forms.returnperiods.{NoReturnPeriodsSetFormProvider, NonStandardNotSubmittedReturnPeriodsFormProvider, NonStandardReturnPeriodsFormProvider, StandardReturnPeriodsFormProvider}
import models.ChooseReturnPeriods.Jan
import models.{GamblingReturnPeriods, NoReturnPeriodsSet, NonStandardNotSubmittedReturnPeriodsOptions, NonStandardReturnPeriodsOptions, NormalMode, StandardReturnPeriodsOptions, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.returnperiods.*
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import viewmodels.checkAnswers.returnperiods.*
import views.html.returnperiods.*

import java.time.LocalDate
import scala.concurrent.Future

class ReturnPeriodsControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  lazy val returnPeriodsRoute: String = routes.ReturnPeriodsController.onPageLoad(NormalMode).url

  private def mockedSessionRepository: SessionRepository = {
    val mockSessionRepository = mock[SessionRepository]
    when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
    mockSessionRepository
  }

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

        val mockSessionRepository = mockedSessionRepository

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

        val mockSessionRepository = mockedSessionRepository

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

        val mockSessionRepository = mockedSessionRepository

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

      "must save the answer and redirect to the next page when valid data is submitted" in {

        val mockSessionRepository = mockedSessionRepository

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

          val expectedAnswers =
            userAnswersNonStandardReturnPeriods
              .set(NonStandardReturnPeriodsPage, NonStandardReturnPeriodsOptions.values.head)
              .success
              .value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
          verify(mockSessionRepository).set(expectedAnswers)
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

      "must redirect to SystemError for a POST if no existing data is found" in {

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

      "must redirect to SystemError for a POST if isInLastNstp is None, without saving" in {

        val mockSessionRepository = mockedSessionRepository

        val application =
          applicationBuilder(userAnswers = Some(userAnswersNonStandardReturnPeriodsMissingIsInLastNstp))
            .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
            .build()

        running(application) {
          val request =
            FakeRequest(POST, returnPeriodsRoute)
              .withFormUrlEncodedBody(("value", NonStandardReturnPeriodsOptions.values.head.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.SystemErrorController.onPageLoad().url
          verify(mockSessionRepository, never()).set(any())
        }
      }
    }

    ///////////////////////////////

    "No Return Periods Set" - {

      val noPeriodsSetFormProvider = new NoReturnPeriodsSetFormProvider()
      val noPeriodsSetForm = noPeriodsSetFormProvider()

      val validAnswer: NoReturnPeriodsSet = NoReturnPeriodsSet.values.head

      val gamblingReturnPeriods = GamblingReturnPeriods(
        mgdRegNumber          = "mgd1",
        returnPeriodsId       = None,
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

      val userAnswersNoPeriodsSet = emptyUserAnswers
        .set(GamblingReturnPeriodsPage, gamblingReturnPeriods)
        .success
        .value

      "must return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(userAnswersNoPeriodsSet)).build()

        running(application) {
          val request = FakeRequest(GET, returnPeriodsRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[NoReturnPeriodsSetView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(noPeriodsSetForm, NormalMode)(request, messages(application)).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val userAnswers = userAnswersNoPeriodsSet.set(ReturnPeriodsChoicePage, validAnswer).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, returnPeriodsRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[NoReturnPeriodsSetView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(noPeriodsSetForm.fill(validAnswer), NormalMode)(request, messages(application)).toString
        }
      }

      "must show the no periods set view even when HasExistingNstpValues is None" in {

        val userAnswers = emptyUserAnswers
          .set(GamblingReturnPeriodsPage, gamblingReturnPeriods.copy(hasExistingNstpValues = None))
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, returnPeriodsRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[NoReturnPeriodsSetView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(noPeriodsSetForm, NormalMode)(request, messages(application)).toString
        }
      }

      "must save the answer and redirect to the next page when valid data is submitted" in {

        val mockSessionRepository = mockedSessionRepository

        val application =
          applicationBuilder(userAnswers = Some(userAnswersNoPeriodsSet))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, returnPeriodsRoute)
              .withFormUrlEncodedBody(("value", validAnswer.toString))

          val result = route(application, request).value

          val expectedAnswers = userAnswersNoPeriodsSet.set(ReturnPeriodsChoicePage, validAnswer).success.value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
          verify(mockSessionRepository).set(expectedAnswers)
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {

        val application = applicationBuilder(userAnswers = Some(userAnswersNoPeriodsSet)).build()

        running(application) {
          val request =
            FakeRequest(POST, returnPeriodsRoute)
              .withFormUrlEncodedBody(("value", "invalid value"))

          val boundForm = noPeriodsSetForm.bind(Map("value" -> "invalid value"))

          val view = application.injector.instanceOf[NoReturnPeriodsSetView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
        }
      }

      "must return a Bad Request when no answer is submitted" in {

        val application = applicationBuilder(userAnswers = Some(userAnswersNoPeriodsSet)).build()

        running(application) {
          val request = FakeRequest(POST, returnPeriodsRoute)

          val boundForm = noPeriodsSetForm.bind(Map.empty[String, String])

          val view = application.injector.instanceOf[NoReturnPeriodsSetView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
        }
      }
    }

    ///////////////////////////////

    "NonStandard Not Submitted Return Periods" - {

      val notSubmittedForm = new NonStandardNotSubmittedReturnPeriodsFormProvider()()
      val nonStandardForm = new NonStandardReturnPeriodsFormProvider()()

      val validAnswer: NonStandardNotSubmittedReturnPeriodsOptions =
        NonStandardNotSubmittedReturnPeriodsOptions.values.head

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

      def answersWith(
        periods: GamblingReturnPeriods = gamblingReturnPeriods,
        switchedToNonStandard: Option[Boolean] = Some(true),
        hasSubmitted: Option[Boolean] = Some(false),
        switchedToStandard: Option[Boolean] = None,
        hasSubmittedStandard: Option[Boolean] = None
      ): UserAnswers = {
        val base = emptyUserAnswers.set(GamblingReturnPeriodsPage, periods).success.value
        val withNonStd = switchedToNonStandard.fold(base)(v => base.set(HasSwitchedToNonStandardValuesPage, v).success.value)
        val withSubmitted = hasSubmitted.fold(withNonStd)(v => withNonStd.set(HasSubmittedNonStandardPeriodsPage, v).success.value)
        val withStd = switchedToStandard.fold(withSubmitted)(v => withSubmitted.set(HasSwitchedToStandardValuesPage, v).success.value)
        hasSubmittedStandard.fold(withStd)(v => withStd.set(HasSubmittedStandardPeriodsPage, v).success.value)
      }

      val notSubmittedAnswers = answersWith()

      def assertNotSubmittedView(answers: UserAnswers): Unit = {
        val application = applicationBuilder(userAnswers = Some(answers)).build()

        running(application) {
          val request = FakeRequest(GET, returnPeriodsRoute)
          val viewModel = NonStandardNotSubmittedReturnPeriodsViewModel.from(gamblingReturnPeriods)(messages(application))
          val view = application.injector.instanceOf[NonStandardNotSubmittedReturnPeriodsView]

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(notSubmittedForm, viewModel.get, NormalMode)(request, messages(application)).toString
        }
      }

      def assertNonStandardView(answers: UserAnswers): Unit = {
        val application = applicationBuilder(userAnswers = Some(answers)).build()

        running(application) {
          val request = FakeRequest(GET, returnPeriodsRoute)
          val viewModel = NonStandardReturnPeriodsViewModel.from(gamblingReturnPeriods)(messages(application))
          val view = application.injector.instanceOf[NonStandardReturnPeriodsView]

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(nonStandardForm, viewModel.get, NormalMode)(request, messages(application)).toString
        }
      }

      "must return OK and the correct view for a GET" in {
        assertNotSubmittedView(notSubmittedAnswers)
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val userAnswers = notSubmittedAnswers.set(NonStandardNotSubmittedReturnPeriodsPage, validAnswer).success.value
        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, returnPeriodsRoute)
          val viewModel = NonStandardNotSubmittedReturnPeriodsViewModel.from(gamblingReturnPeriods)(messages(application))
          val view = application.injector.instanceOf[NonStandardNotSubmittedReturnPeriodsView]

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(notSubmittedForm.fill(validAnswer), viewModel.get, NormalMode)(
            request,
            messages(application)
          ).toString
        }
      }

      "must save the answer and redirect to the next page when valid data is submitted" in {

        val mockSessionRepository = mockedSessionRepository

        val application =
          applicationBuilder(userAnswers = Some(notSubmittedAnswers))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, returnPeriodsRoute)
              .withFormUrlEncodedBody(("value", validAnswer.toString))

          val result = route(application, request).value

          val expectedAnswers =
            notSubmittedAnswers.set(NonStandardNotSubmittedReturnPeriodsPage, validAnswer).success.value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
          verify(mockSessionRepository).set(expectedAnswers)
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {

        val application = applicationBuilder(userAnswers = Some(notSubmittedAnswers)).build()

        running(application) {
          val request =
            FakeRequest(POST, returnPeriodsRoute)
              .withFormUrlEncodedBody(("value", "invalid value"))

          val viewModel = NonStandardNotSubmittedReturnPeriodsViewModel.from(gamblingReturnPeriods)(messages(application))
          val boundForm = notSubmittedForm.bind(Map("value" -> "invalid value"))
          val view = application.injector.instanceOf[NonStandardNotSubmittedReturnPeriodsView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, viewModel.get, NormalMode)(request, messages(application)).toString
        }
      }

      "must return a Bad Request when no answer is submitted" in {

        val application = applicationBuilder(userAnswers = Some(notSubmittedAnswers)).build()

        running(application) {
          val request = FakeRequest(POST, returnPeriodsRoute)

          val viewModel = NonStandardNotSubmittedReturnPeriodsViewModel.from(gamblingReturnPeriods)(messages(application))
          val boundForm = notSubmittedForm.bind(Map.empty[String, String])
          val view = application.injector.instanceOf[NonStandardNotSubmittedReturnPeriodsView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, viewModel.get, NormalMode)(request, messages(application)).toString
        }
      }

      "must show the not-submitted view when the submitted flag is missing" in {
        assertNotSubmittedView(answersWith(hasSubmitted = None))
      }

      "must show the non-standard view when the user has switched to non-standard and has submitted" in {
        assertNonStandardView(answersWith(hasSubmitted = Some(true)))
      }

      "must show the non-standard view when the switched to non-standard flag is false" in {
        assertNonStandardView(answersWith(switchedToNonStandard = Some(false)))
      }

      "must show the non-standard view when the switched to non-standard flag is missing" in {
        assertNonStandardView(answersWith(switchedToNonStandard = None))
      }

      "must show the standard view when there are no existing NSTP values, even if the user has switched to non-standard" in {

        val answers = answersWith(periods = gamblingReturnPeriods.copy(hasExistingNstpValues = Some(false)))
        val application = applicationBuilder(userAnswers = Some(answers)).build()

        running(application) {
          val request = FakeRequest(GET, returnPeriodsRoute)
          val view = application.injector.instanceOf[StandardReturnPeriodsView]
          val form = new StandardReturnPeriodsFormProvider()()

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, NormalMode, Some("1"))(request, messages(application)).toString
        }
      }

      "must show the locked view when the user has switched to and submitted standard" in {

        val answers = answersWith(switchedToStandard = Some(true), hasSubmittedStandard = Some(true))
        val application = applicationBuilder(userAnswers = Some(answers)).build()

        running(application) {
          val request = FakeRequest(GET, returnPeriodsRoute)
          val view = application.injector.instanceOf[LockedReturnPeriodsView]

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view()(request, messages(application)).toString
        }
      }

    }

    ///////////////////////////////

    "Locked Return Periods" - {

      val gamblingReturnPeriods = GamblingReturnPeriods(
        mgdRegNumber          = "mgd1",
        returnPeriodsId       = Some(1),
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

      def lockedAnswers(periods: GamblingReturnPeriods = gamblingReturnPeriods): UserAnswers =
        emptyUserAnswers
          .set(GamblingReturnPeriodsPage, periods)
          .success
          .value
          .set(HasSwitchedToStandardValuesPage, true)
          .success
          .value
          .set(HasSubmittedStandardPeriodsPage, true)
          .success
          .value

      "must return OK and the locked view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(lockedAnswers())).build()

        running(application) {
          val request = FakeRequest(GET, returnPeriodsRoute)
          val result = route(application, request).value
          val view = application.injector.instanceOf[LockedReturnPeriodsView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view()(request, messages(application)).toString
        }
      }

      "must show the locked view instead of the non-standard view when the user has switched to standard" in {

        val nonStandard = gamblingReturnPeriods.copy(hasExistingNstpValues = Some(true))
        val application = applicationBuilder(userAnswers = Some(lockedAnswers(nonStandard))).build()

        running(application) {
          val request = FakeRequest(GET, returnPeriodsRoute)
          val result = route(application, request).value
          val view = application.injector.instanceOf[LockedReturnPeriodsView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view()(request, messages(application)).toString
        }
      }

      "must show the no periods set view, not the locked view, when there is no return period ID" in {

        val noId = gamblingReturnPeriods.copy(returnPeriodsId = None)
        val application = applicationBuilder(userAnswers = Some(lockedAnswers(noId))).build()

        running(application) {
          val request = FakeRequest(GET, returnPeriodsRoute)
          val result = route(application, request).value
          val view = application.injector.instanceOf[NoReturnPeriodsSetView]
          val form = new NoReturnPeriodsSetFormProvider()()

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
        }
      }

      "must show the standard view when the switched flag is false" in {

        val answers = lockedAnswers().set(HasSwitchedToStandardValuesPage, false).success.value
        val application = applicationBuilder(userAnswers = Some(answers)).build()

        running(application) {
          val request = FakeRequest(GET, returnPeriodsRoute)
          val result = route(application, request).value
          val view = application.injector.instanceOf[StandardReturnPeriodsView]
          val form = new StandardReturnPeriodsFormProvider()()

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, NormalMode, Some("1"))(request, messages(application)).toString
        }
      }

      "must show the standard view when the user has switched to standard but not submitted" in {

        val answers = lockedAnswers().set(HasSubmittedStandardPeriodsPage, false).success.value
        val application = applicationBuilder(userAnswers = Some(answers)).build()

        running(application) {
          val request = FakeRequest(GET, returnPeriodsRoute)
          val result = route(application, request).value
          val view = application.injector.instanceOf[StandardReturnPeriodsView]
          val form = new StandardReturnPeriodsFormProvider()()

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, NormalMode, Some("1"))(request, messages(application)).toString
        }
      }

      "must show the standard view when the user has submitted standard but the switched flag is missing" in {

        val answers = emptyUserAnswers
          .set(GamblingReturnPeriodsPage, gamblingReturnPeriods)
          .success
          .value
          .set(HasSubmittedStandardPeriodsPage, true)
          .success
          .value
        val application = applicationBuilder(userAnswers = Some(answers)).build()

        running(application) {
          val request = FakeRequest(GET, returnPeriodsRoute)
          val result = route(application, request).value
          val view = application.injector.instanceOf[StandardReturnPeriodsView]
          val form = new StandardReturnPeriodsFormProvider()()

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, NormalMode, Some("1"))(request, messages(application)).toString
        }
      }

      "must redirect to change registration details on POST without saving" in {

        val mockSessionRepository = mockedSessionRepository

        val application =
          applicationBuilder(userAnswers = Some(lockedAnswers()))
            .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
            .build()

        running(application) {
          val request = FakeRequest(POST, returnPeriodsRoute).withFormUrlEncodedBody(("value", "anything"))
          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.ChangeRegistrationDetailsController.onPageLoad().url
          verify(mockSessionRepository, never()).set(any())
        }
      }
    }
  }

}
