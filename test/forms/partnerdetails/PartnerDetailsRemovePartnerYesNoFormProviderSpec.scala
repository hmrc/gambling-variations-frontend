package forms.partnerdetails

import forms.behaviours.BooleanFieldBehaviours
import play.api.data.FormError

class PartnerDetailsRemovePartnerYesNoFormProviderSpec extends BooleanFieldBehaviours {

  val requiredKey = "partnerDetailsRemovePartnerYesNo.error.required"
  val invalidKey = "error.boolean"

  val form = new PartnerDetailsRemovePartnerYesNoFormProvider()()

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
