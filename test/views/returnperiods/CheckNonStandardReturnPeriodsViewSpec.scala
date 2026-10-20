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

package views.returnperiods

import base.SpecBase
import models.{CheckMode, NoReturnPeriodsSet}
import org.jsoup.Jsoup
import play.api.Application
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.twirl.api.Html
import scala.jdk.CollectionConverters.*
import viewmodels.checkAnswers.returnperiods.CheckNonStandardReturnPeriodsViewModel
import views.html.returnperiods.CheckNonStandardReturnPeriodsView

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class CheckNonStandardReturnPeriodsViewSpec extends SpecBase {

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

  private val view: CheckNonStandardReturnPeriodsView =
    app.injector.instanceOf[CheckNonStandardReturnPeriodsView]

  private val dateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM yyyy")

  private def viewModel(
    returnPeriodsChoice: Option[NoReturnPeriodsSet] = Some(NoReturnPeriodsSet.Standard),
    returnPeriodsId: Int = 1,
    periodEnds: Seq[LocalDate] = calculatedPeriodEnds,
    dates: Seq[Option[LocalDate]] = enteredDates,
    showFinalPeriodWarning: Boolean = false
  ): CheckNonStandardReturnPeriodsViewModel =
    CheckNonStandardReturnPeriodsViewModel(
      returnPeriodsChoice    = returnPeriodsChoice,
      returnPeriodsId        = returnPeriodsId,
      calculatedPeriodEnds   = periodEnds,
      enteredDates           = dates,
      showFinalPeriodWarning = showFinalPeriodWarning
    )

  private def createView(
    model: CheckNonStandardReturnPeriodsViewModel = viewModel()
  ): Html = {
    given play.api.mvc.Request[AnyContentAsEmpty.type] = FakeRequest()
    view(model)
  }

  private def document(
    model: CheckNonStandardReturnPeriodsViewModel = viewModel()
  ) =
    Jsoup.parse(createView(model).toString)

  private def summaryRows(
    model: CheckNonStandardReturnPeriodsViewModel = viewModel()
  ) =
    document(model)
      .select(".govuk-summary-list__row")
      .asScala
      .toList

  "CheckNonStandardReturnPeriodsView" - {

    "render the page heading" in {
      document()
        .select("h1")
        .text() mustEqual msgs("checkNonStandardReturnPeriods.heading")
    }

    "render the standard return periods choice" in {
      val choiceRow = summaryRows().head

      choiceRow.select(".govuk-summary-list__key").text() mustEqual
        msgs("checkNonStandardReturnPeriods.returnPeriods")

      choiceRow.select(".govuk-summary-list__value").text() mustEqual
        msgs("checkNonStandardReturnPeriods.standard")
    }

    "render the non-standard return periods choice" in {
      val rows = summaryRows(
        viewModel(returnPeriodsChoice = Some(NoReturnPeriodsSet.Nonstandard))
      )

      rows.head.select(".govuk-summary-list__value").text() mustEqual
        msgs("checkNonStandardReturnPeriods.nonStandard")
    }

    "omit the return periods choice row when the choice is missing" in {
      val rows = summaryRows(
        viewModel(returnPeriodsChoice = None)
      )

      rows.size mustEqual 9

      rows.head.select(".govuk-summary-list__key").text() mustEqual
        msgs("checkNonStandardReturnPeriods.returnPeriodsEnding")

      rows.exists(
        _.select(".govuk-summary-list__key").text() ==
          msgs("checkNonStandardReturnPeriods.returnPeriods")
      ) mustBe false
    }

    "render the selected return period months as a bullet list" in {
      val monthsRow = summaryRows()(1)

      monthsRow.select(".govuk-summary-list__key").text() mustEqual
        msgs("checkNonStandardReturnPeriods.returnPeriodsEnding")

      monthsRow
        .select(".govuk-summary-list__value li")
        .eachText()
        .asScala
        .toList mustEqual List(
        msgs("months.january"),
        msgs("months.april"),
        msgs("months.july"),
        msgs("months.october")
      )
    }

    "render the correct selected months for return periods ID 2" in {
      val rows = summaryRows(viewModel(returnPeriodsId = 2))

      rows(1)
        .select(".govuk-summary-list__value li")
        .eachText()
        .asScala
        .toList mustEqual List(
        msgs("months.february"),
        msgs("months.may"),
        msgs("months.august"),
        msgs("months.november")
      )
    }

    "render the correct selected months for return periods ID 3" in {
      val rows = summaryRows(viewModel(returnPeriodsId = 3))

      rows(1)
        .select(".govuk-summary-list__value li")
        .eachText()
        .asScala
        .toList mustEqual List(
        msgs("months.march"),
        msgs("months.june"),
        msgs("months.september"),
        msgs("months.december")
      )
    }

    "render no selected months for an unknown return periods ID" in {
      val rows = summaryRows(viewModel(returnPeriodsId = 4))

      rows(1)
        .select(".govuk-summary-list__value li")
        .size() mustEqual 0
    }

    "render the return periods choice change link" in {
      val changeLink =
        summaryRows().head.select(".govuk-summary-list__actions a")

      changeLink.attr("href") mustEqual
        controllers.returnperiods.routes.ReturnPeriodsController
          .onPageLoad(CheckMode)
          .url

      changeLink.text() must include(msgs("site.change"))
    }

    "not render a return periods choice change link when the choice is missing" in {
      val choiceRows = summaryRows(
        viewModel(returnPeriodsChoice = None)
      ).filter(
        _.select(".govuk-summary-list__key").text() ==
          msgs("checkNonStandardReturnPeriods.returnPeriods")
      )

      choiceRows mustBe empty
    }

    "render the selected months change link" in {
      val changeLink =
        summaryRows()(1).select(".govuk-summary-list__actions a")

      changeLink.attr("href") mustEqual
        controllers.returnperiods.routes.ChooseReturnPeriodsController
          .onPageLoad(CheckMode)
          .url
    }

    "render all eight entered period dates" in {
      val rows = summaryRows()

      // One return-period choice row, one selected-months row,
      // followed by eight period-date rows.
      rows.size mustEqual 10

      val actualDates =
        rows
          .drop(2)
          .map(
            _.select(".govuk-summary-list__value").text()
          )

      val expectedDates =
        enteredDates.flatten.map(_.format(dateFormatter))

      actualDates mustEqual expectedDates
    }

    "render the continue link" in {
      val continueLink =
        document().select("a.govuk-button")

      continueLink.size() mustEqual 1

      continueLink.attr("href") mustEqual
        controllers.routes.ChangeRegistrationDetailsController
          .onPageLoad()
          .url
    }

    "render the standard warning when the final period warning is false" in {
      val doc = document(
        viewModel(showFinalPeriodWarning = false)
      )

      doc.select("main").text() must include(
        msgs("checkNonStandardReturnPeriods.warning1")
      )
    }

    "render the final period warning when the final period warning is true" in {
      val doc = document(
        viewModel(showFinalPeriodWarning = true)
      )

      doc.select("main").text() must include(
        msgs("checkNonStandardReturnPeriods.warning2")
      )
    }

    "render the period-date change links in CheckMode" in {
      val periodRows = summaryRows().drop(2)

      periodRows.zipWithIndex.foreach { case (row, index) =>
        val periodNumber = index + 1

        row.select(".govuk-summary-list__actions a").attr("href") mustEqual
          controllers.returnperiods.routes.EnterNonStandardPeriodDateController
            .onPageLoad(periodNumber, models.CheckMode)
            .url
      }
    }

    "render NormalMode add links for missing period dates" in {
      val dates =
        enteredDates.updated(2, None).updated(7, None)

      val rows =
        summaryRows(viewModel(dates = dates))

      val periodRows = rows.drop(2)

      Seq(2, 7).foreach { index =>
        val row = periodRows(index)

        row.select(".govuk-summary-list__actions").isEmpty mustBe true

        row.select(".govuk-summary-list__value a").attr("href") mustEqual
          controllers.returnperiods.routes.EnterNonStandardPeriodDateController
            .onPageLoad(index + 1, models.NormalMode)
            .url
      }
    }
  }
}
