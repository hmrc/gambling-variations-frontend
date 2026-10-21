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
import forms.returnperiods.NonStandardNotSubmittedReturnPeriodsFormProvider
import models.{GamblingReturnPeriods, NonStandardNotSubmittedReturnPeriodsOptions, NormalMode}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.must.Matchers.*
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.Request
import play.api.test.FakeRequest
import viewmodels.checkAnswers.returnperiods.NonStandardNotSubmittedReturnPeriodsViewModel
import views.html.returnperiods.NonStandardNotSubmittedReturnPeriodsView

import java.time.LocalDate
import scala.jdk.CollectionConverters.*

class NonStandardNotSubmittedReturnPeriodsViewSpec extends SpecBase {

  private val today = LocalDate.now()

  private val periodsWithFutureDates = GamblingReturnPeriods(
    mgdRegNumber          = "mgd1",
    returnPeriodsId       = Some(1),
    nstpEndDate1          = Some(today.minusMonths(9)),
    nstpEndDate2          = Some(today.minusMonths(6)),
    nstpEndDate3          = Some(today.minusMonths(3)),
    nstpEndDate4          = Some(today.plusMonths(1)),
    nstpEndDate5          = Some(today.plusMonths(3)),
    nstpEndDate6          = Some(today.plusMonths(6)),
    nstpEndDate7          = Some(today.plusMonths(9)),
    nstpEndDate8          = Some(today.plusMonths(12)),
    isInLastNstp          = Some(false),
    finalPeriodWarning    = Some(false),
    hasExistingNstpValues = Some(true)
  )

  private val periodsAllInPast = periodsWithFutureDates.copy(
    nstpEndDate4 = Some(today.minusMonths(2)),
    nstpEndDate5 = None,
    nstpEndDate6 = None,
    nstpEndDate7 = None,
    nstpEndDate8 = None
  )

  trait Setup(periods: GamblingReturnPeriods = periodsWithFutureDates) {

    private val app = applicationBuilder().build()

    private val view = app.injector.instanceOf[NonStandardNotSubmittedReturnPeriodsView]

    implicit val request: Request[?] = FakeRequest()

    implicit val messages: Messages = app.injector.instanceOf[MessagesApi].preferred(request)

    private val formProvider = new NonStandardNotSubmittedReturnPeriodsFormProvider()

    val viewModel: NonStandardNotSubmittedReturnPeriodsViewModel =
      NonStandardNotSubmittedReturnPeriodsViewModel.from(periods).get

    val doc: Document = Jsoup.parse(view(formProvider(), viewModel, NormalMode).body)

    def renderWithErrors: Document =
      Jsoup.parse(view(formProvider().bind(Map("value" -> "")), viewModel, NormalMode).body)
  }

  "NonStandardNotSubmittedReturnPeriodsView" - {

    "must render the page title, caption and heading" in new Setup() {

      doc.title must include(messages("returnPeriods.nonStandard.title"))
      doc.select(".govuk-caption-l").text() mustBe messages("changeRegistrationDetails.caption")
      doc.select("h1").text() mustBe messages("returnPeriods.nonStandard.title")
    }

    "must post to the return periods submit route" in new Setup() {

      doc.select("form").attr("action") mustBe
        controllers.returnperiods.routes.ReturnPeriodsController.onSubmit(NormalMode).url
    }

    "must render the upcoming periods table when there are future periods" in new Setup() {

      val tables = doc.select("table.govuk-table").asScala

      tables.size mustBe 1
      tables.head.select("caption").text() mustBe messages("returnPeriods.nonStandard.upcomingTable.caption")

      val headers = tables.head.select("thead th").eachText().asScala.toSeq
      headers mustBe Seq(messages("returnPeriods.nonStandard.column1"), messages("returnPeriods.nonStandard.column2"))

      tables.head.select("tbody tr").size mustBe viewModel.futurePeriods.size
    }

    "must not render a past periods table" in new Setup() {

      doc.select(".govuk-table__caption").eachText() must not contain
        messages("returnPeriods.nonStandard.pastTable.caption")
    }

    "must not render a table when there are no future periods" in new Setup(periodsAllInPast) {

      viewModel.futurePeriods mustBe empty
      doc.select("table.govuk-table").size mustBe 0
    }

    "must render the radio legend and one option per answer" in new Setup() {

      doc.select(".govuk-fieldset__legend").text() mustBe
        messages("returnPeriods.nonStandardNotSub.option.legend")

      val expectedLabels: Seq[String] =
        NonStandardNotSubmittedReturnPeriodsOptions.options
          .filter(_.divider.isEmpty)
          .map(item => Jsoup.parse(item.content.asHtml.body).text())

      doc.select(".govuk-radios__item label").eachText().asScala.toSeq mustBe expectedLabels
      doc.select("input[type=radio]").size mustBe expectedLabels.size
    }

    "must render the divider between the options" in new Setup() {

      val dividers = NonStandardNotSubmittedReturnPeriodsOptions.options.flatMap(_.divider)

      doc.select(".govuk-radios__divider").eachText().asScala.toSeq mustBe dividers
    }

    "must render the locked paragraph" in new Setup() {

      doc.select("p.govuk-body").eachText() must contain(messages("returnPeriods.locked.p2"))
    }

    "must render the continue button" in new Setup() {

      doc.select("button.govuk-button").text() mustBe messages("site.continue")
    }

    "must render an error summary linking to the first radio when the form has errors" in new Setup() {

      val errorDoc = renderWithErrors

      errorDoc.select(".govuk-error-summary").size mustBe 1
      errorDoc.select(".govuk-error-summary a").attr("href") mustBe "#value_0"
      errorDoc.title must startWith(messages("error.title.prefix"))
    }

    "must not render an error summary when the form has no errors" in new Setup() {

      doc.select(".govuk-error-summary").size mustBe 0
    }
  }
}
