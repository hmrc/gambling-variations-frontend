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
import controllers.routes
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.api.test.FakeRequest
import viewmodels.*
import views.html.ChangeRegistrationDetailsView

import scala.jdk.CollectionConverters.*

class ChangeRegistrationDetailsViewSpec extends SpecBase {

  private val app = applicationBuilder().build()

  private val view = app.injector.instanceOf[ChangeRegistrationDetailsView]

  private implicit val request: play.api.mvc.Request[?] = FakeRequest()

  private implicit val msgs: Messages =
    app.injector.instanceOf[play.api.i18n.MessagesApi].preferred(request)

  private val managementHomeUrl = "http://foo.com/home"

  private def viewModel(sections: Seq[RegistrationSectionRow]): ChangeRegistrationDetailsViewModel =
    ChangeRegistrationDetailsViewModel(
      mgdRegNumber      = mgdRegNum,
      managementHomeUrl = managementHomeUrl,
      submitUrl         = routes.DeclarationController.onPageLoad().url,
      sections          = sections
    )

  private val unchangedSections = Seq(
    RegistrationSectionRow("Business name", "/business-name", NoDetailsChanged),
    RegistrationSectionRow("Trading details", "/trading-details", NoDetailsChanged),
    RegistrationSectionRow("Return periods", "/return-periods", NoDetailsChanged)
  )

  private val changedSections =
    unchangedSections.updated(
      1,
      RegistrationSectionRow("Trading details", "/trading-details", ChangesReadyToSubmit)
    )

  private def render(sections: Seq[RegistrationSectionRow]): Document =
    Jsoup.parse(view(viewModel(sections)).body)

  private def rows(doc: Document) =
    doc.select("table.govuk-table tbody tr").asScala.toSeq

  "ChangeRegistrationDetailsView" - {

    "must render the caption, the heading and the page title" in {

      val doc = render(unchangedSections)

      doc.select("h1").text mustEqual "Change registration details"
      doc.select(".govuk-caption-l").text mustEqual s"MGD registration number: $mgdRegNum"
      doc.title must include("Change registration details")
    }

    "must render a back link" in {

      val doc = render(unchangedSections)

      doc.select(".govuk-back-link").size mustEqual 1
    }

    "must render the table headings" in {

      val doc = render(unchangedSections)

      val headings = doc.select("table.govuk-table thead th").asScala.map(_.text).toSeq

      headings mustEqual Seq("Section", "Status")
    }

    "must render one linked row per section, in order" in {

      val doc = render(unchangedSections)

      rows(doc).map(_.select("td").first.select("a.govuk-link").text) mustEqual
        Seq("Business name", "Trading details", "Return periods")

      rows(doc).map(_.select("td").first.select("a.govuk-link").attr("href")) mustEqual
        Seq("/business-name", "/trading-details", "/return-periods")
    }

    "must render each status" in {

      val doc = render(
        Seq(
          RegistrationSectionRow("Business name", "/business-name", NoDetailsChanged),
          RegistrationSectionRow("Trading details", "/trading-details", ChangesReadyToSubmit),
          RegistrationSectionRow("Controlling body details", "/controlling-body", NeedsCompleting)
        )
      )

      rows(doc).map(_.select("td").last.text) mustEqual
        Seq("No details changed", "Changes ready to submit", "Needs completing")
    }

    "must only emphasise the needs completing status" in {

      val doc = render(
        Seq(
          RegistrationSectionRow("Business name", "/business-name", NoDetailsChanged),
          RegistrationSectionRow("Trading details", "/trading-details", ChangesReadyToSubmit),
          RegistrationSectionRow("Controlling body details", "/controlling-body", NeedsCompleting)
        )
      )

      doc.select("table.govuk-table tbody strong").asScala.map(_.text).toSeq mustEqual Seq("Needs completing")
    }

    "when no section has changes" - {

      "must tell the user to change at least one detail" in {

        val doc = render(unchangedSections)

        doc.select("p.govuk-body").asScala.map(_.text) must contain(
          "You need to change at least one detail before you can submit your changes to HMRC."
        )
      }

      "must not show the submit button or the approval message" in {

        val doc = render(unchangedSections)

        doc.select(".govuk-button").size mustEqual 0

        doc.select("p.govuk-body").asScala.map(_.text) must not contain
          "Once HMRC approve your changes, we will update your registration certificate. This can take up to 48 hours."
      }
    }

    "when at least one section has changes" - {

      "must not tell the user to change at least one detail" in {

        val doc = render(changedSections)

        doc.select("p.govuk-body").asScala.map(_.text) must not contain
          "You need to change at least one detail before you can submit your changes to HMRC."
      }

      "must show the approval message" in {

        val doc = render(changedSections)

        doc.select("p.govuk-body").asScala.map(_.text) must contain(
          "Once HMRC approve your changes, we will update your registration certificate. This can take up to 48 hours."
        )
      }

      "must show a submit button linking to the declaration page" in {

        val doc = render(changedSections)

        val button = doc.select(".govuk-button")

        button.size mustEqual 1
        button.text mustEqual "Submit changes"
        button.attr("href") mustEqual routes.DeclarationController.onPageLoad().url
      }
    }

    "must link back to the manage Machine Games Duty home page" in {

      val doc = render(unchangedSections)

      val returnLink = doc.select(s"a[href=$managementHomeUrl]")

      returnLink.text mustEqual "Return to manage your Machine Games Duty"
    }
  }
}
