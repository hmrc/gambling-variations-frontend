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

package models

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed trait BusinessTradeClass

object BusinessTradeClass extends Enumerable.Implicits {

  case object Adultgamingcentre                extends WithName("adultGamingCentre") with BusinessTradeClass
  case object Amusementorgamingmachinesupplier extends WithName("amusementOrGamingMachineSupplier") with BusinessTradeClass
  case object Bingopromoter                    extends WithName("bingopromoter") with BusinessTradeClass
  case object BookmakerOrBettingActivities     extends WithName("bookmakerorbettingactivities") with BusinessTradeClass
  case object Casino                           extends WithName("casino") with BusinessTradeClass
  case object Club                             extends WithName("club") with BusinessTradeClass
  case object Familyentertainmentcentre        extends WithName("familyentertainmentcentre") with BusinessTradeClass
  case object Publichouse                      extends WithName("publichouse") with BusinessTradeClass
  case object Other                            extends WithName("other") with BusinessTradeClass

  val values: Seq[BusinessTradeClass] = Seq(
    Adultgamingcentre,
    Amusementorgamingmachinesupplier,
    Bingopromoter,
    BookmakerOrBettingActivities,
    Casino,
    Club,
    Familyentertainmentcentre,
    Publichouse,
    Other
  )

  def options(implicit messages: Messages): Seq[RadioItem] = values.zipWithIndex.map { case (value, index) =>
    RadioItem(
      content = Text(messages(s"businessTradeClass.${value.toString}")),
      value   = Some(value.toString),
      id      = Some(s"value_$index")
    )
  }

  implicit val enumerable: Enumerable[BusinessTradeClass] =
    Enumerable(values.map(v => v.toString -> v)*)
}
