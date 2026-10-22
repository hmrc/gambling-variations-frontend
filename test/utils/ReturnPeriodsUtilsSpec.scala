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

import base.SpecBase
import models.{GamblingReturnPeriods, NoReturnPeriodsSet, UserAnswers}
import pages.returnperiods.{ChooseReturnPeriodsPage, GamblingReturnPeriodsPage, HasExistingNstpValuesPage, ReturnPeriodsChoicePage}

class ReturnPeriodsUtilsSpec extends SpecBase {

  private def held(hasExistingNstpValues: Option[Boolean], returnPeriodsId: Option[Int] = Some(1)) =
    GamblingReturnPeriods(mgdRegNum, returnPeriodsId, None, None, None, None, None, None, None, None, None, None, hasExistingNstpValues)

  private def answers(held: GamblingReturnPeriods): UserAnswers =
    emptyUserAnswers.set(GamblingReturnPeriodsPage, held).success.value

  "ReturnPeriodsUtils.isStandard" - {

    "must use the choice made when no return periods were set, ahead of everything else" in {
      val nonStandardHeld = answers(held(Some(true))).set(HasExistingNstpValuesPage, true).success.value

      ReturnPeriodsUtils.isStandard(nonStandardHeld.set(ReturnPeriodsChoicePage, NoReturnPeriodsSet.Standard).success.value) mustBe true
      ReturnPeriodsUtils.isStandard(
        answers(held(Some(false))).set(ReturnPeriodsChoicePage, NoReturnPeriodsSet.Nonstandard).success.value
      ) mustBe false
    }

    "must use the type stored by the choose return periods page ahead of the value held for the registration" in {
      ReturnPeriodsUtils.isStandard(answers(held(Some(true))).set(HasExistingNstpValuesPage, false).success.value) mustBe true
      ReturnPeriodsUtils.isStandard(answers(held(Some(false))).set(HasExistingNstpValuesPage, true).success.value) mustBe false
    }

    "must use the value held for the registration when nothing has been chosen" in {
      ReturnPeriodsUtils.isStandard(answers(held(Some(false)))) mustBe true
      ReturnPeriodsUtils.isStandard(answers(held(Some(true)))) mustBe false
    }

    "must treat chosen months as standard when no return periods are set and the type is not known" in {
      val notSet = answers(held(None, returnPeriodsId = None))

      ReturnPeriodsUtils.isStandard(notSet.set(ChooseReturnPeriodsPage, 2).success.value) mustBe true
      ReturnPeriodsUtils.isStandard(notSet) mustBe false
    }

    "must not be standard when the type is not known" in {
      ReturnPeriodsUtils.isStandard(answers(held(None))) mustBe false
      ReturnPeriodsUtils.isStandard(emptyUserAnswers) mustBe false
    }
  }
}
