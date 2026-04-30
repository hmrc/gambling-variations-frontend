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

case class BusinessNameResponse(
  mgdRegNumber: String,
  solePropTitle: Option[String],
  solePropFirstName: Option[String],
  solePropMidName: Option[String],
  solePropLastName: Option[String],
  businessName: Option[String],
  businessType: Option[BusinessType],
  tradingName: Option[String],
  systemDate: Option[LocalDate]
)

object BusinessNameResponse {
  implicit val format: OFormat[BusinessNameResponse] = Json.format[BusinessNameResponse]

  val soleProprietorReads: Reads[SoleProprietorName] = SoleProprietorName.reads
  val businessNameReads: Reads[BusinessName] = BusinessName.reads

  implicit val entityNameReads: Reads[EntityName] =
    (__ \ "businessType").readNullable[BusinessType].flatMap {
      case Some(BusinessType.Soleproprietor) => soleProprietorReads.widen[EntityName]
      case _                                 => businessNameReads.widen[EntityName]
    }

}
