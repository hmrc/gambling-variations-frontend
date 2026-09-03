package forms.partner

import forms.behaviours.DateBehaviours
import play.api.data.FormError
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages

import java.time.LocalDate

class PartnerDetailsAddDateOfJoiningPartnershipFormProviderSpec extends DateBehaviours {

  private implicit val messages: Messages = stubMessages()
  private val dateNow = LocalDate.of(2000, 1, 1)

  private val form = new PartnerDetailsAddDateOfJoiningPartnershipFormProvider()(dateNow)

  ".value" - {

    val validData = datesBetween(
      min = dateNow,
      max = dateNow.plusDays(14)
    )

    behave like dateField(form, "value", validData)

    behave like mandatoryDateField(form, "value", "partnerDetailsAddDateOfJoiningPartnership.error.required.all")

    behave like dateFieldWithMin(
      form,
      "value",
      dateNow,
      FormError(
        "value",
        messages("partnerDetailsAddDateOfJoiningPartnership.error.invalid.range")
      )
    )

    behave like dateFieldWithMax(
      form,
      "value",
      dateNow.plusDays(14),
      FormError(
        "value",
        messages("partnerDetailsAddDateOfJoiningPartnership.error.invalid.range")
      )
    )
  }

}
