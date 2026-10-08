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
import forms.returnperiods.StandardReturnPeriodsFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.must.Matchers.*
import play.api.i18n.Messages
import play.api.test.FakeRequest
import views.html.returnperiods.StandardReturnPeriodsView

class StandardReturnPeriodsOptionsViewSpec extends SpecBase {

  trait Setup {

    private val app = applicationBuilder().build()

    private val view =
      app.injector.instanceOf[StandardReturnPeriodsView]

    implicit private val request: play.api.mvc.Request[?] = FakeRequest()

    implicit val messages: Messages =
      app.injector.instanceOf[play.api.i18n.MessagesApi].preferred(request)

    private val formProvider = new StandardReturnPeriodsFormProvider()
    private val form = formProvider()

    private val returnPeriodsId = Some("3")

    private val html = view(form, NormalMode, returnPeriodsId)(request, messages)

    val doc: Document = Jsoup.parse(html.body)

    def renderWithErrors: Document = {
      val formWithErrors = formProvider().bind(Map("value" -> ""))
      Jsoup.parse(view(formWithErrors, NormalMode, returnPeriodsId)(request, messages).body)
    }
  }

  "WhatToDoWithStandardReturnPeriodsView" - {

    "must render page correctly" in new Setup {

      doc.title must include(
        messages("returnPeriods.standard.title")
      )

      doc.select(".govuk-caption-l").text() mustBe
        messages("changeRegistrationDetails.caption")

      doc.select("h1.govuk-heading-l").text() mustBe
        messages("returnPeriods.standard.heading")

      doc.select(".govuk-fieldset__legend").text() mustBe
        messages("returnPeriods.standard.option.legend")

      doc.select("p.govuk-body").text() mustBe
        messages("returnPeriods.standard.p3")

      doc.select("button.govuk-button").text() mustBe
        messages("site.continue")
    }

    "must render the radio options" in new Setup {

      val radioOptions = doc.select(".govuk-radios__item label").eachText()

      radioOptions must contain(
        messages("returnPeriods.standard.changeMonthsStandardPeriodCover")
      )

      radioOptions must contain(
        messages("returnPeriods.standard.switchToNonStandard")
      )

      radioOptions must contain(
        messages("returnPeriods.standard.keepStandardReturnPeriod")
      )

      doc.select("input[type=radio]").size mustBe 3

      doc.select("#value_1-item-hint").text() mustBe
        messages("returnPeriods.standard.switchToNonStandard.hint")
    }

    "must render an error summary when the form has errors" in new Setup {

      renderWithErrors
        .select(".govuk-error-summary")
        .size mustBe 1
    }
  }
}
