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

package viewmodels.checkAnswers

import base.SpecBase
import controllers.routes
import models.LicencesPremises
import pages.LicencesPremisesPage
import play.api.Application
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

class LicencesPremisesSummarySpec extends SpecBase {

  private lazy val app: Application = applicationBuilder().build()
  private implicit lazy val messages: Messages = this.messages(app)

  "LicencesPremisesSummary" - {

    "must return None when the question has not been answered" in {
      LicencesPremisesSummary.row(emptyUserAnswers) mustBe None
    }

    LicencesPremises.values.foreach { answer =>
      s"must render ${answer.toString} with a working Change link" in {
        val answers = emptyUserAnswers.set(LicencesPremisesPage, answer).success.value

        LicencesPremisesSummary.row(answers).value mustBe
          SummaryListRowViewModel(
            key = "licencesPremises.checkYourAnswersLabel",
            value = ValueViewModel(
              HtmlContent(HtmlFormat.escape(messages(s"licencesPremises.$answer")))
            ),
            actions = Seq(
              ActionItemViewModel(
                "site.change",
                routes.LicencesPremisesController.onPageLoad().url
              ).withVisuallyHiddenText(messages("licencesPremises.change.hidden"))
            )
          )
      }
    }
  }
}
