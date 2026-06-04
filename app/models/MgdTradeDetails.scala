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

case class MgdTradeDetails(
  mgdRegNumber: String,
  isBusinessSeasonal: Boolean,
  businessTradeClass: BusinessTradeClass,
  businessActivityDesc: Option[String],
  previousMgdRegistrationNumbers: Option[Seq[String]],
  associatedMgdRegistrationNumbers: Option[Seq[String]],
  systemDate: Option[LocalDate]
)

object MgdTradeDetails {

  implicit val intToBoolean: Format[Boolean] = Format(
    Reads {
      case JsNumber(1) => JsSuccess(true)
      case JsNumber(0) => JsSuccess(false)
      case value       => JsError(s"Cannot parse number to boolean with value of $value")
    },
    Writes {
      case true  => JsNumber(1)
      case false => JsNumber(0)
    }
  )

  implicit val format: OFormat[MgdTradeDetails] =
    Json.format[MgdTradeDetails]
}
