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
import connectors.GamblingConnector
import models.ChooseReturnPeriods.Jan
import models.{BusinessDetails, BusinessType, GamblingReturnPeriods, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.returnperiods.{ChooseReturnPeriodsPage, GamblingReturnPeriodsPage, NonStandardPeriodDatePages}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.returnperiods.NstpPeriodCalculator
import uk.gov.hmrc.http.HeaderCarrier

import java.time.{Clock, Instant, LocalDate, ZoneOffset}
import scala.concurrent.Future

class EnterNonStandardPeriodDateControllerSpec extends SpecBase with MockitoSugar {

  private val periodNumber = 1

  private val periodEnd =
    LocalDate.of(2026, 1, 16)

  private val lowerBoundary =
    periodEnd.minusDays(16)

  private val upperBoundary =
    periodEnd.plusDays(16)

  private val validAnswer =
    LocalDate.of(2026, 1, 15)

  private val registrationDate =
    LocalDate.of(2020, 1, 1)

  private val clock: Clock =
    Clock.fixed(
      Instant.parse("2026-01-16T00:00:00Z"),
      ZoneOffset.UTC
    )

  private val enterNonStandardPeriodDateRoute =
    controllers.returnperiods.routes.EnterNonStandardPeriodDateController
      .onPageLoad(periodNumber, NormalMode)
      .url

  private val businessDetails =
    BusinessDetails(
      mgdRegNumber          = mgdRegNum,
      businessType          = Some(BusinessType.Corporatebody),
      currentlyRegistered   = 1,
      groupReg              = false,
      dateOfRegistration    = Some(registrationDate),
      businessPartnerNumber = Some("XB1234567890"),
      systemDate            = LocalDate.of(2026, 1, 1)
    )

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

  private val onwardRoute =
    Call("GET", "/foo")

  private val userAnswers =
    UserAnswers(userAnswersId)
      .set(
        ChooseReturnPeriodsPage,
        Jan.returnPeriodsId
      )
      .success
      .value
      .set(
        GamblingReturnPeriodsPage,
        gamblingReturnPeriods
      )
      .success
      .value

  private def mockGamblingConnector(): GamblingConnector = {

    val connector = mock[GamblingConnector]

    when(
      connector.getBusinessDetails(any[String])(any[HeaderCarrier])
    ).thenReturn(
      Future.successful(businessDetails)
    )

    when(
      connector.getGamblingReturnPeriods(any[String])(any[HeaderCarrier])
    ).thenReturn(
      Future.successful(gamblingReturnPeriods)
    )

    connector
  }

  private def mockPeriodCalculator(): NstpPeriodCalculator = {

    val calculator = mock[NstpPeriodCalculator]

    when(
      calculator.calculate(any[LocalDate], any[Int])
    ).thenReturn(
      Seq(periodEnd)
    )

    calculator
  }

  private def controller(application: play.api.Application) =
    application.injector
      .instanceOf[EnterNonStandardPeriodDateController]

  private def buildApplication(
    answers: Option[UserAnswers] = Some(userAnswers),
    mockSessionRepository: SessionRepository = mock[SessionRepository],
    mockNavigator: Navigator = new FakeNavigator(onwardRoute)
  ) = {

    val connector = mockGamblingConnector()
    val calculator = mockPeriodCalculator()

    when(
      mockSessionRepository.set(any[UserAnswers])
    ).thenReturn(
      Future.successful(true)
    )

    applicationBuilder(userAnswers = answers)
      .overrides(
        bind[GamblingConnector].toInstance(connector),
        bind[NstpPeriodCalculator].toInstance(calculator),
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[Navigator].toInstance(mockNavigator),
        bind[Clock].toInstance(clock)
      )
      .build()
  }

  "EnterNonStandardPeriodDateController" - {

    "must return OK and render the page for a GET" in {

      val application = buildApplication()

      running(application) {

        val request =
          FakeRequest(
            GET,
            enterNonStandardPeriodDateRoute
          )

        val result =
          controller(application)
            .onPageLoad(periodNumber, NormalMode)
            .apply(request)

        status(result) mustEqual OK

        contentAsString(result) must include("January 2026")
        contentAsString(result) must include("31 December 2025")
        contentAsString(result) must include("1 February 2026")
      }
    }

    "must populate the page when the question has previously been answered" in {

      val existingAnswers =
        userAnswers
          .set(
            NonStandardPeriodDatePages(periodNumber),
            validAnswer
          )
          .success
          .value

      val application =
        buildApplication(answers = Some(existingAnswers))

      running(application) {

        val request =
          FakeRequest(
            GET,
            enterNonStandardPeriodDateRoute
          )

        val result =
          controller(application)
            .onPageLoad(periodNumber, NormalMode)
            .apply(request)

        status(result) mustEqual OK

        val document = Jsoup.parse(contentAsString(result))

        document.getElementById("value.day").attr("value") mustBe "15"
      }
    }

    "must save a valid date and redirect to the next page" in {

      val mockSessionRepository =
        mock[SessionRepository]

      val application =
        buildApplication(
          mockSessionRepository = mockSessionRepository
        )

      running(application) {

        val request =
          FakeRequest(
            POST,
            controllers.returnperiods.routes.EnterNonStandardPeriodDateController
              .onSubmit(periodNumber, NormalMode)
              .url
          ).withFormUrlEncodedBody(
            "value.day"   -> validAnswer.getDayOfMonth.toString,
            "value.month" -> validAnswer.getMonthValue.toString,
            "value.year"  -> validAnswer.getYear.toString
          )

        val result =
          controller(application)
            .onSubmit(periodNumber, NormalMode)
            .apply(request)

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual onwardRoute.url

        val expectedAnswers =
          userAnswers
            .set(
              NonStandardPeriodDatePages(periodNumber),
              validAnswer
            )
            .success
            .value

        verify(mockSessionRepository).set(expectedAnswers)
      }
    }

    "must return Bad Request when an invalid date is submitted" in {

      val application = buildApplication()

      running(application) {

        val request =
          FakeRequest(
            POST,
            controllers.returnperiods.routes.EnterNonStandardPeriodDateController
              .onSubmit(periodNumber, NormalMode)
              .url
          ).withFormUrlEncodedBody(
            "value.day"   -> "31",
            "value.month" -> "2",
            "value.year"  -> "2026"
          )

        val result =
          controller(application)
            .onSubmit(periodNumber, NormalMode)
            .apply(request)

        status(result) mustEqual BAD_REQUEST
      }
    }

    "must return Bad Request when no date is submitted" in {

      val application = buildApplication()

      running(application) {

        val request =
          FakeRequest(
            POST,
            controllers.returnperiods.routes.EnterNonStandardPeriodDateController
              .onSubmit(periodNumber, NormalMode)
              .url
          )

        val result =
          controller(application)
            .onSubmit(periodNumber, NormalMode)
            .apply(request)

        status(result) mustEqual BAD_REQUEST
      }
    }

    "must return Bad Request when the submitted date is outside the permitted boundaries" in {

      val application = buildApplication()

      running(application) {

        val request =
          FakeRequest(
            POST,
            controllers.returnperiods.routes.EnterNonStandardPeriodDateController
              .onSubmit(periodNumber, NormalMode)
              .url
          ).withFormUrlEncodedBody(
            "value.day"   -> lowerBoundary.getDayOfMonth.toString,
            "value.month" -> lowerBoundary.getMonthValue.toString,
            "value.year"  -> lowerBoundary.getYear.toString
          )

        val result =
          controller(application)
            .onSubmit(periodNumber, NormalMode)
            .apply(request)

        status(result) mustEqual BAD_REQUEST
      }
    }

    "must redirect to the system error page for an invalid period number on GET" in {

      val application = buildApplication()

      running(application) {

        val request =
          FakeRequest(
            GET,
            controllers.returnperiods.routes.EnterNonStandardPeriodDateController
              .onPageLoad(9, NormalMode)
              .url
          )

        val result =
          controller(application)
            .onPageLoad(9, NormalMode)
            .apply(request)

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.routes.SystemErrorController.onPageLoad().url
      }
    }

    "must redirect to the system error page for an invalid period number on POST" in {

      val application = buildApplication()

      running(application) {

        val request =
          FakeRequest(
            POST,
            controllers.returnperiods.routes.EnterNonStandardPeriodDateController
              .onSubmit(9, NormalMode)
              .url
          ).withFormUrlEncodedBody(
            "value.day"   -> validAnswer.getDayOfMonth.toString,
            "value.month" -> validAnswer.getMonthValue.toString,
            "value.year"  -> validAnswer.getYear.toString
          )

        val result =
          controller(application)
            .onSubmit(9, NormalMode)
            .apply(request)

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.routes.SystemErrorController.onPageLoad().url
      }
    }

    "must redirect to ChooseReturnPeriods on POST when no return-period selection exists" in {

      val answersWithoutSelection =
        UserAnswers(userAnswersId)
          .set(
            GamblingReturnPeriodsPage,
            gamblingReturnPeriods
          )
          .success
          .value

      val application =
        buildApplication(answers = Some(answersWithoutSelection))

      running(application) {

        val request =
          FakeRequest(
            POST,
            controllers.returnperiods.routes.EnterNonStandardPeriodDateController
              .onSubmit(periodNumber, NormalMode)
              .url
          ).withFormUrlEncodedBody(
            "value.day"   -> validAnswer.getDayOfMonth.toString,
            "value.month" -> validAnswer.getMonthValue.toString,
            "value.year"  -> validAnswer.getYear.toString
          )

        val result =
          controller(application)
            .onSubmit(periodNumber, NormalMode)
            .apply(request)

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.returnperiods.routes.ChooseReturnPeriodsController
            .onPageLoad(NormalMode)
            .url
      }
    }
  }
}
