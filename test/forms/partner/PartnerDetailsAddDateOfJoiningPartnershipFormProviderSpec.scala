package forms.partner

import forms.behaviours.DateBehaviours
import play.api.data.FormError
import play.api.i18n.{Lang, Messages}
import play.api.test.Helpers.stubMessages
import utils.DateTimeFormats.dateTimeFormat

import java.time.LocalDate

class PartnerDetailsAddDateOfJoiningPartnershipFormProviderSpec extends DateBehaviours {

  private implicit val messages: Messages = stubMessages()
  private val dateNow = LocalDate.now() // TODO not use now()
  private val twoWeeksLater = dateNow.plusDays(14)
  private val formatter = dateTimeFormat()(Lang("en"))

  private val form = new PartnerDetailsAddDateOfJoiningPartnershipFormProvider()(twoWeeksLater)

  ".value" - {

    val validData = datesBetween(
      min = dateNow.minusDays(100), // TODO investigate
      max = dateNow.plusDays(14)
    )

    behave like dateField(form, "value", validData)

    behave like mandatoryDateField(form, "value", "partnerDetailsAddDateOfJoiningPartnership.error.required.all")

    behave like dateFieldWithMax(
      form,
      "value",
      dateNow.plusDays(14), // TODO investiage
      FormError(
        "value",
        messages("partnerDetailsAddDateOfJoiningPartnership.error.invalid.range")
      )
    )
  }

}
