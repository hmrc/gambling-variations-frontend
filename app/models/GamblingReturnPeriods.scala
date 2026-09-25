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
import java.time.format.{DateTimeFormatter, DateTimeFormatterBuilder}
import java.util.Locale

case class GamblingReturnPeriods(
  mgdRegNumber: String,
  returnPeriodsId: Option[Int],
  nstpEndDate1: Option[LocalDate],
  nstpEndDate2: Option[LocalDate],
  nstpEndDate3: Option[LocalDate],
  nstpEndDate4: Option[LocalDate],
  nstpEndDate5: Option[LocalDate],
  nstpEndDate6: Option[LocalDate],
  nstpEndDate7: Option[LocalDate],
  nstpEndDate8: Option[LocalDate],
  isInLastNstp: Option[String],
  finalPeriodWarning: Option[String],
  hasExistingNstpValues: Option[String],
  systemDate: Option[LocalDate]
)

object GamblingReturnPeriods {

  private[models] val nstpDateFormatter: DateTimeFormatter = new DateTimeFormatterBuilder()
    .parseCaseInsensitive()
    .appendPattern("dd-MMM-yy")
    .toFormatter(Locale.ENGLISH)

  implicit val nstpDateReads: Reads[LocalDate] =
    Reads.localDateReads(nstpDateFormatter)

  implicit val nstpDateWrites: Writes[LocalDate] =
    Writes.temporalWrites[LocalDate, DateTimeFormatter](nstpDateFormatter)

  implicit val optionNstpDateReads: Reads[Option[LocalDate]] =
    Reads.optionWithNull[LocalDate](nstpDateReads)

  implicit val optionNstpDateWrites: Writes[Option[LocalDate]] =
    Writes.OptionWrites[LocalDate](nstpDateWrites)

  implicit val format: OFormat[GamblingReturnPeriods] =
    Json.format[GamblingReturnPeriods]
}
