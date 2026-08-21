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

import models.BusinessChangeAddrOption.*
import models.UserAnswers
import pages.businessaddress.BusinessChangeAddrScreenerPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.all.{ActionItemViewModel, FluentActionItem, SummaryListRowViewModel, ValueViewModel}
import viewmodels.implicits.*

case object HowToChangeBusinessAddressRow {
  def from(ua: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] = {
    ua.get(BusinessChangeAddrScreenerPage) match {
      case Some(howToChange) =>
        Some(
          SummaryListRowViewModel(
            key = "checkBusinessAddress.question.howToChange",
            value = ValueViewModel(
              howToChange match {
                case DifferentUkAddress   => "checkBusinessAddress.changeOption.difUk"
                case ChangeToNonUkAddress => "checkBusinessAddress.changeOption.nonUk"
                case ChangeToUkAddress    => "checkBusinessAddress.changeOption.toUk"
                case EditCurrentAddress   => "checkBusinessAddress.changeOption.edit"
              }
            ),
            actions = Seq(
              ActionItemViewModel("site.change", "#")
                .withVisuallyHiddenText(messages("checkBusinessAddress.question.howToChange.hidden"))
            )
          )
        )
      case None => None
    }
  }
}
