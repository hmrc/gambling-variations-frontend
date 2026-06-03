package models

import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate

case class MgdTradeDetails(
                            mgdRegNumber: String,
                            isBusinessSeasonal: Option[Boolean],
                            businessTradeClass: Option[BusinessTradeClass],
                            businessActivityDesc: Option[String],
                            previousMgdRegistratonNumbers: Option[Seq[String]],
                            associatedMgdRegistratonNumbers: Option[Seq[String]],
                            systemDate: Option[LocalDate]
                          )

object MgdTradeDetails {
  implicit val format: OFormat[MgdTradeDetails] =
    Json.format[MgdTradeDetails]
}
