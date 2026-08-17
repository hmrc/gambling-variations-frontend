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
import models.BusinessType.Soleproprietor
import models.CorrespondenceChangeAddrOption.*
import pages.*
import pages.partner.*
import pages.partnerdetails.PartnerDetailsContactNumberPage
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
    case PartnerAddFaxNumberYesNoPage =>
      userAnswers => navigatePartnerAddFaxNumberYesNoPage(userAnswers)
    case RemoveAdditionalInfoForPartnerAddressYesNoPage =>
      userAnswers => navigateRemoveAdditionalInfoForPartnerAddressYesNoPage()(userAnswers)
    case PartnerDetailsAdditionalAddressInfoPage =>
      _ => controllers.partner.routes.PartnerDetailsAdditionalAddressInfoController.onPageLoad()
    case PartnerDetailsAdditionalAddressInfoYesNoPage =>
      userAnswers => navigatePartnerDetailsAdditionalAddressInfoYesNoPage()(userAnswers)
    case BusinessChangeAddrScreenerPage =>
      userAnswers => navigateBusinessChangeAddrScreenerPage()(userAnswers)
    case RemovePartnerTradingNameYesNoPage(index) =>
      userAnswers => navigateRemovePartnerTradingNameYesNoPage(index)(userAnswers)
    case PartnerEmailAddressPage =>
      _ => controllers.partner.routes.PartnerEmailAddressController.onPageLoad()
    case BusinessUKAddrScreenerPage =>
      userAnswers => navigateBusinessUKAddrScreenerPage()(userAnswers)
    case RemoveBusinessAddressAddInfoPage => _ => routes.CheckCorrespondenceDetailsController.onPageLoad()
    case PartnerDetailsContactNumberPage(index) =>
      _ => controllers.partner.routes.PartnerContactDetailsController.onPageLoad()
    case PartnerDetailsRemoveEmailAddressYesNoPage(index) =>
      userAnswers => navigatePartnerRemoveEmailYesNoPage(userAnswers, index)

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
      case _          => routes.PageNotFoundController.onPageLoad()
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
        if (userAnswers.get(AddCorrespondingDetailsYesNoPage).contains(true))
          routes.CorrespondenceUKAddrScreenerController.onPageLoad()
        else
          routes.CheckCorrespondenceDetailsController.onPageLoad()

      case None =>
        routes.SystemErrorController.onPageLoad()
    }

  private def navigateAddCorrespondenceAddressAdditionalInformationPage()(answers: UserAnswers): Call =
    answers.get(AddCorrespondenceAddressAdditionalInformationPage) match {
      case Some(true) =>
        routes.CorrespondenceAdditionalInfoController.onPageLoad()

      case Some(false) =>
        if (answers.get(AddCorrespondingDetailsYesNoPage).contains(true))
          routes.CorrespondenceContactNumberController.onPageLoad()
        else
          routes.CheckCorrespondenceDetailsController.onPageLoad()

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
        routes.PageNotFoundController.onPageLoad()

      case Some(false) if previouslyNonUk =>
        routes.PageNotFoundController.onPageLoad()

      case Some(true) =>
        routes.PageNotFoundController.onPageLoad()

      case Some(false) =>
        routes.PageNotFoundController.onPageLoad()

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
          routes.PageNotFoundController.onPageLoad()

        case ChangeToNonUkAddress =>
          routes.CorrespondenceNonUKAddressController.onPageLoad()

        case ChangeToUkAddress =>
          routes.CorrespondenceUKAddressController.onPageLoad()

        case EditCurrentAddress if isUkAddress =>
          routes.CorrespondenceUKAddressController.onPageLoad()

        case EditCurrentAddress =>
          routes.CorrespondenceNonUKAddressController.onPageLoad()
      }
      .getOrElse(routes.SystemErrorController.onPageLoad())
  }

  private def navigateBusinessChangeAddrScreenerPage()(userAnswers: UserAnswers): Call =
    userAnswers
      .get(BusinessChangeAddrScreenerPage)
      .map {
        case BusinessChangeAddrOption.DifferentUkAddress   => routes.PageNotFoundController.onPageLoad()
        case BusinessChangeAddrOption.ChangeToNonUkAddress => routes.PageNotFoundController.onPageLoad()
        case BusinessChangeAddrOption.ChangeToUkAddress    => routes.PageNotFoundController.onPageLoad()
        case BusinessChangeAddrOption.EditCurrentAddress   => routes.PageNotFoundController.onPageLoad()
      }
      .getOrElse(routes.SystemErrorController.onPageLoad())

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

  private def navigatePartnerAddFaxNumberYesNoPage(answers: UserAnswers): Call =
    answers
      .get(PartnerAddFaxNumberYesNoPage)
      .map {
        case false => controllers.partner.routes.PartnerAddFaxNumberYesNoController.onPageLoad()
        case true  => controllers.partner.routes.PartnerAddFaxNumberYesNoController.onPageLoad()
      }
      .getOrElse(routes.SystemErrorController.onPageLoad())

  private def navigatePartnerRemoveEmailYesNoPage(answers: UserAnswers, index: Int): Call =
    answers
      .get(PartnerDetailsRemoveEmailAddressYesNoPage(index))
      .map {
        case false => controllers.partner.routes.PartnerDetailsRemoveEmailAddressYesNoController.onPageLoad()
        case true  => controllers.partner.routes.PartnerDetailsRemoveEmailAddressYesNoController.onPageLoad()
      }
      .getOrElse(routes.SystemErrorController.onPageLoad())

  private def navigatePartnerDetailsAdditionalAddressInfoYesNoPage()(userAnswers: UserAnswers): Call = {
    userAnswers
      .get(PartnerDetailsAdditionalAddressInfoYesNoPage)
      .map {
        case false => controllers.partner.routes.PartnerDetailsAdditionalAddressInfoYesNoController.onPageLoad()
        case true  => controllers.partner.routes.PartnerDetailsAdditionalAddressInfoYesNoController.onPageLoad()
      }
      .getOrElse(routes.SystemErrorController.onPageLoad())
  }

  private def navigateRemoveAdditionalInfoForPartnerAddressYesNoPage()(userAnswers: UserAnswers): Call = {
    userAnswers
      .get(RemoveAdditionalInfoForPartnerAddressYesNoPage)
      .map {
        case false => controllers.partner.routes.RemoveAdditionalInfoForPartnerAddressYesNoController.onPageLoad()
        case true  => controllers.partner.routes.RemoveAdditionalInfoForPartnerAddressYesNoController.onPageLoad()
      }
      .getOrElse(routes.SystemErrorController.onPageLoad())
  }

  private def navigateRemovePartnerTradingNameYesNoPage(index: Int)(userAnswers: UserAnswers): Call = {
    userAnswers
      .get(RemovePartnerTradingNameYesNoPage(index))
      .map {
        case false => controllers.partner.routes.RemovePartnerTradingNameYesNoController.onPageLoad() // need to update it
        case true  => controllers.routes.IndexController.onPageLoad()
      }
      .getOrElse(routes.SystemErrorController.onPageLoad())
  }

}
