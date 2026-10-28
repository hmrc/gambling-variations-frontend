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

package utils

import models.{NoReturnPeriodsSet, UserAnswers}
import pages.returnperiods.{ChooseReturnPeriodsPage, GamblingReturnPeriodsPage, HasExistingNstpValuesPage, ReturnPeriodsChoicePage}

object ReturnPeriodsUtils {

  def isStandard(userAnswers: UserAnswers): Boolean = {
    val existing = userAnswers.get(GamblingReturnPeriodsPage)

    userAnswers
      .get(ReturnPeriodsChoicePage)
      .map(_ == NoReturnPeriodsSet.Standard)
      .orElse(userAnswers.get(HasExistingNstpValuesPage).map(!_))
      .orElse(existing.flatMap(_.hasExistingNstpValues).map(!_))
      .getOrElse(existing.exists(_.returnPeriodsId.isEmpty) && userAnswers.get(ChooseReturnPeriodsPage).isDefined)
  }
}
