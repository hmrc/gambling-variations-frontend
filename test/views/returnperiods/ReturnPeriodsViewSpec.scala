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

package views.returnperiods

import base.SpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.returnperiods.ReturnPeriodsView

import scala.jdk.CollectionConverters.*

class ReturnPeriodsViewSpec extends SpecBase {

  private def renderPage(): (Document, Messages) = {
    val application = applicationBuilder().build()

    running(application) {
      val view = application.injector.instanceOf[ReturnPeriodsView]
      val request = FakeRequest(GET, "/")
      val msgs = messages(application)

      (Jsoup.parse(view()(request, msgs).toString), msgs)
    }
  }

  private val (doc, msgs) = renderPage()

  "ReturnPeriodsView" - {

    "must have the correct title" in {
      doc.title() must startWith(s"${msgs("returnPeriods.title")} - ${msgs("changeRegistrationDetails.caption")}")
    }

    "must show the caption" in {
      doc.select("span.govuk-caption-l").text() mustBe msgs("changeRegistrationDetails.caption")
    }

    "must show a single h1 with the heading" in {
      val headings = doc.select("h1")

      headings.size() mustBe 1
      headings.text() mustBe msgs("returnPeriods.heading")
    }

    "must show both paragraphs in order" in {
      val paragraphs = doc.select("main p.govuk-body").asScala.map(_.text())

      paragraphs must contain inOrder (msgs("returnPeriods.p1"), msgs("returnPeriods.p2"))
    }

    "must show a continue button" in {
      val button = doc.select("main .govuk-button")

      button.size() mustBe 1
      button.text() mustBe msgs("site.continue")
    }
  }
}
