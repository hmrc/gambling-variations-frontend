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

package navigation

import base.SpecBase
import controllers.partnerdetails.PartnerDetailsHelper
import controllers.routes
import models.*
import models.BusinessType.Corporatebody
import models.CorrespondenceChangeAddrOption.*
import models.controllingbody.ControllingBodyChangeOption.*
import pages.*
import pages.businessaddress.*
import pages.businessname.*
import pages.contactdetails.*
import pages.controllingbody.{ControllingBodyAddTradingNameYesNoPage, ControllingBodyChangeScreenerPage}
import pages.correspondencedetails.*
import pages.licencespremises.*
import pages.partnerdetails.*
import pages.tradingdetails.*
import pages.tradingdetails.associatedregnumbers.*
import pages.tradingdetails.previousregnumbers.*
import play.api.libs.json.Json

class NavigatorSpec extends SpecBase with PartnerDetailsHelper {

  private val navigator = new Navigator
  private val emptyAnswers = UserAnswers("id")

  "Navigator" - {

    "in Normal mode" - {

      "should route an unknown page to Index" in {
        case object UnknownPage extends Page

        navigator.nextPage(UnknownPage, NormalMode, emptyAnswers) mustBe
          routes.IndexController.onPageLoad()
      }

      "should route TradingNamePage to CheckBusinessName" in {
        navigator.nextPage(TradingNamePage, NormalMode, emptyAnswers) mustBe
          routes.CheckBusinessNameController.onPageLoad()
      }

      "should route BusinessNamePage to CheckBusinessName" in {
        navigator.nextPage(BusinessNamePage, NormalMode, emptyAnswers) mustBe
          routes.CheckBusinessNameController.onPageLoad()
      }

      "should route RemoveTradeNamePage to CheckBusinessName" in {
        navigator.nextPage(RemoveTradeNamePage, NormalMode, emptyAnswers) mustBe
          routes.CheckBusinessNameController.onPageLoad()
      }

      "should route SoleProprietorPage to ChangeBusinessName" in {
        navigator.nextPage(SoleProprietorPage, NormalMode, emptyAnswers) mustBe
          routes.ChangeBusinessNameController.onPageLoad(BusinessType.Soleproprietor)
      }

      "should route BusinessContactNumberPage to CheckContactDetails" in {
        navigator.nextPage(BusinessContactNumberPage, NormalMode, emptyAnswers) mustBe
          controllers.businesscontactdetails.routes.CheckContactDetailsController.onPageLoad()
      }

      "should route BusinessEmailAddressPage to CheckContactDetails" in {
        navigator.nextPage(BusinessEmailAddressPage, NormalMode, emptyAnswers) mustBe
          controllers.businesscontactdetails.routes.CheckContactDetailsController.onPageLoad()
      }

      "should route BusinessFaxNumberPage to CheckContactDetails" in {
        navigator.nextPage(BusinessFaxNumberPage, NormalMode, emptyAnswers) mustBe
          controllers.businesscontactdetails.routes.CheckContactDetailsController.onPageLoad()
      }

      "should route RemoveEmailAddressPage to CheckContactDetails" in {
        navigator.nextPage(RemoveEmailAddressPage, NormalMode, emptyAnswers) mustBe
          controllers.businesscontactdetails.routes.CheckContactDetailsController.onPageLoad()
      }

      "should route RemoveFaxNumberPage to CheckContactDetails" in {
        navigator.nextPage(RemoveFaxNumberPage, NormalMode, emptyAnswers) mustBe
          controllers.businesscontactdetails.routes.CheckContactDetailsController.onPageLoad()
      }

      "should route BusinessTradeClassPage to CheckTradingDetails" in {
        navigator.nextPage(BusinessTradeClassPage, NormalMode, emptyAnswers) mustBe
          routes.CheckTradingDetailsController.onPageLoad()
      }

      "should route OtherTradeClassPage to CheckTradingDetails" in {
        navigator.nextPage(OtherTradeClassPage, NormalMode, emptyAnswers) mustBe
          routes.CheckTradingDetailsController.onPageLoad()
      }

      "should route IsSeasonalBusinessPage to CheckTradingDetails" in {
        navigator.nextPage(IsSeasonalBusinessPage, NormalMode, emptyAnswers) mustBe
          routes.CheckTradingDetailsController.onPageLoad()
      }

      "should route AddAssociatedRegistrationNumberPage to AssociatedRegNumber when answer is true" in {
        val answers =
          emptyAnswers
            .set(AddAssociatedRegistrationNumberPage, true)
            .success
            .value

        navigator.nextPage(AddAssociatedRegistrationNumberPage, NormalMode, answers) mustBe
          routes.AssociatedRegNumberController.onPageLoad()
      }

      "should route AddAssociatedRegistrationNumberPage to CheckTradingDetails when answer is false" in {
        val answers =
          emptyAnswers
            .set(AddAssociatedRegistrationNumberPage, false)
            .success
            .value

        navigator.nextPage(AddAssociatedRegistrationNumberPage, NormalMode, answers) mustBe
          routes.CheckTradingDetailsController.onPageLoad()
      }

      "should route AddAssociatedRegistrationNumberPage to SystemError when unanswered" in {
        navigator.nextPage(AddAssociatedRegistrationNumberPage, NormalMode, emptyAnswers) mustBe
          routes.SystemErrorController.onPageLoad()
      }

      "should route AssociatedRegNumberPage to AssociatedRegistrationNumbersList" in {
        navigator.nextPage(AssociatedRegNumberPage, NormalMode, emptyAnswers) mustBe
          routes.AssociatedRegistrationNumbersListController.onPageLoad()
      }

      "should route AssociatedRegistrationNumbersPage to AssociatedRegistrationNumbersList" in {
        navigator.nextPage(AssociatedRegistrationNumbersPage, NormalMode, emptyAnswers) mustBe
          routes.AssociatedRegistrationNumbersListController.onPageLoad()
      }

      "should route RemoveAssociatedRegNumberPage to AssociatedRegistrationNumbersList when registration numbers exist" in {
        val answers =
          emptyAnswers
            .set(AssociatedRegistrationNumbersPage, Seq("ABC123"))
            .success
            .value

        navigator.nextPage(RemoveAssociatedRegNumberPage, NormalMode, answers) mustBe
          routes.AssociatedRegistrationNumbersListController.onPageLoad()
      }

      "should route RemoveAssociatedRegNumberPage to CheckTradingDetails when registration numbers are empty" in {
        val answers =
          emptyAnswers
            .set(AssociatedRegistrationNumbersPage, Seq.empty[String])
            .success
            .value

        navigator.nextPage(RemoveAssociatedRegNumberPage, NormalMode, answers) mustBe
          routes.CheckTradingDetailsController.onPageLoad()
      }

      "should route RemoveAssociatedRegNumberPage to CheckTradingDetails when registration numbers are missing" in {
        navigator.nextPage(RemoveAssociatedRegNumberPage, NormalMode, emptyAnswers) mustBe
          routes.CheckTradingDetailsController.onPageLoad()
      }

      "should route PreviousRegNumberPage to PreviousRegistrationNumber" in {
        navigator.nextPage(PreviousRegNumberPage, NormalMode, emptyAnswers) mustBe
          routes.PreviousRegistrationNumberController.onPageLoad()
      }

      "should route AddPreviousRegistrationNumberPage to PreviousRegistrationNumber when answer is true" in {
        val answers =
          emptyAnswers
            .set(AddPreviousRegistrationNumberPage, true)
            .success
            .value

        navigator.nextPage(AddPreviousRegistrationNumberPage, NormalMode, answers) mustBe
          routes.PreviousRegistrationNumberController.onPageLoad()
      }

      "should route AddPreviousRegistrationNumberPage to CheckTradingDetails when answer is false" in {
        val answers =
          emptyAnswers
            .set(AddPreviousRegistrationNumberPage, false)
            .success
            .value

        navigator.nextPage(AddPreviousRegistrationNumberPage, NormalMode, answers) mustBe
          routes.CheckTradingDetailsController.onPageLoad()
      }

      "should route AddPreviousRegistrationNumberPage to SystemError when unanswered" in {
        navigator.nextPage(AddPreviousRegistrationNumberPage, NormalMode, emptyAnswers) mustBe
          routes.SystemErrorController.onPageLoad()
      }

      "should route RemovePreviousRegNumberPage to PreviousRegistrationNumbersList" in {
        navigator.nextPage(RemovePreviousRegNumberPage, NormalMode, emptyAnswers) mustBe
          routes.PreviousRegistrationNumbersListController.onPageLoad()
      }

      "should route LicencesNumberPage to CheckLicencesAndPremisesController" in {
        navigator.nextPage(LicenceNumberPage, NormalMode, emptyAnswers) mustBe
          controllers.licencespremises.routes.CheckLicencesAndPremisesController.onPageLoad()
      }

      "should route RemoveLicencesNumberPage to CheckLicencesAndPremisesController" in {
        navigator.nextPage(RemoveLicenceNumberPage, NormalMode, emptyAnswers) mustBe
          controllers.licencespremises.routes.CheckLicencesAndPremisesController.onPageLoad()
      }

      "should route a licence change to PremisesNotCoveredYesNoController when the premises not covered question is in scope and unanswered" in {
        val inScope = emptyAnswers.set(LicenceHeldByLandlordPage, "1").success.value

        Seq(LicenceNumberPage, LicenceHeldByLandlordPage, OtherLicencesAndPermitsGBPage, OtherLicencesAndPermitsNIPage).foreach { page =>
          navigator.nextPage(page, NormalMode, inScope) mustBe
            controllers.licencespremises.routes.PremisesNotCoveredYesNoController.onPageLoad()
        }
      }

      "should route a licence change to CheckLicencesAndPremisesController when the premises not covered question has been answered" in {
        val answered = emptyAnswers
          .set(LicenceHeldByLandlordPage, "1")
          .success
          .value
          .set(LicencePremisesNotCoveredPage, "0")
          .success
          .value

        navigator.nextPage(LicenceHeldByLandlordPage, NormalMode, answered) mustBe
          controllers.licencespremises.routes.CheckLicencesAndPremisesController.onPageLoad()
      }

      "should route a licence change to CheckLicencesAndPremisesController when the premises not covered question is out of scope" in {
        val outOfScope = emptyAnswers.set(LicenceHeldByLandlordPage, "0").success.value

        navigator.nextPage(LicenceHeldByLandlordPage, NormalMode, outOfScope) mustBe
          controllers.licencespremises.routes.CheckLicencesAndPremisesController.onPageLoad()
      }

      "should route LicenceHeldByLandlordPage to CheckLicencesAndPremisesController" in {
        navigator.nextPage(LicenceHeldByLandlordPage, NormalMode, emptyAnswers) mustBe
          controllers.licencespremises.routes.CheckLicencesAndPremisesController.onPageLoad()
      }

      "should route OtherLicencesAndPermitsGBPage to CheckLicencesAndPremisesController" in {
        navigator.nextPage(OtherLicencesAndPermitsGBPage, NormalMode, emptyAnswers) mustBe
          controllers.licencespremises.routes.CheckLicencesAndPremisesController.onPageLoad()
      }

      "should route OtherLicencesAndPermitsNIPage to CheckLicencesAndPremisesController" in {
        navigator.nextPage(OtherLicencesAndPermitsNIPage, NormalMode, emptyAnswers) mustBe
          controllers.licencespremises.routes.CheckLicencesAndPremisesController.onPageLoad()
      }

      "should route LicencePremisesNotCoveredPage to CheckLicencesAndPremisesController" in {
        navigator.nextPage(LicencePremisesNotCoveredPage, NormalMode, emptyAnswers) mustBe
          controllers.licencespremises.routes.CheckLicencesAndPremisesController.onPageLoad()
      }

      "should route LicencesPremisesPage to CheckLicencesAndPremisesController" in {
        navigator.nextPage(LicencesPremisesPage, NormalMode, emptyAnswers) mustBe
          controllers.licencespremises.routes.CheckLicencesAndPremisesController.onPageLoad()
      }

      "should route RemovePremisesDetailsYesNoPage to CheckLicencesAndPremisesController" in {
        navigator.nextPage(RemovePremisesDetailsYesNoPage, NormalMode, emptyAnswers) mustBe
          controllers.licencespremises.routes.CheckLicencesAndPremisesController.onPageLoad()
      }

    }

    "normal mode correspondence details navigation" - {

      "should route AddCorrespondingDetailsYesNoPage to CorrespondenceName when answer is true" in {
        val answers =
          emptyAnswers
            .set(AddCorrespondingDetailsYesNoPage, true)
            .success
            .value

        navigator.nextPage(AddCorrespondingDetailsYesNoPage, NormalMode, answers) mustBe
          routes.CorrespondenceNameController.onPageLoad()
      }

      "should route AddCorrespondingDetailsYesNoPage to ChangeRegistrationDetails when answer is false" in {
        val answers =
          emptyAnswers
            .set(AddCorrespondingDetailsYesNoPage, false)
            .success
            .value

        navigator.nextPage(AddCorrespondingDetailsYesNoPage, NormalMode, answers) mustBe
          routes.ChangeRegistrationDetailsController.onPageLoad()
      }

      "should route AddCorrespondingDetailsYesNoPage to SystemError when unanswered" in {
        navigator.nextPage(AddCorrespondingDetailsYesNoPage, NormalMode, emptyAnswers) mustBe
          routes.SystemErrorController.onPageLoad()
      }

      "should route CorrespondenceNamePage to CheckCorrespondenceDetails outside the add correspondence journey" in {
        navigator.nextPage(CorrespondenceNamePage, NormalMode, emptyAnswers) mustBe
          routes.CheckCorrespondenceDetailsController.onPageLoad()
      }

      "should route CorrespondenceNamePage to CorrespondenceAdditionalNameYesNo when adding correspondence details" in {
        val answers =
          emptyAnswers
            .set(AddCorrespondingDetailsYesNoPage, true)
            .success
            .value

        navigator.nextPage(CorrespondenceNamePage, NormalMode, answers) mustBe
          routes.CorrespondenceAdditionalNameYesNoController.onPageLoad()
      }

      "should route CorrespondenceAdditionalNameYesNoPage to CorrespondenceAdditionalName when answer is true" in {
        val answers =
          emptyAnswers
            .set(CorrespondenceAdditionalNameYesNoPage, true)
            .success
            .value

        navigator.nextPage(CorrespondenceAdditionalNameYesNoPage, NormalMode, answers) mustBe
          routes.CorrespondenceAdditionalNameController.onPageLoad()
      }

      "should route CorrespondenceAdditionalNameYesNoPage to CheckCorrespondenceDetails when answer is false outside the add correspondence journey" in {
        val answers =
          emptyAnswers
            .set(CorrespondenceAdditionalNameYesNoPage, false)
            .success
            .value

        navigator.nextPage(CorrespondenceAdditionalNameYesNoPage, NormalMode, answers) mustBe
          routes.CheckCorrespondenceDetailsController.onPageLoad()
      }

      "should route CorrespondenceAdditionalNameYesNoPage to CorrespondenceUKAddrScreener when answer is false and adding correspondence details" in {
        val answers =
          emptyAnswers
            .set(CorrespondenceAdditionalNameYesNoPage, false)
            .success
            .value
            .set(AddCorrespondingDetailsYesNoPage, true)
            .success
            .value

        navigator.nextPage(CorrespondenceAdditionalNameYesNoPage, NormalMode, answers) mustBe
          routes.CorrespondenceUKAddrScreenerController.onPageLoad()
      }

      "should route CorrespondenceAdditionalNameYesNoPage to SystemError when unanswered" in {
        navigator.nextPage(CorrespondenceAdditionalNameYesNoPage, NormalMode, emptyAnswers) mustBe
          routes.SystemErrorController.onPageLoad()
      }

      "should route CorrespondenceAdditionalNamePage to CheckCorrespondenceDetails outside the add correspondence journey" in {
        navigator.nextPage(CorrespondenceAdditionalNamePage, NormalMode, emptyAnswers) mustBe
          routes.CheckCorrespondenceDetailsController.onPageLoad()
      }

      "should route CorrespondenceAdditionalNamePage to CorrespondenceUKAddrScreener when adding correspondence details" in {
        val answers =
          emptyAnswers
            .set(AddCorrespondingDetailsYesNoPage, true)
            .success
            .value

        navigator.nextPage(CorrespondenceAdditionalNamePage, NormalMode, answers) mustBe
          routes.CorrespondenceUKAddrScreenerController.onPageLoad()
      }

      "should route CorrespondenceUKAddrScreenerPage to Address Lookup when answer is true" in {
        val answers =
          emptyAnswers
            .set(CorrespondenceUKAddrScreenerPage, true)
            .success
            .value

        navigator.nextPage(CorrespondenceUKAddrScreenerPage, NormalMode, answers) mustBe
          routes.AddressLookupController.initialise()
      }

      "should route CorrespondenceUKAddrScreenerPage to CorrespondenceNonUKAddress when answer is false" in {
        val answers =
          emptyAnswers
            .set(CorrespondenceUKAddrScreenerPage, false)
            .success
            .value

        navigator.nextPage(CorrespondenceUKAddrScreenerPage, NormalMode, answers) mustBe
          routes.CorrespondenceNonUKAddressController.onPageLoad()
      }

      "should route CorrespondenceUKAddrScreenerPage to SystemError when unanswered" in {
        navigator.nextPage(CorrespondenceUKAddrScreenerPage, NormalMode, emptyAnswers) mustBe
          routes.SystemErrorController.onPageLoad()
      }

      "should route BusinessUKAddrScreenerPage to BusinessUKAddress when answer is true" in {
        val answers =
          emptyAnswers
            .set(BusinessUKAddrScreenerPage, true)
            .success
            .value

        navigator.nextPage(BusinessUKAddrScreenerPage, NormalMode, answers) mustBe
          routes.BusinessUKAddressController.onPageLoad()
      }

      "should route BusinessUKAddrScreenerPage to BusinessNonUkAddressScreener when answer is false" in {
        val answers =
          emptyAnswers
            .set(BusinessUKAddrScreenerPage, false)
            .success
            .value

        navigator.nextPage(BusinessUKAddrScreenerPage, NormalMode, answers) mustBe
          routes.BusinessNonUKAddressController.onPageLoad()
      }

      "should route BusinessAddressAdditionalInformationPage to CheckBusinessAddress when answered" in {
        val answers =
          emptyAnswers
            .set(BusinessAddressAdditionalInformationPage, "INFORMATION")
            .success
            .value

        navigator.nextPage(BusinessAddressAdditionalInformationPage, NormalMode, answers) mustBe
          routes.CheckBusinessAddressController.onPageLoad()
      }

      "should route BusinessUKAddrScreenerPage to BusinessNonUKAddress when answer is false" in {
        val answers =
          emptyAnswers
            .set(BusinessUKAddrScreenerPage, false)
            .success
            .value

        navigator.nextPage(BusinessUKAddrScreenerPage, NormalMode, answers) mustBe
          routes.BusinessNonUKAddressController.onPageLoad()
      }

      "should route BusinessUKAddrScreenerPage to SystemError when unanswered" in {
        navigator.nextPage(BusinessUKAddrScreenerPage, NormalMode, emptyAnswers) mustBe
          routes.SystemErrorController.onPageLoad()
      }

      "should route BusinessAddressUkPage to BusinessAddrInfoScreener if in Add flow" in {
        val ua = UserAnswers("id",
                             Json.obj(
                               "mgdRegNumber" -> "ABC00000001",
                               "businessAddressSection" -> Json.obj(
                                 "isInAddFlow" -> true
                               )
                             )
                            )

        navigator.nextPage(BusinessAddressUkPage, NormalMode, ua) mustBe
          routes.BusinessAddrInfoScreenerController.onPageLoad()
      }

      "should route BusinessAddressNonUkPage to BusinessAddrInfoScreener if in Add flow" in {
        val ua = UserAnswers("id",
                             Json.obj(
                               "mgdRegNumber" -> "ABC00000001",
                               "businessAddressSection" -> Json.obj(
                                 "isInAddFlow" -> true
                               )
                             )
                            )

        navigator.nextPage(BusinessAddressNonUkPage, NormalMode, ua) mustBe
          routes.BusinessAddrInfoScreenerController.onPageLoad()
      }

      "should route BusinessAddressUk or NonUk to BusinessAddressPage if in normal flow" in {

        navigator.nextPage(BusinessAddressNonUkPage, NormalMode, emptyAnswers) mustBe
          routes.CheckBusinessAddressController.onPageLoad()
      }

      "should route CorrespondenceAddressUkPage to CorrespondenceAddrInfoScreener when adding correspondence details" in {
        val answers =
          emptyAnswers
            .set(AddCorrespondingDetailsYesNoPage, true)
            .success
            .value

        navigator.nextPage(CorrespondenceAddressUkPage, NormalMode, answers) mustBe
          routes.CorrespondenceAddrInfoScreenerController.onPageLoad()
      }

      "should route CorrespondenceAddressUkPage to CheckCorrespondenceDetails outside the add correspondence journey" in {
        val answers =
          emptyAnswers
            .set(AddCorrespondingDetailsYesNoPage, false)
            .success
            .value

        navigator.nextPage(CorrespondenceAddressUkPage, NormalMode, answers) mustBe
          routes.CheckCorrespondenceDetailsController.onPageLoad()
      }

      "should route CorrespondenceAddressNonUkPage to CorrespondenceAddrInfoScreener when adding correspondence details" in {
        val answers =
          emptyAnswers
            .set(AddCorrespondingDetailsYesNoPage, true)
            .success
            .value

        navigator.nextPage(CorrespondenceAddressNonUkPage, NormalMode, answers) mustBe
          routes.CorrespondenceAddrInfoScreenerController.onPageLoad()
      }

      "should route CorrespondenceAddressNonUkPage to CheckCorrespondenceDetails outside the add correspondence journey" in {
        val answers =
          emptyAnswers
            .set(AddCorrespondingDetailsYesNoPage, false)
            .success
            .value

        navigator.nextPage(CorrespondenceAddressNonUkPage, NormalMode, answers) mustBe
          routes.CheckCorrespondenceDetailsController.onPageLoad()
      }

      "should route CorrespondenceChangeAddrScreenerPage to CorrespondenceNonUKAddress when changing from a UK address to a non-UK address" in {
        val answers =
          emptyAnswers
            .set(
              CorrespondenceAddressUkPage,
              Address(
                address1 = "line1",
                address2 = Some("line2"),
                address3 = None,
                address4 = None,
                postcode = Some("AA1 1AA"),
                country  = Some("GB")
              )
            )
            .success
            .value
            .set(CorrespondenceChangeAddrScreenerPage, ChangeToNonUkAddress)
            .success
            .value

        navigator.nextPage(CorrespondenceChangeAddrScreenerPage, NormalMode, answers) mustBe
          routes.CorrespondenceNonUKAddressController.onPageLoad()
      }

      "should route CorrespondenceChangeAddrScreenerPage to Address Lookup when changing to a different UK address" in {
        val answers =
          emptyAnswers
            .set(
              CorrespondenceAddressUkPage,
              Address(
                address1 = "line1",
                address2 = Some("line2"),
                address3 = None,
                address4 = None,
                postcode = Some("AA1 1AA"),
                country  = Some("GB")
              )
            )
            .success
            .value
            .set(CorrespondenceChangeAddrScreenerPage, DifferentUkAddress)
            .success
            .value

        navigator.nextPage(CorrespondenceChangeAddrScreenerPage, NormalMode, answers) mustBe
          routes.AddressLookupController.initialise()
      }

      "should route CorrespondenceChangeAddrScreenerPage to CorrespondenceUKAddress when editing a UK address" in {
        val answers =
          emptyAnswers
            .set(
              CorrespondenceAddressUkPage,
              Address(
                address1 = "line1",
                address2 = Some("line2"),
                address3 = None,
                address4 = None,
                postcode = Some("AA1 1AA"),
                country  = Some("GB")
              )
            )
            .success
            .value
            .set(CorrespondenceChangeAddrScreenerPage, EditCurrentAddress)
            .success
            .value

        navigator.nextPage(CorrespondenceChangeAddrScreenerPage, NormalMode, answers) mustBe
          routes.CorrespondenceUKAddressController.onPageLoad()
      }

      "should route CorrespondenceChangeAddrScreenerPage to Address Lookup when changing from a non-UK address" in {
        val answers =
          emptyAnswers
            .set(CorrespondenceChangeAddrScreenerPage, ChangeToUkAddress)
            .success
            .value

        navigator.nextPage(CorrespondenceChangeAddrScreenerPage, NormalMode, answers) mustBe
          routes.AddressLookupController.initialise()
      }

      "should route CorrespondenceChangeAddrScreenerPage to CorrespondenceNonUKAddress when editing a non-UK address" in {
        val answers =
          emptyAnswers
            .set(CorrespondenceChangeAddrScreenerPage, EditCurrentAddress)
            .success
            .value

        navigator.nextPage(CorrespondenceChangeAddrScreenerPage, NormalMode, answers) mustBe
          routes.CorrespondenceNonUKAddressController.onPageLoad()
      }

      "should route CorrespondenceChangeAddrScreenerPage to SystemError when unanswered" in {
        navigator.nextPage(CorrespondenceChangeAddrScreenerPage, NormalMode, emptyAnswers) mustBe
          routes.SystemErrorController.onPageLoad()
      }

      "should route BusinessChangeAddrScreenerPage to BusinessUkAddress when changing to a different UK address" in {
        val answers =
          emptyAnswers
            .set(BusinessChangeAddrScreenerPage, BusinessChangeAddrOption.DifferentUkAddress)
            .success
            .value

        navigator.nextPage(BusinessChangeAddrScreenerPage, NormalMode, answers) mustBe
          routes.BusinessUKAddressController.onPageLoad()
      }

      "should route BusinessChangeAddrScreenerPage to BusinessNonUkAddress when changing to a non-UK address" in {
        val answers =
          emptyAnswers
            .set(BusinessChangeAddrScreenerPage, BusinessChangeAddrOption.ChangeToNonUkAddress)
            .success
            .value

        navigator.nextPage(BusinessChangeAddrScreenerPage, NormalMode, answers) mustBe
          routes.BusinessNonUKAddressController.onPageLoad()
      }

      "should route BusinessChangeAddrScreenerPage to BusinessUkAddress when changing to a UK address" in {
        val answers =
          emptyAnswers
            .set(BusinessChangeAddrScreenerPage, BusinessChangeAddrOption.ChangeToUkAddress)
            .success
            .value

        navigator.nextPage(BusinessChangeAddrScreenerPage, NormalMode, answers) mustBe
          routes.BusinessUKAddressController.onPageLoad()
      }

      "should route BusinessChangeAddrScreenerPage to BusinessAddressUK when editing the current UK address" in {
        val answers =
          emptyAnswers
            .set(BusinessChangeAddrScreenerPage, BusinessChangeAddrOption.EditCurrentAddress)
            .success
            .value
            .set(BusinessAddressUkPage, Address("1", Some("2"), Some("3"), Some("4"), Some("postcode"), Some("country")))
            .success
            .value

        navigator.nextPage(BusinessChangeAddrScreenerPage, NormalMode, answers) mustBe
          routes.BusinessUKAddressController.onPageLoad()
      }

      "should route BusinessChangeAddrScreenerPage to BusinessAddressNonUK when editing the current nonUK address" in {
        val answers =
          emptyAnswers
            .set(BusinessChangeAddrScreenerPage, BusinessChangeAddrOption.EditCurrentAddress)
            .success
            .value
            .set(BusinessAddressNonUkPage, Address("1", Some("2"), Some("3"), Some("4"), Some("postcode"), Some("country")))
            .success
            .value

        navigator.nextPage(BusinessChangeAddrScreenerPage, NormalMode, answers) mustBe
          routes.BusinessNonUKAddressController.onPageLoad()
      }

      "should route BusinessChangeAddrScreenerPage to SystemError when unanswered" in {
        navigator.nextPage(BusinessChangeAddrScreenerPage, NormalMode, emptyAnswers) mustBe
          routes.SystemErrorController.onPageLoad()
      }

      "should route AddCorrespondenceAddressAdditionalInformationPage to CorrespondenceAdditionalInfo when answer is true" in {
        val answers =
          emptyAnswers
            .set(AddCorrespondenceAddressAdditionalInformationPage, true)
            .success
            .value

        navigator.nextPage(AddCorrespondenceAddressAdditionalInformationPage, NormalMode, answers) mustBe
          routes.CorrespondenceAdditionalInfoController.onPageLoad()
      }

      "should route AddCorrespondenceAddressAdditionalInformationPage to CorrespondenceContactNumber when answer is false and adding correspondence details" in {
        val answers =
          emptyAnswers
            .set(AddCorrespondenceAddressAdditionalInformationPage, false)
            .success
            .value
            .set(AddCorrespondingDetailsYesNoPage, true)
            .success
            .value

        navigator.nextPage(AddCorrespondenceAddressAdditionalInformationPage, NormalMode, answers) mustBe
          routes.CorrespondenceContactNumberController.onPageLoad()
      }

      "should route AddCorrespondenceAddressAdditionalInformationPage to CheckCorrespondenceDetails when answer is false outside the add correspondence journey" in {
        val answers =
          emptyAnswers
            .set(AddCorrespondenceAddressAdditionalInformationPage, false)
            .success
            .value
            .set(AddCorrespondingDetailsYesNoPage, false)
            .success
            .value

        navigator.nextPage(AddCorrespondenceAddressAdditionalInformationPage, NormalMode, answers) mustBe
          routes.CheckCorrespondenceDetailsController.onPageLoad()
      }

      "should route AddCorrespondenceAddressAdditionalInformationPage to SystemError when unanswered" in {
        navigator.nextPage(AddCorrespondenceAddressAdditionalInformationPage, NormalMode, emptyAnswers) mustBe
          routes.SystemErrorController.onPageLoad()
      }

      "should route CorrespondenceAdditionalInformationPage to CheckCorrespondenceDetails outside the add correspondence journey" in {
        navigator.nextPage(CorrespondenceAdditionalInformationPage, NormalMode, emptyAnswers) mustBe
          routes.CheckCorrespondenceDetailsController.onPageLoad()
      }

      "should route CorrespondenceAdditionalInformationPage to CorrespondenceContactNumber when adding correspondence details" in {
        val answers =
          emptyAnswers
            .set(AddCorrespondingDetailsYesNoPage, true)
            .success
            .value

        navigator.nextPage(CorrespondenceAdditionalInformationPage, NormalMode, answers) mustBe
          routes.CorrespondenceContactNumberController.onPageLoad()
      }

      "should route CorrespondenceContactNumberPage to CheckCorrespondenceDetails outside the add correspondence journey" in {
        navigator.nextPage(CorrespondenceContactNumberPage, NormalMode, emptyAnswers) mustBe
          routes.CheckCorrespondenceDetailsController.onPageLoad()
      }

      "should route CorrespondenceContactNumberPage to FaxNumberForCorrespondenceYesNo when adding correspondence details" in {
        val answers =
          emptyAnswers
            .set(AddCorrespondingDetailsYesNoPage, true)
            .success
            .value

        navigator.nextPage(CorrespondenceContactNumberPage, NormalMode, answers) mustBe
          routes.FaxNumberForCorrespondenceYesNoController.onPageLoad()
      }

      "should route AddCorrespondenceFaxNumberPage to CorrespondenceFaxNumber when answer is true" in {
        val answers =
          emptyAnswers
            .set(AddCorrespondenceFaxNumberPage, true)
            .success
            .value

        navigator.nextPage(AddCorrespondenceFaxNumberPage, NormalMode, answers) mustBe
          routes.CorrespondenceFaxNumberController.onPageLoad()
      }

      "should route AddCorrespondenceFaxNumberPage to CheckCorrespondenceDetails when answer is false" in {
        val answers =
          emptyAnswers
            .set(AddCorrespondenceFaxNumberPage, false)
            .success
            .value

        navigator.nextPage(AddCorrespondenceFaxNumberPage, NormalMode, answers) mustBe
          routes.CheckCorrespondenceDetailsController.onPageLoad()
      }

      "should route AddCorrespondenceFaxNumberPage to SystemError when unanswered" in {
        navigator.nextPage(AddCorrespondenceFaxNumberPage, NormalMode, emptyAnswers) mustBe
          routes.SystemErrorController.onPageLoad()
      }

      "should route CorrespondenceFaxNumberPage to CheckCorrespondenceDetails outside the add correspondence journey" in {
        navigator.nextPage(CorrespondenceFaxNumberPage, NormalMode, emptyAnswers) mustBe
          routes.CheckCorrespondenceDetailsController.onPageLoad()
      }

      "should route CorrespondenceFaxNumberPage to AddEmailAddressForCorrespondenceYesNo when adding correspondence details" in {
        val answers =
          emptyAnswers
            .set(AddCorrespondingDetailsYesNoPage, true)
            .success
            .value

        navigator.nextPage(CorrespondenceFaxNumberPage, NormalMode, answers) mustBe
          routes.AddEmailAddressForCorrespondenceYesNoController.onPageLoad()
      }

      "should route RemoveCorrespondenceFaxNumberPage to CheckCorrespondenceDetails" in {
        navigator.nextPage(RemoveCorrespondenceFaxNumberPage, NormalMode, emptyAnswers) mustBe
          routes.CheckCorrespondenceDetailsController.onPageLoad()
      }

      "should route AddEmailAddressForCorrespondenceYesNoPage to CorrespondenceEmailAddress when answer is true" in {
        val answers =
          emptyAnswers
            .set(AddEmailAddressForCorrespondenceYesNoPage, true)
            .success
            .value

        navigator.nextPage(AddEmailAddressForCorrespondenceYesNoPage, NormalMode, answers) mustBe
          routes.CorrespondenceEmailAddressController.onPageLoad()
      }

      "should route AddEmailAddressForCorrespondenceYesNoPage to CheckCorrespondenceDetails when answer is false and adding correspondence details" in {
        val answers =
          emptyAnswers
            .set(AddEmailAddressForCorrespondenceYesNoPage, false)
            .success
            .value
            .set(AddCorrespondingDetailsYesNoPage, true)
            .success
            .value

        navigator.nextPage(AddEmailAddressForCorrespondenceYesNoPage, NormalMode, answers) mustBe
          routes.CheckCorrespondenceDetailsController.onPageLoad()
      }

      "should route AddEmailAddressForCorrespondenceYesNoPage to CheckCorrespondenceDetails when answer is false outside the add correspondence journey" in {
        val answers =
          emptyAnswers
            .set(AddEmailAddressForCorrespondenceYesNoPage, false)
            .success
            .value
            .set(AddCorrespondingDetailsYesNoPage, false)
            .success
            .value

        navigator.nextPage(AddEmailAddressForCorrespondenceYesNoPage, NormalMode, answers) mustBe
          routes.CheckCorrespondenceDetailsController.onPageLoad()
      }

      "should route AddEmailAddressForCorrespondenceYesNoPage to SystemError when unanswered" in {
        navigator.nextPage(AddEmailAddressForCorrespondenceYesNoPage, NormalMode, emptyAnswers) mustBe
          routes.SystemErrorController.onPageLoad()
      }

      "should route CorrespondenceEmailPage to CheckCorrespondenceDetails" in {
        navigator.nextPage(CorrespondenceEmailPage, NormalMode, emptyAnswers) mustBe
          routes.CheckCorrespondenceDetailsController.onPageLoad()
      }

      "should route RemoveCorrespondenceEmailAddressPage to CheckCorrespondenceDetails" in {
        navigator.nextPage(RemoveCorrespondenceEmailAddressPage, NormalMode, emptyAnswers) mustBe
          routes.CheckCorrespondenceDetailsController.onPageLoad()
      }

      "should route RemoveCorrespondenceDetailsYesNoPage to ChangeRegistrationDetails when answer is true" in {
        val answers =
          emptyAnswers
            .set(RemoveCorrespondenceDetailsYesNoPage, true)
            .success
            .value

        navigator.nextPage(RemoveCorrespondenceDetailsYesNoPage, NormalMode, answers) mustBe
          routes.ChangeRegistrationDetailsController.onPageLoad()
      }

      "should route RemoveCorrespondenceDetailsYesNoPage to CheckCorrespondenceDetails when answer is false" in {
        val answers =
          emptyAnswers
            .set(RemoveCorrespondenceDetailsYesNoPage, false)
            .success
            .value

        navigator.nextPage(RemoveCorrespondenceDetailsYesNoPage, NormalMode, answers) mustBe
          routes.CheckCorrespondenceDetailsController.onPageLoad()
      }

      "should route CorrespondenceUKAddrScreenerPage to CheckCorrespondenceDetails when the answer remains UK" in {
        val answers =
          emptyAnswers
            .set(
              CorrespondenceAddressUkPage,
              Address(
                address1 = "line1",
                address2 = Some("line2"),
                address3 = None,
                address4 = None,
                postcode = Some("AA1 1AA"),
                country  = Some("GB")
              )
            )
            .success
            .value
            .set(CorrespondenceUKAddrScreenerPage, true)
            .success
            .value

        navigator.nextPage(CorrespondenceUKAddrScreenerPage, NormalMode, answers) mustBe
          routes.CheckCorrespondenceDetailsController.onPageLoad()
      }

      "should route CorrespondenceUKAddrScreenerPage to CheckCorrespondenceDetails when the answer remains non-UK" in {
        val answers =
          emptyAnswers
            .set(
              CorrespondenceAddressNonUkPage,
              Address(
                address1 = "line1",
                address2 = Some("line2"),
                address3 = None,
                address4 = None,
                postcode = None,
                country  = Some("France")
              )
            )
            .success
            .value
            .set(CorrespondenceUKAddrScreenerPage, false)
            .success
            .value

        navigator.nextPage(CorrespondenceUKAddrScreenerPage, NormalMode, answers) mustBe
          routes.CheckCorrespondenceDetailsController.onPageLoad()
      }

      "should route PartnerAddFaxNumberYesNoPage to PartnerAddFaxNumberYesNoController when answered" in {
        val answers =
          emptyAnswers
            .set(PartnerDetailsAddFaxNumberYesNoPage(newPartnersIndex1), true)
            .success
            .value

        navigator.nextPage(PartnerDetailsAddFaxNumberYesNoPage(newPartnersIndex1), NormalMode, answers) mustBe
          controllers.partnerdetails.routes.PartnerDetailsAddFaxNumberYesNoController.onPageLoad(newPartnersIndex1.toString)
      }

      "should route PartnerAddFaxNumberYesNoPage to SystemError when unanswered" in {
        navigator.nextPage(PartnerDetailsAddFaxNumberYesNoPage(newPartnersIndex1), NormalMode, emptyAnswers) mustBe
          routes.SystemErrorController.onPageLoad()
      }

      "should route PartnerAddEmailAddressYesNoPage to PartnerAddEmailAddressYesNoPageController when answered" in {
        val answers =
          emptyAnswers
            .set(PartnerDetailsAddEmailAddressYesNoPage(newPartnersIndex1), true)
            .success
            .value

        navigator.nextPage(PartnerDetailsAddEmailAddressYesNoPage(newPartnersIndex1), NormalMode, answers) mustBe
          controllers.partnerdetails.routes.PartnerDetailsAddEmailAddressYesNoPageController.onPageLoad(newPartnersIndex1.toString)
      }

      "should route PartnerAddEmailAddressYesNoPage to SystemError when unanswered" in {
        navigator.nextPage(PartnerDetailsAddEmailAddressYesNoPage(newPartnersIndex1), NormalMode, emptyAnswers) mustBe
          routes.SystemErrorController.onPageLoad()
      }

      "should route PartnerDetailsRemoveEmailAddressYesNoPage to PartnerDetailsRemoveEmailAddressYesNoController when answered" in {
        val answers =
          emptyAnswers
            .set(PartnerDetailsRemoveEmailAddressYesNoPage(newPartnersIndex1), true)
            .success
            .value

        navigator.nextPage(PartnerDetailsRemoveEmailAddressYesNoPage(newPartnersIndex1), NormalMode, answers) mustBe
          controllers.partnerdetails.routes.PartnerDetailsRemoveEmailAddressYesNoController.onPageLoad(newPartnersIndex1.toString, NormalMode)
      }

      "should route PartnerDetailsRemoveEmailAddressYesNoPage to SystemError when unanswered" in {
        navigator.nextPage(PartnerDetailsRemoveEmailAddressYesNoPage(newPartnersIndex1), NormalMode, emptyAnswers) mustBe
          routes.SystemErrorController.onPageLoad()
      }

      "should route PartnerDetailsRemoveFaxNumberYesNoPage to PartnerDetailsRemoveFaxNumberYesNoController when answered" in {
        val answers =
          emptyAnswers
            .set(PartnerDetailsRemoveFaxNumberYesNoPage(newPartnersIndex1), true)
            .success
            .value

        navigator.nextPage(PartnerDetailsRemoveFaxNumberYesNoPage(newPartnersIndex1), NormalMode, answers) mustBe
          controllers.partnerdetails.routes.PartnerDetailsRemoveFaxNumberYesNoController.onPageLoad(newPartnersIndex1.toString, NormalMode)
      }

      "should route PartnerDetailsRemoveFaxNumberYesNoPage to SystemError when unanswered" in {
        navigator.nextPage(PartnerDetailsRemoveFaxNumberYesNoPage(newPartnersIndex1), NormalMode, emptyAnswers) mustBe
          routes.SystemErrorController.onPageLoad()
      }

      "should route PartnerDetailsRemoveNationalInsuranceNumberYesNoPage to PartnerDetailsRemoveNationalInsuranceNumberYesNoController when answered" in {
        val answers =
          emptyAnswers
            .set(PartnerDetailsRemoveNationalInsuranceNumberYesNoPage(newPartnersIndex1), true)
            .success
            .value

        navigator.nextPage(PartnerDetailsNinoPage(newPartnersIndex1), NormalMode, answers) mustBe
          controllers.partnerdetails.routes.PartnerDetailsRemoveNationalInsuranceNumberYesNoController.onPageLoad(newPartnersIndex1.toString,
                                                                                                                  NormalMode
                                                                                                                 )
      }

      "should route PartnerDetailsNinoPage to SystemError when unanswered" in {
        navigator.nextPage(PartnerDetailsNinoPage(newPartnersIndex1), NormalMode, emptyAnswers) mustBe
          routes.SystemErrorController.onPageLoad()
      }

      "should route PartnerDetailsAdditionalAddressInfoYesNoPage to PartnerDetailsAdditionalAddressInfoYesNoController when answered" in {
        val answers =
          emptyAnswers
            .set(PartnerDetailsAdditionalAddressInfoYesNoPage(newPartnersIndex1), true)
            .success
            .value

        navigator.nextPage(PartnerDetailsAdditionalAddressInfoYesNoPage(newPartnersIndex1), NormalMode, answers) mustBe
          controllers.partnerdetails.routes.PartnerDetailsAdditionalAddressInfoYesNoController.onPageLoad(newPartnersIndex1.toString)
      }

      "should route PartnerDetailsAdditionalAddressInfoYesNoPage to SystemError when unanswered" in {
        navigator.nextPage(PartnerDetailsAdditionalAddressInfoYesNoPage(newPartnersIndex1), NormalMode, emptyAnswers) mustBe
          routes.SystemErrorController.onPageLoad()
      }

      "should route RemoveCorrespondenceDetailsYesNoPage to SystemError when unanswered" in {
        navigator.nextPage(RemoveCorrespondenceDetailsYesNoPage, NormalMode, emptyAnswers) mustBe
          routes.SystemErrorController.onPageLoad()
      }

      "should route PartnerDetailsAdditionalAddressInfoPage to PartnerDetailsAdditionalAddressInfoController" in {
        navigator.nextPage(PartnerDetailsAdditionalAddressInfoPage(newPartnersIndex1), NormalMode, emptyAnswers) mustBe
          controllers.partnerdetails.routes.PartnerDetailsAdditionalAddressInfoController.onPageLoad(newPartnersIndex1.toString, NormalMode)
      }

      "should route PartnerDetailsContactNumberPage to PartnerContactDetailsController" in {
        navigator.nextPage(PartnerDetailsContactNumberPage(newPartnersIndex1), NormalMode, emptyAnswers) mustBe
          controllers.partnerdetails.routes.PartnerDetailsContactDetailsController.onPageLoad(newPartnersIndex1.toString, NormalMode)
      }

      "should route RemoveAdditionalInfoForPartnerAddressYesNoPage to RemoveAdditionalInfoForPartnerAddressYesNoController when answer is true" in {
        val answers =
          emptyAnswers
            .set(PartnerDetailsRemoveAdditionalInfoForPartnerAddressYesNoPage(newPartnersIndex1), true)
            .success
            .value

        navigator.nextPage(PartnerDetailsRemoveAdditionalInfoForPartnerAddressYesNoPage(newPartnersIndex1), NormalMode, answers) mustBe
          controllers.partnerdetails.routes.PartnerDetailsRemoveAdditionalInfoForPartnerAddressYesNoController.onPageLoad(newPartnersIndex1.toString,
                                                                                                                          NormalMode
                                                                                                                         )
      }

      "should route RemoveAdditionalInfoForPartnerAddressYesNoPage to RemoveAdditionalInfoForPartnerAddressYesNoController when answer is false" in {
        val answers =
          emptyAnswers
            .set(PartnerDetailsRemoveAdditionalInfoForPartnerAddressYesNoPage(newPartnersIndex1), false)
            .success
            .value

        navigator.nextPage(PartnerDetailsRemoveAdditionalInfoForPartnerAddressYesNoPage(newPartnersIndex1), NormalMode, answers) mustBe
          controllers.partnerdetails.routes.PartnerDetailsRemoveAdditionalInfoForPartnerAddressYesNoController.onPageLoad(newPartnersIndex1.toString,
                                                                                                                          NormalMode
                                                                                                                         )
      }

      "should route RemoveAdditionalInfoForPartnerAddressYesNoPage to SystemError when unanswered" in {
        navigator.nextPage(PartnerDetailsRemoveAdditionalInfoForPartnerAddressYesNoPage(newPartnersIndex1), NormalMode, emptyAnswers) mustBe
          routes.SystemErrorController.onPageLoad()
      }

      "should route PartnerDetailsEmailAddressPage to PartnerDetailsEmailAddressController" in {
        navigator.nextPage(PartnerDetailsCorrespondenceEmailAddressPage(newPartnersIndex1), NormalMode, emptyAnswers) mustBe
          controllers.partnerdetails.routes.PartnerDetailsEmailAddressController.onPageLoad(newPartnersIndex1.toString, NormalMode)
      }

      "should route RemovePartnerTradingNameYesNoPage to RemovePartnerTradingNameYesNoController when answer is false" in {
        val answers =
          emptyAnswers
            .set(PartnerDetailsRemovePartnerTradingNameYesNoPage(newPartnersIndex1), false)
            .success
            .value

        navigator.nextPage(PartnerDetailsRemovePartnerTradingNameYesNoPage(newPartnersIndex1), NormalMode, answers) mustBe
          controllers.partnerdetails.routes.PartnerDetailsRemovePartnerTradingNameYesNoController.onPageLoad(newPartnersIndex1.toString, NormalMode)
      }

      "should route RemovePartnerTradingNameYesNoPage to IndexController when answer is true" in {
        val answers =
          emptyAnswers
            .set(PartnerDetailsRemovePartnerTradingNameYesNoPage(newPartnersIndex1), true)
            .success
            .value

        navigator.nextPage(PartnerDetailsRemovePartnerTradingNameYesNoPage(newPartnersIndex1), NormalMode, answers) mustBe
          routes.IndexController.onPageLoad()
      }

      "should route RemovePartnerTradingNameYesNoPage to SystemError when unanswered" in {
        navigator.nextPage(PartnerDetailsRemovePartnerTradingNameYesNoPage(newPartnersIndex1), NormalMode, emptyAnswers) mustBe
          routes.SystemErrorController.onPageLoad()
      }

      "should route VatRegistrationNumberYesNoPage to VatRegistrationNumberYesNoPage when answer is false" in {
        val answers =
          emptyAnswers
            .set(PartnerDetailsVatRegistrationNumberYesNoPage(newPartnersIndex1), false)
            .success
            .value

        navigator.nextPage(PartnerDetailsVatRegistrationNumberYesNoPage(newPartnersIndex1), NormalMode, answers) mustBe
          controllers.partnerdetails.routes.PartnerDetailsVatRegistrationNumberYesNoController.onPageLoad(newPartnersIndex1.toString)
      }

      "should route VatRegistrationNumberYesNoPage to VatRegistrationNumberYesNoPage when answer is true" in {
        val answers =
          emptyAnswers
            .set(PartnerDetailsVatRegistrationNumberYesNoPage(newPartnersIndex1), true)
            .success
            .value

        navigator.nextPage(PartnerDetailsVatRegistrationNumberYesNoPage(newPartnersIndex1), NormalMode, answers) mustBe
          controllers.partnerdetails.routes.PartnerDetailsVatRegistrationNumberYesNoController.onPageLoad(newPartnersIndex1.toString)
      }

      "should route VatRegistrationNumberYesNoPage to SystemError when unanswered" in {
        navigator.nextPage(PartnerDetailsVatRegistrationNumberYesNoPage(newPartnersIndex1), NormalMode, emptyAnswers) mustBe
          routes.SystemErrorController.onPageLoad()
      }

      "should route PartnerDetailsBusinessTypePage to the correct Partner Details controller when answered" in {
        val businessData = Json.obj(
          "newPartners" -> Json.arr(
            Json.obj("partnerDetailsBusinessType" -> 2)
          )
        )

        navigator.nextPage(PartnerDetailsBusinessTypePage(newPartnersIndex1), NormalMode, emptyAnswers.copy(data = businessData)) mustBe
          controllers.partnerdetails.routes.PartnerDetailsChangeBusinessNameController.onPageLoad(newPartnersIndex1.toString,
                                                                                                  Corporatebody,
                                                                                                  NormalMode
                                                                                                 )
      }

      "should route PartnerDetailsBusinessTypePage to SystemError when unanswered" in {
        navigator.nextPage(PartnerDetailsBusinessTypePage(newPartnersIndex1), NormalMode, emptyAnswers) mustBe
          routes.SystemErrorController.onPageLoad()
      }

      "should route PartnerDetailsRemoveVatRegNumberYesNoPage to PartnerDetailsRemoveVatRegNumberYesNoPage when answer is false" in {
        val answers =
          emptyAnswers
            .set(PartnerDetailsRemoveVatRegNumberYesNoPage(newPartnersIndex1), false)
            .success
            .value

        navigator.nextPage(PartnerDetailsRemoveVatRegNumberYesNoPage(newPartnersIndex1), NormalMode, answers) mustBe
          controllers.partnerdetails.routes.PartnerDetailsRemoveVatRegNumberYesNoController.onPageLoad(newPartnersIndex1.toString, NormalMode)
      }

      "should route PartnerDetailsRemoveVatRegNumberYesNoPage to PartnerDetailsRemoveVatRegNumberYesNoPage when answer is true" in {
        val answers =
          emptyAnswers
            .set(PartnerDetailsRemoveVatRegNumberYesNoPage(newPartnersIndex1), true)
            .success
            .value

        navigator.nextPage(PartnerDetailsRemoveVatRegNumberYesNoPage(newPartnersIndex1), NormalMode, answers) mustBe
          controllers.partnerdetails.routes.PartnerDetailsRemoveVatRegNumberYesNoController.onPageLoad(newPartnersIndex1.toString, NormalMode)
      }

      "should route PartnerDetailsVrnPage to PartnerDetailsVatRegistrationNumberController regardless of the stored answer" in {
        val answers =
          emptyAnswers
            .set(PartnerDetailsVrnPage(newPartnersIndex1), "GB123456789")
            .success
            .value

        navigator.nextPage(PartnerDetailsVrnPage(newPartnersIndex1), NormalMode, answers) mustBe
          controllers.partnerdetails.routes.PartnerDetailsVatRegistrationNumberController.onPageLoad(newPartnersIndex1.toString, NormalMode)
      }

      "should route PartnerDetailsUtrPage to SystemErrorController when answer is false" in {
        navigator.nextPage(PartnerDetailsUtrPage(newPartnersIndex1), NormalMode, emptyAnswers) mustBe
          routes.SystemErrorController.onPageLoad()
      }

      "should route PartnerDetailsUtrPage to VatRegistrationNumberYesNoController when answer is true" in {
        val answers =
          emptyAnswers
            .set(PartnerDetailsUtrPage(newPartnersIndex1), "123")
            .success
            .value

        navigator.nextPage(PartnerDetailsUtrPage(newPartnersIndex1), NormalMode, answers) mustBe
          controllers.partnerdetails.routes.PartnerDetailsVatRegistrationNumberYesNoController.onPageLoad(newPartnersIndex1.toString)
      }

      "should route PartnerDetailsCrnPage to PT-UTR for the same new partner" in {
        val answers =
          emptyAnswers
            .set(PartnerDetailsCrnPage(newPartnersIndex1), "SC123456")
            .success
            .value

        navigator.nextPage(PartnerDetailsCrnPage(newPartnersIndex1), NormalMode, answers) mustBe
          controllers.partnerdetails.routes.PartnerDetailsAddUTRController.onPageLoad(newPartnersIndex1.toString, NormalMode)
      }

      "should route PartnerDetailsCrnPage to PT-UTR for the partner at the given index" in {
        navigator.nextPage(PartnerDetailsCrnPage(newPartnersIndex2), NormalMode, emptyAnswers) mustBe
          controllers.partnerdetails.routes.PartnerDetailsAddUTRController.onPageLoad(newPartnersIndex2.toString, NormalMode)
      }

      "should route PartnerDetailsCrnPage in CheckMode to ChangeRegistrationDetails until PT-CYA exists" in {
        navigator.nextPage(PartnerDetailsCrnPage(businessNumber1), CheckMode, emptyAnswers) mustBe
          routes.ChangeRegistrationDetailsController.onPageLoad()
      }

      "should route PartnerDetailsForeignCorporateReferencePage to PartnerDetailsForeignCorporateReferenceController regardless of the stored answer" in {
        val answers =
          emptyAnswers
            .set(PartnerDetailsForeignCorporateReferencePage(newPartnersIndex1), "ABC-123 456")
            .success
            .value

        navigator.nextPage(PartnerDetailsForeignCorporateReferencePage(newPartnersIndex1), NormalMode, answers) mustBe
          controllers.partnerdetails.routes.PartnerDetailsForeignCorporateReferenceController.onPageLoad(newPartnersIndex1.toString, NormalMode)
      }

      "should route RemovePartnerYesNoPage to RemovePartnerYesNoPage when answer is false" in {
        val answers =
          emptyAnswers
            .set(PartnerDetailsRemovePartnerYesNoPage(newPartnersIndex1), false)
            .success
            .value

        navigator.nextPage(PartnerDetailsRemovePartnerYesNoPage(newPartnersIndex1), NormalMode, answers) mustBe
          controllers.partnerdetails.routes.PartnerDetailsRemovePartnerYesNoController.onPageLoad(newPartnersIndex1.toString, NormalMode)
      }

      "should route RemovePartnerYesNoPage to PartnerDetailsController when answer is true and it's an new partner" in {
        val answers =
          emptyAnswers
            .set(PartnerDetailsRemovePartnerYesNoPage(newPartnersIndex1), true)
            .success
            .value

        navigator.nextPage(PartnerDetailsRemovePartnerYesNoPage(newPartnersIndex1), NormalMode, answers) mustBe
          controllers.partnerdetails.routes.PartnerDetailsController.onPageLoad(None)
      }

      "should route RemovePartnerYesNoPage to PartnerDetailsController when answer is true and it's an existing partner" in {
        val answers =
          emptyAnswers
            .set(PartnerDetailsRemovePartnerYesNoPage(businessNumber1), true)
            .success
            .value

        navigator.nextPage(PartnerDetailsRemovePartnerYesNoPage(businessNumber1), NormalMode, answers) mustBe
          controllers.partnerdetails.routes.PartnerDetailsController.onPageLoad(None)
      }

      "should route RemovePartnerYesNoPage to SystemError when unanswered" in {
        navigator.nextPage(PartnerDetailsRemovePartnerYesNoPage(newPartnersIndex1), NormalMode, emptyAnswers) mustBe
          routes.SystemErrorController.onPageLoad()
      }

    }

    "normal mode controlling body details navigation" - {

      "should route ControllingBodyChangeScreenerPage to Index when the answer is EditDetails" in {
        val answers =
          emptyAnswers
            .set(ControllingBodyChangeScreenerPage, EditDetails)
            .success
            .value

        navigator.nextPage(ControllingBodyChangeScreenerPage, NormalMode, answers) mustBe
          routes.IndexController.onPageLoad()
      }

      "should route ControllingBodyChangeScreenerPage to ControllingBodyBusinessType when the answer is ProvideNew" in {
        val answers =
          emptyAnswers
            .set(ControllingBodyChangeScreenerPage, ProvideNew)
            .success
            .value

        navigator.nextPage(ControllingBodyChangeScreenerPage, NormalMode, answers) mustBe
          controllers.controllingbody.routes.ControllingBodyBusinessTypeController.onPageLoad()
      }

      "should route ControllingBodyChangeScreenerPage to ChangeRegistrationDetails when the answer is KeepSame" in {
        val answers =
          emptyAnswers
            .set(ControllingBodyChangeScreenerPage, KeepSame)
            .success
            .value

        navigator.nextPage(ControllingBodyChangeScreenerPage, NormalMode, answers) mustBe
          routes.ChangeRegistrationDetailsController.onPageLoad()
      }

      "should route ControllingBodyChangeScreenerPage to SystemError when there is no answer" in {
        navigator.nextPage(ControllingBodyChangeScreenerPage, NormalMode, emptyAnswers) mustBe
          routes.SystemErrorController.onPageLoad()
      }

      "should route ControllingBodyAddTradingNameYesNoPage to Index when the answer is Yes" in {
        val answers =
          emptyAnswers
            .set(ControllingBodyAddTradingNameYesNoPage, true)
            .success
            .value

        navigator.nextPage(ControllingBodyAddTradingNameYesNoPage, NormalMode, answers) mustBe
          routes.IndexController.onPageLoad()
      }

      "should route ControllingBodyAddTradingNameYesNoPage to Index when the answer is No" in {
        val answers =
          emptyAnswers
            .set(ControllingBodyAddTradingNameYesNoPage, false)
            .success
            .value

        navigator.nextPage(ControllingBodyAddTradingNameYesNoPage, NormalMode, answers) mustBe
          routes.IndexController.onPageLoad()
      }
    }
  }
}
