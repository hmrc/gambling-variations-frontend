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
import play.api.libs.json.{Reads, __}

import java.time.LocalDate

sealed trait EntityName

case class BusinessNameDetails(
  mgdRegNum: String,
  businessName: String,
  businessType: BusinessType,
  tradingName: Option[String],
  systemDate: Option[LocalDate]
) extends EntityName

case class SoleProprietorNameDetails(
  mgdRegNum: String,
  title: String,
  firstName: String,
  middleName: Option[String],
  lastName: String,
  tradingName: Option[String],
  businessType: BusinessType,
  systemDate: Option[LocalDate]
) extends EntityName

case class BusinessContactDetails(
  mgdRegNum: String,
  phoneNumber: Option[String],
  mobileNumber: Option[String],
  faxNumber: Option[String],
  emailAddr: Option[String],
  systemDate: Option[LocalDate]
) extends EntityName



object EntityName {
  implicit val reads: Reads[EntityName] =
    (__ \ "businessType").readNullable[BusinessType].flatMap {
      case Some(BusinessType.Soleproprietor) => BusinessNameResponse.soleProprietorReads.widen[EntityName]
      case _                                 => BusinessNameResponse.businessNameDetailsReads.widen[EntityName]
    }
}

object SoleProprietorNameDetails {
  val reads: Reads[SoleProprietorNameDetails] = (
    (__ \ "mgdRegNumber").read[String] and
      (__ \ "solePropTitle").read[String] and
      (__ \ "solePropFirstName").read[String] and
      (__ \ "solePropMidName").readNullable[String] and
      (__ \ "solePropLastName").read[String] and
      (__ \ "tradingName").readNullable[String] and
      (__ \ "businessType").read[BusinessType] and
      (__ \ "systemDate").readNullable[LocalDate]
  )(SoleProprietorNameDetails.apply _)
}

object BusinessNameDetails {
  implicit val reads: Reads[BusinessNameDetails] = (
    (__ \ "mgdRegNumber").read[String] and
      (__ \ "businessName").read[String] and
      (__ \ "businessType").read[BusinessType] and
      (__ \ "tradingName").readNullable[String] and
      (__ \ "systemDate").readNullable[LocalDate]
  )(BusinessNameDetails.apply _)
}

object BusinessContactDetails {
  implicit val reads: Reads[BusinessContactDetails] = (
    (__ \ "mgdRegNumber").read[String] and
      (__ \ "phoneNumber").readNullable[String] and
      (__ \ "mobilePhoneNumber").readNullable[String] and
      (__ \ "faxNumber").readNullable[String] and
      (__ \ "emailAddr").readNullable[String] and
      (__ \ "systemDate").readNullable[LocalDate]
  )(BusinessContactDetails.apply _)
}
