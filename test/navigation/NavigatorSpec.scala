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
import models.BusinessType.{Corporatebody, Soleproprietor}
import models.CorrespondenceChangeAddrOption.*
import models.controllingbody.ControllingBodyChangeOption.*
import pages.*
import pages.businessaddress.*
import pages.businessname.*
import pages.contactdetails.*
import pages.controllingbody.{ControllingBodyAddTradingNameYesNoPage, ControllingBodyBusinessTypePage, ControllingBodyChangeScreenerPage, ControllingBodyDateOfIncorporationPage, RemoveControllingBodyFaxNumberPage}
import pages.correspondencedetails.*
import pages.licencespremises.*
import pages.partnerdetails.*
import pages.returnperiods.*
import pages.tradingdetails.*
import pages.tradingdetails.associatedregnumbers.*
import pages.tradingdetails.previousregnumbers.*
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.PartnerUtils
import viewmodels.checkAnswers.partnerdetails.PartnerMandatoryDetails

class NavigatorSpec extends SpecBase with PartnerDetailsHelper {

  private val navigator = new Navigator
  private val emptyAnswers = UserAnswers("id")

  private def firstMissingOrCheckYourAnswers(answers: UserAnswers, index: BusinessNumberOrIndex): Call = PartnerMandatoryDetails
    .firstMissing(answers, index)
    .map(_.call)
    .getOrElse(controllers.partnerdetails.routes.PartnerDetailsCheckYourAnswersController.onPageLoad(index.toString))

  "Navigator" - {

    "in Normal mode" - {

      "should route an unknown page to Index" in {
        case object UnknownPage extends Page

        navigator.nextPage(UnknownPage, NormalMode, emptyAnswers) mustBe
          routes.IndexController.onPageLoad()
      }

      "should route TradingNamePage to CheckBusinessName" in {
        navigator.nextPage(TradingNamePage, NormalMode, emptyAnswers) mustBe
          controllers.businessname.routes.CheckBusinessNameController.onPageLoad()
      }

      "should route BusinessNamePage to CheckBusinessName" in {
        navigator.nextPage(BusinessNamePage, NormalMode, emptyAnswers) mustBe
          controllers.businessname.routes.CheckBusinessNameController.onPageLoad()
      }

      "should route RemoveTradeNamePage to CheckBusinessName" in {
        navigator.nextPage(RemoveTradeNamePage, NormalMode, emptyAnswers) mustBe
          controllers.businessname.routes.CheckBusinessNameController.onPageLoad()
      }

      "should route SoleProprietorPage to ChangeBusinessName" in {
        navigator.nextPage(SoleProprietorPage, NormalMode, emptyAnswers) mustBe
          controllers.businessname.routes.ChangeBusinessNameController.onPageLoad(BusinessType.Soleproprietor)
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
          controllers.tradingdetails.routes.CheckTradingDetailsController.onPageLoad()
      }

      "should route OtherTradeClassPage to CheckTradingDetails" in {
        navigator.nextPage(OtherTradeClassPage, NormalMode, emptyAnswers) mustBe
          controllers.tradingdetails.routes.CheckTradingDetailsController.onPageLoad()
      }

      "should route IsSeasonalBusinessPage to CheckTradingDetails" in {
        navigator.nextPage(IsSeasonalBusinessPage, NormalMode, emptyAnswers) mustBe
          controllers.tradingdetails.routes.CheckTradingDetailsController.onPageLoad()
      }

      "should route AddAssociatedRegistrationNumberPage to AssociatedRegNumber when answer is true" in {
        val answers =
          emptyAnswers
            .set(AddAssociatedRegistrationNumberPage, true)
            .success
            .value

        navigator.nextPage(AddAssociatedRegistrationNumberPage, NormalMode, answers) mustBe
          controllers.tradingdetails.routes.AssociatedRegNumberController.onPageLoad()
      }

      "should route AddAssociatedRegistrationNumberPage to CheckTradingDetails when answer is false" in {
        val answers =
          emptyAnswers
            .set(AddAssociatedRegistrationNumberPage, false)
            .success
            .value

        navigator.nextPage(AddAssociatedRegistrationNumberPage, NormalMode, answers) mustBe
          controllers.tradingdetails.routes.CheckTradingDetailsController.onPageLoad()
      }

      "should route AddAssociatedRegistrationNumberPage to SystemError when unanswered" in {
        navigator.nextPage(AddAssociatedRegistrationNumberPage, NormalMode, emptyAnswers) mustBe
          routes.SystemErrorController.onPageLoad()
      }

      "should route AssociatedRegNumberPage to AssociatedRegistrationNumbersList" in {
        navigator.nextPage(AssociatedRegNumberPage, NormalMode, emptyAnswers) mustBe
          controllers.tradingdetails.routes.AssociatedRegistrationNumbersListController.onPageLoad()
      }

      "should route AssociatedRegistrationNumbersPage to AssociatedRegistrationNumbersList" in {
        navigator.nextPage(AssociatedRegistrationNumbersPage, NormalMode, emptyAnswers) mustBe
          controllers.tradingdetails.routes.AssociatedRegistrationNumbersListController.onPageLoad()
      }

      "should route RemoveAssociatedRegNumberPage to AssociatedRegistrationNumbersList when registration numbers exist" in {
        val answers =
          emptyAnswers
            .set(AssociatedRegistrationNumbersPage, Seq("ABC123"))
            .success
            .value

        navigator.nextPage(RemoveAssociatedRegNumberPage, NormalMode, answers) mustBe
          controllers.tradingdetails.routes.AssociatedRegistrationNumbersListController.onPageLoad()
      }

      "should route RemoveAssociatedRegNumberPage to CheckTradingDetails when registration numbers are empty" in {
        val answers =
          emptyAnswers
            .set(AssociatedRegistrationNumbersPage, Seq.empty[String])
            .success
            .value

        navigator.nextPage(RemoveAssociatedRegNumberPage, NormalMode, answers) mustBe
          controllers.tradingdetails.routes.CheckTradingDetailsController.onPageLoad()
      }

      "should route RemoveAssociatedRegNumberPage to CheckTradingDetails when registration numbers are missing" in {
        navigator.nextPage(RemoveAssociatedRegNumberPage, NormalMode, emptyAnswers) mustBe
          controllers.tradingdetails.routes.CheckTradingDetailsController.onPageLoad()
      }

      "should route PreviousRegNumberPage to PreviousRegistrationNumber" in {
        navigator.nextPage(PreviousRegNumberPage, NormalMode, emptyAnswers) mustBe
          controllers.tradingdetails.routes.PreviousRegistrationNumberController.onPageLoad()
      }

      "should route AddPreviousRegistrationNumberPage to PreviousRegistrationNumber when answer is true" in {
        val answers =
          emptyAnswers
            .set(AddPreviousRegistrationNumberPage, true)
            .success
            .value

        navigator.nextPage(AddPreviousRegistrationNumberPage, NormalMode, answers) mustBe
          controllers.tradingdetails.routes.PreviousRegistrationNumberController.onPageLoad()
      }

      "should route AddPreviousRegistrationNumberPage to CheckTradingDetails when answer is false" in {
        val answers =
          emptyAnswers
            .set(AddPreviousRegistrationNumberPage, false)
            .success
            .value

        navigator.nextPage(AddPreviousRegistrationNumberPage, NormalMode, answers) mustBe
          controllers.tradingdetails.routes.CheckTradingDetailsController.onPageLoad()
      }

      "should route AddPreviousRegistrationNumberPage to SystemError when unanswered" in {
        navigator.nextPage(AddPreviousRegistrationNumberPage, NormalMode, emptyAnswers) mustBe
          routes.SystemErrorController.onPageLoad()
      }

      "should route RemovePreviousRegNumberPage to PreviousRegistrationNumbersList" in {
        navigator.nextPage(RemovePreviousRegNumberPage, NormalMode, emptyAnswers) mustBe
          controllers.tradingdetails.routes.PreviousRegistrationNumbersListController.onPageLoad()
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

      "return period navigation" - {

        "should route ChooseReturnPeriodsPage to the first non-standard period date page" in {
          navigator.nextPage(ChooseReturnPeriodsPage, NormalMode, emptyAnswers) mustBe
            controllers.returnperiods.routes.EnterNonStandardPeriodDateController
              .onPageLoad(1, NormalMode)
        }

        "should route NonStandardPeriodDate1Page to the second non-standard period date page" in {
          navigator.nextPage(NonStandardPeriodDate1Page, NormalMode, emptyAnswers) mustBe
            controllers.returnperiods.routes.EnterNonStandardPeriodDateController
              .onPageLoad(2, NormalMode)
        }

        "should route NonStandardPeriodDate2Page to the third non-standard period date page" in {
          navigator.nextPage(NonStandardPeriodDate2Page, NormalMode, emptyAnswers) mustBe
            controllers.returnperiods.routes.EnterNonStandardPeriodDateController
              .onPageLoad(3, NormalMode)
        }

        "should route NonStandardPeriodDate3Page to the fourth non-standard period date page" in {
          navigator.nextPage(NonStandardPeriodDate3Page, NormalMode, emptyAnswers) mustBe
            controllers.returnperiods.routes.EnterNonStandardPeriodDateController
              .onPageLoad(4, NormalMode)
        }

        "should route NonStandardPeriodDate4Page to the fifth non-standard period date page" in {
          navigator.nextPage(NonStandardPeriodDate4Page, NormalMode, emptyAnswers) mustBe
            controllers.returnperiods.routes.EnterNonStandardPeriodDateController
              .onPageLoad(5, NormalMode)
        }

        "should route NonStandardPeriodDate5Page to the sixth non-standard period date page" in {
          navigator.nextPage(NonStandardPeriodDate5Page, NormalMode, emptyAnswers) mustBe
            controllers.returnperiods.routes.EnterNonStandardPeriodDateController
              .onPageLoad(6, NormalMode)
        }

        "should route NonStandardPeriodDate6Page to the seventh non-standard period date page" in {
          navigator.nextPage(NonStandardPeriodDate6Page, NormalMode, emptyAnswers) mustBe
            controllers.returnperiods.routes.EnterNonStandardPeriodDateController
              .onPageLoad(7, NormalMode)
        }

        "should route NonStandardPeriodDate7Page to the eighth non-standard period date page" in {
          navigator.nextPage(NonStandardPeriodDate7Page, NormalMode, emptyAnswers) mustBe
            controllers.returnperiods.routes.EnterNonStandardPeriodDateController
              .onPageLoad(8, NormalMode)
        }

        "should route NonStandardPeriodDate8Page to CheckReturnPeriods" in {
          navigator.nextPage(NonStandardPeriodDate8Page, NormalMode, emptyAnswers) mustBe
            controllers.returnperiods.routes.CheckReturnPeriodsController
              .onPageLoad()
        }
      }

    }

    "in Check mode" - {

      "should route a non-partner page to ChangeRegistrationDetails" in {
        navigator.nextPage(BusinessNamePage, CheckMode, emptyAnswers) mustBe
          routes.ChangeRegistrationDetailsController.onPageLoad()
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
          controllers.businessaddress.routes.BusinessUKAddressController.onPageLoad()
      }

      "should route BusinessUKAddrScreenerPage to BusinessNonUKAddress when answer is false" in {
        val answers =
          emptyAnswers
            .set(BusinessUKAddrScreenerPage, false)
            .success
            .value

        navigator.nextPage(BusinessUKAddrScreenerPage, NormalMode, answers) mustBe
          controllers.businessaddress.routes.BusinessNonUKAddressController.onPageLoad()
      }

      "should route BusinessAddressAdditionalInformationPage to CheckBusinessAddress when answered" in {
        val answers =
          emptyAnswers
            .set(BusinessAddressAdditionalInformationPage, "INFORMATION")
            .success
            .value

        navigator.nextPage(BusinessAddressAdditionalInformationPage, NormalMode, answers) mustBe
          controllers.businessaddress.routes.CheckBusinessAddressController.onPageLoad()
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
          controllers.businessaddress.routes.BusinessAddrInfoScreenerController.onPageLoad()
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
          controllers.businessaddress.routes.BusinessAddrInfoScreenerController.onPageLoad()
      }

      "should route BusinessAddressUk or NonUk to BusinessAddressPage if in normal flow" in {

        navigator.nextPage(BusinessAddressNonUkPage, NormalMode, emptyAnswers) mustBe
          controllers.businessaddress.routes.CheckBusinessAddressController.onPageLoad()
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
          controllers.businessaddress.routes.BusinessUKAddressController.onPageLoad()
      }

      "should route BusinessChangeAddrScreenerPage to BusinessNonUkAddress when changing to a non-UK address" in {
        val answers =
          emptyAnswers
            .set(BusinessChangeAddrScreenerPage, BusinessChangeAddrOption.ChangeToNonUkAddress)
            .success
            .value

        navigator.nextPage(BusinessChangeAddrScreenerPage, NormalMode, answers) mustBe
          controllers.businessaddress.routes.BusinessNonUKAddressController.onPageLoad()
      }

      "should route BusinessChangeAddrScreenerPage to BusinessUkAddress when changing to a UK address" in {
        val answers =
          emptyAnswers
            .set(BusinessChangeAddrScreenerPage, BusinessChangeAddrOption.ChangeToUkAddress)
            .success
            .value

        navigator.nextPage(BusinessChangeAddrScreenerPage, NormalMode, answers) mustBe
          controllers.businessaddress.routes.BusinessUKAddressController.onPageLoad()
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
          controllers.businessaddress.routes.BusinessUKAddressController.onPageLoad()
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
          controllers.businessaddress.routes.BusinessNonUKAddressController.onPageLoad()
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

      "should route RemoveCorrespondenceDetailsYesNoPage to SystemError when unanswered" in {
        navigator.nextPage(RemoveCorrespondenceDetailsYesNoPage, NormalMode, emptyAnswers) mustBe
          routes.SystemErrorController.onPageLoad()
      }

    }

    "check mode / new partner details navigation" ignore {

      // New, unsaved partners: the add journey. The navigator takes the mode from the index, not the mode argument.

      // --- Business type and incorporation branches ---

      "should route PartnerDetailsBusinessTypePage to PartnerDetailsChangeBusinessNameController when answered" in {
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

      "should route PartnerDetailsIsBusinessIncorporatedUkPage to PartnerDetailsDateOfIncorporationController when the answer is Yes" in {
        val answers =
          emptyAnswers
            .set(PartnerDetailsIsBusinessIncorporatedUkPage(newPartnersIndex1), true)
            .success
            .value

        navigator.nextPage(PartnerDetailsIsBusinessIncorporatedUkPage(newPartnersIndex1), NormalMode, answers) mustBe
          controllers.partnerdetails.routes.PartnerDetailsDateOfIncorporationController.onPageLoad(newPartnersIndex1.toString, NormalMode)
      }

      "should route PartnerDetailsIsBusinessIncorporatedUkPage to PartnerDetailsAddCountryOfIncorporationController when the answer is No" in {
        val answers =
          emptyAnswers
            .set(PartnerDetailsIsBusinessIncorporatedUkPage(newPartnersIndex1), false)
            .success
            .value

        navigator.nextPage(PartnerDetailsIsBusinessIncorporatedUkPage(newPartnersIndex1), NormalMode, answers) mustBe
          controllers.partnerdetails.routes.PartnerDetailsAddCountryOfIncorporationController.onPageLoad(newPartnersIndex1.toString, NormalMode)
      }

      "should route PartnerDetailsIsBusinessIncorporatedUkPage to SystemError when unanswered" in {
        navigator.nextPage(PartnerDetailsIsBusinessIncorporatedUkPage(newPartnersIndex1), NormalMode, emptyAnswers) mustBe
          routes.SystemErrorController.onPageLoad()
      }

      // --- Name, trading name and date of joining ---

      "should route PartnerDetailsBusinessNamePage to PartnerDetailsAddTradingNameYesNoController" in {
        navigator.nextPage(PartnerDetailsBusinessNamePage(newPartnersIndex1), NormalMode, emptyAnswers) mustBe
          controllers.partnerdetails.routes.PartnerDetailsAddTradingNameYesNoController.onPageLoad(newPartnersIndex1.toString)
      }

      "should route PartnerDetailsBusinessNamePage to the next add journey page when called with CheckMode for an unsaved new partner" in {
        navigator.nextPage(PartnerDetailsBusinessNamePage(newPartnersIndex1), CheckMode, emptyAnswers) mustBe
          controllers.partnerdetails.routes.PartnerDetailsAddTradingNameYesNoController.onPageLoad(newPartnersIndex1.toString)
      }

      "should route PartnerDetailsSoleProprietorPage to PartnerDetailsSoleProprietorDobController" in {
        navigator.nextPage(PartnerDetailsSoleProprietorPage(newPartnersIndex1), NormalMode, emptyAnswers) mustBe
          controllers.partnerdetails.routes.PartnerDetailsSoleProprietorDobController.onPageLoad(newPartnersIndex1.toString, NormalMode)
      }

      "should route PartnerDetailsDateOfBirthPage to PartnerDetailsAddTradingNameYesNoController" in {
        navigator.nextPage(PartnerDetailsDateOfBirthPage(newPartnersIndex1), NormalMode, emptyAnswers) mustBe
          controllers.partnerdetails.routes.PartnerDetailsAddTradingNameYesNoController.onPageLoad(newPartnersIndex1.toString)
      }

      "should route PartnerDetailsAddTradingNameYesNoPage to PartnerDetailsTradingNameController when the answer is Yes" in {
        val answers =
          emptyAnswers
            .set(PartnerDetailsAddTradingNameYesNoPage(newPartnersIndex1), true)
            .success
            .value

        navigator.nextPage(PartnerDetailsAddTradingNameYesNoPage(newPartnersIndex1), NormalMode, answers) mustBe
          controllers.partnerdetails.routes.PartnerDetailsTradingNameController.onPageLoad(newPartnersIndex1.toString, NormalMode)
      }

      "should route PartnerDetailsAddTradingNameYesNoPage to PartnerDateOfJoiningController when the answer is No" in {
        val answers =
          emptyAnswers
            .set(PartnerDetailsAddTradingNameYesNoPage(newPartnersIndex1), false)
            .success
            .value

        navigator.nextPage(PartnerDetailsAddTradingNameYesNoPage(newPartnersIndex1), NormalMode, answers) mustBe
          controllers.partnerdetails.routes.PartnerDateOfJoiningController.onPageLoad(newPartnersIndex1.toString, NormalMode)
      }

      "should route PartnerDetailsAddTradingNameYesNoPage to SystemError when unanswered" in {
        navigator.nextPage(PartnerDetailsAddTradingNameYesNoPage(newPartnersIndex1), NormalMode, emptyAnswers) mustBe
          routes.SystemErrorController.onPageLoad()
      }

      "should route PartnerDetailsTradingNamePage to PartnerDateOfJoiningController" in {
        navigator.nextPage(PartnerDetailsTradingNamePage(newPartnersIndex1), NormalMode, emptyAnswers) mustBe
          controllers.partnerdetails.routes.PartnerDateOfJoiningController.onPageLoad(newPartnersIndex1.toString, NormalMode)
      }

      "should route PartnerDetailsDateOfJoiningPage to PartnerDetailsAddNationalInsuranceNumberYesNoController when the business type is Soleproprietor" in {
        val answers = UserAnswers(mgdRegNumber, cleanedDataNewPartnersRouting(businessType = 1))

        navigator.nextPage(PartnerDetailsDateOfJoiningPage(newPartnersIndex1), NormalMode, answers) mustBe
          controllers.partnerdetails.routes.PartnerDetailsAddNationalInsuranceNumberYesNoController.onPageLoad(newPartnersIndex1.toString)
      }

      "should route PartnerDetailsDateOfJoiningPage to PartnerDetailsIsBusinessIncorporatedUkController when the business type is Corporatebody" in {
        val answers = UserAnswers(mgdRegNumber, cleanedDataNewPartnersRouting(businessType = 2))

        navigator.nextPage(PartnerDetailsDateOfJoiningPage(newPartnersIndex1), NormalMode, answers) mustBe
          controllers.partnerdetails.routes.PartnerDetailsIsBusinessIncorporatedUkController.onPageLoad(newPartnersIndex1.toString, NormalMode)
      }

      "should route PartnerDetailsDateOfJoiningPage to PartnerDetailsAddUTRController when the business type is Unincorporatedbody" in {
        val answers = UserAnswers(mgdRegNumber, cleanedDataNewPartnersRouting(businessType = 3))

        navigator.nextPage(PartnerDetailsDateOfJoiningPage(newPartnersIndex1), NormalMode, answers) mustBe
          controllers.partnerdetails.routes.PartnerDetailsAddUTRController.onPageLoad(newPartnersIndex1.toString, NormalMode)
      }

      "should route PartnerDetailsDateOfJoiningPage to PartnerDetailsVatRegistrationNumberYesNoController when the business type is Partnership" in {
        val answers = UserAnswers(mgdRegNumber, cleanedDataNewPartnersRouting(businessType = 4))

        navigator.nextPage(PartnerDetailsDateOfJoiningPage(newPartnersIndex1), NormalMode, answers) mustBe
          controllers.partnerdetails.routes.PartnerDetailsVatRegistrationNumberYesNoController.onPageLoad(newPartnersIndex1.toString)
      }

      "should route PartnerDetailsDateOfJoiningPage to PartnerDetailsDateOfIncorporationController when the business type is LimitedLiabilityPartnership" in {
        val answers = UserAnswers(mgdRegNumber, cleanedDataNewPartnersRouting(businessType = 5))

        navigator.nextPage(PartnerDetailsDateOfJoiningPage(newPartnersIndex1), NormalMode, answers) mustBe
          controllers.partnerdetails.routes.PartnerDetailsDateOfIncorporationController.onPageLoad(newPartnersIndex1.toString, NormalMode)
      }

      "should route PartnerDetailsDateOfJoiningPage to SystemError when the business type is unanswered" in {
        navigator.nextPage(PartnerDetailsDateOfJoiningPage(newPartnersIndex1), NormalMode, emptyAnswers) mustBe
          routes.SystemErrorController.onPageLoad()
      }

      // --- Date of incorporation ---

      "should route PartnerDetailsDateOfIncorporationPage to PartnerDetailsCompanyRegistrationNumberController when the business type is Corporatebody and is incorporated in the UK" in {
        val answers = UserAnswers(mgdRegNumber, cleanedDataNewPartnersRouting(businessType = 2))

        navigator.nextPage(PartnerDetailsDateOfIncorporationPage(newPartnersIndex1), NormalMode, answers) mustBe
          controllers.partnerdetails.routes.PartnerDetailsCompanyRegistrationNumberController.onPageLoad(newPartnersIndex1.toString, NormalMode)
      }

      "should route PartnerDetailsDateOfIncorporationPage to SystemError when the business type is Corporatebody and is not incorporated in the UK" in {
        val answers = UserAnswers(mgdRegNumber, cleanedDataNewPartnersRouting(businessType = 2, isIncorporatedInTheUK = false))

        navigator.nextPage(PartnerDetailsDateOfIncorporationPage(newPartnersIndex1), NormalMode, answers) mustBe
          routes.SystemErrorController.onPageLoad()
      }

      "should route PartnerDetailsDateOfIncorporationPage to PartnerDetailsCompanyRegistrationNumberController when the business type is LimitedLiabilityPartnership" in {
        val answers = UserAnswers(mgdRegNumber, cleanedDataNewPartnersRouting(businessType = 5))

        navigator.nextPage(PartnerDetailsDateOfIncorporationPage(newPartnersIndex1), NormalMode, answers) mustBe
          controllers.partnerdetails.routes.PartnerDetailsCompanyRegistrationNumberController.onPageLoad(newPartnersIndex1.toString, NormalMode)
      }

      "should route PartnerDetailsDateOfIncorporationPage to SystemError when the business type is Soleproprietor" in {
        val answers = UserAnswers(mgdRegNumber, cleanedDataNewPartnersRouting(businessType = 1))

        navigator.nextPage(PartnerDetailsDateOfIncorporationPage(newPartnersIndex1), NormalMode, answers) mustBe
          routes.SystemErrorController.onPageLoad()
      }

      "should route PartnerDetailsDateOfIncorporationPage to SystemError when the business type is Unincorporatedbody" in {
        val answers = UserAnswers(mgdRegNumber, cleanedDataNewPartnersRouting(businessType = 3))

        navigator.nextPage(PartnerDetailsDateOfIncorporationPage(newPartnersIndex1), NormalMode, answers) mustBe
          routes.SystemErrorController.onPageLoad()
      }

      "should route PartnerDetailsDateOfIncorporationPage to SystemError when the business type is Partnership" in {
        val answers = UserAnswers(mgdRegNumber, cleanedDataNewPartnersRouting(businessType = 4))

        navigator.nextPage(PartnerDetailsDateOfIncorporationPage(newPartnersIndex1), NormalMode, answers) mustBe
          routes.SystemErrorController.onPageLoad()
      }

      "should route PartnerDetailsDateOfIncorporationPage to SystemError when the business type is unanswered" in {
        navigator.nextPage(PartnerDetailsDateOfIncorporationPage(newPartnersIndex1), NormalMode, emptyAnswers) mustBe
          routes.SystemErrorController.onPageLoad()
      }

      // --- Reference numbers ---

      "should route PartnerDetailsAddNationalInsuranceNumberYesNoPage to PartnerDetailsAddNationalInsuranceNumberController when the answer is Yes" in {
        val answers =
          emptyAnswers
            .set(PartnerDetailsAddNationalInsuranceNumberYesNoPage(newPartnersIndex1), true)
            .success
            .value

        navigator.nextPage(PartnerDetailsAddNationalInsuranceNumberYesNoPage(newPartnersIndex1), NormalMode, answers) mustBe
          controllers.partnerdetails.routes.PartnerDetailsAddNationalInsuranceNumberController.onPageLoad(newPartnersIndex1.toString, NormalMode)
      }

      "should route PartnerDetailsAddNationalInsuranceNumberYesNoPage to PartnerDetailsAddUTRController when the answer is No" in {
        val answers =
          emptyAnswers
            .set(PartnerDetailsAddNationalInsuranceNumberYesNoPage(newPartnersIndex1), false)
            .success
            .value

        navigator.nextPage(PartnerDetailsAddNationalInsuranceNumberYesNoPage(newPartnersIndex1), NormalMode, answers) mustBe
          controllers.partnerdetails.routes.PartnerDetailsAddUTRController.onPageLoad(newPartnersIndex1.toString, NormalMode)
      }

      "should route PartnerDetailsAddNationalInsuranceNumberYesNoPage to SystemError when unanswered" in {
        navigator.nextPage(PartnerDetailsAddNationalInsuranceNumberYesNoPage(newPartnersIndex1), NormalMode, emptyAnswers) mustBe
          routes.SystemErrorController.onPageLoad()
      }

      "should route PartnerDetailsNinoPage to PartnerDetailsAddUTRController" in {
        val answers =
          emptyAnswers
            .set(PartnerDetailsNinoPage(newPartnersIndex1), "AA123456A")
            .success
            .value

        navigator.nextPage(PartnerDetailsNinoPage(newPartnersIndex1), NormalMode, answers) mustBe
          controllers.partnerdetails.routes.PartnerDetailsAddUTRController.onPageLoad(newPartnersIndex1.toString, NormalMode)
      }

      "should route PartnerDetailsCrnPage to PartnerDetailsAddUTRController for the same new partner" in {
        val answers =
          emptyAnswers
            .set(PartnerDetailsCrnPage(newPartnersIndex1), "SC123456")
            .success
            .value

        navigator.nextPage(PartnerDetailsCrnPage(newPartnersIndex1), NormalMode, answers) mustBe
          controllers.partnerdetails.routes.PartnerDetailsAddUTRController.onPageLoad(newPartnersIndex1.toString, NormalMode)
      }

      "should route PartnerDetailsCrnPage to PartnerDetailsAddUTRController for the partner at the given index" in {
        navigator.nextPage(PartnerDetailsCrnPage(newPartnersIndex2), NormalMode, emptyAnswers) mustBe
          controllers.partnerdetails.routes.PartnerDetailsAddUTRController.onPageLoad(newPartnersIndex2.toString, NormalMode)
      }

      "should route PartnerDetailsCountryOfIncorporationPage to PartnerDetailsForeignCorporateReferenceController" in {
        navigator.nextPage(PartnerDetailsCountryOfIncorporationPage(newPartnersIndex1), NormalMode, emptyAnswers) mustBe
          controllers.partnerdetails.routes.PartnerDetailsForeignCorporateReferenceController.onPageLoad(newPartnersIndex1.toString, NormalMode)
      }

      "should route PartnerDetailsForeignCorporateReferencePage to PartnerDetailsAddUTRController" in {
        val answers =
          emptyAnswers
            .set(PartnerDetailsForeignCorporateReferencePage(newPartnersIndex1), "ABC-123 456")
            .success
            .value

        navigator.nextPage(PartnerDetailsForeignCorporateReferencePage(newPartnersIndex1), NormalMode, answers) mustBe
          controllers.partnerdetails.routes.PartnerDetailsAddUTRController.onPageLoad(newPartnersIndex1.toString, NormalMode)
      }

      "should route PartnerDetailsUtrPage to PartnerDetailsVatRegistrationNumberYesNoController" in {
        val answers =
          emptyAnswers
            .set(PartnerDetailsUtrPage(newPartnersIndex1), "123")
            .success
            .value

        navigator.nextPage(PartnerDetailsUtrPage(newPartnersIndex1), NormalMode, answers) mustBe
          controllers.partnerdetails.routes.PartnerDetailsVatRegistrationNumberYesNoController.onPageLoad(newPartnersIndex1.toString)
      }

      "should route PartnerDetailsVatRegistrationNumberYesNoPage to PartnerDetailsVatRegistrationNumberController when the answer is Yes" in {
        val answers =
          emptyAnswers
            .set(PartnerDetailsVatRegistrationNumberYesNoPage(newPartnersIndex1), true)
            .success
            .value

        navigator.nextPage(PartnerDetailsVatRegistrationNumberYesNoPage(newPartnersIndex1), NormalMode, answers) mustBe
          controllers.partnerdetails.routes.PartnerDetailsVatRegistrationNumberController.onPageLoad(newPartnersIndex1.toString, NormalMode)
      }

      "should route PartnerDetailsVatRegistrationNumberYesNoPage to the first missing mandatory page when the answer is No" in {
        val answers =
          emptyAnswers
            .set(PartnerDetailsVatRegistrationNumberYesNoPage(newPartnersIndex1), false)
            .success
            .value

        navigator.nextPage(PartnerDetailsVatRegistrationNumberYesNoPage(newPartnersIndex1), NormalMode, answers) mustBe
          firstMissingOrCheckYourAnswers(answers, newPartnersIndex1)
      }

      "should route PartnerDetailsVrnPage to the first missing mandatory page" in {
        val answers =
          emptyAnswers
            .set(PartnerDetailsVrnPage(newPartnersIndex1), "GB123456789")
            .success
            .value

        navigator.nextPage(PartnerDetailsVrnPage(newPartnersIndex1), NormalMode, answers) mustBe
          firstMissingOrCheckYourAnswers(answers, newPartnersIndex1)
      }

      // --- Address and contact details ---

      "should route PartnerDetailsAdditionalAddressInfoYesNoPage to PartnerDetailsAdditionalAddressInfoController when the answer is Yes" in {
        val answers =
          emptyAnswers
            .set(PartnerDetailsAdditionalAddressInfoYesNoPage(newPartnersIndex1), true)
            .success
            .value

        navigator.nextPage(PartnerDetailsAdditionalAddressInfoYesNoPage(newPartnersIndex1), NormalMode, answers) mustBe
          controllers.partnerdetails.routes.PartnerDetailsAdditionalAddressInfoController.onPageLoad(newPartnersIndex1.toString, NormalMode)
      }

      "should route PartnerDetailsAdditionalAddressInfoYesNoPage to PartnerDetailsContactDetailsController when the answer is No" in {
        val answers =
          emptyAnswers
            .set(PartnerDetailsAdditionalAddressInfoYesNoPage(newPartnersIndex1), false)
            .success
            .value

        navigator.nextPage(PartnerDetailsAdditionalAddressInfoYesNoPage(newPartnersIndex1), NormalMode, answers) mustBe
          controllers.partnerdetails.routes.PartnerDetailsContactDetailsController.onPageLoad(newPartnersIndex1.toString, NormalMode)
      }

      "should route PartnerDetailsAdditionalAddressInfoYesNoPage to SystemError when unanswered" in {
        navigator.nextPage(PartnerDetailsAdditionalAddressInfoYesNoPage(newPartnersIndex1), NormalMode, emptyAnswers) mustBe
          routes.SystemErrorController.onPageLoad()
      }

      "should route PartnerDetailsAdditionalAddressInfoPage to PartnerDetailsContactDetailsController" in {
        navigator.nextPage(PartnerDetailsAdditionalAddressInfoPage(newPartnersIndex1), NormalMode, emptyAnswers) mustBe
          controllers.partnerdetails.routes.PartnerDetailsContactDetailsController.onPageLoad(newPartnersIndex1.toString, NormalMode)
      }

      "should route PartnerDetailsContactNumberPage to PartnerDetailsAddFaxNumberYesNoController" in {
        navigator.nextPage(PartnerDetailsContactNumberPage(newPartnersIndex1), NormalMode, emptyAnswers) mustBe
          controllers.partnerdetails.routes.PartnerDetailsAddFaxNumberYesNoController.onPageLoad(newPartnersIndex1.toString)
      }

      "should route PartnerDetailsAddFaxNumberYesNoPage to PartnerDetailsChangePartnerFaxNumberController when the answer is Yes" in {
        val answers =
          emptyAnswers
            .set(PartnerDetailsAddFaxNumberYesNoPage(newPartnersIndex1), true)
            .success
            .value

        navigator.nextPage(PartnerDetailsAddFaxNumberYesNoPage(newPartnersIndex1), NormalMode, answers) mustBe
          controllers.partnerdetails.routes.PartnerDetailsChangePartnerFaxNumberController.onPageLoad(newPartnersIndex1.toString, NormalMode)
      }

      "should route PartnerDetailsAddFaxNumberYesNoPage to PartnerDetailsAddEmailAddressYesNoPageController when the answer is No" in {
        val answers =
          emptyAnswers
            .set(PartnerDetailsAddFaxNumberYesNoPage(newPartnersIndex1), false)
            .success
            .value

        navigator.nextPage(PartnerDetailsAddFaxNumberYesNoPage(newPartnersIndex1), NormalMode, answers) mustBe
          controllers.partnerdetails.routes.PartnerDetailsAddEmailAddressYesNoPageController.onPageLoad(newPartnersIndex1.toString)
      }

      "should route PartnerDetailsAddFaxNumberYesNoPage to SystemError when unanswered" in {
        navigator.nextPage(PartnerDetailsAddFaxNumberYesNoPage(newPartnersIndex1), NormalMode, emptyAnswers) mustBe
          routes.SystemErrorController.onPageLoad()
      }

      "should route PartnerDetailsCorrespondenceFaxNumberPage to PartnerDetailsAddEmailAddressYesNoPageController" in {
        navigator.nextPage(PartnerDetailsCorrespondenceFaxNumberPage(newPartnersIndex1), NormalMode, emptyAnswers) mustBe
          controllers.partnerdetails.routes.PartnerDetailsAddEmailAddressYesNoPageController.onPageLoad(newPartnersIndex1.toString)
      }

      "should route PartnerDetailsAddEmailAddressYesNoPage to PartnerDetailsEmailAddressController when the answer is Yes" in {
        val answers =
          emptyAnswers
            .set(PartnerDetailsAddEmailAddressYesNoPage(newPartnersIndex1), true)
            .success
            .value

        navigator.nextPage(PartnerDetailsAddEmailAddressYesNoPage(newPartnersIndex1), NormalMode, answers) mustBe
          controllers.partnerdetails.routes.PartnerDetailsEmailAddressController.onPageLoad(newPartnersIndex1.toString, NormalMode)
      }

      "should route PartnerDetailsAddEmailAddressYesNoPage to PartnerDetailsCheckYourAnswersController when the answer is No" in {
        val answers =
          emptyAnswers
            .set(PartnerDetailsAddEmailAddressYesNoPage(newPartnersIndex1), false)
            .success
            .value

        navigator.nextPage(PartnerDetailsAddEmailAddressYesNoPage(newPartnersIndex1), NormalMode, answers) mustBe
          controllers.partnerdetails.routes.PartnerDetailsCheckYourAnswersController.onPageLoad(newPartnersIndex1.toString)
      }

      "should route PartnerDetailsAddEmailAddressYesNoPage to SystemError when unanswered" in {
        navigator.nextPage(PartnerDetailsAddEmailAddressYesNoPage(newPartnersIndex1), NormalMode, emptyAnswers) mustBe
          routes.SystemErrorController.onPageLoad()
      }

      "should route PartnerDetailsCorrespondenceEmailAddressPage to the first missing mandatory page" in {
        navigator.nextPage(PartnerDetailsCorrespondenceEmailAddressPage(newPartnersIndex1), NormalMode, emptyAnswers) mustBe
          firstMissingOrCheckYourAnswers(emptyAnswers, newPartnersIndex1)
      }

      // --- Remove partner: reached from the partner list ---

      "should route PartnerDetailsRemovePartnerYesNoPage to PartnerDetailsController when the answer is No" in {
        val answers =
          emptyAnswers
            .set(PartnerDetailsRemovePartnerYesNoPage(newPartnersIndex1), false)
            .success
            .value

        navigator.nextPage(PartnerDetailsRemovePartnerYesNoPage(newPartnersIndex1), NormalMode, answers) mustBe
          controllers.partnerdetails.routes.PartnerDetailsController.onPageLoad(None)
      }

      "should route PartnerDetailsRemovePartnerYesNoPage to PartnerDetailsController when the answer is Yes and it's a new partner" in {
        val answers =
          emptyAnswers
            .set(PartnerDetailsRemovePartnerYesNoPage(newPartnersIndex1), true)
            .success
            .value

        navigator.nextPage(PartnerDetailsRemovePartnerYesNoPage(newPartnersIndex1), NormalMode, answers) mustBe
          controllers.partnerdetails.routes.PartnerDetailsController.onPageLoad(None)
      }

      "should route PartnerDetailsRemovePartnerYesNoPage to PartnerDetailsController when the answer is Yes and it's an existing partner" in {
        val answers =
          emptyAnswers
            .set(PartnerDetailsRemovePartnerYesNoPage(businessNumber1), true)
            .success
            .value

        navigator.nextPage(PartnerDetailsRemovePartnerYesNoPage(businessNumber1), NormalMode, answers) mustBe
          controllers.partnerdetails.routes.PartnerDetailsController.onPageLoad(None)
      }

      "should route PartnerDetailsRemovePartnerYesNoPage to SystemError when unanswered" in {
        navigator.nextPage(PartnerDetailsRemovePartnerYesNoPage(newPartnersIndex1), NormalMode, emptyAnswers) mustBe
          routes.SystemErrorController.onPageLoad()
      }
    }

    "normal mode / existing partner details navigation" ignore {

      // Existing partners and saved new partners return to check your answers.

      val existingMode = PartnerUtils.modeFor(businessNumber1)

      val savedNewPartner =
        emptyAnswers
          .set(PartnerDetailsAddPartnerCompletedPage(newPartnersIndex1), true)
          .success
          .value

      def checkYourAnswers(index: BusinessNumberOrIndex): Call =
        controllers.partnerdetails.routes.PartnerDetailsCheckYourAnswersController.onPageLoad(index.toString)

      def valuePages(index: BusinessNumberOrIndex): Seq[Page] = Seq(
        PartnerDetailsBusinessNamePage(index),
        PartnerDetailsSoleProprietorPage(index),
        PartnerDetailsDateOfBirthPage(index),
        PartnerDetailsTradingNamePage(index),
        PartnerDetailsDateOfJoiningPage(index),
        PartnerDetailsDateOfIncorporationPage(index),
        PartnerDetailsNinoPage(index),
        PartnerDetailsCrnPage(index),
        PartnerDetailsUtrPage(index),
        PartnerDetailsVrnPage(index),
        PartnerDetailsCountryOfIncorporationPage(index),
        PartnerDetailsForeignCorporateReferencePage(index),
        PartnerDetailsAdditionalAddressInfoPage(index),
        PartnerDetailsContactNumberPage(index),
        PartnerDetailsCorrespondenceFaxNumberPage(index),
        PartnerDetailsCorrespondenceEmailAddressPage(index)
      )

      def addYesNoPages(index: BusinessNumberOrIndex): Seq[QuestionPage[Boolean]] = Seq(
        PartnerDetailsAddTradingNameYesNoPage(index),
        PartnerDetailsAddNationalInsuranceNumberYesNoPage(index),
        PartnerDetailsVatRegistrationNumberYesNoPage(index),
        PartnerDetailsAdditionalAddressInfoYesNoPage(index),
        PartnerDetailsAddFaxNumberYesNoPage(index),
        PartnerDetailsAddEmailAddressYesNoPage(index)
      )

      def removePages(index: BusinessNumberOrIndex): Seq[Page] = Seq(
        PartnerDetailsRemovePartnerTradingNameYesNoPage(index),
        PartnerDetailsRemoveNationalInsuranceNumberYesNoPage(index),
        PartnerDetailsRemoveVatRegNumberYesNoPage(index),
        PartnerDetailsRemoveEmailAddressYesNoPage(index),
        PartnerDetailsRemoveFaxNumberYesNoPage(index),
        PartnerDetailsRemoveAdditionalInfoForPartnerAddressYesNoPage(index)
      )

      // --- Value pages ---

      valuePages(businessNumber1).foreach { page =>
        s"should route $page to check your answers for an existing partner" in {
          navigator.nextPage(page, CheckMode, emptyAnswers) mustBe checkYourAnswers(businessNumber1)
        }
      }

      valuePages(newPartnersIndex1).foreach { page =>
        s"should route $page to check your answers for a saved new partner" in {
          navigator.nextPage(page, CheckMode, savedNewPartner) mustBe checkYourAnswers(newPartnersIndex1)
        }
      }

      // --- Optional "add X?" questions ---

      val addYesNoTargets: Seq[(QuestionPage[Boolean], Call)] = Seq(
        PartnerDetailsAddTradingNameYesNoPage(businessNumber1) ->
          controllers.partnerdetails.routes.PartnerDetailsTradingNameController.onPageLoad(businessNumber1, existingMode),
        PartnerDetailsAddNationalInsuranceNumberYesNoPage(businessNumber1) ->
          controllers.partnerdetails.routes.PartnerDetailsAddNationalInsuranceNumberController.onPageLoad(businessNumber1, existingMode),
        PartnerDetailsVatRegistrationNumberYesNoPage(businessNumber1) ->
          controllers.partnerdetails.routes.PartnerDetailsVatRegistrationNumberController.onPageLoad(businessNumber1, existingMode),
        PartnerDetailsAdditionalAddressInfoYesNoPage(businessNumber1) ->
          controllers.partnerdetails.routes.PartnerDetailsAdditionalAddressInfoController.onPageLoad(businessNumber1, existingMode),
        PartnerDetailsAddFaxNumberYesNoPage(businessNumber1) ->
          controllers.partnerdetails.routes.PartnerDetailsChangePartnerFaxNumberController.onPageLoad(businessNumber1, existingMode),
        PartnerDetailsAddEmailAddressYesNoPage(businessNumber1) ->
          controllers.partnerdetails.routes.PartnerDetailsEmailAddressController.onPageLoad(businessNumber1, existingMode)
      )

      addYesNoTargets.foreach { case (page, target) =>
        s"should route $page to the question it adds when the answer is Yes for an existing partner" in {
          val answers = emptyAnswers.set(page, true).success.value

          navigator.nextPage(page, CheckMode, answers) mustBe target
        }
      }

      addYesNoPages(businessNumber1).foreach { page =>
        s"should route $page to check your answers when the answer is No for an existing partner" in {
          val answers = emptyAnswers.set(page, false).success.value

          navigator.nextPage(page, CheckMode, answers) mustBe checkYourAnswers(businessNumber1)
        }

        s"should route $page to SystemError when unanswered for an existing partner" in {
          navigator.nextPage(page, CheckMode, emptyAnswers) mustBe routes.SystemErrorController.onPageLoad()
        }
      }

      addYesNoPages(newPartnersIndex1).foreach { page =>
        s"should route $page to check your answers when the answer is No for a saved new partner" in {
          val answers = savedNewPartner.set(page, false).success.value

          navigator.nextPage(page, CheckMode, answers) mustBe checkYourAnswers(newPartnersIndex1)
        }
      }

      // --- Answers that change which pages apply still follow the branch ---

      "should route PartnerDetailsBusinessTypePage to PartnerDetailsChangeBusinessNameController for an existing partner" in {
        val answers =
          emptyAnswers
            .set(PartnerDetailsBusinessTypePage(businessNumber1), Soleproprietor)
            .success
            .value

        navigator.nextPage(PartnerDetailsBusinessTypePage(businessNumber1), CheckMode, answers) mustBe
          controllers.partnerdetails.routes.PartnerDetailsChangeBusinessNameController.onPageLoad(businessNumber1, Corporatebody, existingMode)
      }

      "should route PartnerDetailsBusinessTypePage to SystemError when unanswered for an existing partner" in {
        navigator.nextPage(PartnerDetailsBusinessTypePage(businessNumber1), CheckMode, emptyAnswers) mustBe
          routes.SystemErrorController.onPageLoad()
      }

      "should route PartnerDetailsIsBusinessIncorporatedUkPage to PartnerDetailsDateOfIncorporationController when the answer is Yes for an existing partner" in {
        val answers =
          emptyAnswers
            .set(PartnerDetailsIsBusinessIncorporatedUkPage(businessNumber1), true)
            .success
            .value

        navigator.nextPage(PartnerDetailsIsBusinessIncorporatedUkPage(businessNumber1), CheckMode, answers) mustBe
          controllers.partnerdetails.routes.PartnerDetailsDateOfIncorporationController.onPageLoad(businessNumber1, existingMode)
      }

      "should route PartnerDetailsIsBusinessIncorporatedUkPage to PartnerDetailsAddCountryOfIncorporationController when the answer is No for an existing partner" in {
        val answers =
          emptyAnswers
            .set(PartnerDetailsIsBusinessIncorporatedUkPage(businessNumber1), false)
            .success
            .value

        navigator.nextPage(PartnerDetailsIsBusinessIncorporatedUkPage(businessNumber1), CheckMode, answers) mustBe
          controllers.partnerdetails.routes.PartnerDetailsAddCountryOfIncorporationController.onPageLoad(businessNumber1, existingMode)
      }

      "should route PartnerDetailsIsBusinessIncorporatedUkPage to SystemError when unanswered for an existing partner" in {
        navigator.nextPage(PartnerDetailsIsBusinessIncorporatedUkPage(businessNumber1), CheckMode, emptyAnswers) mustBe
          routes.SystemErrorController.onPageLoad()
      }

      // --- Remove questions: only reachable from check your answers ---

      removePages(businessNumber1).foreach { page =>
        s"should route $page to check your answers for an existing partner" in {
          navigator.nextPage(page, CheckMode, emptyAnswers) mustBe checkYourAnswers(businessNumber1)
        }
      }

      removePages(newPartnersIndex1).foreach { page =>
        s"should route $page to check your answers for a new partner" in {
          navigator.nextPage(page, CheckMode, emptyAnswers) mustBe checkYourAnswers(newPartnersIndex1)
        }
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

      "should route ControllingBodyDateOfIncorporationPage to Index until the onward journey is built" in {
        navigator.nextPage(ControllingBodyDateOfIncorporationPage, NormalMode, emptyAnswers) mustBe
          routes.IndexController.onPageLoad()
      }

      "should route RemoveControllingBodyFaxNumberPage to Index until CB-CYA is built" in {
        navigator.nextPage(RemoveControllingBodyFaxNumberPage, NormalMode, emptyAnswers) mustBe
          routes.IndexController.onPageLoad()
      }

      "should route ChooseReturnPeriodsPage to CheckReturnPeriodsController for standard return periods" in {
        val answers = emptyAnswers.set(HasExistingNstpValuesPage, false).success.value

        navigator.nextPage(ChooseReturnPeriodsPage, NormalMode, answers) mustBe
          controllers.returnperiods.routes.CheckReturnPeriodsController.onPageLoad()
      }

      "should route ChooseReturnPeriodsPage to the first non-standard end date for non-standard return periods" in {
        val answers = emptyAnswers.set(HasExistingNstpValuesPage, true).success.value

        navigator.nextPage(ChooseReturnPeriodsPage, NormalMode, answers) mustBe
          controllers.returnperiods.routes.EnterNonStandardPeriodDateController.onPageLoad(1, NormalMode)
      }

      "should route ChooseReturnPeriodsPage to the first non-standard end date when the return periods type is not known" in {
        navigator.nextPage(ChooseReturnPeriodsPage, NormalMode, emptyAnswers) mustBe
          controllers.returnperiods.routes.EnterNonStandardPeriodDateController.onPageLoad(1, NormalMode)
      }

      "should route ControllingBodyBusinessTypePage to Change Proprietor Name" in {
        val answers =
          UserAnswers("id",
                      Json.obj(
                        "controllingBodyDetails" -> Json.obj(
                          "mgdRegNumber"          -> "XGM00000001761",
                          "typeOfControllingBody" -> 1
                        )
                      )
                     )
        navigator.nextPage(ControllingBodyBusinessTypePage, NormalMode, answers) mustBe
          controllers.controllingbody.routes.ChangeControllingBodyNameController.onPageLoad(BusinessType.Soleproprietor)
      }

      "should route ControllingBodyBusinessTypePage to Change Corporate Body Name" in {
        val answers =
          UserAnswers("id",
                      Json.obj(
                        "controllingBodyDetails" -> Json.obj(
                          "mgdRegNumber"          -> "XGM00000001761",
                          "typeOfControllingBody" -> 2
                        )
                      )
                     )
        navigator.nextPage(ControllingBodyBusinessTypePage, NormalMode, answers) mustBe
          controllers.controllingbody.routes.ChangeControllingBodyNameController.onPageLoad(BusinessType.Corporatebody)
      }

      "should route ControllingBodyBusinessTypePage to Change Unincorporated Body Name" in {
        val answers =
          UserAnswers("id",
                      Json.obj(
                        "controllingBodyDetails" -> Json.obj(
                          "mgdRegNumber"          -> "XGM00000001761",
                          "typeOfControllingBody" -> 3
                        )
                      )
                     )
        navigator.nextPage(ControllingBodyBusinessTypePage, NormalMode, answers) mustBe
          controllers.controllingbody.routes.ChangeControllingBodyNameController.onPageLoad(BusinessType.Unincorporatedbody)
      }

      "should route ControllingBodyBusinessTypePage to Change Partnership Name" in {
        val answers =
          UserAnswers("id",
                      Json.obj(
                        "controllingBodyDetails" -> Json.obj(
                          "mgdRegNumber"          -> "XGM00000001761",
                          "typeOfControllingBody" -> 4
                        )
                      )
                     )
        navigator.nextPage(ControllingBodyBusinessTypePage, NormalMode, answers) mustBe
          controllers.controllingbody.routes.ChangeControllingBodyNameController.onPageLoad(BusinessType.Partnership)
      }

      "should route ControllingBodyBusinessTypePage to Change Limited Liability Partnership" in {
        val answers =
          UserAnswers("id",
                      Json.obj(
                        "controllingBodyDetails" -> Json.obj(
                          "mgdRegNumber"          -> "XGM00000001761",
                          "typeOfControllingBody" -> 5
                        )
                      )
                     )
        navigator.nextPage(ControllingBodyBusinessTypePage, NormalMode, answers) mustBe
          controllers.controllingbody.routes.ChangeControllingBodyNameController.onPageLoad(BusinessType.LimitedLiabilityPartnership)
      }
    }

    "in Check mode" - {

      "should route ChooseReturnPeriodsPage back to CheckReturnPeriodsController for standard return periods" in {
        val answers = emptyAnswers.set(HasExistingNstpValuesPage, false).success.value

        navigator.nextPage(ChooseReturnPeriodsPage, CheckMode, answers) mustBe
          controllers.returnperiods.routes.CheckReturnPeriodsController.onPageLoad()
      }

      "should route ChooseReturnPeriodsPage to CheckReturnPeriods" in {
        navigator.nextPage(ChooseReturnPeriodsPage, CheckMode, emptyAnswers) mustBe
          controllers.returnperiods.routes.CheckReturnPeriodsController
            .onPageLoad()
      }

      Seq(
        NonStandardPeriodDate1Page,
        NonStandardPeriodDate2Page,
        NonStandardPeriodDate3Page,
        NonStandardPeriodDate4Page,
        NonStandardPeriodDate5Page,
        NonStandardPeriodDate6Page,
        NonStandardPeriodDate7Page,
        NonStandardPeriodDate8Page
      ).zipWithIndex.foreach { case (page, index) =>
        s"should route NonStandardPeriodDate${index + 1}Page to CheckReturnPeriods" in {
          navigator.nextPage(page, CheckMode, emptyAnswers) mustBe
            controllers.returnperiods.routes.CheckReturnPeriodsController
              .onPageLoad()
        }
      }

      "should route any other page to ChangeRegistrationDetails" in {
        navigator.nextPage(BusinessNamePage, CheckMode, emptyAnswers) mustBe
          routes.ChangeRegistrationDetailsController.onPageLoad()
      }
    }
  }
}
