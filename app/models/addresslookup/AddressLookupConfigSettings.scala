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

package models.addresslookup

import models.{AddressLookupConfigOptions, AddressLookupConfigSettings, AddressLookupLabelContent, AddressLookupLabels, AppLevelLabels, ConfirmPageConfig, ConfirmPageLabels, EditPageLabels, International, LookupPageLabels, ManualAddressEntryConfig, ManualAddressEntryLineContent, MaxLengthErrorMessages, SelectPageConfig, SelectPageLabels, TimeoutConfig}
import play.api.i18n.Messages
import play.api.libs.json.{Format, Json}

case class AddressLookupConfigSettings(options: AddressLookupConfigOptions, labels: AddressLookupLabels)

object AddressLookupConfigSettings {
  implicit val fmt: Format[AddressLookupConfigSettings] = Json.format[AddressLookupConfigSettings]
}

case class AddressLookupConfigOptions(
  continueUrl: String,
  homeNavHref: String,
  signOutHref: String,
  useNewGovUkServiceNavigation: Boolean = true,
  accessibilityFooterUrl: String,
  deskProServiceName: String,
  showBackButtons: Boolean,
  includeHMRCBranding: Boolean,
  ukMode: Boolean,
  pageHeadingStyle: String,
  selectPageConfig: SelectPageConfig,
  confirmPageConfig: ConfirmPageConfig,
  manualAddressEntryConfig: ManualAddressEntryConfig,
  timeoutConfig: TimeoutConfig
)

object AddressLookupConfigOptions {
  implicit val fmt: Format[AddressLookupConfigOptions] = Json.format[AddressLookupConfigOptions]
}

case class ManualAddressEntryConfig(
  line1MaxLength: Int,
  line2MaxLength: Int,
  line3MaxLength: Int,
  townMaxLength: Int,
  mandatoryFields: Map[String, Boolean],
  maxLengthErrorMessages: MaxLengthErrorMessages,
  showOrganisationName: Boolean = false
)

object ManualAddressEntryConfig {
  implicit val fmt: Format[ManualAddressEntryConfig] = Json.format[ManualAddressEntryConfig]
}

case class TimeoutConfig(timeoutAmount: Int, timeoutUrl: String, timeoutKeepAliveUrl: String)

object TimeoutConfig {
  implicit val fmt: Format[TimeoutConfig] = Json.format[TimeoutConfig]
}

case class MaxLengthErrorMessages(en: ManualAddressEntryLineContent, cy: ManualAddressEntryLineContent)

object MaxLengthErrorMessages {
  implicit val fmt: Format[MaxLengthErrorMessages] = Json.format[MaxLengthErrorMessages]
}

case class ManualAddressEntryLineContent(
  addressLine1: String = "addressLookup.manualAddressLine1",
  addressLine2: String = "addressLookup.manualAddressLine2",
  addressLine3: String = "addressLookup.manualAddressLine3",
  town: String = "addressLookup.manualAddressLin4",
  postcode: String = "addressLookup.manualAddressPostcode"
) {
  def messages(implicit messages: Messages) = ManualAddressEntryLineContent(
    addressLine1 = messages(addressLine1),
    addressLine2 = messages(addressLine2),
    addressLine3 = messages(addressLine3),
    town         = messages(town),
    postcode     = messages(postcode)
  )
}

object ManualAddressEntryLineContent {
  implicit val fmt: Format[ManualAddressEntryLineContent] = Json.format[ManualAddressEntryLineContent]
}

case class AddressLookupLabels(en: AddressLookupLabelContent, cy: AddressLookupLabelContent)

object AddressLookupLabels {
  implicit val fmt: Format[AddressLookupLabels] = Json.format[AddressLookupLabels]
}

case class AddressLookupLabelContent(
  appLevelLabels: AppLevelLabels,
  selectPageLabels: SelectPageLabels,
  lookupPageLabels: LookupPageLabels,
  confirmPageLabels: ConfirmPageLabels,
  editPageLabels: EditPageLabels,
  international: International
)

object AddressLookupLabelContent {
  implicit val fmt: Format[AddressLookupLabelContent] = Json.format[AddressLookupLabelContent]
}

case class AppLevelLabels(navTitle: String)

object AppLevelLabels {
  implicit val fmt: Format[AppLevelLabels] = Json.format[AppLevelLabels]
}

case class SelectPageLabels(title: String = "correspondenceAddressSelectAddress.title",
                            heading: String = "correspondenceAddressSelectAddress.heading"
                           ) {
  def messages(implicit messages: Messages) = SelectPageLabels(title = messages(title), heading = messages(heading))
}

object SelectPageLabels {
  implicit val fmt: Format[SelectPageLabels] = Json.format[SelectPageLabels]
}

case class LookupPageLabels(
  title: String = "correspondenceAddressLookupAddress.title",
  heading: String = "correspondenceAddressLookupAddress.heading",
  postcodeLabel: String = "correspondenceAddressLookupAddress.postcode",
  submitLabel: String = "correspondenceAddressLookupAddress.submit"
) {
  def messages(implicit messages: Messages) = LookupPageLabels(
    title         = messages(title),
    heading       = messages(heading),
    postcodeLabel = messages(postcodeLabel),
    submitLabel   = messages(submitLabel)
  )
}

object LookupPageLabels {
  implicit val fmt: Format[LookupPageLabels] = Json.format[LookupPageLabels]
}

case class ConfirmPageLabels(
  title: String = "correspondenceAddressConfirmAddress.title",
  heading: String = "correspondenceAddressConfirmAddress.heading",
  changeLinkText: String = "correspondenceAddressConfirmAddress.change"
) {
  def messages(implicit messages: Messages) = ConfirmPageLabels(
    title          = messages(title),
    heading        = messages(heading),
    changeLinkText = messages(changeLinkText)
  )
}

object ConfirmPageLabels {
  implicit val fmt: Format[ConfirmPageLabels] = Json.format[ConfirmPageLabels]
}

case class EditPageLabels(
  title: String = ???,
  heading: String,
  line1Label: String,
  line2Label: String,
  line3Label: String,
  townLabel: String,
  postcodeLabel: Option[String],
  countryLabel: Option[String],
  submitLabel: Option[String]
)

object EditPageLabels {
  implicit val fmt: Format[EditPageLabels] = Json.format[EditPageLabels]
}

case class International(editPageLabels: EditPageLabels)

object International {
  implicit val fmt: Format[International] = Json.format[International]
}

case class SelectPageConfig(proposalListLimit: Int, showSearchLinkAgain: Boolean, showNoneOfTheseOption: Boolean)

object SelectPageConfig {
  implicit val fmt: Format[SelectPageConfig] = Json.format[SelectPageConfig]
}

case class ConfirmPageConfig(showConfirmChangeText: Boolean)

object ConfirmPageConfig {
  implicit val fmt: Format[ConfirmPageConfig] = Json.format[ConfirmPageConfig]
}
