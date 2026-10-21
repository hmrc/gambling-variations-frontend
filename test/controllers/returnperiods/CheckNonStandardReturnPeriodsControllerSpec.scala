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
import models.{BusinessDetails, GamblingReturnPeriods, NoReturnPeriodsSet, NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, verifyNoInteractions, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.returnperiods.{ChooseReturnPeriodsPage, GamblingReturnPeriodsPage, NonStandardPeriodDatePages, ReturnPeriodsChoicePage}
import play.api.Application
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.returnperiods.NstpPeriodCalculator
import uk.gov.hmrc.http.HeaderCarrier

import java.time.{Clock, Instant, LocalDate, ZoneOffset}
import scala.concurrent.Future

class CheckNonStandardReturnPeriodsControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  private val fixedDate: LocalDate =
    LocalDate.of(2026, 10, 15)

  private val fixedClock: Clock =
    Clock.fixed(
      Instant.parse("2026-10-15T12:00:00Z"),
      ZoneOffset.UTC
    )

  private val registrationDate: LocalDate =
    LocalDate.of(2025, 1, 1)

  private val existingNstpEndDate8: LocalDate =
    LocalDate.of(2026, 7, 31)

  private val calculatedPeriodEnds: Seq[LocalDate] =
    Seq(
      LocalDate.of(2026, 10, 31),
      LocalDate.of(2027, 1, 31),
      LocalDate.of(2027, 4, 30),
      LocalDate.of(2027, 7, 31),
      LocalDate.of(2027, 10, 31),
      LocalDate.of(2028, 1, 31),
      LocalDate.of(2028, 4, 30),
      LocalDate.of(2028, 7, 31)
    )

  private val enteredDates: Seq[LocalDate] =
    Seq(
      LocalDate.of(2026, 10, 30),
      LocalDate.of(2027, 1, 30),
      LocalDate.of(2027, 4, 29),
      LocalDate.of(2027, 7, 30),
      LocalDate.of(2027, 10, 30),
      LocalDate.of(2028, 1, 30),
      LocalDate.of(2028, 4, 29),
      LocalDate.of(2028, 7, 30)
    )

  private val mockGamblingConnector: GamblingConnector =
    mock[GamblingConnector]

  private val mockNstpPeriodCalculator: NstpPeriodCalculator =
    mock[NstpPeriodCalculator]

  override protected def beforeEach(): Unit = {
    super.beforeEach()

    reset(
      mockGamblingConnector,
      mockNstpPeriodCalculator
    )
  }

  private def gamblingReturnPeriods(
    hasExistingNstpValues: Option[Boolean] = Some(false),
    nstpEndDate8: Option[LocalDate] = None,
    finalPeriodWarning: Option[Boolean] = Some(false)
  ): GamblingReturnPeriods =
    GamblingReturnPeriods(
      mgdRegNumber          = mgdRegNum,
      returnPeriodsId       = Some(1),
      nstpEndDate1          = None,
      nstpEndDate2          = None,
      nstpEndDate3          = None,
      nstpEndDate4          = None,
      nstpEndDate5          = None,
      nstpEndDate6          = None,
      nstpEndDate7          = None,
      nstpEndDate8          = nstpEndDate8,
      isInLastNstp          = Some(false),
      finalPeriodWarning    = finalPeriodWarning,
      hasExistingNstpValues = hasExistingNstpValues
    )

  private def answersWithoutReturnPeriodsId: UserAnswers =
    emptyUserAnswers
      .set(
        GamblingReturnPeriodsPage,
        gamblingReturnPeriods()
      )
      .success
      .value
      .set(
        ReturnPeriodsChoicePage,
        NoReturnPeriodsSet.Standard
      )
      .success
      .value

  private def answersWithReturnPeriodsId(
    returnPeriodsId: Int
  ): UserAnswers =
    answersWithoutReturnPeriodsId
      .set(
        ChooseReturnPeriodsPage,
        returnPeriodsId
      )
      .success
      .value

  private def answersWithoutReturnPeriodsChoice(
    returnPeriodsId: Int = 1
  ): UserAnswers =
    emptyUserAnswers
      .set(
        GamblingReturnPeriodsPage,
        gamblingReturnPeriods()
      )
      .success
      .value
      .set(
        ChooseReturnPeriodsPage,
        returnPeriodsId
      )
      .success
      .value

  private def answersWithAllDates(
    returnPeriodsId: Int = 1
  ): UserAnswers =
    enteredDates.zipWithIndex.foldLeft(
      answersWithReturnPeriodsId(returnPeriodsId)
    ) { case (answers, (date, index)) =>
      answers
        .set(
          NonStandardPeriodDatePages(index + 1),
          date
        )
        .success
        .value
    }

  private def application(
    userAnswers: UserAnswers
  ): Application =
    applicationBuilder(
      userAnswers = Some(userAnswers)
    )
      .overrides(
        bind[GamblingConnector]
          .toInstance(mockGamblingConnector),
        bind[NstpPeriodCalculator]
          .toInstance(mockNstpPeriodCalculator),
        bind[Clock]
          .toInstance(fixedClock)
      )
      .build()

  private def request =
    FakeRequest(
      GET,
      controllers.returnperiods.routes.CheckNonStandardReturnPeriodsController
        .onPageLoad()
        .url
    )

  private def businessDetails(
    dateOfRegistration: Option[LocalDate] = Some(registrationDate)
  ): BusinessDetails =
    BusinessDetails(
      mgdRegNumber          = mgdRegNum,
      businessType          = None,
      currentlyRegistered   = 1,
      groupReg              = false,
      dateOfRegistration    = dateOfRegistration,
      businessPartnerNumber = Some("XB1234567890"),
      systemDate            = LocalDate.of(2026, 1, 1)
    )

  private def stubBusinessDetails(
    details: BusinessDetails = businessDetails()
  ): Unit =
    when(
      mockGamblingConnector
        .getBusinessDetails(any[String])(
          any[HeaderCarrier]
        )
    ).thenReturn(
      Future.successful(details)
    )

  private def stubCalculatedPeriods(
    baseDate: LocalDate,
    returnPeriodsId: Int
  ): Unit =
    when(
      mockNstpPeriodCalculator.calculate(
        baseDate,
        returnPeriodsId
      )
    ).thenReturn(calculatedPeriodEnds)

  private def stubControllerDependencies(
    baseDate: LocalDate = fixedDate,
    returnPeriodsId: Int = 1,
    details: BusinessDetails = businessDetails()
  ): Unit = {
    stubBusinessDetails(details)
    stubCalculatedPeriods(baseDate, returnPeriodsId)
  }

  "CheckNonStandardReturnPeriodsController.onPageLoad" - {

    "must redirect to Choose Return Periods when the return periods ID is missing" in {
      val app =
        application(answersWithoutReturnPeriodsId)

      running(app) {
        val result =
          route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.returnperiods.routes.ChooseReturnPeriodsController
            .onPageLoad(NormalMode)
            .url

        verifyNoInteractions(
          mockGamblingConnector,
          mockNstpPeriodCalculator
        )
      }
    }

    Seq(0, 4).foreach { invalidReturnPeriodsId =>
      s"must redirect to the system error page when the return periods ID is $invalidReturnPeriodsId" in {
        val app =
          application(answersWithReturnPeriodsId(invalidReturnPeriodsId))

        running(app) {
          val result =
            route(app, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            controllers.routes.SystemErrorController
              .onPageLoad()
              .url

          verifyNoInteractions(
            mockGamblingConnector,
            mockNstpPeriodCalculator
          )
        }
      }
    }

    Seq(1, 2, 3).foreach { returnPeriodsId =>
      s"must return OK for return periods ID $returnPeriodsId" in {
        val userAnswers =
          answersWithAllDates(returnPeriodsId)

        stubControllerDependencies(
          baseDate        = fixedDate,
          returnPeriodsId = returnPeriodsId
        )

        val app =
          application(userAnswers)

        running(app) {
          val result =
            route(app, request).value

          status(result) mustEqual OK

          verify(mockGamblingConnector)
            .getBusinessDetails(any[String])(
              any[HeaderCarrier]
            )

          verify(mockNstpPeriodCalculator)
            .calculate(
              fixedDate,
              returnPeriodsId
            )
        }
      }
    }

    "must render all 8 entered dates" in {
      val userAnswers =
        answersWithAllDates()

      stubControllerDependencies()

      val app =
        application(userAnswers)

      running(app) {
        val result =
          route(app, request).value

        status(result) mustEqual OK

        val content =
          contentAsString(result)

        content must include("30 Oct 2026")
        content must include("30 Jan 2027")
        content must include("29 Apr 2027")
        content must include("30 Jul 2027")
        content must include("30 Oct 2027")
        content must include("30 Jan 2028")
        content must include("29 Apr 2028")
        content must include("30 Jul 2028")

        content must include(
          controllers.returnperiods.routes.EnterNonStandardPeriodDateController
            .onPageLoad(1, models.CheckMode)
            .url
        )

        content must include(
          controllers.returnperiods.routes.EnterNonStandardPeriodDateController
            .onPageLoad(8, models.CheckMode)
            .url
        )
      }
    }

    "must render NormalMode add links for all 8 missing dates" in {
      val userAnswers =
        answersWithReturnPeriodsId(1)

      stubControllerDependencies()

      val app =
        application(userAnswers)

      running(app) {
        val result =
          route(app, request).value

        status(result) mustEqual OK

        val content =
          contentAsString(result)

        (1 to 8).foreach { periodNumber =>
          content must include(
            controllers.returnperiods.routes.EnterNonStandardPeriodDateController
              .onPageLoad(periodNumber, NormalMode)
              .url
          )
        }
      }
    }

    "must omit the return periods choice row when ReturnPeriodsChoicePage is missing" in {
      val userAnswers =
        answersWithoutReturnPeriodsChoice()

      stubControllerDependencies()

      val app =
        application(userAnswers)

      running(app) {
        val result =
          route(app, request).value

        status(result) mustEqual OK

        val content =
          contentAsString(result)

        content must include("Return periods ending")
        content must not include "checkNonStandardReturnPeriods.returnPeriods.hidden"
        content must not include "Non-standard"
      }
    }

    "must display the return periods choice row when ReturnPeriodsChoicePage is present" in {
      val userAnswers =
        answersWithReturnPeriodsId(1)

      stubControllerDependencies()

      val app =
        application(userAnswers)

      running(app) {
        val result =
          route(app, request).value

        status(result) mustEqual OK

        val content =
          contentAsString(result)

        content must include("Return periods")
        content must include("Standard")
      }
    }

    "must render CheckMode links for entered dates and NormalMode links for missing dates" in {
      val userAnswers =
        answersWithReturnPeriodsId(1)
          .set(
            NonStandardPeriodDatePages(1),
            enteredDates.head
          )
          .success
          .value
          .set(
            NonStandardPeriodDatePages(4),
            enteredDates(3)
          )
          .success
          .value
          .set(
            NonStandardPeriodDatePages(8),
            enteredDates.last
          )
          .success
          .value

      stubControllerDependencies()

      val app =
        application(userAnswers)

      running(app) {
        val result =
          route(app, request).value

        status(result) mustEqual OK

        val content =
          contentAsString(result)

        Seq(1, 4, 8).foreach { periodNumber =>
          content must include(
            controllers.returnperiods.routes.EnterNonStandardPeriodDateController
              .onPageLoad(periodNumber, models.CheckMode)
              .url
          )
        }

        Seq(2, 3, 5, 6, 7).foreach { periodNumber =>
          content must include(
            controllers.returnperiods.routes.EnterNonStandardPeriodDateController
              .onPageLoad(periodNumber, NormalMode)
              .url
          )
        }
      }
    }

    "must use the current date when there are no existing NSTP values" in {
      val userAnswers =
        answersWithReturnPeriodsId(1)

      val returnPeriods =
        gamblingReturnPeriods(
          hasExistingNstpValues = Some(false),
          nstpEndDate8          = Some(existingNstpEndDate8)
        )

      val answers =
        userAnswers
          .set(GamblingReturnPeriodsPage, returnPeriods)
          .success
          .value

      stubControllerDependencies()

      val app =
        application(answers)

      running(app) {
        val result =
          route(app, request).value

        status(result) mustEqual OK

        verify(mockNstpPeriodCalculator)
          .calculate(fixedDate, 1)
      }
    }

    "must use the current date when hasExistingNstpValues is missing" in {
      val userAnswers =
        answersWithReturnPeriodsId(1)
          .set(
            GamblingReturnPeriodsPage,
            gamblingReturnPeriods(
              hasExistingNstpValues = None,
              nstpEndDate8          = Some(existingNstpEndDate8)
            )
          )
          .success
          .value

      stubControllerDependencies()

      val app =
        application(userAnswers)

      running(app) {
        val result =
          route(app, request).value

        status(result) mustEqual OK

        verify(mockNstpPeriodCalculator)
          .calculate(fixedDate, 1)
      }
    }

    "must use NSTP end date 8 when existing NSTP values are present" in {
      val userAnswers =
        answersWithReturnPeriodsId(1)
          .set(
            GamblingReturnPeriodsPage,
            gamblingReturnPeriods(
              hasExistingNstpValues = Some(true),
              nstpEndDate8          = Some(existingNstpEndDate8)
            )
          )
          .success
          .value

      stubControllerDependencies(
        baseDate = existingNstpEndDate8
      )

      val app =
        application(userAnswers)

      running(app) {
        val result =
          route(app, request).value

        status(result) mustEqual OK

        verify(mockNstpPeriodCalculator)
          .calculate(existingNstpEndDate8, 1)
      }
    }

    "must use the current date when existing NSTP values are present but NSTP end date 8 is missing" in {
      val userAnswers =
        answersWithReturnPeriodsId(1)
          .set(
            GamblingReturnPeriodsPage,
            gamblingReturnPeriods(
              hasExistingNstpValues = Some(true),
              nstpEndDate8          = None
            )
          )
          .success
          .value

      stubControllerDependencies()

      val app =
        application(userAnswers)

      running(app) {
        val result =
          route(app, request).value

        status(result) mustEqual OK

        verify(mockNstpPeriodCalculator)
          .calculate(fixedDate, 1)
      }
    }

    "must use the registration date when it is after the current date" in {
      val laterRegistrationDate =
        LocalDate.of(2027, 1, 1)

      val userAnswers =
        answersWithReturnPeriodsId(1)

      stubControllerDependencies(
        baseDate = laterRegistrationDate,
        details  = businessDetails(Some(laterRegistrationDate))
      )

      val app =
        application(userAnswers)

      running(app) {
        val result =
          route(app, request).value

        status(result) mustEqual OK

        verify(mockNstpPeriodCalculator)
          .calculate(laterRegistrationDate, 1)
      }
    }

    "must use the registration date when it is after existing NSTP end date 8" in {
      val laterRegistrationDate =
        LocalDate.of(2027, 1, 1)

      val userAnswers =
        answersWithReturnPeriodsId(1)
          .set(
            GamblingReturnPeriodsPage,
            gamblingReturnPeriods(
              hasExistingNstpValues = Some(true),
              nstpEndDate8          = Some(existingNstpEndDate8)
            )
          )
          .success
          .value

      stubControllerDependencies(
        baseDate = laterRegistrationDate,
        details  = businessDetails(Some(laterRegistrationDate))
      )

      val app =
        application(userAnswers)

      running(app) {
        val result =
          route(app, request).value

        status(result) mustEqual OK

        verify(mockNstpPeriodCalculator)
          .calculate(laterRegistrationDate, 1)
      }
    }

    "must use the registration date when it equals the reference date" in {
      val userAnswers =
        answersWithReturnPeriodsId(1)

      stubControllerDependencies(
        baseDate = fixedDate,
        details  = businessDetails(Some(fixedDate))
      )

      val app =
        application(userAnswers)

      running(app) {
        val result =
          route(app, request).value

        status(result) mustEqual OK

        verify(mockNstpPeriodCalculator)
          .calculate(fixedDate, 1)
      }
    }

    "must fail when the registration date is missing" in {
      val userAnswers =
        answersWithReturnPeriodsId(1)

      stubBusinessDetails(businessDetails(None))

      val app =
        application(userAnswers)

      running(app) {
        val result =
          route(app, request).value

        val exception =
          intercept[RuntimeException] {
            await(result)
          }

        exception.getMessage mustEqual "No registration date found"

        verifyNoInteractions(mockNstpPeriodCalculator)
      }
    }

    "must fail when getting business details fails" in {
      val userAnswers =
        answersWithReturnPeriodsId(1)

      val expectedException =
        new RuntimeException("Unable to get business details")

      when(
        mockGamblingConnector
          .getBusinessDetails(any[String])(
            any[HeaderCarrier]
          )
      ).thenReturn(
        Future.failed(expectedException)
      )

      val app =
        application(userAnswers)

      running(app) {
        val result =
          route(app, request).value

        val exception =
          intercept[RuntimeException] {
            await(result)
          }

        exception mustEqual expectedException

        verifyNoInteractions(mockNstpPeriodCalculator)
      }
    }

    "must show the final period warning when finalPeriodWarning is true" in {
      val userAnswers =
        answersWithReturnPeriodsId(1)
          .set(
            GamblingReturnPeriodsPage,
            gamblingReturnPeriods(finalPeriodWarning = Some(true))
          )
          .success
          .value

      stubControllerDependencies()

      val app =
        application(userAnswers)

      running(app) {
        val result =
          route(app, request).value

        status(result) mustEqual OK

        contentAsString(result) must include(
          "We can take up to 48 hours to approve extensions"
        )
      }
    }

    "must show the standard warning when finalPeriodWarning is false" in {
      val userAnswers =
        answersWithReturnPeriodsId(1)
          .set(
            GamblingReturnPeriodsPage,
            gamblingReturnPeriods(finalPeriodWarning = Some(false))
          )
          .success
          .value

      stubControllerDependencies()

      val app =
        application(userAnswers)

      running(app) {
        val result =
          route(app, request).value

        status(result) mustEqual OK

        contentAsString(result) must include(
          "These changes will only apply after your current return period."
        )
      }
    }

  }
}
