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

import models.GamblingReturnPeriods
import play.api.i18n.Messages

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, Month}

final case class NstpReturnPeriod(month: String, endDate: String)

object NstpReturnPeriod {

  /** Quarter-end months for each return period ID. */
  private val quarterEndMonths: Map[Int, Seq[Month]] = Map(
    1 -> Seq(Month.JANUARY, Month.APRIL, Month.JULY, Month.OCTOBER),
    2 -> Seq(Month.FEBRUARY, Month.MAY, Month.AUGUST, Month.NOVEMBER),
    3 -> Seq(Month.MARCH, Month.JUNE, Month.SEPTEMBER, Month.DECEMBER)
  )

  private[returnperiods] def validateReturnPeriodId(returnPeriodId: Option[Int]): Option[Int] =
    returnPeriodId.filter(quarterEndMonths.contains)

  private[returnperiods] def parseSelectedMonth(returnPeriodId: Int): Seq[String] =
    quarterEndMonths.getOrElse(returnPeriodId, Nil).map(m => s"months.${m.toString.toLowerCase}")

  /** Set end dates in date order, split into past (up to and including today) and future. */
  private[returnperiods] def pastAndFuture(periods: GamblingReturnPeriods, today: LocalDate): (Seq[LocalDate], Seq[LocalDate]) =
    Seq(
      periods.nstpEndDate1,
      periods.nstpEndDate2,
      periods.nstpEndDate3,
      periods.nstpEndDate4,
      periods.nstpEndDate5,
      periods.nstpEndDate6,
      periods.nstpEndDate7,
      periods.nstpEndDate8
    ).flatten.sorted.partition(!_.isAfter(today))

  /** An end date that falls in the month after a quarter end belongs to that quarter. */
  private[returnperiods] def formatNstpDate(returnPeriodId: Int, endDate: LocalDate)(implicit messages: Messages): NstpReturnPeriod = {
    val quarterEnds = quarterEndMonths.getOrElse(returnPeriodId, Nil)
    val periodMonth =
      if (quarterEnds.contains(endDate.getMonth.minus(1))) endDate.minusMonths(1)
      else endDate

    NstpReturnPeriod(
      month   = format(periodMonth, "MMM yyyy"),
      endDate = format(endDate, "d MMM yyyy")
    )
  }

  private def format(date: LocalDate, pattern: String)(implicit messages: Messages): String =
    date.format(DateTimeFormatter.ofPattern(pattern, messages.lang.locale))
}
