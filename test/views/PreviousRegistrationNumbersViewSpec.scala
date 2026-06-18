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
import forms.PreviousRegistrationNumbersFormProvider
import models.NormalMode
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.*
import play.api.i18n.Messages
import play.api.test.FakeRequest
import views.html.PreviousRegistrationNumbersView

class PreviousRegistrationNumbersViewSpec extends SpecBase {

  trait Setup {
    val app = applicationBuilder().build()

    val view = app.injector.instanceOf[PreviousRegistrationNumbersView]
    val formProvider = new PreviousRegistrationNumbersFormProvider()
    val form = formProvider()

    implicit val request: play.api.mvc.Request[?] = FakeRequest()

    implicit val messages: Messages =
      app.injector.instanceOf[play.api.i18n.MessagesApi].preferred(request)
  }

  "PreviousRegistrationNumbersView" - {
    "must show expected values when data is populated" in new Setup {

      val html = view(form, NormalMode, Some(Seq("XHM00000199", "ZIU00001218")), Some(Seq("GTT28881666")), 2, 1)(request, messages)

      val doc = Jsoup.parse(html.body)

      doc.title             must include(messages("previousRegistrationNumbers.title"))
      doc.select("h1").text must include(messages("previousRegistrationNumbers.heading"))

      doc.select(".previous-reg-number-XHM00000199").text must include("XHM00000199")
      doc.select(".previous-reg-number-ZIU00001218").text must include("ZIU00001218")
      doc.select(".previous-reg-number-GTT28881666").text must include("GTT28881666")
    }

    "must show radio buttons when less than 3 numbers" in new Setup {

      val html = view(form, NormalMode, Some(Seq("XHM00000199", "ZIU00001218")), None, 2, 0)(request, messages)

      val doc = Jsoup.parse(html.body)

      doc.select(".prev-reg-radio-buttons").text must include(messages("previousRegistrationNumbers.yesLabel"))
      doc.select(".prev-reg-radio-buttons").text must include(messages("previousRegistrationNumbers.noLabel"))
    }

    "must show pluralised H2 when unsubmitted previous numbers are present" in new Setup {

      val html =
        view(form, NormalMode, None, Some(Seq("ABC0000001", "XYZ00000001")), 0, 2)(request, messages)

      val doc = Jsoup.parse(html.body)

      doc.select("h2").text must include(messages("previousRegistrationNumbers.unsubmitted", "s"))
    }

    "must show singular H2 when one unsubmitted previous number is present" in new Setup {

      val html = view(form, NormalMode, None, Some(Seq("ABC0000001")), 0, 1)(request, messages)

      val doc = Jsoup.parse(html.body)

      doc.select("h2").text must include(messages("previousRegistrationNumbers.unsubmitted", ""))
    }

    "must show max limit message when 3 numbers are present" in new Setup {

      val html = view(form, NormalMode, Some(Seq("XHM00000199", "ZIU00001218")), Some(Seq("GTT28881666")), 2, 1)(request, messages)

      val doc = Jsoup.parse(html.body)

      doc.title                                           must include(messages("previousRegistrationNumbers.title"))
      doc.select("h1").text                               must include(messages("previousRegistrationNumbers.heading"))
      doc.select(".previous-reg-number-XHM00000199").text must include("XHM00000199")
      doc.select(".prev-reg-max-limit-reached").text      must include(messages("previousRegistrationNumbers.hint"))
    }

  }
}
