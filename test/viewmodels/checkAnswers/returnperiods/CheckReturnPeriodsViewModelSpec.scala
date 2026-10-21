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
import models.ChooseReturnPeriods.{Feb, Jan, Mar}
import models.{CheckMode, GamblingReturnPeriods, NoReturnPeriodsSet, NormalMode, UserAnswers}
import pages.returnperiods.{ChooseReturnPeriodsPage, GamblingReturnPeriodsPage, HasExistingNstpValuesPage}
import play.api.Application
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}

import java.time.LocalDate
import java.util.Locale

class CheckReturnPeriodsViewModelSpec extends SpecBase {

  private val app: Application = applicationBuilder().build()

  private implicit val msgs: Messages = messages(app)

  private val standardReturnPeriods =
    GamblingReturnPeriods(
      mgdRegNumber          = mgdRegNum,
      returnPeriodsId       = Some(Jan.returnPeriodsId),
      nstpEndDate1          = None,
      nstpEndDate2          = None,
      nstpEndDate3          = None,
      nstpEndDate4          = None,
      nstpEndDate5          = None,
      nstpEndDate6          = None,
      nstpEndDate7          = None,
      nstpEndDate8          = None,
      isInLastNstp          = None,
      finalPeriodWarning    = Some(false),
      hasExistingNstpValues = Some(false)
    )

  private def answersWith(returnPeriods: GamblingReturnPeriods): UserAnswers =
    emptyUserAnswers.set(GamblingReturnPeriodsPage, returnPeriods).success.value

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

  private def nonStandardViewModel(
    returnPeriodsId: Int = 1,
    periodEnds: Seq[LocalDate] = calculatedPeriodEnds,
    dates: Seq[Option[LocalDate]] = enteredDates,
    returnPeriodsChoice: Option[NoReturnPeriodsSet] = Some(NoReturnPeriodsSet.Standard),
    showFinalPeriodWarning: Boolean = false
  ): CheckReturnPeriodsViewModel.NonStandard =
    CheckReturnPeriodsViewModel.nonStandard(
      returnPeriodsChoice    = returnPeriodsChoice,
      returnPeriodsId        = returnPeriodsId,
      calculatedPeriodEnds   = periodEnds,
      enteredDates           = dates,
      showFinalPeriodWarning = showFinalPeriodWarning
    )

  "CheckReturnPeriodsViewModel.from" - {

    "must use the return periods held for the registration when the user has not changed them" in {
      CheckReturnPeriodsViewModel.from(answersWith(standardReturnPeriods)).value.returnPeriods mustBe Some(Jan)
    }

    "must use the return periods the user has chosen over the ones held for the registration" in {
      val answers = answersWith(standardReturnPeriods).set(ChooseReturnPeriodsPage, Mar.returnPeriodsId).success.value

      CheckReturnPeriodsViewModel.from(answers).value.returnPeriods mustBe Some(Mar)
    }

    "must show the applies after current period message when finalPeriodWarning is false" in {
      CheckReturnPeriodsViewModel.from(answersWith(standardReturnPeriods)).value.showAppliesAfterCurrentPeriod mustBe true
    }

    "must not show the applies after current period message when finalPeriodWarning is true" in {
      val answers = answersWith(standardReturnPeriods.copy(finalPeriodWarning = Some(true)))

      CheckReturnPeriodsViewModel.from(answers).value.showAppliesAfterCurrentPeriod mustBe false
    }

    "must not show the applies after current period message when finalPeriodWarning is missing" in {
      val answers = answersWith(standardReturnPeriods.copy(finalPeriodWarning = None))

      CheckReturnPeriodsViewModel.from(answers).value.showAppliesAfterCurrentPeriod mustBe false
    }

    "must be empty when the registration has non-standard return periods" in {
      CheckReturnPeriodsViewModel.from(answersWith(standardReturnPeriods.copy(hasExistingNstpValues = Some(true)))) mustBe None
    }

    "must be empty when the user is switching to non-standard return periods" in {
      val answers = answersWith(standardReturnPeriods).set(HasExistingNstpValuesPage, true).success.value

      CheckReturnPeriodsViewModel.from(answers) mustBe None
    }

    "must be standard when the user has chosen standard return periods, whatever is held for the registration" in {
      val answers =
        answersWith(standardReturnPeriods.copy(hasExistingNstpValues = Some(true)))
          .set(HasExistingNstpValuesPage, false)
          .success
          .value

      CheckReturnPeriodsViewModel.from(answers).value.returnPeriods mustBe Some(Jan)
    }

    "must be empty when hasExistingNstpValues is missing" in {
      CheckReturnPeriodsViewModel.from(answersWith(standardReturnPeriods.copy(hasExistingNstpValues = None))) mustBe None
    }

    "must have no return periods when none have been set" in {
      CheckReturnPeriodsViewModel.from(answersWith(standardReturnPeriods.copy(returnPeriodsId = None))).value.returnPeriods mustBe None
    }

    "must have no return periods when the return periods id is not recognised" in {
      CheckReturnPeriodsViewModel.from(answersWith(standardReturnPeriods.copy(returnPeriodsId = Some(9)))).value.returnPeriods mustBe None
    }

    "must be empty when there are no return periods in the user answers" in {
      CheckReturnPeriodsViewModel.from(emptyUserAnswers) mustBe None
    }

    "must show the Return periods row when no return periods are set for the registration" in {
      val notSet = standardReturnPeriods.copy(returnPeriodsId = None)

      CheckReturnPeriodsViewModel.from(answersWith(notSet)).value.showReturnPeriodsType mustBe true
    }

    "must not show the Return periods row when return periods are set for the registration" in {
      CheckReturnPeriodsViewModel.from(answersWith(standardReturnPeriods)).value.showReturnPeriodsType mustBe false
    }

    "must be standard when no return periods are set and the user has chosen periods, even without hasExistingNstpValues" in {
      val notSet = standardReturnPeriods.copy(returnPeriodsId = None, hasExistingNstpValues = None)
      val answers = answersWith(notSet).set(ChooseReturnPeriodsPage, Feb.returnPeriodsId).success.value

      val viewModel = CheckReturnPeriodsViewModel.from(answers).value
      viewModel.returnPeriods mustBe Some(Feb)
      viewModel.showReturnPeriodsType mustBe true
    }

    "must be empty when no return periods are set, the user has not chosen periods and hasExistingNstpValues is missing" in {
      val notSet = standardReturnPeriods.copy(returnPeriodsId = None, hasExistingNstpValues = None)

      CheckReturnPeriodsViewModel.from(answersWith(notSet)) mustBe None
    }

    "must be empty when the registration has non-standard return periods, even if the user has chosen periods" in {
      val answers = answersWith(standardReturnPeriods.copy(hasExistingNstpValues = Some(true)))
        .set(ChooseReturnPeriodsPage, Feb.returnPeriodsId)
        .success
        .value

      CheckReturnPeriodsViewModel.from(answers) mustBe None
    }
  }

  "CheckReturnPeriodsViewModel.Standard.summaryList" - {

    "must have a single Return periods ending row" in {
      val rows = CheckReturnPeriodsViewModel.Standard(Some(Jan), showAppliesAfterCurrentPeriod = true).summaryList.rows

      rows.size mustBe 1
      rows.head.key.content mustBe Text("Return periods ending")
    }

    "must list the four months for each set of return periods as a bulleted list" in {
      def months(viewModel: CheckReturnPeriodsViewModel.Standard) = viewModel.summaryList.rows.head.value.content

      def bulleted(months: String*) =
        HtmlContent(months.map(month => s"<li>$month</li>").mkString("""<ul class="govuk-list govuk-list--bullet">""", "", "</ul>"))

      months(CheckReturnPeriodsViewModel.Standard(Some(Jan), showAppliesAfterCurrentPeriod = true)) mustBe bulleted("January",
                                                                                                                    "April",
                                                                                                                    "July",
                                                                                                                    "October"
                                                                                                                   )
      months(CheckReturnPeriodsViewModel.Standard(Some(Feb), showAppliesAfterCurrentPeriod = true)) mustBe bulleted("February",
                                                                                                                    "May",
                                                                                                                    "August",
                                                                                                                    "November"
                                                                                                                   )
      months(CheckReturnPeriodsViewModel.Standard(Some(Mar), showAppliesAfterCurrentPeriod = true)) mustBe bulleted("March",
                                                                                                                    "June",
                                                                                                                    "September",
                                                                                                                    "December"
                                                                                                                   )
    }

    "must have a change link to the choose return periods page in CheckMode" in {
      val actions = CheckReturnPeriodsViewModel.Standard(Some(Jan), showAppliesAfterCurrentPeriod = true).summaryList.rows.head.actions.value.items

      actions.size mustBe 1
      actions.head.href mustBe controllers.returnperiods.routes.ChooseReturnPeriodsController.onPageLoad(CheckMode).url
      actions.head.content mustBe Text("Change")
      actions.head.visuallyHiddenText mustBe Some("return periods")
    }

    "must put a Return periods row showing Standard above Return periods ending when shown" in {
      val rows = CheckReturnPeriodsViewModel.Standard(Some(Feb), showAppliesAfterCurrentPeriod = true, showReturnPeriodsType = true).summaryList.rows

      rows.map(_.key.content) mustBe Seq(Text("Return periods"), Text("Return periods ending"))
      rows.head.value.content mustBe Text("Standard")
    }

    "must give the Return periods row a change link to the return periods page in CheckMode" in {
      val actions =
        CheckReturnPeriodsViewModel
          .Standard(Some(Feb), showAppliesAfterCurrentPeriod = true, showReturnPeriodsType = true)
          .summaryList
          .rows
          .head
          .actions
          .value
          .items

      actions.size mustBe 1
      actions.head.href mustBe controllers.returnperiods.routes.ReturnPeriodsController.onPageLoad(CheckMode).url
      actions.head.content mustBe Text("Change")
      actions.head.visuallyHiddenText mustBe Some("return period type")
    }

    "must show an add link with no change action when no return periods have been set" in {
      val row = CheckReturnPeriodsViewModel.Standard(None, showAppliesAfterCurrentPeriod = true).summaryList.rows.head

      val addUrl = controllers.returnperiods.routes.ChooseReturnPeriodsController.onPageLoad(CheckMode).url

      row.key.content mustBe Text("Return periods ending")
      row.value.content mustBe HtmlContent(s"""<a class="govuk-link" href="$addUrl">Add return periods ending</a>""")
      row.actions mustBe None
    }
  }

  "CheckReturnPeriodsViewModel.nonStandard" - {

    "must return the selected months for return periods ID 1" in {
      nonStandardViewModel(returnPeriodsId = 1).selectedMonths mustEqual Seq(
        msgs("months.january"),
        msgs("months.april"),
        msgs("months.july"),
        msgs("months.october")
      )
    }

    "must return the selected months for return periods ID 2" in {
      nonStandardViewModel(returnPeriodsId = 2).selectedMonths mustEqual Seq(
        msgs("months.february"),
        msgs("months.may"),
        msgs("months.august"),
        msgs("months.november")
      )
    }

    "must return the selected months for return periods ID 3" in {
      nonStandardViewModel(returnPeriodsId = 3).selectedMonths mustEqual Seq(
        msgs("months.march"),
        msgs("months.june"),
        msgs("months.september"),
        msgs("months.december")
      )
    }

    "must return no selected months for an unknown return periods ID" in {
      nonStandardViewModel(returnPeriodsId = 4).selectedMonths mustBe empty
    }

    "must preserve the return periods choice when it is present" in {
      nonStandardViewModel(returnPeriodsChoice = Some(NoReturnPeriodsSet.Standard)).returnPeriodsChoice mustEqual Some(NoReturnPeriodsSet.Standard)
    }

    "must support a missing return periods choice" in {
      nonStandardViewModel(returnPeriodsChoice = None).returnPeriodsChoice mustBe None
    }

    "must preserve the final period warning flag" in {
      nonStandardViewModel(showFinalPeriodWarning = true).showFinalPeriodWarning mustBe true

      nonStandardViewModel(showFinalPeriodWarning = false).showFinalPeriodWarning mustBe false
    }

    "must create 8 rows for the 8 calculated period ends" in {
      nonStandardViewModel().rows.size mustEqual 8
    }

    "must create the correct period labels for all 8 periods" in {
      nonStandardViewModel().rows.map(_.key.content) mustEqual Seq(
        Text(msgs("checkNonStandardReturnPeriods.periodLabel", "Jan 2026")),
        Text(msgs("checkNonStandardReturnPeriods.periodLabel", "Apr 2026")),
        Text(msgs("checkNonStandardReturnPeriods.periodLabel", "Jul 2026")),
        Text(msgs("checkNonStandardReturnPeriods.periodLabel", "Oct 2026")),
        Text(msgs("checkNonStandardReturnPeriods.periodLabel", "Jan 2027")),
        Text(msgs("checkNonStandardReturnPeriods.periodLabel", "Apr 2027")),
        Text(msgs("checkNonStandardReturnPeriods.periodLabel", "Jul 2027")),
        Text(msgs("checkNonStandardReturnPeriods.periodLabel", "Oct 2027"))
      )
    }

    "must display all 8 entered dates" in {
      nonStandardViewModel().rows.map(_.value.content) mustEqual Seq(
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
      nonStandardViewModel().rows.zipWithIndex.foreach { case (row, index) =>
        val periodNumber = index + 1
        val action = row.actions.value.items.head

        action.href mustEqual
          routes.EnterNonStandardPeriodDateController
            .onPageLoad(periodNumber, CheckMode)
            .url

        action.content mustEqual Text(msgs("site.change"))
      }
    }

    "must not apply the missing row class when all 8 dates are present" in {
      nonStandardViewModel().rows.foreach { row =>
        row.classes mustEqual ""
        row.actions.isDefined mustBe true
      }
    }

    "must set the visually hidden text on a change action" in {
      val result = nonStandardViewModel()

      val periodLabel =
        msgs("checkNonStandardReturnPeriods.periodLabel", "Jan 2026")

      val expectedHiddenText =
        periodLabel.headOption
          .map(first => s"${first.toLower}${periodLabel.tail}")
          .getOrElse(periodLabel)

      result.rows.head.actions.value.items.head.visuallyHiddenText mustEqual
        Some(expectedHiddenText)
    }

    "must create a NormalMode add link when a date is missing" in {
      val result =
        nonStandardViewModel(dates = enteredDates.updated(2, None))

      val row = result.rows(2)

      row.value.content match {
        case HtmlContent(html) =>
          html.body must include(
            routes.EnterNonStandardPeriodDateController
              .onPageLoad(3, NormalMode)
              .url
          )

          html.body must include(
            msgs("checkNonStandardReturnPeriods.addEndDate", "Jul 2026")
          )

        case content =>
          fail(s"Expected HtmlContent but got $content")
      }

      row.actions mustBe None
      row.classes mustEqual "govuk-summary-list__row--no-actions"
    }

    "must use the correct NormalMode period number for a missing eighth date" in {
      val result =
        nonStandardViewModel(dates = enteredDates.updated(7, None))

      val row = result.rows(7)

      row.value.content match {
        case HtmlContent(html) =>
          html.body must include(
            routes.EnterNonStandardPeriodDateController
              .onPageLoad(8, NormalMode)
              .url
          )

          html.body must include(
            msgs("checkNonStandardReturnPeriods.addEndDate", "Oct 2027")
          )

        case content =>
          fail(s"Expected HtmlContent but got $content")
      }

      row.actions mustBe None
      row.classes mustEqual "govuk-summary-list__row--no-actions"
    }

    "must correctly create populated and missing rows across all 8 periods" in {
      val result =
        nonStandardViewModel(
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

      Seq(0, 1, 3, 5, 6).foreach { index =>
        result.rows(index).actions.isDefined mustBe true
        result.rows(index).classes mustEqual ""
      }

      Seq(2, 4, 7).foreach { index =>
        result.rows(index).value.content.isInstanceOf[HtmlContent] mustBe true
        result.rows(index).actions mustBe None
        result.rows(index).classes mustEqual
          "govuk-summary-list__row--no-actions"
      }
    }

    "must use the correct NormalMode link for every missing date" in {
      val result =
        nonStandardViewModel(dates = Seq.fill(8)(None))

      result.rows.size mustEqual 8

      result.rows.zipWithIndex.foreach { case (row, index) =>
        val periodNumber = index + 1

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

        row.actions mustBe None
        row.classes mustEqual "govuk-summary-list__row--no-actions"
      }
    }

    "must treat a missing enteredDates position as a missing date" in {
      val result =
        nonStandardViewModel(dates = enteredDates.take(4))

      result.rows.size mustEqual 8

      result.rows.take(4).foreach { row =>
        row.actions.isDefined mustBe true
        row.classes mustEqual ""
      }

      result.rows.drop(4).foreach { row =>
        row.value.content.isInstanceOf[HtmlContent] mustBe true
        row.actions mustBe None
        row.classes mustEqual "govuk-summary-list__row--no-actions"
      }
    }

    "must ignore entered dates that do not have a corresponding calculated period end" in {
      val result =
        nonStandardViewModel(
          dates = enteredDates :+ Some(LocalDate.of(2028, 1, 30))
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
      nonStandardViewModel(
        periodEnds = Seq.empty,
        dates      = Seq.empty
      ).rows mustBe empty
    }
  }

  "CheckReturnPeriodsViewModel.NonStandard.summaryList" - {

    "must put the return periods choice row, then the return periods ending row, above the period rows" in {
      nonStandardViewModel(returnPeriodsChoice = Some(NoReturnPeriodsSet.Nonstandard)).summaryList.rows.map(_.key.content).take(3) mustEqual Seq(
        Text(msgs("checkNonStandardReturnPeriods.returnPeriods")),
        Text(msgs("checkNonStandardReturnPeriods.returnPeriodsEnding")),
        Text(msgs("checkNonStandardReturnPeriods.periodLabel", "Jan 2026"))
      )
    }

    "must start with the return periods ending row when the choice is missing" in {
      val rows = nonStandardViewModel(returnPeriodsChoice = None).summaryList.rows

      rows.size mustEqual 9
      rows.head.key.content mustEqual Text(msgs("checkNonStandardReturnPeriods.returnPeriodsEnding"))
    }
  }

  "CheckReturnPeriodsViewModel.formatMonth" - {

    "must format January correctly" in {
      CheckReturnPeriodsViewModel.formatMonth(
        LocalDate.of(2026, 1, 31),
        Locale.ENGLISH
      ) mustEqual "Jan 2026"
    }

    "must format December correctly" in {
      CheckReturnPeriodsViewModel.formatMonth(
        LocalDate.of(2027, 12, 31),
        Locale.ENGLISH
      ) mustEqual "Dec 2027"
    }
  }

  "CheckReturnPeriodsViewModel.formatDate" - {

    "must format a single digit day without a leading zero" in {
      CheckReturnPeriodsViewModel.formatDate(
        LocalDate.of(2026, 1, 5),
        Locale.ENGLISH
      ) mustEqual "5 Jan 2026"
    }

    "must format a two digit day correctly" in {
      CheckReturnPeriodsViewModel.formatDate(
        LocalDate.of(2026, 10, 30),
        Locale.ENGLISH
      ) mustEqual "30 Oct 2026"
    }
  }
}
