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
import forms.returnperiods.NonStandardReturnPeriodsFormProvider
import models.{GamblingReturnPeriods, NormalMode}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.must.Matchers.*
import play.api.i18n.Messages
import play.api.test.FakeRequest
import viewmodels.checkAnswers.returnperiods.NonStandardReturnPeriodsViewModel
import views.html.returnperiods.NonStandardReturnPeriodsView

import java.time.LocalDate
import scala.jdk.CollectionConverters.*

class NonStandardReturnPeriodsViewSpec extends SpecBase {

  trait Setup {

    private val app = applicationBuilder().build()

    private val view =
      app.injector.instanceOf[NonStandardReturnPeriodsView]

    implicit private val request: play.api.mvc.Request[?] = FakeRequest()

    implicit val messages: Messages =
      app.injector.instanceOf[play.api.i18n.MessagesApi].preferred(request)

    private val formProvider = new NonStandardReturnPeriodsFormProvider()
    private val form = formProvider()

    private val date = LocalDate.now()
    private val gamblingReturnPeriods = GamblingReturnPeriods(
      mgdRegNumber = "mgd1",
      returnPeriodsId = Some(1),
      nstpEndDate1 = Some(date.minusMonths(9)),
      nstpEndDate2 = Some(date.minusMonths(6)),
      nstpEndDate3 = Some(date.minusMonths(3)),
      nstpEndDate4 = Some(date),
      nstpEndDate5 = Some(date.plusMonths(3)),
      nstpEndDate6 = Some(date.plusMonths(6)),
      nstpEndDate7 = Some(date.plusMonths(9)),
      nstpEndDate8 = Some(date.plusMonths(12)),
      isInLastNstp = Some(true),
      finalPeriodWarning = Some(false),
      hasExistingNstpValues = Some(true)
    )
    private val viewModel = NonStandardReturnPeriodsViewModel.from(gamblingReturnPeriods).get

    private val html = view(form, viewModel, NormalMode)(request, messages)

    val doc: Document = Jsoup.parse(html.body)

    def renderWithErrors: Document = {
      val formWithErrors = formProvider().bind(Map("value" -> ""))
      Jsoup.parse(view(formWithErrors, viewModel, NormalMode)(request, messages).body)
    }
  }

  "NonStandardReturnPeriodsViewSpec" - {

    "must render non-standard page correctly" in new Setup {

      doc.title must include(
        messages("returnPeriods.nonStandard.title")
      )

      doc.select(".govuk-caption-l").text() mustBe
        messages("changeRegistrationDetails.caption")

      doc.select("h1.govuk-heading-l").text() mustBe
        messages("returnPeriods.nonStandard.heading")

      doc.select(".govuk-fieldset__legend").text() mustBe
        messages("returnPeriods.nonStandard.option.legend")

      doc.select(".govuk-table__caption").get(0).text() mustBe messages("returnPeriods.nonStandard.upcomingTable.caption")
      doc.select(".govuk-table__caption").get(1).text() mustBe messages("returnPeriods.nonStandard.pastTable.caption")

      val tables = doc.select("table.govuk-table").asScala

      val pastTable = tables
        .find(_.select("caption").text().trim == messages("returnPeriods.nonStandard.upcomingTable.caption"))

      val upcomingTable = tables
        .find(_.select("caption").text().trim == messages("returnPeriods.nonStandard.pastTable.caption"))

      assert(pastTable.isDefined, "Non-standard return periods table should exist")
      assert(upcomingTable.isDefined, "Standard return periods table should exist")

      assert(pastTable.get.select("tbody tr").size() > 0,
        "Non-standard table should contain rows")

      assert(upcomingTable.get.select("tbody tr").size() > 0,
        "Standard table should contain rows")

      doc.select("button.govuk-button").text() mustBe
        messages("site.continue")
    }

    "must render the radio options" in new Setup {

      val radioOptions = doc.select(".govuk-radios__item label").eachText()

      radioOptions must contain(
        messages("returnPeriods.nonStandard.switchToStandard")
      )

      radioOptions must contain(
        messages("returnPeriods.nonStandard.keepNonStandardAndChangeDates")
      )

      radioOptions must contain(
        messages("returnPeriods.nonStandard.keepNonStandard")
      )

      doc.select("input[type=radio]").size mustBe 3

      doc.select("#value_0-item-hint").text() mustBe
        messages("returnPeriods.nonStandard.switchToStandard.hint", "January", "April", "July", "October")
    }

    "must render an error summary when the form has errors" in new Setup {

      renderWithErrors
        .select(".govuk-error-summary")
        .size mustBe 1
    }
  }
}
