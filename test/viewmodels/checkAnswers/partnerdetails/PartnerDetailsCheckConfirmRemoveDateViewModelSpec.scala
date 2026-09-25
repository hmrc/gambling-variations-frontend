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

package viewmodels.checkAnswers.partnerdetails

import base.SpecBase
import models.UserAnswers
import pages.partnerdetails.PartnerDetailsChosenPartnerToRemovePage
import pages.partnerdetails.PartnerDetailsBusinessNamePage
import pages.partnerdetails.PartnerDetailsDateOfLeavingPage
import pages.partnerdetails.PartnerDetailsMgdRegNumberPage
import pages.partnerdetails.PartnerDetailsTradingNamePage
import play.api.i18n.Messages
import play.api.i18n.MessagesApi
import play.api.test.FakeRequest
import uk.gov.hmrc.govukfrontend.views.Aliases.Text

import java.time.LocalDate

class PartnerDetailsCheckConfirmRemoveDateViewModelSpec extends SpecBase {

  private val application =
    applicationBuilder().build()

  private implicit val messages: Messages =
    application.injector
      .instanceOf[MessagesApi]
      .preferred(FakeRequest())

  private val partnerIndex = 0.toString

  private val mgdRegNumber: String =
    "XWM00000001762"

  private val tradingName: String =
    "XYZ Trading"

  private val businessName: String =
    "XYZ Business"

  private val dateToRemove: LocalDate =
    LocalDate.of(2026, 9, 2)

  private val baseUserAnswers: UserAnswers =
    emptyUserAnswers
      .set(
        PartnerDetailsMgdRegNumberPage(partnerIndex),
        mgdRegNumber
      )
      .success
      .value
      .set(
        PartnerDetailsChosenPartnerToRemovePage,
        partnerIndex
      )
      .success
      .value

  "PartnerDetailsCheckConfirmRemoveDateViewModel" - {

    "create two summary rows" in {

      val answers =
        baseUserAnswers
          .set(
            PartnerDetailsTradingNamePage(partnerIndex),
            tradingName
          )
          .success
          .value
          .set(
            PartnerDetailsDateOfLeavingPage(partnerIndex),
            dateToRemove
          )
          .success
          .value

      val viewModel =
        PartnerDetailsCheckConfirmRemoveDateViewModel.from(
          answers
        )

      viewModel.rows must have size 2
    }

    "create the partner name summary row" in {

      val answers =
        baseUserAnswers
          .set(
            PartnerDetailsTradingNamePage(partnerIndex),
            tradingName
          )
          .success
          .value
          .set(
            PartnerDetailsDateOfLeavingPage(partnerIndex),
            dateToRemove
          )
          .success
          .value

      val viewModel =
        PartnerDetailsCheckConfirmRemoveDateViewModel.from(
          answers
        )

      val partnerNameRow =
        viewModel.rows.head

      partnerNameRow.key.content mustBe
        Text(
          messages(
            "partnerCheckConfirmRemoveDate.labelPartnerName"
          )
        )

      partnerNameRow.value.content mustBe
        Text(tradingName)

      partnerNameRow.actions mustBe None
    }

    "create the date to remove summary row" in {

      val answers =
        baseUserAnswers
          .set(
            PartnerDetailsTradingNamePage(partnerIndex),
            tradingName
          )
          .success
          .value
          .set(
            PartnerDetailsDateOfLeavingPage(partnerIndex),
            dateToRemove
          )
          .success
          .value

      val viewModel =
        PartnerDetailsCheckConfirmRemoveDateViewModel.from(
          answers
        )

      val dateToRemoveRow =
        viewModel.rows(1)

      dateToRemoveRow.key.content mustBe
        Text(
          messages(
            "partnerCheckConfirmRemoveDate.labelDateToRemove"
          )
        )

      dateToRemoveRow.value.content mustBe
        Text("2 Sep 2026")
    }

    "display the date using a short month name and no leading zero" in {

      val date =
        LocalDate.of(2026, 1, 2)

      val answers =
        baseUserAnswers
          .set(
            PartnerDetailsTradingNamePage(partnerIndex),
            tradingName
          )
          .success
          .value
          .set(
            PartnerDetailsDateOfLeavingPage(partnerIndex),
            date
          )
          .success
          .value

      val viewModel =
        PartnerDetailsCheckConfirmRemoveDateViewModel.from(
          answers
        )

      viewModel.rows(1).value.content mustBe
        Text("2 Jan 2026")
    }

    "display a two-digit day without changing it" in {

      val date =
        LocalDate.of(2026, 9, 19)

      val answers =
        baseUserAnswers
          .set(
            PartnerDetailsTradingNamePage(partnerIndex),
            tradingName
          )
          .success
          .value
          .set(
            PartnerDetailsDateOfLeavingPage(partnerIndex),
            date
          )
          .success
          .value

      val viewModel =
        PartnerDetailsCheckConfirmRemoveDateViewModel.from(
          answers
        )

      viewModel.rows(1).value.content mustBe
        Text("19 Sep 2026")
    }

    "use the trading name when both trading name and business name are present" in {

      val answers =
        baseUserAnswers
          .set(
            PartnerDetailsTradingNamePage(partnerIndex),
            tradingName
          )
          .success
          .value
          .set(
            PartnerDetailsBusinessNamePage(partnerIndex),
            businessName
          )
          .success
          .value
          .set(
            PartnerDetailsDateOfLeavingPage(partnerIndex),
            dateToRemove
          )
          .success
          .value

      val viewModel =
        PartnerDetailsCheckConfirmRemoveDateViewModel.from(
          answers
        )

      viewModel.rows.head.value.content mustBe
        Text(tradingName)
    }

    "use the business name when the trading name is absent" in {

      val answers =
        baseUserAnswers
          .set(
            PartnerDetailsBusinessNamePage(partnerIndex),
            businessName
          )
          .success
          .value
          .set(
            PartnerDetailsDateOfLeavingPage(partnerIndex),
            dateToRemove
          )
          .success
          .value

      val viewModel =
        PartnerDetailsCheckConfirmRemoveDateViewModel.from(
          answers
        )

      viewModel.rows.head.value.content mustBe
        Text(businessName)
    }

    "use the MGD registration number when trading name and business name are absent" in {

      val answers =
        baseUserAnswers
          .set(
            PartnerDetailsDateOfLeavingPage(partnerIndex),
            dateToRemove
          )
          .success
          .value

      val viewModel =
        PartnerDetailsCheckConfirmRemoveDateViewModel.from(
          answers
        )

      viewModel.rows.head.value.content mustBe
        Text(mgdRegNumber)
    }

    "display Not provided when the partner name and MGD registration number are absent" in {

      val answers =
        emptyUserAnswers
          .set(
            PartnerDetailsChosenPartnerToRemovePage,
            partnerIndex
          )
          .success
          .value
          .set(
            PartnerDetailsDateOfLeavingPage(partnerIndex),
            dateToRemove
          )
          .success
          .value

      val viewModel =
        PartnerDetailsCheckConfirmRemoveDateViewModel.from(
          answers
        )

      viewModel.rows.head.value.content mustBe
        Text(
          messages(
            "partnerCheckConfirmRemoveDate.notProvided"
          )
        )
    }

    "display Not provided when the date to remove is absent" in {

      val answers =
        baseUserAnswers
          .set(
            PartnerDetailsTradingNamePage(partnerIndex),
            tradingName
          )
          .success
          .value

      val viewModel =
        PartnerDetailsCheckConfirmRemoveDateViewModel.from(
          answers
        )

      viewModel.rows(1).value.content mustBe
        Text(
          messages(
            "partnerCheckConfirmRemoveDate.notProvided"
          )
        )
    }

    "create a Change action for the date to remove row" in {

      val answers =
        baseUserAnswers
          .set(
            PartnerDetailsTradingNamePage(partnerIndex),
            tradingName
          )
          .success
          .value
          .set(
            PartnerDetailsDateOfLeavingPage(partnerIndex),
            dateToRemove
          )
          .success
          .value

      val viewModel =
        PartnerDetailsCheckConfirmRemoveDateViewModel.from(
          answers
        )

      val actions =
        viewModel.rows(1).actions.get

      actions.items must have size 1

      val changeAction =
        actions.items.head

      changeAction.href mustBe
        controllers.partnerdetails.routes.PartnerDetailsDeleteDateController
          .onPageLoad()
          .url

      changeAction.content mustBe
        Text(
          messages("site.change")
        )

      changeAction.visuallyHiddenText mustBe
        Some(
          messages(
            "partnerCheckConfirmRemoveDate.changeDate.hidden"
          )
        )
    }

    "throw an exception when no partner has been selected for removal" in {

      val answers =
        emptyUserAnswers
          .set(
            PartnerDetailsMgdRegNumberPage(partnerIndex),
            mgdRegNumber
          )
          .success
          .value
          .set(
            PartnerDetailsTradingNamePage(partnerIndex),
            tradingName
          )
          .success
          .value
          .set(
            PartnerDetailsDateOfLeavingPage(partnerIndex),
            dateToRemove
          )
          .success
          .value

      val exception =
        intercept[RuntimeException] {
          PartnerDetailsCheckConfirmRemoveDateViewModel.from(
            answers
          )
        }

      exception.getMessage mustBe
        "No selected partner for removal"
    }

    "use the selected partner index when there is more than one partner" in {

      // multiple partners
      val index = "0"
      val selectedPartnerIndex = "1"

      val selectedPartnerName =
        "Selected Partner Ltd"

      val selectedPartnerDate =
        LocalDate.of(2026, 10, 5)

      val answers =
        emptyUserAnswers
          .set(
            PartnerDetailsMgdRegNumberPage(index),
            "XWM00000000001"
          )
          .success
          .value
          .set(
            PartnerDetailsTradingNamePage(index),
            "First Partner Ltd"
          )
          .success
          .value
          .set(
            PartnerDetailsDateOfLeavingPage(index),
            LocalDate.of(2026, 9, 20)
          )
          .success
          .value
          .set(
            PartnerDetailsMgdRegNumberPage(selectedPartnerIndex),
            "XWM00000000002"
          )
          .success
          .value
          .set(
            PartnerDetailsTradingNamePage(selectedPartnerIndex),
            selectedPartnerName
          )
          .success
          .value
          .set(
            PartnerDetailsDateOfLeavingPage(selectedPartnerIndex),
            selectedPartnerDate
          )
          .success
          .value
          .set(
            PartnerDetailsChosenPartnerToRemovePage,
            selectedPartnerIndex
          )
          .success
          .value

      val viewModel =
        PartnerDetailsCheckConfirmRemoveDateViewModel.from(
          answers
        )

      viewModel.rows.head.value.content mustBe
        Text(selectedPartnerName)

      viewModel.rows(1).value.content mustBe
        Text("5 Oct 2026")
    }
  }
}
