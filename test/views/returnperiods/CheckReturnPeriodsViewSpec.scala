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
import models.{CheckMode, ChooseReturnPeriods}
import models.ChooseReturnPeriods.Feb
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.must.Matchers.*
import play.api.i18n.Messages
import play.api.test.FakeRequest
import viewmodels.checkAnswers.returnperiods.CheckReturnPeriodsViewModel
import views.html.returnperiods.CheckReturnPeriodsView

class CheckReturnPeriodsViewSpec extends SpecBase {

  trait Setup {

    private val app = applicationBuilder().build()

    private val view =
      app.injector.instanceOf[CheckReturnPeriodsView]

    implicit private val request: play.api.mvc.Request[?] = FakeRequest()

    implicit val messages: Messages =
      app.injector.instanceOf[play.api.i18n.MessagesApi].preferred(request)

    def render(returnPeriods: Option[ChooseReturnPeriods] = Some(Feb), showAppliesAfterCurrentPeriod: Boolean = true): Document =
      Jsoup.parse(view(CheckReturnPeriodsViewModel(returnPeriods, showAppliesAfterCurrentPeriod))(request, messages).body)
  }

  "CheckReturnPeriodsView" - {

    "must render the title, caption and heading for standard return periods" in new Setup {

      val doc: Document = render()

      doc.title must startWith("Check your standard return periods - Change registration details")

      doc.select(".govuk-caption-l").text() mustBe "Change registration details"

      doc.select("h1.govuk-heading-l").text() mustBe "Check your standard return periods"
    }

    "must render the Return periods ending row with the four months" in new Setup {

      val doc: Document = render()

      doc.select(".govuk-summary-list__row").size() mustBe 1

      doc.select(".govuk-summary-list__key").text() mustBe "Return periods ending"

      val months = doc.select(".govuk-summary-list__value ul.govuk-list li")

      months.eachText().toArray mustBe Array("February", "May", "August", "November")
    }

    "must render a change link to the choose return periods page" in new Setup {

      val link = render().select(".govuk-summary-list__actions a")

      link.size() mustBe 1
      link.attr("href") mustBe controllers.returnperiods.routes.ChooseReturnPeriodsController.onPageLoad(CheckMode).url
      link.text() mustBe "Change return periods"
      link.select(".govuk-visually-hidden").text() mustBe "return periods"
    }

    "must render an add link, and no change link, when no return periods have been set" in new Setup {

      val doc: Document = render(returnPeriods = None)

      val addLink = doc.select(".govuk-summary-list__value a.govuk-link")

      addLink.text() mustBe "Add return periods ending"
      addLink.attr("href") mustBe controllers.returnperiods.routes.ChooseReturnPeriodsController.onPageLoad(CheckMode).url

      doc.select(".govuk-summary-list__actions a").size() mustBe 0
    }

    "must render the applies after current period message when it applies" in new Setup {

      render().select("p.govuk-body").text() must include("These changes will only apply after your current return period.")
    }

    "must not render the applies after current period message when it does not apply" in new Setup {

      render(showAppliesAfterCurrentPeriod = false).select("p.govuk-body").text() must not include
        "These changes will only apply after your current return period."
    }

    "must tell the user to continue to the change registration details page" in new Setup {

      render().select("p.govuk-body").text() must include(
        "You must continue to the change registration details page to submit any changes."
      )
    }

    "must render a Continue button that posts to the controller's onContinue action" in new Setup {

      val doc: Document = render()

      doc.select("form").attr("action") mustBe controllers.returnperiods.routes.CheckReturnPeriodsController.onContinue().url

      doc.select("button.govuk-button").text() mustBe "Continue"
    }
  }
}
