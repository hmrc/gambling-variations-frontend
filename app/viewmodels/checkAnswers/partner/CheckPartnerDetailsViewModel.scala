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
import viewmodels.govuk.all.{FluentValue, stringToText}

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
  isNewPartnerFlow: Option[Boolean]
) {

  def isSubmitted: Boolean = true
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
    val change = ActionItem(
      href               = controllers.partner.routes.PartnerDetailsBusinessTypeController.onPageLoad().url,
      content            = Text(messages("site.change")),
      visuallyHiddenText = Some(label)
    )

    typeOfBusiness match {
      case Some(value) if isNewPartnerFlow.contains(true) =>
        Some(
          SummaryListRow(
            key     = Key(content = Text(label)),
            value   = Value(content = Text(value)),
            actions = Some(Actions(items = Seq(change)))
          )
        )

      case _ =>
        Some(
          SummaryListRow(
            key     = Key(content = Text(label)),
            value   = Value(content = Text("Add type of business")),
            actions = Some(Actions(items = Seq(change)))
          )
        )
    }
  }

  private def soleProprietorNameSummaryListRow(implicit messages: Messages): Option[SummaryListRow] = {
    val isSoleProprietor = typeOfBusiness.contains(messages("businessType.soleproprietor"))

    if (isSoleProprietor) {
      val label = messages("partnerDetailsCheckYourAnswers.soleProprietorName")
      val href = controllers.partner.routes.PartnerSoleProprietorDobController.onPageLoad().url

      soleProprietorName match {
        case Some(value) if isNewPartnerFlow.contains(true) =>
          val changeAction = Seq(
            ActionItem(
              href               = href,
              content            = Text(messages("site.change")),
              visuallyHiddenText = Some(label)
            )
          )

          Some(
            SummaryListRow(
              key     = Key(content = Text(label)),
              value   = Value(content = Text(value)),
              actions = Some(Actions(items = changeAction))
            )
          )

        case Some(value) =>
          Some(
            SummaryListRow(
              key     = Key(content = Text(label)),
              value   = Value(content = Text(value)),
              actions = Some(Actions(items = Nil))
            )
          )

        case None =>
          val addText = messages("partnerDetailsCheckYourAnswers.soleProprietorName.add")
          val addLinkHtml = s"""<a href="$href">$addText</a>"""

          Some(
            SummaryListRow(
              key     = Key(content = Text(label)),
              value   = Value(content = HtmlContent(addLinkHtml)),
              actions = None
            )
          )
      }
    } else None
  }

  private def unincorporatedBodyNameSummaryListRow(implicit messages: Messages): Option[SummaryListRow] = {
    val isUnicorporated = typeOfBusiness.contains(messages("businessType.unincorporatedbody"))

    if (isUnicorporated) {
      val label = messages("partnerDetailsCheckYourAnswers.unincorporatedBodyName")
      val href = controllers.partner.routes.PartnerDetailsBusinessTypeController.onPageLoad().url

      unincorporatedBodyName match {
        case Some(value) if isNewPartnerFlow.contains(true) =>
          val changeAction = Seq(
            ActionItem(
              href               = href,
              content            = Text(messages("site.change")),
              visuallyHiddenText = Some(label)
            )
          )

          Some(
            SummaryListRow(
              key     = Key(content = Text(label)),
              value   = Value(content = Text(value)),
              actions = Some(Actions(items = changeAction))
            )
          )

        case Some(value) =>
          Some(
            SummaryListRow(
              key     = Key(content = Text(label)),
              value   = Value(content = Text(value)),
              actions = Some(Actions(items = Nil))
            )
          )

        case None =>
          val addText = messages("partnerDetailsCheckYourAnswers.unincorporatedBodyName.add")
          val addLinkHtml = s"""<a href="$href">$addText</a>"""

          Some(
            SummaryListRow(
              key     = Key(content = Text(label)),
              value   = Value(content = HtmlContent(addLinkHtml)),
              actions = None
            )
          )
      }
    } else None
  }

  private def corporateBodyNameSummaryListRow(implicit messages: Messages): Option[SummaryListRow] = {
    val isCorporate = typeOfBusiness.contains(messages("businessType.corporatebody"))

    if (isCorporate) {
      val label = messages("partnerDetailsCheckYourAnswers.corporateBodyName")
      val href = controllers.partner.routes.PartnerDetailsBusinessTypeController.onPageLoad().url

      corporateBodyName match {
        case Some(value) if isNewPartnerFlow.contains(true) =>
          val changeAction = Seq(
            ActionItem(
              href               = href,
              content            = Text(messages("site.change")),
              visuallyHiddenText = Some(label)
            )
          )

          Some(
            SummaryListRow(
              key     = Key(content = Text(label)),
              value   = Value(content = Text(value)),
              actions = Some(Actions(items = changeAction))
            )
          )

        case Some(value) =>
          Some(
            SummaryListRow(
              key     = Key(content = Text(label)),
              value   = Value(content = Text(value)),
              actions = Some(Actions(items = Nil))
            )
          )

        case None =>
          val addText = messages("partnerDetailsCheckYourAnswers.corporateBodyName.add")
          val addLinkHtml = s"""<a href="$href">$addText</a>"""

          Some(
            SummaryListRow(
              key     = Key(content = Text(label)),
              value   = Value(content = HtmlContent(addLinkHtml)),
              actions = None
            )
          )
      }
    } else None
  }

  private def partnershipNameSummaryListRow(implicit messages: Messages): Option[SummaryListRow] = {
    val isPartnership = typeOfBusiness.contains(messages("businessType.partnership"))

    if (isPartnership) {
      val label = messages("partnerDetailsCheckYourAnswers.partnershipName")
      val href = controllers.partner.routes.PartnerDetailsBusinessTypeController.onPageLoad().url

      partnershipName match {
        case Some(value) if isNewPartnerFlow.contains(true) =>
          val changeAction = Seq(
            ActionItem(
              href               = href,
              content            = Text(messages("site.change")),
              visuallyHiddenText = Some(label)
            )
          )

          Some(
            SummaryListRow(
              key     = Key(content = Text(label)),
              value   = Value(content = Text(value)),
              actions = Some(Actions(items = changeAction))
            )
          )

        case Some(value) =>
          Some(
            SummaryListRow(
              key     = Key(content = Text(label)),
              value   = Value(content = Text(value)),
              actions = Some(Actions(items = Nil))
            )
          )

        case None =>
          val addText = messages("partnerDetailsCheckYourAnswers.partnershipName.add")
          val addLinkHtml = s"""<a href="$href">$addText</a>"""

          Some(
            SummaryListRow(
              key     = Key(content = Text(label)),
              value   = Value(content = HtmlContent(addLinkHtml)),
              actions = None
            )
          )
      }
    } else None
  }

  private def llpNameSummaryListRow(implicit messages: Messages): Option[SummaryListRow] = {
    val isSoleProprietor = typeOfBusiness.contains(messages("businessType.llp"))

    if (isSoleProprietor) {
      val label = messages("partnerDetailsCheckYourAnswers.llpName")
      val href = controllers.partner.routes.PartnerDetailsBusinessTypeController.onPageLoad().url

      soleProprietorName match {
        case Some(value) if isNewPartnerFlow.contains(true) =>
          val changeAction = Seq(
            ActionItem(
              href               = href,
              content            = Text(messages("site.change")),
              visuallyHiddenText = Some(label)
            )
          )

          Some(
            SummaryListRow(
              key     = Key(content = Text(label)),
              value   = Value(content = Text(value)),
              actions = Some(Actions(items = changeAction))
            )
          )

        case Some(value) =>
          Some(
            SummaryListRow(
              key     = Key(content = Text(label)),
              value   = Value(content = Text(value)),
              actions = Some(Actions(items = Nil))
            )
          )

        case None =>
          val addText = messages("partnerDetailsCheckYourAnswers.llpName.add")
          val addLinkHtml = s"""<a href="$href">$addText</a>"""

          Some(
            SummaryListRow(
              key     = Key(content = Text(label)),
              value   = Value(content = HtmlContent(addLinkHtml)),
              actions = None
            )
          )
      }
    } else None
  }

  private def soleProprietorDobSummaryListRow(implicit messages: Messages): Option[SummaryListRow] = {
    val isSoleProprietor = typeOfBusiness.contains(messages("businessType.soleproprietor"))

    if (isSoleProprietor) {
      val label = messages("partnerDetailsCheckYourAnswers.soleProprietorDob")
      val href = controllers.partner.routes.PartnerSoleProprietorDobController.onPageLoad().url

      soleProprietorDob match {
        case Some(value) if isNewPartnerFlow.contains(true) =>
          val changeAction = Seq(
            ActionItem(
              href               = href,
              content            = Text(messages("site.change")),
              visuallyHiddenText = Some(label)
            )
          )

          Some(
            SummaryListRow(
              key     = Key(content = Text(label)),
              value   = Value(content = Text(value)),
              actions = Some(Actions(items = changeAction))
            )
          )

        case Some(value) =>
          Some(
            SummaryListRow(
              key     = Key(content = Text(label)),
              value   = Value(content = Text(value)),
              actions = Some(Actions(items = Nil))
            )
          )

        case None =>
          val addText = messages("partnerDetailsCheckYourAnswers.soleProprietorDob.add")
          val addLinkHtml = s"""<a href="$href">$addText</a>"""

          Some(
            SummaryListRow(
              key     = Key(content = Text(label)),
              value   = Value(content = HtmlContent(addLinkHtml)),
              actions = None
            )
          )
      }
    } else None
  }

  private def addNinoSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    if (typeOfBusiness.contains(messages("businessType.soleproprietor")) && isNewPartnerFlow.exists(identity)) {
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
    } else None

  private def ninoSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    if (typeOfBusiness.contains(messages("businessType.soleproprietor"))) {
      nino.map { value =>
        val label = messages("partnerDetailsCheckYourAnswers.nino")

        // TODO - update display logic
        val changeAction = if (isNewPartnerFlow.contains(true)) {
          Some(
            ActionItem(
              href               = controllers.partner.routes.PartnerDetailsBusinessTypeController.onPageLoad().url,
              content            = Text(messages("site.change")),
              visuallyHiddenText = Some(label)
            )
          )
        } else None

        val removeAction = if (isNewPartnerFlow.contains(true)) {
          Some(
            ActionItem(
              href               = controllers.partner.routes.PartnerDetailsBusinessTypeController.onPageLoad().url,
              content            = Text(messages("site.remove")),
              visuallyHiddenText = Some(label)
            )
          )
        } else None

        val actionsList = if (isNewPartnerFlow.contains(true)) { Seq(changeAction, removeAction).flatten }
        else Nil

        SummaryListRow(
          key = Key(content = Text(label)),
          value = Value(
            content = Text(formatNino(value)),
            classes = "govuk-summary-list__value"
          ),
          actions = Some(
            Actions(
              items   = actionsList,
              classes = "govuk-summary-list__actions govuk-!-width-one-third"
            )
          )
        )
      }
    } else None

  private def addTradingNameSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    if (isNewPartnerFlow.exists(identity)) {
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
    } else None

  private def tradingNameSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    tradingName.map { value =>
      val label = messages("partnerDetailsCheckYourAnswers.tradingName")

      val changeAction = ActionItem(
        href               = controllers.partner.routes.PartnerDetailsBusinessTypeController.onPageLoad().url,
        content            = Text(messages("site.change")),
        visuallyHiddenText = Some(label)
      )

      val removeAction = if (!isNewPartnerFlow.contains(true)) {
        Some(
          ActionItem(
            href               = controllers.partner.routes.PartnerDetailsBusinessTypeController.onPageLoad().url,
            content            = Text(messages("site.remove")),
            visuallyHiddenText = Some(label)
          )
        )
      } else None

      SummaryListRow(
        key     = Key(content = Text(label)),
        value   = Value(content = Text(value)),
        actions = Some(Actions(items = Seq(Some(changeAction), removeAction).flatten))
      )
    }

  private def isIncorporatedInUkSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    if (typeOfBusiness.contains(messages("businessType.corporatebody"))) {
      isIncorporatedInUk.map { value =>
        val label = messages("partnerDetailsCheckYourAnswers.isIncorporatedInUk")

        val changeAction = ActionItem(
          href               = controllers.partner.routes.PartnerDetailsIsBusinessIncorporatedUkController.onPageLoad().url,
          content            = Text(messages("site.change")),
          visuallyHiddenText = Some(label)
        )

        SummaryListRow(
          key     = Key(content = Text(label)),
          value   = Value(content = Text(value)),
          actions = Some(Actions(items = Seq(changeAction)))
        )
      }
    } else None

  private def countryOfIncorporationSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    if (typeOfBusiness.contains(messages("businessType.corporatebody")) && isIncorporatedInUk.contains("no")) {
      countryOfIncorporation.map { value =>
        val label = messages("partnerDetailsCheckYourAnswers.countryOfIncorporation")

        val changeAction = ActionItem(
          href               = controllers.partner.routes.PartnerDetailsIsBusinessIncorporatedUkController.onPageLoad().url,
          content            = Text(messages("site.change")),
          visuallyHiddenText = Some(label)
        )

        SummaryListRow(
          key     = Key(content = Text(label)),
          value   = Value(content = Text(value)),
          actions = Some(Actions(items = Seq(changeAction)))
        )
      }
    } else None

  private def foreignCorporateReferenceSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    if (typeOfBusiness.contains(messages("businessType.corporatebody")) && isIncorporatedInUk.contains("no")) {
      foreignCorporateReference.map { value =>
        val label = messages("partnerDetailsCheckYourAnswers.foreignCorporateReference")

        val changeAction = if (isNewPartnerFlow.contains(true)) {
          Some(
            ActionItem(
              href               = controllers.partner.routes.PartnerDetailsForeignCorporateReferenceController.onPageLoad().url,
              content            = Text(messages("site.change")),
              visuallyHiddenText = Some(label)
            )
          )
        } else None

        SummaryListRow(
          key     = Key(content = Text(label)),
          value   = Value(content = Text(value)),
          actions = Some(Actions(items = changeAction.toSeq))
        )
      }
    } else None

  private def dateOfIncorporationSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    if (
      (typeOfBusiness.contains(messages("businessType.corporatebody")) && isIncorporatedInUk
        .contains("yes")) || typeOfBusiness.contains(messages("businessType.llp"))
    ) {
      dateOfIncorporation.map { value =>
        val label = messages("partnerDetailsCheckYourAnswers.dateOfIncorporation")

        val changeAction = if (isNewPartnerFlow.contains(true)) {
          Some(
            ActionItem(
              href               = controllers.partner.routes.PartnerDateOfIncorporationController.onPageLoad().url,
              content            = Text(messages("site.change")),
              visuallyHiddenText = Some(label)
            )
          )
        } else None

        SummaryListRow(
          key     = Key(content = Text(label)),
          value   = Value(content = Text(value)),
          actions = Some(Actions(items = changeAction.toSeq))
        )
      }
    } else None

  private def companyRegistrationNumberSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    if (
      (typeOfBusiness.contains(messages("businessType.corporatebody")) && isIncorporatedInUk
        .contains("yes")) || typeOfBusiness.contains(messages("businessType.llp"))
    ) {
      companyRegistrationNumber.map { value =>
        val label = messages("partnerDetailsCheckYourAnswers.companyRegistrationNumber")

        val changeAction = if (isNewPartnerFlow.contains(true)) {
          Some(
            ActionItem(
              href               = controllers.partner.routes.PartnerDetailsForeignCorporateReferenceController.onPageLoad().url,
              content            = Text(messages("site.change")),
              visuallyHiddenText = Some(label)
            )
          )
        } else None

        SummaryListRow(
          key     = Key(content = Text(label)),
          value   = Value(content = Text(value)),
          actions = Some(Actions(items = changeAction.toSeq))
        )
      }
    } else None

  private def utrSummaryListRow(implicit messages: Messages): Option[SummaryListRow] = if (
    typeOfBusiness.contains(messages("businessType.corporatebody")) // TODO -> this should not be an if
  ) {
    val label = messages("partnerDetailsCheckYourAnswers.utr")
    val href = controllers.partner.routes.PartnerDetailsAddUTRController.onPageLoad().url

    utr match {
      case Some(value) if isNewPartnerFlow.contains(true) =>
        val changeAction = Seq(
          ActionItem(
            href               = href,
            content            = Text(messages("site.change")),
            visuallyHiddenText = Some(label)
          )
        )

        Some(
          SummaryListRow(
            key     = Key(content = Text(label)),
            value   = Value(content = Text(value)),
            actions = Some(Actions(items = changeAction))
          )
        )

      case Some(value) =>
        Some(
          SummaryListRow(
            key     = Key(content = Text(label)),
            value   = Value(content = Text(value)),
            actions = Some(Actions(items = Nil))
          )
        )

      case None =>
        val addText = messages("partnerDetailsCheckYourAnswers.utr.add")
        val addLinkHtml = s"""<a href="$href">$addText</a>"""

        Some(
          SummaryListRow(
            key     = Key(content = Text(label)),
            value   = Value(content = HtmlContent(addLinkHtml)),
            actions = None
          )
        )
    }

  } else None

  private def addVatRegistrationNumberSummaryListRow(implicit messages: Messages): Option[SummaryListRow] = if (
    typeOfBusiness.contains(messages("businessType.corporatebody")) && isNewPartnerFlow.exists(identity)
  ) {
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

  } else None

  private def vatRegistrationNumberSummaryListRow(implicit messages: Messages): Option[SummaryListRow] = if (
    typeOfBusiness.contains(messages("businessType.corporatebody"))
  ) {
    vatRegistrationNumber.map { value =>
      val label = messages("partnerDetailsCheckYourAnswers.vatRegistrationNumber")

      val changeAction = if (isNewPartnerFlow.contains(true)) {
        Some(
          ActionItem(
            href               = controllers.partner.routes.PartnerDetailsVatRegistrationNumberController.onPageLoad().url,
            content            = Text(messages("site.change")),
            visuallyHiddenText = Some(label)
          )
        )
      } else None

      val removeAction = if (!isNewPartnerFlow.contains(true)) {
        Some(
          ActionItem(
            href               = controllers.partner.routes.PartnerDetailsRemoveVatRegNumberYesNoController.onPageLoad().url,
            content            = Text(messages("site.remove")),
            visuallyHiddenText = Some(label)
          )
        )
      } else None

      SummaryListRow(
        key     = Key(content = Text(label)),
        value   = Value(content = Text(value)),
        actions = Some(Actions(items = Seq(changeAction, removeAction).flatten))
      )
    }
  } else None

  private def dateOfJoiningSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    dateOfJoining.map { value =>
      val label = messages("partnerDetailsCheckYourAnswers.dateOfJoining")

      val changeAction = if (isNewPartnerFlow.contains(true)) {
        Some(
          ActionItem(
            href               = controllers.partner.routes.PartnerDetailsBusinessTypeController.onPageLoad().url, // TODO: change this
            content            = Text(messages("site.change")),
            visuallyHiddenText = Some(label)
          )
        )
      } else None

      SummaryListRow(
        key     = Key(content = Text(label)),
        value   = Value(content = Text(value)),
        actions = Some(Actions(items = changeAction.toSeq))
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
              href               = controllers.partner.routes.PartnerDetailsBusinessTypeController.onPageLoad().url, // TODO: change this
              content            = "site.change",
              visuallyHiddenText = Some(messages("partnerDetailsCheckYourAnswers.address"))
            )
          )
        )
      )
    )

  private def addAdditionalInformationSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    if (isNewPartnerFlow.exists(identity)) {
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
    } else None

  private def additionalInformationSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    additionalInformation.map { value =>
      val label = messages("partnerDetailsCheckYourAnswers.additionalInformation")

      val changeAction = ActionItem(
        href               = controllers.partner.routes.PartnerDetailsAdditionalAddressInfoYesNoController.onPageLoad().url,
        content            = Text(messages("site.change")),
        visuallyHiddenText = Some(label)
      )

      val removeAction = if (!isNewPartnerFlow.contains(true)) {
        Some(
          ActionItem(
            href = controllers.partner.routes.RemoveAdditionalInfoForPartnerAddressYesNoController
              .onPageLoad()
              .url,
            content            = Text(messages("site.remove")),
            visuallyHiddenText = Some(label)
          )
        )
      } else None

      SummaryListRow(
        key     = Key(content = Text(label)),
        value   = Value(content = Text(value)),
        actions = Some(Actions(items = Seq(Some(changeAction), removeAction).flatten))
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
    if (isNewPartnerFlow.exists(identity)) {
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
    } else None

  private def faxNumberSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    faxNumber.map { value =>
      val label = messages("partnerDetailsCheckYourAnswers.faxNumber")

      val changeAction = ActionItem(
        href               = controllers.partner.routes.ChangePartnerFaxNumberController.onPageLoad().url,
        content            = Text(messages("site.change")),
        visuallyHiddenText = Some(label)
      )

      val removeAction = if (!isNewPartnerFlow.contains(true)) {
        Some(
          ActionItem(
            href = controllers.partner.routes.PartnerDetailsRemoveFaxNumberYesNoController
              .onPageLoad()
              .url,
            content            = Text(messages("site.remove")),
            visuallyHiddenText = Some(label)
          )
        )
      } else None

      SummaryListRow(
        key     = Key(content = Text(label)),
        value   = Value(content = Text(value)),
        actions = Some(Actions(items = Seq(Some(changeAction), removeAction).flatten))
      )
    }

  private def addEmailAddressSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    if (isNewPartnerFlow.exists(identity)) {
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
    } else None

  private def emailAddressSummaryListRow(implicit messages: Messages): Option[SummaryListRow] =
    emailAddress.map { value =>
      val label = messages("partnerDetailsCheckYourAnswers.emailAddress")

      val changeAction = ActionItem(
        href               = controllers.partner.routes.PartnerEmailAddressController.onPageLoad().url,
        content            = Text(messages("site.change")),
        visuallyHiddenText = Some(label)
      )

      val removeAction = if (!isNewPartnerFlow.contains(true)) {
        Some(
          ActionItem(
            href = controllers.partner.routes.PartnerDetailsRemoveEmailAddressYesNoController
              .onPageLoad()
              .url,
            content            = Text(messages("site.remove")),
            visuallyHiddenText = Some(label)
          )
        )
      } else None

      SummaryListRow(
        key     = Key(content = Text(label)),
        value   = Value(content = Text(value)),
        actions = Some(Actions(items = Seq(Some(changeAction), removeAction).flatten))
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
  def from(userAnswers: UserAnswers, index: Int, isNewPartnerFlow: Option[Boolean])(implicit messages: Messages): CheckPartnerDetailsViewModel = {
    val businessInfo = businessTypeInfo(userAnswers, index)
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
      emailAddress     = userAnswers.get(PartnerEmailAddressPage).orElse(userAnswers.get(PartnerDetailsCorrespondenceEmailAddressPage(index))),
      isNewPartnerFlow = isNewPartnerFlow
    )
  }
}
