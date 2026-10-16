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
import models.{CheckMode, NormalMode}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.api.test.Helpers.running
import viewmodels.checkAnswers.returnperiods.CheckNonStandardReturnPeriodsViewModel
import views.html.returnperiods.CheckNonStandardReturnPeriodsView

import java.time.LocalDate

class CheckNonStandardReturnPeriodsViewSpec extends SpecBase {

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

  private def createViewModel(
    returnPeriodsId: Int = 1,
    dates: Seq[Option[LocalDate]] = enteredDates
  )(implicit messages: Messages): CheckNonStandardReturnPeriodsViewModel =
    CheckNonStandardReturnPeriodsViewModel(
      returnPeriodsId      = returnPeriodsId,
      calculatedPeriodEnds = calculatedPeriodEnds,
      enteredDates         = dates
    )

  private def renderView(
    view: CheckNonStandardReturnPeriodsView,
    viewModel: CheckNonStandardReturnPeriodsViewModel
  )(implicit messages: Messages): Document = {
    val request = FakeRequest()

    val html =
      view(viewModel)(
        request,
        messages
      )

    Jsoup.parse(html.toString)
  }

  "CheckNonStandardReturnPeriodsView" - {

    "render the page correctly" in {

      val application =
        applicationBuilder().build()

      running(application) {

        implicit val msgs: Messages =
          messages(application)

        val view =
          application.injector
            .instanceOf[CheckNonStandardReturnPeriodsView]

        val viewModel =
          createViewModel()

        val document =
          renderView(view, viewModel)

        document.title() must include(
          msgs("checkNonStandardReturnPeriods.title")
        )

        document
          .select("h1")
          .text() mustEqual
          msgs("checkNonStandardReturnPeriods.heading")

        document.body().text() must include(
          msgs("changeRegistrationDetails.caption")
        )

        document.body().text() must include(
          msgs("checkNonStandardReturnPeriods.intro")
        )

        document.body().text() must include(
          msgs("checkNonStandardReturnPeriods.continueInfo")
        )

        document.body().text() must include(
          msgs("site.continue")
        )
      }
    }

    "render the selected return period months as a bullet list" in {

      val application =
        applicationBuilder().build()

      running(application) {

        implicit val msgs: Messages =
          messages(application)

        val view =
          application.injector
            .instanceOf[CheckNonStandardReturnPeriodsView]

        val viewModel =
          createViewModel(returnPeriodsId = 1)

        val document =
          renderView(view, viewModel)

        val selectedMonthsRow =
          document
            .select(".govuk-summary-list__row")
            .first()

        selectedMonthsRow
          .select(".govuk-summary-list__key")
          .text() mustEqual
          msgs("checkNonStandardReturnPeriods.returnPeriodsEnding")

        val monthItems =
          selectedMonthsRow
            .select(".govuk-summary-list__value li")

        monthItems.size() mustEqual 4

        monthItems.eachText() must contain theSameElementsInOrderAs Seq(
          msgs("months.january"),
          msgs("months.april"),
          msgs("months.july"),
          msgs("months.october")
        )

        selectedMonthsRow
          .select("ul")
          .hasClass("govuk-list") mustBe true

        selectedMonthsRow
          .select("ul")
          .hasClass("govuk-list--bullet") mustBe true
      }
    }

    "render the correct selected months for return periods ID 2" in {

      val application =
        applicationBuilder().build()

      running(application) {

        implicit val msgs: Messages =
          messages(application)

        val view =
          application.injector
            .instanceOf[CheckNonStandardReturnPeriodsView]

        val viewModel =
          createViewModel(returnPeriodsId = 2)

        val document =
          renderView(view, viewModel)

        val monthItems =
          document
            .select(".govuk-summary-list__row")
            .first()
            .select(".govuk-summary-list__value li")
            .eachText()

        monthItems must contain theSameElementsInOrderAs Seq(
          msgs("months.february"),
          msgs("months.may"),
          msgs("months.august"),
          msgs("months.november")
        )
      }
    }

    "render the correct selected months for return periods ID 3" in {

      val application =
        applicationBuilder().build()

      running(application) {

        implicit val msgs: Messages =
          messages(application)

        val view =
          application.injector
            .instanceOf[CheckNonStandardReturnPeriodsView]

        val viewModel =
          createViewModel(returnPeriodsId = 3)

        val document =
          renderView(view, viewModel)

        val monthItems =
          document
            .select(".govuk-summary-list__row")
            .first()
            .select(".govuk-summary-list__value li")
            .eachText()

        monthItems must contain theSameElementsInOrderAs Seq(
          msgs("months.march"),
          msgs("months.june"),
          msgs("months.september"),
          msgs("months.december")
        )
      }
    }

    "render the return periods change link in CheckMode" in {

      val application =
        applicationBuilder().build()

      running(application) {

        implicit val msgs: Messages =
          messages(application)

        val view =
          application.injector
            .instanceOf[CheckNonStandardReturnPeriodsView]

        val viewModel =
          createViewModel()

        val document =
          renderView(view, viewModel)

        val selectedMonthsRow =
          document
            .select(".govuk-summary-list__row")
            .first()

        val changeLink =
          selectedMonthsRow
            .select(".govuk-summary-list__actions a")

        changeLink.attr("href") mustEqual
          controllers.returnperiods.routes.ChooseReturnPeriodsController
            .onPageLoad(CheckMode)
            .url

        changeLink.text() must include(
          msgs("site.change")
        )

        changeLink.text() must include(
          msgs(
            "checkNonStandardReturnPeriods.returnPeriodsEnding.hidden"
          )
        )
      }
    }

    "render all 8 entered period dates" in {

      val application =
        applicationBuilder().build()

      running(application) {

        implicit val msgs: Messages =
          messages(application)

        val view =
          application.injector
            .instanceOf[CheckNonStandardReturnPeriodsView]

        val viewModel =
          createViewModel()

        val document =
          renderView(view, viewModel)

        val summaryRows =
          document.select(".govuk-summary-list__row")

        summaryRows.size() mustEqual 9

        val bodyText =
          document.body().text()

        bodyText must include("30 Jan 2026")
        bodyText must include("29 Apr 2026")
        bodyText must include("30 Jul 2026")
        bodyText must include("30 Oct 2026")
        bodyText must include("30 Jan 2027")
        bodyText must include("29 Apr 2027")
        bodyText must include("30 Jul 2027")
        bodyText must include("30 Oct 2027")
      }
    }

    "render the period labels for all 8 periods" in {

      val application =
        applicationBuilder().build()

      running(application) {

        implicit val msgs: Messages =
          messages(application)

        val view =
          application.injector
            .instanceOf[CheckNonStandardReturnPeriodsView]

        val viewModel =
          createViewModel()

        val document =
          renderView(view, viewModel)

        val keys =
          document
            .select(".govuk-summary-list__key")
            .eachText()

        keys must contain(
          msgs(
            "checkNonStandardReturnPeriods.periodLabel",
            "Jan 2026"
          )
        )

        keys must contain(
          msgs(
            "checkNonStandardReturnPeriods.periodLabel",
            "Apr 2026"
          )
        )

        keys must contain(
          msgs(
            "checkNonStandardReturnPeriods.periodLabel",
            "Jul 2026"
          )
        )

        keys must contain(
          msgs(
            "checkNonStandardReturnPeriods.periodLabel",
            "Oct 2026"
          )
        )

        keys must contain(
          msgs(
            "checkNonStandardReturnPeriods.periodLabel",
            "Jan 2027"
          )
        )

        keys must contain(
          msgs(
            "checkNonStandardReturnPeriods.periodLabel",
            "Apr 2027"
          )
        )

        keys must contain(
          msgs(
            "checkNonStandardReturnPeriods.periodLabel",
            "Jul 2027"
          )
        )

        keys must contain(
          msgs(
            "checkNonStandardReturnPeriods.periodLabel",
            "Oct 2027"
          )
        )
      }
    }

    "render CheckMode change links for all 8 entered dates" in {

      val application =
        applicationBuilder().build()

      running(application) {

        implicit val msgs: Messages =
          messages(application)

        val view =
          application.injector
            .instanceOf[CheckNonStandardReturnPeriodsView]

        val viewModel =
          createViewModel()

        val document =
          renderView(view, viewModel)

        (1 to 8).foreach { periodNumber =>
          val expectedUrl =
            controllers.returnperiods.routes.EnterNonStandardPeriodDateController
              .onPageLoad(periodNumber, CheckMode)
              .url

          document
            .select(s"""a[href="$expectedUrl"]""")
            .size() mustEqual 1
        }
      }
    }

    "render a NormalMode add link when an entered date is missing" in {

      val application =
        applicationBuilder().build()

      running(application) {

        implicit val msgs: Messages =
          messages(application)

        val view =
          application.injector
            .instanceOf[CheckNonStandardReturnPeriodsView]

        val datesWithThirdDateMissing =
          enteredDates.updated(2, None)

        val viewModel =
          createViewModel(
            dates = datesWithThirdDateMissing
          )

        val document =
          renderView(view, viewModel)

        val expectedUrl =
          controllers.returnperiods.routes.EnterNonStandardPeriodDateController
            .onPageLoad(3, NormalMode)
            .url

        val addLink =
          document.select(s"""a[href="$expectedUrl"]""")

        addLink.size() mustEqual 1

        addLink.text() mustEqual
          msgs(
            "checkNonStandardReturnPeriods.addEndDate",
            "Jul 2026"
          )
      }
    }

    "render all missing dates with NormalMode add links" in {

      val application =
        applicationBuilder().build()

      running(application) {

        implicit val msgs: Messages =
          messages(application)

        val view =
          application.injector
            .instanceOf[CheckNonStandardReturnPeriodsView]

        val viewModel =
          createViewModel(
            dates = Seq.fill(8)(None)
          )

        val document =
          renderView(view, viewModel)

        (1 to 8).foreach { periodNumber =>
          val expectedUrl =
            controllers.returnperiods.routes.EnterNonStandardPeriodDateController
              .onPageLoad(periodNumber, NormalMode)
              .url

          document
            .select(s"""a[href="$expectedUrl"]""")
            .size() mustEqual 1
        }

        document
          .select(
            ".govuk-summary-list__row--no-actions"
          )
          .size() mustEqual 8
      }
    }

    "render both entered dates and missing date links" in {

      val application =
        applicationBuilder().build()

      running(application) {

        implicit val msgs: Messages =
          messages(application)

        val view =
          application.injector
            .instanceOf[CheckNonStandardReturnPeriodsView]

        val mixedDates =
          Seq(
            Some(LocalDate.of(2026, 1, 30)),
            None,
            Some(LocalDate.of(2026, 7, 30)),
            None,
            Some(LocalDate.of(2027, 1, 30)),
            None,
            Some(LocalDate.of(2027, 7, 30)),
            None
          )

        val viewModel =
          createViewModel(
            dates = mixedDates
          )

        val document =
          renderView(view, viewModel)

        Seq(1, 3, 5, 7).foreach { periodNumber =>
          val expectedUrl =
            controllers.returnperiods.routes.EnterNonStandardPeriodDateController
              .onPageLoad(periodNumber, CheckMode)
              .url

          document
            .select(s"""a[href="$expectedUrl"]""")
            .size() mustEqual 1
        }

        Seq(2, 4, 6, 8).foreach { periodNumber =>
          val expectedUrl =
            controllers.returnperiods.routes.EnterNonStandardPeriodDateController
              .onPageLoad(periodNumber, NormalMode)
              .url

          document
            .select(s"""a[href="$expectedUrl"]""")
            .size() mustEqual 1
        }

        document
          .select(
            ".govuk-summary-list__row--no-actions"
          )
          .size() mustEqual 4
      }
    }

    "render the continue link to Change Registration Details" in {

      val application =
        applicationBuilder().build()

      running(application) {

        implicit val msgs: Messages =
          messages(application)

        val view =
          application.injector
            .instanceOf[CheckNonStandardReturnPeriodsView]

        val viewModel =
          createViewModel()

        val document =
          renderView(view, viewModel)

        val continueLink =
          document.select("a.govuk-button")

        continueLink.size() mustEqual 1

        continueLink.text() mustEqual
          msgs("site.continue")

        continueLink.attr("href") mustEqual
          controllers.routes.ChangeRegistrationDetailsController
            .onPageLoad()
            .url
      }
    }

    "not render a form because Continue is a link" in {

      val application =
        applicationBuilder().build()

      running(application) {

        implicit val msgs: Messages =
          messages(application)

        val view =
          application.injector
            .instanceOf[CheckNonStandardReturnPeriodsView]

        val viewModel =
          createViewModel()

        val document =
          renderView(view, viewModel)

        document.select("form").size() mustEqual 0
        document.select("button.govuk-button").size() mustEqual 0
        document.select("a.govuk-button").size() mustEqual 1
      }
    }
  }
}
