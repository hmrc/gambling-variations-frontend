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

package services.returnperiods

import java.time.{LocalDate, YearMonth}

import javax.inject.Singleton

@Singleton
class NstpPeriodCalculator {

  private val numberOfPeriods = 8

  def calculate(
    baseDate: LocalDate,
    returnPeriodsId: Int
  ): Seq[LocalDate] = {

    val staggerMonths = returnPeriodsId match {
      case 1 => Seq(1, 4, 7, 10)
      case 2 => Seq(2, 5, 8, 11)
      case 3 => Seq(3, 6, 9, 12)
      case _ =>
        throw new IllegalArgumentException(
          s"Invalid return periods id: $returnPeriodsId"
        )
    }

    val firstPeriod = nextPeriod(baseDate, staggerMonths)

    (0 until numberOfPeriods).map { index =>
      YearMonth
        .from(firstPeriod.plusMonths(index * 3L))
        .atEndOfMonth()
    }
  }

  private def nextPeriod(
    baseDate: LocalDate,
    staggerMonths: Seq[Int]
  ): YearMonth = {

    val currentMonth = baseDate.getMonthValue

    val month =
      staggerMonths
        .find(_ > currentMonth)
        .getOrElse(staggerMonths.head)

    val year =
      if (month > currentMonth) {
        baseDate.getYear
      } else {
        baseDate.getYear + 1
      }

    YearMonth.of(year, month)
  }
}
