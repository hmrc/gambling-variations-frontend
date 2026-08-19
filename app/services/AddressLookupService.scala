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
import models.Address
import models.addresslookup.*
import play.api.i18n.Messages
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddressLookupService @Inject() (
  connector: AddressLookupConnector,
  appConfig: FrontendAppConfig
)(implicit ec: ExecutionContext) {

  def initJourney()(implicit hc: HeaderCarrier, messages: Messages): Future[String] =
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

  private def configSettings(implicit hc: HeaderCarrier, messages: Messages): AddressLookupConfigSettings = {
    val settings = configureAddressLookup(ukMode = true)

    settings.copy(
      options = settings.options.copy(
        continueUrl = appConfig.host + routes.AddressLookupController.callback("").url.stripSuffix("?id=")
      )
    )
  }
}
