package forms

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class ChangeSoleProprietorNameFormProviderSpec extends StringFieldBehaviours {

  val form = new ChangeSoleProprietorNameFormProvider()()

  ".firstName" - {

    val fieldName = "firstName"
    val requiredKey = "changeSoleProprietorName.error.firstName.required"
    val lengthKey = "changeSoleProprietorName.error.firstName.length"
    val maxLength = 100

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }

  ".middleName" - {

    val fieldName = "middleName"
    val requiredKey = "changeSoleProprietorName.error.middleName.required"
    val lengthKey = "changeSoleProprietorName.error.middleName.length"
    val maxLength = 100

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
