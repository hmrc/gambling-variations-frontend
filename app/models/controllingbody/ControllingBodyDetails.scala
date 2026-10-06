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

package models.controllingbody

import models.{BusinessType, SoleProprietorName}
import play.api.libs.json.{Json, Reads}

// The name fields used by these screens from the I1.36 response.
final case class ControllingBodyDetails(
  mgdRegNumber: String,
  typeOfControllingBody: BusinessType,
  businessName: Option[String] = None,
  solePropTitle: Option[String] = None,
  solePropFirstName: Option[String] = None,
  solePropMiddleName: Option[String] = None,
  solePropLastName: Option[String] = None
) {
  def soleProprietorName: Option[SoleProprietorName] =
    Option.when(Seq(solePropTitle, solePropFirstName, solePropMiddleName, solePropLastName).exists(_.isDefined))(
      SoleProprietorName(solePropTitle.getOrElse(""), solePropFirstName.getOrElse(""), solePropMiddleName, solePropLastName.getOrElse(""))
    )
}

object ControllingBodyDetails {
  implicit val reads: Reads[ControllingBodyDetails] = Json.reads[ControllingBodyDetails]
}
