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

case class BusinessAddress(
  mgdRegNumber: String,
  adi: Option[String],
  address: Option[Address],
  iomOrCiFlag: Option[String]
)

object BusinessAddress {

  implicit val writes: OWrites[BusinessAddress] = Json.writes[BusinessAddress]

  implicit val reads: Reads[BusinessAddress] = (
    (__ \ "mgdRegNumber").read[String] and
      (__ \ "adi").readNullable[String].map(_.filter(_.nonEmpty)) and
      Address.reads.map(Some(_): Option[Address]).orElse(Reads.pure(None)) and
      (__ \ "iomOrCiFlag").readNullable[String]
  )(BusinessAddress.apply _)

}
