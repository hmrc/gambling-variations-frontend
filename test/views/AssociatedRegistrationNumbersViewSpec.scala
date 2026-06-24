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
import forms.AssociatedRegistrationNumbersFormProvider
import models.{NormalMode, RegistrationNumbers}
import org.jsoup.Jsoup
import org.scalatest.matchers.must.Matchers.*
import play.api.i18n.Messages
import play.api.test.FakeRequest
import views.html.AssociatedRegistrationNumbersView

class AssociatedRegistrationNumbersViewSpec extends SpecBase {

  trait Setup {
    val app = applicationBuilder().build()

    val view = app.injector.instanceOf[AssociatedRegistrationNumbersView]
    val formProvider = new AssociatedRegistrationNumbersFormProvider()
    val form = formProvider()

    implicit val request: play.api.mvc.Request[?] = FakeRequest()

    implicit val messages: Messages =
      app.injector.instanceOf[play.api.i18n.MessagesApi].preferred(request)
  }

  "AssociatedRegistrationNumbersView" - {
    "must show expected values when data is populated" in new Setup {

      val html =
        view(form, NormalMode, RegistrationNumbers(Some(Seq("XHM00000199", "ZIU00001218")), Some(Seq("GTT28881666"))), true)(request, messages)

      val doc = Jsoup.parse(html.body)

      doc.title                                             must include(messages("associatedRegistrationNumbers.title"))
      doc.select("h1").text                                 must include(messages("associatedRegistrationNumbers.heading"))
      doc.select("p").text                                  must include(messages("associatedRegistrationNumbers.paragraph"))
      doc.select(".associated-reg-number-XHM00000199").text must include("XHM00000199")
      doc.select(".associated-reg-number-ZIU00001218").text must include("ZIU00001218")
      doc.select(".associated-reg-number-GTT28881666").text must include("GTT28881666")
    }

    "must show radio buttons when less than 3 numbers" in new Setup {

      val html = view(form, NormalMode, RegistrationNumbers(Some(Seq("XHM00000199", "ZIU00001218")), None), true)(request, messages)

      val doc = Jsoup.parse(html.body)

      doc.select(".assoc-reg-radio-buttons").text must include(messages("associatedRegistrationNumbers.yesLabel"))
      doc.select(".assoc-reg-radio-buttons").text must include(messages("associatedRegistrationNumbers.noLabel"))
    }

    "must show pluralised H2 when unsubmitted associated numbers are present" in new Setup {

      val html =
        view(form, NormalMode, RegistrationNumbers(Some(Seq("A")), Some(Seq("ABC0000001", "XYZ00000001"))), false)(request, messages)

      val doc = Jsoup.parse(html.body)

      doc.select(".ready-to-submit-heading").text must include(messages("associatedRegistrationNumbers.unsubmitted.plural"))
    }

    "must show pluralised H2 when associated numbers are present" in new Setup {

      val html =
        view(form, NormalMode, RegistrationNumbers(Some(Seq("ABC0000001", "XYZ00000001")), Some(Seq("A"))), false)(request, messages)

      val doc = Jsoup.parse(html.body)

      doc.select(".submitted-heading").text must include(messages("associatedRegistrationNumbers.submitted.plural"))
    }

    "must show singular H2 when one unsubmitted associated number is present" in new Setup {

      val html = view(form, NormalMode, RegistrationNumbers(Some(Seq("A")), Some(Seq("ABC0000001"))), false)(request, messages)

      val doc = Jsoup.parse(html.body)

      doc.select(".ready-to-submit-heading").text must include(messages("associatedRegistrationNumbers.unsubmitted"))
    }

    "must show singular H2s when one associated number is present" in new Setup {

      val html = view(form, NormalMode, RegistrationNumbers(Some(Seq("A")), Some(Seq("ABC0000001"))), false)(request, messages)

      val doc = Jsoup.parse(html.body)

      doc.select(".submitted-heading").text must include(messages("associatedRegistrationNumbers.submitted"))
    }

    "must show max limit message when 3 numbers are present" in new Setup {

      val html =
        view(form, NormalMode, RegistrationNumbers(Some(Seq("XHM00000199", "ZIU00001218")), Some(Seq("GTT28881666"))), false)(request, messages)

      val doc = Jsoup.parse(html.body)

      doc.title                                             must include(messages("associatedRegistrationNumbers.title"))
      doc.select("h1").text                                 must include(messages("associatedRegistrationNumbers.heading"))
      doc.select(".associated-reg-number-XHM00000199").text must include("XHM00000199")
      doc.select(".assoc-reg-max-limit-reached").text       must include(messages("associatedRegistrationNumbers.hint"))
    }

  }
}
