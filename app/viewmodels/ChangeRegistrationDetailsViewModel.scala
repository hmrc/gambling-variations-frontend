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

package viewmodels

import controllers.routes
import play.api.i18n.Messages

final case class ChangeRegistrationDetailsViewModel(
  mgdRegNumber: String,
  managementHomeUrl: String,
  isGroupMember: Boolean,
  isPartnership: Boolean,
  businessNameChanged: Boolean,
  licencesChanged: Boolean,
  premisesExists: Boolean,
  premisesTriggered: Boolean,
  submitUrl: String
)(implicit messages: Messages) {

  def tasks: Seq[TaskListItem] = {

    def status(flag: Boolean): TaskStatus =
      if (flag) ReadyToSubmit else NoChange

    Seq(
      optional(!isGroupMember)(
        TaskListItem(
          messages("changeRegistrationDetails.businessName"),
          routes.CheckBusinessNameController.onPageLoad().url,
          status(businessNameChanged)
        )
      ),
      optional(!isGroupMember)(
        TaskListItem(
          messages("changeRegistrationDetails.businessAddress"),
          routes.IndexController.onPageLoad().url,
          NoChange
        )
      ),
      optional(!isGroupMember)(
        TaskListItem(
          messages("changeRegistrationDetails.businessContactDetails"),
          routes.IndexController.onPageLoad().url,
          NoChange
        )
      ),
      optional(isGroupMember)(
        TaskListItem(
          messages("changeRegistrationDetails.controllingBodyDetails"),
          "group-member-details",
          NoChange
        )
      ),
      optional(isGroupMember)(
        TaskListItem(
          messages("changeRegistrationDetails.groupMemberDetails"),
          "group-member-details",
          NoChange
        )
      ),
      Some(
        TaskListItem(
          messages("changeRegistrationDetails.correspondenceDetails"),
          routes.IndexController.onPageLoad().url,
          NoChange
        )
      ),
      Some(
        TaskListItem(
          messages("changeRegistrationDetails.tradingDetails"),
          routes.IndexController.onPageLoad().url,
          NoChange
        )
      ),
      Some(
        TaskListItem(
          messages("changeRegistrationDetails.returnPeriod"),
          routes.IndexController.onPageLoad().url,
          NoChange
        )
      ),
      optional(isPartnership)(
        TaskListItem(
          messages("changeRegistrationDetails.partnerDetails"),
          routes.IndexController.onPageLoad().url,
          NoChange
        )
      ),
      optional(isGroupMember)(
        TaskListItem(
          messages("changeRegistrationDetails.disbandMGDGroup"),
          "group-member-details",
          NoChange
        )
      ),
      optional(!isGroupMember)(
        TaskListItem(
          messages("changeRegistrationDetails.premises"),
          routes.IndexController.onPageLoad().url,
          status(licencesChanged)
        )
      ),
      optional(!isGroupMember)(
        TaskListItem(
          messages("changeRegistrationDetails.licences"),
          routes.IndexController.onPageLoad().url,
          status(licencesChanged)
        )
      ),
      optional(!isGroupMember && premisesTriggered)(
        TaskListItem(
          messages("changeRegistrationDetails.premises"),
          "premises",
          if (premisesExists) NoChange else NotStarted
        )
      )
    ).flatten
  }

  def canStart: Boolean =
    tasks.exists(_.status == ReadyToSubmit)

  private def optional(condition: Boolean)(item: => TaskListItem): Option[TaskListItem] =
    if (condition) Some(item) else None
}
