package forms

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class CorrespondenceFaxNumberFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "correspondenceFaxNumber.error.required"
  val lengthKey = "correspondenceFaxNumber.error.length"
  val maxLength = 20

  val form = new CorrespondenceFaxNumberFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength   = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
