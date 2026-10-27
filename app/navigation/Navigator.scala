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

import controllers.routes
import models.*
import models.BusinessType.*
import models.CorrespondenceChangeAddrOption.*
import models.controllingbody.ControllingBodyChangeOption.*
import models.licencespremises.LicencesPremisesAnswers.*
import navigation.PartnerDetailsNavigator.{partnerDetailsCheckRoutes, partnerDetailsNormalRoutes}
import pages.*
import pages.businessaddress.*
import pages.businessname.*
import pages.contactdetails.*
import pages.controllingbody.*
import pages.correspondencedetails.*
import pages.licencespremises.*
import pages.returnperiods.*
import pages.tradingdetails.*
import pages.tradingdetails.associatedregnumbers.*
import pages.tradingdetails.previousregnumbers.*
import play.api.mvc.Call
import utils.ReturnPeriodsUtils

import javax.inject.{Inject, Singleton}

@Singleton
class Navigator @Inject() () {

  private val normalRoutes: PartialFunction[Page, UserAnswers => Call] = {
    case ControllingBodyBusinessNamePage =>
      _ => controllers.controllingbody.routes.ControllingBodyAddTradingNameYesNoController.onPageLoad()
    case ControllingBodyAddTradingNameYesNoPage =>
      // TODO: Connect CB-TN and the business-type-specific identity screens when that journey is built.
      _ => routes.IndexController.onPageLoad()
    case ControllingBodySoleProprietorPage =>
      _ => routes.IndexController.onPageLoad() // TODO: Wire to CB-DOB or CB-CYA when the controlling body journey is built.
    case ControllingBodyEmailPage =>
      _ => controllers.controllingbody.routes.CheckControllingBodyDetailsController.onPageLoad()
    case ControllingBodyDateOfBirthPage =>
      _ => controllers.controllingbody.routes.ControllingBodyAddTradingNameYesNoController.onPageLoad()
    case ControllingBodyDateOfIncorporationPage =>
      _ => routes.IndexController.onPageLoad() // TODO: Connect the next controlling body screen when the journey is built.
    case ControllingBodyIsUkIncorporatedPage =>
      userAnswers => navigateControllingBodyIsUkIncorporatedPage(userAnswers)
    case ControllingBodyCrnPage =>
      _ => routes.IndexController.onPageLoad() // TODO: Redirect to CB-UTR when the controlling body journey is built.
    case RemoveTradeNamePage =>
      _ => controllers.businessname.routes.CheckBusinessNameController.onPageLoad()
    case BusinessNamePage =>
      _ => controllers.businessname.routes.CheckBusinessNameController.onPageLoad()
    case SoleProprietorPage =>
      _ => controllers.businessname.routes.ChangeBusinessNameController.onPageLoad(Soleproprietor)
    case TradingNamePage =>
      _ => controllers.businessname.routes.CheckBusinessNameController.onPageLoad()
    case BusinessFaxNumberPage =>
      _ => controllers.businesscontactdetails.routes.CheckContactDetailsController.onPageLoad()
    case RemoveFaxNumberPage =>
      _ => controllers.businesscontactdetails.routes.CheckContactDetailsController.onPageLoad()
    case RemoveEmailAddressPage =>
      _ => controllers.businesscontactdetails.routes.CheckContactDetailsController.onPageLoad()
    case BusinessContactNumberPage =>
      _ => controllers.businesscontactdetails.routes.CheckContactDetailsController.onPageLoad()
    case BusinessEmailAddressPage =>
      _ => controllers.businesscontactdetails.routes.CheckContactDetailsController.onPageLoad()
    case BusinessTradeClassPage =>
      _ => controllers.tradingdetails.routes.CheckTradingDetailsController.onPageLoad()
    case IsSeasonalBusinessPage =>
      _ => controllers.tradingdetails.routes.CheckTradingDetailsController.onPageLoad()
    case OtherTradeClassPage =>
      _ => controllers.tradingdetails.routes.CheckTradingDetailsController.onPageLoad()
    case AddPreviousRegistrationNumberPage =>
      userAnswers => addPreviousRegistrationNumberRoute()(userAnswers)
    case PreviousRegNumberPage =>
      _ => controllers.tradingdetails.routes.PreviousRegistrationNumberController.onPageLoad()
    case PreviousRegistrationNumbersListPage =>
      _ => controllers.tradingdetails.routes.PreviousRegistrationNumbersListController.onPageLoad()
    case RemovePreviousRegNumberPage =>
      _ => controllers.tradingdetails.routes.PreviousRegistrationNumbersListController.onPageLoad()
    case AddAssociatedRegistrationNumberPage =>
      userAnswers => navigateAddAssociatedRegistrationNumberPage()(userAnswers)
    case AssociatedRegNumberPage =>
      _ => controllers.tradingdetails.routes.AssociatedRegistrationNumbersListController.onPageLoad()
    case AssociatedRegistrationNumbersPage =>
      _ => controllers.tradingdetails.routes.AssociatedRegistrationNumbersListController.onPageLoad()
    case RemoveAssociatedRegNumberPage =>
      userAnswers => navigateRemoveAssociatedRegNumberPage()(userAnswers)
    case AddCorrespondingDetailsYesNoPage =>
      userAnswers => navigateAddCorrespondingDetailsYesNoPage()(userAnswers)
    case CorrespondenceChangeAddrScreenerPage =>
      userAnswers => navigateCorrespondenceChangeAddrScreenerPage()(userAnswers)
    case CorrespondenceAdditionalNameYesNoPage =>
      userAnswers => navigateCorrespondenceAdditionalNameYesNoPage()(userAnswers)
    case CorrespondenceContactNumberPage =>
      userAnswers => navigateCorrespondenceContactNumberPage()(userAnswers)
    case AddCorrespondenceFaxNumberPage =>
      userAnswers => navigateAddCorrespondenceFaxNumberPage()(userAnswers)
    case CorrespondenceFaxNumberPage =>
      userAnswers => navigateCorrespondenceFaxNumberPage()(userAnswers)
    case AddBusinessAddressAdditionalInformationPage =>
      userAnswers => navigateAddBusinessAddressScreenerPage()(userAnswers)
    case AddEmailAddressForCorrespondenceYesNoPage =>
      userAnswers => navigateAddEmailAddressForCorrespondenceYesNoPage()(userAnswers)
    case RemoveCorrespondenceDetailsYesNoPage =>
      userAnswers => navigateRemoveCorrespondenceDetailsYesNoPage(userAnswers)
    case AddCorrespondenceAddressAdditionalInformationPage =>
      userAnswers => navigateAddCorrespondenceAddressAdditionalInformationPage()(userAnswers)
    case CorrespondenceUKAddrScreenerPage =>
      userAnswers => navigateCorrespondenceUKAddrScreenerPage()(userAnswers)
    case CorrespondenceEmailPage =>
      _ => controllers.correspondencedetails.routes.CheckCorrespondenceDetailsController.onPageLoad()
    case RemoveCorrespondenceFaxNumberPage =>
      _ => controllers.correspondencedetails.routes.CheckCorrespondenceDetailsController.onPageLoad()
    case RemoveCorrespondenceEmailAddressPage =>
      _ => controllers.correspondencedetails.routes.CheckCorrespondenceDetailsController.onPageLoad()
    case CorrespondenceNamePage =>
      userAnswers => navigateCorrespondenceNamePage()(userAnswers)
    case CorrespondenceAdditionalNamePage =>
      userAnswers => navigateCorrespondenceAdditionalNamePage()(userAnswers)
    case CorrespondenceAdditionalInformationPage =>
      userAnswers => navigateCorrespondenceAdditionalInformationPage()(userAnswers)
    case RemoveCorrAddressAddInfoPage =>
      _ => controllers.correspondencedetails.routes.CheckCorrespondenceDetailsController.onPageLoad()
    case CorrespondenceAddressUkPage =>
      userAnswers => navigateCorrespondenceAddressUkPage()(userAnswers)
    case CorrespondenceAddressNonUkPage =>
      userAnswers => navigateCorrespondenceAddressNonUkPage()(userAnswers)

    case BusinessChangeAddrScreenerPage =>
      userAnswers => navigateBusinessChangeAddrScreenerPage()(userAnswers)
    case BusinessUKAddrScreenerPage =>
      userAnswers => navigateBusinessUKAddrScreenerPage()(userAnswers)
    case BusinessAddressUkPage =>
      userAnswers => navigateBusinessAddressUkOrNonUkPage()(userAnswers)
    case BusinessAddressNonUkPage =>
      userAnswers => navigateBusinessAddressUkOrNonUkPage()(userAnswers)
    case RemoveBusinessAddressAddInfoPage =>
      _ => controllers.businessaddress.routes.CheckBusinessAddressController.onPageLoad()
    case BusinessAddressAdditionalInformationPage =>
      _ => controllers.businessaddress.routes.CheckBusinessAddressController.onPageLoad()

    // Return Periods
    //    case NonStandardReturnPeriodsPage =>
    //      userAnswers => controllers.returnperiods.routes.ReturnPeriodsController.onPageLoad(NormalMode) // change it

    case ReturnPeriodsChoicePage =>
      userAnswers => controllers.returnperiods.routes.ChooseReturnPeriodsController.onPageLoad(NormalMode)

    // License and Premises Details
    case LicenceNumberPage =>
      userAnswers => navigateLicenceChange(userAnswers)
    case RemoveLicenceNumberPage =>
      _ => controllers.licencespremises.routes.CheckLicencesAndPremisesController.onPageLoad()
    case LicenceHeldByLandlordPage =>
      userAnswers => navigateLicenceChange(userAnswers)
    case OtherLicencesAndPermitsGBPage =>
      userAnswers => navigateLicenceChange(userAnswers)
    case OtherLicencesAndPermitsNIPage =>
      userAnswers => navigateLicenceChange(userAnswers)
    case LicencePremisesNotCoveredPage =>
      _ => controllers.licencespremises.routes.CheckLicencesAndPremisesController.onPageLoad()
    case LicencesPremisesPage =>
      _ => controllers.licencespremises.routes.CheckLicencesAndPremisesController.onPageLoad()
    case RemovePremisesDetailsYesNoPage =>
      _ => controllers.licencespremises.routes.CheckLicencesAndPremisesController.onPageLoad()
    case RemovePremisesAddressPage =>
      _ => controllers.licencespremises.routes.CheckLicencesAndPremisesController.onPageLoad() // Change it

    // Controlling Body Details
    case ControllingBodyChangeScreenerPage =>
      userAnswers => navigateControllingBodyChangeScreenerPage(userAnswers)
    case StandardReturnPeriodsPage =>
      userAnswers => navigateReturnPeriodsPage(userAnswers)
    case RemoveControllingBodyTradeNamePage =>
      _ => routes.IndexController.onPageLoad() // Change it to CB-CYA
    case RemoveControllingBodyFaxNumberPage =>
      _ => routes.IndexController.onPageLoad() // Change it to CB-CYA
    case ControllingBodyAdditionalAddressInformationYesNoPage =>
      userAnswers => navigateControllingBodyAdditionalAddressInformationYesNoPage()(userAnswers)
    case RemoveControllingBodyEmailPage =>
      _ => routes.IndexController.onPageLoad() // Change it to CYA
    case ControllingBodyAddEmailAddressYesNoPage =>
      userAnswers => navigateControllingBodyAddEmailAddressYesNoPage(userAnswers)
    case ControllingBodyAddFaxNumberYesNoPage =>
      userAnswers => navigateControllingBodyAddFaxNumberYesNoPage(userAnswers)
    case ControllingBodyAddNINumberYesNoPage =>
      userAnswers => navigateControllingBodyAddNINumberYesNoPage(userAnswers)
    case ControllingBodyBusinessTypePage =>
      userAnswers => navigateControllingBodyBusinessTypePage(userAnswers)

    // Return Periods
    case ChooseReturnPeriodsPage =>
      userAnswers => navigateChooseReturnPeriodsPage(userAnswers)
    case NonStandardPeriodDate1Page =>
      _ => controllers.returnperiods.routes.EnterNonStandardPeriodDateController.onPageLoad(2, NormalMode)
    case NonStandardPeriodDate2Page =>
      _ => controllers.returnperiods.routes.EnterNonStandardPeriodDateController.onPageLoad(3, NormalMode)
    case NonStandardPeriodDate3Page =>
      _ => controllers.returnperiods.routes.EnterNonStandardPeriodDateController.onPageLoad(4, NormalMode)
    case NonStandardPeriodDate4Page =>
      _ => controllers.returnperiods.routes.EnterNonStandardPeriodDateController.onPageLoad(5, NormalMode)
    case NonStandardPeriodDate5Page =>
      _ => controllers.returnperiods.routes.EnterNonStandardPeriodDateController.onPageLoad(6, NormalMode)
    case NonStandardPeriodDate6Page =>
      _ => controllers.returnperiods.routes.EnterNonStandardPeriodDateController.onPageLoad(7, NormalMode)
    case NonStandardPeriodDate7Page =>
      _ => controllers.returnperiods.routes.EnterNonStandardPeriodDateController.onPageLoad(8, NormalMode)

    case NonStandardPeriodDate8Page =>
      _ => controllers.returnperiods.routes.CheckReturnPeriodsController.onPageLoad()

    case _ =>
      _ => routes.IndexController.onPageLoad()
  }

  private val checkRouteMap: PartialFunction[Page, UserAnswers => Call] = {

    case ReturnPeriodsChoicePage =>
      userAnswers => controllers.returnperiods.routes.CheckReturnPeriodsController.onPageLoad()
    case ChooseReturnPeriodsPage =>
      _ => controllers.returnperiods.routes.CheckReturnPeriodsController.onPageLoad()
    case NonStandardPeriodDate1Page | NonStandardPeriodDate2Page | NonStandardPeriodDate3Page | NonStandardPeriodDate4Page |
        NonStandardPeriodDate5Page | NonStandardPeriodDate6Page | NonStandardPeriodDate7Page | NonStandardPeriodDate8Page =>
      _ => controllers.returnperiods.routes.CheckReturnPeriodsController.onPageLoad()

    case _ =>
      _ => routes.ChangeRegistrationDetailsController.onPageLoad()
  }

  private val combinedNormalRoutes: Page => UserAnswers => Call =
    partnerDetailsNormalRoutes orElse normalRoutes

  private val combinedCheckRouteMap: Page => UserAnswers => Call =
    partnerDetailsCheckRoutes orElse checkRouteMap

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call =
    mode match {
      case NormalMode =>
        combinedNormalRoutes(page)(userAnswers)
      case CheckMode =>
        combinedCheckRouteMap(page)(userAnswers)
    }

  private def navigateChooseReturnPeriodsPage(userAnswers: UserAnswers): Call =
    if (ReturnPeriodsUtils.isStandard(userAnswers)) {
      controllers.returnperiods.routes.CheckReturnPeriodsController.onPageLoad()
    } else {
      controllers.returnperiods.routes.EnterNonStandardPeriodDateController.onPageLoad(1, NormalMode)
    }

  private def navigateCorrespondenceNamePage()(answers: UserAnswers): Call =
    answers.get(AddCorrespondingDetailsYesNoPage) match {
      case Some(true) => controllers.correspondencedetails.routes.CorrespondenceAdditionalNameYesNoController.onPageLoad()
      case _          => controllers.correspondencedetails.routes.CheckCorrespondenceDetailsController.onPageLoad()
    }

  private def navigateCorrespondenceAdditionalNamePage()(answers: UserAnswers): Call =
    answers.get(AddCorrespondingDetailsYesNoPage) match {
      case Some(true) => controllers.correspondencedetails.routes.CorrespondenceUKAddrScreenerController.onPageLoad()
      case _          => controllers.correspondencedetails.routes.CheckCorrespondenceDetailsController.onPageLoad()
    }

  private def navigateCorrespondenceAddressUkPage()(answers: UserAnswers): Call =
    answers.get(AddCorrespondingDetailsYesNoPage) match {
      case Some(true) => controllers.correspondencedetails.routes.CorrespondenceAddrInfoScreenerController.onPageLoad()
      case _          => controllers.correspondencedetails.routes.CheckCorrespondenceDetailsController.onPageLoad()
    }

  private def navigateCorrespondenceAddressNonUkPage()(answers: UserAnswers): Call =
    answers.get(AddCorrespondingDetailsYesNoPage) match {
      case Some(true) => controllers.correspondencedetails.routes.CorrespondenceAddrInfoScreenerController.onPageLoad()
      case _          => controllers.correspondencedetails.routes.CheckCorrespondenceDetailsController.onPageLoad()
    }

  private def navigateCorrespondenceAdditionalInformationPage()(answers: UserAnswers): Call =
    answers.get(AddCorrespondingDetailsYesNoPage) match {
      case Some(true) => controllers.correspondencedetails.routes.CorrespondenceContactNumberController.onPageLoad()
      case _          => controllers.correspondencedetails.routes.CheckCorrespondenceDetailsController.onPageLoad()
    }

  private def navigateCorrespondenceContactNumberPage()(answers: UserAnswers): Call =
    answers.get(AddCorrespondingDetailsYesNoPage) match {
      case Some(true) => controllers.correspondencedetails.routes.FaxNumberForCorrespondenceYesNoController.onPageLoad()
      case _          => controllers.correspondencedetails.routes.CheckCorrespondenceDetailsController.onPageLoad()
    }

  private def navigateCorrespondenceFaxNumberPage()(answers: UserAnswers): Call =
    answers.get(AddCorrespondingDetailsYesNoPage) match {
      case Some(true) => controllers.correspondencedetails.routes.AddEmailAddressForCorrespondenceYesNoController.onPageLoad()
      case _          => controllers.correspondencedetails.routes.CheckCorrespondenceDetailsController.onPageLoad()
    }

  private def navigateAddBusinessAddressScreenerPage()(answers: UserAnswers): Call =
    answers.get(AddBusinessAddressAdditionalInformationPage) match {
      case Some(true) => controllers.businessaddress.routes.BusinessAddressAdditionalInfoController.onPageLoad()
      case _          => controllers.businessaddress.routes.CheckBusinessAddressController.onPageLoad()
    }

  private def navigateAddAssociatedRegistrationNumberPage()(answers: UserAnswers): Call =
    answers
      .get(AddAssociatedRegistrationNumberPage)
      .map {
        case false => controllers.tradingdetails.routes.CheckTradingDetailsController.onPageLoad()
        case true  => controllers.tradingdetails.routes.AssociatedRegNumberController.onPageLoad()
      }
      .getOrElse(routes.SystemErrorController.onPageLoad())

  private def addPreviousRegistrationNumberRoute()(userAnswers: UserAnswers): Call =
    userAnswers
      .get(AddPreviousRegistrationNumberPage)
      .map {
        case false => controllers.tradingdetails.routes.CheckTradingDetailsController.onPageLoad()
        case true  => controllers.tradingdetails.routes.PreviousRegistrationNumberController.onPageLoad()
      }
      .getOrElse(routes.SystemErrorController.onPageLoad())

  private def navigateCorrespondenceAdditionalNameYesNoPage()(userAnswers: UserAnswers): Call =
    userAnswers.get(CorrespondenceAdditionalNameYesNoPage) match {
      case Some(true) =>
        controllers.correspondencedetails.routes.CorrespondenceAdditionalNameController.onPageLoad()
      case Some(false) =>
        if (userAnswers.get(AddCorrespondingDetailsYesNoPage).contains(true)) {
          controllers.correspondencedetails.routes.CorrespondenceUKAddrScreenerController.onPageLoad()
        } else {
          controllers.correspondencedetails.routes.CheckCorrespondenceDetailsController.onPageLoad()
        }
      case None =>
        routes.SystemErrorController.onPageLoad()
    }

  private def navigateAddCorrespondenceAddressAdditionalInformationPage()(answers: UserAnswers): Call =
    answers.get(AddCorrespondenceAddressAdditionalInformationPage) match {
      case Some(true) =>
        controllers.correspondencedetails.routes.CorrespondenceAdditionalInfoController.onPageLoad()
      case Some(false) =>
        if (answers.get(AddCorrespondingDetailsYesNoPage).contains(true)) {
          controllers.correspondencedetails.routes.CorrespondenceContactNumberController.onPageLoad()
        } else {
          controllers.correspondencedetails.routes.CheckCorrespondenceDetailsController.onPageLoad()
        }
      case None =>
        routes.SystemErrorController.onPageLoad()
    }

  private def navigateCorrespondenceUKAddrScreenerPage()(answers: UserAnswers): Call = {
    val previouslyUk = answers.get(CorrespondenceAddressUkPage).isDefined
    val previouslyNonUk = answers.get(CorrespondenceAddressNonUkPage).isDefined

    answers.get(CorrespondenceUKAddrScreenerPage) match {
      case Some(true) if previouslyUk     => controllers.correspondencedetails.routes.CheckCorrespondenceDetailsController.onPageLoad()
      case Some(false) if previouslyNonUk => controllers.correspondencedetails.routes.CheckCorrespondenceDetailsController.onPageLoad()
      case Some(true)                     => routes.AddressLookupController.initialise()
      case Some(false)                    => controllers.correspondencedetails.routes.CorrespondenceNonUKAddressController.onPageLoad()
      case None                           => routes.SystemErrorController.onPageLoad()
    }
  }

  private def navigateBusinessUKAddrScreenerPage()(answers: UserAnswers): Call = {
    val previouslyUk = answers.get(BusinessAddressUkPage).isDefined
    val previouslyNonUk = answers.get(BusinessAddressNonUkPage).isDefined

    answers.get(BusinessUKAddrScreenerPage) match {
      case Some(true) if previouslyUk     => controllers.businessaddress.routes.CheckBusinessAddressController.onPageLoad()
      case Some(false) if previouslyNonUk => controllers.businessaddress.routes.CheckBusinessAddressController.onPageLoad()
      case Some(true)                     => controllers.businessaddress.routes.BusinessUKAddressController.onPageLoad()
      case Some(false)                    => controllers.businessaddress.routes.BusinessNonUKAddressController.onPageLoad()
      case None                           => routes.SystemErrorController.onPageLoad()
    }
  }

  // Once a licence or permit brings the premises not covered question into scope, it is asked until it has been answered
  private def navigateLicenceChange(userAnswers: UserAnswers): Call =
    if (userAnswers.hasLicencesOrPermits && userAnswers.backendFlagOption(LicencePremisesNotCoveredPage).isEmpty) {
      controllers.licencespremises.routes.PremisesNotCoveredYesNoController.onPageLoad()
    } else {
      controllers.licencespremises.routes.CheckLicencesAndPremisesController.onPageLoad()
    }

  private def navigateAddCorrespondingDetailsYesNoPage()(userAnswers: UserAnswers): Call =
    userAnswers
      .get(AddCorrespondingDetailsYesNoPage)
      .map {
        case true  => controllers.correspondencedetails.routes.CorrespondenceNameController.onPageLoad()
        case false => routes.ChangeRegistrationDetailsController.onPageLoad()
      }
      .getOrElse(routes.SystemErrorController.onPageLoad())

  private def navigateCorrespondenceChangeAddrScreenerPage()(userAnswers: UserAnswers): Call = {
    val isUkAddress = userAnswers.get(CorrespondenceAddressUkPage).isDefined

    userAnswers
      .get(CorrespondenceChangeAddrScreenerPage)
      .map {
        case DifferentUkAddress                => routes.AddressLookupController.initialise()
        case ChangeToNonUkAddress              => controllers.correspondencedetails.routes.CorrespondenceNonUKAddressController.onPageLoad()
        case ChangeToUkAddress                 => routes.AddressLookupController.initialise()
        case EditCurrentAddress if isUkAddress => controllers.correspondencedetails.routes.CorrespondenceUKAddressController.onPageLoad()
        case EditCurrentAddress                => controllers.correspondencedetails.routes.CorrespondenceNonUKAddressController.onPageLoad()
      }
      .getOrElse(routes.SystemErrorController.onPageLoad())
  }

  private def navigateBusinessChangeAddrScreenerPage()(userAnswers: UserAnswers): Call = {
    val ukRoute = controllers.businessaddress.routes.BusinessUKAddressController.onPageLoad()
    val nonUkRoute = controllers.businessaddress.routes.BusinessNonUKAddressController.onPageLoad()
    userAnswers
      .get(BusinessChangeAddrScreenerPage)
      .map {
        case BusinessChangeAddrOption.DifferentUkAddress   => ukRoute
        case BusinessChangeAddrOption.ChangeToNonUkAddress => nonUkRoute
        case BusinessChangeAddrOption.ChangeToUkAddress    => ukRoute
        case BusinessChangeAddrOption.EditCurrentAddress =>
          if (userAnswers.get(BusinessAddressUkPage).isDefined) ukRoute else nonUkRoute
      }
      .getOrElse(routes.SystemErrorController.onPageLoad())
  }

  private def navigateAddCorrespondenceFaxNumberPage()(userAnswers: UserAnswers): Call =
    userAnswers.get(AddCorrespondenceFaxNumberPage) match {
      case Some(true) =>
        controllers.correspondencedetails.routes.CorrespondenceFaxNumberController.onPageLoad()
      case Some(false) =>
        if (userAnswers.get(AddCorrespondingDetailsYesNoPage).contains(true))
          controllers.correspondencedetails.routes.AddEmailAddressForCorrespondenceYesNoController.onPageLoad()
        else
          controllers.correspondencedetails.routes.CheckCorrespondenceDetailsController.onPageLoad()
      case None =>
        routes.SystemErrorController.onPageLoad()
    }

  private def navigateAddEmailAddressForCorrespondenceYesNoPage()(userAnswers: UserAnswers): Call =
    userAnswers.get(AddEmailAddressForCorrespondenceYesNoPage) match {
      case Some(true) =>
        controllers.correspondencedetails.routes.CorrespondenceEmailAddressController.onPageLoad()

      case Some(false) =>
        if (userAnswers.get(AddCorrespondingDetailsYesNoPage).contains(true))
          controllers.correspondencedetails.routes.CheckCorrespondenceDetailsController.onPageLoad()
        else
          controllers.correspondencedetails.routes.CheckCorrespondenceDetailsController.onPageLoad()

      case None =>
        routes.SystemErrorController.onPageLoad()
    }

  private def navigateRemoveAssociatedRegNumberPage()(answers: UserAnswers): Call =
    answers
      .get(AssociatedRegistrationNumbersPage)
      .filter(_.nonEmpty)
      .map(_ => controllers.tradingdetails.routes.AssociatedRegistrationNumbersListController.onPageLoad())
      .getOrElse(controllers.tradingdetails.routes.CheckTradingDetailsController.onPageLoad())

  private def navigateRemoveCorrespondenceDetailsYesNoPage(answers: UserAnswers): Call =
    answers
      .get(RemoveCorrespondenceDetailsYesNoPage)
      .map {
        case false => controllers.correspondencedetails.routes.CheckCorrespondenceDetailsController.onPageLoad()
        case true  => routes.ChangeRegistrationDetailsController.onPageLoad()
      }
      .getOrElse(routes.SystemErrorController.onPageLoad())

  private def navigateBusinessAddressUkOrNonUkPage()(userAnswers: UserAnswers): Call = {
    val addFlowRoute = controllers.businessaddress.routes.BusinessAddrInfoScreenerController.onPageLoad()
    val normalRoute = controllers.businessaddress.routes.CheckBusinessAddressController.onPageLoad()
    userAnswers.get(BusinessAddressAddFlowPage) match {
      case Some(isInAddFlow) => if (isInAddFlow) addFlowRoute else normalRoute
      case None              => normalRoute
    }
  }

  private def navigateReturnPeriodsPage(
    userAnswers: UserAnswers
  ): Call = userAnswers.get(StandardReturnPeriodsPage) match {
    case Some(StandardReturnPeriodsOptions.ChangeMonthsStandardPeriodCover) =>
      controllers.returnperiods.routes.ChooseReturnPeriodsController.onPageLoad(NormalMode)
    case Some(StandardReturnPeriodsOptions.SwitchToNonStandard) =>
      controllers.returnperiods.routes.ChooseReturnPeriodsController.onPageLoad(NormalMode)
    case Some(StandardReturnPeriodsOptions.KeepStandardReturnPeriod) =>
      routes.ChangeRegistrationDetailsController.onPageLoad()
    case None =>
      routes.SystemErrorController.onPageLoad()
  }

  private def navigateControllingBodyAddNINumberYesNoPage(userAnswers: UserAnswers): Call =
    userAnswers
      .get(ControllingBodyAddNINumberYesNoPage)
      .map {
        case true  => routes.IndexController.onPageLoad() // TODO later
        case false => routes.IndexController.onPageLoad() // TODO later
      }
      .getOrElse(routes.SystemErrorController.onPageLoad())

  private def navigateControllingBodyBusinessTypePage(userAnswers: UserAnswers): Call = {
    userAnswers
      .get(ControllingBodyBusinessTypePage) match {
      case Some(Soleproprietor) =>
        controllers.controllingbody.routes.ChangeControllingBodyNameController.onPageLoad(BusinessType.Soleproprietor)
      case Some(Corporatebody) =>
        controllers.controllingbody.routes.ChangeControllingBodyNameController.onPageLoad(BusinessType.Corporatebody)
      case Some(Unincorporatedbody) =>
        controllers.controllingbody.routes.ChangeControllingBodyNameController.onPageLoad(BusinessType.Unincorporatedbody)
      case Some(Partnership) =>
        controllers.controllingbody.routes.ChangeControllingBodyNameController.onPageLoad(BusinessType.Partnership)
      case Some(LimitedLiabilityPartnership) =>
        controllers.controllingbody.routes.ChangeControllingBodyNameController.onPageLoad(BusinessType.LimitedLiabilityPartnership)
      case _ => routes.SystemErrorController.onPageLoad()
    }
  }

  private def navigateControllingBodyAddFaxNumberYesNoPage(userAnswers: UserAnswers): Call =
    userAnswers
      .get(ControllingBodyAddFaxNumberYesNoPage)
      .map {
        case true  => routes.IndexController.onPageLoad() // TODO later
        case false => routes.IndexController.onPageLoad() // TODO later
      }
      .getOrElse(routes.SystemErrorController.onPageLoad())

  private def navigateControllingBodyAddEmailAddressYesNoPage(userAnswers: UserAnswers): Call =
    userAnswers
      .get(ControllingBodyAddEmailAddressYesNoPage)
      .map {
        case true  => routes.IndexController.onPageLoad() // TODO later
        case false => routes.IndexController.onPageLoad() // TODO later
      }
      .getOrElse(routes.SystemErrorController.onPageLoad())

  private def navigateControllingBodyChangeScreenerPage(userAnswers: UserAnswers): Call =
    userAnswers
      .get(ControllingBodyChangeScreenerPage)
      .map {
        case EditDetails => controllers.controllingbody.routes.CheckControllingBodyDetailsController.onPageLoad()
        case ProvideNew  => controllers.controllingbody.routes.ControllingBodyBusinessTypeController.onPageLoad()
        case KeepSame    => routes.ChangeRegistrationDetailsController.onPageLoad()
      }
      .getOrElse(routes.SystemErrorController.onPageLoad())

  private def navigateControllingBodyAdditionalAddressInformationYesNoPage()(answers: UserAnswers): Call =
    answers.get(ControllingBodyAdditionalAddressInformationYesNoPage) match {
      case Some(true)  => routes.IndexController.onPageLoad() // TODO later -> Change it to CB-AI
      case Some(false) => routes.IndexController.onPageLoad() // TODO later -> Redirect to CB-CN in add flow and CB-CYA in change flow
      case None        => routes.SystemErrorController.onPageLoad()
    }

  private def navigateControllingBodyIsUkIncorporatedPage(userAnswers: UserAnswers): Call =
    userAnswers
      .get(ControllingBodyIsUkIncorporatedPage)
      .map {
        case "1" => routes.IndexController.onPageLoad() // TODO later -> DateOfIncorporation Screen
        case "0" => routes.IndexController.onPageLoad() // TODO later -> CountryOfIncorporation Screen
        case _   => routes.SystemErrorController.onPageLoad()
      }
      .getOrElse(routes.SystemErrorController.onPageLoad())
}
