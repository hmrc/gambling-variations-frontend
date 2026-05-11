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
) {

  def tasks: Seq[TaskListItem] = {

    def status(flag: Boolean): TaskStatus =
      if (flag) ReadyToSubmit else NoChange

    Seq(
      optional(!isGroupMember)(
        TaskListItem(
          "Business name",
          routes.CheckBusinessNameController.onPageLoad().url,
          status(businessNameChanged)
        )
      ),
      optional(!isGroupMember)(
        TaskListItem(
          "Business address",
          routes.IndexController.onPageLoad().url,
          NoChange
        )
      ),
      optional(!isGroupMember)(
        TaskListItem(
          "Business contact details",
          routes.IndexController.onPageLoad().url,
          NoChange
        )
      ),
      Some(
        TaskListItem(
          "Correspondence details",
          routes.IndexController.onPageLoad().url,
          NoChange
        )
      ),
      Some(
        TaskListItem(
          "Trading details",
          routes.IndexController.onPageLoad().url,
          NoChange
        )
      ),
      Some(
        TaskListItem(
          "Return period",
          routes.IndexController.onPageLoad().url,
          NoChange
        )
      ),
      optional(isPartnership)(
        TaskListItem(
          "Partner details",
          routes.IndexController.onPageLoad().url,
          NoChange
        )
      ),
      optional(isGroupMember)(
        TaskListItem(
          "Group member details",
          "group-member-details",
          NoChange
        )
      ),
      optional(isGroupMember)(
        TaskListItem(
          "Controlling body details",
          "group-member-details",
          NoChange
        )
      ),
      optional(isGroupMember)(
        TaskListItem(
          "Disband MGD group",
          "group-member-details",
          NoChange
        )
      ),
      optional(!isGroupMember)(
        TaskListItem(
          "Premises",
          routes.IndexController.onPageLoad().url,
          status(licencesChanged)
        )
      ),
      optional(!isGroupMember)(
        TaskListItem(
          "Licences",
          routes.IndexController.onPageLoad().url,
          status(licencesChanged)
        )
      ),
      optional(!isGroupMember && premisesTriggered)(
        TaskListItem(
          "Premises triggered",
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
