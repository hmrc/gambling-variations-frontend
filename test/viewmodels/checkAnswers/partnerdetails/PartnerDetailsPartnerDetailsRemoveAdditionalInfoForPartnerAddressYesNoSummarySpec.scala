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

package viewmodels.checkAnswers.partnerdetails

import base.SpecBase
import pages.partnerdetails.PartnerDetailsRemoveAdditionalInfoForPartnerAddressYesNoPage
import play.api.Application
import play.api.i18n.Messages
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

class PartnerDetailsPartnerDetailsRemoveAdditionalInfoForPartnerAddressYesNoSummarySpec extends SpecBase {

  lazy val app: Application = applicationBuilder().build()
  val businessNumber = "12345" // TODO investigate

  implicit val messages: Messages = this.messages(app)

  "RemoveAdditionalInfoForPartnerAddressYesNoSummary" - {

    "must return None when the question has not been answered" in {
      PartnerDetailsRemoveAdditionalInfoForPartnerAddressYesNoSummary.row(emptyUserAnswers) mustBe None
    }

    "must return the correct row when the answer is Yes" in {
      val answers =
        emptyUserAnswers
          .set(PartnerDetailsRemoveAdditionalInfoForPartnerAddressYesNoPage(businessNumber), true)
          .success
          .value

      PartnerDetailsRemoveAdditionalInfoForPartnerAddressYesNoSummary.row(answers).value mustBe
        SummaryListRowViewModel(
          key   = "removeAdditionalInfoForPartnerAddressYesNo.checkYourAnswersLabel",
          value = ValueViewModel("site.yes"),
          actions = Seq(
            ActionItemViewModel(
              "site.change",
              controllers.partnerdetails.routes.RemoveAdditionalInfoForPartnerAddressYesNoController
                .onPageLoad()
                .url
            ).withVisuallyHiddenText(
              messages("removeAdditionalInfoForPartnerAddressYesNo.change.hidden")
            )
          )
        )
    }

    "must return the correct row when the answer is No" in {
      val answers =
        emptyUserAnswers
          .set(PartnerDetailsRemoveAdditionalInfoForPartnerAddressYesNoPage(businessNumber), false)
          .success
          .value

      PartnerDetailsRemoveAdditionalInfoForPartnerAddressYesNoSummary.row(answers).value mustBe
        SummaryListRowViewModel(
          key   = "removeAdditionalInfoForPartnerAddressYesNo.checkYourAnswersLabel",
          value = ValueViewModel("site.no"),
          actions = Seq(
            ActionItemViewModel(
              "site.change",
              controllers.partnerdetails.routes.RemoveAdditionalInfoForPartnerAddressYesNoController
                .onPageLoad()
                .url
            ).withVisuallyHiddenText(
              messages("removeAdditionalInfoForPartnerAddressYesNo.change.hidden")
            )
          )
        )
    }
  }
}
