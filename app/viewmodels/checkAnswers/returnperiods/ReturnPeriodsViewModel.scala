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
import java.util.Locale

final case class ReturnPeriodsViewModel(
  hasNonStandardReturnPeriods: Boolean,
  isInLastNonStandardReturnPeriod: Boolean,
  hasPastPeriods: Boolean,
  hasFuturePeriods: Boolean,
  selectedMonths: Seq[String],
  pastPeriods: Seq[NstpReturnPeriod],
  futurePeriods: Seq[NstpReturnPeriod]
)

final case class NstpReturnPeriod(
  month: String,
  endDate: String
)

object ReturnPeriodsViewModel {

  def from(gamblingReturnPeriods: GamblingReturnPeriods)(implicit messages: Messages): Option[ReturnPeriodsViewModel] = {
    val today = LocalDate.now()

    val hasExistingNstpValues = gamblingReturnPeriods.hasExistingNstpValues.getOrElse(false)

    for {
      isInLastNstp    <- gamblingReturnPeriods.isInLastNstp
      returnPeriodsId <- validateReturnPeriodId(gamblingReturnPeriods.returnPeriodsId)

      viewModel <- if hasExistingNstpValues then {
                     val nstpEndDates = Seq(
                       gamblingReturnPeriods.nstpEndDate1,
                       gamblingReturnPeriods.nstpEndDate2,
                       gamblingReturnPeriods.nstpEndDate3,
                       gamblingReturnPeriods.nstpEndDate4,
                       gamblingReturnPeriods.nstpEndDate5,
                       gamblingReturnPeriods.nstpEndDate6,
                       gamblingReturnPeriods.nstpEndDate7,
                       gamblingReturnPeriods.nstpEndDate8
                       // TODO flattening, but if hasExistingNstpValues, they all should exist
                     ).flatten.span(_.isBefore(today.plusDays(1)))

                     Some(
                       ReturnPeriodsViewModel(
                         hasNonStandardReturnPeriods     = hasExistingNstpValues,
                         isInLastNonStandardReturnPeriod = isInLastNstp,
                         hasPastPeriods                  = nstpEndDates._1.nonEmpty,
                         hasFuturePeriods                = nstpEndDates._2.nonEmpty,
                         selectedMonths                  = parseSelectedMonth(returnPeriodsId),
                         pastPeriods                     = nstpEndDates._1.map(formatNstpDate(returnPeriodsId, _)),
                         futurePeriods                   = nstpEndDates._2.map(formatNstpDate(returnPeriodsId, _))
                       )
                     )
                   } else None
    } yield viewModel
  }

  private def formatNstpDate(returnPeriodId: Int, returnPeriodDate: LocalDate)(implicit messages: Messages): NstpReturnPeriod = returnPeriodId match {
    case 1 =>
      val monthAdjusted = returnPeriodDate.getMonth match {
        case Month.FEBRUARY | Month.MAY | Month.AUGUST | Month.NOVEMBER =>
          returnPeriodDate.minusMonths(1)
        case _ =>
          returnPeriodDate
      }
      NstpReturnPeriod(
        month   = formatDate(monthAdjusted, false),
        endDate = formatDate(returnPeriodDate, true)
      )
    case 2 =>
      val monthAdjusted = returnPeriodDate.getMonth match {
        case Month.MARCH | Month.JUNE | Month.SEPTEMBER | Month.DECEMBER =>
          returnPeriodDate.minusMonths(1)
        case _ =>
          returnPeriodDate
      }
      NstpReturnPeriod(
        month   = formatDate(monthAdjusted, false),
        endDate = formatDate(returnPeriodDate, true)
      )
    case 3 =>
      val monthAdjusted = returnPeriodDate.getMonth match {
        case Month.APRIL | Month.JULY | Month.NOVEMBER | Month.JANUARY =>
          returnPeriodDate.minusMonths(1)
        case _ =>
          returnPeriodDate
      }
      NstpReturnPeriod(
        month   = formatDate(monthAdjusted, false),
        endDate = formatDate(returnPeriodDate, true)
      )
    case _ =>
      // TODO error?
      NstpReturnPeriod(
        month   = "Something went wrong",
        endDate = "Something went wrong"
      )
  }

  private def validateReturnPeriodId(returnPeriodId: Option[Int]): Option[Int] = returnPeriodId.flatMap(id =>
    if id > 0 && id <= 3 then Some(id)
    else None
  )

  private def parseSelectedMonth(returnPeriodId: Int): Seq[String] = {
    returnPeriodId match {
      case 1 => Seq("months.january", "months.april", "months.july", "months.october")
      case 2 => Seq("months.february", "months.may", "months.august", "months.november")
      case 3 => Seq("months.march", "months.june", "months.september", "months.december")
      case _ => Seq.empty
    }
  }

  private def formatDate(date: LocalDate, fullDate: Boolean)(implicit messages: Messages): String = {
    val locale = Locale.forLanguageTag(messages.lang.code)
    val formatter =
      if fullDate then DateTimeFormatter.ofPattern("dd MMM yyyy", locale)
      else DateTimeFormatter.ofPattern("MMM yyyy", locale)

    date.format(formatter)
  }

}
