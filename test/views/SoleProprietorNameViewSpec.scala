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
import forms.SoleProprietorNameFormProvider
import models.{NormalMode, SoleProprietorName}
import org.jsoup.Jsoup
import play.api.i18n.Messages
import play.api.test.FakeRequest
import views.html.SoleProprietorNameView

class SoleProprietorNameViewSpec extends SpecBase {

  "SoleProprietorNameView" - {

    "must render the page with the expected content" in new Setup {

      val html = view(form, NormalMode)
      val doc = Jsoup.parse(html.body)

      doc.title must include(messages("soleProprietorName.title"))
      doc.select(".govuk-caption-l").text mustEqual messages("soleProprietorName.changeRegistrationDetails")
      doc.select("h1").text mustEqual messages("soleProprietorName.heading")

      doc.select("form").attr("action") mustEqual controllers.routes.SoleProprietorNameController.onSubmit(NormalMode).url

      doc.select("label[for=title]").text mustEqual messages("soleProprietorName.title.label")
      doc.select("label[for=firstName]").text mustEqual messages("soleProprietorName.firstName.label")
      doc.select("label[for=middleName]").text mustEqual messages("soleProprietorName.middleName.label")
      doc.select("label[for=lastName]").text mustEqual messages("soleProprietorName.lastName.label")

      doc.select("input#title").attr("name") mustEqual "title"
      doc.select("input#firstName").attr("name") mustEqual "firstName"
      doc.select("input#middleName").attr("name") mustEqual "middleName"
      doc.select("input#lastName").attr("name") mustEqual "lastName"

      doc.select("button").text must include(messages("site.continue"))
    }

    "must render populated values when the form is filled" in new Setup {

      val populatedForm = form.fill(
        SoleProprietorName(
          title = "Mr",
          firstName = "John",
          middleName = Some("Middle Name"),
          lastName = "Doe"
        )
      )

      val html = view(populatedForm, NormalMode)
      val doc = Jsoup.parse(html.body)

      doc.select("input#title").attr("value") mustEqual "Mr"
      doc.select("input#firstName").attr("value") mustEqual "John"
      doc.select("input#middleName").attr("value") mustEqual "Middle Name"
      doc.select("input#lastName").attr("value") mustEqual "Doe"
    }

    "must render an error summary when the form has errors" in new Setup {

      val errorForm = form.bind(
        Map(
          "title"      -> "",
          "firstName"  -> "",
          "middleName" -> "",
          "lastName"   -> ""
        )
      )

      val html = view(errorForm, NormalMode)
      val doc = Jsoup.parse(html.body)

      doc.select(".govuk-error-summary").size mustBe 1
    }
  }

  trait Setup {
    val app = applicationBuilder().build()
    val form = new SoleProprietorNameFormProvider()()
    val view = app.injector.instanceOf[SoleProprietorNameView]

    implicit val request: play.api.mvc.Request[?] = FakeRequest()

    implicit val messages: Messages =
      app.injector
        .instanceOf[play.api.i18n.MessagesApi]
        .preferred(request)
  }
}
