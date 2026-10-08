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

package views.partnerdetails

import base.SpecBase
import controllers.partnerdetails.PartnerDetailsHelper
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import pages.BusinessNumberOrIndex
import play.api.data.Form
import play.api.data.Forms.boolean
import play.api.i18n.Messages
import play.api.test.FakeRequest
import viewmodels.checkAnswers.partnerdetails.{PartnerDetailsRow, PartnerDetailsViewModel}
import views.html.partnerdetails.PartnerDetailsView

class PartnerDetailsViewSpec extends SpecBase with PartnerDetailsHelper {

  trait Setup {
    val paginationHelper = PaginationHelper(1)
    val paginationHelperMaxSizePage2 = PaginationHelper(100, 2, (0 to 100).map(e => (e.toString, e.toString)))

    private val app = applicationBuilder().build()

    val view: PartnerDetailsView =
      app.injector.instanceOf[PartnerDetailsView]

    val form: Form[Boolean] =
      Form(
        "value" -> boolean
      )

    val request: play.api.mvc.Request[?] =
      FakeRequest()

    val messages: Messages =
      app.injector
        .instanceOf[play.api.i18n.MessagesApi]
        .preferred(request)

    def partner(
      index: BusinessNumberOrIndex = "0",
      name: String = "Test Partner",
      status: String = messages("partnerDetails.status.active"),
      statusDetails: Option[String] = None,
      partnerDetailsUrl: String = "/partner-details/0",
      removeUrl: Option[String] = Some("/partner-details/remove/0"),
      canRemove: Boolean = true
    ): PartnerDetailsRow =
      PartnerDetailsRow(
        index             = "0",
        name              = name,
        status            = status,
        statusDetails     = statusDetails,
        partnerDetailsUrl = partnerDetailsUrl,
        removeUrl         = removeUrl,
        canRemove         = canRemove
      )

    def viewModel(
      partners: Seq[PartnerDetailsRow] = Seq.empty,
      addAnotherPartner: Boolean = true,
      showNoPartnersMessage: Boolean = false,
      showMinimumPartnersMessage: Boolean = false,
      showMaximumPartnersMessage: Boolean = false,
      showSubmitMessage: Boolean = false
    ): PartnerDetailsViewModel =
      PartnerDetailsViewModel(
        partners                   = partners,
        addAnotherPartner          = addAnotherPartner,
        showNoPartnersMessage      = showNoPartnersMessage,
        showMinimumPartnersMessage = showMinimumPartnersMessage,
        showMaximumPartnersMessage = showMaximumPartnersMessage,
        showSubmitMessage          = showSubmitMessage
      )

    def render(
      model: PartnerDetailsViewModel,
      paginationHelper: PaginationHelper
    ): Document = {
      val html =
        view(
          form,
          model,
          paginationHelper.paginatedViewModel,
          paginationHelper.page,
          paginationHelper.from,
          paginationHelper.to,
          paginationHelper.totalRecords
        )(request, messages)

      Jsoup.parse(html.body)
    }
  }

  "PartnerDetailsView" - {

    "must render page correctly when there are no partners" in new Setup {

      val doc =
        render(
          viewModel(
            showNoPartnersMessage = true
          ),
          paginationHelper
        )

      doc.title must include(
        messages("partnerDetails.title")
      )

      doc
        .select(".govuk-caption-l")
        .text() must include(
        messages("changeRegistrationDetails.caption")
      )

      doc.select("h1").text mustBe
        messages("partnerDetails.heading")

      doc
        .select(".govuk-body")
        .text() must include(
        messages("partnerDetails.noPartners")
      )

      doc
        .select("table.govuk-table")
        .size() mustBe 0

      doc
        .select("input[type=radio]")
        .size() mustBe 2

      doc
        .select("button.govuk-button")
        .text() must include(
        messages("site.continue")
      )

      doc
        .select(".govuk-error-summary")
        .size() mustBe 0
    }

    "must render partner table when partners exist" in new Setup {

      val doc =
        render(
          viewModel(
            partners = Seq(
              partner()
            )
          ),
          paginationHelper
        )

      doc
        .select("table.govuk-table")
        .size() mustBe 1

      doc
        .select("tbody.govuk-table__body tr.govuk-table__row")
        .size() mustBe 1
    }

    "must render multiple pages naviagtion" in new Setup {
      val doc =
        render(
          viewModel(
            partners = Seq(
              partner()
            )
          ),
          paginationHelperMaxSizePage2
        )

      doc
        .select(".govuk-pagination__prev a")
        .first()
        .attr("href") must include("change-registration-details/partner-details?page=1")

      doc
        .select(".govuk-pagination__next a")
        .first()
        .attr("href") must include("change-registration-details/partner-details?page=3")
    }

    "must render partner name" in new Setup {

      val doc =
        render(
          viewModel(
            partners = Seq(
              partner(
                name = "ABC Partners"
              )
            )
          ),
          paginationHelper
        )

      doc
        .select("tbody.govuk-table__body")
        .text() must include("ABC Partners")
    }

    "must render partner details link" in new Setup {

      val doc =
        render(
          viewModel(
            partners = Seq(
              partner(
                partnerDetailsUrl = "/partner-details/0"
              )
            )
          ),
          paginationHelper
        )

      val link =
        doc
          .select("tbody.govuk-table__body a.govuk-link")
          .first()

      link.attr("href") mustBe "/partner-details/0"
    }

    "must render view or change visually hidden text" in new Setup {

      val doc =
        render(
          viewModel(
            partners = Seq(
              partner(
                name = "ABC Partners"
              )
            )
          ),
          paginationHelper
        )

      doc
        .select(".govuk-visually-hidden")
        .text() must include(
        messages("site.viewOrChange")
      )
    }

    "must render partner status" in new Setup {

      val doc =
        render(
          viewModel(
            partners = Seq(
              partner(
                status = messages("partnerDetails.status.active")
              )
            )
          ),
          paginationHelper
        )

      doc
        .select("tbody.govuk-table__body")
        .text() must include(
        messages("partnerDetails.status.active")
      )
    }

    "must render status details when present" in new Setup {

      val doc =
        render(
          viewModel(
            partners = Seq(
              partner(
                status        = messages("partnerDetails.status.dueToJoin"),
                statusDetails = Some("5 Sep 2026")
              )
            )
          ),
          paginationHelper
        )

      val tableText =
        doc
          .select("tbody.govuk-table__body")
          .text()

      tableText must include(
        messages("partnerDetails.status.dueToJoin")
      )

      tableText must include("5 Sep 2026")
    }

    "must render remove link when new partner can be removed" in new Setup {

      val doc =
        render(
          viewModel(
            partners = Seq(
              partner(
                index     = 1,
                name      = "ABC Partners",
                removeUrl = Some("/partner-details/change-remove/1"),
                canRemove = true
              )
            )
          ),
          paginationHelper
        )

      val links =
        doc
          .select("tbody.govuk-table__body a.govuk-link")

      links.size() mustBe 2

      val removeLink = links.last()

      removeLink.attr("href") mustBe "/partner-details/change-remove/1"

      removeLink.text() must include(
        messages("site.remove")
      )

      removeLink.text() must include("ABC Partners")
    }

    "must render remove link when existing partner can be removed" in new Setup {

      val doc =
        render(
          viewModel(
            partners = Seq(
              partner(
                index     = "0",
                name      = "ABC Partners",
                removeUrl = Some("/partner-details/remove/0"),
                canRemove = true
              )
            )
          ),
          paginationHelper
        )

      val links =
        doc
          .select("tbody.govuk-table__body a.govuk-link")

      links.size() mustBe 2

      val removeLink = links.last()

      removeLink.attr("href") mustBe "/partner-details/remove/0"

      removeLink.text() must include(
        messages("site.remove")
      )

      removeLink.text() must include("ABC Partners")
    }

    "must render cannot remove message when partner cannot be removed" in new Setup {

      val doc =
        render(
          viewModel(
            partners = Seq(
              partner(
                canRemove = false,
                removeUrl = None
              )
            )
          ),
          paginationHelper
        )

      doc
        .select("tbody.govuk-table__body")
        .text() must include(
        messages("partnerDetails.cannotRemove")
      )
    }

    "must not render remove link when partner cannot be removed" in new Setup {

      val doc =
        render(
          viewModel(
            partners = Seq(
              partner(
                canRemove = false,
                removeUrl = None
              )
            )
          ),
          paginationHelper
        )

      doc
        .select("tbody.govuk-table__body a")
        .size() mustBe 1
    }

    "must render minimum partners message" in new Setup {

      val doc =
        render(
          viewModel(
            partners                   = Seq(partner()),
            showMinimumPartnersMessage = true
          ),
          paginationHelper
        )

      doc
        .text() must include(
        messages("partnerDetails.minimumPartners")
      )
    }

    "must not render minimum partners message when not required" in new Setup {

      val doc =
        render(
          viewModel(
            partners                   = Seq(partner()),
            showMinimumPartnersMessage = false
          ),
          paginationHelper
        )

      doc
        .text() must not include (
        messages("partnerDetails.minimumPartners")
      )
    }

    "must render add another partner radios when another partner can be added" in new Setup {

      val doc =
        render(
          viewModel(
            partners          = Seq(partner()),
            addAnotherPartner = true
          ),
          paginationHelper
        )

      doc
        .select("input[type=radio]")
        .size() mustBe 2

      doc
        .text() must include(
        messages("partnerDetails.yesAddAnother")
      )

      doc
        .text() must include(
        messages("partnerDetails.noReturn")
      )
    }

    "must render maximum reached message when another partner cannot be added" in new Setup {

      val doc =
        render(
          viewModel(
            partners                   = Seq(partner()),
            addAnotherPartner          = false,
            showMaximumPartnersMessage = true
          ),
          paginationHelper
        )

      doc
        .text() must include(
        messages("partnerDetails.maximumReached")
      )
    }

    "must not render add another partner radios when maximum has been reached" in new Setup {

      val doc =
        render(
          viewModel(
            partners                   = Seq(partner()),
            addAnotherPartner          = false,
            showMaximumPartnersMessage = true
          ),
          paginationHelper
        )

      doc
        .select("input[type=radio]")
        .size() mustBe 0
    }

    "must render must continue message when required" in new Setup {

      val doc =
        render(viewModel(
                 partners          = Seq(partner()),
                 showSubmitMessage = true
               ),
               paginationHelper
              )

      doc
        .text() must include(
        messages("partnerDetails.mustContinue")
      )
    }

    "must not render must continue message when not required" in new Setup {

      val doc =
        render(
          viewModel(
            partners          = Seq(partner()),
            showSubmitMessage = false
          ),
          paginationHelper
        )

      doc
        .text() must not include (
        messages("partnerDetails.mustContinue")
      )
    }

    "must render multiple partners" in new Setup {

      val partners =
        Seq(
          PartnerDetailsRow(
            index             = "0",
            name              = "Alpha Partners",
            status            = messages("partnerDetails.status.active"),
            statusDetails     = None,
            partnerDetailsUrl = "/partner-details/0",
            removeUrl         = Some("/partner-details/remove/0"),
            canRemove         = true
          ),
          PartnerDetailsRow(
            index             = "1",
            name              = "Beta Partners",
            status            = messages("partnerDetails.status.dueToJoin"),
            statusDetails     = Some("10 Sep 2026"),
            partnerDetailsUrl = "/partner-details/1",
            removeUrl         = Some("/partner-details/remove/1"),
            canRemove         = true
          )
        )

      val doc =
        render(
          viewModel(
            partners = partners
          ),
          paginationHelper
        )

      doc
        .select("tbody.govuk-table__body tr.govuk-table__row")
        .size() mustBe 2

      doc
        .select("tbody.govuk-table__body")
        .text() must include("Alpha Partners")

      doc
        .select("tbody.govuk-table__body")
        .text() must include("Beta Partners")

      doc
        .select("tbody.govuk-table__body")
        .text() must include("10 Sep 2026")
    }

    "must render error summary when form has errors" in new Setup {

      val boundForm =
        form.bind(
          Map("value" -> "")
        )

      val html =
        view(
          boundForm,
          viewModel(
            showNoPartnersMessage = true
          ),
          paginationHelper.paginatedViewModel,
          paginationHelper.page,
          paginationHelper.from,
          paginationHelper.to,
          paginationHelper.totalRecords
        )(request, messages)

      val doc =
        Jsoup.parse(html.body)

      doc
        .select(".govuk-error-summary")
        .size() mustBe 1
    }

    "must render continue button" in new Setup {

      val doc =
        render(
          viewModel(
            showNoPartnersMessage = true
          ),
          paginationHelper
        )

      doc
        .select("button.govuk-button")
        .size() mustBe 1

      doc
        .select("button.govuk-button")
        .text() must include(
        messages("site.continue")
      )
    }

    "must render form with the correct action" in new Setup {

      val doc =
        render(
          viewModel(
            showNoPartnersMessage = true
          ),
          paginationHelper
        )

      doc
        .select("form")
        .attr("action") mustBe
        controllers.partnerdetails.routes.PartnerDetailsController.onSubmit(Some(1)).url
    }

    "must render form with autocomplete disabled" in new Setup {

      val doc =
        render(
          viewModel(
            showNoPartnersMessage = true
          ),
          paginationHelper
        )

      doc
        .select("form")
        .attr("autocomplete") mustBe "off"
    }
  }
}
