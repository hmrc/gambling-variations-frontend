package forms

import javax.inject.Inject
import forms.mappings.Mappings
import models.BusinessContactNumber
import play.api.data.Form
import play.api.data.Forms.*
import play.api.data.validation.*

class BusinessContactNumberFormProvider @Inject() extends Mappings {

  private val AllowedCharsRegex = "^[0-9 ]+$"
  private val DigitsOnlyRegex   = "^[0-9]{11,20}$"

  private def isValidFormat(number: String): Boolean =
    number.replaceAll(" ", "").matches(DigitsOnlyRegex)

  private val phoneConstraint: Constraint[String] =
    Constraint { value =>
      val trimmed = value.trim

      if (trimmed.isEmpty) {
        Valid
      } else if (!trimmed.matches(AllowedCharsRegex)) {
        Invalid("businessContactNumber.error.phoneNumber.invalid")
      } else if (!isValidFormat(trimmed)) {
        Invalid("businessContactNumber.error.phoneNumber.invalidFormat")
      } else {
        Valid
      }
    }

  private val mobileConstraint: Constraint[String] =
    Constraint { value =>
      val trimmed = value.trim

      if (trimmed.isEmpty) {
        Valid
      } else if (!trimmed.matches(AllowedCharsRegex)) {
        Invalid("businessContactNumber.error.mobileNumber.invalid")
      } else if (!isValidFormat(trimmed)) {
        Invalid("businessContactNumber.error.mobileNumber.invalidFormat")
      } else {
        Valid
      }
    }

  def apply(): Form[BusinessContactNumber] =
    Form(
      mapping(
        "phoneNumber" ->
          optional(
            text()
              .transform(_.trim, identity)
              .verifying(phoneConstraint)
          ),
        "mobileNumber" ->
          optional(
            text()
              .transform(_.trim, identity)
              .verifying(mobileConstraint)
          )
      )(
        (phone: Option[String], mobile: Option[String]) =>
          BusinessContactNumber(phone, mobile)
      )(
        (b: BusinessContactNumber) =>
          Some((b.phoneNumber, b.mobileNumber))
      )
    )
}

