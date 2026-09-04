package forms.licencespremises

import forms.behaviours.CheckboxFieldBehaviours
import forms.licencespremises.OtherLicencesAndPermitsGBFormProvider
import models.UserAnswers
import models.licencespremises.OtherLicencesAndPermitsGB
import models.licencespremises.OtherLicencesAndPermitsGB.*
import play.api.data.FormError
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.test.Helpers.stubMessages

class OtherLicencesAndPermitsGBFormProviderSpec extends CheckboxFieldBehaviours {

  val form = new OtherLicencesAndPermitsGBFormProvider()()
  private implicit val messages: Messages = stubMessages()
  val userAnswers = UserAnswers(
    "id",
    Json.obj(
      "licencesPremisesSection" -> Json.obj(
        "mgdRegNum" -> "XGM000001761",
        "clubGaming" -> "1",
        "clubMachine" -> "0",
        "clubPremises" -> "0",
        "familyEntertainment" -> "1",
        "localAuthority" -> "0",
        "onPremises" -> "1",
        "prizeGaming" -> "0"
      )
    )
  )

  ".permitsGB" - {

    val fieldName = "permitsGB"
    val requiredKey = "otherLicencesAndPermitsGB.error.required"

    behave like checkboxField[OtherLicencesAndPermitsGB](
      form,
      fieldName,
      validValues  = OtherLicencesAndPermitsGB.values,
      invalidError = FormError(s"$fieldName[0]", "error.invalid")
    )

    behave like mandatoryCheckboxField(
      form,
      fieldName,
      requiredKey
    )
  }
}
