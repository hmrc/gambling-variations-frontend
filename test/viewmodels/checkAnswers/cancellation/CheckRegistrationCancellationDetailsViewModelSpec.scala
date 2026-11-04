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

package viewmodels.checkAnswers.cancellation

import base.SpecBase
import controllers.cancellation.routes
import models.cancellation.ReasonForCancellation
import models.{CheckMode, UserAnswers}
import pages.cancellation.{CancelRegistrationDatePage, ChangeGroupRegistrationNumberPage, ReasonForCancellationPage}
import play.api.i18n.{Messages, MessagesApi}
import play.api.test.FakeRequest
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Content
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

import java.time.LocalDate

class CheckRegistrationCancellationDetailsViewModelSpec extends SpecBase {

  private val application =
    applicationBuilder().build()

  private implicit val messages: Messages =
    application.injector
      .instanceOf[MessagesApi]
      .preferred(FakeRequest())

  private val cancellationDate = LocalDate.of(2026, 2, 2)
  private val groupRegistrationNumber = "XWM000000001770"

  private def userAnswersWithGroupReason: UserAnswers =
    emptyUserAnswers
      .set(CancelRegistrationDatePage, cancellationDate)
      .success
      .value
      .set(ReasonForCancellationPage, ReasonForCancellation.BusinessIsPartOfTheGroup)
      .success
      .value
      .set(ChangeGroupRegistrationNumberPage, groupRegistrationNumber)
      .success
      .value

  private def rowFor(
    rows: Seq[SummaryListRow],
    messageKey: String
  ): Option[SummaryListRow] =
    rows.find(_.key.content == Text(messages(messageKey)))

  private def valueOf(row: SummaryListRow): Content =
    row.value.content

  private def actionOf(row: SummaryListRow) =
    row.actions.value.items.head

  "CheckRegistrationCancellationDetailsViewModel" - {

    "summaryList" - {

      "contain the cancellation date, cancellation reason and group registration number rows when the business is part of a group" in {

        val result =
          CheckRegistrationCancellationDetailsViewModel
            .summaryList(userAnswersWithGroupReason)

        result.rows must have size 3

        rowFor(
          result.rows,
          "checkRegistrationCancellationDetails.cancellationDate"
        ) mustBe defined

        rowFor(
          result.rows,
          "checkRegistrationCancellationDetails.cancellationReason"
        ) mustBe defined

        rowFor(
          result.rows,
          "checkRegistrationCancellationDetails.groupRegistrationNumber"
        ) mustBe defined
      }

      "return the rows in the expected order" in {

        val result =
          CheckRegistrationCancellationDetailsViewModel
            .summaryList(userAnswersWithGroupReason)

        result.rows.map(_.key.content) mustBe Seq(
          Text(messages("checkRegistrationCancellationDetails.cancellationDate")),
          Text(messages("checkRegistrationCancellationDetails.cancellationReason")),
          Text(messages("checkRegistrationCancellationDetails.groupRegistrationNumber"))
        )
      }

      "show the formatted cancellation date" in {

        val result =
          CheckRegistrationCancellationDetailsViewModel
            .summaryList(userAnswersWithGroupReason)

        val row =
          rowFor(
            result.rows,
            "checkRegistrationCancellationDetails.cancellationDate"
          ).value

        valueOf(row) mustBe Text("2 February 2026")
      }

      "use the CheckMode cancellation date route for the cancellation date Change link" in {

        val result =
          CheckRegistrationCancellationDetailsViewModel
            .summaryList(userAnswersWithGroupReason)

        val row =
          rowFor(
            result.rows,
            "checkRegistrationCancellationDetails.cancellationDate"
          ).value

        val action = actionOf(row)

        action.href mustBe
          routes.CancelRegistrationDateController
            .onPageLoad(CheckMode)
            .url

        action.content mustBe Text(messages("site.change"))

        action.visuallyHiddenText mustBe Some(
          messages(
            "checkRegistrationCancellationDetails.cancellationDate.change.hidden"
          )
        )
      }

      "show the selected cancellation reason" in {

        val result =
          CheckRegistrationCancellationDetailsViewModel
            .summaryList(userAnswersWithGroupReason)

        val row =
          rowFor(
            result.rows,
            "checkRegistrationCancellationDetails.cancellationReason"
          ).value

        valueOf(row) mustBe Text(
          messages(
            "reasonForCancellation.businessIsPartOfTheGroup"
          )
        )
      }

      "use the CheckMode reason route for the cancellation reason Change link" in {

        val result =
          CheckRegistrationCancellationDetailsViewModel
            .summaryList(userAnswersWithGroupReason)

        val row =
          rowFor(
            result.rows,
            "checkRegistrationCancellationDetails.cancellationReason"
          ).value

        val action = actionOf(row)

        action.href mustBe
          routes.ReasonForCancellationController
            .onPageLoad(CheckMode)
            .url

        action.content mustBe Text(messages("site.change"))

        action.visuallyHiddenText mustBe Some(
          messages(
            "checkRegistrationCancellationDetails.cancellationReason.change.hidden"
          )
        )
      }

      "show the group registration number when the business is part of a group" in {

        val result =
          CheckRegistrationCancellationDetailsViewModel
            .summaryList(userAnswersWithGroupReason)

        val row =
          rowFor(
            result.rows,
            "checkRegistrationCancellationDetails.groupRegistrationNumber"
          ).value

        valueOf(row) mustBe Text(groupRegistrationNumber)
      }

      "use the CheckMode group registration number route for its Change link" in {

        val result =
          CheckRegistrationCancellationDetailsViewModel
            .summaryList(userAnswersWithGroupReason)

        val row =
          rowFor(
            result.rows,
            "checkRegistrationCancellationDetails.groupRegistrationNumber"
          ).value

        val action = actionOf(row)

        action.href mustBe
          routes.ChangeGroupRegistrationNumberController
            .onPageLoad(CheckMode)
            .url

        action.content mustBe Text(messages("site.change"))

        action.visuallyHiddenText mustBe Some(
          messages(
            "checkRegistrationCancellationDetails.groupRegistrationNumber.change.hidden"
          )
        )
      }

      "not show the group registration number row when another cancellation reason is selected" in {

        val userAnswers =
          emptyUserAnswers
            .set(CancelRegistrationDatePage, cancellationDate)
            .success
            .value
            .set(
              ReasonForCancellationPage,
              ReasonForCancellation.NoLongerNeedToBeRegistered
            )
            .success
            .value
            .set(
              ChangeGroupRegistrationNumberPage,
              groupRegistrationNumber
            )
            .success
            .value

        val result =
          CheckRegistrationCancellationDetailsViewModel
            .summaryList(userAnswers)

        rowFor(
          result.rows,
          "checkRegistrationCancellationDetails.groupRegistrationNumber"
        ) mustBe None

        result.rows must have size 2
      }

      "not show the group registration number row when the number is missing" in {

        val userAnswers =
          emptyUserAnswers
            .set(CancelRegistrationDatePage, cancellationDate)
            .success
            .value
            .set(
              ReasonForCancellationPage,
              ReasonForCancellation.BusinessIsPartOfTheGroup
            )
            .success
            .value

        val result =
          CheckRegistrationCancellationDetailsViewModel
            .summaryList(userAnswers)

        rowFor(
          result.rows,
          "checkRegistrationCancellationDetails.groupRegistrationNumber"
        ) mustBe None

        result.rows must have size 2
      }

      "not show the cancellation date row when the cancellation date is missing" in {

        val userAnswers =
          emptyUserAnswers
            .set(
              ReasonForCancellationPage,
              ReasonForCancellation.NoLongerNeedToBeRegistered
            )
            .success
            .value

        val result =
          CheckRegistrationCancellationDetailsViewModel
            .summaryList(userAnswers)

        rowFor(
          result.rows,
          "checkRegistrationCancellationDetails.cancellationDate"
        ) mustBe None

        rowFor(
          result.rows,
          "checkRegistrationCancellationDetails.cancellationReason"
        ) mustBe defined

        result.rows must have size 1
      }

      "not show the cancellation reason row when the reason is missing" in {

        val userAnswers =
          emptyUserAnswers
            .set(CancelRegistrationDatePage, cancellationDate)
            .success
            .value
            .set(
              ChangeGroupRegistrationNumberPage,
              groupRegistrationNumber
            )
            .success
            .value

        val result =
          CheckRegistrationCancellationDetailsViewModel
            .summaryList(userAnswers)

        rowFor(
          result.rows,
          "checkRegistrationCancellationDetails.cancellationReason"
        ) mustBe None

        rowFor(
          result.rows,
          "checkRegistrationCancellationDetails.groupRegistrationNumber"
        ) mustBe None

        result.rows must have size 1
      }

      "return an empty summary list when no cancellation answers are available" in {

        val result =
          CheckRegistrationCancellationDetailsViewModel
            .summaryList(emptyUserAnswers)

        result.rows mustBe empty
      }
    }
  }
}
