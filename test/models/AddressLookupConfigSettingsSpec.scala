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

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.Json

class AddressLookupConfigSettingsSpec extends AnyWordSpec with Matchers {

  "AddressLookupConfiguration" should {
    "serialise and deserialise correctly" in {
      val options = AddressLookupConfigOptions(
        continueUrl            = "/continue",
        homeNavHref            = "/home",
        signOutHref            = "/sign-out",
        accessibilityFooterUrl = "/accessibility",
        deskProServiceName     = "gambling-variations-frontend",
        allowedCountryCodes    = Seq("GB", "JE"),
        selectPageConfig       = SelectPageConfig(10, showSearchLinkAgain = true, showNoneOfTheseOption = true),
        confirmPageConfig = ConfirmPageConfig(
          showChangeLink        = false,
          showSubHeadingAndInfo = false,
          showSearchAgainLink   = false,
          showConfirmChangeText = false
        ),
        manualAddressEntryConfig = ManualAddressEntryConfig(
          line1MaxLength  = 35,
          line2MaxLength  = 35,
          line3MaxLength  = 35,
          townMaxLength   = 35,
          mandatoryFields = Map("line1" -> true),
          maxLengthErrorMessages = MaxLengthErrorMessages(
            en = ManualAddressEntryLineContent("l1", "l2", "l3", "town"),
            cy = ManualAddressEntryLineContent("ll1", "ll2", "ll3", "tref")
          )
        )
      )
      
    }
  }
}
