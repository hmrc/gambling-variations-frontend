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

import config.FrontendAppConfig
import connectors.AddressLookupConnector
import controllers.routes
import models.*

import models.addresslookup.*
import play.api.i18n.Messages
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddressLookupService @Inject() (
  connector: AddressLookupConnector,
  appConfig: FrontendAppConfig
) {
)(implicit ec: ExecutionContext) {

  def initJourney()(implicit hc: HeaderCarrier): Future[String] =
    connector.initJourney(configSettings)

  def retrieveAddress(id: String)(implicit hc: HeaderCarrier): Future[Address] =
    connector.retrieveAddress(id)

  def configureAddressLookup(ukMode: Boolean)(implicit hc: HeaderCarrier, messages: Messages): AddressLookupConfigSettings =
    AddressLookupConfigSettings(
      options = AddressLookupConfigOptions(
        continueUrl            = appConfig.loginContinueUrl,
        homeNavHref            = appConfig.addressLookupHomeNavHref,
        signOutHref            = appConfig.signOutUrl,
        accessibilityFooterUrl = appConfig.accessibilityFooterUrl,
        deskProServiceName     = appConfig.addressLookupDeskProServiceName,
        timeoutConfig = TimeoutConfig(
          timeoutAmount       = 900,
          timeoutUrl          = appConfig.addressLookupTimeoutUrl,
          timeoutKeepAliveUrl = appConfig.addressLookupTimeoutKeepAliveUrl
        ),
        showBackButtons     = true,
        includeHMRCBranding = false,
        ukMode              = ukMode,
        pageHeadingStyle    = "govuk-heading-l",
        selectPageConfig = SelectPageConfig(
          proposalListLimit     = 10,
          showSearchLinkAgain   = true,
          showNoneOfTheseOption = true
        ),
        confirmPageConfig = ConfirmPageConfig(
          showConfirmChangeText = true
        ),
        manualAddressEntryConfig = ManualAddressEntryConfig(
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
            en = ManualAddressEntryLineContent().messages,
            cy = ManualAddressEntryLineContent().messages
          ),
          showOrganisationName = false
        )
      ),
      labels = AddressLookupLabels(
        en = AddressLookupLabelContent(
          appLevelLabels = AppLevelLabels(
            navTitle = messages("service.name")
          ),
          selectPageLabels  = SelectPageLabels().messages,
          lookupPageLabels  = LookupPageLabels().messages,
          confirmPageLabels = ConfirmPageLabels().messages,
          editPageLabels    = EditPageLabels().messages,
          international     = International.messages
        ),
        cy = AddressLookupLabelContent(
          appLevelLabels = AppLevelLabels(
            navTitle = messages("service.name")
          ),
          selectPageLabels  = SelectPageLabels().messages,
          lookupPageLabels  = LookupPageLabels().messages,
          confirmPageLabels = ConfirmPageLabels().messages,
          editPageLabels    = EditPageLabels().messages,
          international     = International.messages
        )
      )
    )
      private def configSettings: AddressLookupConfigSettings =
    AddressLookupConfigSettings(
      options = AddressLookupConfigOptions(
        continueUrl                     = appConfig.host + routes.AddressLookupController.callback("").url.stripSuffix("?id="),
        homeNavHref                     = appConfig.gamblingManagementHomeUrl,
        signOutHref                     = appConfig.signOutUrl,
        accessibilityFooterUrl          = "/accessibility-statement/gambling-variations-frontend",
        deskProServiceName              = "gambling-variations-frontend",
        allowedCountryCodes             = Seq("GB"),
        selectPageConfig                = SelectPageConfig(30, showSearchLinkAgain = true, showNoneOfTheseOption = false),
        confirmPageConfig               = ConfirmPageConfig(showChangeLink = true, showSubHeadingAndInfo = false, showSearchAgainLink = true, showConfirmChangeText = false),
        manualAddressEntryConfig        = manualAddressEntryConfig,
        useNewGovUkServiceNavigation    = true
      ),
      labels = AddressLookupLabels(en = labels, cy = labels)
    )

  private val manualAddressEntryConfig: ManualAddressEntryConfig =
    ManualAddressEntryConfig(
      line1MaxLength  = 35,
      line2MaxLength  = 35,
      line3MaxLength  = 35,
      townMaxLength   = 35,
      mandatoryFields = Map("addressLine1" -> true, "addressLine2" -> true, "addressLine3" -> false, "town" -> true, "postcode" -> true),
      maxLengthErrorMessages = MaxLengthErrorMessages(
        en = manualAddressEntryLineContent,
        cy = manualAddressEntryLineContent
      )
    )

  private val manualAddressEntryLineContent: ManualAddressEntryLineContent =
    ManualAddressEntryLineContent(
      addressLine1 = "Address line 1 must be 35 characters or less",
      addressLine2 = "Address line 2 must be 35 characters or less",
      addressLine3 = "Address line 3 must be 35 characters or less",
      town         = "Town or city must be 35 characters or less"
    )

  private val editPageLabels: EditPageLabels =
    EditPageLabels(
      title         = "Enter correspondence address",
      heading       = "Enter correspondence address",
      line1Label    = "Address line 1",
      line2Label    = "Address line 2",
      line3Label    = "Address line 3",
      townLabel     = "Town or city",
      postcodeLabel = Some("Postcode"),
      countryLabel  = Some("Country"),
      submitLabel   = Some("Continue")
    )

  private val labels: AddressLookupLabelContent =
    AddressLookupLabelContent(
      appLevelLabels = AppLevelLabels(navTitle = "Manage your gambling variation"),
      selectPageLabels = SelectPageLabels(
        title               = "Select correspondence address",
        heading             = "Select correspondence address",
        headingWithPostcode = "Select correspondence address for {0}",
        proposalListLabel   = "Select an address",
        submitLabel         = "Continue",
        searchAgainLinkText = "Search again"
      ),
      lookupPageLabels = LookupPageLabels(
        title                      = "Find correspondence address",
        heading                    = "Find correspondence address",
        afterHeadingText           = "We will use this address to contact you about your registration.",
        filterLabel                = "Property name or number",
        postcodeLabel              = "Postcode",
        submitLabel                = "Find address",
        noResultsFoundMessage      = "No addresses found",
        resultLimitExceededMessage = "Too many addresses found"
      ),
      confirmPageLabels = ConfirmPageLabels(
        title               = "Confirm correspondence address",
        heading             = "Confirm correspondence address",
        searchAgainLinkText = "Search again",
        confirmChangeText   = "By confirming this change, you agree that the information you have given is complete and correct."
      ),
      editPageLabels = editPageLabels,
      international  = International(editPageLabels = editPageLabels)
    )
}
