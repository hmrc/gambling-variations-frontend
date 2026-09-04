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

package viewmodels

import models.licencespremises.OtherLicencesAndPermitsGB
import models.licencespremises.OtherLicencesAndPermitsGB.*
import play.api.data.Form
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{Checkboxes, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.checkboxes.{CheckboxItem, ExclusiveCheckbox}
import viewmodels.govuk.all.CheckboxesViewModel.FluentLegend
import viewmodels.govuk.all.{FieldsetViewModel, HintViewModel, LegendViewModel}

object OtherLicencesAndPermitsViewModel {
  def apply(form: Form[Set[OtherLicencesAndPermitsGB]])(implicit messages: Messages): Checkboxes = {
    val noneIndex = 8
    Checkboxes(
      fieldset = Some(
        FieldsetViewModel(
          LegendViewModel(Text(messages("otherLicencesAndPermitsGB.heading"))).asPageHeading(LegendSize.Large)
        )
      ),
      name = "permitsGB",
      hint = Some(HintViewModel(Text(messages("otherLicencesAndPermitsGB.hint")))),
      items = positiveValues.zipWithIndex.map { case (checkedBox, index) =>
        CheckboxItem(
          id      = Some(s"permitsGB-${checkedBox.toString}"),
          name = Some(s"permitsGB[$index]"),
          content = Text(messages(s"otherLicencesAndPermitsGB.option.${checkedBox.toString}")),
          value   = checkedBox.toString,
          checked = form.value.exists(_.contains(checkedBox))
        )
      } ++ Seq(CheckboxItem(divider = Some(messages("site.or"))))
        ++ Seq(
          CheckboxItem(
            id        = Some(s"permitsGB-none"),
            name = Some(s"permitsGB[$noneIndex]"),
            content   = Text(messages(s"otherLicencesAndPermitsGB.option.none")),
            value     = "none",
            behaviour = Some(ExclusiveCheckbox)
          )
        )
    )
  }

}
