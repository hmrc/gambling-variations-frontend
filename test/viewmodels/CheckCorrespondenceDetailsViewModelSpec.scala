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

import models.NormalMode
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class CheckCorrespondenceDetailsViewModelSpec extends AnyWordSpec with Matchers {

  private def viewModel(
    correspondenceName: Option[String] = Some("Test Ltd"),
    phoneNumber: Option[String] = Some("0123456789"),
    mobilePhoneNumber: Option[String] = Some("07123456789")
  ): CheckCorrespondenceDetailsViewModel =
    CheckCorrespondenceDetailsViewModel(
      correspondenceName                     = correspondenceName,
      addCorrespondenceAdditionalName        = None,
      additionalCorrespondenceName           = None,
      correspondenceAddress                  = None,
      addCorrespondenceAdditionalInformation = None,
      correspondenceAdditionalInformation    = None,
      phoneNumber                            = phoneNumber,
      mobilePhoneNumber                      = mobilePhoneNumber,
      addCorrespondenceFaxNumber             = None,
      faxNumber                              = None,
      addCorrespondenceEmailAddress          = None,
      emailAddress                           = None,
      hasUkPostcode                          = None,
      isSubmitted                            = false,
      isAddingNewCorrespondenceDetails       = None
    )

  "continueCall" should {

    "navigate to CorrespondenceNameController when correspondence name is missing" in {
      val vm = viewModel(correspondenceName = None)

      vm.continueCall shouldBe
        controllers.routes.CorrespondenceNameController.onPageLoad(NormalMode)
    }

    "navigate to CorrespondenceContactNumberController when both phone numbers are missing" in {
      val vm = viewModel(
        phoneNumber       = None,
        mobilePhoneNumber = None
      )

      vm.continueCall shouldBe
        controllers.routes.CorrespondenceContactNumberController.onPageLoad(NormalMode)
    }

    "navigate to ChangeRegistrationDetailsController when mandatory details are present" in {
      val vm = viewModel()

      vm.continueCall shouldBe
        controllers.routes.ChangeRegistrationDetailsController.onPageLoad()
    }
  }
}
