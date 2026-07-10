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
      }

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
