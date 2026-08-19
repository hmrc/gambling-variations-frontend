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

import controllers.routes
import models.UserAnswers
import pages.BusinessAddressAdditionalInformationPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryListRow, Value}
import viewmodels.govuk.all.{ActionItemViewModel, FluentActionItem, SummaryListRowViewModel, ValueViewModel}
import viewmodels.implicits.*

case object BusinessAddressAdditionalInfoRow {
  def from(ua: UserAnswers)(implicit messages: Messages): SummaryListRow = {
    val addInfoValue: Value = ValueViewModel(ua.get(BusinessAddressAdditionalInformationPage) match {
      case Some(addInfo) => addInfo
      case _             => "site.notProvided"
    })

    val addressAdditionalInfoExists = ua.get(BusinessAddressAdditionalInformationPage).isDefined

    SummaryListRowViewModel(
      key   = "checkBusinessAddress.label.addInfo",
      value = addInfoValue,
      actions = Seq(
        ActionItemViewModel("site.change", routes.BusinessAddressAdditionalInfoController.onPageLoad().url)
          .withVisuallyHiddenText(messages("checkBusinessAddress.label.addInfo.hidden"))
      ) ++
        Seq(if (addressAdditionalInfoExists) {
          Some(
            ActionItemViewModel("site.remove", "#")
              .withVisuallyHiddenText(messages("checkBusinessAddress.label.addInfo.hidden"))
          )
        } else { None }).flatten
    )
  }
}
