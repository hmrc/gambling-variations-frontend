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

package navigation

import base.SpecBase
import controllers.routes
import models.*
import pages.*

class NavigatorSpec extends SpecBase {

  val navigator = new Navigator

  "Navigator" - {

    "in Normal mode" - {

      "must go to" - {

        "CheckBusinessNameController from RemoveTradeNamePage" in {

          case object UnknownPage extends Page
          navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id")) mustBe routes.IndexController.onPageLoad()
        }

        "must go from TradingNamePage to CheckBusinessName" in {

          navigator.nextPage(TradingNamePage, NormalMode, UserAnswers("id")) mustBe
            routes.CheckBusinessNameController.onPageLoad()
        }

        "must go from CorrespondenceEmailPage to CheckCorrespondenceDetails page" in {

          navigator.nextPage(CorrespondenceEmailPage, NormalMode, UserAnswers("id")) mustBe
            routes.CheckCorrespondenceDetailsController.onPageLoad()
        }

        "must go from RemoveCorrespondenceEmailAddressPage to CheckCorrespondenceDetails page" in {

          navigator.nextPage(RemoveCorrespondenceEmailAddressPage, NormalMode, UserAnswers("id")) mustBe
            routes.CheckCorrespondenceDetailsController.onPageLoad()
        }

        "must go from CorrespondenceContactNumberPage to FaxNumberForCorrespondenceYesNo page" in {

          navigator.nextPage(CorrespondenceContactNumberPage, NormalMode, UserAnswers("id")) mustBe
            routes.CheckCorrespondenceDetailsController.onPageLoad()
        }

        "must go from AddCorrespondenceFaxNumberPage to CorrespondenceFaxNumber page when answer is true" in {

          val answers = UserAnswers("id")
            .set(AddCorrespondenceFaxNumberPage, true)
            .success
            .value

          navigator.nextPage(AddCorrespondenceFaxNumberPage, NormalMode, answers) mustBe
            routes.CorrespondenceFaxNumberController.onPageLoad(NormalMode)
        }

        "must go from AddAssociatedRegistrationNumberPage to AssociatedRegNumber page when yes is selected" in {

          val answers =
            UserAnswers("id")
              .set(AddAssociatedRegistrationNumberPage, true)
              .success
              .value

          navigator.nextPage(AddAssociatedRegistrationNumberPage, NormalMode, answers) mustBe
            routes.AssociatedRegNumberController.onPageLoad(NormalMode)
        }

        "must go from BusinessNamePage to CheckBusinessNameController" in {

          navigator.nextPage(BusinessNamePage, NormalMode, UserAnswers("id")) mustBe
            routes.CheckBusinessNameController.onPageLoad()
        }

        "must go from RemoveEmailAddressPage to CheckContactDetailsController" in {

          navigator.nextPage(RemoveEmailAddressPage, NormalMode, UserAnswers("id")) mustBe
            routes.CheckContactDetailsController.onPageLoad()
        }

        "must go from BusinessTradeClassPage to CheckTradingDetailsController" in {

          navigator.nextPage(BusinessTradeClassPage, NormalMode, UserAnswers("id")) mustBe
            routes.CheckTradingDetailsController.onPageLoad()
        }

        "must go from OtherTradeClassPage to CheckTradingDetailsController" in {

          navigator.nextPage(OtherTradeClassPage, NormalMode, UserAnswers("id")) mustBe
            routes.CheckTradingDetailsController.onPageLoad()
        }

        "must go from RemovePreviousRegNumberPage to PreviousRegistrationNumbersListController" in {

          navigator.nextPage(RemovePreviousRegNumberPage, NormalMode, UserAnswers("id")) mustBe
            routes.PreviousRegistrationNumbersListController.onPageLoad(NormalMode)
        }

        "must go from AssociatedRegistrationNumbersPage to AssociatedRegistrationNumbersListController" in {

          navigator.nextPage(AssociatedRegistrationNumbersPage, NormalMode, UserAnswers("id")) mustBe
            routes.AssociatedRegistrationNumbersListController.onPageLoad()
        }

        "must go from RemoveCorrespondenceFaxNumberPage to CheckCorrespondenceDetailsController" in {

          navigator.nextPage(RemoveCorrespondenceFaxNumberPage, NormalMode, UserAnswers("id")) mustBe
            routes.CheckCorrespondenceDetailsController.onPageLoad()
        }

        "must go from CorrespondenceNamePage to CheckCorrespondenceDetailsController" in {

          navigator.nextPage(CorrespondenceNamePage, NormalMode, UserAnswers("id")) mustBe
            routes.CheckCorrespondenceDetailsController.onPageLoad()
        }

        "must go from CorrespondenceAdditionalNamePage to CheckCorrespondenceDetailsController" in {

          navigator.nextPage(CorrespondenceAdditionalNamePage, NormalMode, UserAnswers("id")) mustBe
            routes.CheckCorrespondenceDetailsController.onPageLoad()
        }

        "must go from CorrespondenceAdditionalInformationPage to CheckCorrespondenceDetailsController" in {

          navigator.nextPage(CorrespondenceAdditionalInformationPage, NormalMode, UserAnswers("id")) mustBe
            routes.CheckCorrespondenceDetailsController.onPageLoad()
        }

        "must go from AddAssociatedRegistrationNumberPage to CheckTradingDetails when no is selected" in {

          val answers =
            UserAnswers("id")
              .set(AddAssociatedRegistrationNumberPage, false)
              .success
              .value

          navigator.nextPage(AddAssociatedRegistrationNumberPage, NormalMode, answers) mustBe
            routes.CheckTradingDetailsController.onPageLoad()
        }

        "must go from AddAssociatedRegistrationNumberPage to SystemError when unanswered" in {

          navigator.nextPage(AddAssociatedRegistrationNumberPage, NormalMode, UserAnswers("id")) mustBe
            routes.SystemErrorController.onPageLoad()
        }

        "must go to AssociatedRegistrationNumbersList when associated registration numbers exist" in {

          val answers =
            UserAnswers("id")
              .set(AssociatedRegistrationNumbersPage, Seq("ABC123"))
              .success
              .value

          navigator.nextPage(RemoveAssociatedRegNumberPage, NormalMode, answers) mustBe
            routes.AssociatedRegistrationNumbersListController.onPageLoad()
        }

        "must go to CheckTradingDetails when associated registration numbers are empty" in {

          val answers =
            UserAnswers("id")
              .set(AssociatedRegistrationNumbersPage, Seq.empty[String])
              .success
              .value

          navigator.nextPage(RemoveAssociatedRegNumberPage, NormalMode, answers) mustBe
            routes.CheckTradingDetailsController.onPageLoad()
        }

        "must go to CheckTradingDetails when associated registration numbers are missing" in {

          navigator.nextPage(RemoveAssociatedRegNumberPage, NormalMode, UserAnswers("id")) mustBe
            routes.CheckTradingDetailsController.onPageLoad()
        }

        "must go to CorrespondenceName when AddCorrespondingDetailsYesNoPage is true" in {

          val answers =
            UserAnswers("id")
              .set(AddCorrespondingDetailsYesNoPage, true)
              .success
              .value

          navigator.nextPage(AddCorrespondingDetailsYesNoPage, NormalMode, answers) mustBe
            routes.CorrespondenceNameController.onPageLoad(NormalMode)
        }

        "must go to ChangeRegistrationDetails when AddCorrespondingDetailsYesNoPage is false" in {

          val answers =
            UserAnswers("id")
              .set(AddCorrespondingDetailsYesNoPage, false)
              .success
              .value

          navigator.nextPage(AddCorrespondingDetailsYesNoPage, NormalMode, answers) mustBe
            routes.ChangeRegistrationDetailsController.onPageLoad()
        }

        "must go to SystemError when AddCorrespondingDetailsYesNoPage is unanswered" in {

          navigator.nextPage(AddCorrespondingDetailsYesNoPage, NormalMode, UserAnswers("id")) mustBe
            routes.SystemErrorController.onPageLoad()
        }

        "must go to SystemError when RemoveCorrespondenceDetailsYesNoPage is unanswered" in {

          navigator.nextPage(RemoveCorrespondenceDetailsYesNoPage, NormalMode, UserAnswers("id")) mustBe
            routes.SystemErrorController.onPageLoad()
        }

        "must go to CheckCorrespondenceDetails when CorrespondenceFaxNumberPage is navigated" in {

          navigator.nextPage(
            CorrespondenceFaxNumberPage,
            NormalMode,
            UserAnswers("id")
          ) mustBe
            routes.CheckCorrespondenceDetailsController.onPageLoad()
        }

        "must go from AddCorrespondenceFaxNumberPage to CheckCorrespondenceDetails page when answer is false" in {

          val answers = UserAnswers("id")
            .set(AddCorrespondenceFaxNumberPage, false)
            .success
            .value

          navigator.nextPage(AddCorrespondenceFaxNumberPage, NormalMode, answers) mustBe
            routes.CheckCorrespondenceDetailsController.onPageLoad()
        }

        "must go from AddCorrespondenceFaxNumberPage to SystemError page when answer is missing" in {

          navigator.nextPage(AddCorrespondenceFaxNumberPage, NormalMode, UserAnswers("id")) mustBe
            routes.SystemErrorController.onPageLoad()
        }

        "must go from CorrespondenceAdditionalNameYesNoPage to CorrespondenceAdditionalName page when answer is true" in {

          val answers = UserAnswers("id")
            .set(CorrespondenceAdditionalNameYesNoPage, true)
            .success
            .value

          navigator.nextPage(CorrespondenceAdditionalNameYesNoPage, NormalMode, answers) mustBe
            routes.CorrespondenceAdditionalNameController.onPageLoad(NormalMode)
        }

        "must go from RemoveCorrespondenceDetailsYesNoPage to ChangeRegistrationDetails page when answer is true" in {

          val answers = UserAnswers("id")
            .set(RemoveCorrespondenceDetailsYesNoPage, true)
            .success
            .value

          navigator.nextPage(RemoveCorrespondenceDetailsYesNoPage, NormalMode, answers) mustBe
            routes.ChangeRegistrationDetailsController.onPageLoad()
        }

        "must go from RemoveCorrespondenceDetailsYesNoPage to CheckCorrespondenceDetails page when answer is true" in {

          val answers = UserAnswers("id")
            .set(RemoveCorrespondenceDetailsYesNoPage, false)
            .success
            .value

          navigator.nextPage(RemoveCorrespondenceDetailsYesNoPage, NormalMode, answers) mustBe
            routes.CheckCorrespondenceDetailsController.onPageLoad()
        }

        "must go from CorrespondenceAdditionalNameYesNoPage to CheckCorrespondenceDetails page when answer is false" in {

          val answers = UserAnswers("id")
            .set(CorrespondenceAdditionalNameYesNoPage, false)
            .success
            .value

          navigator.nextPage(CorrespondenceAdditionalNameYesNoPage, NormalMode, answers) mustBe
            routes.CheckCorrespondenceDetailsController.onPageLoad()
        }

        "must go from CorrespondenceAdditionalNameYesNoPage to SystemError page when answer is missing" in {

          navigator.nextPage(CorrespondenceAdditionalNameYesNoPage, NormalMode, UserAnswers("id")) mustBe
            routes.SystemErrorController.onPageLoad()
        }

        "must go from SoleProprietorPage to the normal mode sole proprietor page" in {

          navigator.nextPage(SoleProprietorPage, NormalMode, UserAnswers("id")) mustBe
            routes.ChangeBusinessNameController.onPageLoad(BusinessType.Soleproprietor, NormalMode)
        }

        "must go from BusinessContactNumberPage to next page" in {

          navigator.nextPage(BusinessContactNumberPage, NormalMode, UserAnswers("id")) mustBe
            routes.CheckContactDetailsController.onPageLoad()
        }

        "must go from EmailAddressPage to next page" in {

          navigator.nextPage(BusinessEmailAddressPage, NormalMode, UserAnswers("id")) mustBe
            routes.CheckContactDetailsController.onPageLoad()
        }

        "must go from FaxNumberPage to CheckYourAnswers" in {

          navigator.nextPage(BusinessFaxNumberPage, NormalMode, UserAnswers("id")) mustBe
            routes.CheckContactDetailsController.onPageLoad()
        }

        "must go from RemoveFaxNumberPage to CheckYourAnswers" in {

          navigator.nextPage(RemoveFaxNumberPage, NormalMode, UserAnswers("id")) mustBe
            routes.CheckContactDetailsController.onPageLoad()
        }

        "must go from IsSeasonalBusinessPage to the normal mode check trading details page" in {
          navigator.nextPage(IsSeasonalBusinessPage, NormalMode, UserAnswers("id")) mustBe
            routes.CheckTradingDetailsController.onPageLoad()
        }

        "must go from AssociatedRegNumberPage to the normal mode associated registration number page" in {

          navigator.nextPage(AssociatedRegNumberPage, NormalMode, UserAnswers("id")) mustBe
            routes.AssociatedRegistrationNumbersListController.onPageLoad()
        }

        "a page that doesn't exist in the route map to Index" in {
          navigator.nextPage(RemoveTradeNamePage, NormalMode, UserAnswers("id")) mustBe routes.CheckBusinessNameController.onPageLoad()
        }
      }

    }

    "in Check mode" - {

      "must go to" - {
        "CheckBusinessNameController from RemoveTradeNamePage" in {

          navigator.nextPage(RemoveTradeNamePage, CheckMode, UserAnswers("id")) mustBe routes.CheckBusinessNameController.onPageLoad()
        }

        "a page that doesn't exist in the edit route map to CheckYourAnswers" in {

          case object UnknownPage extends Page
          navigator.nextPage(UnknownPage, CheckMode, UserAnswers("id")) mustBe routes.CheckYourAnswersController.onPageLoad()
        }

        "must go from IsSeasonalBusinessPage to the check mode check trading details page" in {
          navigator.nextPage(IsSeasonalBusinessPage, CheckMode, UserAnswers("id")) mustBe
            routes.CheckTradingDetailsController.onPageLoad()
        }

        "must go from SoleProprietorPage to the check mode sole proprietor page" in {

          navigator.nextPage(SoleProprietorPage, CheckMode, UserAnswers("id")) mustBe
            routes.ChangeBusinessNameController.onPageLoad(BusinessType.Soleproprietor, CheckMode)
        }

        "must go from CorrespondenceNamePage to CorrespondenceAdditionalNameYesNo when AddCorrespondingDetailsYesNoPage is true" in {

          val answers =
            UserAnswers("id")
              .set(AddCorrespondingDetailsYesNoPage, true)
              .success
              .value

          navigator.nextPage(CorrespondenceNamePage, NormalMode, answers) mustBe
            routes.CorrespondenceAdditionalNameYesNoController.onPageLoad(NormalMode)
        }

        "must go from CorrespondenceAdditionalNamePage to CorrespondenceUKAddrScreener when AddCorrespondingDetailsYesNoPage is true" in {

          val answers =
            UserAnswers("id")
              .set(AddCorrespondingDetailsYesNoPage, true)
              .success
              .value

          navigator.nextPage(CorrespondenceAdditionalNamePage, NormalMode, answers) mustBe
            routes.CorrespondenceUKAddrScreenerController.onPageLoad(NormalMode)
        }

        "must go from CorrespondenceAddressUkPage to CorrespondenceAddrInfoScreener when AddCorrespondingDetailsYesNoPage is true" in {

          val answers =
            UserAnswers("id")
              .set(AddCorrespondingDetailsYesNoPage, true)
              .success
              .value

          navigator.nextPage(CorrespondenceAddressUkPage, NormalMode, answers) mustBe
            routes.CorrespondenceAddrInfoScreenerController.onPageLoad(NormalMode)
        }

        "must go from CorrespondenceAddressNonUkPage to CorrespondenceAddrInfoScreener when AddCorrespondingDetailsYesNoPage is true" in {

          val answers =
            UserAnswers("id")
              .set(AddCorrespondingDetailsYesNoPage, true)
              .success
              .value

          navigator.nextPage(CorrespondenceAddressNonUkPage, NormalMode, answers) mustBe
            routes.CorrespondenceAddrInfoScreenerController.onPageLoad(NormalMode)
        }

        "must go from CorrespondenceAdditionalInformationPage to CorrespondenceContactNumber when AddCorrespondingDetailsYesNoPage is true" in {

          val answers =
            UserAnswers("id")
              .set(AddCorrespondingDetailsYesNoPage, true)
              .success
              .value

          navigator.nextPage(CorrespondenceAdditionalInformationPage, NormalMode, answers) mustBe
            routes.CorrespondenceContactNumberController.onPageLoad(NormalMode)
        }

        "must go from CorrespondenceContactNumberPage to FaxNumberForCorrespondenceYesNo when AddCorrespondingDetailsYesNoPage is true" in {

          val answers =
            UserAnswers("id")
              .set(AddCorrespondingDetailsYesNoPage, true)
              .success
              .value

          navigator.nextPage(CorrespondenceContactNumberPage, NormalMode, answers) mustBe
            routes.FaxNumberForCorrespondenceYesNoController.onPageLoad(NormalMode)
        }

        "must go from CorrespondenceFaxNumberPage to AddEmailAddressForCorrespondenceYesNo when AddCorrespondingDetailsYesNoPage is true" in {

          val answers =
            UserAnswers("id")
              .set(AddCorrespondingDetailsYesNoPage, true)
              .success
              .value

          navigator.nextPage(CorrespondenceFaxNumberPage, NormalMode, answers) mustBe
            routes.AddEmailAddressForCorrespondenceYesNoController.onPageLoad(NormalMode)
        }

        "must go from CorrespondenceChangeAddrScreenerPage to CorrespondenceNonUKAddress when changing from a UK address" in {

          val answers =
            UserAnswers("id")
              .set(
                CorrespondenceAddressUkPage,
                Address(
                  address1 = "line1",
                  address2 = Some("line2"),
                  address3 = None,
                  address4 = None,
                  postcode = Some("AA1 1AA"),
                  country  = Some("GB")
                )
              )
              .success
              .value
              .set(CorrespondenceChangeAddrScreenerPage, true)
              .success
              .value

          navigator.nextPage(CorrespondenceChangeAddrScreenerPage, NormalMode, answers) mustBe
            routes.CorrespondenceNonUKAddressController.onPageLoad(NormalMode)
        }

        "must go from CorrespondenceChangeAddrScreenerPage to CorrespondenceUKAddress when keeping a UK address" in {

          val answers =
            UserAnswers("id")
              .set(
                CorrespondenceAddressUkPage,
                Address(
                  address1 = "line1",
                  address2 = Some("line2"),
                  address3 = None,
                  address4 = None,
                  postcode = Some("AA1 1AA"),
                  country  = Some("GB")
                )
              )
              .success
              .value
              .set(CorrespondenceChangeAddrScreenerPage, false)
              .success
              .value

          navigator.nextPage(CorrespondenceChangeAddrScreenerPage, NormalMode, answers) mustBe
            routes.CorrespondenceUKAddressController.onPageLoad(NormalMode)
        }

        "must go from CorrespondenceChangeAddrScreenerPage to CorrespondenceUKAddress when changing from a non-UK address" in {

          val answers =
            UserAnswers("id")
              .set(CorrespondenceChangeAddrScreenerPage, true)
              .success
              .value

          navigator.nextPage(CorrespondenceChangeAddrScreenerPage, NormalMode, answers) mustBe
            routes.CorrespondenceUKAddressController.onPageLoad(NormalMode)
        }

        "must go from CorrespondenceChangeAddrScreenerPage to CorrespondenceNonUKAddress when keeping a non-UK address" in {

          val answers =
            UserAnswers("id")
              .set(CorrespondenceChangeAddrScreenerPage, false)
              .success
              .value

          navigator.nextPage(CorrespondenceChangeAddrScreenerPage, NormalMode, answers) mustBe
            routes.CorrespondenceNonUKAddressController.onPageLoad(NormalMode)
        }

        "must go to SystemError when CorrespondenceChangeAddrScreenerPage is unanswered" in {

          navigator.nextPage(CorrespondenceChangeAddrScreenerPage, NormalMode, UserAnswers("id")) mustBe
            routes.SystemErrorController.onPageLoad()
        }

        "must go to CorrespondenceAdditionalInfo when additional address information is required" in {

          val answers =
            UserAnswers("id")
              .set(AddCorrespondenceAddressAdditionalInformationPage, true)
              .success
              .value

          navigator.nextPage(AddCorrespondenceAddressAdditionalInformationPage, NormalMode, answers) mustBe
            routes.CorrespondenceAdditionalInfoController.onPageLoad(NormalMode)
        }

        "must go to CorrespondenceContactNumber when no additional address information is required" in {

          val answers =
            UserAnswers("id")
              .set(AddCorrespondenceAddressAdditionalInformationPage, false)
              .success
              .value
              .set(AddCorrespondingDetailsYesNoPage, true)
              .success
              .value

          navigator.nextPage(AddCorrespondenceAddressAdditionalInformationPage, NormalMode, answers) mustBe
            routes.CorrespondenceContactNumberController.onPageLoad(NormalMode)
        }

        "must go to CheckCorrespondenceDetails when no additional address information is required outside correspondence journey" in {

          val answers =
            UserAnswers("id")
              .set(AddCorrespondenceAddressAdditionalInformationPage, false)
              .success
              .value
              .set(AddCorrespondingDetailsYesNoPage, false)
              .success
              .value

          navigator.nextPage(AddCorrespondenceAddressAdditionalInformationPage, NormalMode, answers) mustBe
            routes.CheckCorrespondenceDetailsController.onPageLoad()
        }

        "must go to SystemError when AddCorrespondenceAddressAdditionalInformationPage is unanswered" in {

          navigator.nextPage(AddCorrespondenceAddressAdditionalInformationPage, NormalMode, UserAnswers("id")) mustBe
            routes.SystemErrorController.onPageLoad()
        }

        "must go to CorrespondenceUKAddress when UK address is selected" in {

          val answers =
            UserAnswers("id")
              .set(CorrespondenceUKAddrScreenerPage, true)
              .success
              .value

          navigator.nextPage(CorrespondenceUKAddrScreenerPage, NormalMode, answers) mustBe
            routes.CorrespondenceUKAddressController.onPageLoad(NormalMode)
        }

        "must go to CorrespondenceNonUKAddress when non-UK address is selected" in {

          val answers =
            UserAnswers("id")
              .set(CorrespondenceUKAddrScreenerPage, false)
              .success
              .value

          navigator.nextPage(CorrespondenceUKAddrScreenerPage, NormalMode, answers) mustBe
            routes.CorrespondenceNonUKAddressController.onPageLoad(NormalMode)
        }

        "must go to SystemError when CorrespondenceUKAddrScreenerPage is unanswered" in {

          navigator.nextPage(CorrespondenceUKAddrScreenerPage, NormalMode, UserAnswers("id")) mustBe
            routes.SystemErrorController.onPageLoad()
        }

        "must go from CorrespondenceAddressUkPage to CheckCorrespondenceDetails when AddCorrespondingDetailsYesNoPage is false" in {

          val answers =
            UserAnswers("id")
              .set(AddCorrespondingDetailsYesNoPage, false)
              .success
              .value

          navigator.nextPage(CorrespondenceAddressUkPage, NormalMode, answers) mustBe
            routes.CheckCorrespondenceDetailsController.onPageLoad()
        }

        "must go from CorrespondenceAddressNonUkPage to CheckCorrespondenceDetails when AddCorrespondingDetailsYesNoPage is false" in {

          val answers =
            UserAnswers("id")
              .set(AddCorrespondingDetailsYesNoPage, false)
              .success
              .value

          navigator.nextPage(CorrespondenceAddressNonUkPage, NormalMode, answers) mustBe
            routes.CheckCorrespondenceDetailsController.onPageLoad()
        }

        "must go to CorrespondenceEmailAddress when AddEmailAddressForCorrespondenceYesNoPage is true" in {

          val answers =
            UserAnswers("id")
              .set(AddEmailAddressForCorrespondenceYesNoPage, true)
              .success
              .value

          navigator.nextPage(AddEmailAddressForCorrespondenceYesNoPage, NormalMode, answers) mustBe
            routes.CorrespondenceEmailAddressController.onPageLoad(NormalMode)
        }

        "must go to CheckCorrespondenceDetails when AddEmailAddressForCorrespondenceYesNoPage is false and AddCorrespondingDetailsYesNoPage is true" in {

          val answers =
            UserAnswers("id")
              .set(AddEmailAddressForCorrespondenceYesNoPage, false)
              .success
              .value
              .set(AddCorrespondingDetailsYesNoPage, true)
              .success
              .value

          navigator.nextPage(AddEmailAddressForCorrespondenceYesNoPage, NormalMode, answers) mustBe
            routes.CheckCorrespondenceDetailsController.onPageLoad()
        }

        "must go to CheckCorrespondenceDetails when AddEmailAddressForCorrespondenceYesNoPage is false and AddCorrespondingDetailsYesNoPage is false" in {

          val answers =
            UserAnswers("id")
              .set(AddEmailAddressForCorrespondenceYesNoPage, false)
              .success
              .value
              .set(AddCorrespondingDetailsYesNoPage, false)
              .success
              .value

          navigator.nextPage(AddEmailAddressForCorrespondenceYesNoPage, NormalMode, answers) mustBe
            routes.CheckCorrespondenceDetailsController.onPageLoad()
        }

        "must go to SystemError when AddEmailAddressForCorrespondenceYesNoPage is unanswered" in {

          navigator.nextPage(AddEmailAddressForCorrespondenceYesNoPage, NormalMode, UserAnswers("id")) mustBe
            routes.SystemErrorController.onPageLoad()
        }

        "must go from CorrespondenceAdditionalNameYesNoPage to CorrespondenceUKAddrScreener when answer is false and correspondence details are being added" in {

          val answers =
            UserAnswers("id")
              .set(CorrespondenceAdditionalNameYesNoPage, false)
              .success
              .value
              .set(AddCorrespondingDetailsYesNoPage, true)
              .success
              .value

          navigator.nextPage(CorrespondenceAdditionalNameYesNoPage, NormalMode, answers) mustBe
            routes.CorrespondenceUKAddrScreenerController.onPageLoad(NormalMode)
        }

      }

    }

    "must go from AddAssociatedRegistrationNumberPage to AssociatedRegNumber page when yes is selected" in {

      val answers =
        UserAnswers("id")
          .set(AddAssociatedRegistrationNumberPage, true)
          .success
          .value

      navigator.nextPage(AddAssociatedRegistrationNumberPage, CheckMode, answers) mustBe
        routes.AssociatedRegNumberController.onPageLoad(CheckMode)
    }

    "must go from AddAssociatedRegistrationNumberPage to CheckTradingDetails when no is selected" in {

      val answers =
        UserAnswers("id")
          .set(AddAssociatedRegistrationNumberPage, false)
          .success
          .value

      navigator.nextPage(AddAssociatedRegistrationNumberPage, CheckMode, answers) mustBe
        routes.CheckTradingDetailsController.onPageLoad()
    }

    "must go from AddAssociatedRegistrationNumberPage to SystemError when unanswered" in {

      navigator.nextPage(AddAssociatedRegistrationNumberPage, CheckMode, UserAnswers("id")) mustBe
        routes.SystemErrorController.onPageLoad()
    }

    "must go to AssociatedRegistrationNumbersList when associated registration numbers exist" in {

      val answers =
        UserAnswers("id")
          .set(AssociatedRegistrationNumbersPage, Seq("ABC123"))
          .success
          .value

      navigator.nextPage(RemoveAssociatedRegNumberPage, CheckMode, answers) mustBe
        routes.AssociatedRegistrationNumbersListController.onPageLoad()
    }

    "must go to CheckTradingDetails when associated registration numbers are empty" in {

      val answers =
        UserAnswers("id")
          .set(AssociatedRegistrationNumbersPage, Seq.empty[String])
          .success
          .value

      navigator.nextPage(RemoveAssociatedRegNumberPage, CheckMode, answers) mustBe
        routes.CheckTradingDetailsController.onPageLoad()
    }

    "must go to CheckTradingDetails when previous registration numbers exist but list is empty" in {

      val answers =
        UserAnswers("id")
          .set(PreviousRegistrationNumbersListPage, Seq.empty[String])
          .success
          .value

      navigator.nextPage(RemovePreviousRegNumberPage, CheckMode, answers) mustBe
        routes.CheckTradingDetailsController.onPageLoad()
    }

    "must go to RemovePreviousRegNumber when previous registration numbers exist" in {

      val answers =
        UserAnswers("id")
          .set(PreviousRegistrationNumbersListPage, Seq("OLD1"))
          .success
          .value

      navigator.nextPage(RemovePreviousRegNumberPage, CheckMode, answers) mustBe
        routes.RemovePreviousRegNumberController.onPageLoad(CheckMode)
    }

    "must go to CheckTradingDetails when previous registration numbers are missing" in {

      navigator.nextPage(RemovePreviousRegNumberPage, CheckMode, UserAnswers("id")) mustBe
        routes.CheckTradingDetailsController.onPageLoad()
    }

    "must go from PreviousRegNumberPage to the normal mode previous registration number page" in {

      navigator.nextPage(PreviousRegNumberPage, NormalMode, UserAnswers("id")) mustBe
        routes.PreviousRegistrationNumberController.onPageLoad(NormalMode)
    }

    "must go from AddPreviousRegistrationNumberPage to previous registration number page when yes is selected in Normal mode" in {

      val answers =
        UserAnswers("id")
          .set(AddPreviousRegistrationNumberPage, true)
          .success
          .value

      navigator.nextPage(AddPreviousRegistrationNumberPage, NormalMode, answers) mustBe
        routes.PreviousRegistrationNumberController.onPageLoad(NormalMode)
    }

    "must go from AddPreviousRegistrationNumberPage to check trading details when no is selected in Normal mode" in {

      val answers =
        UserAnswers("id")
          .set(AddPreviousRegistrationNumberPage, false)
          .success
          .value

      navigator.nextPage(AddPreviousRegistrationNumberPage, NormalMode, answers) mustBe
        routes.CheckTradingDetailsController.onPageLoad()
    }

    "must go from AddPreviousRegistrationNumberPage to system error when no answer exists in Normal mode" in {

      navigator.nextPage(AddPreviousRegistrationNumberPage, NormalMode, UserAnswers("id")) mustBe
        routes.SystemErrorController.onPageLoad()
    }

    "must go from PreviousRegNumberPage to the check mode previous registration number page" in {

      navigator.nextPage(PreviousRegNumberPage, CheckMode, UserAnswers("id")) mustBe
        routes.PreviousRegistrationNumberController.onPageLoad(CheckMode)
    }

    "must go from BusinessNamePage to CheckBusinessNameController" in {

      navigator.nextPage(BusinessNamePage, CheckMode, UserAnswers("id")) mustBe
        routes.CheckBusinessNameController.onPageLoad()
    }

    "must go from BusinessEmailAddressPage to BusinessEmailAddressController in Check mode" in {

      navigator.nextPage(BusinessEmailAddressPage, CheckMode, UserAnswers("id")) mustBe
        routes.BusinessEmailAddressController.onPageLoad(CheckMode)
    }

    "must go from OtherTradeClassPage to CheckTradingDetailsController in Check mode" in {

      navigator.nextPage(OtherTradeClassPage, CheckMode, UserAnswers("id")) mustBe
        routes.CheckTradingDetailsController.onPageLoad()
    }

    "must go from BusinessTradeClassPage to CheckTradingDetailsController in Check mode" in {

      navigator.nextPage(BusinessTradeClassPage, CheckMode, UserAnswers("id")) mustBe
        routes.CheckTradingDetailsController.onPageLoad()
    }

    "must go from PreviousRegistrationNumbersListPage to PreviousRegistrationNumbersListController in Check mode" in {

      navigator.nextPage(PreviousRegistrationNumbersListPage, CheckMode, UserAnswers("id")) mustBe
        routes.PreviousRegistrationNumbersListController.onPageLoad(CheckMode)
    }

    "must go from AddPreviousRegistrationNumberPage to previous registration number page when yes is selected in Check mode" in {

      val answers =
        UserAnswers("id")
          .set(AddPreviousRegistrationNumberPage, true)
          .success
          .value

      navigator.nextPage(AddPreviousRegistrationNumberPage, CheckMode, answers) mustBe
        routes.PreviousRegistrationNumberController.onPageLoad(CheckMode)
    }

    "must go from AddPreviousRegistrationNumberPage to check trading details when no is selected in Check mode" in {

      val answers =
        UserAnswers("id")
          .set(AddPreviousRegistrationNumberPage, false)
          .success
          .value

      navigator.nextPage(AddPreviousRegistrationNumberPage, CheckMode, answers) mustBe
        routes.CheckTradingDetailsController.onPageLoad()
    }
  }
}
