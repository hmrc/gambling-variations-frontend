package forms.controllingbody

import forms.behaviours.BooleanFieldBehaviours
import forms.controllingbody.ControllingBodyVATScreenerFormProvider
import play.api.data.FormError

class ControllingBodyVATScreenerFormProviderSpec extends BooleanFieldBehaviours {

  val requiredKey = "controllingBodyVATScreener.error.required"
  val invalidKey = "error.boolean"

  val form = new ControllingBodyVATScreenerFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like booleanField(
      form,
      fieldName,
      invalidError = FormError(fieldName, invalidKey)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
