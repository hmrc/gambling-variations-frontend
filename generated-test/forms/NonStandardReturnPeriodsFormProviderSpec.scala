package forms

import forms.behaviours.OptionFieldBehaviours
import models.NonStandardReturnPeriodsOptions
import play.api.data.FormError

//TODO
class NonStandardReturnPeriodsFormProviderSpec extends OptionFieldBehaviours {

  val form = new NonStandardReturnPeriodsOptions()()

  ".value" - {

    val fieldName = "value"
    val requiredKey = "returnPeriods.nonStandard.error.required"

    behave like optionsField[NonStandardReturnPeriodsOptions](
      form,
      fieldName,
      validValues  = NonStandardReturnPeriodsOptions.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
