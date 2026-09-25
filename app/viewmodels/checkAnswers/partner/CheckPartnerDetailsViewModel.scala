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
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{Content, HtmlContent}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*
import utils.DateTimeFormats.shortDateDisplay
import viewmodels.govuk.all.FluentValue

import java.time.LocalDate

case class CheckPartnerDetailsViewModel(
  index: Int,
  // Business Details
  typeOfBusiness: Option[String],
  soleProprietorName: Option[String],
  soleProprietorDob: Option[String], // soleProprietor only

  unincorporatedBodyName: Option[String],
  corporateBodyName: Option[String],
  partnershipName: Option[String],
  llpName: Option[String],
  addTradingName: Option[String], // all business type
  tradingName: Option[String], // all business type
  dateOfJoining: Option[String], // all business type
  dateOfLeaving: Option[String], // all business type

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
  emailAddress: Option[String],

  // conditions
  isNewPartnerFlow: Option[Boolean],
  isSubmitted: Boolean,
  isDueToLeave: Boolean,
  isDueToJoin: Boolean
) {

  // --- Update this ---
  def continueCall: Call = controllers.partner.routes.PartnerDetailsCheckYourAnswersController.onPageLoad()

  // --- Summary Lists for View ---

  def businessDetailsSummaryList(implicit messages: Messages): Seq[SummaryListRow] = Seq(
    typeOfBusinessSummaryListRow,
    soleProprietorNameSummaryListRow,
    soleProprietorDobSummaryListRow,
    corporateBodyNameSummaryListRow,
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

  private def typeOfBusinessSummaryListRow(implicit messages: Messages): Option[SummaryListRow] = {
    val label = messages("partnerDetailsCheckYourAnswers.typeOfBusiness")
    val url = controllers.partner.routes.PartnerDetailsBusinessTypeController.onPageLoad().url
    val changeAction = buildAction(url, "site.change", label)

    typeOfBusiness match {
      case Some(value) if isNewPartnerFlow.contains(true) =>
        Some(createSummaryListRow(label, Text(value), Seq(changeAction)))
      case Some(value) if isDueToJoin || isDueToLeave =>
        Some(createSummaryListRow(label, Text(value), Seq()))
      case _ =>
        Some(createSummaryListRow(label, Text("Add type of business"), Seq(changeAction)))
    }
  }

  private def soleProprietorNameSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    if (typeOfBusiness.contains(messages("businessType.soleproprietor"))) {
      mandatoryFieldRow(
        soleProprietorName,
        "partnerDetailsCheckYourAnswers.soleProprietorName",
        "partnerDetailsCheckYourAnswers.soleProprietorName.add",
        controllers.partner.routes.PartnerSoleProprietorDobController.onPageLoad().url
      )
    } else None

  private def unincorporatedBodyNameSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    if (typeOfBusiness.contains(messages("businessType.unincorporatedbody"))) {
      mandatoryFieldRow(
        unincorporatedBodyName,
        "partnerDetailsCheckYourAnswers.unincorporatedBodyName",
        "partnerDetailsCheckYourAnswers.unincorporatedBodyName.add",
        controllers.partner.routes.PartnerDetailsBusinessTypeController.onPageLoad().url
      )
    } else None

  private def corporateBodyNameSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    if (typeOfBusiness.contains(messages("businessType.corporatebody"))) {
      mandatoryFieldRow(
        corporateBodyName,
        "partnerDetailsCheckYourAnswers.corporateBodyName",
        "partnerDetailsCheckYourAnswers.corporateBodyName.add",
        controllers.partner.routes.PartnerDetailsBusinessTypeController.onPageLoad().url
      )
    } else None

  private def partnershipNameSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    if (typeOfBusiness.contains(messages("businessType.partnership"))) {
      mandatoryFieldRow(
        partnershipName,
        "partnerDetailsCheckYourAnswers.partnershipName",
        "partnerDetailsCheckYourAnswers.partnershipName.add",
        controllers.partner.routes.PartnerDetailsBusinessTypeController.onPageLoad().url
      )
    } else None

  private def llpNameSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    if (typeOfBusiness.contains(messages("businessType.llp"))) {
      mandatoryFieldRow(
        llpName,
        "partnerDetailsCheckYourAnswers.llpName",
        "partnerDetailsCheckYourAnswers.llpName.add",
        controllers.partner.routes.PartnerDetailsBusinessTypeController.onPageLoad().url
      )
    } else None

  private def soleProprietorDobSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    if (typeOfBusiness.contains(messages("businessType.soleproprietor"))) {
      mandatoryFieldRow(
        soleProprietorDob,
        "partnerDetailsCheckYourAnswers.soleProprietorDob",
        "partnerDetailsCheckYourAnswers.soleProprietorDob.add",
        controllers.partner.routes.PartnerSoleProprietorDobController.onPageLoad().url
      )
    } else None

  private def addNinoSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    if (typeOfBusiness.contains(messages("businessType.soleproprietor")) && isNewPartnerFlow.contains(true)) {
      addNino.map { value =>
        val label = messages("partnerDetailsCheckYourAnswers.addNino")
        val url = controllers.partner.routes.PartnerDetailsAddNationalInsuranceNumberYesNoController.onPageLoad().url
        createSummaryListRow(label, Text(value), Seq(buildAction(url, "site.change", label)))
      }
    } else None

  private def ninoSummaryListRow(implicit messages: Messages): Option[SummaryListRow] = {
    val label = messages("partnerDetailsCheckYourAnswers.nino")
    val url = controllers.partner.routes.PartnerDetailsBusinessTypeController.onPageLoad().url
    val changeAction = buildAction(url, "site.change", label)
    val removeAction = buildAction(url, "site.remove", label)

    if (typeOfBusiness.contains(messages("businessType.soleproprietor"))) {
      nino.map { value =>

        val actions = (isNewPartnerFlow.contains(true), isSubmitted, isDueToJoin, isDueToLeave) match {
          case (_, _, b1, b2) if b1 || b2 => Nil
          case (true, false, _, _)        => Seq(changeAction, removeAction)
          case (true, true, _, _)         => Seq(changeAction)
          case (false, _, _, _)           => Nil
        }

        createSummaryListRow(
          label,
          Text(formatNino(value)),
          actions,
          "govuk-summary-list__actions govuk-!-width-one-third"
        )
      } orElse Some(
        createSummaryListRow(
          label,
          Text(messages("partnerDetailsCheckYourAnswers.noData")),
          Seq(changeAction),
          "govuk-summary-list__actions govuk-!-width-one-third"
        )
      )
    } else None
  }

  private def addTradingNameSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    if (isNewPartnerFlow.contains(true)) {
      addTradingName.map { value =>
        val label = messages("partnerDetailsCheckYourAnswers.addTradingName")
        val url = controllers.partner.routes.PartnerDetailsAddTradingNameYesNoController.onPageLoad().url
        createSummaryListRow(label, Text(value), Seq(buildAction(url, "site.change", label)))
      }
    } else None

  private def tradingNameSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    tradingName.map { value =>
      val label = messages("partnerDetailsCheckYourAnswers.tradingName")
      val changeUrl = controllers.partner.routes.PartnerDetailsBusinessTypeController.onPageLoad().url

      val changeAction = buildAction(changeUrl, "site.change", label)
      val removeAction = if (!isNewPartnerFlow.contains(true)) Some(buildAction(changeUrl, "site.remove", label)) else None

      createSummaryListRow(label, Text(value), changeAction +: removeAction.toList)
    }

  private def isIncorporatedInUkSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    if (typeOfBusiness.contains(messages("businessType.corporatebody"))) {
      isIncorporatedInUk.map { value =>
        val label = messages("partnerDetailsCheckYourAnswers.isIncorporatedInUk")
        val url = controllers.partner.routes.PartnerDetailsIsBusinessIncorporatedUkController.onPageLoad().url
        createSummaryListRow(label, Text(value), Seq(buildAction(url, "site.change", label)))
      }
    } else None

  private def countryOfIncorporationSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    if (typeOfBusiness.contains(messages("businessType.corporatebody")) && isIncorporatedInUk.contains("no")) {
      countryOfIncorporation.map { value =>
        val label = messages("partnerDetailsCheckYourAnswers.countryOfIncorporation")
        val url = controllers.partner.routes.PartnerDetailsIsBusinessIncorporatedUkController.onPageLoad().url
        createSummaryListRow(label, Text(value), Seq(buildAction(url, "site.change", label)))
      }
    } else None

  private def foreignCorporateReferenceSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    if (typeOfBusiness.contains(messages("businessType.corporatebody")) && isIncorporatedInUk.contains("no")) {
      foreignCorporateReference.map { value =>
        val label = messages("partnerDetailsCheckYourAnswers.foreignCorporateReference")
        val url = controllers.partner.routes.PartnerDetailsForeignCorporateReferenceController.onPageLoad().url

        val actions = if (isNewPartnerFlow.contains(true)) Seq(buildAction(url, "site.change", label)) else Nil
        createSummaryListRow(label, Text(value), actions)
      }
    } else None

  private def dateOfIncorporationSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    if (
      typeOfBusiness.contains(messages("businessType.corporatebody")) && isIncorporatedInUk
        .contains("yes") || typeOfBusiness.contains(messages("businessType.llp"))
    ) {
      dateOfIncorporation.map { value =>
        val label = messages("partnerDetailsCheckYourAnswers.dateOfIncorporation")
        val url = controllers.partner.routes.PartnerDateOfIncorporationController.onPageLoad().url

        val actions = if (isNewPartnerFlow.contains(true)) Seq(buildAction(url, "site.change", label)) else Nil
        createSummaryListRow(label, Text(value), actions)
      }
    } else None

  private def companyRegistrationNumberSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    if (
      typeOfBusiness.contains(messages("businessType.corporatebody")) && isIncorporatedInUk
        .contains("yes") || typeOfBusiness.contains(messages("businessType.llp"))
    ) {
      companyRegistrationNumber.map { value =>
        val label = messages("partnerDetailsCheckYourAnswers.companyRegistrationNumber")
        val url = controllers.partner.routes.PartnerDetailsForeignCorporateReferenceController.onPageLoad().url

        val actions = if (isNewPartnerFlow.contains(true)) Seq(buildAction(url, "site.change", label)) else Nil
        createSummaryListRow(label, Text(value), actions)
      }
    } else None

  private def utrSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    mandatoryFieldRow(
      utr,
      "partnerDetailsCheckYourAnswers.utr",
      "partnerDetailsCheckYourAnswers.utr.add",
      controllers.partner.routes.PartnerDetailsAddUTRController.onPageLoad().url
    )

  private def addVatRegistrationNumberSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    if (typeOfBusiness.contains(messages("businessType.corporatebody")) && isNewPartnerFlow.contains(true)) {
      addVatRegistrationNumber.map { value =>
        val label = messages("partnerDetailsCheckYourAnswers.addVatRegistrationNumber")
        val url = controllers.partner.routes.VatRegistrationNumberYesNoController.onPageLoad().url
        createSummaryListRow(label, Text(value), Seq(buildAction(url, "site.change", label)))
      }
    } else None

  private def vatRegistrationNumberSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    if (typeOfBusiness.contains(messages("businessType.corporatebody"))) {
      vatRegistrationNumber.map { value =>
        val label = messages("partnerDetailsCheckYourAnswers.vatRegistrationNumber")

        val changeAction = if (isNewPartnerFlow.contains(true)) {
          Some(buildAction(controllers.partner.routes.PartnerDetailsVatRegistrationNumberController.onPageLoad().url, "site.change", label))
        } else None

        val removeAction = if (!isNewPartnerFlow.contains(true)) {
          Some(buildAction(controllers.partner.routes.PartnerDetailsRemoveVatRegNumberYesNoController.onPageLoad().url, "site.remove", label))
        } else None

        createSummaryListRow(label, Text(value), Seq(changeAction, removeAction).flatten)
      }
    } else None

  private def dateOfJoiningSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    dateOfJoining.map { value =>
      val label = messages("partnerDetailsCheckYourAnswers.dateOfJoining")
      val url = controllers.partner.routes.PartnerDetailsBusinessTypeController.onPageLoad().url // TODO: change this

      val actions = if (isNewPartnerFlow.contains(true)) Seq(buildAction(url, "site.change", label)) else Nil
      createSummaryListRow(label, Text(value), actions)
    }

  // --- Address Rows ---

  private def addressSummaryListRow(implicit messages: Messages): SummaryListRow = {
    val label = messages("partnerDetailsCheckYourAnswers.address")
    val url = controllers.partner.routes.PartnerDetailsBusinessTypeController.onPageLoad().url // TODO: change this
    createSummaryListRow(label, addressContent, Seq(buildAction(url, "site.change", label)))
  }

  private def addAdditionalInformationSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    if (isNewPartnerFlow.contains(true)) {
      addAdditionalInformation.map { value =>
        val label = messages("partnerDetailsCheckYourAnswers.addAdditionalInformation")
        val url = controllers.partner.routes.PartnerDetailsAdditionalAddressInfoYesNoController.onPageLoad().url
        createSummaryListRow(label, Text(value), Seq(buildAction(url, "site.change", label)))
      }
    } else None

  private def additionalInformationSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    additionalInformation.map { value =>
      val label = messages("partnerDetailsCheckYourAnswers.additionalInformation")

      val changeAction =
        buildAction(controllers.partner.routes.PartnerDetailsAdditionalAddressInfoYesNoController.onPageLoad().url, "site.change", label)
      val removeAction = if (!isNewPartnerFlow.contains(true)) {
        Some(buildAction(controllers.partner.routes.RemoveAdditionalInfoForPartnerAddressYesNoController.onPageLoad().url, "site.remove", label))
      } else None

      createSummaryListRow(label, Text(value), changeAction +: removeAction.toList)
    }

  // --- Contact Details Rows ---

  private def contactNumbersSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    contactNumbers.map { numbers =>
      val label = messages("partnerDetailsCheckYourAnswers.contactNumbers")
      val url = controllers.partner.routes.PartnerContactDetailsController.onPageLoad().url

      val row = createSummaryListRow(label, contactNumbersContent(numbers), Seq(buildAction(url, "site.change", label)))
      row.copy(value = row.value.withCssClass("contact-numbers"))
    }

  private def addFaxNumberSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    if (isNewPartnerFlow.contains(true)) {
      addFaxNumber.map { add =>
        val label = messages("partnerDetailsCheckYourAnswers.addFaxNumber")
        val url = controllers.partner.routes.PartnerAddFaxNumberYesNoController.onPageLoad().url
        val value = if (add) messages("site.yes") else messages("site.no")

        createSummaryListRow(label, Text(value), Seq(buildAction(url, "site.change", label)))
      }
    } else None

  private def faxNumberSummaryListRow(implicit messages: Messages): Option[SummaryListRow] = {
    val label = messages("partnerDetailsCheckYourAnswers.faxNumber")
    val changeAction = buildAction(controllers.partner.routes.ChangePartnerFaxNumberController.onPageLoad().url, "site.change", label)
    val removeAction = buildAction(controllers.partner.routes.PartnerDetailsRemoveFaxNumberYesNoController.onPageLoad().url, "site.remove", label)

    val actions = (isNewPartnerFlow.contains(true), isSubmitted, isDueToJoin, isDueToLeave) match {
      case (_, _, b1, b2) if b1 || b2 => Nil
      case (true, _, _, _)            => Seq(changeAction)
      case (false, _, _, _)           => Seq(removeAction)
    }

    faxNumber.map(value => createSummaryListRow(label, Text(value), actions)) orElse Some(
      createSummaryListRow(
        label,
        Text(messages("partnerDetailsCheckYourAnswers.noData")),
        Seq(changeAction),
        "govuk-summary-list__actions govuk-!-width-one-third"
      )
    )
  }

  private def addEmailAddressSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    if (isNewPartnerFlow.contains(true)) {
      addEmailAddress.map { add =>
        val label = messages("partnerDetailsCheckYourAnswers.addEmailAddress")
        val url = controllers.partner.routes.PartnerAddEmailAddressYesNoPageController.onPageLoad().url
        val value = if (add) messages("site.yes") else messages("site.no")

        createSummaryListRow(label, Text(value), Seq(buildAction(url, "site.change", label)))
      }
    } else None

  private def emailAddressSummaryListRow(implicit messages: Messages): Option[SummaryListRow] = {
    val label = messages("partnerDetailsCheckYourAnswers.emailAddress")
    val changeAction = buildAction(controllers.partner.routes.PartnerEmailAddressController.onPageLoad().url, "site.change", label)
    val removeAction = buildAction(controllers.partner.routes.PartnerDetailsRemoveEmailAddressYesNoController.onPageLoad().url, "site.remove", label)

    val actions = (isNewPartnerFlow.contains(true), isSubmitted, isDueToJoin, isDueToLeave) match {
      case (_, _, b1, b2) if b1 || b2 => Nil
      case (true, _, _, _)            => Seq(changeAction)
      case (false, _, _, _)           => Seq(removeAction)
    }

    emailAddress.map { value =>

      createSummaryListRow(label, Text(value), actions)
    } orElse Some(
      createSummaryListRow(
        label,
        Text(messages("partnerDetailsCheckYourAnswers.noData")),
        Seq(changeAction),
        "govuk-summary-list__actions govuk-!-width-one-third"
      )
    )
  }

  // --- Row Construction Helpers ---

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

  private def buildAction(url: String, contentKey: String, label: String)(implicit messages: Messages): ActionItem =
    ActionItem(
      href               = url,
      content            = Text(messages(contentKey)),
      visuallyHiddenText = Some(label)
    )

  // --- Handles rows that must show a value if present, or an "Add [Field]" link if missing. ---
  private def mandatoryFieldRow(
    fieldValue: Option[String],
    labelKey: String,
    addMessageKey: String,
    url: String
  )(implicit messages: Messages): Option[SummaryListRow] = {
    val label = messages(labelKey)

    fieldValue match {
      case Some(value) =>
        val actions = if (isNewPartnerFlow.contains(true)) Seq(buildAction(url, "site.change", label)) else Nil
        Some(createSummaryListRow(label, Text(value), actions))

      case None =>
        val addText = messages(addMessageKey)
        val addLinkHtml = s"""<a href="$url">$addText</a>"""
        Some(createSummaryListRow(label, HtmlContent(addLinkHtml), Nil))
    }
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

  private def formatNino(nino: String): String = {
    val clean = nino.replaceAll("[^a-zA-Z0-9]", "").toUpperCase
    clean.replaceFirst("^([A-Z]{2})([0-9]{6})([A-Z])$", "$1-$2-$3")
  }

}

object CheckPartnerDetailsViewModel {

  private case class BusinessInfo(
    typeOfBusiness: Option[String] = None,
    soleProprietorName: Option[String] = None,
    soleProprietorDob: Option[String] = None,
    corporateBodyName: Option[String] = None,
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
          BusinessInfo(typeOfBusiness = typeLabel, corporateBodyName = businessName)

        case BusinessType.Unincorporatedbody =>
          BusinessInfo(typeOfBusiness = typeLabel, unincorporatedBodyName = businessName)

        case BusinessType.Partnership =>
          BusinessInfo(typeOfBusiness = typeLabel, partnershipName = businessName)

        case BusinessType.LimitedLiabilityPartnership =>
          BusinessInfo(typeOfBusiness = typeLabel, llpName = businessName)
      }
    }
  }

  // TODO -> Update to use flag!
  def from(userAnswers: UserAnswers, index: Int, isNewPartnerFlow: Option[Boolean], isSubmitted: Boolean)(implicit
    messages: Messages
  ): CheckPartnerDetailsViewModel = {

    val businessInfo = businessTypeInfo(userAnswers, index)
    val today = LocalDate.now()
    val dueToLeaveDate = userAnswers.get(PartnerDetailsDateOfJoiningPage(index))
    val dueToJoinDate = userAnswers.get(PartnerDetailsDateOfLeavingPage(index))

    val dueToLeave = dueToLeaveDate match {
      case Some(date) if today.isBefore(date) => true
      case _                                  => false
    }
    val dueToJoin = dueToJoinDate match {
      case Some(date) if today.isBefore(date) => true
      case _                                  => false
    }

    CheckPartnerDetailsViewModel(
      index                     = index,
      typeOfBusiness            = businessInfo.flatMap(_.typeOfBusiness),
      soleProprietorName        = businessInfo.flatMap(_.soleProprietorName),
      soleProprietorDob         = businessInfo.flatMap(_.soleProprietorDob),
      corporateBodyName         = businessInfo.flatMap(_.corporateBodyName),
      unincorporatedBodyName    = businessInfo.flatMap(_.unincorporatedBodyName),
      partnershipName           = businessInfo.flatMap(_.partnershipName),
      llpName                   = businessInfo.flatMap(_.llpName),
      addTradingName            = userAnswers.get(PartnerDetailsAddTradingNameYesNoPage(index)).map(_.toString),
      tradingName               = userAnswers.get(PartnerTradingNamePage).orElse(userAnswers.get(PartnerDetailsTradingNamePage(index))),
      dateOfJoining             = dueToLeaveDate.map(shortDateDisplay),
      dateOfLeaving             = dueToJoinDate.map(shortDateDisplay),
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
      emailAddress     = userAnswers.get(PartnerEmailAddressPage).orElse(userAnswers.get(PartnerDetailsCorrespondenceEmailAddressPage(index))),
      isNewPartnerFlow = isNewPartnerFlow,
      isSubmitted      = isSubmitted,
      isDueToLeave     = dueToLeave,
      isDueToJoin      = dueToJoin
    )
  }
}
