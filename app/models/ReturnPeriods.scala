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

sealed trait ReturnPeriods

object ReturnPeriods extends Enumerable.Implicits {

  case object Option1 extends WithName("option1") with ReturnPeriods
  case object Option2 extends WithName("option2") with ReturnPeriods
  case object Option3 extends WithName("option3") with ReturnPeriods

  val values: Seq[ReturnPeriods] = Seq(
    Option1,
    Option2,
    Option3
  )

  def options(isInLastNonStandardReturnPeriod: Boolean, hint: Seq[String])(implicit messages: Messages): Seq[RadioItem] = {
    val radios =
      if isInLastNonStandardReturnPeriod then
        Seq(
          RadioItem(
            content = Text(messages(s"returnPeriods.${Option1.toString}")),
            value   = Some(Option1.toString),
            id      = Some("value_0"), // TODO
            // TODO
            hint = Some(Hint(content = Text(messages("returnPeriods.option1.hint", hint.map(messages.apply(_))*))))
          ),
          RadioItem(
            content = Text(messages(s"returnPeriods.${Option2.toString}")),
            value   = Some(Option2.toString),
            id      = Some("value_1") // TODO
          ),
          RadioItem(
            divider = Some("or") // TODO should be a message
          )
        )
      else Seq.empty

    radios ++ Seq(
      RadioItem(
        content = Text(messages(s"returnPeriods.${Option3.toString}")),
        value   = Some(Option3.toString),
        id      = Some("value_2") // TODO
      )
    )
  }

  implicit val enumerable: Enumerable[ReturnPeriods] =
    Enumerable(values.map(v => v.toString -> v)*)
}
