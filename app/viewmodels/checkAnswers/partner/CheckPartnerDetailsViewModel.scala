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

package viewmodels.checkAnswers.partner

import models.{Address, BusinessType, ContactNumber, UserAnswers}
import pages.partner.*
import pages.partnerdetails.*
import play.api.i18n.Messages
import play.api.mvc.Call
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{Content, HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*
import utils.DateTimeFormats.shortDateDisplay
import viewmodels.govuk.all.{FluentValue, stringToText}

case class CheckPartnerDetailsViewModel(
  index: Int,
  // Business Details
  typeOfBusiness: Option[String],
  soleProprietorName: Option[String],
  soleProprietorDob: Option[String], // soleProprietor only

  unincorporatedBodyName: Option[String],
  partnershipName: Option[String],
  llpName: Option[String],
  addTradingName: Option[String], // all business type
  tradingName: Option[String], // all business type
  dateOfJoining: Option[String], // all business type

  addNino: Option[String], // soleProprietor
  nino: Option[String], // soleProprietor

  utr: Option[String], // all business types

  addVatRegistrationNumber: Option[String], // all business type
  vatRegistrationNumber: Option[String], // all business type

  isIncorporatedInUk: Option[String], // unincorporatedBody
  countryOfIncorporation: Option[String], // unincorporatedBody non uk
  dateOfIncorporation: Option[String], // llp & unincorporatedBody uk
  foreignCorporateReference: Option[String], // unincorporatedBody non uk
  companyRegistrationNumber: Option[String], // llp & unincorporatedBody uk

  // Address
  address: Option[Address],
  addAdditionalInformation: Option[String],
  additionalInformation: Option[String],

  // Contact Details
  contactNumbers: Option[ContactNumber],
  addFaxNumber: Option[Boolean],
  faxNumber: Option[String],
  addEmailAddress: Option[Boolean],
  emailAddress: Option[String]
) {

  def isSubmitted: Boolean = true
  def continueCall: Call = controllers.partner.routes.PartnerDetailsCheckYourAnswersController.onPageLoad()

  // --- Summary Lists for View ---

  def businessDetailsSummaryList(implicit messages: Messages): Seq[SummaryListRow] = Seq(
    typeOfBusinessSummaryListRow,
    soleProprietorNameSummaryListRow,
    soleProprietorDobSummaryListRow,
    unincorporatedBodyNameSummaryListRow,
    partnershipNameSummaryListRow,
    llpNameSummaryListRow,
    addTradingNameSummaryListRow,
    tradingNameSummaryListRow,
    dateOfJoiningSummaryListRow,
    addNinoSummaryListRow,
    ninoSummaryListRow,
    utrSummaryListRow,
    addVatRegistrationNumberSummaryListRow,
    vatRegistrationNumberSummaryListRow,
    isIncorporatedInUkSummaryListRow,
    countryOfIncorporationSummaryListRow,
    dateOfIncorporationSummaryListRow,
    foreignCorporateReferenceSummaryListRow,
    companyRegistrationNumberSummaryListRow
  ).flatten

  def addressSummaryList(implicit messages: Messages): Seq[SummaryListRow] = Seq(
    Some(addressSummaryListRow),
    addAdditionalInformationSummaryListRow,
    additionalInformationSummaryListRow
  ).flatten

  def contactDetailsSummaryList(implicit messages: Messages): Seq[SummaryListRow] = Seq(
    contactNumbersSummaryListRow,
    addFaxNumberSummaryListRow,
    faxNumberSummaryListRow,
    addEmailAddressSummaryListRow,
    emailAddressSummaryListRow
  ).flatten

  // --- Business Details Rows ---

  private def typeOfBusinessSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    typeOfBusiness.map { value =>
      SummaryListRow(
        key   = Key(content = messages("partnerDetailsCheckYourAnswers.typeOfBusiness")),
        value = Value(content = value),
        actions = Some(
          Actions(
            items = Seq(
              ActionItem(
                href               = controllers.partner.routes.PartnerDetailsBusinessTypeController.onPageLoad().url,
                content            = "site.change",
                visuallyHiddenText = Some(messages("partnerDetailsCheckYourAnswers.typeOfBusiness"))
              )
            )
          )
        )
      )
    }

  private def soleProprietorNameSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    soleProprietorName.map { value =>
      SummaryListRow(
        key   = Key(content = messages("partnerDetailsCheckYourAnswers.soleProprietorName")),
        value = Value(content = value),
        actions = Some(
          Actions(
            items = Seq(
              ActionItem(
                href               = controllers.partner.routes.PartnerDetailsBusinessTypeController.onPageLoad().url,
                content            = "site.change",
                visuallyHiddenText = Some(messages("partnerDetailsCheckYourAnswers.soleProprietorName"))
              )
            )
          )
        )
      )
    }

  private def soleProprietorDobSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    soleProprietorDob.map { value =>
      SummaryListRow(
        key   = Key(content = messages("partnerDetailsCheckYourAnswers.soleProprietorDob")),
        value = Value(content = value),
        actions = Some(
          Actions(
            items = Seq(
              ActionItem(
                href               = controllers.partner.routes.PartnerSoleProprietorDobController.onPageLoad().url,
                content            = "site.change",
                visuallyHiddenText = Some(messages("partnerDetailsCheckYourAnswers.soleProprietorDob"))
              )
            )
          )
        )
      )
    }

  private def unincorporatedBodyNameSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    unincorporatedBodyName.map { value =>
      SummaryListRow(
        key   = Key(content = messages("partnerDetailsCheckYourAnswers.unincorporatedBodyName")),
        value = Value(content = value),
        actions = Some(
          Actions(
            items = Seq(
              ActionItem(
                href               = controllers.partner.routes.PartnerDetailsBusinessTypeController.onPageLoad().url,
                content            = "site.change",
                visuallyHiddenText = Some(messages("partnerDetailsCheckYourAnswers.unincorporatedBodyName"))
              )
            )
          )
        )
      )
    }

  private def partnershipNameSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    partnershipName.map { value =>
      SummaryListRow(
        key   = Key(content = messages("partnerDetailsCheckYourAnswers.partnershipName")),
        value = Value(content = value),
        actions = Some(
          Actions(
            items = Seq(
              ActionItem(
                href               = controllers.partner.routes.PartnerDetailsBusinessTypeController.onPageLoad().url,
                content            = "site.change",
                visuallyHiddenText = Some(messages("partnerDetailsCheckYourAnswers.partnershipName"))
              )
            )
          )
        )
      )
    }

  private def llpNameSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    llpName.map { value =>
      SummaryListRow(
        key   = Key(content = messages("partnerDetailsCheckYourAnswers.llpName")),
        value = Value(content = value),
        actions = Some(
          Actions(
            items = Seq(
              ActionItem(
                href               = controllers.partner.routes.PartnerDetailsBusinessTypeController.onPageLoad().url,
                content            = "site.change",
                visuallyHiddenText = Some(messages("partnerDetailsCheckYourAnswers.llpName"))
              )
            )
          )
        )
      )
    }

  private def addTradingNameSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    addTradingName.map { value =>
      SummaryListRow(
        key   = Key(content = messages("partnerDetailsCheckYourAnswers.addTradingName")),
        value = Value(content = value),
        actions = Some(
          Actions(
            items = Seq(
              ActionItem(
                href               = controllers.partner.routes.PartnerDetailsAddTradingNameYesNoController.onPageLoad().url,
                content            = "site.change",
                visuallyHiddenText = Some(messages("partnerDetailsCheckYourAnswers.addTradingName"))
              )
            )
          )
        )
      )
    }

  private def tradingNameSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    tradingName.map { value =>
      SummaryListRow(
        key   = Key(content = messages("partnerDetailsCheckYourAnswers.tradingName")),
        value = Value(content = value),
        actions = Some(
          Actions(
            items = Seq(
              ActionItem(
                href               = controllers.partner.routes.PartnerTradingNameController.onPageLoad().url,
                content            = "site.change",
                visuallyHiddenText = Some(messages("partnerDetailsCheckYourAnswers.tradingName"))
              )
            ),
            classes = "govuk-summary-list__actions govuk-!-width-one-third"
          )
        )
      )
    }

  private def dateOfJoiningSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    dateOfJoining.map { value =>
      SummaryListRow(
        key   = Key(content = messages("partnerDetailsCheckYourAnswers.dateOfJoining")),
        value = Value(content = value),
        actions = Some(
          Actions(
            items = Seq(
              ActionItem(
                href               = controllers.partner.routes.PartnerDeleteDateController.onPageLoad().url,
                content            = "site.change",
                visuallyHiddenText = Some(messages("partnerDetailsCheckYourAnswers.dateOfJoining"))
              )
            )
          )
        )
      )
    }

  private def addNinoSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    addNino.map { value =>
      SummaryListRow(
        key   = Key(content = messages("partnerDetailsCheckYourAnswers.addNino")),
        value = Value(content = value),
        actions = Some(
          Actions(
            items = Seq(
              ActionItem(
                href               = controllers.partner.routes.PartnerDetailsAddNationalInsuranceNumberYesNoController.onPageLoad().url,
                content            = "site.change",
                visuallyHiddenText = Some(messages("partnerDetailsCheckYourAnswers.addNino"))
              )
            )
          )
        )
      )
    }

  private def formatNino(nino: String): String = {
    val clean = nino.replaceAll("[^a-zA-Z0-9]", "").toUpperCase
    clean.replaceFirst("^([A-Z]{2})([0-9]{6})([A-Z])$", "$1-$2-$3")
  }

  private def ninoSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    nino.map { value =>
      SummaryListRow(
        key   = Key(content = messages("partnerDetailsCheckYourAnswers.nino")),
        value = Value(content = formatNino(value)),
        actions = Some(
          Actions(
            items = Seq(
              ActionItem(
                href               = controllers.partner.routes.PartnerDetailsAddNationalInsuranceNumberController.onPageLoad().url,
                content            = "site.change",
                visuallyHiddenText = Some(messages("partnerDetailsCheckYourAnswers.nino"))
              )
            ),
            classes = "govuk-summary-list__actions govuk-!-width-one-third"
          )
        )
      )
    }

  private def utrSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    utr.map { value =>
      SummaryListRow(
        key   = Key(content = messages("partnerDetailsCheckYourAnswers.utr")),
        value = Value(content = value),
        actions = Some(
          Actions(
            items = Seq(
              ActionItem(
                href               = controllers.partner.routes.PartnerDetailsAddUTRController.onPageLoad().url,
                content            = "site.change",
                visuallyHiddenText = Some(messages("partnerDetailsCheckYourAnswers.utr"))
              )
            )
          )
        )
      )
    }

  private def addVatRegistrationNumberSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    addVatRegistrationNumber.map { value =>
      SummaryListRow(
        key   = Key(content = messages("partnerDetailsCheckYourAnswers.addVatRegistrationNumber")),
        value = Value(content = value),
        actions = Some(
          Actions(
            items = Seq(
              ActionItem(
                href               = controllers.partner.routes.VatRegistrationNumberYesNoController.onPageLoad().url,
                content            = "site.change",
                visuallyHiddenText = Some(messages("partnerDetailsCheckYourAnswers.addVatRegistrationNumber"))
              )
            )
          )
        )
      )
    }

  private def vatRegistrationNumberSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    vatRegistrationNumber.map { value =>
      SummaryListRow(
        key   = Key(content = messages("partnerDetailsCheckYourAnswers.vatRegistrationNumber")),
        value = Value(content = value),
        actions = Some(
          Actions(
            items = Seq(
              ActionItem(
                href               = controllers.partner.routes.PartnerDetailsVatRegistrationNumberController.onPageLoad().url,
                content            = "site.change",
                visuallyHiddenText = Some(messages("partnerDetailsCheckYourAnswers.vatRegistrationNumber"))
              )
            ),
            classes = "govuk-summary-list__actions govuk-!-width-one-third"
          )
        )
      )
    }

  private def isIncorporatedInUkSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    isIncorporatedInUk.map { value =>
      SummaryListRow(
        key   = Key(content = messages("partnerDetailsCheckYourAnswers.isIncorporatedInUk")),
        value = Value(content = value),
        actions = Some(
          Actions(
            items = Seq(
              ActionItem(
                href               = controllers.partner.routes.PartnerDetailsIsBusinessIncorporatedUkController.onPageLoad().url,
                content            = "site.change",
                visuallyHiddenText = Some(messages("partnerDetailsCheckYourAnswers.isIncorporatedInUk"))
              )
            )
          )
        )
      )
    }

  private def countryOfIncorporationSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    countryOfIncorporation.map { value =>
      SummaryListRow(
        key   = Key(content = messages("partnerDetailsCheckYourAnswers.countryOfIncorporation")),
        value = Value(content = value),
        actions = Some(
          Actions(
            items = Seq(
              ActionItem(
                href               = controllers.partner.routes.PartnerDetailsIsBusinessIncorporatedUkController.onPageLoad().url,
                content            = "site.change",
                visuallyHiddenText = Some(messages("partnerDetailsCheckYourAnswers.countryOfIncorporation"))
              )
            )
          )
        )
      )
    }

  private def dateOfIncorporationSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    dateOfIncorporation.map { value =>
      SummaryListRow(
        key   = Key(content = messages("partnerDetailsCheckYourAnswers.dateOfIncorporation")),
        value = Value(content = value),
        actions = Some(
          Actions(
            items = Seq(
              ActionItem(
                href               = controllers.partner.routes.PartnerDateOfIncorporationController.onPageLoad().url,
                content            = "site.change",
                visuallyHiddenText = Some(messages("partnerDetailsCheckYourAnswers.dateOfIncorporation"))
              )
            )
          )
        )
      )
    }

  private def foreignCorporateReferenceSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    foreignCorporateReference.map { value =>
      SummaryListRow(
        key   = Key(content = messages("partnerDetailsCheckYourAnswers.foreignCorporateReference")),
        value = Value(content = value),
        actions = Some(
          Actions(
            items = Seq(
              ActionItem(
                href               = controllers.partner.routes.PartnerDetailsForeignCorporateReferenceController.onPageLoad().url,
                content            = "site.change",
                visuallyHiddenText = Some(messages("partnerDetailsCheckYourAnswers.foreignCorporateReference"))
              )
            )
          )
        )
      )
    }

  private def companyRegistrationNumberSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    companyRegistrationNumber.map { value =>
      SummaryListRow(
        key   = Key(content = messages("partnerDetailsCheckYourAnswers.companyRegistrationNumber")),
        value = Value(content = value),
        actions = Some(
          Actions(
            items = Seq(
              ActionItem(
                href               = controllers.partner.routes.PartnerDetailsIsBusinessIncorporatedUkController.onPageLoad().url,
                content            = "site.change",
                visuallyHiddenText = Some(messages("partnerDetailsCheckYourAnswers.companyRegistrationNumber"))
              )
            )
          )
        )
      )
    }

  // --- Address Rows ---

  private def addressSummaryListRow(implicit messages: Messages): SummaryListRow =
    SummaryListRow(
      key   = Key(content = messages("partnerDetailsCheckYourAnswers.address")),
      value = Value(content = addressContent),
      actions = Some(
        Actions(
          items = Seq(
            ActionItem(
              href               = controllers.partner.routes.PartnerDetailsBusinessTypeController.onPageLoad().url,
              content            = "site.change",
              visuallyHiddenText = Some(messages("partnerDetailsCheckYourAnswers.address"))
            )
          )
        )
      )
    )

  private def addAdditionalInformationSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    addAdditionalInformation.map { value =>
      SummaryListRow(
        key   = Key(content = messages("partnerDetailsCheckYourAnswers.addAdditionalInformation")),
        value = Value(content = value),
        actions = Some(
          Actions(
            items = Seq(
              ActionItem(
                href               = controllers.partner.routes.PartnerDetailsAdditionalAddressInfoYesNoController.onPageLoad().url,
                content            = "site.change",
                visuallyHiddenText = Some(messages("partnerDetailsCheckYourAnswers.addAdditionalInformation"))
              )
            )
          )
        )
      )
    }

  private def additionalInformationSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    additionalInformation.map { value =>
      SummaryListRow(
        key   = Key(content = messages("partnerDetailsCheckYourAnswers.additionalInformation")),
        value = Value(content = value),
        actions = Some(
          Actions(
            items = Seq(
              ActionItem(
                href               = controllers.partner.routes.PartnerDetailsAdditionalAddressInfoController.onPageLoad().url,
                content            = "site.change",
                visuallyHiddenText = Some(messages("partnerDetailsCheckYourAnswers.additionalInformation"))
              )
            ),
            classes = "govuk-summary-list__actions govuk-!-width-one-third"
          )
        )
      )
    }

  // --- Contact Details Rows ---

  private def contactNumbersSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    contactNumbers.map { numbers =>
      SummaryListRow(
        key   = Key(content = messages("partnerDetailsCheckYourAnswers.contactNumbers")),
        value = Value(content = contactNumbersContent(numbers)).withCssClass("contact-numbers"),
        actions = Some(
          Actions(
            items = Seq(
              ActionItem(
                href               = controllers.partner.routes.PartnerContactDetailsController.onPageLoad().url,
                content            = "site.change",
                visuallyHiddenText = Some(messages("partnerDetailsCheckYourAnswers.contactNumbers"))
              )
            )
          )
        )
      )
    }

  private def addFaxNumberSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    addFaxNumber.map { add =>
      SummaryListRow(
        key   = Key(content = messages("partnerDetailsCheckYourAnswers.addFaxNumber")),
        value = Value(content = if (add) messages("site.yes") else messages("site.no")),
        actions = Some(
          Actions(
            items = Seq(
              ActionItem(
                href               = controllers.partner.routes.PartnerAddFaxNumberYesNoController.onPageLoad().url,
                content            = "site.change",
                visuallyHiddenText = Some(messages("partnerDetailsCheckYourAnswers.addFaxNumber"))
              )
            )
          )
        )
      )
    }

  private def faxNumberSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    faxNumber.map { value =>
      SummaryListRow(
        key   = Key(content = messages("partnerDetailsCheckYourAnswers.faxNumber")),
        value = Value(content = value),
        actions = Some(
          Actions(
            items = Seq(
              ActionItem(
                href               = controllers.partner.routes.ChangePartnerFaxNumberController.onPageLoad().url,
                content            = "site.change",
                visuallyHiddenText = Some(messages("partnerDetailsCheckYourAnswers.faxNumber"))
              )
            ),
            classes = "govuk-summary-list__actions govuk-!-width-one-third"
          )
        )
      )
    }

  private def addEmailAddressSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    addEmailAddress.map { add =>
      SummaryListRow(
        key   = Key(content = messages("partnerDetailsCheckYourAnswers.addEmailAddress")),
        value = Value(content = if (add) messages("site.yes") else messages("site.no")),
        actions = Some(
          Actions(
            items = Seq(
              ActionItem(
                href               = controllers.partner.routes.PartnerAddEmailAddressYesNoPageController.onPageLoad().url,
                content            = "site.change",
                visuallyHiddenText = Some(messages("partnerDetailsCheckYourAnswers.addEmailAddress"))
              )
            )
          )
        )
      )
    }

  private def emailAddressSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    emailAddress.map { value =>
      SummaryListRow(
        key   = Key(content = messages("partnerDetailsCheckYourAnswers.emailAddress")),
        value = Value(content = value),
        actions = Some(
          Actions(
            items = Seq(
              ActionItem(
                href               = controllers.partner.routes.PartnerEmailAddressController.onPageLoad().url,
                content            = "site.change",
                visuallyHiddenText = Some(messages("partnerDetailsCheckYourAnswers.emailAddress"))
              )
            ),
            classes = "govuk-summary-list__actions govuk-!-width-one-third"
          )
        )
      )
    }

  // --- Helpers for Complex Values ---
  private def addressContent: Content = HtmlContent(
    Html(
      address.fold("")(a =>
        a.postcode match {
          case Some(_) =>
            Seq(
              Some(a.address1),
              a.address2,
              a.address3,
              a.address4,
              a.postcode
            ).flatten.mkString("<br>")
          case None =>
            Seq(
              Some(a.address1),
              a.address2,
              a.address3,
              a.address4,
              a.country
            ).flatten.mkString("<br>")
        }
      )
    )
  )

  private def contactNumbersContent(numbers: ContactNumber)(implicit messages: Messages): Content = {
    val phoneBlock = numbers.phoneNumber.map { phone =>
      s"${messages("contactDetails.label.phoneNumber")}<br>$phone"
    }
    val mobileBlock = numbers.mobilePhoneNumber.map { mobile =>
      s"${messages("contactDetails.label.mobilePhoneNumber")}<br>$mobile"
    }
    val contentBlocks = Seq(phoneBlock, mobileBlock).flatten
    if (contentBlocks.isEmpty) {
      Text(messages("site.notProvided"))
    } else {
      HtmlContent(Html(contentBlocks.mkString("<br><br>")))
    }
  }

}

object CheckPartnerDetailsViewModel {

  case class BusinessInfo(
    typeOfBusiness: Option[String] = None,
    soleProprietorName: Option[String] = None,
    soleProprietorDob: Option[String] = None,
    unincorporatedBodyName: Option[String] = None,
    partnershipName: Option[String] = None,
    llpName: Option[String] = None
  )

  private def businessTypeInfo(userAnswers: UserAnswers, index: Int)(implicit messages: Messages): Option[BusinessInfo] = {
    userAnswers.get(PartnerDetailsBusinessTypePage(index)).map { bt =>
      val typeLabel = Some(messages(s"businessType.$bt"))
      val businessName = userAnswers.get(PartnerDetailsBusinessNamePage(index))

      bt match {
        case BusinessType.Soleproprietor =>
          BusinessInfo(
            typeOfBusiness     = typeLabel,
            soleProprietorName = userAnswers.get(PartnerDetailsSoleProprietorPage(index)).map(_.fullName),
            soleProprietorDob  = userAnswers.get(PartnerDetailsDateOfBirthPage(index)).map(shortDateDisplay)
          )

        case BusinessType.Corporatebody =>
          BusinessInfo(typeOfBusiness = typeLabel)

        case BusinessType.Unincorporatedbody =>
          BusinessInfo(typeOfBusiness = typeLabel, unincorporatedBodyName = businessName)

        case BusinessType.Partnership =>
          BusinessInfo(typeOfBusiness = typeLabel, partnershipName = businessName)

        case BusinessType.LimitedLiabilityPartnership =>
          BusinessInfo(typeOfBusiness = typeLabel, llpName = businessName)
      }
    }
  }

  def from(userAnswers: UserAnswers, index: Int)(implicit messages: Messages): CheckPartnerDetailsViewModel = {
    val businessInfo = businessTypeInfo(userAnswers, index)
    CheckPartnerDetailsViewModel(
      index                     = index,
      typeOfBusiness            = businessInfo.flatMap(_.typeOfBusiness),
      soleProprietorName        = businessInfo.flatMap(_.soleProprietorName),
      soleProprietorDob         = businessInfo.flatMap(_.soleProprietorDob),
      unincorporatedBodyName    = businessInfo.flatMap(_.unincorporatedBodyName),
      partnershipName           = businessInfo.flatMap(_.partnershipName),
      llpName                   = businessInfo.flatMap(_.llpName),
      addTradingName            = userAnswers.get(PartnerDetailsAddTradingNameYesNoPage(index)).map(_.toString),
      tradingName               = userAnswers.get(PartnerTradingNamePage).orElse(userAnswers.get(PartnerDetailsTradingNamePage(index))),
      dateOfJoining             = userAnswers.get(PartnerDetailsDateOfJoiningPage(index)).map(shortDateDisplay),
      addNino                   = userAnswers.get(PartnerDetailsAddNationalInsuranceNumberYesNoPage(index)).map(_.toString),
      nino                      = userAnswers.get(PartnerDetailsNinoPage(index)),
      utr                       = userAnswers.get(PartnerDetailsUtrPage(index)),
      addVatRegistrationNumber  = userAnswers.get(VatRegistrationNumberYesNoPage(index)).map(_.toString),
      vatRegistrationNumber     = userAnswers.get(PartnerDetailsVrnPage(index)),
      isIncorporatedInUk        = userAnswers.get(PartnerDetailsIsBusinessIncorporatedUkPage(index)).map(_.toString),
      countryOfIncorporation    = userAnswers.get(PartnerDetailsCountryOfIncorporation(index)),
      dateOfIncorporation       = userAnswers.get(PartnerDetailsDateOfIncorporation(index)).map(shortDateDisplay),
      foreignCorporateReference = userAnswers.get(PartnerDetailsForeignCorporateReferencePage(index)),
      companyRegistrationNumber = userAnswers.get(PartnerDetailsCrnPage(index)),
      // Temporary stub until Address page lookup is wired up
      address                  = userAnswers.get(NewPartnerDetailsCorrespondenceDetailsSectionPage(index)).flatMap(_.correspondenceAddress),
      addAdditionalInformation = userAnswers.get(PartnerDetailsAdditionalAddressInfoYesNoPage).map(_.toString),
      additionalInformation    = userAnswers.get(PartnerDetailsAdditionalAddressInfoPage),
      contactNumbers           = userAnswers.get(PartnerDetailsContactNumberPage(index)),
      addFaxNumber             = userAnswers.get(PartnerAddFaxNumberYesNoPage(index)),
      faxNumber                = userAnswers.get(PartnerDetailsCorrespondenceFaxNumberPage(index)),
      addEmailAddress          = userAnswers.get(PartnerAddEmailAddressYesNoPage(index)),
      emailAddress             = userAnswers.get(PartnerEmailAddressPage).orElse(userAnswers.get(PartnerDetailsCorrespondenceEmailAddressPage(index)))
    )
  }
}
