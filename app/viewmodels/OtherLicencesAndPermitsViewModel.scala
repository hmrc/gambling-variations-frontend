package viewmodels

import models.licencespremises.OtherLicencesAndPermitsGB
import models.licencespremises.OtherLicencesAndPermitsGB.*
import models.UserAnswers
import play.api.data.Form
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{Checkboxes, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.checkboxes.{CheckboxItem, ExclusiveCheckbox}
import viewmodels.govuk.all.CheckboxesViewModel.FluentLegend
import viewmodels.govuk.all.{FieldsetViewModel, FluentValue, HintViewModel, LegendViewModel, stringToText}

object OtherLicencesAndPermitsViewModel {
  def getSelectedLicencesAndPermits(ua: UserAnswers)(implicit messages: Messages): Set[OtherLicencesAndPermitsGB] = {
    val trueVal = "1"
    /**iterates the types and their corresponding pages to check for a "1"
    then converts to a set for the form to read**/
    mappedValuesWithPages.keys.filter(value => ua.get(mappedValuesWithPages(value)).contains(trueVal)).toSet
  }

  def otherLPCheckboxItems(form: Form[Set[OtherLicencesAndPermitsGB]])(implicit messages: Messages): Checkboxes = {
    Checkboxes(
      // form mapping doesn't work without [] in the name
      name = s"permitsGB[]",
      idPrefix = Some("permitsGB"),
      fieldset = Some(
        FieldsetViewModel(
          LegendViewModel(Text(messages("otherLicencesAndPermitsGB.heading"))).asPageHeading(LegendSize.Large)
        )
      ),
      hint = Some(HintViewModel(Text(messages("otherLicencesAndPermitsGB.hint")))),
      items = positiveValues.zipWithIndex.map { case (checkedBox, index) =>
        CheckboxItem(
          content = Text(messages(s"otherLicencesAndPermitsGB.option.${checkedBox.toString}")),
          value = checkedBox.toString,
          checked = form.value.exists(_.contains(checkedBox))
        )
      } ++ Seq(CheckboxItem(divider = Some(messages("site.or"))))
        ++ Seq(
        CheckboxItem(
          content = Text(messages(s"otherLicencesAndPermitsGB.option.none")),
          value = "none",
          behaviour = Some(ExclusiveCheckbox)
        )
      )
    )
  }

}
