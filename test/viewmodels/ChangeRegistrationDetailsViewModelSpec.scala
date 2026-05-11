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

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.i18n.{Messages, MessagesApi}
import play.api.test.Helpers.stubMessagesApi
import org.scalatest.OptionValues._

class ChangeRegistrationDetailsViewModelSpec extends AnyWordSpec with Matchers {

  implicit val messages: Messages = {
    val messagesApi: MessagesApi = stubMessagesApi(
      Map(
        "en" -> Map(
          "changeRegistrationDetails.businessName"           -> "Business name",
          "changeRegistrationDetails.businessAddress"        -> "Business address",
          "changeRegistrationDetails.businessContactDetails" -> "Business contact details",
          "changeRegistrationDetails.controllingBodyDetails" -> "Controlling body details",
          "changeRegistrationDetails.groupMemberDetails"     -> "Group member details",
          "changeRegistrationDetails.correspondenceDetails"  -> "Correspondence details",
          "changeRegistrationDetails.tradingDetails"         -> "Trading details",
          "changeRegistrationDetails.returnPeriod"           -> "Return period",
          "changeRegistrationDetails.partnerDetails"         -> "Partner details",
          "changeRegistrationDetails.disbandMGDGroup"        -> "Disband MGD group",
          "changeRegistrationDetails.premises"               -> "Premises",
          "changeRegistrationDetails.licences"               -> "Licences"
        )
      )
    )

    messagesApi.preferred(Seq.empty)
  }

  "ChangeRegistrationDetailsViewModel#tasks" should {

    "include non-group-member tasks" in {

      val viewModel = ChangeRegistrationDetailsViewModel(
        mgdRegNumber        = "XM123",
        managementHomeUrl   = "/home",
        isGroupMember       = false,
        isPartnership       = false,
        businessNameChanged = true,
        licencesChanged     = true,
        premisesExists      = true,
        premisesTriggered   = false,
        submitUrl           = "/submit"
      )

      val tasks = viewModel.tasks

      tasks.map(_.name) must contain allOf (
        "Business name",
        "Business address",
        "Business contact details",
        "Correspondence details",
        "Trading details",
        "Return period",
        "Premises",
        "Licences"
      )

      tasks.map(_.name) must not contain (
        "Controlling body details"
        )

      tasks.find(_.name == "Business name").value.status mustBe ReadyToSubmit
      tasks.find(_.name == "Licences").value.status mustBe ReadyToSubmit
    }

    "include partnership task when isPartnership is true" in {

      val viewModel = ChangeRegistrationDetailsViewModel(
        mgdRegNumber        = "XM123",
        managementHomeUrl   = "/home",
        isGroupMember       = false,
        isPartnership       = true,
        businessNameChanged = false,
        licencesChanged     = false,
        premisesExists      = true,
        premisesTriggered   = false,
        submitUrl           = "/submit"
      )

      viewModel.tasks.map(_.name) must contain ("Partner details")
    }

    "include group member tasks when isGroupMember is true" in {

      val viewModel = ChangeRegistrationDetailsViewModel(
        mgdRegNumber        = "XM123",
        managementHomeUrl   = "/home",
        isGroupMember       = true,
        isPartnership       = false,
        businessNameChanged = false,
        licencesChanged     = false,
        premisesExists      = true,
        premisesTriggered   = false,
        submitUrl           = "/submit"
      )

      val taskMessages = viewModel.tasks.map(_.name)

      taskMessages must contain allOf (
        "Controlling body details",
        "Group member details",
        "Disband MGD group"
      )

      taskMessages must not contain (
        "Business name"
        )
    }

    "include triggered premises task with NotStarted when premises do not exist" in {

      val viewModel = ChangeRegistrationDetailsViewModel(
        mgdRegNumber        = "XM123",
        managementHomeUrl   = "/home",
        isGroupMember       = false,
        isPartnership       = false,
        businessNameChanged = false,
        licencesChanged     = false,
        premisesExists      = false,
        premisesTriggered   = true,
        submitUrl           = "/submit"
      )

      val premisesTasks =
        viewModel.tasks.filter(_.name == "Premises")

      premisesTasks.size mustBe 2

      premisesTasks.exists(_.status == NotStarted) mustBe true
    }

    "include triggered premises task with NoChange when premises exist" in {

      val viewModel = ChangeRegistrationDetailsViewModel(
        mgdRegNumber        = "XM123",
        managementHomeUrl   = "/home",
        isGroupMember       = false,
        isPartnership       = false,
        businessNameChanged = false,
        licencesChanged     = false,
        premisesExists      = true,
        premisesTriggered   = true,
        submitUrl           = "/submit"
      )

      val premisesTasks =
        viewModel.tasks.filter(_.name == "Premises")

      premisesTasks.exists(_.status == NoChange) mustBe true
    }
  }

  "ChangeRegistrationDetailsViewModel#canStart" should {

    "return true when at least one task is ReadyToSubmit" in {

      val viewModel = ChangeRegistrationDetailsViewModel(
        mgdRegNumber        = "XM123",
        managementHomeUrl   = "/home",
        isGroupMember       = false,
        isPartnership       = false,
        businessNameChanged = true,
        licencesChanged     = false,
        premisesExists      = true,
        premisesTriggered   = false,
        submitUrl           = "/submit"
      )

      viewModel.canStart mustBe true
    }

    "return false when no tasks are ReadyToSubmit" in {

      val viewModel = ChangeRegistrationDetailsViewModel(
        mgdRegNumber        = "XM123",
        managementHomeUrl   = "/home",
        isGroupMember       = false,
        isPartnership       = false,
        businessNameChanged = false,
        licencesChanged     = false,
        premisesExists      = true,
        premisesTriggered   = false,
        submitUrl           = "/submit"
      )

      viewModel.canStart mustBe false
    }
  }
}
