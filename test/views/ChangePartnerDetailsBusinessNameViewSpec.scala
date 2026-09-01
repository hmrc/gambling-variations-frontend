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
import forms.ChangeBusinessNameFormProvider
import models.{BusinessType, NormalMode}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.matchers.must.Matchers.*
import play.api.i18n.Messages
import play.api.test.FakeRequest
import utils.BusinessTypeKeyBuilder
import views.html.ChangePartnerDetailsBusinessNameView

class ChangePartnerDetailsBusinessNameViewSpec extends SpecBase {

  trait Setup(businessType: BusinessType) {
    private val app = applicationBuilder().build()

    private val view = app.injector.instanceOf[ChangePartnerDetailsBusinessNameView]

    implicit private val request: play.api.mvc.Request[?] = FakeRequest()

    implicit val messages: Messages =
      app.injector.instanceOf[play.api.i18n.MessagesApi].preferred(request)

    private val formProvider = new ChangeBusinessNameFormProvider()

    private val filledForm = formProvider(businessType).fill("Business Name")

    private val html = view(filledForm,
                            NormalMode,
                            businessType,
                            BusinessTypeKeyBuilder.headingKeyFor(businessType),
                            BusinessTypeKeyBuilder.titleKeyFor(businessType)
                           )(request, messages)

    val doc: Document = Jsoup.parse(html.body)

  }

  "ChangePartnerDetailsBusinessNameView" - {

    "must render page correctly with limited liability partnership businessType" in new Setup(BusinessType.LimitedLiabilityPartnership) {
      doc.title must include(messages("changeBusinessName.title.llp"))

      doc.select("span").select(".govuk-caption-l").text() must include(messages("changeRegistrationDetails.caption"))

      doc.select("input").`val`() must include("Business Name")

      doc.body().text() must include(messages("changeBusinessName.heading.llp"))

      doc.select("button.govuk-button").text must include(messages("site.continue"))
    }

    "must render page correctly with partnership businessType" in new Setup(BusinessType.Partnership) {
      doc.title must include(messages("changeBusinessName.title.partnership"))

      doc.select("span").select(".govuk-caption-l").text() must include(messages("changeRegistrationDetails.caption"))

      doc.select("input").`val`() must include("Business Name")

      doc.body().text() must include(messages("changeBusinessName.heading.partnership"))

      doc.select("button.govuk-button").text must include(messages("site.continue"))
    }

    "must render page correctly with unincorporated body businessType" in new Setup(BusinessType.Unincorporatedbody) {
      doc.title must include(messages("changeBusinessName.title.unincorporatedbody"))

      doc.select("span").select(".govuk-caption-l").text() must include(messages("changeRegistrationDetails.caption"))

      doc.select("input").`val`() must include("Business Name")

      doc.body().text() must include(messages("changeBusinessName.heading.unincorporatedbody"))

      doc.select("button.govuk-button").text must include(messages("site.continue"))
    }

    "must render page correctly with corporate body businessType" in new Setup(BusinessType.Corporatebody) {
      doc.title must include(messages("changeBusinessName.title.corporatebody"))

      doc.select("span").select(".govuk-caption-l").text() must include(messages("changeRegistrationDetails.caption"))

      doc.select("input").`val`() must include("Business Name")

      doc.body().text() must include(messages("changeBusinessName.heading.corporatebody"))

      doc.select("button.govuk-button").text must include(messages("site.continue"))
    }

  }
}
