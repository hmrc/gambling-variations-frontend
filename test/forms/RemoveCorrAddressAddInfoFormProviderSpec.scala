package forms

import forms.behaviours.BooleanFieldBehaviours
import play.api.data.FormError

class RemoveCorrAddressAddInfoFormProviderSpec extends BooleanFieldBehaviours {

  val requiredKey = "removeAdditionalAddrInfo.error.required"
  val invalidKey = "error.boolean"

  val form = new RemoveCorrAddressAddInfoFormProvider()()

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
