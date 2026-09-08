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

package viewmodels.checkAnswers.licencespremises

import base.SpecBase
import pages.licencespremises.LicenceDetailsLandlordLicenceYesNoPage
import play.api.Application
import play.api.i18n.Messages
import viewmodels.checkAnswers.licensepremises.LicenceDetailsLandlordLicenceYesNoSummary
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

class LicenceDetailsLandlordLicenceYesNoSummarySpec extends SpecBase {
  

  lazy val app: Application = applicationBuilder().build()

  implicit val messages: Messages = this.messages(app)

  "LicenceDetailsAddNationalInsuranceNumberYesNoSummary" - {

    "must return None when the question has not been answered" in {
      LicenceDetailsLandlordLicenceYesNoSummary.row(emptyUserAnswers) mustBe None
    }

    "must return the correct row when the answer is Yes" in {
      val answers =
        emptyUserAnswers
          .set(LicenceDetailsLandlordLicenceYesNoPage, true)
          .success
          .value

      LicenceDetailsLandlordLicenceYesNoSummary.row(answers).value mustBe
        SummaryListRowViewModel(
          key   = "licenceDetailsLandlordLicenceYesNo.checkYourAnswersLabel",
          value = ValueViewModel("site.yes"),
          actions = Seq(
            ActionItemViewModel(
              "site.change",
              controllers.licencespremises.routes.LicenceDetailsLandlordLicenceYesNoController.onPageLoad().url
            ).withVisuallyHiddenText(
              messages("licenceDetailsLandlordLicenceYesNo.change.hidden")
            )
          )
        )
    }

    "must return the correct row when the answer is No" in {
      val answers =
        emptyUserAnswers
          .set(LicenceDetailsLandlordLicenceYesNoPage, false)
          .success
          .value

      LicenceDetailsLandlordLicenceYesNoSummary.row(answers).value mustBe
        SummaryListRowViewModel(
          key   = "licenceDetailsLandlordLicenceYesNo.checkYourAnswersLabel",
          value = ValueViewModel("site.no"),
          actions = Seq(
            ActionItemViewModel(
              "site.change",
              controllers.licencespremises.routes.LicenceDetailsLandlordLicenceYesNoController.onPageLoad().url
            ).withVisuallyHiddenText(
              messages("licenceDetailsLandlordLicenceYesNo.change.hidden")
            )
          )
        )
    }
  }
}
