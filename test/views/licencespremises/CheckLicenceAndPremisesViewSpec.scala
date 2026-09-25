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

package views.licencespremises

import base.SpecBase
import controllers.licencespremises.routes
import models.licencespremises.LicencesAndPremisesRadioOptions.{ByPost, Online}
import models.licencespremises.OtherLicencesAndPermitsGB.clubMachine
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.Application
import play.api.i18n.Messages
import play.api.test.FakeRequest
import viewmodels.checkAnswers.licencespremises.CheckLicencesAndPremisesViewModel
import views.html.licencespremises.CheckLicenceAndPremisesView

class CheckLicenceAndPremisesViewSpec extends SpecBase {

  private lazy val application: Application = applicationBuilder().build()
  private implicit lazy val msgs: Messages = messages(application)

  private val emptyViewModel = CheckLicencesAndPremisesViewModel(
    licenceNumber          = None,
    isPubTenant            = false,
    licencesAndPermitsGB   = Nil,
    licencesAndPermitsNI   = Nil,
    hasPremisesNotCovered  = false,
    provideAddressesAnswer = None,
    premisesCount          = 0,
    isSubmitted            = false
  )

  private def render(viewModel: CheckLicencesAndPremisesViewModel): Document = {
    val view = application.injector.instanceOf[CheckLicenceAndPremisesView]
    Jsoup.parse(view(viewModel)(FakeRequest(), msgs).toString)
  }

  "CheckLicenceAndPremisesView" - {

    "must render the title, caption, heading and default back link" in {
      val document = render(emptyViewModel)

      document.title() mustEqual
        s"${msgs("checkLicenceAndPremises.title")} - ${msgs("changeRegistrationDetails.caption")} - ${msgs("service.name")} - GOV.UK"
      document.select(".govuk-caption-l").text() mustEqual msgs("changeRegistrationDetails.caption")
      document.select("h1").text() mustEqual msgs("checkLicenceAndPremises.heading")
      document.select("a.govuk-back-link").attr("href") mustEqual "#"
    }

    // The rows themselves, their values and their change links are covered by CheckLicencesAndPremisesViewModelSpec
    "must render the summary list" in {
      val document = render(emptyViewModel.copy(licenceNumber = Some("123-456789-A-123456-789"), licencesAndPermitsGB = Seq(clubMachine)))

      document.select(".govuk-summary-list").text() must include("123-456789-A-123456-789")
      document.select(".govuk-summary-list").text() must include(msgs("otherLicencesAndPermitsGB.option.clubMachine"))
    }

    "must render the message that premises details are required, and not the by post section or the button link" in {
      val document = render(emptyViewModel.copy(provideAddressesAnswer = Some(Online)))

      document.select("main").text() must include(msgs("checkLicenceAndPremises.noLicences.p1"))
      document.select("main h2").isEmpty mustBe true
      document.select("main").text() must not include msgs("checkLicenceAndPremises.byPost.heading")
      document.select("main").text() must not include msgs("changeRegistrationDetails.readyToSubmit")
      document.select(".govuk-button").attr("href") mustEqual "#"
    }

    "must not render the message that premises details are required when premises have been provided" in {
      val document = render(emptyViewModel.copy(premisesCount = 20))

      document.select("main").text() must not include msgs("checkLicenceAndPremises.noLicences.p1")
      document.select(".govuk-button").attr("href") mustEqual controllers.routes.ChangeRegistrationDetailsController.onPageLoad().url
    }

    "must render the not covered message when online is selected without any premises" in {
      val document = render(emptyViewModel.copy(isPubTenant = true, hasPremisesNotCovered = true, provideAddressesAnswer = Some(Online)))

      document.select("main").text() must include(msgs("checkLicenceAndPremises.premisesNotCovered.p1"))
    }

    "must render the by post section when the method is not provided and there are no premises" in {
      val document = render(emptyViewModel)

      document.select("main h2").text() mustEqual msgs("checkLicenceAndPremises.byPost.heading")
      document.select("main").text() must not include msgs("checkLicenceAndPremises.noLicences.p1")
    }

    "must render the by post section with the download link and address, and the ready to submit message" in {
      val document = render(emptyViewModel.copy(provideAddressesAnswer = Some(ByPost), isSubmitted = true))

      document.select("main h2").text() mustEqual msgs("checkLicenceAndPremises.byPost.heading")

      val formLink = document.select("main a[href*=mgd5]")
      formLink.attr("href") mustEqual "https://www.tax.service.gov.uk/print-and-post/form/Customs/1.0/MGD5/mgd5.xdp"
      formLink.attr("rel") mustEqual "noreferrer noopener"
      formLink.text() mustEqual msgs("checkLicenceAndPremises.byPost.link")
      formLink.parents().get(0).text() mustEqual
        s"${msgs("checkLicenceAndPremises.byPost.link")} ${msgs("checkLicenceAndPremises.byPost.p1")}"

      val addressLines = (1 to 6).map(line => msgs(s"checkLicenceAndPremises.byPost.address.line$line"))
      addressLines.foreach(line => document.select("main").text() must include(line))

      document.select("main").text() must include(msgs("changeRegistrationDetails.readyToSubmit"))
    }

    "must not render the by post section when the method is online" in {
      val document = render(emptyViewModel.copy(premisesCount = 20))

      document.select("main h2").isEmpty mustBe true
      document.select("main a[href*=mgd5]").isEmpty mustBe true
    }
  }
}
