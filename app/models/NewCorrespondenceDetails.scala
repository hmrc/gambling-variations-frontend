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

import play.api.libs.functional.syntax.*
import play.api.libs.json.*

case class NewCorrespondenceDetails(
  mgdRegNumber: String,
  nameLine1: Option[String],
  nameLine2: Option[String],
  correspondenceAddress: Option[Address],
  additionalInformation: Option[String],
  iomOrCiFlag: Option[String],
  contactNumber: Option[ContactNumber],
  faxNumber: Option[String],
  emailAddr: Option[String]
)

object NewCorrespondenceDetails {

  implicit val writes: OWrites[NewCorrespondenceDetails] = Json.writes[NewCorrespondenceDetails]

  private val iomOrCiFlagReads: Reads[Option[String]] =
    (__ \ "iomOrCiFlag").readNullable[JsValue].map {
      case Some(JsBoolean(b)) => Some(b.toString)
      case Some(JsString(s))  => Some(s)
      case _                  => None
    }

  implicit val reads: Reads[NewCorrespondenceDetails] = (
    (__ \ "mgdRegNumber").read[String] and
      (__ \ "nameLine1").readNullable[String] and
      (__ \ "nameLine2").readNullable[String] and
      (__ \ "correspondenceAddress").readNullable[Address] and
      (__ \ "additionalInformation").readNullable[String] and
      iomOrCiFlagReads and
      (__ \ "contactNumber").readNullable[ContactNumber] and
      (__ \ "faxNumber").readNullable[String] and
      (__ \ "emailAddr").readNullable[String]
  )(NewCorrespondenceDetails.apply _)
}
