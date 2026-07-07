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
  allowedCountryCodes: Seq[String],
  selectPageConfig: SelectPageConfig,
  confirmPageConfig: ConfirmPageConfig,
  manualAddressEntryConfig: ManualAddressEntryConfig
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

case class MaxLengthErrorMessages(en: ManualAddressEntryLineContent, cy: ManualAddressEntryLineContent)

object MaxLengthErrorMessages {
  implicit val fmt: Format[MaxLengthErrorMessages] = Json.format[MaxLengthErrorMessages]
}

case class ManualAddressEntryLineContent(
  addressLine1: String,
  addressLine2: String,
  addressLine3: String,
  town: String
)

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

case class SelectPageLabels(title: String,
                            heading: String,
                            headingWithPostcode: String,
                            proposalListLabel: String,
                            submitLabel: String,
                            searchAgainLinkText: String
                           )

object SelectPageLabels {
  implicit val fmt: Format[SelectPageLabels] = Json.format[SelectPageLabels]
}

case class LookupPageLabels(
  title: String,
  heading: String,
  afterHeadingText: String,
  filterLabel: String,
  postcodeLabel: String,
  submitLabel: String,
  noResultsFoundMessage: String,
  resultLimitExceededMessage: String
)

object LookupPageLabels {
  implicit val fmt: Format[LookupPageLabels] = Json.format[LookupPageLabels]
}

case class ConfirmPageLabels(
  title: String,
  heading: String,
  searchAgainLinkText: String,
  confirmChangeText: String
)

object ConfirmPageLabels {
  implicit val fmt: Format[ConfirmPageLabels] = Json.format[ConfirmPageLabels]
}

case class EditPageLabels(
  title: String,
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

case class ConfirmPageConfig(showChangeLink: Boolean, showSubHeadingAndInfo: Boolean, showSearchAgainLink: Boolean, showConfirmChangeText: Boolean)

object ConfirmPageConfig {
  implicit val fmt: Format[ConfirmPageConfig] = Json.format[ConfirmPageConfig]
}
