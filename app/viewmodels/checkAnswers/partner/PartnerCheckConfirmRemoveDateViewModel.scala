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

package viewmodels.checkAnswers.partner

import controllers.partner.routes
import models.UserAnswers
import pages.partnerdetails.ChosenPartnerToRemovePage
import pages.partnerdetails.PartnerDetailsBusinessNamePage
import pages.partnerdetails.PartnerDetailsDateOfLeavingPage
import pages.partnerdetails.PartnerDetailsPage
import pages.partnerdetails.PartnerDetailsTradingNamePage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.ActionItem
import uk.gov.hmrc.govukfrontend.views.Aliases.Actions
import uk.gov.hmrc.govukfrontend.views.Aliases.Key
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryListRow
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.Aliases.Value

import java.time.LocalDate
import java.time.format.DateTimeFormatter

final case class PartnerCheckConfirmRemoveDateViewModel(
  rows: Seq[SummaryListRow]
)

object PartnerCheckConfirmRemoveDateViewModel {

  private val dateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMMM uuuu")

  def from(
    userAnswers: UserAnswers
  )(implicit messages: Messages): PartnerCheckConfirmRemoveDateViewModel = {

    val partnerIndex =
      userAnswers
        .get(ChosenPartnerToRemovePage)
        .getOrElse(
          throw new RuntimeException(
            "No selected partner for removal"
          )
        )

    val partnerName =
      userAnswers
        .get(PartnerDetailsTradingNamePage(partnerIndex))
        .orElse(
          userAnswers.get(
            PartnerDetailsBusinessNamePage(partnerIndex)
          )
        )
        .orElse(
          userAnswers.get(
            PartnerDetailsPage(partnerIndex)
          )
        )
        .getOrElse(
          messages(
            "partnerCheckConfirmRemoveDate.notProvided"
          )
        )

    val dateToRemove =
      userAnswers.get(
        PartnerDetailsDateOfLeavingPage(partnerIndex)
      )

    PartnerCheckConfirmRemoveDateViewModel(
      rows = Seq(
        partnerNameRow(partnerName),
        dateToRemoveRow(dateToRemove)
      )
    )
  }

  private def partnerNameRow(
    partnerName: String
  )(implicit messages: Messages): SummaryListRow =
    SummaryListRow(
      key = Key(
        content = Text(
          messages(
            "partnerCheckConfirmRemoveDate.labelPartnerName"
          )
        )
      ),
      value = Value(
        content = Text(partnerName)
      )
    )

  private def dateToRemoveRow(
    dateToRemove: Option[LocalDate]
  )(implicit messages: Messages): SummaryListRow = {

    val displayedDate =
      dateToRemove
        .map(_.format(dateFormatter))
        .getOrElse(
          messages(
            "partnerCheckConfirmRemoveDate.notProvided"
          )
        )

    SummaryListRow(
      key = Key(
        content = Text(
          messages(
            "partnerCheckConfirmRemoveDate.labelDateToRemove"
          )
        )
      ),
      value = Value(
        content = Text(displayedDate)
      ),
      actions = Some(
        Actions(
          items = Seq(
            ActionItem(
              href = routes.PartnerDeleteDateController
                .onPageLoad()
                .url,
              content = Text(
                messages("site.change")
              ),
              visuallyHiddenText = Some(
                messages(
                  "partnerCheckConfirmRemoveDate.changeDate.hidden"
                )
              )
            )
          )
        )
      )
    )
  }
}
