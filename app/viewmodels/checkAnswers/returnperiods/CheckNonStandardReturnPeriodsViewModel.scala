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
import models.{CheckMode, NormalMode}
import play.api.i18n.Messages
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{Content, HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

final case class CheckNonStandardReturnPeriodsViewModel(
  selectedMonths: Seq[String],
  rows: Seq[SummaryListRow]
)

object CheckNonStandardReturnPeriodsViewModel {

  def apply(
    returnPeriodsId: Int,
    calculatedPeriodEnds: Seq[LocalDate],
    enteredDates: Seq[Option[LocalDate]]
  )(implicit messages: Messages): CheckNonStandardReturnPeriodsViewModel = {

    val selectedMonths =
      selectedMonthKeys(returnPeriodsId).map(messages(_))

    val locale =
      Locale.forLanguageTag(messages.lang.code)

    val rows =
      calculatedPeriodEnds.zipWithIndex.map { case (periodEnd, index) =>
        val periodNumber = index + 1

        val monthYear =
          formatMonth(periodEnd, locale)

        val periodLabel =
          messages("checkNonStandardReturnPeriods.periodLabel", monthYear)

        enteredDates.lift(index).flatten match {
          case Some(enteredDate) =>
            createRow(
              label = periodLabel,
              value = Text(formatDate(enteredDate, locale)),
              action = Some(
                ActionItem(
                  href = routes.EnterNonStandardPeriodDateController
                    .onPageLoad(periodNumber, CheckMode)
                    .url,
                  content            = Text(messages("site.change")),
                  visuallyHiddenText = Some(lowerCaseFirst(periodLabel))
                )
              ),
              missing = false
            )

          case None =>
            val addLinkText =
              messages(
                "checkNonStandardReturnPeriods.addEndDate",
                monthYear
              )

            val addLink =
              Html(
                s"""<a class="govuk-link" href="${routes.EnterNonStandardPeriodDateController
                    .onPageLoad(periodNumber, NormalMode)
                    .url}">${HtmlFormat.escape(addLinkText).body}</a>"""
              )

            createRow(
              label   = periodLabel,
              value   = HtmlContent(addLink),
              action  = None,
              missing = true
            )
        }
      }

    CheckNonStandardReturnPeriodsViewModel(
      selectedMonths = selectedMonths,
      rows           = rows
    )
  }

  def formatMonth(date: LocalDate, locale: Locale): String =
    date.format(DateTimeFormatter.ofPattern("MMM yyyy", locale))

  def formatDate(date: LocalDate, locale: Locale): String =
    date.format(DateTimeFormatter.ofPattern("d MMM yyyy", locale))

  private def lowerCaseFirst(value: String): String =
    value.headOption
      .map(first => s"${first.toLower}${value.tail}")
      .getOrElse(value)

  private def selectedMonthKeys(returnPeriodsId: Int): Seq[String] =
    returnPeriodsId match {
      case 1 =>
        Seq(
          "months.january",
          "months.april",
          "months.july",
          "months.october"
        )

      case 2 =>
        Seq(
          "months.february",
          "months.may",
          "months.august",
          "months.november"
        )

      case 3 =>
        Seq(
          "months.march",
          "months.june",
          "months.september",
          "months.december"
        )

      case _ =>
        Seq.empty
    }

  private def createRow(
    label: String,
    value: Content,
    action: Option[ActionItem],
    missing: Boolean
  ): SummaryListRow =
    SummaryListRow(
      key     = Key(content = Text(label)),
      value   = Value(content = value),
      actions = action.map(item => Actions(items = Seq(item))),
      classes =
        if (missing) "govuk-summary-list__row--no-actions"
        else ""
    )
}
