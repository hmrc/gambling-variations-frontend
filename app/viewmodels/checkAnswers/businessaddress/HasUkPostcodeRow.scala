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
import pages.businessaddress.BusinessAddressHasUkPostcodePage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.all.{ActionItemViewModel, FluentActionItem, SummaryListRowViewModel, ValueViewModel}
import viewmodels.implicits.*

case object HasUkPostcodeRow {
  def from(ua: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] = {
    val result = ua
      .get(BusinessAddressHasUkPostcodePage)
      .fold(None)(hasPostcode =>
        Some(
          SummaryListRowViewModel(
            key   = "checkBusinessAddress.question.ukPostcode",
            value = ValueViewModel(if (hasPostcode) "site.yes" else "site.no"),
            actions = Seq(
              ActionItemViewModel("site.change", routes.BusinessUKAddrScreenerController.onPageLoad().url)
                .withVisuallyHiddenText(messages("checkBusinessAddress.question.ukPostcode.hidden"))
            )
          )
        )
      )
    result
  }
}
