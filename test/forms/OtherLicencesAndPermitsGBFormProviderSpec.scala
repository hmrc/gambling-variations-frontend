package forms

import forms.behaviours.CheckboxFieldBehaviours
import forms.licencespremises.OtherLicencesAndPermitsGBFormProvider
import models.licencespremises.OtherLicencesAndPermitsGB
import play.api.data.FormError

class OtherLicencesAndPermitsGBFormProviderSpec extends CheckboxFieldBehaviours {

  val form = new OtherLicencesAndPermitsGBFormProvider()()

  ".value" - {

    val fieldName = "permitsGB[]"
    val requiredKey = "otherLicencesAndPermitsGB.error.required"

    behave like checkboxField[OtherLicencesAndPermitsGB](
      form,
      fieldName,
      validValues  = OtherLicencesAndPermitsGB.allValues,
      invalidError = FormError(s"$fieldName[]", "error.invalid")
    )

    behave like mandatoryCheckboxField(
      form,
      fieldName,
      requiredKey
    )
  }
}
