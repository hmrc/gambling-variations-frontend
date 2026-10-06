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
import forms.ChangeBusinessNameFormProvider
import models.BusinessType
import org.jsoup.Jsoup
import play.api.test.FakeRequest
import play.api.test.Helpers.running
import utils.BusinessTypeKeyBuilder
import views.html.controllingbody.ChangeControllingBodyNameView

class ChangeControllingBodyNameViewSpec extends SpecBase {

  "ChangeControllingBodyNameView" - {
    BusinessType.values.filterNot(_ == BusinessType.Soleproprietor).foreach { businessType =>
      s"render the $businessType screen with accessible input, Back and Continue" in {
        val application = applicationBuilder().build()
        running(application) {
          implicit val request: play.api.mvc.Request[?] = FakeRequest()
          implicit val msgs: play.api.i18n.Messages = messages(application)
          val view = application.injector.instanceOf[ChangeControllingBodyNameView]
          val form = (new ChangeBusinessNameFormProvider())(businessType).fill("Controlling Body")
          val doc = Jsoup.parse(view(form, businessType).body)

          doc.title() must include(msgs(BusinessTypeKeyBuilder.titleKeyFor(businessType)))
          doc.title() must include(msgs("changeRegistrationDetails.caption"))
          doc.select("h1").size() mustEqual 1
          doc.select("label[for=value]").text() mustEqual msgs(BusinessTypeKeyBuilder.headingKeyFor(businessType))
          doc.select(".govuk-caption-l").text() mustEqual msgs("changeRegistrationDetails.caption")
          doc.select("#value").`val`() mustEqual "Controlling Body"
          doc.select("#value").attr("autocomplete") mustEqual "organization"
          doc.select("form").attr("method") mustEqual "POST"
          doc.select("form").attr("action") mustEqual routes.ChangeControllingBodyNameController.onSubmit(businessType).url
          doc.select(".govuk-back-link").text() mustEqual msgs("site.back")
          doc.select("button.govuk-button").text() mustEqual msgs("site.continue")
        }
      }
    }

    "associate inline errors and the error summary with the name field" in {
      val application = applicationBuilder().build()
      running(application) {
        implicit val request: play.api.mvc.Request[?] = FakeRequest()
        implicit val msgs: play.api.i18n.Messages = messages(application)
        val businessType = BusinessType.Partnership
        val view = application.injector.instanceOf[ChangeControllingBodyNameView]
        val form = (new ChangeBusinessNameFormProvider())(businessType).bind(Map("value" -> ""))
        val doc = Jsoup.parse(view(form, businessType).body)

        doc.title()                                   must startWith(msgs("error.title.prefix"))
        doc.select("#value").attr("aria-describedby") must include("value-error")
        doc.select("#value-error").text()             must include(msgs("changeBusinessName.error.required.partnership"))
        doc.select(".govuk-error-summary a").attr("href") mustEqual "#value"
      }
    }
  }
}
