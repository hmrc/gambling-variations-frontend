package forms.cancellation

import forms.behaviours.BooleanFieldBehaviours
import forms.cancellation.WarningForCancellationFormProvider
import play.api.data.FormError

class WarningForCancellationFormProviderSpec extends BooleanFieldBehaviours {

  val requiredKey = "warningForCancellation.error.required"
  val invalidKey = "error.boolean"

  val form = new WarningForCancellationFormProvider()()

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
