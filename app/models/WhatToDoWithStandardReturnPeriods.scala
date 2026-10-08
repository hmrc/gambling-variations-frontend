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

sealed trait WhatToDoWithStandardReturnPeriods

object WhatToDoWithStandardReturnPeriods extends Enumerable.Implicits {

  case object Changemonthsstandardperiodcover
    extends WithName("changeMonthsStandardPeriodCover")
      with WhatToDoWithStandardReturnPeriods

  case object Switchtononstandard
    extends WithName("switchToNonStandard")
      with WhatToDoWithStandardReturnPeriods

  case object Keepstandardreturnperiod
    extends WithName("keepStandardReturnPeriod")
      with WhatToDoWithStandardReturnPeriods

  val values: Seq[WhatToDoWithStandardReturnPeriods] =
    Seq(
      Changemonthsstandardperiodcover,
      Switchtononstandard,
      Keepstandardreturnperiod
    )

  def options(implicit messages: Messages): Seq[RadioItem] =
    Seq(
      RadioItem(
        content = Text(
          messages(
            "whatToDoWithStandardReturnPeriods.changeMonthsStandardPeriodCover"
          )
        ),
        value = Some(Changemonthsstandardperiodcover.toString),
        id = Some("value_0")
      ),
      RadioItem(
        content = Text(
          messages(
            "whatToDoWithStandardReturnPeriods.switchToNonStandard"
          )
        ),
        value = Some(Switchtononstandard.toString),
        id = Some("value_1"),
        hint = Some(
          Hint(
            content = Text(
              messages(
                "whatToDoWithStandardReturnPeriods.switchToNonStandard.hint"
              )
            )
          )
        )
      ),
      RadioItem(
        divider = Some("or")
      ),
      RadioItem(
        content = Text(
          messages(
            "whatToDoWithStandardReturnPeriods.keepStandardReturnPeriod"
          )
        ),
        value = Some(Keepstandardreturnperiod.toString),
        id = Some("value_3")
      )
    )

  implicit val enumerable: Enumerable[WhatToDoWithStandardReturnPeriods] =
    Enumerable(
      values.map(v => v.toString -> v): _*
    )
}
