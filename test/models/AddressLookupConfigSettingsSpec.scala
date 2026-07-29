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

import models.addresslookup.{
  AddressLookupConfigOptions,
  AddressLookupConfigSettings,
  AddressLookupLabelContent,
  AddressLookupLabels,
  AppLevelLabels,
  ConfirmPageConfig,
  ConfirmPageLabels,
  EditPageLabels,
  International,
  LookupPageLabels,
  ManualAddressEntryConfig,
  ManualAddressEntryLineContent,
  MaxLengthErrorMessages,
  SelectPageConfig,
  SelectPageLabels,
  TimeoutConfig
}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsSuccess, Json}

class AddressLookupConfigSettingsSpec extends AnyWordSpec with Matchers {

  "AddressLookupConfigSettings" should {

    val manualAddressEntryLineContent = ManualAddressEntryLineContent(
      addressLine1 = "Enter address line 1",
      addressLine2 = "Enter address line 2",
      addressLine3 = "Enter address line 3",
      town         = "Enter town or city",
      postcode     = "Enter postcode"
    )

    val manualAddressEntryConfig = ManualAddressEntryConfig(
      line1MaxLength  = 35,
      line2MaxLength  = 35,
      line3MaxLength  = 35,
      townMaxLength   = 35,
      mandatoryFields = Map("line1" -> true, "line2" -> false),
      maxLengthErrorMessages = MaxLengthErrorMessages(
        en = manualAddressEntryLineContent,
        cy = manualAddressEntryLineContent
      )
    )

    val timeoutConfig = TimeoutConfig(
      timeoutAmount       = 900,
      timeoutUrl          = "http://localhost:9000/timeout",
      timeoutKeepAliveUrl = "http://localhost:9000/keep-alive"
    )

    val options = AddressLookupConfigOptions(
      continueUrl              = "http://localhost:9000/continue",
      homeNavHref               = "http://localhost:9000/home",
      signOutHref               = "http://localhost:9000/sign-out",
      accessibilityFooterUrl    = "http://localhost:9000/accessibility",
      deskProServiceName        = "gambling-variations-frontend",
      showBackButtons           = true,
      includeHMRCBranding       = false,
      ukMode                    = true,
      pageHeadingStyle          = "govuk-heading-l",
      selectPageConfig = SelectPageConfig(
        proposalListLimit     = 30,
        showSearchLinkAgain   = true,
        showNoneOfTheseOption = false
      ),
      confirmPageConfig = ConfirmPageConfig(
        showConfirmChangeText = false
      ),
      manualAddressEntryConfig = manualAddressEntryConfig,
      timeoutConfig            = timeoutConfig
    )

    val editPageLabels = EditPageLabels(
      title         = "Enter address",
      heading       = "Enter address",
      line1Label    = "Address line 1",
      line2Label    = "Address line 2",
      line3Label    = "Address line 3",
      townLabel     = "Town or city",
      postcodeLabel = Some("Postcode"),
      countryLabel  = Some("Country"),
      submitLabel   = Some("Continue")
    )

    val labelContent = AddressLookupLabelContent(
      appLevelLabels = AppLevelLabels(navTitle = "Manage your gambling variation"),
      selectPageLabels = SelectPageLabels(
        title   = "Select address",
        heading = "Select address"
      ),
      lookupPageLabels = LookupPageLabels(
        title         = "Find address",
        heading       = "Find address",
        postcodeLabel = "Postcode",
        submitLabel   = "Find address"
      ),
      confirmPageLabels = ConfirmPageLabels(
        title          = "Confirm address",
        heading        = "Confirm address",
        changeLinkText = "Change"
      ),
      editPageLabels = editPageLabels,
      international  = International(editPageLabels = editPageLabels)
    )

    val labels = AddressLookupLabels(en = labelContent, cy = labelContent)

    val settings = AddressLookupConfigSettings(options = options, labels = labels)

    "round-trip through JSON (write then read produces an equal value)" in {
      val json = Json.toJson(settings)
      json.validate[AddressLookupConfigSettings] shouldBe JsSuccess(settings)
    }

    "serialise nested options and labels under their own keys" in {
      val json = Json.toJson(settings)

      (json \ "options" \ "continueUrl").as[String]                       shouldBe "http://localhost:9000/continue"
      (json \ "options" \ "ukMode").as[Boolean]                           shouldBe true
      (json \ "options" \ "pageHeadingStyle").as[String]                  shouldBe "govuk-heading-l"
      (json \ "labels" \ "en" \ "appLevelLabels" \ "navTitle").as[String] shouldBe "Manage your gambling variation"
    }
  }

}
