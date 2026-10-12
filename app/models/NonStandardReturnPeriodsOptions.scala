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
import uk.gov.hmrc.govukfrontend.views.Aliases.{Content, HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.hint.Hint
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed trait NonStandardReturnPeriodsOptions

object NonStandardReturnPeriodsOptions extends Enumerable.Implicits {

  case object SwitchToStandard              extends WithName("switchToStandard") with NonStandardReturnPeriodsOptions
  case object KeepNonStandardAndChangeDates extends WithName("keepNonStandardAndChangeDates") with NonStandardReturnPeriodsOptions
  case object KeepNonStandard               extends WithName("keepNonStandard") with NonStandardReturnPeriodsOptions

  val values: Seq[NonStandardReturnPeriodsOptions] = Seq(
    SwitchToStandard,
    KeepNonStandardAndChangeDates,
    KeepNonStandard
  )

  def options(isInLastNonStandardReturnPeriod: Boolean, hint: Seq[String])(implicit messages: Messages): Seq[RadioItem] = {
    val radios =
      if isInLastNonStandardReturnPeriod then
        Seq(
          RadioItem(
            content    = Text(messages(s"returnPeriods.nonStandard.${SwitchToStandard.toString}")),
            value      = Some(SwitchToStandard.toString),
            id         = Some("value_0"),
            hint       = Some(Hint(content = Text(messages("returnPeriods.nonStandard.switchToStandard.hint", hint.map(messages.apply(_))*)))),
            attributes = Map("data-testid" -> s"return-periods-${SwitchToStandard.toString}")
          ),
          RadioItem(
            content    = Text(messages(s"returnPeriods.nonStandard.${KeepNonStandardAndChangeDates.toString}")),
            value      = Some(KeepNonStandardAndChangeDates.toString),
            id         = Some("value_1"),
            attributes = Map("data-testid" -> s"return-periods-${KeepNonStandardAndChangeDates.toString}")
          ),
          RadioItem(
            divider = Some(messages("site.or"))
          )
        )
      else Seq.empty

    radios ++ Seq(
      RadioItem(
        content    = Text(messages(s"returnPeriods.nonStandard.${KeepNonStandard.toString}")),
        value      = Some(KeepNonStandard.toString),
        id         = Some("value_2"),
        attributes = Map("-testid" -> s"return-periods-${KeepNonStandard.toString}")
      )
    )
  }

  implicit val enumerable: Enumerable[NonStandardReturnPeriodsOptions] =
    Enumerable(values.map(v => v.toString -> v)*)
}
