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
import uk.gov.hmrc.govukfrontend.views.viewmodels.hint.Hint
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed trait StandardReturnPeriodsOptions

object StandardReturnPeriodsOptions extends Enumerable.Implicits {

  case object ChangeMonthsStandardPeriodCover extends WithName("changeMonthsStandardPeriodCover") with StandardReturnPeriodsOptions

  case object SwitchToNonStandard extends WithName("switchToNonStandard") with StandardReturnPeriodsOptions

  case object KeepStandardReturnPeriod extends WithName("keepStandardReturnPeriod") with StandardReturnPeriodsOptions

  val values: Seq[StandardReturnPeriodsOptions] =
    Seq(
      ChangeMonthsStandardPeriodCover,
      SwitchToNonStandard,
      KeepStandardReturnPeriod
    )

  def options(implicit messages: Messages): Seq[RadioItem] =
    Seq(
      RadioItem(
        content = Text(
          messages(
            "returnPeriods.standard.changeMonthsStandardPeriodCover"
          )
        ),
        value = Some(ChangeMonthsStandardPeriodCover.toString),
        id    = Some("value_0")
      ),
      RadioItem(
        content = Text(
          messages(
            "returnPeriods.standard.switchToNonStandard"
          )
        ),
        value = Some(SwitchToNonStandard.toString),
        id    = Some("value_1"),
        hint = Some(
          Hint(
            content = Text(
              messages(
                "returnPeriods.standard.switchToNonStandard.hint"
              )
            )
          )
        )
      ),
      RadioItem(
        divider = Some(messages("site.or"))
      ),
      RadioItem(
        content = Text(
          messages(
            "returnPeriods.standard.keepStandardReturnPeriod"
          )
        ),
        value = Some(KeepStandardReturnPeriod.toString),
        id    = Some("value_3")
      )
    )

  implicit val enumerable: Enumerable[StandardReturnPeriodsOptions] =
    Enumerable(
      values.map(v => v.toString -> v)*
    )
}
