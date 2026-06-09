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

import models.BusinessTradeClass.{reads, writes}
import models.BusinessType.{reads, writes}
import models.CorrespondenceDetails.{reads, writes}
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.*

case class CorrespondenceDetails(
  mgdRegNumber: String,
  nameLine1: String,
  nameLine2: Option[String],
  correspondenceAddress: Option[Address],
  additionalInformation: Option[String],
  contactNumber: Option[ContactNumber],
  faxNumber: Option[String],
  emailAddr: Option[String]
)

object CorrespondenceDetails {

  implicit val writes: OWrites[CorrespondenceDetails] = Json.writes[CorrespondenceDetails]

  implicit val reads: Reads[CorrespondenceDetails] = (
    (__ \ "mgdRegNumber").read[String] and
      (__ \ "nameLine1").read[String] and
      (__ \ "nameLine2").readNullable[String].map(_.filter(_.nonEmpty)) and
      Address.reads.map(Some(_): Option[Address]).orElse(Reads.pure(None)) and
      (__ \ "adi").readNullable[String].map(_.filter(_.nonEmpty)) and
      ContactNumber.reads
        .map(contactNumber => if (contactNumber.phoneNumber.isEmpty && contactNumber.mobilePhoneNumber.isEmpty) None else Some(contactNumber))
        .orElse(Reads.pure(None)) and
      (__ \ "faxNumber").readNullable[String].map(_.filter(_.nonEmpty)) and
      (__ \ "emailAddr").readNullable[String].map(_.filter(_.nonEmpty))
  )(CorrespondenceDetails.apply _)

}
