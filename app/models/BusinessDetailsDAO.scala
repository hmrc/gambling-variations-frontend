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

import play.api.libs.json.{Format, Json, OFormat, Reads, Writes}
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats
import java.time.{Instant, LocalDate}
import java.time.format.DateTimeFormatter

case class BusinessDetailsDAO(
  id: String, // mgdRegNumber
  lastUpdated: Instant,
  businessDetails: BusinessDetails
)

object BusinessDetailsDAO {

  // Instant format using HMRC Mongo helper
  implicit val instantFormat: Format[Instant] = MongoJavatimeFormats.instantFormat

  // LocalDate format for nested BusinessDetails fields
  implicit val localDateFormat: Format[LocalDate] = Format(
    Reads.localDateReads("yyyy-MM-dd"),
    Writes.temporalWrites[LocalDate, DateTimeFormatter](DateTimeFormatter.ISO_LOCAL_DATE)
  )

  // Json formatter for BusinessDetailsDAO
  implicit val format: OFormat[BusinessDetailsDAO] = Json.format[BusinessDetailsDAO]
}
