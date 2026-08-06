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

package viewmodels.checkAnswers.businessAddress

import models.UserAnswers
import controllers.routes
import pages.BusinessAddressUkPage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

class BusinessAddressSummary {
  def from(answers: UserAnswers)(implicit messages: Messages): SummaryListRow =
    answers
      .get(BusinessAddressUkPage)
      .map { answer =>
        SummaryListRowViewModel(
          key = "checkBusinessAddress.label.address",
          value = ValueViewModel(
            s"""
                ${answer.address1}
                ${answer.address2.getOrElse(messages("site.notProvided"))}
                ${answer.address2.getOrElse(messages("site.notProvided"))}
                ${answer.address3.getOrElse(messages("site.notProvided"))}
                ${answer.address4.getOrElse(messages("site.notProvided"))}
                ${answer.postcode.getOrElse(messages("site.notProvided"))}
            """
          ),
          actions = Seq(ActionItemViewModel("site.change", routes.PageNotFoundController.onPageLoad().url))
        )
      }
      .getOrElse(
        SummaryListRowViewModel(
          key     = "checkBusinessAddress.label.address",
          value   = ValueViewModel(messages("site.notProvided")),
          actions = Seq(ActionItemViewModel("site.change", routes.PageNotFoundController.onPageLoad().url))
        )
      )
}
