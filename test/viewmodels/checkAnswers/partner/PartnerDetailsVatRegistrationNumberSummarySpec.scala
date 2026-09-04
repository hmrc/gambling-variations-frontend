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

package viewmodels.checkAnswers.partner

import base.SpecBase
import pages.partnerdetails.PartnerDetailsVrnPage
import play.api.Application
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

class PartnerDetailsVatRegistrationNumberSummarySpec extends SpecBase {
  // TODO: Interim solution - will be refactored with the indexing ticket
  private val index: Int = 0

  private val vrn: String = "GB353868127"

  lazy val app: Application = applicationBuilder().build()

  implicit val messages: Messages = this.messages(app)

  "PartnerDetailsVatRegistrationNumberSummary" - {

    "must return None when the question has not been answered" in {
      PartnerDetailsVatRegistrationNumberSummary.row(emptyUserAnswers) mustBe None
    }

    "must return the correct row when the VAT registration number has been answered" in {
      val answers =
        emptyUserAnswers
          .set(PartnerDetailsVrnPage(index), vrn)
          .success
          .value

      PartnerDetailsVatRegistrationNumberSummary.row(answers).value mustBe
        SummaryListRowViewModel(
          key   = "partnerDetailsVatRegistrationNumber.checkYourAnswersLabel",
          value = ValueViewModel(HtmlFormat.escape(vrn).toString),
          actions = Seq(
            ActionItemViewModel(
              "site.change",
              controllers.partner.routes.PartnerDetailsVatRegistrationNumberController.onPageLoad().url
            ).withVisuallyHiddenText(
              messages("partnerDetailsVatRegistrationNumber.change.hidden")
            )
          )
        )
    }
  }
}
