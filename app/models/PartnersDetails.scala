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

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.*
import models.SoleProprietorPartnerDetails.*
import models.CorrespondenceDetails.*

import java.time.LocalDate
import java.time.format.DateTimeFormatter

final case class PartnersDetails(
  partners: Seq[PartnerDetails],
  systemDate: Option[LocalDate]
)

object PartnersDetails {
  private val fmt = DateTimeFormatter.ISO_LOCAL_DATE
  implicit val dateReads: Reads[LocalDate] =
    Reads.localDateReads(fmt)
  implicit val dateWrites: Writes[LocalDate] =
    Writes.temporalWrites[LocalDate, DateTimeFormatter](fmt)

  implicit val format: OFormat[PartnersDetails] =
    Json.format[PartnersDetails]
}

sealed trait PartnerDetails {
  val mgdRegNumber: String
  val businessPartnerNumber: String
  val dateOfJoining: Option[LocalDate]
  val dateOfLeaving: Option[LocalDate]

  val tradingName: Option[String]
  val dateOfBirth: Option[LocalDate]
  val nino: Option[String]
  val utr: Option[String]
  val vrn: Option[String]
  val crn: Option[String]
  val dateOfIncorporation: Option[LocalDate]
  val countryOfIncorporation: Option[String]
  val foreignCorporateRef: Option[String]
  val correspondenceDetails: Option[CorrespondenceDetails]

  val isFutureLeaveDate: Option[Boolean]
  val isFutureJoinDate: Option[Boolean]
  val businessType: BusinessType

  def getName: Option[String]
}

final case class SoleProprietorPartnerDetails(
  mgdRegNumber: String,
  businessPartnerNumber: String,
  dateOfJoining: Option[LocalDate],
  dateOfLeaving: Option[LocalDate],
  soleProprietorName: Option[SoleProprietorName],
  tradingName: Option[String],
  dateOfBirth: Option[LocalDate],
  nino: Option[String],
  utr: Option[String],
  vrn: Option[String],
  crn: Option[String],
  dateOfIncorporation: Option[LocalDate],
  countryOfIncorporation: Option[String],
  foreignCorporateRef: Option[String],
  correspondenceDetails: Option[CorrespondenceDetails],
  isFutureLeaveDate: Option[Boolean],
  isFutureJoinDate: Option[Boolean],
  businessType: BusinessType
) extends PartnerDetails {
  override def getName: Option[String] = soleProprietorName.map(_.fullName)
}

case class BusinessPartnerDetails(
  mgdRegNumber: String,
  businessPartnerNumber: String,
  dateOfJoining: Option[LocalDate],
  dateOfLeaving: Option[LocalDate],
  businessName: Option[String],
  tradingName: Option[String],
  dateOfBirth: Option[LocalDate],
  nino: Option[String],
  utr: Option[String],
  vrn: Option[String],
  crn: Option[String],
  dateOfIncorporation: Option[LocalDate],
  countryOfIncorporation: Option[String],
  foreignCorporateRef: Option[String],
  correspondenceDetails: Option[CorrespondenceDetails],
  isFutureLeaveDate: Option[Boolean],
  isFutureJoinDate: Option[Boolean],
  businessType: BusinessType
) extends PartnerDetails {
  override def getName: Option[String] = businessName
}

object PartnerDetails {
  private val fmt = DateTimeFormatter.ISO_LOCAL_DATE
  implicit val dateReads: Reads[LocalDate] =
    Reads.localDateReads(fmt)
  implicit val dateWrites: Writes[LocalDate] =
    Writes.temporalWrites[LocalDate, DateTimeFormatter](fmt)

  implicit val format: OWrites[PartnerDetails] = Json.writes[PartnerDetails]

  implicit val reads: Reads[PartnerDetails] =
    (__ \ "businessType").readNullable[BusinessType].flatMap {
      case Some(BusinessType.Soleproprietor) => SoleProprietorPartnerDetails.reads.widen[PartnerDetails]
      case Some(_)                           => BusinessPartnerDetails.reads.widen[PartnerDetails]
      case _ => Reads.failed("Invalid business type structure: expected businessType to contain a valid structure for PartnerDetails.")
    }
}

object SoleProprietorPartnerDetails {
  import MgdTradeDetails.intToBoolean

  implicit val format: OWrites[SoleProprietorPartnerDetails] = Json.writes[SoleProprietorPartnerDetails]

  // Note: We need separate reads for SoleProprietor name as param names are a little different from
  // what case class actually expects
  private val solePropReads: Reads[SoleProprietorName] = (
    (__ \ "solePropTitle").read[String] and
      (__ \ "solePropFirstName").read[String] and
      (__ \ "solePropMiddleName").readNullable[String] and
      (__ \ "solePropLastName").read[String]
  )(SoleProprietorName.apply _)

  implicit val reads: Reads[SoleProprietorPartnerDetails] = (
    (__ \ "mgdRegNumber").read[String] and
      (__ \ "businessPartnerNumber").read[String] and
      (__ \ "dateOfJoining").readNullable[LocalDate] and
      (__ \ "dateOfLeaving").readNullable[LocalDate] and
      solePropReads.map(Some(_)).orElse(Reads.pure(None)) and
      (__ \ "tradingName").readNullable[String] and
      (__ \ "dateOfBirth").readNullable[LocalDate] and
      (__ \ "nino").readNullable[String] and
      (__ \ "utr").readNullable[String] and
      (__ \ "vrn").readNullable[String] and
      (__ \ "crn").readNullable[String] and
      (__ \ "dateOfIncorporation").readNullable[LocalDate] and
      (__ \ "countryOfIncorporation").readNullable[String] and
      (__ \ "foreignCorporateRef").readNullable[String] and
      CorrespondenceDetails.reads.map(Some(_)).orElse(Reads.pure(None)) and
      (__ \ "isFutureLeaveDate").readNullable[Boolean] and
      (__ \ "isFutureJoinDate").readNullable[Boolean] and
      (__ \ "businessType").read[BusinessType]
  )(SoleProprietorPartnerDetails.apply _)
}

object BusinessPartnerDetails {
  import MgdTradeDetails.intToBoolean

  implicit val format: OWrites[BusinessPartnerDetails] = Json.writes[BusinessPartnerDetails]

  implicit val reads: Reads[BusinessPartnerDetails] = (
    (__ \ "mgdRegNumber").read[String] and
      (__ \ "businessPartnerNumber").read[String] and
      (__ \ "dateOfJoining").readNullable[LocalDate] and
      (__ \ "dateOfLeaving").readNullable[LocalDate] and
      (__ \ "businessName").readNullable[String] and
      (__ \ "tradingName").readNullable[String] and
      (__ \ "dateOfBirth").readNullable[LocalDate] and
      (__ \ "nino").readNullable[String] and
      (__ \ "utr").readNullable[String] and
      (__ \ "vrn").readNullable[String] and
      (__ \ "crn").readNullable[String] and
      (__ \ "dateOfIncorporation").readNullable[LocalDate] and
      (__ \ "countryOfIncorporation").readNullable[String] and
      (__ \ "foreignCorporateRef").readNullable[String] and
      CorrespondenceDetails.reads.map(Some(_)).orElse(Reads.pure(None)) and
      (__ \ "isFutureLeaveDate").readNullable[Boolean] and
      (__ \ "isFutureJoinDate").readNullable[Boolean] and
      (__ \ "businessType").read[BusinessType]
  )(BusinessPartnerDetails.apply _)
}
