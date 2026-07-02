package forms

import forms.behaviours.BooleanFieldBehaviours
import play.api.data.FormError

class CorrespondenceAddrAdditionalInfoScreenerFormProviderSpec extends BooleanFieldBehaviours {

  val requiredKey = "correspondenceAddrAdditionalInfoScreener.error.required"
  val invalidKey = "error.boolean"

  val form = new CorrespondenceAddrAdditionalInfoScreenerFormProvider()()

  ".correspondenceAddrAdditionalInfoScreener" - {

    val fieldName = "correspondenceAddrAdditionalInfoScreener"

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
