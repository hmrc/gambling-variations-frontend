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

package viewmodels.checkAnswers.partnerdetails

import base.SpecBase
import models.Address
import models.BusinessType.*
import play.api.i18n.{Messages, MessagesApi}
import play.api.test.FakeRequest
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Content
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

class CheckPartnerDetailsViewModelSpec extends SpecBase {

  private val application =
    applicationBuilder().build()

  private implicit val messages: Messages =
    application.injector
      .instanceOf[MessagesApi]
      .preferred(FakeRequest())

  private val change: Content = Text(messages("site.change"))
  private val remove: Content = Text(messages("site.remove"))

  private val contactLink: String =
    s"""<a href="https://www.gov.uk/find-hmrc-contacts/gambling-duties-enquiries" class="govuk-link" target="_blank" rel="noopener noreferrer">${messages(
        "partnerDetailsCheckYourAnswers.error.contactUsLinkText"
      )}</a>"""
  private val joiningDate = "1 Oct 2026"
  private val leavingDate = "31 Dec 2026"

  // Existing UK corporate body, fully answered, not joining or leaving
  private val existingPartner: CheckPartnerDetailsViewModel =
    CheckPartnerDetailsViewModel(
      businessNumberOrIndex     = "BPN000000001",
      typeOfBusiness            = Some(Corporatebody),
      businessName              = Some("Partner Company Ltd"),
      soleProprietorDob         = None,
      addTradingName            = Some(true),
      tradingName               = Some("Trading name"),
      dateOfJoining             = Some(joiningDate),
      dateOfLeaving             = None,
      addNino                   = None,
      nino                      = None,
      utr                       = Some("1121766916"),
      addVatRegistrationNumber  = None,
      vatRegistrationNumber     = None,
      isIncorporatedInUk        = Some(true),
      countryOfIncorporation    = None,
      dateOfIncorporation       = Some("12 May 2015"),
      foreignCorporateReference = None,
      companyRegistrationNumber = Some("01230002"),
      address                   = None,
      addAdditionalInformation  = None,
      additionalInformation     = None,
      contactNumbers            = None,
      addFaxNumber              = None,
      faxNumber                 = None,
      addEmailAddress           = None,
      emailAddress              = None,
      isNewPartnerFlow          = false,
      maybeSubmitted            = None,
      isDueToLeave              = false,
      isDueToJoin               = false,
      isMissingMandatoryDetails = false,
      hasChanges                = None
    )

  private val newPartnerNotSaved: CheckPartnerDetailsViewModel =
    existingPartner.copy(
      businessNumberOrIndex = 0,
      isNewPartnerFlow      = true,
      maybeSubmitted        = Some(false)
    )

  private val newPartnerSaved: CheckPartnerDetailsViewModel =
    newPartnerNotSaved.copy(
      maybeSubmitted = Some(true)
    )

  private val soleProprietor: CheckPartnerDetailsViewModel =
    newPartnerNotSaved.copy(
      typeOfBusiness            = Some(Soleproprietor),
      businessName              = Some("Ms PartnerFirst1 PartnerLast1"),
      soleProprietorDob         = Some("2 Jan 1985"),
      nino                      = Some("AA-000001-A"),
      vatRegistrationNumber     = Some("123450002"),
      isIncorporatedInUk        = None,
      dateOfIncorporation       = None,
      companyRegistrationNumber = None
    )

  // --- Helpers ---

  private def label(key: String): Content =
    Text(messages(s"partnerDetailsCheckYourAnswers.$key"))

  private def rowFor(rows: Seq[SummaryListRow], key: String): Option[SummaryListRow] =
    rows.find(_.key.content == label(key))

  private def keys(rows: Seq[SummaryListRow]): Seq[Content] =
    rows.map(_.key.content)

  private def actionsOf(row: SummaryListRow): Seq[Content] =
    row.actions.toSeq.flatMap(_.items).map(_.content)

  private def noticeBodies(viewModel: CheckPartnerDetailsViewModel): Seq[String] =
    viewModel.notices.map(_.body)

  private val missingDetails = messages("partnerDetailsCheckYourAnswers.error.missingDetails")
  private val missingChanges = messages("partnerDetailsCheckYourAnswers.error.missingChanges")
  private val contactUs = messages("partnerDetailsCheckYourAnswers.error.contactUs", contactLink)
  private val cannotChangeJoining = messages("partnerDetailsCheckYourAnswers.error.cannotChangeJoining", joiningDate, contactLink)
  private val cannotChangeLeaving = messages("partnerDetailsCheckYourAnswers.error.cannotChangeLeaving", leavingDate, contactLink)

  "CheckPartnerDetailsViewModel" - {

    "notices" - {

      "show submit changes then missing details for a new partner with missing mandatory fields" in {

        val viewModel =
          newPartnerNotSaved.copy(
            isMissingMandatoryDetails = true
          )

        noticeBodies(viewModel) mustBe Seq(missingChanges, missingDetails)
      }

      "show only submit changes for a complete new partner" in {

        noticeBodies(newPartnerNotSaved) mustBe Seq(missingChanges)
        noticeBodies(newPartnerSaved) mustBe Seq(missingChanges)
      }

      "show only contact us for an existing partner not due to join or leave" in {

        noticeBodies(existingPartner) mustBe Seq(contactUs)
      }

      "show only the joining notice for an existing partner due to join" in {

        val viewModel =
          existingPartner.copy(
            isDueToJoin = true
          )

        noticeBodies(viewModel) mustBe Seq(cannotChangeJoining)
      }

      "show only the leaving notice for an existing partner due to leave" in {

        val viewModel =
          existingPartner.copy(
            isDueToLeave  = true,
            dateOfLeaving = Some(leavingDate)
          )

        noticeBodies(viewModel) mustBe Seq(cannotChangeLeaving)
      }

      "show only the leaving notice when the partner is due to both join and leave" in {

        val viewModel =
          existingPartner.copy(
            isDueToJoin   = true,
            isDueToLeave  = true,
            dateOfLeaving = Some(leavingDate)
          )

        noticeBodies(viewModel) mustBe Seq(cannotChangeLeaving)
      }

      "fall back to contact us when due to join but the joining date is missing" in {

        val viewModel =
          existingPartner.copy(
            isDueToJoin   = true,
            dateOfJoining = None
          )

        noticeBodies(viewModel) mustBe Seq(contactUs)
      }

      "never show missing details for an existing partner" in {

        val viewModel =
          existingPartner.copy(
            isMissingMandatoryDetails = true
          )

        noticeBodies(viewModel) must not contain missingDetails
      }
    }

    "business details rows" - {

      "show date of incorporation and CRN for a UK corporate body" in {

        val rowKeys =
          keys(existingPartner.businessDetailsSummaryList)

        rowKeys must contain allOf (label("dateOfIncorporation"), label("companyRegistrationNumber"))
        rowKeys must contain noneOf (label("countryOfIncorporation"), label("foreignCorporateReference"))
      }

      "show country of incorporation and foreign reference for a non-UK corporate body" in {

        val viewModel =
          existingPartner.copy(
            isIncorporatedInUk = Some(false)
          )

        val rowKeys =
          keys(viewModel.businessDetailsSummaryList)

        rowKeys must contain allOf (label("countryOfIncorporation"), label("foreignCorporateReference"))
        rowKeys must contain noneOf (label("dateOfIncorporation"), label("companyRegistrationNumber"))
      }

      "show date of incorporation and CRN, but not the UK question, for an LLP" in {

        val viewModel =
          existingPartner.copy(
            typeOfBusiness = Some(LimitedLiabilityPartnership)
          )

        val rowKeys =
          keys(viewModel.businessDetailsSummaryList)

        rowKeys must contain allOf (label("llpName"), label("dateOfIncorporation"), label("companyRegistrationNumber"))
        rowKeys must not contain label("isIncorporatedInUk")
      }

      "show the sole proprietor rows for a sole proprietor" in {

        val rowKeys =
          keys(soleProprietor.businessDetailsSummaryList)

        rowKeys must contain allOf (
          label("soleProprietorName"),
          label("soleProprietorDob"),
          label("nino"),
          label("vatRegistrationNumber")
        )
        rowKeys must not contain label("isIncorporatedInUk")
      }

      "not show the UTR row for a partnership" in {

        val viewModel =
          existingPartner.copy(
            typeOfBusiness = Some(Partnership)
          )

        keys(viewModel.businessDetailsSummaryList) must not contain label("utr")
      }

      "show an add link with no actions when a mandatory answer is missing" in {

        val viewModel =
          newPartnerNotSaved.copy(
            utr = None
          )

        val utrRow =
          rowFor(viewModel.businessDetailsSummaryList, "utr").value

        utrRow.value.content.asHtml.body must include(messages("partnerDetailsCheckYourAnswers.utr.add"))
        utrRow.actions mustBe None
      }

      "only allow mandatory answers to be changed in the new partner flow" in {

        actionsOf(rowFor(newPartnerNotSaved.businessDetailsSummaryList, "utr").value) mustBe Seq(change)
        actionsOf(rowFor(existingPartner.businessDetailsSummaryList, "utr").value) mustBe empty
      }

      "show Not provided, with no add link, for a missing mandatory answer when due to join or leave" in {

        val viewModel =
          existingPartner.copy(
            utr         = None,
            isDueToJoin = true
          )

        val utrRow = rowFor(viewModel.businessDetailsSummaryList, "utr").value

        utrRow.value.content mustBe Text(messages("partnerDetailsCheckYourAnswers.noData"))
        utrRow.actions mustBe None
      }
    }

    "trading name actions" - {

      "change only for a new partner that has not been saved" in {

        actionsOf(rowFor(newPartnerNotSaved.businessDetailsSummaryList, "tradingName").value) mustBe Seq(change)
      }

      "change and remove for a new partner that has been saved" in {

        actionsOf(rowFor(newPartnerSaved.businessDetailsSummaryList, "tradingName").value) mustBe Seq(change, remove)
      }

      "change and remove for an existing partner" in {

        actionsOf(rowFor(existingPartner.businessDetailsSummaryList, "tradingName").value) mustBe Seq(change, remove)
      }

      "nothing for an existing partner due to join or leave" in {

        val viewModel =
          existingPartner.copy(
            isDueToLeave = true
          )

        actionsOf(rowFor(viewModel.businessDetailsSummaryList, "tradingName").value) mustBe empty
      }

      "show no data and keep only the change action when there is no trading name" in {

        val viewModel =
          newPartnerSaved.copy(
            tradingName = None
          )

        val tradingNameRow =
          rowFor(viewModel.businessDetailsSummaryList, "tradingName").value

        tradingNameRow.value.content mustBe Text(messages("partnerDetailsCheckYourAnswers.noData"))
        actionsOf(tradingNameRow) mustBe Seq(change)
      }
    }

    "NINO actions" - {

      "change only for a new partner that has not been saved" in {

        actionsOf(rowFor(soleProprietor.businessDetailsSummaryList, "nino").value) mustBe Seq(change)
      }

      "change and remove for a new partner that has been saved" in {

        val viewModel =
          soleProprietor.copy(
            maybeSubmitted = Some(true)
          )

        actionsOf(rowFor(viewModel.businessDetailsSummaryList, "nino").value) mustBe Seq(change, remove)
      }

      "nothing for an existing partner" in {

        val viewModel =
          soleProprietor.copy(
            businessNumberOrIndex = "BPN000000001",
            isNewPartnerFlow      = false,
            maybeSubmitted        = None
          )

        actionsOf(rowFor(viewModel.businessDetailsSummaryList, "nino").value) mustBe empty
      }
    }

    "yes/no rows" - {

      "are shown for a new partner that has not been saved" in {

        val tradingNameYesNo =
          rowFor(newPartnerNotSaved.businessDetailsSummaryList, "addTradingName").value

        tradingNameYesNo.value.content mustBe Text(messages("site.yes"))
      }

      "are hidden for a new partner that has been saved" in {

        rowFor(newPartnerSaved.businessDetailsSummaryList, "addTradingName") mustBe None
      }

      "are hidden for an existing partner" in {

        rowFor(existingPartner.businessDetailsSummaryList, "addTradingName") mustBe None
      }
    }

    "address rows" - {

      "escape HTML in the address" in {

        val viewModel =
          existingPartner.copy(
            address = Some(
              Address(
                address1 = "<script>alert(1)</script>",
                address2 = None,
                address3 = None,
                address4 = None,
                postcode = Some("AA1 1AA"),
                country  = None
              )
            )
          )

        val addressHtml =
          rowFor(viewModel.addressSummaryList, "address").value.value.content.asHtml.body

        addressHtml must include("&lt;script&gt;")
        addressHtml must not include "<script>"
      }

      "lock the address for an existing partner due to join or leave" in {

        val viewModel =
          existingPartner.copy(
            isDueToJoin = true
          )

        actionsOf(rowFor(viewModel.addressSummaryList, "address").value) mustBe empty
      }
    }
  }
}
