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
import forms.cancellation.ReasonForCancellationFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.test.FakeRequest
import views.html.cancellation.ReasonForCancellationView

import scala.jdk.CollectionConverters.*

class ReasonForCancellationViewSpec extends SpecBase {

  trait Setup {
    private val app = applicationBuilder().build()

    val view = app.injector.instanceOf[ReasonForCancellationView]

    val formProvider = new ReasonForCancellationFormProvider()

    val form = formProvider()

    val request: play.api.mvc.Request[?] = FakeRequest()

    val messages: Messages =
      app.injector
        .instanceOf[play.api.i18n.MessagesApi]
        .preferred(request)
  }

  "ReasonForCancellationView" - {

    "must render page correctly" in new Setup {

      val html = view(form, NormalMode)(request, messages)
      val doc: Document = Jsoup.parse(html.body)
      print(doc.toString)

      doc.title must include(
        messages("reasonForCancellation.title")
      )

      doc
        .select(".govuk-caption-l")
        .text() must include(
        messages("reasonForCancellation.caption")
      )

      doc
        .select(".govuk-fieldset__heading")
        .text mustBe messages("reasonForCancellation.heading")

      doc
        .select("input[type=radio] + label")
        .eachText()
        .asScala
        .toList must contain theSameElementsAs List(
        messages("reasonForCancellation.allMachineGamesAreExcluded"),
        messages("reasonForCancellation.allTakingsFromMGDAreExcluded"),
        messages("reasonForCancellation.noMachineGamesAreAvailableToPlay"),
        messages("reasonForCancellation.noLongerNeedToBeRegistered"),
        messages("reasonForCancellation.businessIsPartOfTheGroup")
      )

      doc
        .select("input[type=radio] ~ .govuk-radios__hint")
        .eachText()
        .asScala
        .toList must contain theSameElementsAs List(
        messages("reasonForCancellation.allMachineGamesAreExcluded.hint"),
        messages("reasonForCancellation.allTakingsFromMGDAreExcluded.hint"),
        messages("reasonForCancellation.noMachineGamesAreAvailableToPlay.hint"),
        messages("reasonForCancellation.noLongerNeedToBeRegistered.hint")
      )

      doc
        .select("button.govuk-button")
        .text must include(
        messages("site.continue")
      )

      doc
        .select(".govuk-error-summary")
        .size() mustEqual 0
    }

    "must render error summary when form has errors" in new Setup {

      val boundForm =
        form.bind(Map("reasonForCancellation" -> ""))

      val html = view(boundForm, NormalMode)(request, messages)
      val doc: Document = Jsoup.parse(html.body)

      doc
        .select(".govuk-error-summary")
        .size() mustEqual 1

      doc
        .select(".govuk-error-summary")
        .text must include(
        messages("reasonForCancellation.error.required")
      )
    }

  }
}
