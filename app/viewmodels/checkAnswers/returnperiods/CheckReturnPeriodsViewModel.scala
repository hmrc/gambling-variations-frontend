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

import controllers.returnperiods.routes
import models.{CheckMode, ChooseReturnPeriods, NoReturnPeriodsSet, NormalMode, UserAnswers}
import pages.returnperiods.{ChooseReturnPeriodsPage, GamblingReturnPeriodsPage}
import play.api.i18n.Messages
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{Content, HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*
import utils.ReturnPeriodsUtils
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

sealed trait CheckReturnPeriodsViewModel {
  def summaryList(implicit messages: Messages): SummaryList
}

object CheckReturnPeriodsViewModel {

  final case class Standard(
    returnPeriods: Option[ChooseReturnPeriods],
    showAppliesAfterCurrentPeriod: Boolean,
    showReturnPeriodsType: Boolean = false
  ) extends CheckReturnPeriodsViewModel {

    private val chooseReturnPeriodsUrl = routes.ChooseReturnPeriodsController.onPageLoad(CheckMode).url

    def summaryList(implicit messages: Messages): SummaryList =
      SummaryListViewModel(returnPeriodsTypeRow.toSeq :+ returnPeriodsEndingRow)

    private def returnPeriodsTypeRow(implicit messages: Messages): Option[SummaryListRow] =
      Option.when(showReturnPeriodsType)(
        SummaryListRowViewModel(
          key   = "checkReturnPeriods.returnPeriods",
          value = ValueViewModel("checkReturnPeriods.returnPeriods.standard"),
          actions = Seq(
            ActionItemViewModel("site.change", routes.ReturnPeriodsController.onPageLoad(CheckMode).url)
              .withVisuallyHiddenText(messages("checkReturnPeriods.returnPeriods.change.hidden"))
          )
        )
      )

    private def returnPeriodsEndingRow(implicit messages: Messages): SummaryListRow =
      returnPeriods match {
        case Some(periods) =>
          val months =
            standardMonthKeys(periods).map(month => s"<li>${HtmlFormat.escape(messages(month))}</li>").mkString

          SummaryListRowViewModel(
            key   = "checkReturnPeriods.returnPeriodsEnding",
            value = ValueViewModel(HtmlContent(s"""<ul class="govuk-list govuk-list--bullet">$months</ul>""")),
            actions = Seq(
              ActionItemViewModel("site.change", chooseReturnPeriodsUrl)
                .withVisuallyHiddenText(messages("checkReturnPeriods.returnPeriodsEnding.change.hidden"))
            )
          )

        case None =>
          val addText = HtmlFormat.escape(messages("checkReturnPeriods.returnPeriodsEnding.add"))
          SummaryListRowViewModel(
            key   = "checkReturnPeriods.returnPeriodsEnding",
            value = ValueViewModel(HtmlContent(s"""<a class="govuk-link" href="$chooseReturnPeriodsUrl">$addText</a>"""))
          )
      }
  }

  final case class NonStandard(
    returnPeriodsChoice: Option[NoReturnPeriodsSet],
    selectedMonths: Seq[String],
    rows: Seq[SummaryListRow],
    showFinalPeriodWarning: Boolean
  ) extends CheckReturnPeriodsViewModel {

    def summaryList(implicit messages: Messages): SummaryList =
      SummaryList(rows = returnPeriodsChoiceRow.toSeq ++ Seq(returnPeriodsEndingRow) ++ rows)

    private def returnPeriodsChoiceRow(implicit messages: Messages): Option[SummaryListRow] =
      returnPeriodsChoice.map { choice =>
        val choiceText = choice match {
          case NoReturnPeriodsSet.Standard    => messages("checkNonStandardReturnPeriods.standard")
          case NoReturnPeriodsSet.Nonstandard => messages("checkNonStandardReturnPeriods.nonStandard")
        }

        SummaryListRow(
          key   = Key(content = Text(messages("checkNonStandardReturnPeriods.returnPeriods"))),
          value = Value(content = Text(choiceText)),
          actions = Some(
            Actions(items =
              Seq(
                ActionItem(
                  href               = routes.ReturnPeriodsController.onPageLoad(CheckMode).url,
                  content            = Text(messages("site.change")),
                  visuallyHiddenText = Some(messages("checkNonStandardReturnPeriods.returnPeriods.hidden"))
                )
              )
            )
          )
        )
      }

    private def returnPeriodsEndingRow(implicit messages: Messages): SummaryListRow =
      SummaryListRow(
        key = Key(content = Text(messages("checkNonStandardReturnPeriods.returnPeriodsEnding"))),
        value = Value(content =
          HtmlContent(
            Html(
              "<ul class=\"govuk-list govuk-list--bullet\">" +
                selectedMonths.map(month => s"<li>${HtmlFormat.escape(month).body}</li>").mkString +
                "</ul>"
            )
          )
        ),
        actions = Some(
          Actions(items =
            Seq(
              ActionItem(
                href               = routes.ChooseReturnPeriodsController.onPageLoad(CheckMode).url,
                content            = Text(messages("site.change")),
                visuallyHiddenText = Some(messages("checkNonStandardReturnPeriods.returnPeriodsEnding.hidden"))
              )
            )
          )
        )
      )
  }

  def from(userAnswers: UserAnswers): Option[Standard] = {
    val existing = userAnswers.get(GamblingReturnPeriodsPage)
    val periodsNotSet = existing.exists(_.returnPeriodsId.isEmpty)
    val returnPeriods =
      userAnswers
        .get(ChooseReturnPeriodsPage)
        .orElse(existing.flatMap(_.returnPeriodsId))
        .flatMap(ChooseReturnPeriods.fromReturnPeriodsId)

    val showAppliesAfterCurrentPeriod = existing.flatMap(_.finalPeriodWarning).contains(false)
    Option.when(ReturnPeriodsUtils.isStandard(userAnswers))(Standard(returnPeriods, showAppliesAfterCurrentPeriod, periodsNotSet))
  }

  def nonStandard(
    returnPeriodsChoice: Option[NoReturnPeriodsSet],
    returnPeriodsId: Int,
    calculatedPeriodEnds: Seq[LocalDate],
    enteredDates: Seq[Option[LocalDate]],
    showFinalPeriodWarning: Boolean
  )(implicit messages: Messages): NonStandard = {

    val selectedMonths = nonStandardMonthKeys(returnPeriodsId).map(messages(_))
    val locale = Locale.forLanguageTag(messages.lang.code)

    val rows =
      calculatedPeriodEnds.zipWithIndex.map { case (periodEnd, index) =>
        val periodNumber = index + 1
        val monthYear = formatMonth(periodEnd, locale)
        val periodLabel = messages("checkNonStandardReturnPeriods.periodLabel", monthYear)

        enteredDates.lift(index).flatten match {
          case Some(enteredDate) =>
            createRow(
              label = periodLabel,
              value = Text(formatDate(enteredDate, locale)),
              action = Some(
                ActionItem(
                  href               = routes.EnterNonStandardPeriodDateController.onPageLoad(periodNumber, CheckMode).url,
                  content            = Text(messages("site.change")),
                  visuallyHiddenText = Some(lowerCaseFirst(periodLabel))
                )
              ),
              missing = false
            )

          case None =>
            val addLinkText = messages("checkNonStandardReturnPeriods.addEndDate", monthYear)
            val addLink =
              Html(
                s"""<a class="govuk-link" href="${routes.EnterNonStandardPeriodDateController
                    .onPageLoad(periodNumber, NormalMode)
                    .url}">${HtmlFormat.escape(addLinkText).body}</a>"""
              )

            createRow(label = periodLabel, value = HtmlContent(addLink), action = None, missing = true)
        }
      }

    NonStandard(returnPeriodsChoice, selectedMonths, rows, showFinalPeriodWarning)
  }

  def formatMonth(date: LocalDate, locale: Locale): String =
    date.format(DateTimeFormatter.ofPattern("MMM yyyy", locale))

  def formatDate(date: LocalDate, locale: Locale): String =
    date.format(DateTimeFormatter.ofPattern("d MMM yyyy", locale))

  private def standardMonthKeys(returnPeriods: ChooseReturnPeriods): Seq[String] =
    returnPeriods match {
      case ChooseReturnPeriods.Jan => Seq("months.january", "months.april", "months.july", "months.october")
      case ChooseReturnPeriods.Feb => Seq("months.february", "months.may", "months.august", "months.november")
      case ChooseReturnPeriods.Mar => Seq("months.march", "months.june", "months.september", "months.december")
    }

  private def nonStandardMonthKeys(returnPeriodsId: Int): Seq[String] =
    ChooseReturnPeriods.fromReturnPeriodsId(returnPeriodsId).map(standardMonthKeys).getOrElse(Seq.empty)

  private def lowerCaseFirst(value: String): String =
    value.headOption.map(first => s"${first.toLower}${value.tail}").getOrElse(value)

  private def createRow(label: String, value: Content, action: Option[ActionItem], missing: Boolean): SummaryListRow =
    SummaryListRow(
      key     = Key(content = Text(label)),
      value   = Value(content = value),
      actions = action.map(item => Actions(items = Seq(item))),
      classes = if (missing) "govuk-summary-list__row--no-actions" else ""
    )
}
