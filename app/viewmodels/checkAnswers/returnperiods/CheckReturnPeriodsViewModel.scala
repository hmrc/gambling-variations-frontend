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

package viewmodels.checkAnswers.returnperiods

import models.{CheckMode, ChooseReturnPeriods, UserAnswers}
import pages.returnperiods.{ChooseReturnPeriodsPage, GamblingReturnPeriodsPage, HasExistingNstpValuesPage}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

final case class CheckReturnPeriodsViewModel(
  returnPeriods: Option[ChooseReturnPeriods],
  showAppliesAfterCurrentPeriod: Boolean,
  showReturnPeriodsType: Boolean = false
) {
  private val chooseReturnPeriodsUrl =
    controllers.returnperiods.routes.ChooseReturnPeriodsController.onPageLoad(CheckMode).url

  private def returnPeriodsTypeRow(implicit messages: Messages): Option[SummaryListRow] =
    Option.when(showReturnPeriodsType)(
      SummaryListRowViewModel(
        key   = "checkReturnPeriods.returnPeriods",
        value = ValueViewModel("checkReturnPeriods.returnPeriods.standard"),
        actions = Seq(
          ActionItemViewModel("site.change", controllers.returnperiods.routes.ReturnPeriodsController.onPageLoad(CheckMode).url)
            .withVisuallyHiddenText(messages("checkReturnPeriods.returnPeriods.change.hidden"))
        )
      )
    )

  def summaryList(implicit messages: Messages): SummaryList =
    SummaryListViewModel(returnPeriodsTypeRow.toSeq :+ returnPeriodsEndingRow)

  private def returnPeriodsEndingRow(implicit messages: Messages): SummaryListRow = {
    returnPeriods match {
      case Some(periods) =>
        val months =
          CheckReturnPeriodsViewModel
            .monthKeysFor(periods)
            .map(month => s"<li>${HtmlFormat.escape(messages(month))}</li>")
            .mkString

        SummaryListRowViewModel(
          key   = "checkReturnPeriods.returnPeriodsEnding",
          value = ValueViewModel(HtmlContent(s"""<ul class="govuk-list govuk-list--bullet">$months</ul>""")),
          actions = Seq(
            ActionItemViewModel("site.change", chooseReturnPeriodsUrl)
              .withVisuallyHiddenText(messages("checkReturnPeriods.returnPeriodsEnding.change.hidden"))
          )
        )

      // Return periods have not been set: show an "add" link in place of the months
      case None =>
        val addText = HtmlFormat.escape(messages("checkReturnPeriods.returnPeriodsEnding.add"))
        SummaryListRowViewModel(
          key   = "checkReturnPeriods.returnPeriodsEnding",
          value = ValueViewModel(HtmlContent(s"""<a class="govuk-link" href="$chooseReturnPeriodsUrl">$addText</a>"""))
        )
    }
  }
}

object CheckReturnPeriodsViewModel {

  private def monthKeysFor(returnPeriods: ChooseReturnPeriods): Seq[String] =
    returnPeriods match {
      case ChooseReturnPeriods.Jan => Seq("months.january", "months.april", "months.july", "months.october")
      case ChooseReturnPeriods.Feb => Seq("months.february", "months.may", "months.august", "months.november")
      case ChooseReturnPeriods.Mar => Seq("months.march", "months.june", "months.september", "months.december")
    }

  /** Standard return periods only: None when the periods are not returned or non-standard periods. */
  def from(userAnswers: UserAnswers): Option[CheckReturnPeriodsViewModel] = {
    val existing = userAnswers.get(GamblingReturnPeriodsPage)
    val periodsNotSet = existing.exists(_.returnPeriodsId.isEmpty)
    val isStandard =
      userAnswers
        .get(HasExistingNstpValuesPage)
        .orElse(existing.flatMap(_.hasExistingNstpValues))
        .contains(false) || periodsNotSet && userAnswers.get(ChooseReturnPeriodsPage).isDefined
    val returnPeriods =
      userAnswers
        .get(ChooseReturnPeriodsPage)
        .orElse(existing.flatMap(_.returnPeriodsId))
        .flatMap(ChooseReturnPeriods.fromReturnPeriodsId)

    val showAppliesAfterCurrentPeriod = existing.flatMap(_.finalPeriodWarning).contains(false)
    Option.when(isStandard)(CheckReturnPeriodsViewModel(returnPeriods, showAppliesAfterCurrentPeriod, periodsNotSet))
  }
}
