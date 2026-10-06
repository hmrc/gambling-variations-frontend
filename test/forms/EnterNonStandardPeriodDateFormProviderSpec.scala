package forms

import java.time.{LocalDate, ZoneOffset}
import forms.behaviours.DateBehaviours
import forms.returnperiods.EnterNonStandardPeriodDateFormProvider
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages

class EnterNonStandardPeriodDateFormProviderSpec extends DateBehaviours {

  private implicit val messages: Messages = stubMessages()
  private val form = new EnterNonStandardPeriodDateFormProvider()()

  ".value" - {

    val validData = datesBetween(
      min = LocalDate.of(2000, 1, 1),
      max = LocalDate.now(ZoneOffset.UTC)
    )

    behave like dateField(form, "value", validData)

    behave like mandatoryDateField(form, "value", "enterNonStandardPeriodDate.error.required.all")
  }
}
