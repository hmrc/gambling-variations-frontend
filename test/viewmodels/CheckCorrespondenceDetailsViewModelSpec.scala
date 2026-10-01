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

import models.{Address, CorrespondenceChangeAddrOption}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class CheckCorrespondenceDetailsViewModelSpec extends AnyWordSpec with Matchers {

  private val address =
    Address(
      address1 = "1 Test Street",
      address2 = None,
      address3 = None,
      address4 = None,
      country  = None,
      postcode = Some("NE1 1AA")
    )

  private def viewModel(
    correspondenceName: Option[String] = Some("Test Ltd"),
    addCorrespondenceAdditionalName: Option[Boolean] = None,
    additionalCorrespondenceName: Option[String] = Some("Additional Name"),
    correspondenceAddress: Option[Address] = Some(address),
    addCorrespondenceAdditionalInformation: Option[Boolean] = None,
    correspondenceAdditionalInformation: Option[String] = Some("Additional information"),
    phoneNumber: Option[String] = Some("0123456789"),
    mobilePhoneNumber: Option[String] = Some("07123456789"),
    addCorrespondenceFaxNumber: Option[Boolean] = None,
    faxNumber: Option[String] = Some("01234567890"),
    addCorrespondenceEmailAddress: Option[Boolean] = None,
    emailAddress: Option[String] = Some("test@test.com"),
    hasUkPostcode: Option[Boolean] = None,
    isSubmitted: Boolean = false,
    isAddingNewCorrespondenceDetails: Option[Boolean] = Some(true),
    changeCorrespondenceAddress: Option[CorrespondenceChangeAddrOption] = None
  ): CheckCorrespondenceDetailsViewModel =
    CheckCorrespondenceDetailsViewModel(
      correspondenceName                     = correspondenceName,
      addCorrespondenceAdditionalName        = addCorrespondenceAdditionalName,
      additionalCorrespondenceName           = additionalCorrespondenceName,
      correspondenceAddress                  = correspondenceAddress,
      addCorrespondenceAdditionalInformation = addCorrespondenceAdditionalInformation,
      correspondenceAdditionalInformation    = correspondenceAdditionalInformation,
      phoneNumber                            = phoneNumber,
      mobilePhoneNumber                      = mobilePhoneNumber,
      addCorrespondenceFaxNumber             = addCorrespondenceFaxNumber,
      faxNumber                              = faxNumber,
      addCorrespondenceEmailAddress          = addCorrespondenceEmailAddress,
      emailAddress                           = emailAddress,
      hasUkPostcode                          = hasUkPostcode,
      isSubmitted                            = isSubmitted,
      isAddingNewCorrespondenceDetails       = isAddingNewCorrespondenceDetails,
      changeCorrespondenceAddress            = changeCorrespondenceAddress
    )

  "continueCall" should {

    "navigate to CorrespondenceNameController when correspondence name is missing" in {
      val vm = viewModel(correspondenceName = None)

      vm.continueCall shouldBe
        controllers.routes.CorrespondenceNameController.onPageLoad()
    }

    "navigate to CorrespondenceUKAddrScreenerController when correspondence address is missing" in {
      val vm = viewModel(correspondenceAddress = None)

      vm.continueCall shouldBe
        controllers.routes.CorrespondenceUKAddrScreenerController.onPageLoad()
    }

    "navigate to CorrespondenceUKAddrScreenerController when address line 1 is empty" in {
      val vm = viewModel(
        correspondenceAddress = Some(
          Address(
            address1 = "",
            address2 = None,
            address3 = None,
            address4 = None,
            country  = None,
            postcode = Some("NE1 1AA")
          )
        )
      )

      vm.continueCall shouldBe
        controllers.routes.CorrespondenceUKAddrScreenerController.onPageLoad()
    }

    "navigate to CorrespondenceContactNumberController when both phone numbers are missing" in {
      val vm = viewModel(
        phoneNumber       = None,
        mobilePhoneNumber = None
      )

      vm.continueCall shouldBe
        controllers.routes.CorrespondenceContactNumberController.onPageLoad()
    }

    "navigate to CheckCorrespondenceDetailsController when mandatory details are present" in {
      val vm = viewModel()

      vm.continueCall shouldBe
        controllers.routes.CheckCorrespondenceDetailsController.onContinue()
    }
  }

  "continueCall in change flow" should {

    "navigate to CorrespondenceNameController when correspondence name is missing" in {
      val vm = viewModel(
        correspondenceName               = None,
        isAddingNewCorrespondenceDetails = Some(false)
      )

      vm.continueCall shouldBe
        controllers.routes.CorrespondenceNameController.onPageLoad()
    }

    "navigate to CorrespondenceUKAddrScreenerController when address is missing" in {
      val vm = viewModel(
        correspondenceAddress            = None,
        isAddingNewCorrespondenceDetails = Some(false)
      )

      vm.continueCall shouldBe
        controllers.routes.CorrespondenceUKAddrScreenerController.onPageLoad()
    }

    "navigate to CorrespondenceUKAddrScreenerController when address line 1 is empty" in {
      val vm = viewModel(
        correspondenceAddress = Some(
          Address(
            address1 = "",
            address2 = None,
            address3 = None,
            address4 = None,
            country  = None,
            postcode = Some("NE1 1AA")
          )
        ),
        isAddingNewCorrespondenceDetails = Some(false)
      )

      vm.continueCall shouldBe
        controllers.routes.CorrespondenceUKAddrScreenerController.onPageLoad()
    }

    "navigate to CorrespondenceContactNumberController when contact numbers are missing" in {
      val vm = viewModel(
        phoneNumber                      = None,
        mobilePhoneNumber                = None,
        isAddingNewCorrespondenceDetails = Some(false)
      )

      vm.continueCall shouldBe
        controllers.routes.CorrespondenceContactNumberController.onPageLoad()
    }

    "navigate to CheckCorrespondenceDetailsController when all required details are present" in {
      val vm = viewModel(
        isAddingNewCorrespondenceDetails = Some(false)
      )

      vm.continueCall shouldBe
        controllers.routes.CheckCorrespondenceDetailsController.onContinue()
    }

    "navigate to CheckCorrespondenceDetailsController when additional name is missing" in {
      val vm = viewModel(
        additionalCorrespondenceName     = None,
        isAddingNewCorrespondenceDetails = Some(false)
      )

      vm.continueCall shouldBe
        controllers.routes.CheckCorrespondenceDetailsController.onContinue()
    }

    "navigate to CheckCorrespondenceDetailsController when fax number is missing" in {
      val vm = viewModel(
        faxNumber                        = None,
        isAddingNewCorrespondenceDetails = Some(false)
      )

      vm.continueCall shouldBe
        controllers.routes.CheckCorrespondenceDetailsController.onContinue()
    }

    "navigate to CheckCorrespondenceDetailsController when email address is missing" in {
      val vm = viewModel(
        emailAddress                     = None,
        isAddingNewCorrespondenceDetails = Some(false)
      )

      vm.continueCall shouldBe
        controllers.routes.CheckCorrespondenceDetailsController.onContinue()
    }
  }

  "change flow" should {

    "not require the additional name yes/no answer" in {
      val vm = viewModel(
        addCorrespondenceAdditionalName  = None,
        additionalCorrespondenceName     = Some("Additional Name"),
        isAddingNewCorrespondenceDetails = Some(false)
      )

      vm.continueCall shouldBe
        controllers.routes.CheckCorrespondenceDetailsController.onContinue()
    }

    "not require the additional address information yes/no answer" in {
      val vm = viewModel(
        addCorrespondenceAdditionalInformation = None,
        correspondenceAdditionalInformation    = Some("Additional information"),
        isAddingNewCorrespondenceDetails       = Some(false)
      )

      vm.continueCall shouldBe
        controllers.routes.CheckCorrespondenceDetailsController.onContinue()
    }

    "not require the fax yes/no answer" in {
      val vm = viewModel(
        addCorrespondenceFaxNumber       = None,
        faxNumber                        = Some("01234567890"),
        isAddingNewCorrespondenceDetails = Some(false)
      )

      vm.continueCall shouldBe
        controllers.routes.CheckCorrespondenceDetailsController.onContinue()
    }

    "not require the email yes/no answer" in {
      val vm = viewModel(
        addCorrespondenceEmailAddress    = None,
        emailAddress                     = Some("test@test.com"),
        isAddingNewCorrespondenceDetails = Some(false)
      )

      vm.continueCall shouldBe
        controllers.routes.CheckCorrespondenceDetailsController.onContinue()
    }
  }

  "submitted status" should {

    "retain submitted status when correspondence details have been submitted" in {
      val vm = viewModel(
        isSubmitted = true
      )

      vm.isSubmitted shouldBe true
    }

    "not be submitted by default" in {
      val vm = viewModel()

      vm.isSubmitted shouldBe false
    }
  }

  "isAddingNewCorrespondenceDetails" should {

    "identify the add flow" in {
      val vm = viewModel(
        isAddingNewCorrespondenceDetails = Some(true)
      )

      vm.isAddingNewCorrespondenceDetails shouldBe Some(true)
    }

    "identify the change flow" in {
      val vm = viewModel(
        isAddingNewCorrespondenceDetails = Some(false)
      )

      vm.isAddingNewCorrespondenceDetails shouldBe Some(false)
    }

    "allow the value to be absent" in {
      val vm = viewModel(
        isAddingNewCorrespondenceDetails = None
      )

      vm.isAddingNewCorrespondenceDetails shouldBe None
    }
  }
}
