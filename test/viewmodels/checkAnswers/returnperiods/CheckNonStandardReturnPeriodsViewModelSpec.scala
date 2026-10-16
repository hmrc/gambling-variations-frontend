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

import base.SpecBase
import controllers.returnperiods.routes
import models.{CheckMode, NormalMode}
import play.api.Application
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}

import java.time.LocalDate
import java.util.Locale

class CheckNonStandardReturnPeriodsViewModelSpec extends SpecBase {

  private val app: Application =
    applicationBuilder().build()

  private implicit val msgs: Messages =
    messages(app)

  private val calculatedPeriodEnds: Seq[LocalDate] =
    Seq(
      LocalDate.of(2026, 1, 31),
      LocalDate.of(2026, 4, 30),
      LocalDate.of(2026, 7, 31),
      LocalDate.of(2026, 10, 31),
      LocalDate.of(2027, 1, 31),
      LocalDate.of(2027, 4, 30),
      LocalDate.of(2027, 7, 31),
      LocalDate.of(2027, 10, 31)
    )

  private val enteredDates: Seq[Option[LocalDate]] =
    Seq(
      Some(LocalDate.of(2026, 1, 30)),
      Some(LocalDate.of(2026, 4, 29)),
      Some(LocalDate.of(2026, 7, 30)),
      Some(LocalDate.of(2026, 10, 30)),
      Some(LocalDate.of(2027, 1, 30)),
      Some(LocalDate.of(2027, 4, 29)),
      Some(LocalDate.of(2027, 7, 30)),
      Some(LocalDate.of(2027, 10, 30))
    )

  private def viewModel(
    returnPeriodsId: Int = 1,
    periodEnds: Seq[LocalDate] = calculatedPeriodEnds,
    dates: Seq[Option[LocalDate]] = enteredDates
  ): CheckNonStandardReturnPeriodsViewModel =
    CheckNonStandardReturnPeriodsViewModel(
      returnPeriodsId      = returnPeriodsId,
      calculatedPeriodEnds = periodEnds,
      enteredDates         = dates
    )

  "CheckNonStandardReturnPeriodsViewModel.apply" - {

    "must return the selected months for return periods ID 1" in {

      val result =
        viewModel(returnPeriodsId = 1)

      result.selectedMonths mustEqual Seq(
        msgs("months.january"),
        msgs("months.april"),
        msgs("months.july"),
        msgs("months.october")
      )
    }

    "must return the selected months for return periods ID 2" in {

      val result =
        viewModel(returnPeriodsId = 2)

      result.selectedMonths mustEqual Seq(
        msgs("months.february"),
        msgs("months.may"),
        msgs("months.august"),
        msgs("months.november")
      )
    }

    "must return the selected months for return periods ID 3" in {

      val result =
        viewModel(returnPeriodsId = 3)

      result.selectedMonths mustEqual Seq(
        msgs("months.march"),
        msgs("months.june"),
        msgs("months.september"),
        msgs("months.december")
      )
    }

    "must return no selected months for an unknown return periods ID" in {

      val result =
        viewModel(returnPeriodsId = 4)

      result.selectedMonths mustBe empty
    }

    "must create 8 rows for the 8 calculated period ends" in {

      val result =
        viewModel()

      result.rows.size mustEqual 8
    }

    "must create the correct period labels for all 8 periods" in {

      val result =
        viewModel()

      result.rows.map(_.key.content) mustEqual Seq(
        Text(
          msgs(
            "checkNonStandardReturnPeriods.periodLabel",
            "Jan 2026"
          )
        ),
        Text(
          msgs(
            "checkNonStandardReturnPeriods.periodLabel",
            "Apr 2026"
          )
        ),
        Text(
          msgs(
            "checkNonStandardReturnPeriods.periodLabel",
            "Jul 2026"
          )
        ),
        Text(
          msgs(
            "checkNonStandardReturnPeriods.periodLabel",
            "Oct 2026"
          )
        ),
        Text(
          msgs(
            "checkNonStandardReturnPeriods.periodLabel",
            "Jan 2027"
          )
        ),
        Text(
          msgs(
            "checkNonStandardReturnPeriods.periodLabel",
            "Apr 2027"
          )
        ),
        Text(
          msgs(
            "checkNonStandardReturnPeriods.periodLabel",
            "Jul 2027"
          )
        ),
        Text(
          msgs(
            "checkNonStandardReturnPeriods.periodLabel",
            "Oct 2027"
          )
        )
      )
    }

    "must display all 8 entered dates" in {

      val result =
        viewModel()

      result.rows.map(_.value.content) mustEqual Seq(
        Text("30 Jan 2026"),
        Text("29 Apr 2026"),
        Text("30 Jul 2026"),
        Text("30 Oct 2026"),
        Text("30 Jan 2027"),
        Text("29 Apr 2027"),
        Text("30 Jul 2027"),
        Text("30 Oct 2027")
      )
    }

    "must create the correct CheckMode change link for all 8 periods" in {

      val result =
        viewModel()

      result.rows.zipWithIndex.foreach { case (row, index) =>
        val periodNumber =
          index + 1

        val action =
          row.actions.value.items.head

        action.href mustEqual
          routes.EnterNonStandardPeriodDateController
            .onPageLoad(periodNumber, CheckMode)
            .url

        action.content mustEqual Text(msgs("site.change"))
      }
    }

    "must not apply the missing row class when all 8 dates are present" in {

      val result =
        viewModel()

      result.rows.foreach { row =>
        row.classes mustEqual ""
        row.actions.isDefined mustEqual true
      }
    }

    "must set the visually hidden text on a change action" in {

      val result =
        viewModel()

      val periodLabel =
        msgs(
          "checkNonStandardReturnPeriods.periodLabel",
          "Jan 2026"
        )

      val expectedHiddenText =
        periodLabel.headOption
          .map(first => s"${first.toLower}${periodLabel.tail}")
          .getOrElse(periodLabel)

      val action =
        result.rows.head.actions.value.items.head

      action.visuallyHiddenText mustEqual
        Some(expectedHiddenText)
    }

    "must create a NormalMode add link when a date is missing" in {

      val dates =
        enteredDates.updated(2, None)

      val result =
        viewModel(dates = dates)

      val row =
        result.rows(2)

      row.value.content match {
        case HtmlContent(html) =>
          html.body must include(
            routes.EnterNonStandardPeriodDateController
              .onPageLoad(3, NormalMode)
              .url
          )

          html.body must include(
            msgs(
              "checkNonStandardReturnPeriods.addEndDate",
              "Jul 2026"
            )
          )

        case content =>
          fail(s"Expected HtmlContent but got $content")
      }

      row.actions mustEqual None

      row.classes mustEqual
        "govuk-summary-list__row--no-actions"
    }

    "must use the correct NormalMode period number for a missing eighth date" in {

      val dates =
        enteredDates.updated(7, None)

      val result =
        viewModel(dates = dates)

      val row =
        result.rows(7)

      row.value.content match {
        case HtmlContent(html) =>
          html.body must include(
            routes.EnterNonStandardPeriodDateController
              .onPageLoad(8, NormalMode)
              .url
          )

          html.body must include(
            msgs(
              "checkNonStandardReturnPeriods.addEndDate",
              "Oct 2027"
            )
          )

        case content =>
          fail(s"Expected HtmlContent but got $content")
      }

      row.actions mustEqual None

      row.classes mustEqual
        "govuk-summary-list__row--no-actions"
    }

    "must correctly create populated and missing rows across all 8 periods" in {

      val result =
        viewModel(
          dates = Seq(
            Some(LocalDate.of(2026, 1, 30)),
            Some(LocalDate.of(2026, 4, 29)),
            None,
            Some(LocalDate.of(2026, 10, 30)),
            None,
            Some(LocalDate.of(2027, 4, 29)),
            Some(LocalDate.of(2027, 7, 30)),
            None
          )
        )

      result.rows.size mustEqual 8

      result.rows(0).value.content mustEqual
        Text("30 Jan 2026")
      result.rows(0).actions.isDefined mustEqual true
      result.rows(0).classes mustEqual ""

      result.rows(1).value.content mustEqual
        Text("29 Apr 2026")
      result.rows(1).actions.isDefined mustEqual true
      result.rows(1).classes mustEqual ""

      result.rows(2).value.content.isInstanceOf[HtmlContent] mustEqual true
      result.rows(2).actions mustEqual None
      result.rows(2).classes mustEqual
        "govuk-summary-list__row--no-actions"

      result.rows(3).value.content mustEqual
        Text("30 Oct 2026")
      result.rows(3).actions.isDefined mustEqual true
      result.rows(3).classes mustEqual ""

      result.rows(4).value.content.isInstanceOf[HtmlContent] mustEqual true
      result.rows(4).actions mustEqual None
      result.rows(4).classes mustEqual
        "govuk-summary-list__row--no-actions"

      result.rows(5).value.content mustEqual
        Text("29 Apr 2027")
      result.rows(5).actions.isDefined mustEqual true
      result.rows(5).classes mustEqual ""

      result.rows(6).value.content mustEqual
        Text("30 Jul 2027")
      result.rows(6).actions.isDefined mustEqual true
      result.rows(6).classes mustEqual ""

      result.rows(7).value.content.isInstanceOf[HtmlContent] mustEqual true
      result.rows(7).actions mustEqual None
      result.rows(7).classes mustEqual
        "govuk-summary-list__row--no-actions"
    }

    "must use the correct NormalMode link for every missing date" in {

      val result =
        viewModel(
          dates = Seq.fill(8)(None)
        )

      result.rows.size mustEqual 8

      result.rows.zipWithIndex.foreach { case (row, index) =>
        val periodNumber =
          index + 1

        row.value.content match {
          case HtmlContent(html) =>
            html.body must include(
              routes.EnterNonStandardPeriodDateController
                .onPageLoad(periodNumber, NormalMode)
                .url
            )

          case content =>
            fail(s"Expected HtmlContent but got $content")
        }

        row.actions mustEqual None

        row.classes mustEqual
          "govuk-summary-list__row--no-actions"
      }
    }

    "must treat a missing enteredDates position as a missing date" in {

      val result =
        viewModel(
          dates = enteredDates.take(4)
        )

      result.rows.size mustEqual 8

      result.rows.take(4).foreach { row =>
        row.actions.isDefined mustEqual true
        row.classes mustEqual ""
      }

      result.rows.drop(4).foreach { row =>
        row.value.content.isInstanceOf[HtmlContent] mustEqual true
        row.actions mustEqual None
        row.classes mustEqual
          "govuk-summary-list__row--no-actions"
      }
    }

    "must ignore entered dates that do not have a corresponding calculated period end" in {

      val additionalDate =
        Some(LocalDate.of(2028, 1, 30))

      val result =
        viewModel(
          dates = enteredDates :+ additionalDate
        )

      result.rows.size mustEqual 8

      result.rows.map(_.value.content) mustEqual Seq(
        Text("30 Jan 2026"),
        Text("29 Apr 2026"),
        Text("30 Jul 2026"),
        Text("30 Oct 2026"),
        Text("30 Jan 2027"),
        Text("29 Apr 2027"),
        Text("30 Jul 2027"),
        Text("30 Oct 2027")
      )
    }

    "must return no rows when there are no calculated period ends" in {

      val result =
        viewModel(
          periodEnds = Seq.empty,
          dates      = Seq.empty
        )

      result.rows mustBe empty
    }
  }

  "CheckNonStandardReturnPeriodsViewModel.formatMonth" - {

    "must format January correctly" in {

      CheckNonStandardReturnPeriodsViewModel.formatMonth(
        LocalDate.of(2026, 1, 31),
        Locale.ENGLISH
      ) mustEqual "Jan 2026"
    }

    "must format December correctly" in {

      CheckNonStandardReturnPeriodsViewModel.formatMonth(
        LocalDate.of(2027, 12, 31),
        Locale.ENGLISH
      ) mustEqual "Dec 2027"
    }
  }

  "CheckNonStandardReturnPeriodsViewModel.formatDate" - {

    "must format a single digit day without a leading zero" in {

      CheckNonStandardReturnPeriodsViewModel.formatDate(
        LocalDate.of(2026, 1, 5),
        Locale.ENGLISH
      ) mustEqual "5 Jan 2026"
    }

    "must format a two digit day correctly" in {

      CheckNonStandardReturnPeriodsViewModel.formatDate(
        LocalDate.of(2026, 10, 30),
        Locale.ENGLISH
      ) mustEqual "30 Oct 2026"
    }
  }
}
