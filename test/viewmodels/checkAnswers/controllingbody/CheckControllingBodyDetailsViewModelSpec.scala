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

import base.SpecBase
import controllers.controllingbody.routes
import models.BusinessType.{Corporatebody, LimitedLiabilityPartnership, Partnership, Soleproprietor, Unincorporatedbody}
import models.{Address, ContactNumber, CorrespondenceDetails, SoleProprietorName}
import org.jsoup.Jsoup
import pages.controllingbody.*
import play.api.Application
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{SummaryList, SummaryListRow}

import java.time.LocalDate

class CheckControllingBodyDetailsViewModelSpec extends SpecBase {

  // The application is built before any reverse route is used, so that the routes carry the context path
  private val application: Application = applicationBuilder().build()
  private implicit val msgs: Messages = messages(application)

  private val address = Address("18 Arundel Mews", Some("Worthing"), None, None, Some("BN11 5RG"), Some("United Kingdom"))

  // A corporate body with every detail provided, as in the add journey prototype
  private val corporateBody = CheckControllingBodyDetailsViewModel(
    businessType        = Some(Corporatebody),
    businessName        = Some("Corporate Body CB Inc"),
    addTradingName      = Some(true),
    tradingName         = Some("Controlling Traders"),
    isUkIncorporated    = Some(true),
    dateOfIncorporation = Some(LocalDate.of(2000, 1, 1)),
    crn                 = Some("CR345678"),
    utr                 = Some("1234567890"),
    addVrn              = Some(true),
    vrn                 = Some("123456789"),
    address             = Some(address),
    addAdditionalInfo   = Some(true),
    additionalInfo      = Some("4th floor, CMC Building"),
    phoneNumber         = Some("0191 202 2500"),
    mobilePhoneNumber   = Some("07890 123 456"),
    addFaxNumber        = Some(true),
    faxNumber           = Some("020 7844 4444"),
    addEmailAddress     = Some(true),
    emailAddress        = Some("abc.def@gmail.com")
  )

  // A sole proprietor who declined the optional details, as in the add journey prototype
  private val soleProprietor = corporateBody.copy(
    businessType        = Some(Soleproprietor),
    businessName        = None,
    soleProprietorName  = Some(SoleProprietorName("Mr", "Allan", None, "Warren")),
    dateOfBirth         = Some(LocalDate.of(1980, 4, 1)),
    addTradingName      = Some(false),
    tradingName         = None,
    addNino             = Some(true),
    nino                = Some("QQ 12 34 66 C"),
    isUkIncorporated    = None,
    dateOfIncorporation = None,
    crn                 = None,
    addVrn              = Some(false),
    vrn                 = None,
    addAdditionalInfo   = Some(false),
    additionalInfo      = None
  )

  private def key(name: String): String = msgs(s"checkControllingBodyDetails.$name")

  private def keys(list: SummaryList): Seq[String] = list.rows.map(_.key.content.asInstanceOf[Text].value)

  private def rowFor(list: SummaryList, name: String): SummaryListRow = list.rows.find(_.key.content == Text(key(name))).value

  private def valueHtml(row: SummaryListRow): String = row.value.content.asHtml.toString

  private def valueText(row: SummaryListRow): String = Jsoup.parse(valueHtml(row)).text()

  private def valueLink(row: SummaryListRow): Option[String] = Option(Jsoup.parse(valueHtml(row)).selectFirst("a")).map(_.attr("href"))

  private def hrefs(row: SummaryListRow): Seq[String] = row.actions.toSeq.flatMap(_.items.map(_.href))

  private def hiddenTexts(row: SummaryListRow): Seq[String] = row.actions.toSeq.flatMap(_.items.flatMap(_.visuallyHiddenText))

  private def allRows(viewModel: CheckControllingBodyDetailsViewModel): Seq[SummaryListRow] =
    viewModel.businessDetails.rows ++ viewModel.addressDetails.rows ++ viewModel.contactDetails.rows

  "CheckControllingBodyDetailsViewModel" - {

    "for a corporate body with every detail" - {

      "must show the business details rows in order" in {
        keys(corporateBody.businessDetails) mustEqual Seq(
          key("typeOfBusiness"),
          key("corporateBodyName"),
          key("addTradingName"),
          key("tradingName"),
          key("isIncorporated"),
          key("dateOfInc"),
          key("companyRegNumber"),
          key("utr"),
          key("addVat"),
          key("vat")
        )
      }

      "must show the address and contact details rows in order" in {
        keys(corporateBody.addressDetails) mustEqual Seq(key("address"), key("addAddrInfo"), key("addAdditionalInfo"))
        keys(corporateBody.contactDetails) mustEqual Seq(
          key("contactNumbers"),
          key("addFaxNumber"),
          key("faxNumber"),
          key("addEmailAddress"),
          key("emailAddress")
        )
      }

      "must show the values" in {
        val business = corporateBody.businessDetails

        valueText(rowFor(business, "typeOfBusiness")) mustEqual msgs("businessType.corporatebody")
        valueText(rowFor(business, "corporateBodyName")) mustEqual "Corporate Body CB Inc"
        valueText(rowFor(business, "addTradingName")) mustEqual msgs("site.yes")
        valueText(rowFor(business, "tradingName")) mustEqual "Controlling Traders"
        valueText(rowFor(business, "isIncorporated")) mustEqual msgs("site.yes")
        valueText(rowFor(business, "dateOfInc")) mustEqual "1 Jan 2000"
        valueText(rowFor(business, "companyRegNumber")) mustEqual "CR345678"
        valueText(rowFor(business, "utr")) mustEqual "1234567890"
        valueText(rowFor(business, "addVat")) mustEqual msgs("site.yes")
        valueText(rowFor(business, "vat")) mustEqual "123456789"

        val addressList = corporateBody.addressDetails

        valueHtml(rowFor(addressList, "address")) mustEqual "18 Arundel Mews<br>Worthing<br>BN11 5RG<br>United Kingdom"
        valueText(rowFor(addressList, "addAddrInfo")) mustEqual msgs("site.yes")
        valueText(rowFor(addressList, "addAdditionalInfo")) mustEqual "4th floor, CMC Building"

        val contact = corporateBody.contactDetails

        valueHtml(rowFor(contact, "contactNumbers")) mustEqual
          s"${key("contactNumbers.phoneNumber")}<br>0191 202 2500<br><br>${key("contactNumbers.mobilePhoneNumber")}<br>07890 123 456"
        valueText(rowFor(contact, "addFaxNumber")) mustEqual msgs("site.yes")
        valueText(rowFor(contact, "faxNumber")) mustEqual "020 7844 4444"
        valueText(rowFor(contact, "addEmailAddress")) mustEqual msgs("site.yes")
        valueText(rowFor(contact, "emailAddress")) mustEqual "abc.def@gmail.com"
      }

      "must link change to the screens that are built, and to # for the screens that are not built yet" in {
        val business = corporateBody.businessDetails

        hrefs(rowFor(business, "typeOfBusiness")) mustEqual Seq(routes.ControllingBodyBusinessTypeController.onPageLoad().url)
        hrefs(rowFor(business, "corporateBodyName")) mustEqual Seq(routes.ChangeControllingBodyNameController.onPageLoad(Corporatebody).url)
        hrefs(rowFor(business, "addTradingName")) mustEqual Seq(routes.ControllingBodyAddTradingNameYesNoController.onPageLoad().url)
        hrefs(rowFor(corporateBody.contactDetails, "emailAddress")) mustEqual Seq(routes.ControllingBodyEmailAddressController.onPageLoad().url)

        Seq("tradingName", "isIncorporated", "dateOfInc", "companyRegNumber", "utr", "addVat", "vat").foreach { name =>
          hrefs(rowFor(business, name)) mustEqual Seq("#")
        }
        Seq("address", "addAddrInfo", "addAdditionalInfo").foreach { name =>
          hrefs(rowFor(corporateBody.addressDetails, name)) mustEqual Seq("#")
        }
        Seq("contactNumbers", "addFaxNumber", "faxNumber", "addEmailAddress").foreach { name =>
          hrefs(rowFor(corporateBody.contactDetails, name)) mustEqual Seq("#")
        }
      }

      "must give every change link the visually hidden text of its row" in {
        allRows(corporateBody).foreach { row =>
          val hidden = hiddenTexts(row)
          hidden must have size 1
          // A missing message would show as the key itself
          hidden.head must not startWith "checkControllingBodyDetails."
        }
        hiddenTexts(rowFor(corporateBody.businessDetails, "typeOfBusiness")) mustEqual Seq(key("typeOfBusiness.hidden"))
        hiddenTexts(rowFor(corporateBody.businessDetails, "isIncorporated")) mustEqual Seq(key("isIncorporated.hidden"))
        hiddenTexts(rowFor(corporateBody.contactDetails, "emailAddress")) mustEqual Seq(key("emailAddress.hidden"))
      }

      "must not offer to remove any detail in the add flow" in {
        allRows(corporateBody).foreach { row =>
          row.actions.toSeq.flatMap(_.items.map(_.content)) must not contain Text(msgs("site.remove"))
        }
      }

      "must not be missing mandatory details and must continue to change registration details" in {
        corporateBody.isMissingMandatoryDetails mustBe false
        corporateBody.continueUrl mustEqual controllers.routes.ChangeRegistrationDetailsController.onPageLoad().url
      }
    }

    "for a sole proprietor" - {

      "must show the sole proprietor rows and omit the incorporation rows" in {
        val business = soleProprietor.businessDetails

        keys(business) mustEqual Seq(
          key("typeOfBusiness"),
          key("soleProprietorName"),
          key("soleProprietorDOB"),
          key("addTradingName"),
          key("addNino"),
          key("nino"),
          key("utr"),
          key("addVat")
        )
        valueText(rowFor(business, "soleProprietorName")) mustEqual "Mr Allan Warren"
        valueText(rowFor(business, "soleProprietorDOB")) mustEqual "1 Apr 1980"
        valueText(rowFor(business, "addTradingName")) mustEqual msgs("site.no")
        valueText(rowFor(business, "addNino")) mustEqual msgs("site.yes")
        valueText(rowFor(business, "nino")) mustEqual "QQ 12 34 66 C"
        hrefs(rowFor(business, "soleProprietorName")) mustEqual Seq(routes.ChangeControllingBodyNameController.onPageLoad(Soleproprietor).url)
        hrefs(rowFor(business, "soleProprietorDOB")) mustEqual Seq("#")
        hrefs(rowFor(business, "addNino")) mustEqual Seq("#")
        hrefs(rowFor(business, "nino")) mustEqual Seq("#")
        keys(soleProprietor.addressDetails) mustEqual Seq(key("address"), key("addAddrInfo"))
        soleProprietor.isMissingMandatoryDetails mustBe false
      }
    }

    "for the other types of business" - {

      "must name the row after the type of business and link it to the matching name screen" in {
        val cases = Seq(
          Unincorporatedbody          -> "unincorporatedBodyName",
          Partnership                 -> "partnershipName",
          LimitedLiabilityPartnership -> "limitedLiabilityPartnershipName"
        )

        cases.foreach { case (businessType, name) =>
          val row = rowFor(corporateBody.copy(businessType = Some(businessType)).businessDetails, name)

          valueText(row) mustEqual "Corporate Body CB Inc"
          hrefs(row) mustEqual Seq(routes.ChangeControllingBodyNameController.onPageLoad(businessType).url)
        }
      }

      "must show the incorporation details for a limited liability partnership without asking if it is incorporated in the UK" in {
        val business = corporateBody.copy(businessType = Some(LimitedLiabilityPartnership), isUkIncorporated = None).businessDetails

        keys(business) must contain allOf (key("dateOfInc"), key("companyRegNumber"))
        keys(business) must not contain key("isIncorporated")
      }

      "must show neither the incorporation details nor the National Insurance rows for an unincorporated body" in {
        val business = corporateBody.copy(businessType = Some(Unincorporatedbody), addNino = Some(true), nino = Some("QQ123456C")).businessDetails

        keys(business) mustEqual Seq(
          key("typeOfBusiness"),
          key("unincorporatedBodyName"),
          key("addTradingName"),
          key("tradingName"),
          key("utr"),
          key("addVat"),
          key("vat")
        )
      }

      "must not show the Unique Taxpayer Reference for a partnership" in {
        val partnership = corporateBody.copy(businessType = Some(Partnership), utr = None)

        keys(partnership.businessDetails) must not contain key("utr")
        partnership.isMissingMandatoryDetails mustBe false
      }
    }

    "for a corporate body incorporated outside the UK" - {

      "must show the country of incorporation and foreign corporate reference instead of the UK incorporation details" in {
        val viewModel = corporateBody.copy(
          isUkIncorporated       = Some(false),
          countryOfIncorporation = Some("Spain"),
          foreignCorporateRef    = Some("ES123"),
          dateOfIncorporation    = None,
          crn                    = None
        )
        val business = viewModel.businessDetails

        keys(business) must contain inOrder (key("isIncorporated"), key("countryOfIncorporation"), key("foreignCorporateRef"), key("utr"))
        keys(business) must contain noneOf (key("dateOfInc"), key("companyRegNumber"))
        valueText(rowFor(business, "isIncorporated")) mustEqual msgs("site.no")
        valueText(rowFor(business, "countryOfIncorporation")) mustEqual "Spain"
        valueText(rowFor(business, "foreignCorporateRef")) mustEqual "ES123"
        hrefs(rowFor(business, "countryOfIncorporation")) mustEqual Seq("#")
        hrefs(rowFor(business, "foreignCorporateRef")) mustEqual Seq("#")
        viewModel.isMissingMandatoryDetails mustBe false
      }

      "must show neither set of incorporation details until the question is answered" in {
        val viewModel = corporateBody.copy(isUkIncorporated = None)

        keys(viewModel.businessDetails) must contain noneOf (
          key("countryOfIncorporation"),
          key("foreignCorporateRef"),
          key("dateOfInc"),
          key("companyRegNumber")
        )
        valueText(rowFor(viewModel.businessDetails, "isIncorporated")) mustEqual key("isIncorporated.add")
        viewModel.isMissingMandatoryDetails mustBe true
        viewModel.continueUrl mustEqual "#"
      }
    }

    "screener questions" - {

      "must be hidden until answered, while a detail with data is still shown" in {
        val viewModel = corporateBody.copy(
          addTradingName    = None,
          addVrn            = None,
          vrn               = None,
          addAdditionalInfo = None,
          addFaxNumber      = None,
          addEmailAddress   = None,
          emailAddress      = None
        )

        keys(viewModel.businessDetails) must contain(key("tradingName"))
        keys(viewModel.businessDetails) must contain noneOf (key("addTradingName"), key("addVat"), key("vat"))
        keys(viewModel.addressDetails) mustEqual Seq(key("address"), key("addAdditionalInfo"))
        keys(viewModel.contactDetails) mustEqual Seq(key("contactNumbers"), key("faxNumber"))
      }

      "must hide the detail when answered no" in {
        val viewModel = corporateBody.copy(
          addTradingName    = Some(false),
          addVrn            = Some(false),
          addAdditionalInfo = Some(false),
          addFaxNumber      = Some(false),
          addEmailAddress   = Some(false)
        )

        keys(viewModel.businessDetails) must contain allOf (key("addTradingName"), key("addVat"))
        keys(viewModel.businessDetails) must contain noneOf (key("tradingName"), key("vat"))
        keys(viewModel.addressDetails) mustEqual Seq(key("address"), key("addAddrInfo"))
        keys(viewModel.contactDetails) mustEqual Seq(key("contactNumbers"), key("addFaxNumber"), key("addEmailAddress"))
        valueText(rowFor(viewModel.businessDetails, "addTradingName")) mustEqual msgs("site.no")
      }

      "must show the detail as not provided when answered yes without data" in {
        val viewModel = corporateBody.copy(tradingName = None, vrn = None, additionalInfo = None, faxNumber = None, emailAddress = None)

        valueText(rowFor(viewModel.businessDetails, "tradingName")) mustEqual msgs("site.notProvided")
        valueText(rowFor(viewModel.businessDetails, "vat")) mustEqual msgs("site.notProvided")
        valueText(rowFor(viewModel.addressDetails, "addAdditionalInfo")) mustEqual msgs("site.notProvided")
        valueText(rowFor(viewModel.contactDetails, "faxNumber")) mustEqual msgs("site.notProvided")
        valueText(rowFor(viewModel.contactDetails, "emailAddress")) mustEqual msgs("site.notProvided")
        viewModel.isMissingMandatoryDetails mustBe false
      }
    }

    "missing mandatory details" - {

      "must show an add link in place of the value and the change link" in {
        val viewModel = corporateBody.copy(businessName = None, utr = None, address = None, phoneNumber = None, mobilePhoneNumber = None)

        val nameRow = rowFor(viewModel.businessDetails, "corporateBodyName")
        valueText(nameRow) mustEqual key("corporateBodyName.add")
        valueLink(nameRow) mustEqual Some(routes.ChangeControllingBodyNameController.onPageLoad(Corporatebody).url)
        nameRow.actions mustBe None

        valueText(rowFor(viewModel.businessDetails, "utr")) mustEqual key("utr.add")
        valueText(rowFor(viewModel.addressDetails, "address")) mustEqual key("address.add")
        valueText(rowFor(viewModel.contactDetails, "contactNumbers")) mustEqual key("contactNumbers.add")
        viewModel.isMissingMandatoryDetails mustBe true
      }

      "must continue to the first missing detail" in {
        corporateBody.copy(businessName = None, utr = None).continueUrl mustEqual
          routes.ChangeControllingBodyNameController.onPageLoad(Corporatebody).url
        corporateBody.copy(utr = None, address = None).continueUrl mustEqual "#"
      }

      "must only ask for the type of business while it is not known" in {
        val viewModel = corporateBody.copy(businessType = None)

        keys(viewModel.businessDetails) mustEqual Seq(
          key("typeOfBusiness"),
          key("addTradingName"),
          key("tradingName"),
          key("utr"),
          key("addVat"),
          key("vat")
        )
        valueText(rowFor(viewModel.businessDetails, "typeOfBusiness")) mustEqual key("typeOfBusiness.add")
        valueLink(rowFor(viewModel.businessDetails, "typeOfBusiness")) mustEqual Some(routes.ControllingBodyBusinessTypeController.onPageLoad().url)
        viewModel.isMissingMandatoryDetails mustBe true
        viewModel.continueUrl mustEqual routes.ControllingBodyBusinessTypeController.onPageLoad().url
      }

      "must include the sole proprietor's date of birth and the incorporation details" in {
        val withoutDateOfBirth = soleProprietor.copy(dateOfBirth = None)

        valueText(rowFor(withoutDateOfBirth.businessDetails, "soleProprietorDOB")) mustEqual key("soleProprietorDOB.add")
        withoutDateOfBirth.isMissingMandatoryDetails mustBe true
        corporateBody.copy(crn = None).isMissingMandatoryDetails mustBe true
        corporateBody.copy(dateOfIncorporation = None).isMissingMandatoryDetails mustBe true
        corporateBody.copy(isUkIncorporated = Some(false), countryOfIncorporation = Some("Spain")).isMissingMandatoryDetails mustBe true
        corporateBody.copy(isUkIncorporated = Some(false), foreignCorporateRef = Some("ES123")).isMissingMandatoryDetails mustBe true
        corporateBody.copy(businessType = Some(LimitedLiabilityPartnership), crn = None).isMissingMandatoryDetails mustBe true
      }

      "must accept a single contact number and show the other as not provided" in {
        val viewModel = corporateBody.copy(mobilePhoneNumber = None)

        viewModel.isMissingMandatoryDetails mustBe false
        valueHtml(rowFor(viewModel.contactDetails, "contactNumbers")) mustEqual
          s"${key("contactNumbers.phoneNumber")}<br>0191 202 2500<br><br>${key("contactNumbers.mobilePhoneNumber")}<br>${msgs("site.notProvided")}"
      }

      "must escape the details" in {
        val viewModel = corporateBody.copy(businessName = Some("<b>Bold</b> & Co"),
                                           address      = Some(address.copy(address1 = "1 <High> Street")),
                                           phoneNumber  = Some("<1>")
                                          )

        valueText(rowFor(viewModel.businessDetails, "corporateBodyName")) mustEqual "<b>Bold</b> & Co"
        valueHtml(rowFor(viewModel.addressDetails, "address"))        must startWith("1 &lt;High&gt; Street<br>")
        valueHtml(rowFor(viewModel.contactDetails, "contactNumbers")) must include("&lt;1&gt;")
      }
    }

    "from user answers" - {

      "must read the controlling body details as stored by the data required action and the screens" in {
        val correspondence = CorrespondenceDetails(
          mgdRegNumber          = mgdRegNum,
          nameLine1             = None,
          nameLine2             = None,
          correspondenceAddress = Some(address),
          additionalInformation = Some("4th floor, CMC Building"),
          iomOrCiFlag           = None,
          contactNumber         = Some(ContactNumber(Some("0191 202 2500"), Some("07890 123 456"))),
          faxNumber             = Some("020 7844 4444"),
          emailAddr             = Some("abc.def@gmail.com")
        )

        val answers = (for {
          answers <- emptyUserAnswers.set(ControllingBodySectionPage, mgdRegNum)
          answers <- answers.set(ControllingBodyBusinessTypePage, Corporatebody)
          answers <- answers.set(ControllingBodyBusinessNamePage, "Corporate Body CB Inc")
          answers <- answers.set(ControllingBodySoleProprietorPage, SoleProprietorName("Mr", "Allan", None, "Warren"))
          answers <- answers.set(ControllingBodyDateOfBirthPage, LocalDate.of(1980, 4, 1))
          answers <- answers.set(ControllingBodyAddTradingNameYesNoPage, true)
          answers <- answers.set(ControllingBodyTradingNamePage, "Controlling Traders")
          answers <- answers.set(ControllingBodyAddNinoYesNoPage, true)
          answers <- answers.set(ControllingBodyNinoPage, "QQ123456C")
          answers <- answers.set(ControllingBodyIsUkIncorporatedPage, "0")
          answers <- answers.set(ControllingBodyCountryOfIncorporationPage, "Spain")
          answers <- answers.set(ControllingBodyForeignCorporateReferencePage, "ES123")
          answers <- answers.set(ControllingBodyDateOfIncorporationPage, LocalDate.of(2000, 1, 1))
          answers <- answers.set(ControllingBodyCrnPage, "CR345678")
          answers <- answers.set(ControllingBodyUtrPage, "1234567890")
          answers <- answers.set(ControllingBodyAddVrnYesNoPage, true)
          answers <- answers.set(ControllingBodyVrnPage, "123456789")
          answers <- answers.set(ControllingBodyCorrespondenceSectionPage, correspondence)
          answers <- answers.set(ControllingBodyAddAdditionalInfoYesNoPage, true)
          answers <- answers.set(ControllingBodyAddFaxNumberYesNoPage, false)
          answers <- answers.set(ControllingBodyAddEmailAddressYesNoPage, true)
        } yield answers).success.value

        CheckControllingBodyDetailsViewModel.from(answers) mustEqual corporateBody.copy(
          soleProprietorName     = Some(SoleProprietorName("Mr", "Allan", None, "Warren")),
          dateOfBirth            = Some(LocalDate.of(1980, 4, 1)),
          addNino                = Some(true),
          nino                   = Some("QQ123456C"),
          isUkIncorporated       = Some(false),
          countryOfIncorporation = Some("Spain"),
          foreignCorporateRef    = Some("ES123"),
          addFaxNumber           = Some(false)
        )
      }

      "must read a UK incorporated flag of 1 as yes and treat blank values as missing" in {
        val answers = (for {
          answers <- emptyUserAnswers.set(ControllingBodyIsUkIncorporatedPage, "1")
          answers <- answers.set(ControllingBodyUtrPage, " ")
          answers <- answers.set(ControllingBodyTradingNamePage, "")
          answers <- answers.set(
                       ControllingBodyCorrespondenceSectionPage,
                       CorrespondenceDetails(mgdRegNum,
                                             None,
                                             None,
                                             Some(address.copy(address1 = " ")),
                                             None,
                                             None,
                                             Some(ContactNumber(Some(" "), None)),
                                             None,
                                             None
                                            )
                     )
        } yield answers).success.value

        val viewModel = CheckControllingBodyDetailsViewModel.from(answers)

        viewModel.isUkIncorporated mustBe Some(true)
        viewModel.utr mustBe None
        viewModel.tradingName mustBe None
        viewModel.address mustBe None
        viewModel.phoneNumber mustBe None
      }

      "must have no details when there are no answers" in {
        val viewModel = CheckControllingBodyDetailsViewModel.from(emptyUserAnswers)

        viewModel mustEqual CheckControllingBodyDetailsViewModel()
        viewModel.isMissingMandatoryDetails mustBe true
        viewModel.continueUrl mustEqual routes.ControllingBodyBusinessTypeController.onPageLoad().url
      }
    }
  }
}
