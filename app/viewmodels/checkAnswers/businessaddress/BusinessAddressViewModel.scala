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

package viewmodels.checkAnswers.businessaddress

import models.{Address, CheckMode, Mode, NormalMode, UserAnswers}
import pages.*
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import viewmodels.govuk.all.SummaryListViewModel

case object BusinessAddressViewModel {
  def from(ua: UserAnswers, mode: Mode)(implicit messages: Messages): SummaryList = {
    val hasUkPostcodeIsDefined = ua.get(BusinessAddressHasUkPostcodePage).isDefined
    val addressAdditionalInfoIsDefined = ua.get(AddBusinessAddressAdditionalInformationPage).isDefined
    val howToChangeBusinessAddressIsDefined = ua.get(BusinessAddressChangeScreenerPage).isDefined
    val yesNoRows =
      if (hasUkPostcodeIsDefined && addressAdditionalInfoIsDefined) {
        Seq(HasUkPostcodeRow.from(ua), AddAddressAdditionalInfoRow.from(ua))
      } else if (hasUkPostcodeIsDefined) {
        Seq(HasUkPostcodeRow.from(ua))
      } else {
        Seq(AddAddressAdditionalInfoRow.from(ua))
      }

    val howToChangeBusinessAddressRow = Seq(HowToChangeBusinessAddressRow.from(ua))

    val addressBaseRows = Seq(
      BusinessAddressRow(ua, mode).toRow,
      BusinessAddressAdditionalInfoRow.from(ua)
    )

    SummaryListViewModel(
      if (mode == NormalMode && (hasUkPostcodeIsDefined || addressAdditionalInfoIsDefined)) {
        yesNoRows ++ addressBaseRows
      } else if (mode == CheckMode && howToChangeBusinessAddressIsDefined) {
        howToChangeBusinessAddressRow ++ addressBaseRows
      } else {
        addressBaseRows
      }
    )
  }
}
