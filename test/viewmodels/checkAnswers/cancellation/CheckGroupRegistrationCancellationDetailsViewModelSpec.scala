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

import base.SpecBase
import controllers.cancellation.routes
import pages.cancellation.CancelRegistrationDatePage
import play.api.i18n.{Messages, MessagesApi}
import play.api.test.FakeRequest
import uk.gov.hmrc.govukfrontend.views.Aliases.Text

import java.time.LocalDate

class CheckGroupRegistrationCancellationDetailsViewModelSpec extends SpecBase {

  private val application = applicationBuilder().build()

  private given messages: Messages =
    application.injector.instanceOf[MessagesApi].preferred(FakeRequest())

  private val userAnswers =
    emptyUserAnswers.set(CancelRegistrationDatePage, LocalDate.of(2026, 11, 20)).success.value

  "CheckGroupRegistrationCancellationDetailsViewModelSpec" - {

    "summaryList" - {
      "contain only the cancellation row" in {
        val rows = CheckGroupRegistrationCancellationDetailsViewModel.summaryList(userAnswers).rows

        rows must have size 1
        rows.head.key.content mustBe Text(
          messages("checkGroupRegistrationCancellationDetails.cancellationDate")
        )
      }

      "format the date with short month name" in {
        val row = CheckGroupRegistrationCancellationDetailsViewModel.summaryList(userAnswers).rows.head
        row.value.content mustBe Text("20 Nov 2026")
      }

      "not show a leading zero on the day" in {
        val answers = emptyUserAnswers.set(CancelRegistrationDatePage, LocalDate.of(2026, 11, 2)).success.value
        val row = CheckGroupRegistrationCancellationDetailsViewModel.summaryList(answers).rows.head
        row.value.content mustBe Text("2 Nov 2026")
      }

      "link 'Change' to the group cancellation date page with hidden text" in {
        val action =
          CheckGroupRegistrationCancellationDetailsViewModel.summaryList(userAnswers).rows.head.actions.value.items.head

        action.href mustBe routes.CancelGroupRegistrationDateController.onPageLoad().url
        action.content mustBe Text(messages("site.change"))
        action.visuallyHiddenText mustBe Some(
          messages("checkGroupRegistrationCancellationDetails.cancellationDate.change.hidden")
        )
      }

      "return an empty summary list when there are no dates" in {
        CheckGroupRegistrationCancellationDetailsViewModel.summaryList(emptyUserAnswers).rows mustBe empty
      }
    }
  }

}
