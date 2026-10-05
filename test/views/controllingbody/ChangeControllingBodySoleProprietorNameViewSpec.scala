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

package views.controllingbody

import base.SpecBase
import controllers.controllingbody.routes
import forms.SoleProprietorNameFormProvider
import models.{BusinessType, SoleProprietorName}
import org.jsoup.Jsoup
import play.api.test.FakeRequest
import play.api.test.Helpers.running
import views.html.controllingbody.ChangeControllingBodySoleProprietorNameView

class ChangeControllingBodySoleProprietorNameViewSpec extends SpecBase {

  "ChangeControllingBodySoleProprietorNameView" - {
    "render the name fields, optional middle names, Back and Continue" in {
      val application = applicationBuilder().build()
      running(application) {
        implicit val request: play.api.mvc.Request[?] = FakeRequest()
        implicit val msgs: play.api.i18n.Messages = messages(application)
        val view = application.injector.instanceOf[ChangeControllingBodySoleProprietorNameView]
        val form = (new SoleProprietorNameFormProvider())().fill(SoleProprietorName("Mr", "Tom", None, "Smith"))
        val doc = Jsoup.parse(view(form).body)

        doc.title() must include(msgs("soleProprietorName.title"))
        doc.title() must include(msgs("changeRegistrationDetails.caption"))
        doc.select("h1").text() mustEqual msgs("soleProprietorName.heading")
        doc.select(".govuk-caption-l").text() mustEqual msgs("changeRegistrationDetails.caption")
        Seq(
          ("title", "Mr", "honorific-prefix"),
          ("firstName", "Tom", "given-name"),
          ("middleName", "", "additional-name"),
          ("lastName", "Smith", "family-name")
        ).foreach { case (field, value, autocomplete) =>
          doc.select(s"#$field").`val`() mustEqual value
          doc.select(s"label[for=$field]").text() mustEqual msgs(s"soleProprietorName.$field.label")
          doc.select(s"#$field").attr("autocomplete") mustEqual autocomplete
        }
        doc.select("form").attr("method") mustEqual "POST"
        doc.select("form").attr("action") mustEqual routes.ChangeControllingBodyNameController.onSubmit(BusinessType.Soleproprietor).url
        doc.select(".govuk-back-link").text() mustEqual msgs("site.back")
        doc.select("button.govuk-button").text() mustEqual msgs("site.continue")
      }
    }

    "link every required-field error to its input and keep middle names optional" in {
      val application = applicationBuilder().build()
      running(application) {
        implicit val request: play.api.mvc.Request[?] = FakeRequest()
        implicit val msgs: play.api.i18n.Messages = messages(application)
        val view = application.injector.instanceOf[ChangeControllingBodySoleProprietorNameView]
        val form = (new SoleProprietorNameFormProvider())().bind(Map.empty[String, String])
        val doc = Jsoup.parse(view(form).body)

        doc.title() must startWith(msgs("error.title.prefix"))
        Seq("title", "firstName", "lastName").foreach { field =>
          doc.select(s".govuk-error-summary a[href='#$field']").text() mustEqual msgs(s"soleProprietorName.error.$field.required")
          doc.select(s"#$field").attr("aria-describedby") must include(s"$field-error")
        }
        doc.select("#middleName-error").isEmpty mustBe true
      }
    }
  }
}
