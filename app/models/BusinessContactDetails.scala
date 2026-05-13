package models

import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate

final case class BusinessContactDetails(
                                  mgdRegNumber: String,
                                  phoneNumber: Option[String],
                                  mobilePhoneNumber: Option[String],
                                  faxNumber: Option[String],
                                  emailAddr: Option[String],
                                  systemDate: LocalDate
                                )

object BusinessContactDetails {
  implicit val format: OFormat[BusinessContactDetails] = Json.format[BusinessContactDetails]
}
