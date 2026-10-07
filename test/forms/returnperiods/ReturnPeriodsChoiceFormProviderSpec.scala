package forms.returnperiods

import forms.behaviours.OptionFieldBehaviours
import models.ReturnPeriodsChoice
import play.api.data.FormError

class ReturnPeriodsChoiceFormProviderSpec extends OptionFieldBehaviours {

  val form = new ReturnPeriodsChoiceFormProvider()()

  ".value" - {

    val fieldName = "value"
    val requiredKey = "returnPeriodsChoice.error.required"

    behave like optionsField[ReturnPeriodsChoice](
      form,
      fieldName,
      validValues  = ReturnPeriodsChoice.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
