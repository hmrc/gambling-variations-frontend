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

package models

import play.api.libs.json.*

import java.time.LocalDate
import java.time.format.DateTimeFormatter

//TODO this is an actual structure backend will provide from rds
final case class PartnersDetails(
  partners: Seq[PartnerDetails],
  systemDate: Option[LocalDate]
)

final case class PartnerDetails(
  mgdRegNumber: String, //TODO ✅

  dateOfJoining: Option[LocalDate],//TODO ✅
  dateOfLeaving: Option[LocalDate],//TODO ✅

  //TODO prop details are in its own object
  solePropTitle: Option[String],//TODO ✅
  solePropFirstName: Option[String],//TODO ✅
  solePropMiddleName: Option[String],//TODO ✅
  solePropLastName: Option[String],//TODO ✅

  businessName: Option[String],//TODO ✅
  tradingName: Option[String],//TODO ✅
  dateOfBirth: Option[LocalDate],//TODO ✅
  nino: Option[String],//TODO ✅
  utr: Option[String],//TODO ✅
  vrn: Option[String],//TODO ✅
  crn: Option[String],//TODO ✅
  
  dateOfIncorporation: Option[LocalDate],//TODO ✅
  countryOfIncorporation: Option[String],//TODO ✅
  foreignCorporateRef: Option[String],//TODO ✅

  address1: Option[String],//TODO ✅
  address2: Option[String],//TODO ✅
  address3: Option[String],//TODO ✅
  address4: Option[String],//TODO ✅
  postcode: Option[String],//TODO ✅
  country: Option[String],//TODO ✅

  adi: Option[String],//TODO ✅
  iomOrCiFlag: Option[String],//TODO ✅
  phoneNumber: Option[String],//TODO ✅
  mobilePhoneNumber: Option[String],//TODO ✅
  faxNumber: Option[String],//TODO ✅
  emailAddress: Option[String],//TODO ✅

  isFutureLeaveDate: Option[Int], //TODO not sure if it should be cached, seems like logic data
  isFutureJoinDate: Option[Int],  //TODO not sure if it should be cached, seems like logic data
  businessType: Option[Int]       //TODO not sure if it should be cached, seems like logic data
)
object PartnerDetails {

  private val fmt = DateTimeFormatter.ISO_LOCAL_DATE
  implicit val localDateWrites: Writes[LocalDate] =
    Writes.temporalWrites[LocalDate, DateTimeFormatter](fmt)

  implicit val format: OFormat[PartnerDetails] =
    Json.format[PartnerDetails]
}

object PartnersDetails {
  private val fmt = DateTimeFormatter.ISO_LOCAL_DATE
  implicit val localDateWrites: Writes[LocalDate] =
    Writes.temporalWrites[LocalDate, DateTimeFormatter](fmt)

  implicit val format: OFormat[PartnersDetails] =
    Json.format[PartnersDetails]
}