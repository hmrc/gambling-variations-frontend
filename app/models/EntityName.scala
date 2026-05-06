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

case class BusinessName(
  mgdRegNum: String,
  businessName: String,
  businessType: BusinessType,
  tradingName: Option[String],
  systemDate: Option[LocalDate]
) extends EntityName

case class SoleProprietorName(
  mgdRegNumber: String,
  solePropTitle: String,
  solePropFirstName: String,
  solePropMidName: Option[String],
  solePropLastName: String,
  tradingName: Option[String],
  businessType: BusinessType,
  systemDate: Option[LocalDate]
) extends EntityName

object EntityName {
  implicit val reads: Reads[EntityName] =
    (__ \ "businessType").readNullable[BusinessType].flatMap {
      case Some(BusinessType.Soleproprietor) => BusinessNameResponse.soleProprietorReads.widen[EntityName]
      case _                                 => BusinessNameResponse.businessNameReads.widen[EntityName]
    }
}

object SoleProprietorName {
  val reads: Reads[SoleProprietorName] = (
    (__ \ "mgdRegNumber").read[String] and
      (__ \ "solePropTitle").read[String] and
      (__ \ "solePropFirstName").read[String] and
      (__ \ "solePropMidName").readNullable[String] and
      (__ \ "solePropLastName").read[String] and
      (__ \ "tradingName").readNullable[String] and
      (__ \ "businessType").read[BusinessType] and
      (__ \ "systemDate").readNullable[LocalDate]
  )(SoleProprietorName.apply _)
}

object BusinessName {
  implicit val reads: Reads[BusinessName] = (
    (__ \ "mgdRegNumber").read[String] and
      (__ \ "businessName").read[String] and
      (__ \ "businessType").read[BusinessType] and
      (__ \ "tradingName").readNullable[String] and
      (__ \ "systemDate").readNullable[LocalDate]
  )(BusinessName.apply _)
}
