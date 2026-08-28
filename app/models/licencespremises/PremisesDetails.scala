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

package models.licencespremises

import play.api.libs.json.{Json, OWrites, Reads}

import java.time.LocalDate

case class PremisesDetails(mgdRegNumber: String,
                           address1: Option[String],
                           address2: Option[String],
                           address3: Option[String],
                           address4: Option[String],
                           postcode: Option[String],
                           systemDate: Option[LocalDate]
                          )

case class PremisesDetailsResponse(
  totalRows: Option[Int],
  premises: Seq[PremisesDetails]
)

object PremisesDetails {
  implicit val reads: Reads[PremisesDetails] = Json.reads[PremisesDetails]
  implicit val writes: OWrites[PremisesDetails] = Json.writes[PremisesDetails]
}

object PremisesDetailsResponse {
  implicit val reads: Reads[PremisesDetailsResponse] = Json.reads[PremisesDetailsResponse]
  implicit val writes: OWrites[PremisesDetailsResponse] = Json.writes[PremisesDetailsResponse]
}
