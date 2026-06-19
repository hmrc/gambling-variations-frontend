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

        "must go from IsSeasonalBusinessPage to the normal mode seasonal business page" in {

          navigator.nextPage(IsSeasonalBusinessPage, NormalMode, UserAnswers("id")) mustBe
            routes.SeasonalBusinessController.onPageLoad(NormalMode)
        }

        "must go from AssociatedRegNumberPage to the normal mode associated registration number page" in {

          navigator.nextPage(AssociatedRegNumberPage, NormalMode, UserAnswers("id")) mustBe
            routes.AssociatedRegNumberController.onPageLoad(NormalMode)
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

        "must go from IsSeasonalBusinessPage to CheckYourAnswers" in {

          navigator.nextPage(IsSeasonalBusinessPage, CheckMode, UserAnswers("id")) mustBe
            routes.SeasonalBusinessController.onPageLoad(CheckMode)
        }

        "must go from AssociatedRegNumberPage to the check mode associated registration number page" in {

          navigator.nextPage(AssociatedRegNumberPage, CheckMode, UserAnswers("id")) mustBe
            routes.AssociatedRegNumberController.onPageLoad(CheckMode)
        }

        "must go from SoleProprietorPage to the check mode sole proprietor page" in {

          navigator.nextPage(SoleProprietorPage, CheckMode, UserAnswers("id")) mustBe
            routes.ChangeBusinessNameController.onPageLoad(BusinessType.Soleproprietor, CheckMode)
        }
      }

    }
  }
}
