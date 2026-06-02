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
import org.scalatest.matchers.must.Matchers.*
import play.api.i18n.Messages
import play.api.test.FakeRequest
import views.html.BusinessContactDetailsView

class BusinessContactDetailsViewSpec extends SpecBase {

  trait Setup {
    val app = applicationBuilder().build()

    val view = app.injector.instanceOf[BusinessContactDetailsView]

    implicit val request: play.api.mvc.Request[?] = FakeRequest()

    implicit val messages: Messages =
      app.injector.instanceOf[play.api.i18n.MessagesApi].preferred(request)
  }

  "BusinessContactDetailsView" - {
    "must show expected values when data is populated" in new Setup {

      val html = view("1", "2", "3", "4", true)(request, messages)

      val doc = Jsoup.parse(html.body)

      doc.title             must include(messages("contactDetails.title"))
      doc.select("h1").text must include(messages("contactDetails.heading"))

      doc.select(".contact-numbers").text must include(messages("contactDetails.label.phoneNumber"))
      doc.select(".contact-numbers").text must include(messages("1"))
      doc.select(".contact-numbers").text must include(messages("contactDetails.label.mobilePhoneNumber"))
      doc.select(".contact-numbers").text must include(messages("2"))
      doc.select(".fax-number").text      must include(messages("3"))
      doc.select(".email-address").text   must include(messages("4"))
    }

    "must display 'Not provided' when value is not provided" in new Setup {

      val html = view("1", "2", "", "4", false)(request, messages)

      val doc = Jsoup.parse(html.body)

      doc.select(".contact-numbers").text must include(messages("contactDetails.label.phoneNumber"))
      doc.select(".contact-numbers").text must include(messages("1"))
      doc.select(".contact-numbers").text must include(messages("contactDetails.label.mobilePhoneNumber"))
      doc.select(".contact-numbers").text must include(messages("2"))
      doc.select(".fax-number").text      must include(messages("contactDetails.message.notProvided"))
      doc.select(".email-address").text   must include(messages("4"))
    }

  }
}
