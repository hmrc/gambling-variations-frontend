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

package viewmodels.checkAnswers.returnperiods

import base.SpecBase
import models.GamblingReturnPeriods
import play.api.i18n.Messages
import play.api.test.Helpers.running

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class NonStandardReturnPeriodsViewModelSpec extends SpecBase {

  private val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

  private implicit lazy val msgs: Messages = messages(application)

  private val todayDate = LocalDate.now()

  // Must match the view model's output format ("1 Apr 2019", not "01 Apr 2019")
  private val endDateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH)

  private def periods(
    returnPeriodsId: Option[Int] = Some(1),
    endDates: Seq[LocalDate] = Nil,
    hasExistingNstpValues: Option[Boolean] = Some(true)
  ): GamblingReturnPeriods = {
    val dates = endDates.map(Some(_)).padTo(8, None)

    GamblingReturnPeriods(
      mgdRegNumber          = "mgd1",
      returnPeriodsId       = returnPeriodsId,
      nstpEndDate1          = dates.head,
      nstpEndDate2          = dates(1),
      nstpEndDate3          = dates(2),
      nstpEndDate4          = dates(3),
      nstpEndDate5          = dates(4),
      nstpEndDate6          = dates(5),
      nstpEndDate7          = dates(6),
      nstpEndDate8          = dates(7),
      isInLastNstp          = Some(false),
      finalPeriodWarning    = Some(false),
      hasExistingNstpValues = hasExistingNstpValues
    )
  }

  private def viewModelFor(p: GamblingReturnPeriods): Option[NonStandardReturnPeriodsViewModel] =
    NonStandardReturnPeriodsViewModel.from(p)

  /** Eight quarterly end dates: today, three in the past and four in the future. */
  private val datesAroundToday: Seq[LocalDate] =
    Seq(-9, -6, -3, 0, 3, 6, 9, 12).map(m => todayDate.plusMonths(m.toLong))

  "NonStandardReturnPeriodsViewModel" - {

    "must build for every valid return period ID when hasExistingNstpValues is true" in {

      running(application) {
        Seq(1, 2, 3).foreach { id =>
          viewModelFor(periods(returnPeriodsId = Some(id))) mustBe defined
        }
      }
    }

    "must split end dates into past (including today) and future" in {

      running(application) {
        val viewModel = viewModelFor(periods(endDates = datesAroundToday)).value

        def parsed(p: NstpReturnPeriod): LocalDate = LocalDate.parse(p.endDate, endDateFormatter)

        viewModel.pastPeriods.length mustEqual 4
        viewModel.futurePeriods.length mustEqual 4

        viewModel.hasPastPeriods mustEqual true
        viewModel.hasFuturePeriods mustEqual true

        all(viewModel.pastPeriods.map(parsed)) must not be after(todayDate)
        all(viewModel.futurePeriods.map(parsed)) mustBe after(todayDate)
      }
    }

    "must return the selected months for each return period ID" in {

      running(application) {
        viewModelFor(periods(returnPeriodsId = Some(1))).value.selectedMonths mustEqual
          Seq("months.january", "months.april", "months.july", "months.october")

        viewModelFor(periods(returnPeriodsId = Some(2))).value.selectedMonths mustEqual
          Seq("months.february", "months.may", "months.august", "months.november")

        viewModelFor(periods(returnPeriodsId = Some(3))).value.selectedMonths mustEqual
          Seq("months.march", "months.june", "months.september", "months.december")
      }
    }

    "must roll end dates in the month after a quarter end back to that quarter" - {

      "for return period ID 1 (Jan, Apr, Jul, Oct)" in {

        running(application) {
          val dates = Seq(LocalDate.of(2019, 2, 3), LocalDate.of(2019, 5, 3), LocalDate.of(2019, 8, 3), LocalDate.of(2019, 11, 3))

          viewModelFor(periods(returnPeriodsId = Some(1), endDates = dates)).value.pastPeriods mustEqual Seq(
            NstpReturnPeriod(month = "Jan 2019", endDate = "3 Feb 2019"),
            NstpReturnPeriod(month = "Apr 2019", endDate = "3 May 2019"),
            NstpReturnPeriod(month = "Jul 2019", endDate = "3 Aug 2019"),
            NstpReturnPeriod(month = "Oct 2019", endDate = "3 Nov 2019")
          )
        }
      }

      "for return period ID 2 (Feb, May, Aug, Nov)" in {

        running(application) {
          val dates = Seq(LocalDate.of(2019, 3, 2), LocalDate.of(2019, 6, 2), LocalDate.of(2019, 9, 2), LocalDate.of(2019, 12, 2))

          viewModelFor(periods(returnPeriodsId = Some(2), endDates = dates)).value.pastPeriods mustEqual Seq(
            NstpReturnPeriod(month = "Feb 2019", endDate = "2 Mar 2019"),
            NstpReturnPeriod(month = "May 2019", endDate = "2 Jun 2019"),
            NstpReturnPeriod(month = "Aug 2019", endDate = "2 Sep 2019"),
            NstpReturnPeriod(month = "Nov 2019", endDate = "2 Dec 2019")
          )
        }
      }

      "for return period ID 3 (Mar, Jun, Sep, Dec), including across a year end" in {

        running(application) {
          val date = LocalDate.of(2020, 1, 1)
          val dates = Seq(-9, -6, -3, 0, 3, 6, 9, 12).map(m => date.plusMonths(m.toLong))

          viewModelFor(periods(returnPeriodsId = Some(3), endDates = dates)).value.pastPeriods mustEqual Seq(
            NstpReturnPeriod(month = "Mar 2019", endDate = "1 Apr 2019"),
            NstpReturnPeriod(month = "Jun 2019", endDate = "1 Jul 2019"),
            NstpReturnPeriod(month = "Sep 2019", endDate = "1 Oct 2019"),
            NstpReturnPeriod(month = "Dec 2019", endDate = "1 Jan 2020"),
            NstpReturnPeriod(month = "Mar 2020", endDate = "1 Apr 2020"),
            NstpReturnPeriod(month = "Jun 2020", endDate = "1 Jul 2020"),
            NstpReturnPeriod(month = "Sep 2020", endDate = "1 Oct 2020"),
            NstpReturnPeriod(month = "Dec 2020", endDate = "1 Jan 2021")
          )
        }
      }

      "but must leave end dates already in a quarter-end month unchanged" in {

        running(application) {
          val dates = Seq(LocalDate.of(2019, 3, 28), LocalDate.of(2019, 6, 28))

          viewModelFor(periods(returnPeriodsId = Some(3), endDates = dates)).value.pastPeriods mustEqual Seq(
            NstpReturnPeriod(month = "Mar 2019", endDate = "28 Mar 2019"),
            NstpReturnPeriod(month = "Jun 2019", endDate = "28 Jun 2019")
          )
        }
      }
    }

    "must not build when the return period ID is below 1 or above 3" in {

      running(application) {
        viewModelFor(periods(returnPeriodsId = Some(0))) mustBe empty
        viewModelFor(periods(returnPeriodsId = Some(4))) mustBe empty
      }
    }

    "must not build when hasExistingNstpValues is false or None" in {

      running(application) {
        viewModelFor(periods(hasExistingNstpValues = Some(false))) mustBe empty
        viewModelFor(periods(hasExistingNstpValues = None)) mustBe empty
      }
    }
  }

  private def after(date: LocalDate) =
    org.scalatest.matchers.BeMatcher[LocalDate] { left =>
      org.scalatest.matchers.MatchResult(left.isAfter(date), s"$left was not after $date", s"$left was after $date")
    }
}
