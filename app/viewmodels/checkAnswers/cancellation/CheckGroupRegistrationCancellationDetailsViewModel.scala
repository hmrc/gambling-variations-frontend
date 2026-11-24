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

package viewmodels.checkAnswers.cancellation

import controllers.cancellation.routes
import models.{CheckMode, UserAnswers}
import pages.cancellation.CancelRegistrationDatePage
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object CheckGroupRegistrationCancellationDetailsViewModel {

  def summaryList(userAnswers: UserAnswers)(using messages: Messages): SummaryList = {
    SummaryListViewModel(
      cancellationDateRow(userAnswers).toSeq
    )
  }
  private def cancellationDateRow(
    userAnswers: UserAnswers
  )(using messages: Messages): Option[SummaryListRow] = {
    userAnswers.get(CancelRegistrationDatePage).map { date =>
      SummaryListRowViewModel(
        key   = "checkGroupRegistrationCancellationDetails.cancellationDate",
        value = ValueViewModel(formatDate(date)),
        actions = Seq(
          ActionItemViewModel(
            "site.change",
            routes.CancelRegistrationDateController.onPageLoadGroup(CheckMode).url
          ).withVisuallyHiddenText(
            messages("checkGroupRegistrationCancellationDetails.cancellationDate.change.hidden")
          )
        )
      )
    }
  }
  private def formatDate(date: LocalDate)(using messages: Messages): String = {
    val locale = Locale.forLanguageTag(messages.lang.code)
    date.format(DateTimeFormatter.ofPattern("d MMM yyyy", locale))

  }
}
