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
import forms.DeclarationSubmittedFormProvider
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.must.Matchers.*
import play.api.i18n.Messages
import play.api.test.FakeRequest
import views.html.DeclarationView

class DeclarationViewSpec extends SpecBase {

  trait Setup(isGroupMember: Boolean) {
    private val app = applicationBuilder().build()

    private val view = app.injector.instanceOf[DeclarationView]

    implicit private val request: play.api.mvc.Request[?] = FakeRequest()

    implicit val messages: Messages =
      app.injector.instanceOf[play.api.i18n.MessagesApi].preferred(request)

    private val formProvider = new DeclarationSubmittedFormProvider()
    private val form = formProvider()

    private val html = view(form, isGroupMember)(request, messages)

    val doc: Document = Jsoup.parse(html.body)

  }

  "DeclarationView" - {

    "must render page correctly for group" in new Setup(true) {

      doc.title must include(messages("declaration.title"))

      doc.select("h1").select(".govuk-heading-l").text()   must include(messages("declaration.heading"))
      doc.select("p").select(".govuk-body").text()         must include(messages("declaration.group.p1"))
      doc.select("p").select(".govuk-body").text()         must include(messages("declaration.group.p2"))
      doc.select("li").text()                              must include(messages("declaration.group.bp1"))
      doc.select("li").text()                              must include(messages("declaration.group.bp2"))
      doc.select("li").text()                              must include(messages("declaration.group.bp3"))
      doc.select("li").text()                              must include(messages("declaration.group.bp4"))
      doc.select("li").text()                              must include(messages("declaration.group.bp5"))
      doc.select("li").text()                              must include(messages("declaration.group.bp6"))
      doc.select("li").text()                              must include(messages("declaration.group.bp7"))
      doc.select("li").text()                              must include(messages("declaration.group.bp8"))
      doc.select("span").select(".govuk-caption-l").text() must include(messages("changeRegistrationDetails.caption"))

      doc.select("button.govuk-button").text must include(messages("declaration.submit"))

    }

    "must render page correctly for individual" in new Setup(false) {

      doc.title must include(messages("declaration.title"))

      doc.select("h1").select(".govuk-heading-l").text()   must include(messages("declaration.heading"))
      doc.select("p").select(".govuk-body").text()         must include(messages("declaration.p1"))
      doc.select("span").select(".govuk-caption-l").text() must include(messages("changeRegistrationDetails.caption"))

      doc.select("button.govuk-button").text must include(messages("declaration.submit"))

    }

  }
}
