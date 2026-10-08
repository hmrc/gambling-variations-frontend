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
import models.licencespremises.LicencesPremisesAnswers.*
import models.controllingbody.ControllingBodyChangeOption.*
import pages.*
import pages.businessaddress.*
import pages.businessname.*
import pages.contactdetails.*
import pages.controllingbody.{ControllingBodyAddTradingNameYesNoPage, ControllingBodyBusinessNamePage, ControllingBodyEmailPage, ControllingBodySoleProprietorPage}
import pages.controllingbody.ControllingBodyChangeScreenerPage
import pages.controllingbody.{ControllingBodyAddTradingNameYesNoPage, ControllingBodyBusinessNamePage, ControllingBodySoleProprietorPage}
import pages.correspondencedetails.*
import pages.licencespremises.*
import pages.partnerdetails.*
import pages.returnperiods.WhatToDoWithStandardReturnPeriodsPage
import pages.tradingdetails.*
import pages.tradingdetails.associatedregnumbers.*
import pages.tradingdetails.previousregnumbers.*
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class Navigator @Inject() () {

  private val normalRoutes: Page => UserAnswers => Call = {
    case ControllingBodyBusinessNamePage =>
      _ => controllers.controllingbody.routes.ControllingBodyAddTradingNameYesNoController.onPageLoad()
    case ControllingBodyAddTradingNameYesNoPage =>
      // TODO: Connect CB-TN and the business-type-specific identity screens when that journey is built.
      _ => routes.IndexController.onPageLoad()
    case ControllingBodySoleProprietorPage =>
      _ => routes.IndexController.onPageLoad() // TODO: Wire to CB-DOB or CB-CYA when the controlling body journey is built.
    case ControllingBodyEmailPage =>
      _ => routes.IndexController.onPageLoad() // TODO: Redirect to CB-CYA when the controlling body journey is built.
    case RemoveTradeNamePage =>
      _ => routes.CheckBusinessNameController.onPageLoad()
    case BusinessNamePage =>
      _ => routes.CheckBusinessNameController.onPageLoad()
    case SoleProprietorPage =>
      _ => routes.ChangeBusinessNameController.onPageLoad(Soleproprietor)
    case TradingNamePage =>
      _ => routes.CheckBusinessNameController.onPageLoad()
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
      _ => routes.CheckTradingDetailsController.onPageLoad()
    case IsSeasonalBusinessPage =>
      _ => routes.CheckTradingDetailsController.onPageLoad()
    case OtherTradeClassPage =>
      _ => routes.CheckTradingDetailsController.onPageLoad()
    case AddPreviousRegistrationNumberPage =>
      userAnswers => addPreviousRegistrationNumberRoute()(userAnswers)
    case PreviousRegNumberPage =>
      _ => routes.PreviousRegistrationNumberController.onPageLoad()
    case PreviousRegistrationNumbersListPage =>
      _ => routes.PreviousRegistrationNumbersListController.onPageLoad()
    case RemovePreviousRegNumberPage =>
      _ => routes.PreviousRegistrationNumbersListController.onPageLoad()
    case AddAssociatedRegistrationNumberPage =>
      userAnswers => navigateAddAssociatedRegistrationNumberPage()(userAnswers)
    case AssociatedRegNumberPage =>
      _ => routes.AssociatedRegistrationNumbersListController.onPageLoad()
    case AssociatedRegistrationNumbersPage =>
      _ => routes.AssociatedRegistrationNumbersListController.onPageLoad()
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
      _ => routes.CheckCorrespondenceDetailsController.onPageLoad()
    case RemoveCorrespondenceFaxNumberPage =>
      _ => routes.CheckCorrespondenceDetailsController.onPageLoad()
    case RemoveCorrespondenceEmailAddressPage =>
      _ => routes.CheckCorrespondenceDetailsController.onPageLoad()
    case CorrespondenceNamePage =>
      userAnswers => navigateCorrespondenceNamePage()(userAnswers)
    case CorrespondenceAdditionalNamePage =>
      userAnswers => navigateCorrespondenceAdditionalNamePage()(userAnswers)
    case CorrespondenceAdditionalInformationPage =>
      userAnswers => navigateCorrespondenceAdditionalInformationPage()(userAnswers)
    case RemoveCorrAddressAddInfoPage =>
      _ => routes.CheckCorrespondenceDetailsController.onPageLoad()
    case CorrespondenceAddressUkPage =>
      userAnswers => navigateCorrespondenceAddressUkPage()(userAnswers)
    case CorrespondenceAddressNonUkPage =>
      userAnswers => navigateCorrespondenceAddressNonUkPage()(userAnswers)
    case PartnerDetailsRemoveAdditionalInfoForPartnerAddressYesNoPage(index) =>
      userAnswers => navigateRemoveAdditionalInfoForPartnerAddressYesNoPage(index)(userAnswers)
    case BusinessChangeAddrScreenerPage =>
      userAnswers => navigateBusinessChangeAddrScreenerPage()(userAnswers)
    case BusinessUKAddrScreenerPage =>
      userAnswers => navigateBusinessUKAddrScreenerPage()(userAnswers)
    case BusinessAddressUkPage =>
      userAnswers => navigateBusinessAddressUkOrNonUkPage()(userAnswers)
    case BusinessAddressNonUkPage =>
      userAnswers => navigateBusinessAddressUkOrNonUkPage()(userAnswers)
    case RemoveBusinessAddressAddInfoPage =>
      _ => routes.CheckBusinessAddressController.onPageLoad()
    case BusinessAddressAdditionalInformationPage =>
      _ => routes.CheckBusinessAddressController.onPageLoad()
    case PartnerDetailsIsBusinessIncorporatedUkPage(index) =>
      userAnswers => navigatePartnerDetailsIsBusinessIncorporatedUkPage(index, userAnswers)

    // Partner Details
    case PartnerDetailsAdditionalAddressInfoPage(index) =>
      _ => controllers.partnerdetails.routes.PartnerDetailsAdditionalAddressInfoController.onPageLoad(index.toString, NormalMode)
    case PartnerDetailsAdditionalAddressInfoYesNoPage(index) =>
      userAnswers => navigatePartnerDetailsAdditionalAddressInfoYesNoPage(index)(userAnswers)
    case PartnerDetailsRemoveEmailAddressYesNoPage(index) =>
      userAnswers => navigatePartnerRemoveEmailYesNoPage(index)(userAnswers)
    case PartnerDetailsRemoveFaxNumberYesNoPage(index) =>
      userAnswers => navigatePartnerRemoveFaxNumberYesNoPage(index)(userAnswers)
    case PartnerDetailsAddFaxNumberYesNoPage(index) =>
      userAnswers => navigatePartnerAddFaxNumberYesNoPage(index)(userAnswers)
    case PartnerDetailsAddEmailAddressYesNoPage(index) =>
      userAnswers => navigatePartnerAddEmailAddressYesNoPage(index)(userAnswers)
    case PartnerDetailsRemovePartnerTradingNameYesNoPage(index) =>
      userAnswers => navigateRemovePartnerTradingNameYesNoPage(index)(userAnswers)
    case PartnerDetailsContactNumberPage(index) =>
      _ => controllers.partnerdetails.routes.PartnerDetailsContactDetailsController.onPageLoad(index.toString, NormalMode)
    case PartnerDetailsNinoPage(index) =>
      userAnswers => navigatePartnerRemoveNinoYesNoPage(index)(userAnswers)
    case PartnerDetailsAddNationalInsuranceNumberYesNoPage(index) =>
      userAnswers => navigatePartnerAddNinoYesNoPage(index)(userAnswers)
    case PartnerDetailsVrnPage(index) =>
      userAnswers => controllers.partnerdetails.routes.PartnerDetailsVatRegistrationNumberController.onPageLoad(index.toString, NormalMode)
    case PartnerDetailsVatRegistrationNumberYesNoPage(index) =>
      userAnswers => navigateVatRegistrationNumberYesNoPage(index)(userAnswers)
    case PartnerDetailsTradingNamePage(index) =>
      _ => controllers.partnerdetails.routes.PartnerDetailsTradingNameController.onPageLoad(index.toString, NormalMode)
    case PartnerDetailsAddTradingNameYesNoPage(index) =>
      userAnswers => navigatePartnerAddTradingNameYesNoPage(index)(userAnswers)
    case PartnerDetailsRemoveVatRegNumberYesNoPage(index) =>
      _ => controllers.partnerdetails.routes.PartnerDetailsRemoveVatRegNumberYesNoController.onPageLoad(index.toString, NormalMode)
    case PartnerDetailsBusinessTypePage(index) =>
      userAnswers => navigatePartnerDetailsBusinessTypePage(index)(userAnswers)
    case PartnerDetailsUtrPage(index) =>
      userAnswers => navigatePartnerDetailsUTRPage(index)(userAnswers)
    case PartnerDetailsForeignCorporateReferencePage(index) =>
      userAnswers => controllers.partnerdetails.routes.PartnerDetailsForeignCorporateReferenceController.onPageLoad(index.toString, NormalMode)
    case PartnerDetailsCorrespondenceEmailAddressPage(index) =>
      userAnswers => controllers.partnerdetails.routes.PartnerDetailsEmailAddressController.onPageLoad(index.toString, NormalMode)
    case PartnerDetailsCountryOfIncorporationPage(index) =>
      userAnswers => navigatePartnerDetailsCountryOfIncorporationPage(index)(userAnswers) // change it
    case PartnerDetailsRemovePartnerYesNoPage(index) =>
      userAnswers => navigatePartnerDetailsRemovePartnerPage(index)(userAnswers) // change it

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
    case WhatToDoWithStandardReturnPeriodsPage =>
      userAnswers => navigateWhatToDoWithStandardReturnPeriodsPage(userAnswers)

    case _ =>
      _ => routes.IndexController.onPageLoad()

  }

  private val checkRouteMap: Page => UserAnswers => Call = { _ => _ =>
    routes.ChangeRegistrationDetailsController.onPageLoad()
  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = {
    mode match {
      case NormalMode =>
        normalRoutes(page)(userAnswers)
      case CheckMode =>
        checkRouteMap(page)(userAnswers)
    }
  }

  private def navigateCorrespondenceNamePage()(answers: UserAnswers): Call =
    answers.get(AddCorrespondingDetailsYesNoPage) match {
      case Some(true) => routes.CorrespondenceAdditionalNameYesNoController.onPageLoad()
      case _          => routes.CheckCorrespondenceDetailsController.onPageLoad()
    }

  private def navigateCorrespondenceAdditionalNamePage()(answers: UserAnswers): Call =
    answers.get(AddCorrespondingDetailsYesNoPage) match {
      case Some(true) => routes.CorrespondenceUKAddrScreenerController.onPageLoad()
      case _          => routes.CheckCorrespondenceDetailsController.onPageLoad()
    }

  private def navigateCorrespondenceAddressUkPage()(answers: UserAnswers): Call =
    answers.get(AddCorrespondingDetailsYesNoPage) match {
      case Some(true) => routes.CorrespondenceAddrInfoScreenerController.onPageLoad()
      case _          => routes.CheckCorrespondenceDetailsController.onPageLoad()
    }

  private def navigateCorrespondenceAddressNonUkPage()(answers: UserAnswers): Call =
    answers.get(AddCorrespondingDetailsYesNoPage) match {
      case Some(true) => routes.CorrespondenceAddrInfoScreenerController.onPageLoad()
      case _          => routes.CheckCorrespondenceDetailsController.onPageLoad()
    }

  private def navigateCorrespondenceAdditionalInformationPage()(answers: UserAnswers): Call =
    answers.get(AddCorrespondingDetailsYesNoPage) match {
      case Some(true) => routes.CorrespondenceContactNumberController.onPageLoad()
      case _          => routes.CheckCorrespondenceDetailsController.onPageLoad()
    }

  private def navigateCorrespondenceContactNumberPage()(answers: UserAnswers): Call =
    answers.get(AddCorrespondingDetailsYesNoPage) match {
      case Some(true) => routes.FaxNumberForCorrespondenceYesNoController.onPageLoad()
      case _          => routes.CheckCorrespondenceDetailsController.onPageLoad()
    }

  private def navigateCorrespondenceFaxNumberPage()(answers: UserAnswers): Call =
    answers.get(AddCorrespondingDetailsYesNoPage) match {
      case Some(true) => routes.AddEmailAddressForCorrespondenceYesNoController.onPageLoad()
      case _          => routes.CheckCorrespondenceDetailsController.onPageLoad()
    }

  private def navigateAddBusinessAddressScreenerPage()(answers: UserAnswers): Call =
    answers.get(AddBusinessAddressAdditionalInformationPage) match {
      case Some(true) => routes.BusinessAddressAdditionalInfoController.onPageLoad()
      case _          => routes.CheckBusinessAddressController.onPageLoad()
    }

  private def navigateAddAssociatedRegistrationNumberPage()(answers: UserAnswers): Call =
    answers
      .get(AddAssociatedRegistrationNumberPage)
      .map {
        case false => routes.CheckTradingDetailsController.onPageLoad()
        case true  => routes.AssociatedRegNumberController.onPageLoad()
      }
      .getOrElse(routes.SystemErrorController.onPageLoad())

  private def addPreviousRegistrationNumberRoute()(userAnswers: UserAnswers): Call =
    userAnswers
      .get(AddPreviousRegistrationNumberPage)
      .map {
        case false => routes.CheckTradingDetailsController.onPageLoad()
        case true  => routes.PreviousRegistrationNumberController.onPageLoad()
      }
      .getOrElse(routes.SystemErrorController.onPageLoad())

  private def navigateCorrespondenceAdditionalNameYesNoPage()(userAnswers: UserAnswers): Call =
    userAnswers.get(CorrespondenceAdditionalNameYesNoPage) match {
      case Some(true) =>
        routes.CorrespondenceAdditionalNameController.onPageLoad()
      case Some(false) =>
        if (userAnswers.get(AddCorrespondingDetailsYesNoPage).contains(true)) {
          routes.CorrespondenceUKAddrScreenerController.onPageLoad()
        } else {
          routes.CheckCorrespondenceDetailsController.onPageLoad()
        }
      case None =>
        routes.SystemErrorController.onPageLoad()
    }

  private def navigateAddCorrespondenceAddressAdditionalInformationPage()(answers: UserAnswers): Call =
    answers.get(AddCorrespondenceAddressAdditionalInformationPage) match {
      case Some(true) =>
        routes.CorrespondenceAdditionalInfoController.onPageLoad()

      case Some(false) =>
        if (answers.get(AddCorrespondingDetailsYesNoPage).contains(true)) {
          routes.CorrespondenceContactNumberController.onPageLoad()
        } else {
          routes.CheckCorrespondenceDetailsController.onPageLoad()
        }
      case None =>
        routes.SystemErrorController.onPageLoad()
    }

  private def navigateCorrespondenceUKAddrScreenerPage()(answers: UserAnswers): Call = {

    val previouslyUk =
      answers.get(CorrespondenceAddressUkPage).isDefined

    val previouslyNonUk =
      answers.get(CorrespondenceAddressNonUkPage).isDefined

    answers.get(CorrespondenceUKAddrScreenerPage) match {
      case Some(true) if previouslyUk =>
        routes.CheckCorrespondenceDetailsController.onPageLoad()

      case Some(false) if previouslyNonUk =>
        routes.CheckCorrespondenceDetailsController.onPageLoad()

      case Some(true) =>
        routes.AddressLookupController.initialise()

      case Some(false) =>
        routes.CorrespondenceNonUKAddressController.onPageLoad()

      case None =>
        routes.SystemErrorController.onPageLoad()
    }
  }

  private def navigateBusinessUKAddrScreenerPage()(answers: UserAnswers): Call = {

    val previouslyUk =
      answers.get(BusinessAddressUkPage).isDefined

    val previouslyNonUk =
      answers.get(BusinessAddressNonUkPage).isDefined

    answers.get(BusinessUKAddrScreenerPage) match {
      case Some(true) if previouslyUk =>
        routes.CheckBusinessAddressController.onPageLoad()

      case Some(false) if previouslyNonUk =>
        routes.CheckBusinessAddressController.onPageLoad()

      case Some(true) =>
        routes.BusinessUKAddressController.onPageLoad()

      case Some(false) =>
        routes.BusinessNonUKAddressController.onPageLoad()

      case None =>
        routes.SystemErrorController.onPageLoad()
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
        case true  => routes.CorrespondenceNameController.onPageLoad()
        case false => routes.ChangeRegistrationDetailsController.onPageLoad()
      }
      .getOrElse(routes.SystemErrorController.onPageLoad())

  private def navigateCorrespondenceChangeAddrScreenerPage()(userAnswers: UserAnswers): Call = {

    val isUkAddress =
      userAnswers.get(CorrespondenceAddressUkPage).isDefined

    userAnswers
      .get(CorrespondenceChangeAddrScreenerPage)
      .map {
        case DifferentUkAddress =>
          routes.AddressLookupController.initialise()

        case ChangeToNonUkAddress =>
          routes.CorrespondenceNonUKAddressController.onPageLoad()

        case ChangeToUkAddress =>
          routes.AddressLookupController.initialise()

        case EditCurrentAddress if isUkAddress =>
          routes.CorrespondenceUKAddressController.onPageLoad()

        case EditCurrentAddress =>
          routes.CorrespondenceNonUKAddressController.onPageLoad()
      }
      .getOrElse(routes.SystemErrorController.onPageLoad())
  }

  private def navigateBusinessChangeAddrScreenerPage()(userAnswers: UserAnswers): Call = {
    val ukRoute = routes.BusinessUKAddressController.onPageLoad()
    val nonUkRoute = routes.BusinessNonUKAddressController.onPageLoad()
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
        routes.CorrespondenceFaxNumberController.onPageLoad()

      case Some(false) =>
        if (userAnswers.get(AddCorrespondingDetailsYesNoPage).contains(true))
          routes.AddEmailAddressForCorrespondenceYesNoController.onPageLoad()
        else
          routes.CheckCorrespondenceDetailsController.onPageLoad()

      case None =>
        routes.SystemErrorController.onPageLoad()
    }

  private def navigateAddEmailAddressForCorrespondenceYesNoPage()(userAnswers: UserAnswers): Call =
    userAnswers.get(AddEmailAddressForCorrespondenceYesNoPage) match {
      case Some(true) =>
        routes.CorrespondenceEmailAddressController.onPageLoad()

      case Some(false) =>
        if (userAnswers.get(AddCorrespondingDetailsYesNoPage).contains(true))
          routes.CheckCorrespondenceDetailsController.onPageLoad()
        else
          routes.CheckCorrespondenceDetailsController.onPageLoad()

      case None =>
        routes.SystemErrorController.onPageLoad()
    }

  private def navigateRemoveAssociatedRegNumberPage()(answers: UserAnswers): Call =
    answers
      .get(AssociatedRegistrationNumbersPage)
      .filter(_.nonEmpty)
      .map(_ => routes.AssociatedRegistrationNumbersListController.onPageLoad())
      .getOrElse(routes.CheckTradingDetailsController.onPageLoad())

  private def navigateRemoveCorrespondenceDetailsYesNoPage(answers: UserAnswers): Call =
    answers
      .get(RemoveCorrespondenceDetailsYesNoPage)
      .map {
        case false => routes.CheckCorrespondenceDetailsController.onPageLoad()
        case true  => routes.ChangeRegistrationDetailsController.onPageLoad()
      }
      .getOrElse(routes.SystemErrorController.onPageLoad())

  private def navigatePartnerAddFaxNumberYesNoPage(index: BusinessNumberOrIndex)(answers: UserAnswers): Call =
    answers
      .get(PartnerDetailsAddFaxNumberYesNoPage(index))
      .map(_ => controllers.partnerdetails.routes.PartnerDetailsAddFaxNumberYesNoController.onPageLoad(index.toString))
      .getOrElse(routes.SystemErrorController.onPageLoad())

  private def navigatePartnerRemoveNinoYesNoPage(index: BusinessNumberOrIndex)(answers: UserAnswers): Call =
    answers
      .get(PartnerDetailsRemoveNationalInsuranceNumberYesNoPage(index))
      .map(_ => controllers.partnerdetails.routes.PartnerDetailsRemoveNationalInsuranceNumberYesNoController.onPageLoad(index.toString, NormalMode))
      .getOrElse(routes.SystemErrorController.onPageLoad())

  private def navigatePartnerAddNinoYesNoPage(index: BusinessNumberOrIndex)(answers: UserAnswers): Call =
    answers
      .get(PartnerDetailsAddNationalInsuranceNumberYesNoPage(index))
      .map {
        case false =>
          // Should go to Add Nino
          controllers.partnerdetails.routes.PartnerDetailsAddNationalInsuranceNumberYesNoController.onPageLoad(index.toString)
        case true =>
          // Should go to Trading name
          controllers.partnerdetails.routes.PartnerDetailsAddNationalInsuranceNumberYesNoController.onPageLoad(index.toString)
      }
      .getOrElse(routes.SystemErrorController.onPageLoad())

  private def navigatePartnerAddTradingNameYesNoPage(index: BusinessNumberOrIndex)(answers: UserAnswers): Call =
    answers
      .get(PartnerDetailsAddTradingNameYesNoPage(index))
      .map {
        case false =>
          // Should go to Add/change trading name
          controllers.partnerdetails.routes.PartnerDetailsAddTradingNameYesNoController.onPageLoad(index.toString)
        case true =>
          // Should go to Is the partner's business incorporated in the UK? or PT-UTR - UTR Taxpayer Reference
          controllers.partnerdetails.routes.PartnerDetailsAddTradingNameYesNoController.onPageLoad(index.toString)
      }
      .getOrElse(routes.SystemErrorController.onPageLoad())

  private def navigatePartnerDetailsBusinessTypePage(index: BusinessNumberOrIndex)(answers: UserAnswers): Call =
    answers
      .get(PartnerDetailsBusinessTypePage(index))
      .fold(routes.SystemErrorController.onPageLoad())(businessType =>
        controllers.partnerdetails.routes.PartnerDetailsChangeBusinessNameController.onPageLoad(index.toString, businessType, NormalMode)
      )

  private def navigatePartnerDetailsUTRPage(index: BusinessNumberOrIndex)(answers: UserAnswers): Call =
    answers
      .get(PartnerDetailsUtrPage(index))
      .fold(routes.SystemErrorController.onPageLoad())(_ =>
        controllers.partnerdetails.routes.PartnerDetailsVatRegistrationNumberYesNoController.onPageLoad(index.toString)
      )

  /** If user is in the add partner flow, go to PT-FOR. Otherwise, if user has directly come from PT-CYA and hasn't changed answer to PT-IN, then
    * return to PT-CYA.
    */
  private def navigatePartnerDetailsCountryOfIncorporationPage(index: BusinessNumberOrIndex)(answers: UserAnswers): Call =
    answers
      .get(PartnerDetailsUtrPage(index))
      .fold(routes.SystemErrorController.onPageLoad())(_ =>
        controllers.partnerdetails.routes.PartnerDetailsAddCountryOfIncorporationController.onPageLoad(index.toString, NormalMode)
      )

  private def navigatePartnerDetailsRemovePartnerPage(index: BusinessNumberOrIndex)(answers: UserAnswers): Call =
    answers
      .get(PartnerDetailsRemovePartnerYesNoPage(index))
      .fold(routes.SystemErrorController.onPageLoad()) { wantToRemove =>
        if (wantToRemove) {
          index match {
            case businessNumber: String =>
              controllers.partnerdetails.routes.PartnerDetailsDeleteDateController.onPageLoad()
            case newPartnerIndex: Int =>
              controllers.partnerdetails.routes.PartnerDetailsRemovePartnerYesNoController.onPageLoad(index.toString, NormalMode)
          }
        } else
          controllers.partnerdetails.routes.PartnerDetailsRemovePartnerYesNoController.onPageLoad(index.toString, NormalMode)
      }

  private def navigatePartnerAddEmailAddressYesNoPage(index: BusinessNumberOrIndex)(answers: UserAnswers): Call =
    answers
      .get(PartnerDetailsAddEmailAddressYesNoPage(index))
      .map(_ => controllers.partnerdetails.routes.PartnerDetailsAddEmailAddressYesNoPageController.onPageLoad(index.toString))
      .getOrElse(routes.SystemErrorController.onPageLoad())

  private def navigatePartnerRemoveEmailYesNoPage(index: BusinessNumberOrIndex)(answers: UserAnswers): Call =
    answers
      .get(PartnerDetailsRemoveEmailAddressYesNoPage(index))
      .map(_ => controllers.partnerdetails.routes.PartnerDetailsRemoveEmailAddressYesNoController.onPageLoad(index.toString, NormalMode))
      .getOrElse(routes.SystemErrorController.onPageLoad())

  private def navigatePartnerRemoveFaxNumberYesNoPage(index: BusinessNumberOrIndex)(answers: UserAnswers): Call =
    answers
      .get(PartnerDetailsRemoveFaxNumberYesNoPage(index))
      .map(_ => controllers.partnerdetails.routes.PartnerDetailsRemoveFaxNumberYesNoController.onPageLoad(index.toString, NormalMode))
      .getOrElse(routes.SystemErrorController.onPageLoad())

  private def navigatePartnerDetailsAdditionalAddressInfoYesNoPage(index: BusinessNumberOrIndex)(userAnswers: UserAnswers): Call = {
    userAnswers
      .get(PartnerDetailsAdditionalAddressInfoYesNoPage(index))
      .map {
        case false => controllers.partnerdetails.routes.PartnerDetailsAdditionalAddressInfoYesNoController.onPageLoad(index.toString)
        case true  => controllers.partnerdetails.routes.PartnerDetailsAdditionalAddressInfoYesNoController.onPageLoad(index.toString)
      }
      .getOrElse(routes.SystemErrorController.onPageLoad())
  }

  private def navigateRemoveAdditionalInfoForPartnerAddressYesNoPage(index: BusinessNumberOrIndex)(userAnswers: UserAnswers): Call = {
    userAnswers
      .get(PartnerDetailsRemoveAdditionalInfoForPartnerAddressYesNoPage(index))
      .map {
        case false =>
          controllers.partnerdetails.routes.PartnerDetailsRemoveAdditionalInfoForPartnerAddressYesNoController.onPageLoad(index.toString, NormalMode)
        case true =>
          controllers.partnerdetails.routes.PartnerDetailsRemoveAdditionalInfoForPartnerAddressYesNoController.onPageLoad(index.toString, NormalMode)
      }
      .getOrElse(routes.SystemErrorController.onPageLoad())
  }

  private def navigateRemovePartnerTradingNameYesNoPage(index: BusinessNumberOrIndex)(userAnswers: UserAnswers): Call = {
    userAnswers
      .get(PartnerDetailsRemovePartnerTradingNameYesNoPage(index))
      .map {
        case false => controllers.partnerdetails.routes.PartnerDetailsRemovePartnerTradingNameYesNoController.onPageLoad(index.toString, NormalMode)
        case true  => controllers.routes.IndexController.onPageLoad()
      }
      .getOrElse(routes.SystemErrorController.onPageLoad())
  }

  private def navigateVatRegistrationNumberYesNoPage(index: BusinessNumberOrIndex)(userAnswers: UserAnswers): Call = {
    userAnswers
      .get(PartnerDetailsVatRegistrationNumberYesNoPage(index))
      .map {
        case false =>
          controllers.partnerdetails.routes.PartnerDetailsVatRegistrationNumberYesNoController.onPageLoad(index.toString)
        case true => controllers.partnerdetails.routes.PartnerDetailsVatRegistrationNumberYesNoController.onPageLoad(index.toString)
      }
      .getOrElse(routes.SystemErrorController.onPageLoad())
  }

  private def navigateBusinessAddressUkOrNonUkPage()(userAnswers: UserAnswers): Call = {
    val addFlowRoute = routes.BusinessAddrInfoScreenerController.onPageLoad()
    val normalRoute = routes.CheckBusinessAddressController.onPageLoad()
    userAnswers.get(BusinessAddressAddFlowPage) match {
      case Some(isInAddFlow) => if (isInAddFlow) addFlowRoute else normalRoute
      case None              => normalRoute
    }
  }

  private def navigateWhatToDoWithStandardReturnPeriodsPage(
    userAnswers: UserAnswers
  ): Call = {

    userAnswers.get(WhatToDoWithStandardReturnPeriodsPage) match {

      case Some(WhatToDoWithStandardReturnPeriods.Changemonthsstandardperiodcover) =>
        controllers.returnperiods.routes.ChooseReturnPeriodsController
          .onPageLoad(NormalMode)

      case Some(WhatToDoWithStandardReturnPeriods.Switchtononstandard) =>
        controllers.returnperiods.routes.ChooseReturnPeriodsController
          .onPageLoad(NormalMode)

      case Some(WhatToDoWithStandardReturnPeriods.Keepstandardreturnperiod) =>
        routes.ChangeRegistrationDetailsController.onPageLoad()

      case None =>
        routes.SystemErrorController.onPageLoad()
    }
  }

  private def navigatePartnerDetailsIsBusinessIncorporatedUkPage(index: BusinessNumberOrIndex, userAnswers: UserAnswers): Call = {
    userAnswers
      .get(PartnerDetailsIsBusinessIncorporatedUkPage(index))
      .map {
        case true  => routes.IndexController.onPageLoad() // TODO later -> DateOfIncorporation Screen
        case false => routes.IndexController.onPageLoad() // TODO later -> CountryOfIncorporation Screen
      }
      .getOrElse(routes.SystemErrorController.onPageLoad())

  }

  private def navigateControllingBodyChangeScreenerPage(userAnswers: UserAnswers): Call =
    userAnswers
      .get(ControllingBodyChangeScreenerPage)
      .map {
        case EditDetails => routes.IndexController.onPageLoad() // TODO later -> CB-CYA, controlling body check your answers
        case ProvideNew  => controllers.controllingbody.routes.ControllingBodyBusinessTypeController.onPageLoad()
        case KeepSame    => routes.ChangeRegistrationDetailsController.onPageLoad()
      }
      .getOrElse(routes.SystemErrorController.onPageLoad())
}
