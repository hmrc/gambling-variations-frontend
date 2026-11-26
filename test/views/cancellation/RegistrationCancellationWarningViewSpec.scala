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

package views.cancellation

import base.SpecBase
import forms.cancellation.RegistrationCancellationWarningFormProvider
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.test.FakeRequest
import views.html.cancellation.RegistrationCancellationWarningView

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class RegistrationCancellationWarningViewSpec extends SpecBase {

  private val nonGroupKey = "cancelRegistrationWarning"
  private val groupKey = "cancelGroupRegistrationWarning"

  trait Setup {

    private val app = applicationBuilder().build()

    val view: RegistrationCancellationWarningView = app.injector.instanceOf[RegistrationCancellationWarningView]

    val currentDate: LocalDate = LocalDate.of(2020, 3, 20)

    private val dateFormatter =
      DateTimeFormatter.ofPattern("d MMMM yyyy")

    val currentDateFormatted: String = dateFormatter.format(currentDate)

    val request: play.api.mvc.Request[?] =
      FakeRequest()

    implicit val messages: Messages =
      app.injector
        .instanceOf[play.api.i18n.MessagesApi]
        .preferred(request)

    val formProvider = new RegistrationCancellationWarningFormProvider()

    val form = formProvider()

    def document(formToRender: play.api.data.Form[Boolean] = form, key: String): Document = {
      val html = view(formToRender, currentDateFormatted, key)(request, messages)
      Jsoup.parse(html.body)
    }
  }

  "RegistrationCancellationWarningView" - {

    "group" - {

      "must render page correctly" in new Setup {

        val doc = document(key = groupKey)

        doc.title must include(
          messages("cancelGroupRegistrationWarning.title")
        )

        doc
          .select(".govuk-caption-l")
          .text() must include(
          messages("cancelGroupRegistrationWarning.caption")
        )

        doc
          .select("h1")
          .text() mustBe
          messages("cancelGroupRegistrationWarning.heading")

        doc
          .body()
          .text() must include
        messages("cancelGroupRegistrationWarning.paragraph")

        doc
          .select("button.govuk-button")
          .text() must include(
          messages("site.continue")
        )

        doc
          .select(".govuk-error-summary")
          .size() mustEqual 0
      }

      "must render error summary when form has errors" in new Setup {

        val boundForm =
          form.bind(Map("reasonForCancellation" -> ""))

        val html = view(boundForm, currentDateFormatted, groupKey)(request, messages)
        val doc: Document = Jsoup.parse(html.body)

        doc
          .select(".govuk-error-summary")
          .size() mustEqual 1

        doc
          .select(".govuk-error-summary")
          .text must include(
          messages("cancelGroupRegistrationWarning.error.required")
        )
      }
    }

    "noNgroup" - {

      "must render page correctly" in new Setup {

        val doc = document(key = nonGroupKey)

        doc.title must include(
          messages("cancelRegistrationWarning.title")
        )

        doc
          .select(".govuk-caption-l")
          .text() must include(
          messages("cancelRegistrationWarning.caption")
        )

        doc
          .select("h1")
          .text() mustBe
          messages("cancelRegistrationWarning.heading")

        doc
          .body()
          .text() must include
        messages("cancelRegistrationWarning.paragraph")

        doc
          .select("button.govuk-button")
          .text() must include(
          messages("site.continue")
        )

        doc
          .select(".govuk-error-summary")
          .size() mustEqual 0
      }

      "must render error summary when form has errors" in new Setup {

        val boundForm =
          form.bind(Map("reasonForCancellation" -> ""))

        val html = view(boundForm, currentDateFormatted, nonGroupKey)(request, messages)
        val doc: Document = Jsoup.parse(html.body)

        doc
          .select(".govuk-error-summary")
          .size() mustEqual 1

        doc
          .select(".govuk-error-summary")
          .text must include(
          messages("cancelRegistrationWarning.error.required")
        )
      }
    }

  }
}
