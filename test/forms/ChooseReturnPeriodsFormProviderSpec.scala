package forms

import forms.behaviours.OptionFieldBehaviours
import forms.returnperiods.ChooseReturnPeriodsFormProvider
import models.ChooseReturnPeriods
import play.api.data.FormError

class ChooseReturnPeriodsFormProviderSpec extends OptionFieldBehaviours {

  val form = new ChooseReturnPeriodsFormProvider()()

  ".value" - {

    val fieldName = "value"
    val requiredKey = "chooseReturnPeriods.error.required"

    behave like optionsField[ChooseReturnPeriods](
      form,
      fieldName,
      validValues  = ChooseReturnPeriods.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
