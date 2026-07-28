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
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.api.test.Helpers.running
import views.html.SystemErrorView

class SystemErrorViewSpec extends SpecBase {

  "SystemErrorView" - {

    "must render the page with correct heading and service desk link" in new Setup {

      val serviceDeskUrl = "https://www.gov.uk/find-hmrc-contacts/technical-support-with-hmrc-online-services"

      val html = view(serviceDeskUrl)
      val doc = Jsoup.parse(html.body)

      doc.title must include(messages("systemError.title"))

      doc.select("h1").text must include(messages("systemError.heading"))

      doc.select("p").text must include(messages("systemError.p1"))

      val link = doc.select("#service-desk-link")

      link.attr("href") mustEqual serviceDeskUrl

      link.attr("target") mustEqual "_blank"
      link.attr("rel") must include("noreferrer")
    }

    "must render the language toggle on an English page" in {

      val application = applicationBuilder().build()

      running(application) {
        val serviceDeskUrl = "https://www.gov.uk/find-hmrc-contacts/technical-support-with-hmrc-online-services"
        val request = FakeRequest("GET", controllers.routes.SystemErrorController.onPageLoad().url)
        val view = application.injector.instanceOf[SystemErrorView]
        val html = view(serviceDeskUrl)(request, messages(application))
        val doc = Jsoup.parse(html.body)
        val welshToggle = doc.select(".hmrc-service-navigation-language-select a").select(":contains(CYM)")

        doc.select(".govuk-back-link").text                         must include("Back")
        doc.select(".hmrc-service-navigation-language-select").text must include("ENG CYM")
        welshToggle.text                                            must include("CYM")
      }
    }

    "must render the language toggle on a Welsh page" in {

      val application = applicationBuilder().build()

      running(application) {
        val serviceDeskUrl = "https://www.gov.uk/find-hmrc-contacts/technical-support-with-hmrc-online-services"
        val request = FakeRequest("GET", controllers.routes.SystemErrorController.onPageLoad().url)
          .withCookies(play.api.mvc.Cookie("PLAY_LANG", "cy"))
        val view = application.injector.instanceOf[SystemErrorView]
        val messages = application.injector.instanceOf[play.api.i18n.MessagesApi].preferred(request)
        val html = view(serviceDeskUrl)(request, messages)
        val doc = Jsoup.parse(html.body)
        val englishToggle = doc.select(".hmrc-service-navigation-language-select a").select(":contains(ENG)")

        doc.select(".govuk-back-link").text                         must include("Yn ôl")
        doc.select(".hmrc-service-navigation-language-select").text must include("ENG")
        doc.select(".hmrc-service-navigation-language-select").text must include("CYM")
        englishToggle.text                                          must include("ENG")
      }
    }
  }

  trait Setup {
    val app = applicationBuilder().build()
    val view = app.injector.instanceOf[SystemErrorView]

    implicit val request: play.api.mvc.Request[?] = FakeRequest()

    implicit val messages: Messages =
      app.injector
        .instanceOf[play.api.i18n.MessagesApi]
        .preferred(request)
  }
}
