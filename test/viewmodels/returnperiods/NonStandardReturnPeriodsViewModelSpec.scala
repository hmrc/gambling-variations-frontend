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

package viewmodels.returnperiods

import base.SpecBase
import models.GamblingReturnPeriods
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.test.FakeRequest
import play.api.test.Helpers.running
import viewmodels.checkAnswers.returnperiods.{NonStandardReturnPeriodsViewModel, NstpReturnPeriod}

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class NonStandardReturnPeriodsViewModelSpec extends SpecBase {

  private val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

  private val todayDate = LocalDate.now()

  private def createViewModel(
    returnPeriodsId: Option[Int],
    includeDates: Boolean,
    isInLastNtsp: Option[Boolean],
    finalPeriodWarning: Option[Boolean],
    hasExistingNtspValues: Option[Boolean]
  )(messages: Messages): Option[NonStandardReturnPeriodsViewModel] =
    NonStandardReturnPeriodsViewModel.from(
      GamblingReturnPeriods(
        mgdRegNumber          = "mgd1",
        returnPeriodsId       = returnPeriodsId,
        nstpEndDate1          = if includeDates then Some(todayDate.minusMonths(9)) else None,
        nstpEndDate2          = if includeDates then Some(todayDate.minusMonths(6)) else None,
        nstpEndDate3          = if includeDates then Some(todayDate.minusMonths(3)) else None,
        nstpEndDate4          = if includeDates then Some(todayDate) else None,
        nstpEndDate5          = if includeDates then Some(todayDate.plusMonths(3)) else None,
        nstpEndDate6          = if includeDates then Some(todayDate.plusMonths(6)) else None,
        nstpEndDate7          = if includeDates then Some(todayDate.plusMonths(9)) else None,
        nstpEndDate8          = if includeDates then Some(todayDate.plusMonths(12)) else None,
        isInLastNstp          = isInLastNtsp,
        finalPeriodWarning    = finalPeriodWarning,
        hasExistingNstpValues = hasExistingNtspValues
      )
    )(messages)

  "ViewModel" - {

    "is present when returnPeriodsId is correct and hasExistingNstpValues is true" in {

      running(application) {
        val viewModel1 = createViewModel(
          returnPeriodsId       = Some(1),
          includeDates          = false,
          isInLastNtsp          = Some(false),
          finalPeriodWarning    = Some(false),
          hasExistingNtspValues = Some(true)
        )(messages(application))

        val viewModel2 = createViewModel(
          returnPeriodsId       = Some(2),
          includeDates          = false,
          isInLastNtsp          = Some(false),
          finalPeriodWarning    = Some(false),
          hasExistingNtspValues = Some(true)
        )(messages(application))

        val viewModel3 = createViewModel(
          returnPeriodsId       = Some(3),
          includeDates          = false,
          isInLastNtsp          = Some(false),
          finalPeriodWarning    = Some(false),
          hasExistingNtspValues = Some(true)
        )(messages(application))

        viewModel1.isDefined mustEqual true
        viewModel2.isDefined mustEqual true
        viewModel3.isDefined mustEqual true
      }
    }

    "correctly splits dates from past to future" in {

      running(application) {
        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)

        val viewModel = createViewModel(
          returnPeriodsId       = Some(1),
          includeDates          = true,
          isInLastNtsp          = Some(false),
          finalPeriodWarning    = Some(false),
          hasExistingNtspValues = Some(true)
        )(messages(application))

        viewModel.isDefined mustEqual true

        viewModel.get.pastPeriods.length mustEqual 4
        viewModel.get.futurePeriods.length mustEqual 4

        viewModel.get.hasPastPeriods mustEqual true
        viewModel.get.hasFuturePeriods mustEqual true

        viewModel.get.pastPeriods.forall(e => LocalDate.parse(e.endDate, formatter).isBefore(todayDate))
        viewModel.get.futurePeriods.forall(e => LocalDate.parse(e.endDate, formatter).isAfter(todayDate))
      }
    }

    "correctly parses selected months" in {

      running(application) {

        val viewModel1 = createViewModel(
          returnPeriodsId       = Some(1),
          includeDates          = true,
          isInLastNtsp          = Some(false),
          finalPeriodWarning    = Some(false),
          hasExistingNtspValues = Some(true)
        )(messages(application))

        val viewModel2 = createViewModel(
          returnPeriodsId       = Some(2),
          includeDates          = true,
          isInLastNtsp          = Some(false),
          finalPeriodWarning    = Some(false),
          hasExistingNtspValues = Some(true)
        )(messages(application))

        val viewModel3 = createViewModel(
          returnPeriodsId       = Some(3),
          includeDates          = true,
          isInLastNtsp          = Some(false),
          finalPeriodWarning    = Some(false),
          hasExistingNtspValues = Some(true)
        )(messages(application))

        viewModel1.isDefined mustEqual true
        viewModel2.isDefined mustEqual true
        viewModel3.isDefined mustEqual true

        viewModel1.get.selectedMonths mustEqual Seq("months.january", "months.april", "months.july", "months.october")
        viewModel2.get.selectedMonths mustEqual Seq("months.february", "months.may", "months.august", "months.november")
        viewModel3.get.selectedMonths mustEqual Seq("months.march", "months.june", "months.september", "months.december")
      }
    }

    "correctly adjust date" in {
      running(application) {
        val date = LocalDate.of(2020, 1, 1)
        val viewModel = NonStandardReturnPeriodsViewModel.from(
          GamblingReturnPeriods(
            mgdRegNumber          = "mgd1",
            returnPeriodsId       = Some(3),
            nstpEndDate1          = Some(date.minusMonths(9)),
            nstpEndDate2          = Some(date.minusMonths(6)),
            nstpEndDate3          = Some(date.minusMonths(3)),
            nstpEndDate4          = Some(date),
            nstpEndDate5          = Some(date.plusMonths(3)),
            nstpEndDate6          = Some(date.plusMonths(6)),
            nstpEndDate7          = Some(date.plusMonths(9)),
            nstpEndDate8          = Some(date.plusMonths(12)),
            isInLastNstp          = Some(true),
            finalPeriodWarning    = Some(true),
            hasExistingNstpValues = Some(true)
          )
        )(messages(application))

        viewModel.get.pastPeriods.head mustEqual NstpReturnPeriod(
          month   = "Mar 2019",
          endDate = "01 Apr 2019"
        )
        viewModel.get.pastPeriods(1) mustEqual NstpReturnPeriod(
          month   = "Jun 2019",
          endDate = "01 Jul 2019"
        )
        viewModel.get.pastPeriods(2) mustEqual NstpReturnPeriod(
          month   = "Oct 2019",
          endDate = "01 Oct 2019"
        )
        viewModel.get.pastPeriods(3) mustEqual NstpReturnPeriod(
          month   = "Dec 2019",
          endDate = "01 Jan 2020"
        )
        viewModel.get.pastPeriods(4) mustEqual NstpReturnPeriod(
          month   = "Mar 2020",
          endDate = "01 Apr 2020"
        )
        viewModel.get.pastPeriods(5) mustEqual NstpReturnPeriod(
          month   = "Jun 2020",
          endDate = "01 Jul 2020"
        )
        viewModel.get.pastPeriods(6) mustEqual NstpReturnPeriod(
          month   = "Oct 2020",
          endDate = "01 Oct 2020"
        )
        viewModel.get.pastPeriods(7) mustEqual NstpReturnPeriod(
          month   = "Dec 2020",
          endDate = "01 Jan 2021"
        )
      }
    }

    "failed to build when returnPeriodsId is below 0 and above 3" in {

      running(application) {
        val viewModel1 = createViewModel(
          returnPeriodsId       = Some(4),
          includeDates          = false,
          isInLastNtsp          = Some(false),
          finalPeriodWarning    = Some(false),
          hasExistingNtspValues = Some(true)
        )(messages(application))

        val viewModel2 = createViewModel(
          returnPeriodsId       = Some(0),
          includeDates          = false,
          isInLastNtsp          = Some(false),
          finalPeriodWarning    = Some(false),
          hasExistingNtspValues = Some(true)
        )(messages(application))

        viewModel1.isEmpty mustEqual true
        viewModel2.isEmpty mustEqual true
      }
    }

    "failed to build when hasExistingNstpValues is None or false" in {

      running(application) {
        val viewModel1 = createViewModel(
          returnPeriodsId       = Some(1),
          includeDates          = false,
          isInLastNtsp          = Some(false),
          finalPeriodWarning    = Some(false),
          hasExistingNtspValues = Some(false)
        )(messages(application))

        val viewModel2 = createViewModel(
          returnPeriodsId       = Some(1),
          includeDates          = false,
          isInLastNtsp          = Some(false),
          finalPeriodWarning    = Some(false),
          hasExistingNtspValues = None
        )(messages(application))

        viewModel1.isEmpty mustEqual true
        viewModel2.isEmpty mustEqual true
      }
    }

  }

}
