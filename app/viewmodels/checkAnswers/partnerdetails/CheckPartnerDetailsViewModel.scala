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

import controllers.partnerdetails.*
import models.BusinessType.*
import models.{Address, BusinessType, ContactNumber, Mode, UserAnswers}
import pages.BusinessNumberOrIndex
import pages.partnerdetails.*
import play.api.i18n.Messages
import play.api.libs.json.Reads
import play.api.mvc.Call
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{Content, HtmlContent}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*
import utils.DateTimeFormats.shortDateDisplay
import utils.PartnerUtils
import viewmodels.govuk.all.FluentValue

import java.time.LocalDate

case class CheckPartnerDetailsViewModel(
  businessNumberOrIndex: BusinessNumberOrIndex,
  typeOfBusiness: Option[BusinessType],
  businessName: Option[String],
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
  countryOfIncorporation: Option[String],
  dateOfIncorporation: Option[String],
  foreignCorporateReference: Option[String],
  companyRegistrationNumber: Option[String],
  address: Option[Address],
  addAdditionalInformation: Option[Boolean],
  additionalInformation: Option[String],
  contactNumbers: Option[ContactNumber],
  addFaxNumber: Option[Boolean],
  faxNumber: Option[String],
  addEmailAddress: Option[Boolean],
  emailAddress: Option[String],
  isNewPartnerFlow: Boolean,
  maybeSubmitted: Option[Boolean],
  isDueToLeave: Boolean,
  isDueToJoin: Boolean,
  isMissingMandatoryDetails: Boolean
) {

  import CheckPartnerDetailsViewModel.NoDataActionClasses
  private val index = businessNumberOrIndex.toString
  private def mode(index: BusinessNumberOrIndex): Mode = PartnerUtils.modeFor(index)
  val isNewPartnerSubmitted: Boolean = maybeSubmitted.exists(identity) // NEW PARTNERS ONLY

  // --- Update this ---
  // NOTE: routing will be done with the integration ticket.
  def continueCall: Call = if (isMissingMandatoryDetails) {
    routes.PartnerDetailsCheckYourAnswersController.onPageLoad(index)
  } else {
    routes.PartnerDetailsCheckYourAnswersController.onPageLoad(index)
  }

  def notices(implicit messages: Messages): Seq[Html] = {
    val url = "https://www.gov.uk/find-hmrc-contacts/gambling-duties-enquiries"
    val contactUsLink = messages("partnerDetailsCheckYourAnswers.error.contactUsLinkText")
    val link =
      s"""<a href="$url" class="govuk-link" target="_blank" rel="noopener noreferrer">$contactUsLink</a>"""

    val messagesToShow =
      if (isNewPartnerFlow)
        Seq(
          Some(messages("partnerDetailsCheckYourAnswers.error.missingChanges")),
          Option.when(isMissingMandatoryDetails)(messages("partnerDetailsCheckYourAnswers.error.missingDetails"))
        ).flatten
      else {
        val leaving =
          if (isDueToLeave) dateOfLeaving.map(d => messages("partnerDetailsCheckYourAnswers.error.cannotChangeLeaving", d, link))
          else None

        val joining =
          if (isDueToJoin) dateOfJoining.map(d => messages("partnerDetailsCheckYourAnswers.error.cannotChangeJoining", d, link))
          else None

        // If due to both join and leave, show only the leaving notice (per design spec)
        leaving.orElse(joining) match {
          case Some(lockedNotice) => Seq(lockedNotice)
          case None               => Seq(messages("partnerDetailsCheckYourAnswers.error.contactUs", link))
        }
      }

    messagesToShow.map(Html(_))
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

  private def dueToJoinOrLeave: Boolean = isDueToJoin || isDueToLeave

  /** Optional fields that can also be removed outside the new-partner flow. */
  private def changeOrRemove(change: ActionItem, remove: ActionItem): Seq[ActionItem] =
    if (isNewPartnerFlow) Seq(change)
    else if (dueToJoinOrLeave) Nil
    else Seq(change, remove)

  private def is(bt: BusinessType): Boolean = typeOfBusiness.contains(bt)

  private def isUkCorporateBody: Boolean = is(Corporatebody) && isIncorporatedInUk.contains(true)

  private def isNonUkCorporateBody: Boolean = is(Corporatebody) && isIncorporatedInUk.contains(false)

  // --- Business details rows ---

  private def typeOfBusinessRow(implicit messages: Messages): SummaryListRow =
    requiredRow(
      "typeOfBusiness",
      typeOfBusiness.map(bt => messages(s"businessType.$bt")),
      // NOTE: routing will be done with the integration ticket
      routes.PartnerDetailsBusinessTypeController.onPageLoad(index, mode(businessNumberOrIndex)).url,
      isNewPartnerFlow
    )

  private def businessNameRow(implicit messages: Messages): Option[SummaryListRow] =
    typeOfBusiness.map { bt =>
      requiredRow(
        businessNameKey(bt),
        businessName,
        // NOTE: routing will be done with the integration ticket
        routes.PartnerDetailsChangeBusinessNameController.onPageLoad(index, businessType = bt, mode(businessNumberOrIndex)).url,
        isNewPartnerFlow
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
      requiredRow(
        "soleProprietorDob",
        soleProprietorDob,
        // NOTE: routing will be done with the integration ticket
        routes.PartnerDetailsSoleProprietorDobController.onPageLoad(index, mode(businessNumberOrIndex)).url,
        isNewPartnerFlow
      )
    )

  private def addTradingNameRow(implicit messages: Messages): Option[SummaryListRow] =
    // NOTE: routing will be done with the integration ticket
    yesNoRow("addTradingName", addTradingName, routes.PartnerDetailsAddTradingNameYesNoController.onPageLoad(index).url)

  private def tradingNameRow(implicit messages: Messages): SummaryListRow = {
    val label = labelFor("tradingName")
    // NOTE: routing will be done with the integration ticket
    val change = changeAction(routes.PartnerDetailsTradingNameController.onPageLoad(index, mode(businessNumberOrIndex)).url, label)
    val remove = removeAction(routes.PartnerDetailsRemovePartnerTradingNameYesNoController.onPageLoad(index, mode(businessNumberOrIndex)).url, label)

    val actions =
      if (isNewPartnerFlow && !isNewPartnerSubmitted) Seq(change)
      else if (!isNewPartnerFlow && dueToJoinOrLeave) Nil
      else Seq(change, remove)

    optionalRow(label, tradingName.map(Text(_)), actions, change)
  }

  private def dateOfJoiningRow(implicit messages: Messages): SummaryListRow =
    requiredRow(
      "dateOfJoining",
      dateOfJoining,
      // NOTE: routing will be done with the integration ticket (currently points at the DOB page placeholder)
      routes.PartnerDetailsSoleProprietorDobController.onPageLoad(index, mode(businessNumberOrIndex)).url,
      isNewPartnerFlow
    )

  private def addNinoRow(implicit messages: Messages): Option[SummaryListRow] =
    if (is(Soleproprietor))
      // NOTE: routing will be done with the integration ticket
      yesNoRow("addNino", addNino, routes.PartnerDetailsAddNationalInsuranceNumberYesNoController.onPageLoad(index).url)
    else None

  private def ninoRow(implicit messages: Messages): Option[SummaryListRow] =
    Option.when(is(Soleproprietor)) {
      val label = labelFor("nino")
      // NOTE: routing will be done with the integration ticket
      val change = changeAction(routes.PartnerDetailsAddNationalInsuranceNumberController.onPageLoad(index, mode(businessNumberOrIndex)).url, label)
      val remove =
        removeAction(routes.PartnerDetailsRemoveNationalInsuranceNumberYesNoController.onPageLoad(index, mode(businessNumberOrIndex)).url, label)

      val actions =
        if (!isNewPartnerFlow) Nil
        else if (isNewPartnerSubmitted) Seq(change, remove)
        else Seq(change)

      optionalRow(label, nino.map(Text(_)), actions, change)
    }

  private def utrRow(implicit messages: Messages): Option[SummaryListRow] =
    Option.when(!is(Partnership))(
      // NOTE: routing will be done with the integration ticket
      requiredRow("utr", utr, routes.PartnerDetailsAddUTRController.onPageLoad(index, mode(businessNumberOrIndex)).url, isNewPartnerFlow)
    )

  private def addVatRegistrationNumberRow(implicit messages: Messages): Option[SummaryListRow] =
    if (is(Corporatebody))
      // NOTE: routing will be done with the integration ticket
      yesNoRow("addVatRegistrationNumber", addVatRegistrationNumber, routes.PartnerDetailsVatRegistrationNumberYesNoController.onPageLoad(index).url)
    else None

  private def vatRegistrationNumberRow(implicit messages: Messages): Option[SummaryListRow] =
    Option.when(is(Soleproprietor)) {
      val label = labelFor("vatRegistrationNumber")
      // NOTE: routing will be done with the integration ticket
      val change = changeAction(routes.PartnerDetailsVatRegistrationNumberController.onPageLoad(index, mode(businessNumberOrIndex)).url, label)
      val remove = removeAction(routes.PartnerDetailsRemoveVatRegNumberYesNoController.onPageLoad(index, mode(businessNumberOrIndex)).url, label)

      val actions =
        if (!isNewPartnerFlow) Nil
        else if (isNewPartnerSubmitted) Seq(change, remove)
        else Seq(change)

      optionalRow(label, vatRegistrationNumber.map(Text(_)), actions, change)
    }

  private def isIncorporatedInUkRow(implicit messages: Messages): Option[SummaryListRow] =
    Option.when(is(Corporatebody))(
      requiredRow(
        "isIncorporatedInUk",
        isIncorporatedInUk.map(b => yesNo(b)),
        // NOTE: routing will be done with the integration ticket
        routes.PartnerDetailsIsBusinessIncorporatedUkController.onPageLoad(index, mode(businessNumberOrIndex)).url,
        isNewPartnerFlow
      )
    )

  private def countryOfIncorporationRow(implicit messages: Messages): Option[SummaryListRow] =
    Option.when(isNonUkCorporateBody)(
      requiredRow(
        "countryOfIncorporation",
        countryOfIncorporation,
        // NOTE: routing will be done with the integration ticket (currently points at the "incorporated in UK" page placeholder)
        routes.PartnerDetailsIsBusinessIncorporatedUkController.onPageLoad(index, mode(businessNumberOrIndex)).url,
        isNewPartnerFlow
      )
    )

  private def foreignCorporateReferenceRow(implicit messages: Messages): Option[SummaryListRow] =
    Option.when(isNonUkCorporateBody)(
      requiredRow(
        "foreignCorporateReference",
        foreignCorporateReference,
        // NOTE: routing will be done with the integration ticket
        routes.PartnerDetailsForeignCorporateReferenceController.onPageLoad(index, mode(businessNumberOrIndex)).url,
        isNewPartnerFlow
      )
    )

  private def dateOfIncorporationRow(implicit messages: Messages): Option[SummaryListRow] =
    Option.when(isUkCorporateBody || is(LimitedLiabilityPartnership))(
      requiredRow(
        "dateOfIncorporation",
        dateOfIncorporation,
        // NOTE: routing will be done with the integration ticket
        routes.PartnerDetailsDateOfIncorporationController.onPageLoad(index, mode(businessNumberOrIndex)).url,
        isNewPartnerFlow
      )
    )

  private def companyRegistrationNumberRow(implicit messages: Messages): Option[SummaryListRow] =
    Option.when(isUkCorporateBody || is(LimitedLiabilityPartnership))(
      requiredRow(
        "companyRegistrationNumber",
        companyRegistrationNumber,
        // NOTE: routing will be done with the integration ticket (currently points at the foreign corporate reference page placeholder)
        routes.PartnerDetailsForeignCorporateReferenceController.onPageLoad(index, mode(businessNumberOrIndex)).url,
        isNewPartnerFlow
      )
    )

  // --- Address rows ---

  private def addressRow(implicit messages: Messages): SummaryListRow = {
    val label = labelFor("address")
    // NOTE: routing will be done with the integration ticket (currently points at the business type page placeholder)
    val url = routes.PartnerDetailsBusinessTypeController.onPageLoad(index, mode(businessNumberOrIndex)).url

    createSummaryListRow(label, addressContent, if (isNewPartnerFlow || !dueToJoinOrLeave) Seq(changeAction(url, label)) else Nil)
  }

  private def addAdditionalInformationRow(implicit messages: Messages): Option[SummaryListRow] =
    yesNoRow(
      "addAdditionalInformation",
      addAdditionalInformation,
      // NOTE: routing will be done with the integration ticket
      routes.PartnerDetailsAdditionalAddressInfoYesNoController.onPageLoad(index).url
    )

  private def additionalInformationRow(implicit messages: Messages): SummaryListRow = {
    val label = labelFor("additionalInformation")
    // NOTE: routing will be done with the integration ticket
    val change = changeAction(routes.PartnerDetailsAdditionalAddressInfoYesNoController.onPageLoad(index).url, label)
    val remove = removeAction(
      routes.PartnerDetailsRemoveAdditionalInfoForPartnerAddressYesNoController.onPageLoad(index, mode(businessNumberOrIndex)).url,
      label
    )

    optionalRow(label, additionalInformation.map(Text(_)), changeOrRemove(change, remove), change)
  }

  // --- Contact details rows ---

  private def contactNumbersRow(implicit messages: Messages): SummaryListRow = {
    val label = labelFor("contactNumbers")
    // NOTE: routing will be done with the integration ticket
    val change = changeAction(routes.PartnerDetailsContactDetailsController.onPageLoad(index, mode(businessNumberOrIndex)).url, label)
    val actions = if (isNewPartnerFlow || !dueToJoinOrLeave) Seq(change) else Nil
    val row = optionalRow(label, contactNumbers.map(n => contactNumbersContent(n)), actions, change)

    if (contactNumbers.isDefined) row.copy(value = row.value.withCssClass("contact-numbers")) else row
  }

  private def addFaxNumberRow(implicit messages: Messages): Option[SummaryListRow] =
    // NOTE: routing will be done with the integration ticket
    yesNoRow("addFaxNumber", addFaxNumber, routes.PartnerDetailsAddFaxNumberYesNoController.onPageLoad(index).url)

  private def faxNumberRow(implicit messages: Messages): SummaryListRow = {
    val label = labelFor("faxNumber")
    // NOTE: routing will be done with the integration ticket
    val change = changeAction(routes.PartnerDetailsChangePartnerFaxNumberController.onPageLoad(index, mode(businessNumberOrIndex)).url, label)
    val remove = removeAction(routes.PartnerDetailsRemoveFaxNumberYesNoController.onPageLoad(index, mode(businessNumberOrIndex)).url, label)

    optionalRow(label, faxNumber.map(Text(_)), changeOrRemove(change, remove), change)
  }

  private def addEmailAddressRow(implicit messages: Messages): Option[SummaryListRow] =
    // NOTE: routing will be done with the integration ticket
    yesNoRow("addEmailAddress", addEmailAddress, routes.PartnerDetailsAddEmailAddressYesNoPageController.onPageLoad(index).url)

  private def emailAddressRow(implicit messages: Messages): SummaryListRow = {
    val label = labelFor("emailAddress")
    // NOTE: routing will be done with the integration ticket
    val change = changeAction(routes.PartnerDetailsEmailAddressController.onPageLoad(index, mode(businessNumberOrIndex)).url, label)
    val remove = removeAction(routes.PartnerDetailsRemoveEmailAddressYesNoController.onPageLoad(index, mode(businessNumberOrIndex)).url, label)

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
    if (isNewPartnerFlow && !isNewPartnerSubmitted)
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

  def from(
    userAnswers: UserAnswers,
    index: BusinessNumberOrIndex,
    isNewPartnerFlow: Boolean,
    isSubmitted: Option[Boolean]
  ): CheckPartnerDetailsViewModel = {

    val businessType = userAnswers.get(PartnerDetailsBusinessTypePage(index))

    CheckPartnerDetailsViewModel(
      businessNumberOrIndex     = index,
      typeOfBusiness            = businessType,
      businessName              = businessType.flatMap(bt => businessNameFor(userAnswers, index, bt)),
      soleProprietorDob         = userAnswers.get(PartnerDetailsDateOfBirthPage(index)).map(shortDateDisplay),
      addTradingName            = userAnswers.get(PartnerDetailsAddTradingNameYesNoPage(index)),
      tradingName               = userAnswers.get(PartnerDetailsTradingNamePage(index)),
      dateOfJoining             = userAnswers.get(PartnerDetailsDateOfJoiningPage(index)).map(shortDateDisplay),
      dateOfLeaving             = userAnswers.get(PartnerDetailsDateOfLeavingPage(index)).map(shortDateDisplay),
      addNino                   = userAnswers.get(PartnerDetailsAddNationalInsuranceNumberYesNoPage(index)),
      nino                      = userAnswers.get(PartnerDetailsNinoPage(index)).map(formatNino),
      utr                       = userAnswers.get(PartnerDetailsUtrPage(index)),
      addVatRegistrationNumber  = userAnswers.get(PartnerDetailsVatRegistrationNumberYesNoPage(index)),
      vatRegistrationNumber     = userAnswers.get(PartnerDetailsVrnPage(index)),
      isIncorporatedInUk        = userAnswers.get(PartnerDetailsIsBusinessIncorporatedUkPage(index)),
      countryOfIncorporation    = userAnswers.get(PartnerDetailsCountryOfIncorporationPage(index)),
      dateOfIncorporation       = userAnswers.get(PartnerDetailsDateOfIncorporation(index)).map(shortDateDisplay),
      foreignCorporateReference = userAnswers.get(PartnerDetailsForeignCorporateReferencePage(index)),
      companyRegistrationNumber = userAnswers.get(PartnerDetailsCrnPage(index)),
      address                   = correspondenceAddress(userAnswers, index),
      addAdditionalInformation  = userAnswers.get(PartnerDetailsAdditionalAddressInfoYesNoPage(index)),
      additionalInformation     = userAnswers.get(PartnerDetailsAdditionalAddressInfoPage(index)),
      contactNumbers            = userAnswers.get(PartnerDetailsContactNumberPage(index)),
      addFaxNumber              = userAnswers.get(PartnerDetailsAddFaxNumberYesNoPage(index)),
      faxNumber                 = userAnswers.get(PartnerDetailsCorrespondenceFaxNumberPage(index)),
      addEmailAddress           = userAnswers.get(PartnerDetailsAddEmailAddressYesNoPage(index)),
      emailAddress              = userAnswers.get(PartnerDetailsCorrespondenceEmailAddressPage(index)),
      isNewPartnerFlow          = isNewPartnerFlow,
      maybeSubmitted            = isSubmitted,
      isDueToLeave              = userAnswers.get(PartnerDetailsIsFutureLeaveDatePage(index)).contains(1),
      isDueToJoin               = userAnswers.get(PartnerDetailsIsFutureJoinDatePage(index)).contains(1),
      isMissingMandatoryDetails = PartnerMandatoryDetails.isMissing(userAnswers, index)
    )
  }

  private def businessNameFor(userAnswers: UserAnswers, index: BusinessNumberOrIndex, bt: BusinessType): Option[String] = bt match {
    case Soleproprietor => userAnswers.get(PartnerDetailsSoleProprietorPage(index)).map(_.fullName)
    case _              => userAnswers.get(PartnerDetailsBusinessNamePage(index))
  }

  private def correspondenceAddress(userAnswers: UserAnswers, index: BusinessNumberOrIndex): Option[Address] =
    userAnswers.get(PartnerDetailsNewCorrespondenceDetailsSectionPage(index)).flatMap(_.correspondenceAddress)

  private def formatNino(nino: String): String = {
    val clean = nino.replaceAll("[^a-zA-Z0-9]", "").toUpperCase
    clean.replaceFirst("^([A-Z]{2})([0-9]{6})([A-Z])$", "$1-$2-$3")
  }

}

sealed trait Partner {
  def ref: BusinessNumberOrIndex
  def isNew: Boolean
  def isSubmitted: Option[Boolean]
}

final case class NewPartner(index: Int, isSubmitted: Option[Boolean]) extends Partner {
  val ref: BusinessNumberOrIndex = index
  val isNew: Boolean = true
}

final case class ExistingPartner(businessPartnerNumber: String) extends Partner {
  val ref: BusinessNumberOrIndex = businessPartnerNumber
  val isNew: Boolean = false
  val isSubmitted: Option[Boolean] = None
}

object Partner {
  def indexParser(index: String, userAnswers: UserAnswers): Either[String, Partner] =
    if ((userAnswers.data \ "partners" \ index).toOption.isDefined)
      Right(ExistingPartner(index))
    else if (index.matches("\\d{1,2}")) {
      val i = index.toInt
      Right(NewPartner(i, userAnswers.get(PartnerDetailsAddPartnerCompletedPage(i))))
    } else
      Left(s"Invalid partner reference: $index")

}
