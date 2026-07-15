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

package services

import connectors.GamblingConnector
import models.BusinessDetails
import models.addresslookup.*
import play.api.i18n.Messages
import play.api.mvc.Request
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.Future

class AddressLookupService @Inject()(
  connector: AddressLookupService
)() {

  def configureAddressLookup(ukMode: Boolean)(implicit hc: HeaderCarrier, messages: Messages): AddressLookupConfigSettings =
    AddressLookupConfigSettings(
      options = AddressLookupConfigOptions(
        continueUrl = ???,
        homeNavHref = ???,
        signOutHref = ???,
        accessibilityFooterUrl = ???,
        deskProServiceName = ???,
        showBackButtons = true,
        includeHMRCBranding = false,
        ukMode = ukMode,
        pageHeadingStyle = "govuk-heading-l",
        selectPageConfig = SelectPageConfig(
          proposalListLimit = 10,
          showSearchLinkAgain = true,
          showNoneOfTheseOption = true
        ),
        confirmPageConfig = ConfirmPageConfig(
          showConfirmChangeText = true
        ),
        manualAddressEntryConfig = ManualAddressEntryConfig(
          line1MaxLength = 35,
          line2MaxLength = 35,
          line3MaxLength = 35,
          townMaxLength = 35,
          mandatoryFields = Map(
            "addressLine1" -> true,
            "addressLine2" -> true,
            "addressLine3" -> true,
            "town" -> false,
            "postcode" -> true
          ),
          maxLengthErrorMessages = MaxLengthErrorMessages(
            en = ManualAddressEntryLineContent().messages,
            cy = ManualAddressEntryLineContent().messages
          ),
          showOrganisationName = false
      ),
        timeoutConfig = TimeoutConfig(
          timeoutAmount = 900,
          timeoutUrl = ???,
          timeoutKeepAliveUrl = ???
        )
    ),
      labels = AddressLookupLabels(
        en = AddressLookupLabelContent(
          appLevelLabels = AppLevelLabels(
            navTitle = messages("service.name")
          ),
          selectPageLabels = SelectPageLabels().messages,
          lookupPageLabels = LookupPageLabels().messages,
          confirmPageLabels = ConfirmPageLabels().messages,
          editPageLabels = ???,
          international = ???
        ),
        cy = AddressLookupLabelContent(
          appLevelLabels = AppLevelLabels(
            navTitle = messages("service.name")
          ),
          selectPageLabels = SelectPageLabels().messages,
          lookupPageLabels = LookupPageLabels().messages,
          confirmPageLabels = ConfirmPageLabels().messages,
          editPageLabels = EditPageLabels(),
          international = ???
        )
      )
} //denton travel, premier travel, liberty coaches, prestoge travel, keith coaches, maynards, 

"""
  |  "labels": {
  |    "en": {
  |      "editPageLabels": {
  |        "title": "META title from Confluence",
  |        "heading": "Enter your XXX address",
  |        "organisationLabel": "",
  |        "line1Label": "Address line 1",
  |        "line2Label": "Address line 2",
  |        "line3Label": "Town or city",
  |        "townLabel": "County (optional)",
  |        "postcodeLabel": "Postcode",
  |        "countryLabel": "",
  |        "submitLabel": "Continue"
  |      },
  |      "international": {
  |        "editPageLabels": {
  |          "organisationLabel": "",
  |          "line1Label": "Address line 1",
  |          "line2Label": "Address line 2",
  |          "line3Label": "Town or city",
  |          "townLabel": "Region or postal code",
  |          "postcodeLabel": "",
  |          "countryLabel": "Country"
  |        }
  |      }
  |    },
  |
  |    "cy": {
  |      "editPageLabels": {
  |        "title": "META title from Confluence",
  |        "heading": "Enter address",
  |        "organisationLabel": "",
  |        "line1Label": "Address line 1",
  |        "line2Label": "Address line 2",
  |        "line3Label": "Town or city",
  |        "townLabel": "County (optional)",
  |        "postcodeLabel": "Postcode",
  |        "countryLabel": "",
  |        "submitLabel": "Continue"
  |      },
  |      "international": {
  |        "editPageLabels": {
  |          "organisationLabel": "",
  |          "line1Label": "Address line 1",
  |          "line2Label": "Address line 2",
  |          "line3Label": "Town or city",
  |          "townLabel": "Region or postal code (optional)",
  |          "postcodeLabel": "",
  |          "countryLabel": "Country"
  |        }
  |      }
  |    }
  |  }
  |""".stripMargin
