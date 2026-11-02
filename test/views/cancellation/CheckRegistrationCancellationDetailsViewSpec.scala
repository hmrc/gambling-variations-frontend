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

package views.cancellation

import base.SpecBase
import controllers.cancellation.routes
import models.cancellation.ReasonForCancellation
import pages.cancellation.{CancelRegistrationDatePage, ChangeGroupRegistrationNumberPage, ReasonForCancellationPage}
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.{AnyContent, Request}
import play.api.test.FakeRequest
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.checkAnswers.cancellation.CheckRegistrationCancellationDetailsViewModel
import views.html.cancellation.CheckRegistrationCancellationDetailsView

import java.time.LocalDate

class CheckRegistrationCancellationDetailsViewSpec extends SpecBase {

  private val application =
    applicationBuilder().build()

  private implicit val request: Request[AnyContent] =
    FakeRequest()

  private implicit val messages: Messages =
    application.injector
      .instanceOf[MessagesApi]
      .preferred(request)

  private val view =
    application.injector
      .instanceOf[CheckRegistrationCancellationDetailsView]

  private val userAnswers =
    emptyUserAnswers
      .set(
        CancelRegistrationDatePage,
        LocalDate.of(2026, 2, 2)
      )
      .success
      .value
      .set(
        ReasonForCancellationPage,
        ReasonForCancellation.BusinessIsPartOfTheGroup
      )
      .success
      .value
      .set(
        ChangeGroupRegistrationNumberPage,
        "XWM000000001770"
      )
      .success
      .value

  private val summaryList: SummaryList =
    CheckRegistrationCancellationDetailsViewModel
      .summaryList(userAnswers)

  private val document =
    org.jsoup.Jsoup.parse(
      view(summaryList).toString
    )

  "CheckRegistrationCancellationDetailsView" - {

    "must display the correct page title" in {

      document
        .title() must include(
        messages("checkRegistrationCancellationDetails.title")
      )
    }

    "must display the correct caption" in {

      document
        .select(".govuk-caption-l")
        .text() mustBe
        messages("checkRegistrationCancellationDetails.caption")
    }

    "must display the correct heading" in {

      document
        .select("h1")
        .text() mustBe
        messages("checkRegistrationCancellationDetails.heading")
    }

    "must display the cancellation date" in {

      document
        .text() must include("2 February 2026")
    }

    "must display the cancellation reason" in {

      document
        .text() must include(
        messages("reasonForCancellation.businessIsPartOfTheGroup")
      )
    }

    "must display the group registration number" in {

      document
        .text() must include("XWM000000001770")
    }

    "must display the Continue button" in {

      document
        .select(".govuk-button")
        .text() mustBe
        messages("site.continue")
    }

    "must link Continue to the cancellation declaration page" in {

      document
        .select(".govuk-button")
        .attr("href") mustBe
        routes.RegistrationCancellationDeclarationController
          .onPageLoad()
          .url
    }
  }
}
