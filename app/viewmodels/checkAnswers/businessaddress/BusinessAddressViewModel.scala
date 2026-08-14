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

import models.{CheckMode, Mode, NormalMode, UserAnswers}
import pages.*
import play.api.i18n.Messages
import repositories.SessionRepository
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import viewmodels.govuk.all.SummaryListViewModel

case object BusinessAddressViewModel {
  def from(ua: UserAnswers, mode: Mode)(implicit messages: Messages): SummaryList = {
    val isUk = ua.get(BusinessAddressUkPage).fold(false)(_ => true)
    val isNonUk = ua.get(BusinessAddressNonUkPage).fold(false)(_ => true)

    val addressExists = isUk || isNonUk

    val hasPostcodeRowOpt: Option[SummaryListRow] = HasUkPostcodeRow.from(ua)
    val addAddressInfoRowOpt: Option[SummaryListRow] = AddAddressAdditionalInfoRow.from(ua)

    val howToChangeRow = Seq(HowToChangeBusinessAddressRow.from(ua))
    val addressRow = Seq(BusinessAddressRow(ua, mode, isUk, isNonUk).toRow)
    val addInfoRow = Seq(BusinessAddressAdditionalInfoRow.from(ua, mode))

    val addressBaseRows = addressRow ++ addInfoRow

    SummaryListViewModel(
      if (!addressExists) {
        Seq(hasPostcodeRowOpt match {
          case Some(row) => Some(row)
          case None      => None
        }).flatten ++ addressRow ++
          Seq(addAddressInfoRowOpt match {
            case Some(row) => Some(row)
            case None      => None
          }).flatten ++ addInfoRow
      } else {
        howToChangeRow ++ addressBaseRows
      }
    )
  }
}
