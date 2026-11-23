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
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.must.Matchers.*
import play.api.i18n.Messages
import play.api.test.FakeRequest
import views.html.cancellation.RegistrationCancellationLockedView

class RegistrationCancellationLockedViewSpec extends SpecBase {

  trait Setup {
    private val app = applicationBuilder().build()

    private val view = app.injector.instanceOf[RegistrationCancellationLockedView]

    implicit private val request: play.api.mvc.Request[?] = FakeRequest()

    implicit val messages: Messages =
      app.injector.instanceOf[play.api.i18n.MessagesApi].preferred(request)

    private val html = view("")(request, messages)

    val doc: Document = Jsoup.parse(html.body)

  }

  "RegistrationCancellationLockedView" - {

    "must render page correctly" in new Setup {

      println(doc.toString)

      doc.title must include(
        messages("registrationCancellationLocked.title")
      )

      doc.select("span").select(".govuk-caption-l").text() must include(
        messages("registrationCancellationLocked.caption")
      )

      doc.select("h1").text() must include(
        messages("registrationCancellationLocked.heading")
      )

      doc.select("p").select(".govuk-body").text() must include(
        messages("registrationCancellationLocked.p1")
      )

      doc.select("p").select(".govuk-link").text() must include(
        messages("registrationCancellationLocked.link")
      )
    }

  }
}
