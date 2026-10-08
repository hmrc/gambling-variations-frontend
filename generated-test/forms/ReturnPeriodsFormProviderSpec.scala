package forms

import forms.behaviours.OptionFieldBehaviours
import models.ReturnPeriods
import play.api.data.FormError

//TODO
class ReturnPeriodsFormProviderSpec extends OptionFieldBehaviours {

  val form = new ReturnPeriodsFormProvider()()

  ".value" - {

    val fieldName = "value"
    val requiredKey = "returnPeriods.error.required"

    behave like optionsField[ReturnPeriods](
      form,
      fieldName,
      validValues  = ReturnPeriods.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
