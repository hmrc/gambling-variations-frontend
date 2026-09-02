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
import controllers.partner.routes.PartnerDetailsAddNationalInsuranceNumberYesNoController
import pages.partner.PartnerDetailsAddNationalInsuranceNumberYesNoPage
import play.api.Application
import play.api.i18n.Messages
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

class PartnerDetailsAddNationalInsuranceNumberYesNoSummarySpec extends SpecBase {

  // TODO: Interim solution - will be refactored with the indexing ticket
  private val index: Int = 0

  lazy val app: Application = applicationBuilder().build()

  implicit val messages: Messages = this.messages(app)

  "PartnerDetailsAddNationalInsuranceNumberYesNoSummary" - {

    "must return None when the question has not been answered" in {
      PartnerDetailsAddNationalInsuranceNumberYesNoSummary.row(emptyUserAnswers) mustBe None
    }

    "must return the correct row when the answer is Yes" in {
      val answers =
        emptyUserAnswers
          .set(PartnerDetailsAddNationalInsuranceNumberYesNoPage(index), true)
          .success
          .value

      PartnerDetailsAddNationalInsuranceNumberYesNoSummary.row(answers).value mustBe
        SummaryListRowViewModel(
          key   = "partnerDetailsAddNinoYesNo.checkYourAnswersLabel",
          value = ValueViewModel("site.yes"),
          actions = Seq(
            ActionItemViewModel(
              "site.change",
              PartnerDetailsAddNationalInsuranceNumberYesNoController.onPageLoad().url
            ).withVisuallyHiddenText(
              messages("partnerDetailsAddNinoYesNo.change.hidden")
            )
          )
        )
    }

    "must return the correct row when the answer is No" in {
      val answers =
        emptyUserAnswers
          .set(PartnerDetailsAddNationalInsuranceNumberYesNoPage(index), false)
          .success
          .value

      PartnerDetailsAddNationalInsuranceNumberYesNoSummary.row(answers).value mustBe
        SummaryListRowViewModel(
          key   = "partnerDetailsAddNinoYesNo.checkYourAnswersLabel",
          value = ValueViewModel("site.no"),
          actions = Seq(
            ActionItemViewModel(
              "site.change",
              PartnerDetailsAddNationalInsuranceNumberYesNoController.onPageLoad().url
            ).withVisuallyHiddenText(
              messages("partnerDetailsAddNinoYesNo.change.hidden")
            )
          )
        )
    }
  }
}
