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

import play.api.libs.json.{Format, JsError, JsNumber, JsSuccess, Reads, Writes}
import play.api.mvc.JavascriptLiteral

sealed trait RegistrationStatus {
  def code: Int
}

//Note: Potential improvement; add radio options function just like in BusinessType
// and use it for screens that need it, e.g. PartnerDetailsBusinessTypeController
object RegistrationStatus extends Enumerable.Implicits {

  case object Cancelled extends WithName("cancelled") with RegistrationStatus {
    val code = 0
  }

  case object Registered extends WithName("registered") with RegistrationStatus {
    val code = 1
  }

  case object ToBeRegistered extends WithName("toBeRegistered") with RegistrationStatus {
    val code = 2
  }

  val values: Seq[RegistrationStatus] = Seq(
    Cancelled,
    Registered,
    ToBeRegistered
  )

  def fromCode(code: Int): Option[RegistrationStatus] =
    values.find(_.code == code)

  implicit val enumerable: Enumerable[RegistrationStatus] =
    Enumerable(values.map(v => v.toString -> v)*)

  implicit val reads: Reads[RegistrationStatus] =
    Reads { json =>
      json.validate[Int].flatMap { code =>
        fromCode(code)
          .map(JsSuccess(_))
          .getOrElse(JsError("Invalid business type"))
      }
    }

  implicit val writes: Writes[RegistrationStatus] =
    Writes(bt => JsNumber(bt.code))

  implicit val format: Format[RegistrationStatus] =
    Format(reads, writes)

  implicit val jsLiteral: JavascriptLiteral[RegistrationStatus] = (value: RegistrationStatus) => value.toString

}
