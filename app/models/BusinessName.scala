/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package models

import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate

case class BusinessName(
  mgdRegNumber: String,
  solePropTitle: Option[String],
  solePropFirstName: Option[String],
  solePropMidName: Option[String],
  solePropLastName: Option[String],
  businessName: Option[String],
  businessType: BusinessType,
  tradingName: Option[String],
  systemDate: Option[LocalDate]
)

object BusinessName {
  implicit val format: OFormat[BusinessName] = Json.format[BusinessName]
}
