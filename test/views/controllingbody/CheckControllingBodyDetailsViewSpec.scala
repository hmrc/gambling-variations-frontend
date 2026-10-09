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
import models.Address
import models.BusinessType.Corporatebody
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.Application
import play.api.i18n.Messages
import play.api.test.FakeRequest
import viewmodels.checkAnswers.controllingbody.CheckControllingBodyDetailsViewModel
import views.html.controllingbody.CheckControllingBodyDetailsView

import java.time.LocalDate
import scala.jdk.CollectionConverters.*

class CheckControllingBodyDetailsViewSpec extends SpecBase {

  private lazy val application: Application = applicationBuilder().build()
  private implicit lazy val msgs: Messages = messages(application)

  // A corporate body with every mandatory detail provided
  private val complete = CheckControllingBodyDetailsViewModel(
    businessType        = Some(Corporatebody),
    businessName        = Some("Corporate Body CB Inc"),
    isUkIncorporated    = Some(true),
    dateOfIncorporation = Some(LocalDate.of(2000, 1, 1)),
    crn                 = Some("CR345678"),
    utr                 = Some("1234567890"),
    address             = Some(Address("18 Arundel Mews", Some("Worthing"), None, None, Some("BN11 5RG"), None)),
    phoneNumber         = Some("0191 202 2500")
  )

  private def render(viewModel: CheckControllingBodyDetailsViewModel): Document = {
    val view = application.injector.instanceOf[CheckControllingBodyDetailsView]
    Jsoup.parse(view(viewModel)(FakeRequest(), msgs).toString)
  }

  "CheckControllingBodyDetailsView" - {

    "must render the title, caption, heading, section headings and default back link" in {
      val document = render(complete)

      document.title() mustEqual
        s"${msgs("checkControllingBodyDetails.title")} - ${msgs("changeRegistrationDetails.caption")} - ${msgs("service.name")} - GOV.UK"
      document.select(".govuk-caption-l").text() mustEqual msgs("changeRegistrationDetails.caption")
      document.select("h1").text() mustEqual msgs("checkControllingBodyDetails.heading")
      document.select("main h2").eachText().asScala.toSeq mustEqual Seq(
        msgs("checkControllingBodyDetails.h2.businessDetails"),
        msgs("checkControllingBodyDetails.h2.address"),
        msgs("checkControllingBodyDetails.h2.contactDetails")
      )
      document.select("a.govuk-back-link").attr("href") mustEqual "#"
    }

    // The rows, their values and their links are covered by CheckControllingBodyDetailsViewModelSpec
    "must render a summary list under each section heading" in {
      val document = render(complete)

      val lists = document.select(".govuk-summary-list")
      lists.size mustEqual 3
      lists.get(0).text() must include("Corporate Body CB Inc")
      lists.get(1).text() must include("18 Arundel Mews")
      lists.get(2).text() must include("0191 202 2500")
    }

    "must render the submit message and link continue to change registration details when no mandatory detail is missing" in {
      val document = render(complete)

      document.select("main p.govuk-body").first().text() mustEqual msgs("checkControllingBodyDetails.submit")
      document.select("main").text() must not include msgs("checkControllingBodyDetails.mandatory")
      document.select(".govuk-button").text() mustEqual msgs("site.continue")
      document.select(".govuk-button").attr("href") mustEqual controllers.routes.ChangeRegistrationDetailsController.onPageLoad().url
    }

    "must render the missing details message, an add link without a change link, and link continue to the first missing detail" in {
      val document = render(complete.copy(businessName = None))

      document.select("main p.govuk-body").first().text() mustEqual msgs("checkControllingBodyDetails.mandatory")
      document.select("main").text() must not include msgs("checkControllingBodyDetails.submit")

      val nameRow = document.select(".govuk-summary-list__row").get(1)
      nameRow.select(".govuk-summary-list__key").text() mustEqual msgs("checkControllingBodyDetails.corporateBodyName")
      nameRow.select(".govuk-summary-list__value a").text() mustEqual msgs("checkControllingBodyDetails.corporateBodyName.add")
      nameRow.select(".govuk-summary-list__value a").attr("href") mustEqual routes.ChangeControllingBodyNameController.onPageLoad(Corporatebody).url
      nameRow.select(".govuk-summary-list__actions").isEmpty mustBe true
      nameRow.hasClass("govuk-summary-list__row--no-actions") mustBe true

      document.select(".govuk-button").attr("href") mustEqual routes.ChangeControllingBodyNameController.onPageLoad(Corporatebody).url
    }
  }
}
