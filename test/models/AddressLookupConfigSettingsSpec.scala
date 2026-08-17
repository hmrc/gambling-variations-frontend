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

import models.addresslookup.*
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsSuccess, Json}

class AddressLookupConfigSettingsSpec extends AnyWordSpec with Matchers {

  "AddressLookupConfigSettings" should {

    val manualAddressEntryLineContent = ManualAddressEntryLineContent(
      addressLine1 = "Enter address line 1",
      addressLine2 = "Enter address line 2",
      addressLine3 = "Enter address line 3",
      town         = "Enter town or city"
    )

    val manualAddressEntryConfig = ManualAddressEntryConfig(
      line1MaxLength = 35,
      line2MaxLength = 35,
      line3MaxLength = 35,
      townMaxLength  = 35,
      mandatoryFields = Map(
        "addressLine1" -> true,
        "addressLine2" -> true,
        "addressLine3" -> true,
        "town"         -> false,
        "postcode"     -> true
      ),
      maxLengthErrorMessages = MaxLengthErrorMessages(
        en = manualAddressEntryLineContent,
        cy = manualAddressEntryLineContent
      )
    )

    val timeoutConfig = TimeoutConfig(
      timeoutAmount       = 900,
      timeoutUrl          = "/timeout",
      timeoutKeepAliveUrl = "/keep-alive"
    )

    val options = AddressLookupConfigOptions(
      continueUrl            = "/continue",
      homeNavHref            = "/",
      signOutHref            = "/sign-out",
      accessibilityFooterUrl = "/accessibility-statement/gambling-variations",
      deskProServiceName     = "gambling-variations-frontend",
      showBackButtons        = true,
      includeHMRCBranding    = false,
      ukMode                 = true,
      pageHeadingStyle       = "govuk-heading-l",
      selectPageConfig = SelectPageConfig(
        proposalListLimit     = 10,
        showSearchAgainLink   = true,
        showNoneOfTheseOption = true
      ),
      confirmPageConfig = ConfirmPageConfig(
        showConfirmChangeText = true
      ),
      manualAddressEntryConfig = manualAddressEntryConfig,
      timeoutConfig            = timeoutConfig
    )

    val editPageLabels = EditPageLabels(
      title         = "Enter the correspondence address",
      heading       = "Enter the correspondence address",
      line1Label    = "Address line 1",
      line2Label    = "Address line 2",
      line3Label    = "Town or city",
      townLabel     = "County (optional)",
      postcodeLabel = Some("Postcode"),
      countryLabel  = None,
      submitLabel   = Some("Continue")
    )

    val labelContent = AddressLookupLabelContent(
      appLevelLabels = AppLevelLabels(navTitle = "Manage your gambling variation"),
      selectPageLabels = SelectPageLabels(
        title               = "Select the correspondence address",
        heading             = "Select the correspondence address",
        headingWithPostcode = "Select the correspondence address",
        proposalListLabel   = "Select an address",
        submitLabel         = "Continue",
        searchAgainLinkText = "Search again",
        editAddressLinkText = "Enter the address manually"
      ),
      lookupPageLabels = LookupPageLabels(
        title                      = "Enter the correspondence address",
        heading                    = "Enter the correspondence address",
        filterLabel                = "Property name or number (optional)",
        postcodeLabel              = "Postcode",
        submitLabel                = "Find address",
        noResultsFoundMessage      = "No addresses found",
        resultLimitExceededMessage = "Too many addresses found",
        manualAddressLinkText      = "Enter the address manually"
      ),
      confirmPageLabels = ConfirmPageLabels(
        title               = "Confirm the correspondence address",
        heading             = "Review and confirm",
        submitLabel         = "Confirm address",
        searchAgainLinkText = "Search again",
        changeLinkText      = "Change",
        confirmChangeText   = "The information is complete and correct"
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

      (json \ "version").as[Int]                                                  shouldBe 2
      (json \ "options" \ "continueUrl").as[String]                               shouldBe "/continue"
      (json \ "options" \ "ukMode").as[Boolean]                                   shouldBe true
      (json \ "options" \ "pageHeadingStyle").as[String]                          shouldBe "govuk-heading-l"
      (json \ "options" \ "selectPageConfig" \ "showSearchAgainLink").as[Boolean] shouldBe true
      (json \ "labels" \ "en" \ "appLevelLabels" \ "navTitle").as[String]         shouldBe "Manage your gambling variation"
    }

    "serialise the complete lookup page content" in {
      val json = Json.toJson(settings)

      (json \ "labels" \ "en" \ "lookupPageLabels" \ "filterLabel").as[String] shouldBe
        "Property name or number (optional)"
      (json \ "labels" \ "en" \ "lookupPageLabels" \ "manualAddressLinkText").as[String] shouldBe
        "Enter the address manually"
      (json \ "labels" \ "en" \ "confirmPageLabels" \ "submitLabel").as[String] shouldBe "Confirm address"
    }
  }

}
