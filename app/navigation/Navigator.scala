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
import pages.*
import pages.businessaddress.*
import pages.businessname.*
import pages.contactdetails.*
import pages.correspondencedetails.*
import pages.licencespremises.{LicenceNumberPage, LicencesPremisesPage, RemoveLicenceNumberPage, RemovePremisesDetailsYesNoPage}
import pages.partnerdetails.*
import pages.tradingdetails.*
import pages.tradingdetails.associatedregnumbers.*
import pages.tradingdetails.previousregnumbers.*
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class Navigator @Inject() () {

  private val normalRoutes: Page => UserAnswers => Call = {
    case RemoveTradeNamePage =>
      _ => routes.CheckBusinessNameController.onPageLoad()
    case BusinessNamePage =>
      _ => routes.CheckBusinessNameController.onPageLoad()
    case SoleProprietorPage =>
      _ => routes.ChangeBusinessNameController.onPageLoad(Soleproprietor)
    case TradingNamePage =>
      _ => routes.CheckBusinessNameController.onPageLoad()
    case BusinessFaxNumberPage =>
      _ => routes.CheckContactDetailsController.onPageLoad()
    case RemoveFaxNumberPage =>
      _ => routes.CheckContactDetailsController.onPageLoad()
    case RemoveEmailAddressPage =>
      _ => routes.CheckContactDetailsController.onPageLoad()
    case BusinessContactNumberPage =>
      _ => routes.CheckContactDetailsController.onPageLoad()
    case BusinessEmailAddressPage =>
      _ => routes.CheckContactDetailsController.onPageLoad()
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
      userAnswers => navigateRemoveAdditionalInfoForPartnerAddressYesNoPage()(userAnswers)
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
      userAnswers => navigatePartnerDetailsIsBusinessIncorporatedUkPage(???, userAnswers) // TODO index

    // Partner Details
    case PartnerDetailsAdditionalAddressInfoPage(index) =>
      _ => controllers.partnerdetails.routes.PartnerDetailsAdditionalAddressInfoController.onPageLoad(???, NormalMode) // TODO filling bs
    case PartnerDetailsAdditionalAddressInfoYesNoPage(index) =>
      userAnswers => navigatePartnerDetailsAdditionalAddressInfoYesNoPage()(userAnswers)
    case PartnerDetailsRemoveEmailAddressYesNoPage(index) =>
      userAnswers => navigatePartnerRemoveEmailYesNoPage(???)(userAnswers) // TODO ???
    case PartnerDetailsRemoveFaxNumberYesNoPage(index) =>
      userAnswers => navigatePartnerRemoveFaxNumberYesNoPage(???)(userAnswers) // TODO ???
    case PartnerDetailsAddFaxNumberYesNoPage(index) =>
      userAnswers => navigatePartnerAddFaxNumberYesNoPage(???)(userAnswers) // TODO ???
    case PartnerDetailsAddEmailAddressYesNoPage(index) =>
      userAnswers => navigatePartnerAddEmailAddressYesNoPage(???)(userAnswers) // TODO ???
    case PartnerDetailsRemovePartnerTradingNameYesNoPage(index) =>
      userAnswers => navigateRemovePartnerTradingNameYesNoPage(???)(userAnswers) // TODO ???
    case PartnerDetailsContactNumberPage(index) =>
      _ => controllers.partnerdetails.routes.PartnerDetailsContactDetailsController.onPageLoad(???, ???) // TODO
    case PartnerDetailsNinoPage(index) =>
      userAnswers => navigatePartnerRemoveNinoYesNoPage(???)(userAnswers) // TODO ???
    case PartnerDetailsAddNationalInsuranceNumberYesNoPage(index) =>
      userAnswers => navigatePartnerAddNinoYesNoPage(???)(userAnswers) // TODO ???
    case PartnerDetailsVrnPage(index) =>
      userAnswers => controllers.partnerdetails.routes.PartnerDetailsVatRegistrationNumberController.onPageLoad(???, ???) // TODO: to be plumbed in
    case PartnerDetailsVatRegistrationNumberYesNoPage(index) =>
      userAnswers => navigateVatRegistrationNumberYesNoPage(???)(userAnswers) // TODO ???
    case PartnerDetailsRemoveEmailAddressYesNoPage(index) =>
      _ => controllers.partnerdetails.routes.PartnerDetailsEmailAddressController.onPageLoad(???, ???) // TODO ???
    case PartnerDetailsTradingNamePage(index) =>
      _ => controllers.partnerdetails.routes.PartnerDetailsTradingNameController.onPageLoad(???, ???) // TODO ???
    case PartnerDetailsAddTradingNameYesNoPage(index) =>
      userAnswers => navigatePartnerAddTradingNameYesNoPage(???)(userAnswers)
    case PartnerDetailsRemoveVatRegNumberYesNoPage(index) =>
      _ => controllers.partnerdetails.routes.PartnerDetailsRemoveVatRegNumberYesNoController.onPageLoad(???) // TODO ???
    case PartnerDetailsBusinessTypePage(index) =>
      userAnswers => navigatePartnerDetailsBusinessTypePage(index.asInstanceOf[Int])(userAnswers) // TODO ????
    case PartnerDetailsUtrPage(index) =>
      userAnswers => navigatePartnerDetailsUTRPage(???)(userAnswers) // TODO ???
    case PartnerDetailsForeignCorporateReferencePage(index) =>
      userAnswers => controllers.partnerdetails.routes.PartnerDetailsForeignCorporateReferenceController.onPageLoad(???) // TODO ???

    // License and Premises Details
    case LicenceNumberPage =>
      _ => controllers.licencespremises.routes.CheckLicencesAndPremisesController.onPageLoad()
    case RemoveLicenceNumberPage =>
      _ => controllers.licencespremises.routes.CheckLicencesAndPremisesController.onPageLoad()
    case RemovePremisesDetailsYesNoPage =>
      _ => controllers.licencespremises.routes.RemovePremisesDetailsYesNoController.onPageLoad() // change it
    case LicencesPremisesPage =>
      _ => controllers.licencespremises.routes.LicencesPremisesController.onPageLoad() // change it

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

  private def navigatePartnerAddFaxNumberYesNoPage(index: Int)(answers: UserAnswers): Call =
    answers
      .get(PartnerDetailsAddFaxNumberYesNoPage(index))
      .map(_ => controllers.partnerdetails.routes.PartnerDetailsAddFaxNumberYesNoController.onPageLoad(???)) // TODO ???
      .getOrElse(routes.SystemErrorController.onPageLoad())

  private def navigatePartnerRemoveNinoYesNoPage(index: Int)(answers: UserAnswers): Call =
    answers
      .get(PartnerDetailsRemoveNationalInsuranceNumberYesNoPage(index))
      .map(_ => controllers.partnerdetails.routes.PartnerDetailsRemoveNationalInsuranceNumberYesNoController.onPageLoad(???)) // TODO ???
      .getOrElse(routes.SystemErrorController.onPageLoad())

  private def navigatePartnerAddNinoYesNoPage(index: Int)(answers: UserAnswers): Call =
    answers
      .get(PartnerDetailsAddNationalInsuranceNumberYesNoPage(index))
      .map {
        case false =>
          // Should go to Add Nino
          controllers.partnerdetails.routes.PartnerDetailsAddNationalInsuranceNumberYesNoController.onPageLoad(???) // TODO ???
        case true =>
          // Should go to Trading name
          controllers.partnerdetails.routes.PartnerDetailsAddNationalInsuranceNumberYesNoController.onPageLoad(???) // TODO ???
      }
      .getOrElse(routes.SystemErrorController.onPageLoad())

  private def navigatePartnerAddTradingNameYesNoPage(index: Int)(answers: UserAnswers): Call =
    answers
      .get(PartnerDetailsAddTradingNameYesNoPage(index))
      .map {
        case false =>
          // Should go to Add/change trading name
          controllers.partnerdetails.routes.PartnerDetailsAddTradingNameYesNoController.onPageLoad(???) // TODO ???
        case true =>
          // Should go to Is the partner's business incorporated in the UK? or PT-UTR - UTR Taxpayer Reference
          controllers.partnerdetails.routes.PartnerDetailsAddTradingNameYesNoController.onPageLoad(???) // TODO ???
      }
      .getOrElse(routes.SystemErrorController.onPageLoad())

  private def navigatePartnerDetailsBusinessTypePage(index: Int)(answers: UserAnswers): Call =
    answers
      .get(PartnerDetailsBusinessTypePage(index))
      .fold(routes.SystemErrorController.onPageLoad())(toBeUsed => // TODO
        controllers.partnerdetails.routes.PartnerDetailsChangeBusinessNameController.onPageLoad(index.toString, toBeUsed, NormalMode)
      ) // TODO ???

  private def navigatePartnerDetailsUTRPage(index: Int)(answers: UserAnswers): Call =
    answers
      .get(PartnerDetailsUtrPage(index))
      .fold(routes.SystemErrorController.onPageLoad())(_ =>
        controllers.partnerdetails.routes.PartnerDetailsVatRegistrationNumberYesNoController.onPageLoad(???) // TODO ???
      )

  private def navigatePartnerAddEmailAddressYesNoPage(index: Int)(answers: UserAnswers): Call =
    answers
      .get(PartnerDetailsAddEmailAddressYesNoPage(index))
      .map(_ => controllers.partnerdetails.routes.PartnerDetailsAddEmailAddressYesNoPageController.onPageLoad(???)) // TODO ???
      .getOrElse(routes.SystemErrorController.onPageLoad())

  private def navigatePartnerRemoveEmailYesNoPage(index: Int)(answers: UserAnswers): Call =
    answers
      .get(PartnerDetailsRemoveEmailAddressYesNoPage(index))
      .map(_ => controllers.partnerdetails.routes.PartnerDetailsRemoveEmailAddressYesNoController.onPageLoad(???)) // TODO ???
      .getOrElse(routes.SystemErrorController.onPageLoad())

  private def navigatePartnerRemoveFaxNumberYesNoPage(index: Int)(answers: UserAnswers): Call =
    answers
      .get(PartnerDetailsRemoveFaxNumberYesNoPage(index))
      .map(_ => controllers.partnerdetails.routes.PartnerDetailsRemoveFaxNumberYesNoController.onPageLoad(???)) // TODO ???
      .getOrElse(routes.SystemErrorController.onPageLoad())

  private def navigatePartnerDetailsAdditionalAddressInfoYesNoPage()(userAnswers: UserAnswers): Call = {
    userAnswers
      .get(PartnerDetailsAdditionalAddressInfoYesNoPage("-1-1-1-1-")) // TODO
      .map {
        case false => controllers.partnerdetails.routes.PartnerDetailsAdditionalAddressInfoYesNoController.onPageLoad(???) // TODO ???
        case true  => controllers.partnerdetails.routes.PartnerDetailsAdditionalAddressInfoYesNoController.onPageLoad(???) // TODO ???
      }
      .getOrElse(routes.SystemErrorController.onPageLoad())
  }

  private def navigateRemoveAdditionalInfoForPartnerAddressYesNoPage()(userAnswers: UserAnswers): Call = {
    userAnswers
      .get(PartnerDetailsRemoveAdditionalInfoForPartnerAddressYesNoPage("-1-1-1-1")) // TODO
      .map {
        case false => controllers.partnerdetails.routes.PartnerDetailsRemoveAdditionalInfoForPartnerAddressYesNoController.onPageLoad(???) // TODO ???
        case true  => controllers.partnerdetails.routes.PartnerDetailsRemoveAdditionalInfoForPartnerAddressYesNoController.onPageLoad(???) // TODO ???
      }
      .getOrElse(routes.SystemErrorController.onPageLoad())
  }

  private def navigateRemovePartnerTradingNameYesNoPage(index: Int)(userAnswers: UserAnswers): Call = {
    userAnswers
      .get(PartnerDetailsRemovePartnerTradingNameYesNoPage(index))
      .map {
        case false => controllers.partnerdetails.routes.PartnerDetailsRemovePartnerTradingNameYesNoController.onPageLoad(???) // TODO ???
        case true  => controllers.routes.IndexController.onPageLoad()
      }
      .getOrElse(routes.SystemErrorController.onPageLoad())
  }

  private def navigateVatRegistrationNumberYesNoPage(index: Int)(userAnswers: UserAnswers): Call = {
    userAnswers
      .get(PartnerDetailsVatRegistrationNumberYesNoPage(index))
      .map {
        case false =>
          controllers.partnerdetails.routes.PartnerDetailsVatRegistrationNumberYesNoController.onPageLoad(???) // TODO ??? // need to update it
        case true => controllers.partnerdetails.routes.PartnerDetailsVatRegistrationNumberYesNoController.onPageLoad(???) // TODO ???
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

  private def navigatePartnerDetailsIsBusinessIncorporatedUkPage(index: Int, userAnswers: UserAnswers): Call = {
    userAnswers
      .get(PartnerDetailsIsBusinessIncorporatedUkPage(index))
      .map {
        case true  => routes.IndexController.onPageLoad() // TODO later -> DateOfIncorporation Screen
        case false => routes.IndexController.onPageLoad() // TODO later -> CountryOfIncorporation Screen
      }
      .getOrElse(routes.SystemErrorController.onPageLoad())

  }
}
