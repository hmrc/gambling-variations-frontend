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

package viewmodels.checkAnswers.controllingbody

import controllers.controllingbody.routes
import models.BusinessType.{Corporatebody, LimitedLiabilityPartnership, Partnership, Soleproprietor, Unincorporatedbody}
import models.{Address, BusinessType, SoleProprietorName, UserAnswers}
import pages.QuestionPage
import pages.controllingbody.*
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{Content, HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}
import utils.AddressFormatter
import utils.DateTimeFormats.shortDateDisplay
import viewmodels.govuk.summarylist.*
import viewmodels.implicits.*

import java.time.LocalDate

/** CB-CYA in the add flow: screener questions are shown once answered, and missing mandatory details link to the screen that provides them. */
case class CheckControllingBodyDetailsViewModel(
  businessType: Option[BusinessType] = None,
  businessName: Option[String] = None,
  soleProprietorName: Option[SoleProprietorName] = None,
  dateOfBirth: Option[LocalDate] = None,
  addTradingName: Option[Boolean] = None,
  tradingName: Option[String] = None,
  addNino: Option[Boolean] = None,
  nino: Option[String] = None,
  isUkIncorporated: Option[Boolean] = None,
  countryOfIncorporation: Option[String] = None,
  foreignCorporateRef: Option[String] = None,
  dateOfIncorporation: Option[LocalDate] = None,
  crn: Option[String] = None,
  utr: Option[String] = None,
  addVrn: Option[Boolean] = None,
  vrn: Option[String] = None,
  address: Option[Address] = None,
  addAdditionalInfo: Option[Boolean] = None,
  additionalInfo: Option[String] = None,
  phoneNumber: Option[String] = None,
  mobilePhoneNumber: Option[String] = None,
  addFaxNumber: Option[Boolean] = None,
  faxNumber: Option[String] = None,
  addEmailAddress: Option[Boolean] = None,
  emailAddress: Option[String] = None
) {

  import CheckControllingBodyDetailsViewModel.*

  private val isSoleProprietor: Boolean = businessType.contains(Soleproprietor)
  private val isCorporateBody: Boolean = businessType.contains(Corporatebody)

  // A corporate body is first asked whether it is incorporated in the UK, a limited liability partnership always is
  private val showUkIncorporation: Boolean =
    (isCorporateBody && isUkIncorporated.contains(true)) || businessType.contains(LimitedLiabilityPartnership)

  private val showNonUkIncorporation: Boolean = isCorporateBody && isUkIncorporated.contains(false)

  // The mandatory details, each with the screen that provides it. The name row depends on the type of business, so it waits for the type.
  private val typeOfBusinessDetail: MandatoryDetail =
    MandatoryDetail("typeOfBusiness", businessType.map(value => messages => Text(messages(s"businessType.$value"))), businessTypeUrl)

  private val nameDetail: Option[MandatoryDetail] = businessType.map {
    case Soleproprietor              => MandatoryDetail.text("soleProprietorName", soleProprietorName.map(_.fullName), nameUrl(Soleproprietor))
    case Corporatebody               => MandatoryDetail.text("corporateBodyName", businessName, nameUrl(Corporatebody))
    case Unincorporatedbody          => MandatoryDetail.text("unincorporatedBodyName", businessName, nameUrl(Unincorporatedbody))
    case Partnership                 => MandatoryDetail.text("partnershipName", businessName, nameUrl(Partnership))
    case LimitedLiabilityPartnership => MandatoryDetail.text("limitedLiabilityPartnershipName", businessName, nameUrl(LimitedLiabilityPartnership))
  }

  private val dateOfBirthDetail: Option[MandatoryDetail] =
    Option.when(isSoleProprietor)(MandatoryDetail.date("soleProprietorDOB", dateOfBirth, dateOfBirthUrl))

  private val isUkIncorporatedDetail: Option[MandatoryDetail] =
    Option.when(isCorporateBody) {
      MandatoryDetail("isIncorporated", isUkIncorporated.map(answer => messages => Text(yesNo(answer)(messages))), isUkIncorporatedUrl)
    }

  private val countryOfIncorporationDetail: Option[MandatoryDetail] =
    Option.when(showNonUkIncorporation)(MandatoryDetail.text("countryOfIncorporation", countryOfIncorporation, countryOfIncorporationUrl))

  private val foreignCorporateRefDetail: Option[MandatoryDetail] =
    Option.when(showNonUkIncorporation)(MandatoryDetail.text("foreignCorporateRef", foreignCorporateRef, foreignCorporateRefUrl))

  private val dateOfIncorporationDetail: Option[MandatoryDetail] =
    Option.when(showUkIncorporation)(MandatoryDetail.date("dateOfInc", dateOfIncorporation, dateOfIncorporationUrl))

  private val crnDetail: Option[MandatoryDetail] =
    Option.when(showUkIncorporation)(MandatoryDetail.text("companyRegNumber", crn, crnUrl))

  // A partnership is not asked for a Unique Taxpayer Reference
  private val utrDetail: Option[MandatoryDetail] =
    Option.when(!businessType.contains(Partnership))(MandatoryDetail.text("utr", utr, utrUrl))

  private val addressDetail: MandatoryDetail =
    MandatoryDetail(
      "address",
      address.map(value => _ => HtmlContent(AddressFormatter.format(value).map(HtmlFormat.escape).mkString("<br>"))),
      addressUrl
    )

  private val contactNumbersDetail: MandatoryDetail =
    MandatoryDetail(
      "contactNumbers",
      Option.when(phoneNumber.isDefined || mobilePhoneNumber.isDefined)(messages => contactNumbersContent(messages)),
      contactNumbersUrl
    )

  private val mandatoryDetails: Seq[MandatoryDetail] =
    Seq(
      Some(typeOfBusinessDetail),
      nameDetail,
      dateOfBirthDetail,
      isUkIncorporatedDetail,
      countryOfIncorporationDetail,
      foreignCorporateRefDetail,
      dateOfIncorporationDetail,
      crnDetail,
      utrDetail,
      Some(addressDetail),
      Some(contactNumbersDetail)
    ).flatten

  val isMissingMandatoryDetails: Boolean = mandatoryDetails.exists(_.isMissing)

  // Continue leads to the first missing mandatory detail, otherwise back to the change registration details page
  val continueUrl: String =
    mandatoryDetails.find(_.isMissing).map(_.href).getOrElse(controllers.routes.ChangeRegistrationDetailsController.onPageLoad().url)

  def businessDetails(implicit messages: Messages): SummaryList =
    SummaryListViewModel(
      rows = Seq(
        Some(typeOfBusinessDetail.row),
        nameDetail.map(_.row),
        dateOfBirthDetail.map(_.row),
        screenerRow("addTradingName", addTradingName, addTradingNameUrl),
        optionalRow("tradingName", addTradingName, tradingName, tradingNameUrl),
        Option.when(isSoleProprietor)(screenerRow("addNino", addNino, addNinoUrl)).flatten,
        Option.when(isSoleProprietor)(optionalRow("nino", addNino, nino, ninoUrl)).flatten,
        isUkIncorporatedDetail.map(_.row),
        countryOfIncorporationDetail.map(_.row),
        foreignCorporateRefDetail.map(_.row),
        dateOfIncorporationDetail.map(_.row),
        crnDetail.map(_.row),
        utrDetail.map(_.row),
        screenerRow("addVat", addVrn, addVrnUrl),
        optionalRow("vat", addVrn, vrn, vrnUrl)
      ).flatten
    )

  def addressDetails(implicit messages: Messages): SummaryList =
    SummaryListViewModel(
      rows = Seq(
        Some(addressDetail.row),
        screenerRow("addAddrInfo", addAdditionalInfo, addAdditionalInfoUrl),
        optionalRow("addAdditionalInfo", addAdditionalInfo, additionalInfo, additionalInfoUrl)
      ).flatten
    )

  def contactDetails(implicit messages: Messages): SummaryList =
    SummaryListViewModel(
      rows = Seq(
        Some(contactNumbersDetail.row),
        screenerRow("addFaxNumber", addFaxNumber, addFaxNumberUrl),
        optionalRow("faxNumber", addFaxNumber, faxNumber, faxNumberUrl),
        screenerRow("addEmailAddress", addEmailAddress, addEmailAddressUrl),
        optionalRow("emailAddress", addEmailAddress, emailAddress, routes.ControllingBodyEmailAddressController.onPageLoad().url)
      ).flatten
    )

  // A screener question is only shown once it has been answered
  private def screenerRow(key: String, answer: Option[Boolean], href: String)(implicit messages: Messages): Option[SummaryListRow] =
    answer.map(value => changeableRow(key, Text(yesNo(value)), href))

  // An optional detail is shown when its screener was answered yes, or when there is data and the screener has not been answered yet
  private def optionalRow(key: String, screenerAnswer: Option[Boolean], value: Option[String], href: String)(implicit
    messages: Messages
  ): Option[SummaryListRow] =
    Option.when(screenerAnswer.contains(true) || (screenerAnswer.isEmpty && value.isDefined)) {
      changeableRow(key, Text(value.getOrElse(messages("site.notProvided"))), href)
    }

  private def contactNumbersContent(messages: Messages): Content = {
    def line(labelKey: String, number: Option[String]): String =
      s"${messages(s"$prefix.contactNumbers.$labelKey")}<br>${HtmlFormat.escape(number.getOrElse(messages("site.notProvided")))}"

    HtmlContent(s"${line("phoneNumber", phoneNumber)}<br><br>${line("mobilePhoneNumber", mobilePhoneNumber)}")
  }
}

object CheckControllingBodyDetailsViewModel {

  private val prefix = "checkControllingBodyDetails"

  private def businessTypeUrl: String = routes.ControllingBodyBusinessTypeController.onPageLoad().url

  private def nameUrl(businessType: BusinessType): String = routes.ChangeControllingBodyNameController.onPageLoad(businessType).url

  private def addTradingNameUrl: String = routes.ControllingBodyAddTradingNameYesNoController.onPageLoad().url

  // Screens that are not built yet, named by their codes in the specification
  private val dateOfBirthUrl = "#" // CB-DOB
  private val tradingNameUrl = "#" // CB-TN
  private val addNinoUrl = "#" // CB-NI-SC
  private val ninoUrl = "#" // CB-NI
  private val isUkIncorporatedUrl = "#" // CB-IN
  private val countryOfIncorporationUrl = "#" // CB-COU
  private val foreignCorporateRefUrl = "#" // CB-FOR
  private val dateOfIncorporationUrl = "#" // CB-DIN
  private val crnUrl = "#" // CB-CR
  private val utrUrl = "#" // CB-UTR
  private val addVrnUrl = "#" // CB-VAT-SC
  private val vrnUrl = "#" // CB-VAT
  private val addressUrl = "#" // CB-ADLK-S
  private val addAdditionalInfoUrl = "#" // CB-AI-SC
  private val additionalInfoUrl = "#" // CB-AI
  private val contactNumbersUrl = "#" // CB-CN
  private val addFaxNumberUrl = "#" // CB-FX-SC
  private val faxNumberUrl = "#" // CB-FX
  private val addEmailAddressUrl = "#" // CB-EA-SC

  // A detail that must be provided before the controlling body can be submitted, shown as a link to add it while it is missing
  private final case class MandatoryDetail(key: String, value: Option[Messages => Content], href: String) {

    val isMissing: Boolean = value.isEmpty

    def row(implicit messages: Messages): SummaryListRow =
      value match {
        case Some(content) =>
          changeableRow(key, content(messages), href)
        case None =>
          SummaryListRowViewModel(key = s"$prefix.$key", value = ValueViewModel(HtmlContent(addLink(href, messages(s"$prefix.$key.add")))))
      }
  }

  private object MandatoryDetail {

    def text(key: String, value: Option[String], href: String): MandatoryDetail =
      MandatoryDetail(key, value.map(text => _ => Text(text)), href)

    def date(key: String, value: Option[LocalDate], href: String): MandatoryDetail =
      MandatoryDetail(key, value.map(date => _ => Text(shortDateDisplay(date))), href)
  }

  private def changeableRow(key: String, content: Content, href: String)(implicit messages: Messages): SummaryListRow =
    SummaryListRowViewModel(
      key     = s"$prefix.$key",
      value   = ValueViewModel(content),
      actions = Seq(ActionItemViewModel("site.change", href).withVisuallyHiddenText(messages(s"$prefix.$key.hidden")))
    )

  private def addLink(href: String, text: String): String =
    s"""<a href="${HtmlFormat.escape(href)}" class="govuk-link">${HtmlFormat.escape(text)}</a>"""

  private def yesNo(answer: Boolean)(implicit messages: Messages): String =
    if (answer) messages("site.yes") else messages("site.no")

  def from(answers: UserAnswers): CheckControllingBodyDetailsViewModel = {

    def text(page: QuestionPage[String]): Option[String] =
      answers.get(page).map(_.trim).filter(_.nonEmpty)

    val contactNumber = answers.get(ControllingBodyContactNumberPage)

    CheckControllingBodyDetailsViewModel(
      businessType       = answers.get(ControllingBodyBusinessTypePage),
      businessName       = text(ControllingBodyBusinessNamePage),
      soleProprietorName = answers.get(ControllingBodySoleProprietorPage),
      dateOfBirth        = answers.get(ControllingBodyDateOfBirthPage),
      addTradingName     = answers.get(ControllingBodyAddTradingNameYesNoPage),
      tradingName        = text(ControllingBodyTradingNamePage),
      addNino            = answers.get(ControllingBodyAddNinoYesNoPage),
      nino               = text(ControllingBodyNinoPage),
      // The backend holds this flag as "1" or "0"
      isUkIncorporated = answers.get(ControllingBodyIsUkIncorporatedPage).collect {
        case "1" => true
        case "0" => false
      },
      countryOfIncorporation = text(ControllingBodyCountryOfIncorporationPage),
      foreignCorporateRef    = text(ControllingBodyForeignCorporateReferencePage),
      dateOfIncorporation    = answers.get(ControllingBodyDateOfIncorporationPage),
      crn                    = text(ControllingBodyCrnPage),
      utr                    = text(ControllingBodyUtrPage),
      addVrn                 = answers.get(ControllingBodyAddVrnYesNoPage),
      vrn                    = text(ControllingBodyVrnPage),
      address                = answers.get(ControllingBodyAddressPage).filter(_.address1.trim.nonEmpty),
      addAdditionalInfo      = answers.get(ControllingBodyAddAdditionalInfoYesNoPage),
      additionalInfo         = text(ControllingBodyAdditionalInfoPage),
      phoneNumber            = contactNumber.flatMap(_.phoneNumber).map(_.trim).filter(_.nonEmpty),
      mobilePhoneNumber      = contactNumber.flatMap(_.mobilePhoneNumber).map(_.trim).filter(_.nonEmpty),
      addFaxNumber           = answers.get(ControllingBodyAddFaxNumberYesNoPage),
      faxNumber              = text(ControllingBodyFaxNumberPage),
      addEmailAddress        = answers.get(ControllingBodyAddEmailAddressYesNoPage),
      emailAddress           = text(ControllingBodyEmailPage)
    )
  }
}
