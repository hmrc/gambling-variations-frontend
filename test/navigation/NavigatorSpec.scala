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
            routes.FaxNumberForCorrespondenceYesNoController.onPageLoad(NormalMode)
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
            routes.AssociatedRegistrationNumbersListController.onPageLoad(NormalMode)
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
            routes.AssociatedRegistrationNumbersListController.onPageLoad(NormalMode)
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
            routes.IndexController.onPageLoad() // update it when available
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
            routes.IndexController.onPageLoad()
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
            routes.AssociatedRegistrationNumbersListController.onPageLoad(NormalMode)
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

        "must go from AssociatedRegNumberPage to the check mode associated registration number page" in {

          navigator.nextPage(AssociatedRegNumberPage, CheckMode, UserAnswers("id")) mustBe
            routes.AssociatedRegistrationNumbersListController.onPageLoad(CheckMode)
        }

        "must go from SoleProprietorPage to the check mode sole proprietor page" in {

          navigator.nextPage(SoleProprietorPage, CheckMode, UserAnswers("id")) mustBe
            routes.ChangeBusinessNameController.onPageLoad(BusinessType.Soleproprietor, CheckMode)
        }

        "must go from AddCorrespondenceFaxNumberPage to CorrespondenceFaxNumber page when answer is true" in {

          val answers = UserAnswers("id")
            .set(AddCorrespondenceFaxNumberPage, true)
            .success
            .value

          navigator.nextPage(AddCorrespondenceFaxNumberPage, CheckMode, answers) mustBe
            routes.CorrespondenceFaxNumberController.onPageLoad(CheckMode)
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
        routes.AssociatedRegistrationNumbersListController.onPageLoad(CheckMode)
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

    "must go from AddCorrespondenceFaxNumberPage to CheckCorrespondenceDetails when answer is false" in {

      val answers =
        UserAnswers("id")
          .set(AddCorrespondenceFaxNumberPage, false)
          .success
          .value

      navigator.nextPage(AddCorrespondenceFaxNumberPage, CheckMode, answers) mustBe
        routes.CheckCorrespondenceDetailsController.onPageLoad()
    }

    "must go from AddCorrespondenceFaxNumberPage to SystemError when unanswered" in {

      navigator.nextPage(AddCorrespondenceFaxNumberPage, CheckMode, UserAnswers("id")) mustBe
        routes.SystemErrorController.onPageLoad()
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

    "must go from AssociatedRegistrationNumbersPage to AssociatedRegistrationNumbersListController in Check mode" in {

      navigator.nextPage(AssociatedRegistrationNumbersPage, CheckMode, UserAnswers("id")) mustBe
        routes.AssociatedRegistrationNumbersListController.onPageLoad(CheckMode)
    }

    "must go from CorrespondenceFaxNumberPage to CorrespondenceFaxNumberController in Check mode" in {

      navigator.nextPage(CorrespondenceFaxNumberPage, CheckMode, UserAnswers("id")) mustBe
        routes.CorrespondenceFaxNumberController.onPageLoad(CheckMode)
    }

    "must go from RemoveCorrespondenceFaxNumberPage to RemoveCorrespondenceFaxNumberController in Check mode" in {

      navigator.nextPage(RemoveCorrespondenceFaxNumberPage, CheckMode, UserAnswers("id")) mustBe
        routes.RemoveCorrespondenceFaxNumberController.onPageLoad(CheckMode)
    }

    "must go from CorrespondenceNamePage to CheckCorrespondenceDetailsController in Check mode" in {

      navigator.nextPage(CorrespondenceNamePage, CheckMode, UserAnswers("id")) mustBe
        routes.CheckCorrespondenceDetailsController.onPageLoad()
    }

    "must go from CorrespondenceAdditionalNamePage to CheckCorrespondenceDetailsController in Check mode" in {

      navigator.nextPage(CorrespondenceAdditionalNamePage, CheckMode, UserAnswers("id")) mustBe
        routes.CheckCorrespondenceDetailsController.onPageLoad()
    }

    "must go from CorrespondenceAdditionalInformationPage to CheckCorrespondenceDetailsController in Check mode" in {

      navigator.nextPage(CorrespondenceAdditionalInformationPage, CheckMode, UserAnswers("id")) mustBe
        routes.CheckCorrespondenceDetailsController.onPageLoad()
    }

    "must go from RemoveCorrAddressAddInfoPage to CheckCorrespondenceDetailsController in Check mode" in {

      navigator.nextPage(RemoveCorrAddressAddInfoPage, CheckMode, UserAnswers("id")) mustBe
        routes.CheckCorrespondenceDetailsController.onPageLoad()
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
