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

import models.BusinessType.*
import models.{Address, BusinessType, ContactNumber, UserAnswers}
import pages.partner.*
import pages.partnerdetails.*
import play.api.i18n.Messages
import play.api.libs.json.Reads
import play.api.mvc.Call
import play.twirl.api.Html
import queries.Gettable
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
  isDueToJoin: Boolean,
  isMissingMandatoryFields: Boolean
) {

  // --- Update this ---
  def continueCall: Call = controllers.partner.routes.PartnerDetailsCheckYourAnswersController.onPageLoad()

  // --- Summary Lists for View ---

  private lazy val dueToJoinOrLeave: Boolean = isDueToLeave || isDueToJoin

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

    val actions = (isNewPartnerFlow.contains(true), isSubmitted) match {
      case (true, _)     => Seq(changeAction)
      case (false, true) => Nil
      case _             => Nil
    }

    typeOfBusiness
      .map(bt => createSummaryListRow(label, Text(bt), actions))
      .orElse(Some(createSummaryListRow(label, Text("Add type of business"), Seq(changeAction))))
  }

  private def soleProprietorNameSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    if (typeOfBusiness.contains(messages("businessType.soleproprietor"))) {

      val label = messages("partnerDetailsCheckYourAnswers.soleProprietorName")
      val url = controllers.partner.routes.ChangePartnerDetailsBusinessNameController.onPageLoad(businessType = Soleproprietor).url

      soleProprietorName match {
        case Some(value) =>
          val actions = (isNewPartnerFlow.contains(true), isSubmitted, dueToJoinOrLeave) match {
            case (true, _, _)  => Seq(buildAction(url, "site.change", label))
            case (_, false, _) => Seq(buildAction(url, "site.change", label))
            case _             => Nil
          }

          Some(createSummaryListRow(label, Text(value), actions))

        case None =>
          val addText = messages("partnerDetailsCheckYourAnswers.soleProprietorName.add")
          val addLinkHtml = s"""<a href="$url">$addText</a>"""
          Some(createSummaryListRow(label, HtmlContent(addLinkHtml), Nil))
      }
    } else None

  private def unincorporatedBodyNameSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    if (typeOfBusiness.contains(messages("businessType.unincorporatedbody"))) {

      val label = messages("partnerDetailsCheckYourAnswers.unincorporatedBodyName")
      val url = controllers.partner.routes.ChangePartnerDetailsBusinessNameController.onPageLoad(businessType = Unincorporatedbody).url

      unincorporatedBodyName match {
        case Some(value) =>
          val actions = (isNewPartnerFlow.contains(true), isSubmitted, dueToJoinOrLeave) match {
            case (true, _, _)  => Seq(buildAction(url, "site.change", label))
            case (_, false, _) => Seq(buildAction(url, "site.change", label))
            case _             => Nil
          }

          Some(createSummaryListRow(label, Text(value), actions))

        case None =>
          val addText = messages("partnerDetailsCheckYourAnswers.unincorporatedBodyName.add")
          val addLinkHtml = s"""<a href="$url">$addText</a>"""
          Some(createSummaryListRow(label, HtmlContent(addLinkHtml), Nil))
      }
    } else None

  private def corporateBodyNameSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    if (typeOfBusiness.contains(messages("businessType.corporatebody"))) {

      val label = messages("partnerDetailsCheckYourAnswers.corporateBodyName")
      val url = controllers.partner.routes.ChangePartnerDetailsBusinessNameController.onPageLoad(businessType = Corporatebody).url

      corporateBodyName match {
        case Some(value) =>
          val actions = (isNewPartnerFlow.contains(true), isSubmitted, dueToJoinOrLeave) match {
            case (true, _, _)  => Seq(buildAction(url, "site.change", label))
            case (_, false, _) => Seq(buildAction(url, "site.change", label))
            case _             => Nil
          }

          Some(createSummaryListRow(label, Text(value), actions))

        case None =>
          val addText = messages("partnerDetailsCheckYourAnswers.corporateBodyName.add")
          val addLinkHtml = s"""<a href="$url">$addText</a>"""
          Some(createSummaryListRow(label, HtmlContent(addLinkHtml), Nil))
      }
    } else None

  private def partnershipNameSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    if (typeOfBusiness.contains(messages("businessType.partnership"))) {

      val label = messages("partnerDetailsCheckYourAnswers.partnershipName")
      val url = controllers.partner.routes.ChangePartnerDetailsBusinessNameController.onPageLoad(businessType = Partnership).url

      partnershipName match {
        case Some(value) =>
          val actions = (isNewPartnerFlow.contains(true), isSubmitted, dueToJoinOrLeave) match {
            case (true, _, _)  => Seq(buildAction(url, "site.change", label))
            case (_, false, _) => Seq(buildAction(url, "site.change", label))
            case _             => Nil
          }

          Some(createSummaryListRow(label, Text(value), actions))

        case None =>
          val addText = messages("partnerDetailsCheckYourAnswers.partnershipName.add")
          val addLinkHtml = s"""<a href="$url">$addText</a>"""
          Some(createSummaryListRow(label, HtmlContent(addLinkHtml), Nil))
      }
    } else None

  private def llpNameSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    if (typeOfBusiness.contains(messages("businessType.llp"))) {

      val label = messages("partnerDetailsCheckYourAnswers.llpName")
      val url = controllers.partner.routes.ChangePartnerDetailsBusinessNameController.onPageLoad(businessType = LimitedLiabilityPartnership).url

      llpName match {
        case Some(value) =>
          val actions = (isNewPartnerFlow.contains(true), isSubmitted, dueToJoinOrLeave) match {
            case (true, _, _)  => Seq(buildAction(url, "site.change", label))
            case (_, false, _) => Seq(buildAction(url, "site.change", label))
            case _             => Nil
          }

          Some(createSummaryListRow(label, Text(value), actions))

        case None =>
          val addText = messages("partnerDetailsCheckYourAnswers.llpName.add")
          val addLinkHtml = s"""<a href="$url">$addText</a>"""
          Some(createSummaryListRow(label, HtmlContent(addLinkHtml), Nil))
      }
    } else None

  private def soleProprietorDobSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    if (typeOfBusiness.contains(messages("businessType.soleproprietor"))) {
      val url = controllers.partner.routes.PartnerSoleProprietorDobController.onPageLoad().url
      val label = messages("partnerDetailsCheckYourAnswers.soleProprietorDob")

      soleProprietorDob match {
        case Some(value) =>
          val actions = (isNewPartnerFlow.contains(true), isSubmitted, dueToJoinOrLeave) match {
            case (true, _, _)      => Seq(buildAction(url, "site.change", label))
            case (false, _, true)  => Nil
            case (false, false, _) => Seq(buildAction(url, "site.change", label))
            case _                 => Nil
          }

          Some(createSummaryListRow(label, Text(value), actions))

        case None =>
          val addText = messages("partnerDetailsCheckYourAnswers.soleProprietorDob.add")
          val addLinkHtml = s"""<a href="$url">$addText</a>"""
          Some(createSummaryListRow(label, HtmlContent(addLinkHtml), Nil))
      }
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
    if (typeOfBusiness.contains(messages("businessType.soleproprietor"))) {
      val label = messages("partnerDetailsCheckYourAnswers.nino")

      val changeUrl = controllers.partner.routes.PartnerDetailsAddNationalInsuranceNumberController.onPageLoad().url
      val removeUrl = controllers.partner.routes.PartnerDetailsRemoveNationalInsuranceNumberYesNoController.onPageLoad().url

      val changeAction = buildAction(changeUrl, "site.change", label)
      val removeAction = buildAction(removeUrl, "site.remove", label)

      val actions = (isNewPartnerFlow.contains(true), isSubmitted, dueToJoinOrLeave) match {
        case (true, _, _)          => Seq(changeAction)
        case (false, _, true)      => Nil
        case (false, false, false) => Seq(removeAction)
        case _                     => Nil
      }

      nino.map(value => createSummaryListRow(label, Text(value), actions)).orElse {
        val fallbackActions = if (actions.contains(changeAction)) Seq(changeAction) else Nil

        Some(
          createSummaryListRow(
            label,
            Text(messages("partnerDetailsCheckYourAnswers.noData")),
            fallbackActions,
            "govuk-summary-list__actions govuk-!-width-one-third"
          )
        )
      }
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

  private def tradingNameSummaryListRow(implicit messages: Messages): Option[SummaryListRow] = {
    val label = messages("partnerDetailsCheckYourAnswers.tradingName")

    val changeUrl = controllers.partner.routes.PartnerTradingNameController.onPageLoad().url
    val removeUrl = controllers.partner.routes.RemovePartnerTradingNameYesNoController.onPageLoad().url

    val changeAction = buildAction(changeUrl, "site.change", label)
    val removeAction = buildAction(removeUrl, "site.remove", label)

    val actions = (isNewPartnerFlow.contains(true), dueToJoinOrLeave) match {
      case (true, _)      => Seq(changeAction)
      case (false, true)  => Nil
      case (false, false) => Seq(removeAction)
    }

    tradingName.map(value => createSummaryListRow(label, Text(value), actions)).orElse {
      val fallbackActions = if (actions.contains(changeAction)) Seq(changeAction) else Nil

      Some(
        createSummaryListRow(
          label,
          Text(messages("partnerDetailsCheckYourAnswers.noData")),
          fallbackActions,
          "govuk-summary-list__actions govuk-!-width-one-third"
        )
      )
    }
  }

  private def isIncorporatedInUkSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    if (typeOfBusiness.contains(messages("businessType.corporatebody"))) {
      val label = messages("partnerDetailsCheckYourAnswers.isIncorporatedInUk")
      val url = controllers.partner.routes.PartnerDetailsIsBusinessIncorporatedUkController.onPageLoad().url

      isIncorporatedInUk match {
        case Some(value) =>
          val actions = (isNewPartnerFlow.contains(true), isSubmitted, dueToJoinOrLeave) match {
            case (true, _, _)     => Seq(buildAction(url, "site.change", label))
            case (false, _, true) => Nil
            case (_, false, _)    => Seq(buildAction(url, "site.change", label))
            case _                => Nil
          }

          Some(createSummaryListRow(label, Text(value), actions))

        case None =>
          val addText = messages("partnerDetailsCheckYourAnswers.isIncorporatedInUk.add")
          val addLinkHtml = s"""<a href="$url">$addText</a>"""
          Some(createSummaryListRow(label, HtmlContent(addLinkHtml), Nil))
      }
    } else None

  private def countryOfIncorporationSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    if (typeOfBusiness.contains(messages("businessType.corporatebody")) && isIncorporatedInUk.contains("no")) {
      val label = messages("partnerDetailsCheckYourAnswers.countryOfIncorporation")
      val url = controllers.partner.routes.PartnerDetailsIsBusinessIncorporatedUkController.onPageLoad().url

      countryOfIncorporation match {
        case Some(value) =>
          val actions = (isNewPartnerFlow.contains(true), isSubmitted, dueToJoinOrLeave) match {
            case (true, _, _)     => Seq(buildAction(url, "site.change", label))
            case (false, _, true) => Nil
            case (_, false, _)    => Seq(buildAction(url, "site.change", label))
            case _                => Nil
          }

          Some(createSummaryListRow(label, Text(value), actions))

        case None =>
          val addText = messages("partnerDetailsCheckYourAnswers.countryOfIncorporation.add")
          val addLinkHtml = s"""<a href="$url">$addText</a>"""
          Some(createSummaryListRow(label, HtmlContent(addLinkHtml), Nil))
      }
    } else None

  private def foreignCorporateReferenceSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    if (typeOfBusiness.contains(messages("businessType.corporatebody")) && isIncorporatedInUk.contains("no")) {
      val label = messages("partnerDetailsCheckYourAnswers.foreignCorporateReference")
      val url = controllers.partner.routes.PartnerDetailsForeignCorporateReferenceController.onPageLoad().url

      foreignCorporateReference match {
        case Some(value) =>
          val actions = (isNewPartnerFlow.contains(true), isSubmitted, dueToJoinOrLeave) match {
            case (true, _, _)     => Seq(buildAction(url, "site.change", label))
            case (false, _, true) => Nil
            case (_, false, _)    => Seq(buildAction(url, "site.change", label))
            case _                => Nil
          }

          Some(createSummaryListRow(label, Text(value), actions))

        case None =>
          val addText = messages("partnerDetailsCheckYourAnswers.foreignCorporateReference.add")
          val addLinkHtml = s"""<a href="$url">$addText</a>"""
          Some(createSummaryListRow(label, HtmlContent(addLinkHtml), Nil))
      }
    } else None

  private def dateOfIncorporationSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    if (
      typeOfBusiness.contains(messages("businessType.corporatebody")) && isIncorporatedInUk
        .contains("yes") || typeOfBusiness.contains(messages("businessType.llp"))
    ) {
      val label = messages("partnerDetailsCheckYourAnswers.dateOfIncorporation")
      val url = controllers.partner.routes.PartnerDateOfIncorporationController.onPageLoad().url

      dateOfIncorporation match {
        case Some(value) =>
          val actions = (isNewPartnerFlow.contains(true), isSubmitted, dueToJoinOrLeave) match {
            case (true, _, _)     => Seq(buildAction(url, "site.change", label))
            case (false, _, true) => Nil
            case (_, false, _)    => Seq(buildAction(url, "site.change", label))
            case _                => Nil
          }

          Some(createSummaryListRow(label, Text(value), actions))

        case None =>
          val addText = messages("partnerDetailsCheckYourAnswers.dateOfIncorporation.add")
          val addLinkHtml = s"""<a href="$url">$addText</a>"""
          Some(createSummaryListRow(label, HtmlContent(addLinkHtml), Nil))
      }
    } else None

  private def companyRegistrationNumberSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    if (
      typeOfBusiness.contains(messages("businessType.corporatebody")) && isIncorporatedInUk
        .contains("yes") || typeOfBusiness.contains(messages("businessType.llp"))
    ) {
      val label = messages("partnerDetailsCheckYourAnswers.companyRegistrationNumber")
      val url = controllers.partner.routes.PartnerDetailsForeignCorporateReferenceController.onPageLoad().url

      companyRegistrationNumber match {
        case Some(value) =>
          val actions = (isNewPartnerFlow.contains(true), isSubmitted, dueToJoinOrLeave) match {
            case (true, _, _)     => Seq(buildAction(url, "site.change", label))
            case (false, _, true) => Nil
            case (_, false, _)    => Seq(buildAction(url, "site.change", label))
            case _                => Nil
          }

          Some(createSummaryListRow(label, Text(value), actions))

        case None =>
          val addText = messages("partnerDetailsCheckYourAnswers.companyRegistrationNumber.add")
          val addLinkHtml = s"""<a href="$url">$addText</a>"""
          Some(createSummaryListRow(label, HtmlContent(addLinkHtml), Nil))
      }
    } else None

  private def utrSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    if (!typeOfBusiness.contains(messages("businessType.partnership"))) {
      val url = controllers.partner.routes.PartnerDetailsAddUTRController.onPageLoad().url
      val label = messages("partnerDetailsCheckYourAnswers.utr")

      utr match {
        case Some(value) =>
          val actions = (isNewPartnerFlow.contains(true), isSubmitted, dueToJoinOrLeave) match {
            case (true, _, _)     => Seq(buildAction(url, "site.change", label))
            case (false, _, true) => Nil
            case (_, false, _)    => Seq(buildAction(url, "site.change", label))
            case _                => Nil
          }

          Some(createSummaryListRow(label, Text(value), actions))

        case None =>
          val addText = messages("partnerDetailsCheckYourAnswers.utr.add")
          val addLinkHtml = s"""<a href="$url">$addText</a>"""
          Some(createSummaryListRow(label, HtmlContent(addLinkHtml), Nil))
      }

    } else None

  private def addVatRegistrationNumberSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    if (typeOfBusiness.contains(messages("businessType.corporatebody")) && isNewPartnerFlow.contains(true)) {
      addVatRegistrationNumber.map { value =>
        val label = messages("partnerDetailsCheckYourAnswers.addVatRegistrationNumber")
        val url = controllers.partner.routes.VatRegistrationNumberYesNoController.onPageLoad().url
        createSummaryListRow(label, Text(value), Seq(buildAction(url, "site.change", label)))
      }
    } else None

  private def vatRegistrationNumberSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    if (typeOfBusiness.contains(messages("businessType.soleproprietor"))) {
      val label = messages("partnerDetailsCheckYourAnswers.vatRegistrationNumber")
      val url = controllers.partner.routes.PartnerDetailsVatRegistrationNumberController.onPageLoad().url
      val changeAction = buildAction(url, "site.change", label)
//      val removeAction = buildAction(url, "site.remove", label)

      val actions = (isNewPartnerFlow.contains(true), isSubmitted, dueToJoinOrLeave) match {
        case (true, _, _)  => Seq(changeAction)
        case (_, false, _) => Seq(changeAction)
        //            case (false, false, _) => Seq(removeAction) TODO: add remove action
        case _ => Nil
      }

      vatRegistrationNumber
        .map(value => createSummaryListRow(label, Text(value), actions))
        .orElse {
          val fallbackActions = if (actions.contains(changeAction)) Seq(changeAction) else Nil

          Some(
            createSummaryListRow(
              label,
              Text(messages("partnerDetailsCheckYourAnswers.noData")),
              fallbackActions,
              "govuk-summary-list__actions govuk-!-width-one-third"
            )
          )
        }
    } else None

  private def dateOfJoiningSummaryListRow(implicit messages: Messages): Option[SummaryListRow] = {
    val label = messages("partnerDetailsCheckYourAnswers.dateOfJoining")
    val url = controllers.partner.routes.PartnerSoleProprietorDobController.onPageLoad().url // todo - change this

    dateOfJoining match {
      case Some(value) =>
        val actions = (isNewPartnerFlow.contains(true), isSubmitted, dueToJoinOrLeave) match {
          case (true, _, _)  => Seq(buildAction(url, "site.change", label))
          case (_, false, _) => Seq(buildAction(url, "site.change", label))
          case _             => Nil
        }

        Some(createSummaryListRow(label, Text(value), actions))

      case None =>
        val addText = messages("partnerDetailsCheckYourAnswers.dateOfJoining.add")
        val addLinkHtml = s"""<a href="$url">$addText</a>"""
        Some(createSummaryListRow(label, HtmlContent(addLinkHtml), Nil))
    }
  }

  // --- Address Rows ---

  private def addressSummaryListRow(implicit messages: Messages): SummaryListRow = {
    val label = messages("partnerDetailsCheckYourAnswers.address")
    val url = controllers.partner.routes.PartnerDetailsBusinessTypeController.onPageLoad().url // TODO: change this
    val changeAction = buildAction(url, "site.change", label)

    val actions = (isNewPartnerFlow.contains(true), dueToJoinOrLeave) match {
      case (false, true) => Nil
      case _             => Seq(changeAction)
    }

    createSummaryListRow(label, addressContent, actions)

  }

  private def addAdditionalInformationSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    if (isNewPartnerFlow.contains(true)) {
      addAdditionalInformation.map { value =>
        val label = messages("partnerDetailsCheckYourAnswers.addAdditionalInformation")
        val url = controllers.partner.routes.PartnerDetailsAdditionalAddressInfoYesNoController.onPageLoad().url
        createSummaryListRow(label, Text(value), Seq(buildAction(url, "site.change", label)))
      }
    } else None

  private def additionalInformationSummaryListRow(implicit messages: Messages): Option[SummaryListRow] = {
    val label = messages("partnerDetailsCheckYourAnswers.additionalInformation")

    val changeAction = buildAction(
      controllers.partner.routes.PartnerDetailsAdditionalAddressInfoYesNoController.onPageLoad().url,
      "site.change",
      label
    )
    val removeAction = buildAction(
      controllers.partner.routes.RemoveAdditionalInfoForPartnerAddressYesNoController.onPageLoad().url,
      "site.remove",
      label
    )

    val actions = (isNewPartnerFlow.contains(true), dueToJoinOrLeave) match {
      case (true, true)  => Seq(changeAction)
      case (false, true) => Nil
      case (true, _)     => Seq(changeAction)
      case (false, _)    => Seq(changeAction, removeAction)
    }

    additionalInformation.map(value => createSummaryListRow(label, Text(value), actions)).orElse {
      val fallbackActions = if (actions.contains(changeAction)) Seq(changeAction) else Nil

      Some(
        createSummaryListRow(
          label,
          Text(messages("partnerDetailsCheckYourAnswers.noData")),
          fallbackActions,
          "govuk-summary-list__actions govuk-!-width-one-third"
        )
      )
    }
  }

  // --- Contact Details Rows ---

  private def contactNumbersSummaryListRow(implicit messages: Messages): Option[SummaryListRow] = {
    val label = messages("partnerDetailsCheckYourAnswers.contactNumbers")
    val url = controllers.partner.routes.PartnerContactDetailsController.onPageLoad().url
    val changeAction = buildAction(url, "site.change", label)

    val actions = (isNewPartnerFlow.contains(true), dueToJoinOrLeave) match {
      case (false, true) => Nil
      case _             => Seq(changeAction)
    }

    contactNumbers
      .map { numbers =>
        val row = createSummaryListRow(label, contactNumbersContent(numbers), actions)
        row.copy(value = row.value.withCssClass("contact-numbers"))
      }
      .orElse {
        val fallbackActions = if (actions.contains(changeAction)) Seq(changeAction) else Nil

        Some(
          createSummaryListRow(
            label,
            Text(messages("partnerDetailsCheckYourAnswers.noData")),
            fallbackActions,
            "govuk-summary-list__actions govuk-!-width-one-third"
          )
        )
      }
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
    val changeAction = buildAction(
      controllers.partner.routes.ChangePartnerFaxNumberController.onPageLoad().url,
      "site.change",
      label
    )
    val removeAction = buildAction(
      controllers.partner.routes.PartnerDetailsRemoveFaxNumberYesNoController.onPageLoad().url,
      "site.remove",
      label
    )

    val actions = (isNewPartnerFlow.contains(true), dueToJoinOrLeave) match {
      case (true, true)  => Seq(changeAction)
      case (false, true) => Nil
      case (true, _)     => Seq(changeAction)
      case (false, _)    => Seq(changeAction, removeAction)
    }

    faxNumber
      .map(value => createSummaryListRow(label, Text(value), actions))
      .orElse {
        val fallbackActions = if (actions.contains(changeAction)) Seq(changeAction) else Nil

        Some(
          createSummaryListRow(
            label,
            Text(messages("partnerDetailsCheckYourAnswers.noData")),
            fallbackActions,
            "govuk-summary-list__actions govuk-!-width-one-third"
          )
        )
      }
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
    val changeAction = buildAction(
      controllers.partner.routes.PartnerEmailAddressController.onPageLoad().url,
      "site.change",
      label
    )
    val removeAction = buildAction(
      controllers.partner.routes.PartnerDetailsRemoveEmailAddressYesNoController.onPageLoad().url,
      "site.remove",
      label
    )

    val actions = (isNewPartnerFlow.contains(true), dueToJoinOrLeave) match {
      case (true, true)  => Seq(changeAction)
      case (false, true) => Nil
      case (true, _)     => Seq(changeAction)
      case (false, _)    => Seq(changeAction, removeAction)
    }

    emailAddress
      .map(value => createSummaryListRow(label, Text(value), actions))
      .orElse {
        val fallbackActions = if (actions.contains(changeAction)) Seq(changeAction) else Nil

        Some(
          createSummaryListRow(
            label,
            Text(messages("partnerDetailsCheckYourAnswers.noData")),
            fallbackActions,
            "govuk-summary-list__actions govuk-!-width-one-third"
          )
        )
      }
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

  private def formatNino(nino: String): String = {
    val clean = nino.replaceAll("[^a-zA-Z0-9]", "").toUpperCase
    clean.replaceFirst("^([A-Z]{2})([0-9]{6})([A-Z])$", "$1-$2-$3")
  }

  private def missingMandatoryFields(userAnswers: UserAnswers, index: Int): Boolean = {

    def missing[A: Reads](page: Gettable[A]): Boolean = userAnswers.get(page).isEmpty

    def businessInfoMissing: Boolean =
      userAnswers.get(PartnerDetailsBusinessTypePage(index)).forall {
        case BusinessType.Soleproprietor =>
          missing(PartnerDetailsSoleProprietorPage(index)) &&
            missing(PartnerDetailsUtrPage(index)) &&
            missing(PartnerDetailsDateOfBirthPage(index))

        case BusinessType.Corporatebody | BusinessType.Unincorporatedbody |
             BusinessType.Partnership | BusinessType.LimitedLiabilityPartnership =>
          missing(PartnerDetailsBusinessNamePage(index))
      }

    (missing(PartnerTradingNamePage) && missing(PartnerDetailsTradingNamePage(index))) ||
      missing(PartnerDetailsDateOfLeavingPage(index)) ||
      missing(PartnerDetailsIsBusinessIncorporatedUkPage(index)) ||
      missing(PartnerDetailsCountryOfIncorporation(index)) ||
      missing(PartnerDetailsDateOfIncorporation(index)) ||
      missing(PartnerDetailsForeignCorporateReferencePage(index)) ||
      missing(PartnerDetailsCrnPage(index)) ||
      missing(PartnerDetailsContactNumberPage(index)) ||
      userAnswers.get(NewPartnerDetailsCorrespondenceDetailsSectionPage(index)).flatMap(_.correspondenceAddress).isEmpty ||
      businessInfoMissing
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
      nino                      = userAnswers.get(PartnerDetailsNinoPage(index)).map(formatNino),
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
      isDueToJoin      = dueToJoin,
      isMissingMandatoryFields = missingMandatoryFields(userAnswers, index)
    )
  }
}
