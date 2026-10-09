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

package pages.controllingbody

import base.SpecBase
import models.{Address, ContactNumber, CorrespondenceDetails}
import play.api.libs.json.JsPath

class CheckControllingBodyDetailsPagesSpec extends SpecBase {

  "the controlling body screener pages" - {

    "must be held with the controlling body details" in {
      ControllingBodyAddNinoYesNoPage.path mustEqual JsPath \ "controllingBodyDetails" \ "addNino"
      ControllingBodyAddVrnYesNoPage.path mustEqual JsPath \ "controllingBodyDetails" \ "addVrn"
      ControllingBodyAddAdditionalInfoYesNoPage.path mustEqual JsPath \ "controllingBodyDetails" \ "addAdditionalInfo"
      ControllingBodyAddFaxNumberYesNoPage.path mustEqual JsPath \ "controllingBodyDetails" \ "addFaxNumber"
      ControllingBodyAddEmailAddressYesNoPage.path mustEqual JsPath \ "controllingBodyDetails" \ "addEmailAddress"
    }
  }

  "the controlling body correspondence pages" - {

    "must read the details that the data required action stores in the correspondence section" in {
      val address = Address("Address 1", Some("Address 2"), None, None, Some("AA1 1AA"), Some("Spain"))
      val details = CorrespondenceDetails(
        mgdRegNumber          = mgdRegNum,
        nameLine1             = None,
        nameLine2             = None,
        correspondenceAddress = Some(address),
        additionalInformation = Some("adi"),
        iomOrCiFlag           = Some("0"),
        contactNumber         = Some(ContactNumber(Some("phoneNumber"), Some("mobilePhoneNumber"))),
        faxNumber             = Some("faxNumber"),
        emailAddr             = Some("emailAddr")
      )

      val answers = emptyUserAnswers.set(ControllingBodyCorrespondenceSectionPage, details).success.value

      answers.get(ControllingBodyAddressPage).value mustEqual address
      answers.get(ControllingBodyAdditionalInfoPage).value mustEqual "adi"
      answers.get(ControllingBodyContactNumberPage).value mustEqual ContactNumber(Some("phoneNumber"), Some("mobilePhoneNumber"))
      answers.get(ControllingBodyFaxNumberPage).value mustEqual "faxNumber"
      answers.get(ControllingBodyEmailPage).value mustEqual "emailAddr"
    }

    "must be absent when the correspondence section has no such details" in {
      val details = CorrespondenceDetails(mgdRegNum, None, None, None, None, None, None, None, None)

      val answers = emptyUserAnswers.set(ControllingBodyCorrespondenceSectionPage, details).success.value

      answers.get(ControllingBodyAddressPage) mustBe None
      answers.get(ControllingBodyAdditionalInfoPage) mustBe None
      answers.get(ControllingBodyContactNumberPage) mustBe None
      answers.get(ControllingBodyFaxNumberPage) mustBe None
      answers.get(ControllingBodyEmailPage) mustBe None
    }
  }
}
