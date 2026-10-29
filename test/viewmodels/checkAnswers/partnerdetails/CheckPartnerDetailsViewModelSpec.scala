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
import models.BusinessType.*
import models.{Address, ContactNumber}
import play.api.i18n.{Messages, MessagesApi}
import play.api.test.FakeRequest
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Content
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.DateTimeFormats.longDateDisplay

import java.time.LocalDate

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

  private val joiningDate = LocalDate.of(2026, 1, 10)
  private val leavingDate = LocalDate.of(2026, 12, 31)

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
      hasChanges                = None,
      hasAnsweredAllQuestions   = true
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
      addNino                   = Some(true),
      nino                      = Some("AA-000001-A"),
      addVatRegistrationNumber  = Some(true),
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
  private val cannotChangeJoining = messages("partnerDetailsCheckYourAnswers.error.cannotChangeJoining", longDateDisplay(joiningDate), contactLink)
  private val cannotChangeLeaving = messages("partnerDetailsCheckYourAnswers.error.cannotChangeLeaving", longDateDisplay(leavingDate), contactLink)

  "CheckPartnerDetailsViewModel" - {

    "notices" - {

      // --- New partners ---

      "show only missing details for a new partner with missing mandatory fields" in {

        val viewModel =
          newPartnerNotSaved.copy(
            isMissingMandatoryDetails = true
          )

        noticeBodies(viewModel) mustBe Seq(missingDetails)
      }

      "show only missing details for a saved new partner with missing mandatory fields" in {

        val viewModel =
          newPartnerSaved.copy(
            isMissingMandatoryDetails = true
          )

        noticeBodies(viewModel) mustBe Seq(missingDetails)
      }

      "show only submit changes for a complete new partner" in {

        noticeBodies(newPartnerNotSaved) mustBe Seq(missingChanges)
        noticeBodies(newPartnerSaved) mustBe Seq(missingChanges)
      }

      "ignore the changes flag for a new partner" in {

        val viewModel =
          newPartnerNotSaved.copy(
            hasChanges = Some(true)
          )

        noticeBodies(viewModel) mustBe Seq(missingChanges)
      }

      // --- Existing partners ---

      "show only contact us for an existing partner with no changes" in {

        noticeBodies(existingPartner) mustBe Seq(contactUs)
        noticeBodies(existingPartner.copy(hasChanges = Some(false))) mustBe Seq(contactUs)
      }

      "show submit changes then contact us for an existing partner who has made changes" in {

        val viewModel =
          existingPartner.copy(
            hasChanges = Some(true)
          )

        noticeBodies(viewModel) mustBe Seq(missingChanges, contactUs)
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

      "not show submit changes when due to leave, even if the partner has changes" in {

        val viewModel =
          existingPartner.copy(
            hasChanges    = Some(true),
            isDueToLeave  = true,
            dateOfLeaving = Some(leavingDate)
          )

        noticeBodies(viewModel) mustBe Seq(cannotChangeLeaving)
      }

      "not show submit changes when due to join, even if the partner has changes" in {

        val viewModel =
          existingPartner.copy(
            hasChanges  = Some(true),
            isDueToJoin = true
          )

        noticeBodies(viewModel) mustBe Seq(cannotChangeJoining)
      }

      "fall back to contact us when due to join but the joining date is missing" in {

        val viewModel =
          existingPartner.copy(
            isDueToJoin   = true,
            dateOfJoining = None
          )

        noticeBodies(viewModel) mustBe Seq(contactUs)
      }

      "not show submit changes when the join flag is set without a date, even if the partner has changes" in {

        val viewModel =
          existingPartner.copy(
            hasChanges    = Some(true),
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

    "trading name rows" - {

      "show the trading name row only when the screener answer is yes" in {

        val viewModel =
          newPartnerNotSaved.copy(addTradingName = Some(true), tradingName = Some("Trading name"))

        rowFor(viewModel.businessDetailsSummaryList, "tradingName") mustBe defined
      }

      "hide the trading name row when the screener answer is no" in {

        val viewModel =
          newPartnerNotSaved.copy(addTradingName = Some(false), tradingName = Some("Trading name"))

        rowFor(viewModel.businessDetailsSummaryList, "tradingName") mustBe None
      }

      "change only for a new partner that has not been saved" in {

        val viewModel =
          newPartnerNotSaved.copy(addTradingName = Some(true), tradingName = Some("Trading name"))

        actionsOf(rowFor(viewModel.businessDetailsSummaryList, "tradingName").value) mustBe Seq(change)
      }

      "change and remove for a saved new partner" in {

        val viewModel =
          newPartnerSaved.copy(addTradingName = Some(true), tradingName = Some("Trading name"))

        actionsOf(rowFor(viewModel.businessDetailsSummaryList, "tradingName").value) mustBe Seq(change, remove)
      }

      "lock the trading name for an existing partner due to leave" in {

        val viewModel =
          existingPartner.copy(addTradingName = Some(true), tradingName = Some("Trading name"), isDueToLeave = true)

        actionsOf(rowFor(viewModel.businessDetailsSummaryList, "tradingName").value) mustBe empty
      }
    }

    "date of joining row" - {

      "show the date of joining for an existing partner" in {

        rowFor(existingPartner.businessDetailsSummaryList, "dateOfJoining") mustBe defined
      }

      "show the date of joining for a new partner" in {

        rowFor(newPartnerNotSaved.businessDetailsSummaryList, "dateOfJoining") mustBe defined
      }

      "allow the date of joining to be changed for a new partner" in {

        actionsOf(rowFor(newPartnerNotSaved.businessDetailsSummaryList, "dateOfJoining").value) mustBe Seq(change)
      }
    }

    "NINO rows" - {

      "show the NINO screener row only for a sole proprietor" in {

        rowFor(existingPartner.businessDetailsSummaryList, "addNino") mustBe None
        rowFor(soleProprietor.businessDetailsSummaryList, "addNino") mustBe defined
      }

      "hide the NINO row when the screener answer is no" in {

        val viewModel =
          soleProprietor.copy(addNino = Some(false), nino = None)

        rowFor(viewModel.businessDetailsSummaryList, "nino") mustBe None
      }

      "change only for a new partner that has not been saved" in {

        actionsOf(rowFor(soleProprietor.businessDetailsSummaryList, "nino").value) mustBe Seq(change)
      }

      "change and remove for a saved new partner" in {

        actionsOf(rowFor(soleProprietor.copy(maybeSubmitted = Some(true)).businessDetailsSummaryList, "nino").value) mustBe Seq(change, remove)
      }

      "lock the NINO for an existing partner" in {

        val viewModel =
          soleProprietor.copy(
            businessNumberOrIndex = "BPN000000001",
            isNewPartnerFlow      = false,
            maybeSubmitted        = None
          )

        actionsOf(rowFor(viewModel.businessDetailsSummaryList, "nino").value) mustBe empty
      }
    }

    "incorporated in UK rows" - {

      "show the incorporated in UK question for a corporate body" in {

        rowFor(existingPartner.businessDetailsSummaryList, "isIncorporatedInUk") mustBe defined
      }

      "hide the incorporated in UK question for a partnership" in {

        val viewModel =
          existingPartner.copy(typeOfBusiness = Some(Partnership))

        rowFor(viewModel.businessDetailsSummaryList, "isIncorporatedInUk") mustBe None
      }
    }

    "country of incorporation and foreign reference rows" - {

      "show both rows for a non-UK corporate body" in {

        val viewModel =
          existingPartner.copy(isIncorporatedInUk = Some(false))

        keys(viewModel.businessDetailsSummaryList) must contain allOf (label("countryOfIncorporation"), label("foreignCorporateReference"))
      }

      "hide both rows for a UK corporate body" in {

        keys(existingPartner.businessDetailsSummaryList) must contain noneOf (label("countryOfIncorporation"), label("foreignCorporateReference"))
      }

      "allow the foreign corporate reference to be changed for a new partner" in {

        val viewModel =
          newPartnerNotSaved.copy(isIncorporatedInUk = Some(false), foreignCorporateReference = Some("ABC-123"))

        actionsOf(rowFor(viewModel.businessDetailsSummaryList, "foreignCorporateReference").value) mustBe Seq(change)
      }

      "lock the foreign corporate reference for an existing partner" in {

        val viewModel =
          existingPartner.copy(isIncorporatedInUk = Some(false), foreignCorporateReference = Some("ABC-123"))

        actionsOf(rowFor(viewModel.businessDetailsSummaryList, "foreignCorporateReference").value) mustBe empty
      }
    }

    "date of incorporation and company registration number rows" - {

      "show both rows for a UK corporate body" in {

        keys(existingPartner.businessDetailsSummaryList) must contain allOf (label("dateOfIncorporation"), label("companyRegistrationNumber"))
      }

      "show both rows for a limited liability partnership" in {

        val viewModel =
          existingPartner.copy(typeOfBusiness = Some(LimitedLiabilityPartnership))

        keys(viewModel.businessDetailsSummaryList) must contain allOf (label("dateOfIncorporation"), label("companyRegistrationNumber"))
      }

      "hide both rows for a partnership" in {

        val viewModel =
          existingPartner.copy(typeOfBusiness = Some(Partnership))

        keys(viewModel.businessDetailsSummaryList) must contain noneOf (label("dateOfIncorporation"), label("companyRegistrationNumber"))
      }

      "show Not provided for a missing date of incorporation when due to join" in {

        val viewModel =
          existingPartner.copy(dateOfIncorporation = None, isDueToJoin = true)

        rowFor(viewModel.businessDetailsSummaryList, "dateOfIncorporation").value.value.content mustBe
          Text(messages("partnerDetailsCheckYourAnswers.noData"))
      }
    }

    "UTR row" - {

      "show the UTR row for a corporate body" in {

        rowFor(existingPartner.businessDetailsSummaryList, "utr") mustBe defined
      }

      "show the UTR row for a sole proprietor" in {

        rowFor(soleProprietor.businessDetailsSummaryList, "utr") mustBe defined
      }

      "hide the UTR row for a partnership" in {

        val viewModel =
          existingPartner.copy(typeOfBusiness = Some(Partnership))

        rowFor(viewModel.businessDetailsSummaryList, "utr") mustBe None
      }
    }

    "VAT rows" - {

      "show the VAT screener row for a new partner that is a partnership" in {

        val viewModel =
          newPartnerNotSaved.copy(typeOfBusiness = Some(Partnership), addVatRegistrationNumber = Some(true))

        rowFor(viewModel.businessDetailsSummaryList, "addVatRegistrationNumber") mustBe defined
      }

      "hide the VAT number row when the screener answer is no" in {

        val viewModel =
          newPartnerNotSaved.copy(addVatRegistrationNumber = Some(false), vatRegistrationNumber = Some("123450002"))

        rowFor(viewModel.businessDetailsSummaryList, "vatRegistrationNumber") mustBe None
      }

      "change only for a new partner that has not been saved" in {

        val viewModel =
          newPartnerNotSaved.copy(addVatRegistrationNumber = Some(true), vatRegistrationNumber = Some("123450002"))

        actionsOf(rowFor(viewModel.businessDetailsSummaryList, "vatRegistrationNumber").value) mustBe Seq(change)
      }

      "change and remove for a saved new partner" in {

        val viewModel =
          newPartnerSaved.copy(addVatRegistrationNumber = Some(true), vatRegistrationNumber = Some("123450002"))

        actionsOf(rowFor(viewModel.businessDetailsSummaryList, "vatRegistrationNumber").value) mustBe Seq(change, remove)
      }

      "offer no actions for an existing partner's VAT number" in {

        val viewModel =
          existingPartner.copy(addVatRegistrationNumber = Some(true), vatRegistrationNumber = Some("123450002"))

        actionsOf(rowFor(viewModel.businessDetailsSummaryList, "vatRegistrationNumber").value) mustBe empty
      }
    }

    "address rows" - {

      "show the address row even when the address is missing" in {

        val viewModel =
          newPartnerNotSaved.copy(address = None)

        rowFor(viewModel.addressSummaryList, "address") mustBe defined
      }

      "allow the address to be changed for a new partner due to join or leave" in {

        val viewModel =
          newPartnerNotSaved.copy(isDueToJoin = true)

        actionsOf(rowFor(viewModel.addressSummaryList, "address").value) must not be empty
      }

      "allow the address to be changed for an existing partner not due to join or leave" in {

        actionsOf(rowFor(existingPartner.addressSummaryList, "address").value) must have size 1
      }

      "show the additional information yes/no row only when the address is answered" in {

        val viewModel =
          newPartnerNotSaved.copy(addAdditionalInformation = Some(true))

        rowFor(viewModel.addressSummaryList, "addAdditionalInformation").value.value.content mustBe Text(messages("site.yes"))
      }

      "hide the additional information yes/no row for an existing partner" in {

        rowFor(existingPartner.addressSummaryList, "addAdditionalInformation") mustBe None
      }

      "show the additional information row only when the screener answer is yes" in {

        val viewModel =
          newPartnerNotSaved.copy(addAdditionalInformation = Some(true), additionalInformation = Some("Care of Jane"))

        rowFor(viewModel.addressSummaryList, "additionalInformation") mustBe defined
      }

      "hide the additional information row when the screener answer is no" in {

        val viewModel =
          newPartnerNotSaved.copy(addAdditionalInformation = Some(false), additionalInformation = Some("Care of Jane"))

        rowFor(viewModel.addressSummaryList, "additionalInformation") mustBe None
      }

      "escape HTML in the additional information" in {

        val viewModel =
          newPartnerNotSaved.copy(addAdditionalInformation = Some(true), additionalInformation = Some("<i>care</i>"))

        val html =
          rowFor(viewModel.addressSummaryList, "additionalInformation").value.value.content.asHtml.body

        html must include("&lt;i&gt;care&lt;/i&gt;")
        html must not include "<i>care</i>"
      }

      "offer change and remove for a submitted new partner's additional information" in {

        val viewModel =
          newPartnerSaved.copy(addAdditionalInformation = Some(true), additionalInformation = Some("Care of Jane"))

        actionsOf(rowFor(viewModel.addressSummaryList, "additionalInformation").value) must have size 2
      }

      "offer change only for an unsubmitted new partner's additional information" in {

        val viewModel =
          newPartnerNotSaved.copy(addAdditionalInformation = Some(true), additionalInformation = Some("Care of Jane"))

        actionsOf(rowFor(viewModel.addressSummaryList, "additionalInformation").value) must have size 1
      }
    }

    "contact details rows" - {

      "escape HTML in the phone number" in {

        val viewModel =
          existingPartner.copy(
            contactNumbers = Some(
              ContactNumber(
                phoneNumber       = Some("<script>alert(1)</script>"),
                mobilePhoneNumber = None
              )
            )
          )

        val contactHtml =
          rowFor(viewModel.contactDetailsSummaryList, "contactNumbers").value.value.content.asHtml.body

        contactHtml must include("&lt;script&gt;")
        contactHtml must not include "<script>"
      }

      "show 'not provided' for a missing mobile number without hiding the phone number" in {

        val viewModel =
          existingPartner.copy(
            contactNumbers = Some(
              ContactNumber(
                phoneNumber       = Some("01234567"),
                mobilePhoneNumber = None
              )
            )
          )

        val contactHtml =
          rowFor(viewModel.contactDetailsSummaryList, "contactNumbers").value.value.content.asHtml.body

        contactHtml must include("01234567")
        contactHtml must include(messages("site.notProvided"))
      }

      "add the contact-numbers CSS class only when numbers exist" in {

        val withNumbers =
          existingPartner.copy(contactNumbers = Some(ContactNumber(Some("01234567890"), None)))

        val withoutNumbers =
          existingPartner.copy(contactNumbers = None)

        rowFor(withNumbers.contactDetailsSummaryList, "contactNumbers").value.value.classes    must include("contact-numbers")
        rowFor(withoutNumbers.contactDetailsSummaryList, "contactNumbers").value.value.classes must not include "contact-numbers"
      }

      "lock contact numbers for an existing partner due to join or leave" in {

        val viewModel =
          existingPartner.copy(isDueToJoin = true)

        actionsOf(rowFor(viewModel.contactDetailsSummaryList, "contactNumbers").value) mustBe empty
      }
    }

    "fax and email rows" - {

      "do not show the fax number row only when the screener answer is false" in {

        val viewModel =
          newPartnerNotSaved.copy(addFaxNumber = Some(false), faxNumber = None)

        val addFaxHtmlKey =
          rowFor(viewModel.contactDetailsSummaryList, "addFaxNumber").value.key.content.asHtml.body

        val addFaxHtmlValue =
          rowFor(viewModel.contactDetailsSummaryList, "addFaxNumber").value.value.content.asHtml.body

        val faxHtml: Option[SummaryListRow] =
          rowFor(viewModel.contactDetailsSummaryList, "faxNumber")

        addFaxHtmlKey   must include(messages("partnerDetailsCheckYourAnswers.addFaxNumber"))
        addFaxHtmlValue must include("No")
        faxHtml mustBe None

      }

      "escape HTML in the fax number" in {

        val viewModel =
          existingPartner.copy(addFaxNumber = Some(true), faxNumber = Some("<b>123</b>"))

        val faxHtml =
          rowFor(viewModel.contactDetailsSummaryList, "faxNumber").value.value.content.asHtml.body

        faxHtml must include("&lt;b&gt;123&lt;/b&gt;")
        faxHtml must not include "<b>123</b>"
      }

      "offer change and remove for a submitted new partner's fax number" in {

        val viewModel =
          existingPartner.copy(maybeSubmitted = Some(true), addFaxNumber = Some(true), faxNumber = Some("0123456789"))

        actionsOf(rowFor(viewModel.contactDetailsSummaryList, "faxNumber").value) must have size 2
      }

      "offer change only for an unsubmitted new partner's fax number" in {

        val viewModel =
          newPartnerNotSaved.copy(maybeSubmitted = Some(false), addFaxNumber = Some(true), faxNumber = Some("0123456789"))

        actionsOf(rowFor(viewModel.contactDetailsSummaryList, "faxNumber").value) must have size 1
      }

      "show the email row only when the screener answer is yes" in {

        val viewModel =
          newPartnerNotSaved.copy(addEmailAddress = Some(false), emailAddress = Some("a@example.com"))

        rowFor(viewModel.contactDetailsSummaryList, "emailAddress") mustBe None
      }
    }
  }
}
