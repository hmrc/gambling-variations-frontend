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
import viewmodels.checkAnswers.returnperiods.NstpReturnPeriod.*

import java.time.LocalDate

final case class NonStandardReturnPeriodsViewModel(
  isInLastNonStandardReturnPeriod: Boolean,
  selectedMonths: Seq[String],
  pastPeriods: Seq[NstpReturnPeriod],
  futurePeriods: Seq[NstpReturnPeriod]
) {
  def hasPastPeriods: Boolean = pastPeriods.nonEmpty
  def hasFuturePeriods: Boolean = futurePeriods.nonEmpty
}

object NonStandardReturnPeriodsViewModel {

  def from(periods: GamblingReturnPeriods, today: LocalDate = LocalDate.now())(implicit
    messages: Messages
  ): Option[NonStandardReturnPeriodsViewModel] =
    for {
      _            <- periods.hasExistingNstpValues.filter(identity)
      isInLastNstp <- periods.isInLastNstp
      id           <- NstpReturnPeriod.validateReturnPeriodId(periods.returnPeriodsId)
    } yield {
      val (past, future) = NstpReturnPeriod.pastAndFuture(periods, today)

      NonStandardReturnPeriodsViewModel(
        isInLastNonStandardReturnPeriod = isInLastNstp,
        selectedMonths                  = NstpReturnPeriod.parseSelectedMonth(id),
        pastPeriods                     = past.map(NstpReturnPeriod.formatNstpDate(id, _)),
        futurePeriods                   = future.map(NstpReturnPeriod.formatNstpDate(id, _))
      )
    }
}
