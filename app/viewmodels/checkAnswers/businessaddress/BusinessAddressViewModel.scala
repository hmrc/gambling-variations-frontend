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
    val isUk = ua.get(BusinessAddressUkPage).isDefined
    val hasUkPostcodeOpt: Seq[SummaryListRow] = ua.get(BusinessAddressHasUkPostcodePage) match {
      case Some(_) => HasUkPostcodeRow.from(ua).flatMap(_ => )
      case None => Seq.empty
    }

    val addAddressAdditionalInfoOpt = ua.get(AddBusinessAddressAdditionalInformationPage) match {
      case Some(_) => AddAddressAdditionalInfoRow.from(ua)
      case None => Seq.empty
    }


    val addressUa: Option[Address] = if (isUk) {
      ua.get(BusinessAddressUkPage)
    } else {
      ua.get(BusinessAddressNonUkPage)
    }

    val businessAddressRowsBase =
      Seq(
        BusinessAddressAdditionalInfoRow.from(ua),
        BusinessAddressRow(address = addressUa, isUk = isUk).toRow)


    val rows = if(hasUkPostcodeOpt)
}
}