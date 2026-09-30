/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package models.controllingbody

import models.BusinessType
import play.api.libs.json.{Json, OWrites, Reads}

import java.time.LocalDate

final case class ControlBodyDetails(
  mgdRegNumber: String,
  businessPartnerNumber: Option[String],
  dateOfJoining: Option[LocalDate],
  dateOfLeaving: Option[LocalDate],
  solePropTitle: Option[String],
  solePropFirstName: Option[String],
  solePropMiddleName: Option[String],
  solePropLastName: Option[String],
  businessName: Option[String],
  tradingName: Option[String],
  dateOfBirth: Option[LocalDate],
  nino: Option[String],
  utr: Option[Long],
  vrn: Option[Long],
  crn: Option[String],
  dateOfIncorporation: Option[LocalDate],
  countryOfIncorporation: Option[String],
  foreignCorporateRef: Option[String],
  address1: Option[String],
  address2: Option[String],
  address3: Option[String],
  address4: Option[String],
  postcode: Option[String],
  country: Option[String],
  adi: Option[String],
  isIomOrCiFlag: Option[String],
  phoneNumber: Option[String],
  mobilePhoneNumber: Option[String],
  faxNumber: Option[String],
  emailAddr: Option[String],
  typeOfControllingBody: Option[BusinessType],
  isRepMemSameAsCb: Option[String],
  isUkIncorporated: Option[String]
)

object ControlBodyDetails {
  implicit val reads: Reads[ControlBodyDetails] = Json.reads[ControlBodyDetails]
  implicit val writes: OWrites[ControlBodyDetails] = Json.writes[ControlBodyDetails]
}
