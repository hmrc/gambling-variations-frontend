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

import controllers.partner.routes
import models.BusinessType.*
import models.{Address, BusinessType, ContactNumber, UserAnswers}
import pages.partner.*
import pages.partnerdetails.*
import play.api.i18n.Messages
import play.api.libs.json.Reads
import play.api.mvc.Call
import play.twirl.api.{Html, HtmlFormat}
import queries.Gettable
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{Content, HtmlContent}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*
import utils.DateTimeFormats.shortDateDisplay
import viewmodels.govuk.all.FluentValue

import java.time.LocalDate

case class CheckPartnerDetailsViewModel(
  index: Int,
  // Business details
  typeOfBusiness: Option[BusinessType],
  businessName: Option[String], // sole proprietor's full name, or the business name for every other type
  soleProprietorDob: Option[String],
  addTradingName: Option[Boolean],
  tradingName: Option[String],
  dateOfJoining: Option[String],
  dateOfLeaving: Option[String],
  addNino: Option[Boolean],
  nino: Option[String],
  utr: Option[String],
  addVatRegistrationNumber: Option[Boolean],
  vatRegistrationNumber: Option[String],
  isIncorporatedInUk: Option[Boolean],
  countryOfIncorporation: Option[String], // non-UK corporate body
  dateOfIncorporation: Option[String], // UK corporate body, LLP
  foreignCorporateReference: Option[String], // non-UK corporate body
  companyRegistrationNumber: Option[String], // UK corporate body, LLP
  // Address
  address: Option[Address],
  addAdditionalInformation: Option[Boolean],
  additionalInformation: Option[String],
  // Contact details
  contactNumbers: Option[ContactNumber],
  addFaxNumber: Option[Boolean],
  faxNumber: Option[String],
  addEmailAddress: Option[Boolean],
  emailAddress: Option[String],
  // Conditions
  isNewPartnerFlow: Option[Boolean],
  isSubmitted: Boolean,
  isDueToLeave: Boolean,
  isDueToJoin: Boolean,
  isMissingMandatoryFields: Boolean
) {

  import CheckPartnerDetailsViewModel.NoDataActionClasses

  // --- Update this ---
  def continueCall: Call = if (isMissingMandatoryFields) {
    routes.PartnerDetailsCheckYourAnswersController.onPageLoad()
  } else {
    routes.PartnerDetailsCheckYourAnswersController.onPageLoad()
  }

  def notices(implicit messages: Messages): Seq[Html] =
    if (isNew) Nil
    else {
      val url = "" // TODO: contact-us URL
      val link = s"""<a href="$url" class="govuk-link">${messages("partnerDetailsCheckYourAnswers.error.contactUsLinkText")}</a>"""

      val leaving = dateOfLeaving
        .filter(_ => isDueToLeave)
        .map(messages("partnerDetailsCheckYourAnswers.error.cannotChangeLeaving", _, link))

      val joining = dateOfJoining
        .filter(_ => isDueToJoin)
        .map(messages("partnerDetailsCheckYourAnswers.error.cannotChangeJoining", _, link))

      val contactUs = Option.when(isSubmitted && leaving.isEmpty && joining.isEmpty)(
        messages("partnerDetailsCheckYourAnswers.error.contactUs", link)
      )

      Seq(leaving, joining, contactUs).flatten.map(Html(_))
    }

  // --- Summary lists ---

  def businessDetailsSummaryList(implicit messages: Messages): Seq[SummaryListRow] = Seq(
    Some(typeOfBusinessRow),
    businessNameRow,
    soleProprietorDobRow,
    addTradingNameRow,
    Some(tradingNameRow),
    Some(dateOfJoiningRow),
    addNinoRow,
    ninoRow,
    utrRow,
    addVatRegistrationNumberRow,
    vatRegistrationNumberRow,
    isIncorporatedInUkRow,
    countryOfIncorporationRow,
    dateOfIncorporationRow,
    foreignCorporateReferenceRow,
    companyRegistrationNumberRow
  ).flatten

  def addressSummaryList(implicit messages: Messages): Seq[SummaryListRow] = Seq(
    Some(addressRow),
    addAdditionalInformationRow,
    Some(additionalInformationRow)
  ).flatten

  def contactDetailsSummaryList(implicit messages: Messages): Seq[SummaryListRow] = Seq(
    Some(contactNumbersRow),
    addFaxNumberRow,
    Some(faxNumberRow),
    addEmailAddressRow,
    Some(emailAddressRow)
  ).flatten

  // --- Access rules ---

  private def isNew: Boolean = isNewPartnerFlow.contains(true)

  private def dueToJoinOrLeave: Boolean = isDueToJoin || isDueToLeave

  /** Mandatory fields: locked once submitted, or while the partner is due to join or leave. */
  private def editable: Boolean = isNew || (!isSubmitted && !dueToJoinOrLeave)

  /** Business name, joining date, VRN: locked once submitted only. */
  private def editableUntilSubmitted: Boolean = isNew || !isSubmitted

  /** Address and contact numbers: locked only while due to join or leave. */
  private def editableUnlessJoiningOrLeaving: Boolean = isNew || !dueToJoinOrLeave

  /** Optional fields that can also be removed outside the new-partner flow. */
  private def changeOrRemove(change: ActionItem, remove: ActionItem): Seq[ActionItem] =
    if (isNew) Seq(change)
    else if (dueToJoinOrLeave) Nil
    else Seq(change, remove)

  private def is(bt: BusinessType): Boolean = typeOfBusiness.contains(bt)

  private def isUkCorporateBody: Boolean = is(Corporatebody) && isIncorporatedInUk.contains(true)

  private def isNonUkCorporateBody: Boolean = is(Corporatebody) && isIncorporatedInUk.contains(false)

  // --- Business details rows ---

  private def typeOfBusinessRow(implicit messages: Messages): SummaryListRow = {
    val label = labelFor("typeOfBusiness")
    val change = changeAction(routes.PartnerDetailsBusinessTypeController.onPageLoad().url, label)

    typeOfBusiness match {
      case Some(bt) => createSummaryListRow(label, Text(messages(s"businessType.$bt")), if (isNew) Seq(change) else Nil)
      case None     => createSummaryListRow(label, Text("Add type of business"), Seq(change)) // TODO: move to messages
    }
  }

  private def businessNameRow(implicit messages: Messages): Option[SummaryListRow] =
    typeOfBusiness.map { bt =>
      requiredRow(
        businessNameKey(bt),
        businessName,
        routes.ChangePartnerDetailsBusinessNameController.onPageLoad(businessType = bt).url,
        editableUntilSubmitted
      )
    }

  private def businessNameKey(bt: BusinessType): String = bt match {
    case Soleproprietor              => "soleProprietorName"
    case Corporatebody               => "corporateBodyName"
    case Unincorporatedbody          => "unincorporatedBodyName"
    case Partnership                 => "partnershipName"
    case LimitedLiabilityPartnership => "llpName"
  }

  private def soleProprietorDobRow(implicit messages: Messages): Option[SummaryListRow] =
    Option.when(is(Soleproprietor))(
      requiredRow("soleProprietorDob", soleProprietorDob, routes.PartnerSoleProprietorDobController.onPageLoad().url, editable)
    )

  private def addTradingNameRow(implicit messages: Messages): Option[SummaryListRow] =
    yesNoRow("addTradingName", addTradingName, routes.PartnerDetailsAddTradingNameYesNoController.onPageLoad().url)

  private def tradingNameRow(implicit messages: Messages): SummaryListRow = {
    val label = labelFor("tradingName")
    val change = changeAction(routes.PartnerTradingNameController.onPageLoad().url, label)
    val remove = removeAction(routes.RemovePartnerTradingNameYesNoController.onPageLoad().url, label)
    val actions = if (isNew) Seq(change) else if (dueToJoinOrLeave) Nil else Seq(change, remove)

    optionalRow(label, tradingName.map(Text(_)), actions, change)
  }

  private def dateOfJoiningRow(implicit messages: Messages): SummaryListRow =
    requiredRow(
      "dateOfJoining",
      dateOfJoining,
      routes.PartnerSoleProprietorDobController.onPageLoad().url, // TODO: point at the date-of-joining page
      editableUntilSubmitted
    )

  private def addNinoRow(implicit messages: Messages): Option[SummaryListRow] =
    if (is(Soleproprietor)) yesNoRow("addNino", addNino, routes.PartnerDetailsAddNationalInsuranceNumberYesNoController.onPageLoad().url)
    else None

  private def ninoRow(implicit messages: Messages): Option[SummaryListRow] =
    Option.when(is(Soleproprietor)) {
      val label = labelFor("nino")
      val change = changeAction(routes.PartnerDetailsAddNationalInsuranceNumberController.onPageLoad().url, label)
      val remove = removeAction(routes.PartnerDetailsRemoveNationalInsuranceNumberYesNoController.onPageLoad().url, label)
      val actions = if (isNew) Seq(change) else if (!isSubmitted && !dueToJoinOrLeave) Seq(remove) else Nil

      optionalRow(label, nino.map(Text(_)), actions, change)
    }

  private def utrRow(implicit messages: Messages): Option[SummaryListRow] =
    Option.when(!is(Partnership))(
      requiredRow("utr", utr, routes.PartnerDetailsAddUTRController.onPageLoad().url, editable)
    )

  private def addVatRegistrationNumberRow(implicit messages: Messages): Option[SummaryListRow] =
    if (is(Corporatebody))
      yesNoRow("addVatRegistrationNumber", addVatRegistrationNumber, routes.VatRegistrationNumberYesNoController.onPageLoad().url)
    else None

  private def vatRegistrationNumberRow(implicit messages: Messages): Option[SummaryListRow] =
    Option.when(is(Soleproprietor)) {
      val label = labelFor("vatRegistrationNumber")
      val change = changeAction(routes.PartnerDetailsVatRegistrationNumberController.onPageLoad().url, label)
      val actions = if (editableUntilSubmitted) Seq(change) else Nil // TODO: add remove action

      optionalRow(label, vatRegistrationNumber.map(Text(_)), actions, change)
    }

  private def isIncorporatedInUkRow(implicit messages: Messages): Option[SummaryListRow] =
    Option.when(is(Corporatebody))(
      requiredRow(
        "isIncorporatedInUk",
        isIncorporatedInUk.map(b => yesNo(b)),
        routes.PartnerDetailsIsBusinessIncorporatedUkController.onPageLoad().url,
        editable
      )
    )

  private def countryOfIncorporationRow(implicit messages: Messages): Option[SummaryListRow] =
    Option.when(isNonUkCorporateBody)(
      requiredRow(
        "countryOfIncorporation",
        countryOfIncorporation,
        routes.PartnerDetailsIsBusinessIncorporatedUkController.onPageLoad().url, // TODO: point at the country-of-incorporation page
        editable
      )
    )

  private def foreignCorporateReferenceRow(implicit messages: Messages): Option[SummaryListRow] =
    Option.when(isNonUkCorporateBody)(
      requiredRow(
        "foreignCorporateReference",
        foreignCorporateReference,
        routes.PartnerDetailsForeignCorporateReferenceController.onPageLoad().url,
        editable
      )
    )

  private def dateOfIncorporationRow(implicit messages: Messages): Option[SummaryListRow] =
    Option.when(isUkCorporateBody || is(LimitedLiabilityPartnership))(
      requiredRow("dateOfIncorporation", dateOfIncorporation, routes.PartnerDateOfIncorporationController.onPageLoad().url, editable)
    )

  private def companyRegistrationNumberRow(implicit messages: Messages): Option[SummaryListRow] =
    Option.when(isUkCorporateBody || is(LimitedLiabilityPartnership))(
      requiredRow(
        "companyRegistrationNumber",
        companyRegistrationNumber,
        routes.PartnerDetailsForeignCorporateReferenceController.onPageLoad().url, // TODO: point at the CRN page
        editable
      )
    )

  // --- Address rows ---

  private def addressRow(implicit messages: Messages): SummaryListRow = {
    val label = labelFor("address")
    val url = routes.PartnerDetailsBusinessTypeController.onPageLoad().url // TODO: point at the address page

    createSummaryListRow(label, addressContent, if (editableUnlessJoiningOrLeaving) Seq(changeAction(url, label)) else Nil)
  }

  private def addAdditionalInformationRow(implicit messages: Messages): Option[SummaryListRow] =
    yesNoRow(
      "addAdditionalInformation",
      addAdditionalInformation,
      routes.PartnerDetailsAdditionalAddressInfoYesNoController.onPageLoad().url
    )

  private def additionalInformationRow(implicit messages: Messages): SummaryListRow = {
    val label = labelFor("additionalInformation")
    val change = changeAction(routes.PartnerDetailsAdditionalAddressInfoYesNoController.onPageLoad().url, label)
    val remove = removeAction(routes.RemoveAdditionalInfoForPartnerAddressYesNoController.onPageLoad().url, label)

    optionalRow(label, additionalInformation.map(Text(_)), changeOrRemove(change, remove), change)
  }

  // --- Contact details rows ---

  private def contactNumbersRow(implicit messages: Messages): SummaryListRow = {
    val label = labelFor("contactNumbers")
    val change = changeAction(routes.PartnerContactDetailsController.onPageLoad().url, label)
    val actions = if (editableUnlessJoiningOrLeaving) Seq(change) else Nil
    val row = optionalRow(label, contactNumbers.map(n => contactNumbersContent(n)), actions, change)

    if (contactNumbers.isDefined) row.copy(value = row.value.withCssClass("contact-numbers")) else row
  }

  private def addFaxNumberRow(implicit messages: Messages): Option[SummaryListRow] =
    yesNoRow("addFaxNumber", addFaxNumber, routes.PartnerAddFaxNumberYesNoController.onPageLoad().url)

  private def faxNumberRow(implicit messages: Messages): SummaryListRow = {
    val label = labelFor("faxNumber")
    val change = changeAction(routes.ChangePartnerFaxNumberController.onPageLoad().url, label)
    val remove = removeAction(routes.PartnerDetailsRemoveFaxNumberYesNoController.onPageLoad().url, label)

    optionalRow(label, faxNumber.map(Text(_)), changeOrRemove(change, remove), change)
  }

  private def addEmailAddressRow(implicit messages: Messages): Option[SummaryListRow] =
    yesNoRow("addEmailAddress", addEmailAddress, routes.PartnerAddEmailAddressYesNoPageController.onPageLoad().url)

  private def emailAddressRow(implicit messages: Messages): SummaryListRow = {
    val label = labelFor("emailAddress")
    val change = changeAction(routes.PartnerEmailAddressController.onPageLoad().url, label)
    val remove = removeAction(routes.PartnerDetailsRemoveEmailAddressYesNoController.onPageLoad().url, label)

    optionalRow(label, emailAddress.map(Text(_)), changeOrRemove(change, remove), change)
  }

  // --- Row builders ---

  private def labelFor(key: String)(implicit messages: Messages): String =
    messages(s"partnerDetailsCheckYourAnswers.$key")

  private def yesNo(value: Boolean)(implicit messages: Messages): String =
    messages(if (value) "site.yes" else "site.no")

  /** Shows the value, or an "add" link with no actions when missing. */
  private def requiredRow(key: String, value: Option[String], url: String, canChange: Boolean)(implicit
    messages: Messages
  ): SummaryListRow = {
    val label = labelFor(key)

    value match {
      case Some(v) =>
        createSummaryListRow(label, Text(v), if (canChange) Seq(changeAction(url, label)) else Nil)
      case None =>
        val addText = messages(s"partnerDetailsCheckYourAnswers.$key.add")
        createSummaryListRow(label, HtmlContent(s"""<a href="$url">$addText</a>"""), Nil)
    }
  }

  /** Shows the value, or "no data" (keeping only the change action) when missing. */
  private def optionalRow(label: String, value: Option[Content], actions: Seq[ActionItem], change: ActionItem)(implicit
    messages: Messages
  ): SummaryListRow =
    value match {
      case Some(v) =>
        createSummaryListRow(label, v, actions)
      case None =>
        createSummaryListRow(
          label,
          Text(messages("partnerDetailsCheckYourAnswers.noData")),
          actions.filter(_ == change),
          NoDataActionClasses
        )
    }

  /** Yes/no question rows, only shown in the new-partner flow. */
  private def yesNoRow(key: String, value: Option[Boolean], url: String)(implicit messages: Messages): Option[SummaryListRow] =
    if (isNew)
      value.map { v =>
        val label = labelFor(key)
        createSummaryListRow(label, Text(yesNo(v)), Seq(changeAction(url, label)))
      }
    else None

  private def createSummaryListRow(
    label: String,
    valueContent: Content,
    actions: Seq[ActionItem],
    actionClasses: String = "govuk-summary-list__actions"
  ): SummaryListRow =
    SummaryListRow(
      key     = Key(content = Text(label)),
      value   = Value(content = valueContent),
      actions = if (actions.nonEmpty) Some(Actions(items = actions, classes = actionClasses)) else None
    )

  private def changeAction(url: String, label: String)(implicit messages: Messages): ActionItem =
    buildAction(url, "site.change", label)

  private def removeAction(url: String, label: String)(implicit messages: Messages): ActionItem =
    buildAction(url, "site.remove", label)

  private def buildAction(url: String, contentKey: String, label: String)(implicit messages: Messages): ActionItem =
    ActionItem(
      href               = url,
      content            = Text(messages(contentKey)),
      visuallyHiddenText = Some(label)
    )

  // --- Complex values ---

  private def escape(s: String): String = HtmlFormat.escape(s).body

  private def addressContent: Content = HtmlContent(
    Html(
      address.fold("")(a =>
        Seq(Some(a.address1), a.address2, a.address3, a.address4, a.postcode.orElse(a.country)).flatten
          .map(escape)
          .mkString("<br>")
      )
    )
  )

  private def contactNumbersContent(numbers: ContactNumber)(implicit messages: Messages): Content = {
    val phoneBlock = numbers.phoneNumber.map(p => s"${messages("contactDetails.label.phoneNumber")}<br>${escape(p)}")
    val mobileBlock = numbers.mobilePhoneNumber.map(m => s"${messages("contactDetails.label.mobilePhoneNumber")}<br>${escape(m)}")
    val blocks = Seq(phoneBlock, mobileBlock).flatten

    if (blocks.isEmpty) Text(messages("site.notProvided"))
    else HtmlContent(Html(blocks.mkString("<br><br>")))
  }
}

object CheckPartnerDetailsViewModel {

  private val NoDataActionClasses = "govuk-summary-list__actions govuk-!-width-one-third"

  // TODO -> Update to use flag!
  def from(
    userAnswers: UserAnswers,
    index: Int,
    isNewPartnerFlow: Option[Boolean],
    isSubmitted: Boolean
  ): CheckPartnerDetailsViewModel = {

    val today = LocalDate.now()
    val businessType = userAnswers.get(PartnerDetailsBusinessTypePage(index))
    val joiningDate = userAnswers.get(PartnerDetailsDateOfJoiningPage(index))
    val leavingDate = userAnswers.get(PartnerDetailsDateOfLeavingPage(index))

    CheckPartnerDetailsViewModel(
      index                     = index,
      typeOfBusiness            = businessType,
      businessName              = businessType.flatMap(bt => businessNameFor(userAnswers, index, bt)),
      soleProprietorDob         = userAnswers.get(PartnerDetailsDateOfBirthPage(index)).map(shortDateDisplay),
      addTradingName            = userAnswers.get(PartnerDetailsAddTradingNameYesNoPage(index)),
      tradingName               = userAnswers.get(PartnerTradingNamePage).orElse(userAnswers.get(PartnerDetailsTradingNamePage(index))),
      dateOfJoining             = joiningDate.map(shortDateDisplay),
      dateOfLeaving             = leavingDate.map(shortDateDisplay),
      addNino                   = userAnswers.get(PartnerDetailsAddNationalInsuranceNumberYesNoPage(index)),
      nino                      = userAnswers.get(PartnerDetailsNinoPage(index)).map(formatNino),
      utr                       = userAnswers.get(PartnerDetailsUtrPage(index)),
      addVatRegistrationNumber  = userAnswers.get(VatRegistrationNumberYesNoPage(index)),
      vatRegistrationNumber     = userAnswers.get(PartnerDetailsVrnPage(index)),
      isIncorporatedInUk        = userAnswers.get(PartnerDetailsIsBusinessIncorporatedUkPage(index)),
      countryOfIncorporation    = userAnswers.get(PartnerDetailsCountryOfIncorporation(index)),
      dateOfIncorporation       = userAnswers.get(PartnerDetailsDateOfIncorporation(index)).map(shortDateDisplay),
      foreignCorporateReference = userAnswers.get(PartnerDetailsForeignCorporateReferencePage(index)),
      companyRegistrationNumber = userAnswers.get(PartnerDetailsCrnPage(index)),
      address                   = correspondenceAddress(userAnswers, index),
      addAdditionalInformation  = userAnswers.get(PartnerDetailsAdditionalAddressInfoYesNoPage),
      additionalInformation     = userAnswers.get(PartnerDetailsAdditionalAddressInfoPage),
      contactNumbers            = userAnswers.get(PartnerDetailsContactNumberPage(index)),
      addFaxNumber              = userAnswers.get(PartnerAddFaxNumberYesNoPage(index)),
      faxNumber                 = userAnswers.get(PartnerDetailsCorrespondenceFaxNumberPage(index)),
      addEmailAddress           = userAnswers.get(PartnerAddEmailAddressYesNoPage(index)),
      emailAddress     = userAnswers.get(PartnerEmailAddressPage).orElse(userAnswers.get(PartnerDetailsCorrespondenceEmailAddressPage(index))),
      isNewPartnerFlow = isNewPartnerFlow,
      isSubmitted      = isSubmitted,
      isDueToJoin      = joiningDate.exists(d => today.isBefore(d)),
      isDueToLeave     = leavingDate.exists(d => today.isBefore(d)),
      isMissingMandatoryFields = PartnerMandatoryFields.isMissing(userAnswers, index)
    )
  }

  private def businessNameFor(userAnswers: UserAnswers, index: Int, bt: BusinessType): Option[String] = bt match {
    case Soleproprietor => userAnswers.get(PartnerDetailsSoleProprietorPage(index)).map(_.fullName)
    case _              => userAnswers.get(PartnerDetailsBusinessNamePage(index))
  }

  private def correspondenceAddress(userAnswers: UserAnswers, index: Int): Option[Address] =
    userAnswers.get(NewPartnerDetailsCorrespondenceDetailsSectionPage(index)).flatMap(_.correspondenceAddress)

  private def formatNino(nino: String): String = {
    val clean = nino.replaceAll("[^a-zA-Z0-9]", "").toUpperCase
    clean.replaceFirst("^([A-Z]{2})([0-9]{6})([A-Z])$", "$1-$2-$3")
  }

}
