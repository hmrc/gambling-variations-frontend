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

package models.cancellation

import models.{Enumerable, WithName}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{Hint, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed trait ReasonForCancellation

object ReasonForCancellation extends Enumerable.Implicits {

  case object AllMachineGamesAreExcluded       extends WithName("allMachineGamesAreExcluded") with ReasonForCancellation
  case object AllTakingsFromMGDAreExcluded     extends WithName("allTakingsFromMGDAreExcluded") with ReasonForCancellation
  case object NoMachineGamesAreAvailableToPlay extends WithName("noMachineGamesAreAvailableToPlay") with ReasonForCancellation
  case object NoLongerNeedToBeRegistered       extends WithName("noLongerNeedToBeRegistered") with ReasonForCancellation
  case object BusinessIsPartOfTheGroup         extends WithName("businessIsPartOfTheGroup") with ReasonForCancellation

  private val optionsWithHint = Seq(
    AllMachineGamesAreExcluded,
    AllTakingsFromMGDAreExcluded,
    NoMachineGamesAreAvailableToPlay,
    NoLongerNeedToBeRegistered
  )
  private val optionsWithoutHint = Seq(
    BusinessIsPartOfTheGroup
  )

  val values: Seq[ReasonForCancellation] = optionsWithHint ++ optionsWithoutHint

  def options(implicit messages: Messages): Seq[RadioItem] =
    optionsWithHint.zipWithIndex.map { case (value, index) =>
      RadioItem(
        content = Text(messages(s"reasonForCancellation.${value.toString}")),
        value   = Some(value.toString),
        id      = Some(s"value_$index"),
        hint    = Some(Hint(content = Text(messages(s"reasonForCancellation.${value.toString}.hint"))))
      )

    } ++ Seq(
      RadioItem(
        divider = Some(messages("site.or"))
      )
    ) ++ optionsWithoutHint.zipWithIndex.map { case (value, index) =>
      RadioItem(
        content = Text(messages(s"reasonForCancellation.${value.toString}")),
        value   = Some(value.toString),
        id      = Some(s"value_${index + optionsWithHint.size}")
      )
    }

  implicit val enumerable: Enumerable[ReasonForCancellation] =
    Enumerable(values.map(v => v.toString -> v)*)
}
