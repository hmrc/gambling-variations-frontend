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

case class CorrespondenceDetails(
  mgdRegNumber: String,
  nameLine1: Option[String],
  nameLine2: Option[String],
  correspondenceAddress: Option[Address],
  additionalInformation: Option[String],
  iomOrCiFlag: Option[Boolean],
  contactNumber: Option[ContactNumber],
  faxNumber: Option[String],
  emailAddr: Option[String]
)

object CorrespondenceDetails {

  private val booleanYNFormat: Format[Boolean] = Format(
    Reads {
      case JsString(value) =>
        value.trim.toLowerCase match {
          case "y"     => JsSuccess(true)
          case "n"     => JsSuccess(false)
          case "1"     => JsSuccess(true)
          case "0"     => JsSuccess(false)
          case "true"  => JsSuccess(true)
          case "false" => JsSuccess(false)
          case _ =>
            JsError("Expected Y/N, 1/0 or true/false")
        }

      case JsNull => JsError("Expected a boolean value, got null")
      case _      => JsError("Expected a string representing a boolean")
    },
    Writes {
      case true  => JsString("Y")
      case false => JsString("N")
    }
  )

  implicit val writes: OWrites[CorrespondenceDetails] =
    OWrites { details =>
      val json = Json.writes[CorrespondenceDetails].writes(details)

      json ++ Json.obj(
        "iomOrCiFlag" -> details.iomOrCiFlag.map(booleanYNFormat.writes)
      )
    }

  implicit val reads: Reads[CorrespondenceDetails] = (
    (__ \ "mgdRegNumber").read[String] and
      (__ \ "nameLine1").readNullable[String] and
      (__ \ "nameLine2").readNullable[String].map(_.filter(_.nonEmpty)) and
      Address.reads.map(Some(_): Option[Address]).orElse(Reads.pure(None)) and
      (__ \ "adi").readNullable[String].map(_.filter(_.nonEmpty)) and
      (__ \ "iomOrCiFlag").readNullable[Boolean](booleanYNFormat).orElse(Reads.pure(None)) and
      ContactNumber.reads
        .map(contactNumber => if (contactNumber.phoneNumber.isEmpty && contactNumber.mobilePhoneNumber.isEmpty) None else Some(contactNumber))
        .orElse(Reads.pure(None)) and
      (__ \ "faxNumber").readNullable[String].map(_.filter(_.nonEmpty)) and
      (__ \ "emailAddr").readNullable[String].map(_.filter(_.nonEmpty))
  )(CorrespondenceDetails.apply _)

}
