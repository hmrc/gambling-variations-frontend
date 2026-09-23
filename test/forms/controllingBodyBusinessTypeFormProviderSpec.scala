package forms

import forms.behaviours.OptionFieldBehaviours
import forms.controllingbody.ControllingBodyBusinessTypeFormProvider
import models.controllingBodyBusinessType
import play.api.data.FormError

class controllingBodyBusinessTypeFormProviderSpec extends OptionFieldBehaviours {

  val form = new ControllingBodyBusinessTypeFormProvider()()

  ".value" - {

    val fieldName = "value"
    val requiredKey = "controllingBodyBusinessType.error.required"

    behave like optionsField[controllingBodyBusinessType](
      form,
      fieldName,
      validValues  = controllingBodyBusinessType.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
