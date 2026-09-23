package forms.controllingbody

import forms.behaviours.OptionFieldBehaviours
import forms.controllingbody.ControllingBodyBusinessTypeFormProvider
import models.BusinessType
import play.api.data.FormError

class ControllingBodyBusinessTypeFormProviderSpec extends OptionFieldBehaviours {

  val form = new ControllingBodyBusinessTypeFormProvider()()

  ".value" - {

    val fieldName = "value"
    val requiredKey = "controllingBodyBusinessType.error.required"

    behave like optionsField[BusinessType](
      form,
      fieldName,
      validValues  = BusinessType.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
