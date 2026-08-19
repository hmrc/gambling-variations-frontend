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

package views

import base.SpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.must.Matchers.*
import play.api.Application
import play.api.i18n.Messages
import play.api.mvc.RequestHeader
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import views.html.ErrorTemplate

class ErrorTemplateSpec extends SpecBase {

  trait Setup {
    val app: Application = applicationBuilder().build()

    val view: ErrorTemplate = app.injector.instanceOf[ErrorTemplate]

    implicit val rh: RequestHeader = FakeRequest()

    implicit val messages: Messages =
      app.injector.instanceOf[play.api.i18n.MessagesApi].preferred(rh)
  }

  "ErrorTemplate" - {

    "must render the page title, main heading, and body message correctly" in new Setup {

      val pageTitleKey = "site.serviceName"
      val headingKey = "site.serviceName"
      val messageKey = "site.continue"

      val html: HtmlFormat.Appendable = view(pageTitleKey, headingKey, messageKey)(rh, messages)

      val doc: Document = Jsoup.parse(html.body)

      doc.title() must include(messages(pageTitleKey))
      doc.select("h1").text() mustBe messages(headingKey)
      doc.select(".govuk-body").text() mustBe messages(messageKey)
    }
  }
}
