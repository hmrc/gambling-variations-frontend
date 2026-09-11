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

import models.licencespremises.OtherLicencesAndPermitsNI
import models.licencespremises.OtherLicencesAndPermitsNI.*
import play.api.data.Form
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{Checkboxes, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.checkboxes.{CheckboxItem, ExclusiveCheckbox}
import viewmodels.govuk.all.CheckboxesViewModel.FluentLegend
import viewmodels.govuk.all.{FieldsetViewModel, HintViewModel, LegendViewModel}

object OtherLicencesAndPermitsNIViewModel {
  def apply(form: Form[Set[OtherLicencesAndPermitsNI]])(implicit messages: Messages): Checkboxes = {
    Checkboxes(
      fieldset = Some(
        FieldsetViewModel(
          LegendViewModel(Text(messages("otherLicencesAndPermitsNI.heading"))).asPageHeading(LegendSize.Large)
        )
      ),
      name = "permitsNI[]",
      hint = Some(HintViewModel(Text(messages("otherLicencesAndPermitsNI.hint")))),
      items = positiveValues.zipWithIndex.map { case (checkedBox, index) =>
        CheckboxItem(
          id      = Some(s"permitsNI-${checkedBox.toString}"),
          content = Text(messages(s"otherLicencesAndPermitsNI.option.${checkedBox.toString}")),
          value   = checkedBox.toString,
          checked = form.value.exists(_.contains(checkedBox))
        )
      } ++ Seq(CheckboxItem(divider = Some(messages("site.or"))))
        ++ Seq(
          CheckboxItem(
            id        = Some(s"permitsNI-${noOtherLicencesAndPermits.toString}"),
            content   = Text(messages(s"otherLicencesAndPermitsNI.option.none")),
            value     = noOtherLicencesAndPermits.toString,
            checked   = form.value.exists(_.contains(noOtherLicencesAndPermits)),
            behaviour = Some(ExclusiveCheckbox)
          )
        )
    )
  }

}
