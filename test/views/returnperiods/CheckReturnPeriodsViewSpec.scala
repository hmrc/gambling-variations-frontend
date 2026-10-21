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
import models.ChooseReturnPeriods.Feb
import models.{CheckMode, ChooseReturnPeriods, NoReturnPeriodsSet}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.Application
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import scala.jdk.CollectionConverters.*
import viewmodels.checkAnswers.returnperiods.CheckReturnPeriodsViewModel
import views.html.returnperiods.CheckReturnPeriodsView

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class CheckReturnPeriodsViewSpec extends SpecBase {

  private val app: Application =
    applicationBuilder().build()

  private implicit val msgs: Messages =
    messages(app)

  private implicit val request: play.api.mvc.Request[AnyContentAsEmpty.type] = FakeRequest()

  private val view: CheckReturnPeriodsView =
    app.injector.instanceOf[CheckReturnPeriodsView]

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

  private val dateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM yyyy")

  private def standardDocument(
    returnPeriods: Option[ChooseReturnPeriods] = Some(Feb),
    showAppliesAfterCurrentPeriod: Boolean = true,
    showReturnPeriodsType: Boolean = false
  ): Document =
    Jsoup.parse(
      view(CheckReturnPeriodsViewModel.Standard(returnPeriods, showAppliesAfterCurrentPeriod, showReturnPeriodsType)).body
    )

  private def nonStandardViewModel(
    returnPeriodsChoice: Option[NoReturnPeriodsSet] = Some(NoReturnPeriodsSet.Standard),
    returnPeriodsId: Int = 1,
    periodEnds: Seq[LocalDate] = calculatedPeriodEnds,
    dates: Seq[Option[LocalDate]] = enteredDates,
    showFinalPeriodWarning: Boolean = false
  ): CheckReturnPeriodsViewModel.NonStandard =
    CheckReturnPeriodsViewModel.nonStandard(
      returnPeriodsChoice    = returnPeriodsChoice,
      returnPeriodsId        = returnPeriodsId,
      calculatedPeriodEnds   = periodEnds,
      enteredDates           = dates,
      showFinalPeriodWarning = showFinalPeriodWarning
    )

  private def nonStandardDocument(
    model: CheckReturnPeriodsViewModel.NonStandard = nonStandardViewModel()
  ): Document =
    Jsoup.parse(view(model).body)

  private def nonStandardRows(
    model: CheckReturnPeriodsViewModel.NonStandard = nonStandardViewModel()
  ) =
    nonStandardDocument(model)
      .select(".govuk-summary-list__row")
      .asScala
      .toList

  private val versions: Seq[(String, () => Document, String)] =
    Seq(
      ("standard", () => standardDocument(), "Check your standard return periods"),
      ("non-standard", () => nonStandardDocument(), "Check your non-standard return periods")
    )

  "CheckReturnPeriodsView" - {

    "both versions" - {

      versions.foreach { case (version, document, heading) =>
        s"must render the title, caption and heading for the $version version" in {

          val doc = document()

          doc.title must startWith(s"$heading - Change registration details")

          doc.select(".govuk-caption-l").text() mustBe "Change registration details"

          doc.select("h1.govuk-heading-l").text() mustBe heading
        }

        s"must tell the user to continue to the change registration details page in the $version version" in {

          document().select("p.govuk-body").text() must include(
            "You must continue to the change registration details page to submit any changes."
          )
        }

        s"must render a Continue button in the $version version" in {

          document().select(".govuk-button").text() mustBe "Continue"
        }
      }
    }

    "standard version" - {

      "must render the Return periods ending row with the four months" in {

        val doc: Document = standardDocument()

        doc.select(".govuk-summary-list__row").size() mustBe 1

        doc.select(".govuk-summary-list__key").text() mustBe "Return periods ending"

        val months = doc.select(".govuk-summary-list__value ul.govuk-list li")

        months.eachText().toArray mustBe Array("February", "May", "August", "November")
      }

      "must render a change link to the choose return periods page" in {

        val link = standardDocument().select(".govuk-summary-list__actions a")

        link.size() mustBe 1
        link.attr("href") mustBe controllers.returnperiods.routes.ChooseReturnPeriodsController.onPageLoad(CheckMode).url
        link.text() mustBe "Change return periods"
        link.select(".govuk-visually-hidden").text() mustBe "return periods"
      }

      "must render an add link, and no change link, when no return periods have been set" in {

        val doc: Document = standardDocument(returnPeriods = None)

        val addLink = doc.select(".govuk-summary-list__value a.govuk-link")

        addLink.text() mustBe "Add return periods ending"
        addLink.attr("href") mustBe controllers.returnperiods.routes.ChooseReturnPeriodsController.onPageLoad(CheckMode).url

        doc.select(".govuk-summary-list__actions a").size() mustBe 0
      }

      "must show a Return periods row with Standard above Return periods ending when return periods were not set" in {

        val doc = standardDocument(showReturnPeriodsType = true)
        val rows = doc.select(".govuk-summary-list__row")

        rows.size() mustBe 2
        doc.select(".govuk-summary-list__key").eachText() mustBe java.util.List.of("Return periods", "Return periods ending")
        rows.first().select(".govuk-summary-list__value").text() mustBe "Standard"
      }

      "must link the Return periods row to the return periods page in CheckMode" in {

        val link = standardDocument(showReturnPeriodsType = true).select(".govuk-summary-list__row").first().select(".govuk-summary-list__actions a")

        link.size() mustBe 1
        link.attr("href") mustBe controllers.returnperiods.routes.ReturnPeriodsController.onPageLoad(CheckMode).url
        link.select(".govuk-visually-hidden").text() mustBe "return period type"
      }

      "must render the applies after current period message when it applies" in {

        standardDocument().select("p.govuk-body").text() must include("These changes will only apply after your current return period.")
      }

      "must not render the applies after current period message when it does not apply" in {

        standardDocument(showAppliesAfterCurrentPeriod = false).select("p.govuk-body").text() must not include
          "These changes will only apply after your current return period."
      }

      "must render a Continue button in a form that posts to the controller's onContinue action" in {

        val doc: Document = standardDocument()

        doc.select("form").attr("action") mustBe controllers.returnperiods.routes.CheckReturnPeriodsController.onContinue().url

        doc.select("button.govuk-button").text() mustBe "Continue"
      }
    }

    "non-standard version" - {

      "render the standard return periods choice" in {
        val choiceRow = nonStandardRows().head

        choiceRow.select(".govuk-summary-list__key").text() mustEqual
          msgs("checkNonStandardReturnPeriods.returnPeriods")

        choiceRow.select(".govuk-summary-list__value").text() mustEqual
          msgs("checkNonStandardReturnPeriods.standard")
      }

      "render the non-standard return periods choice" in {
        val rows = nonStandardRows(
          nonStandardViewModel(returnPeriodsChoice = Some(NoReturnPeriodsSet.Nonstandard))
        )

        rows.head.select(".govuk-summary-list__value").text() mustEqual
          msgs("checkNonStandardReturnPeriods.nonStandard")
      }

      "omit the return periods choice row when the choice is missing" in {
        val rows = nonStandardRows(
          nonStandardViewModel(returnPeriodsChoice = None)
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
        val monthsRow = nonStandardRows()(1)

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
        val rows = nonStandardRows(nonStandardViewModel(returnPeriodsId = 2))

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
        val rows = nonStandardRows(nonStandardViewModel(returnPeriodsId = 3))

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
        val rows = nonStandardRows(nonStandardViewModel(returnPeriodsId = 4))

        rows(1)
          .select(".govuk-summary-list__value li")
          .size() mustEqual 0
      }

      "render the return periods choice change link" in {
        val changeLink =
          nonStandardRows().head.select(".govuk-summary-list__actions a")

        changeLink.attr("href") mustEqual
          controllers.returnperiods.routes.ReturnPeriodsController
            .onPageLoad(CheckMode)
            .url

        changeLink.text() must include(msgs("site.change"))
      }

      "not render a return periods choice change link when the choice is missing" in {
        val choiceRows = nonStandardRows(
          nonStandardViewModel(returnPeriodsChoice = None)
        ).filter(
          _.select(".govuk-summary-list__key").text() ==
            msgs("checkNonStandardReturnPeriods.returnPeriods")
        )

        choiceRows mustBe empty
      }

      "render the selected months change link" in {
        val changeLink =
          nonStandardRows()(1).select(".govuk-summary-list__actions a")

        changeLink.attr("href") mustEqual
          controllers.returnperiods.routes.ChooseReturnPeriodsController
            .onPageLoad(CheckMode)
            .url
      }

      "render all eight entered period dates" in {
        val rows = nonStandardRows()

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
          nonStandardDocument().select("a.govuk-button")

        continueLink.size() mustEqual 1

        continueLink.attr("href") mustEqual
          controllers.routes.ChangeRegistrationDetailsController
            .onPageLoad()
            .url
      }

      "render the standard warning when the final period warning is false" in {
        val doc = nonStandardDocument(
          nonStandardViewModel(showFinalPeriodWarning = false)
        )

        doc.select("main").text() must include(
          msgs("checkNonStandardReturnPeriods.warning1")
        )
      }

      "render the final period warning when the final period warning is true" in {
        val doc = nonStandardDocument(
          nonStandardViewModel(showFinalPeriodWarning = true)
        )

        doc.select("main").text() must include(
          msgs("checkNonStandardReturnPeriods.warning2")
        )
      }

      "render the period-date change links in CheckMode" in {
        val periodRows = nonStandardRows().drop(2)

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
          nonStandardRows(nonStandardViewModel(dates = dates))

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
}
