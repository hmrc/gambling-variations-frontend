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

import play.api.libs.functional.syntax.*
import play.api.libs.json.*

import java.time.LocalDate

// A member of the group as the backend returns it. The business partner number identifies an existing member.
final case class GroupMember(
  mgdRegNumber: String,
  businessPartnerNumber: String,
  businessName: Option[String],
  crn: Option[String],
  foreignCorporateRef: Option[String],
  isRepMemberOfGroup: Option[String],
  dateOfJoining: Option[LocalDate],
  dateOfLeaving: Option[LocalDate],
  emailAddr: Option[String],
  systemDate: Option[LocalDate]
)

object GroupMember {

  // The backend returns an empty string for a text it does not hold, so an empty string is read as a missing value
  private def optionalText(key: String): Reads[Option[String]] =
    (__ \ key).readNullable[String].map(_.filter(_.nonEmpty))

  private val reads: Reads[GroupMember] = (
    (__ \ "mgdRegNumber").read[String] and
      (__ \ "businessPartnerNumber").read[String].filter(JsonValidationError("error.required"))(_.nonEmpty) and
      optionalText("businessName") and
      optionalText("crn") and
      optionalText("foreignCorporateRef") and
      optionalText("isRepMemberOfGroup") and
      (__ \ "dateOfJoining").readNullable[LocalDate] and
      (__ \ "dateOfLeaving").readNullable[LocalDate] and
      optionalText("emailAddr") and
      (__ \ "systemDate").readNullable[LocalDate]
  )(GroupMember.apply _)

  implicit val format: OFormat[GroupMember] = OFormat(reads, Json.writes[GroupMember])
}

final case class GroupMembers(groupMembers: Seq[GroupMember])

object GroupMembers {

  private val memberKeys: Seq[String] =
    Seq("businessPartnerNumber", "businessName", "crn", "foreignCorporateRef", "isRepMemberOfGroup", "dateOfJoining", "dateOfLeaving", "emailAddr")

  // For a registration with no group members the backend returns only the registration number. An item that holds
  // nothing of a member is therefore not a member, and is left out. Any other item must have a business partner number.
  private def holdsNoMember(item: JsValue): Boolean =
    memberKeys.forall(key => (item \ key).toOption.forall(value => value == JsNull || value == JsString("")))

  private val reads: Reads[GroupMembers] =
    (__ \ "groupMembers")
      .readWithDefault(JsArray.empty)
      .map(items => JsArray(items.value.filterNot(holdsNoMember)))
      .andThen(Reads.seq[GroupMember])
      .map(GroupMembers(_))

  implicit val format: OFormat[GroupMembers] = OFormat(reads, Json.writes[GroupMembers])
}
