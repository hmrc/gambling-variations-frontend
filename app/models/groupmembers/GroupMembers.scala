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

package models.groupmembers

import play.api.libs.json.{Json, OFormat, OWrites, Reads}

import java.time.LocalDate

case class GroupMembers(
  mgdRegNumber: String,
  businessPartnerNumber: Option[String],
  businessName: Option[String],
  crn: Option[String],
  foreignCorporateRef: Option[String],
  dateOfJoining: Option[LocalDate],
  dateOfLeaving: Option[LocalDate],
  isRepMemPartOfGroup: Option[String],
  systemDate: Option[LocalDate]
)

case class GroupMembersResponse(
                                    groupMembers: Seq[GroupMembers]
                                  )

object GroupMembersResponse {
  implicit val reads: Reads[GroupMembersResponse] = Json.reads[GroupMembersResponse]
  implicit val writes: OWrites[GroupMembersResponse] = Json.writes[GroupMembersResponse]
}

object GroupMembers {
  implicit val reads: Reads[GroupMembers] = Json.reads[GroupMembers]
  implicit val writes: OWrites[GroupMembers] = Json.writes[GroupMembers]
}

implicit val format: OFormat[GroupMembers] =
  Json.format[GroupMembers]
