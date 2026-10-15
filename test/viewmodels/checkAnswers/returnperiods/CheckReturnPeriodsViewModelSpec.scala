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
import models.ChooseReturnPeriods.{Feb, Jan, Mar}
import models.{CheckMode, GamblingReturnPeriods, UserAnswers}
import pages.returnperiods.{ChooseReturnPeriodsPage, GamblingReturnPeriodsPage, HasExistingNstpValuesPage}
import play.api.Application
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{HtmlContent, Text}

class CheckReturnPeriodsViewModelSpec extends SpecBase {

  private val app: Application = applicationBuilder().build()

  implicit val msgs: Messages = messages(app)

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
  }

  "CheckReturnPeriodsViewModel.summaryList" - {

    "must have a single Return periods ending row" in {
      val rows = CheckReturnPeriodsViewModel(Some(Jan), showAppliesAfterCurrentPeriod = true).summaryList.rows

      rows.size mustBe 1
      rows.head.key.content mustBe Text("Return periods ending")
    }

    "must list the four months for each set of return periods as a bulleted list" in {
      def months(viewModel: CheckReturnPeriodsViewModel) = viewModel.summaryList.rows.head.value.content

      def bulleted(months: String*) =
        HtmlContent(months.map(month => s"<li>$month</li>").mkString("""<ul class="govuk-list govuk-list--bullet">""", "", "</ul>"))

      months(CheckReturnPeriodsViewModel(Some(Jan), showAppliesAfterCurrentPeriod = true)) mustBe bulleted("January", "April", "July", "October")
      months(CheckReturnPeriodsViewModel(Some(Feb), showAppliesAfterCurrentPeriod = true)) mustBe bulleted("February", "May", "August", "November")
      months(CheckReturnPeriodsViewModel(Some(Mar), showAppliesAfterCurrentPeriod = true)) mustBe bulleted("March", "June", "September", "December")
    }

    "must have a change link to the choose return periods page in CheckMode" in {
      val actions = CheckReturnPeriodsViewModel(Some(Jan), showAppliesAfterCurrentPeriod = true).summaryList.rows.head.actions.value.items

      actions.size mustBe 1
      actions.head.href mustBe controllers.returnperiods.routes.ChooseReturnPeriodsController.onPageLoad(CheckMode).url
      actions.head.content mustBe Text("Change")
      actions.head.visuallyHiddenText mustBe Some("return periods")
    }

    "must show an add link with no change action when no return periods have been set" in {
      val row = CheckReturnPeriodsViewModel(None, showAppliesAfterCurrentPeriod = true).summaryList.rows.head

      val addUrl = controllers.returnperiods.routes.ChooseReturnPeriodsController.onPageLoad(CheckMode).url

      row.key.content mustBe Text("Return periods ending")
      row.value.content mustBe HtmlContent(s"""<a class="govuk-link" href="$addUrl">Add return periods ending</a>""")
      row.actions mustBe None
    }
  }
}
