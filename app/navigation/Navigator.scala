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
import pages.*
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class Navigator @Inject() () {

  private val normalRoutes: Page => UserAnswers => Call = {
    case RemoveTradeNamePage                       => _ => routes.CheckBusinessNameController.onPageLoad()
    case BusinessNamePage                          => _ => routes.CheckBusinessNameController.onPageLoad()
    case SoleProprietorPage                        => _ => routes.ChangeBusinessNameController.onPageLoad(Soleproprietor)
    case TradingNamePage                           => _ => routes.CheckBusinessNameController.onPageLoad()
    case BusinessFaxNumberPage                     => _ => routes.CheckContactDetailsController.onPageLoad()
    case RemoveFaxNumberPage                       => _ => routes.CheckContactDetailsController.onPageLoad()
    case RemoveEmailAddressPage                    => _ => routes.CheckContactDetailsController.onPageLoad()
    case BusinessContactNumberPage                 => _ => routes.CheckContactDetailsController.onPageLoad()
    case BusinessEmailAddressPage                  => _ => routes.CheckContactDetailsController.onPageLoad()
    case BusinessTradeClassPage                    => _ => routes.CheckTradingDetailsController.onPageLoad()
    case IsSeasonalBusinessPage                    => _ => routes.CheckTradingDetailsController.onPageLoad()
    case OtherTradeClassPage                       => _ => routes.CheckTradingDetailsController.onPageLoad()
    case AddPreviousRegistrationNumberPage         => userAnswers => addPreviousRegistrationNumberRoute(NormalMode)(userAnswers)
    case PreviousRegNumberPage                     => _ => routes.PreviousRegistrationNumberController.onPageLoad()
    case PreviousRegistrationNumbersListPage       => _ => routes.PreviousRegistrationNumbersListController.onPageLoad()
    case RemovePreviousRegNumberPage               => _ => routes.PreviousRegistrationNumbersListController.onPageLoad()
    case AddAssociatedRegistrationNumberPage       => userAnswers => navigateAddAssociatedRegistrationNumberPage(NormalMode)(userAnswers)
    case AssociatedRegNumberPage                   => _ => routes.AssociatedRegistrationNumbersListController.onPageLoad()
    case AssociatedRegistrationNumbersPage         => _ => routes.AssociatedRegistrationNumbersListController.onPageLoad()
    case RemoveAssociatedRegNumberPage             => userAnswers => navigateRemoveAssociatedRegNumberPage(NormalMode)(userAnswers)
    case AddCorrespondingDetailsYesNoPage          => userAnswers => navigateAddCorrespondingDetailsYesNoPage(NormalMode)(userAnswers)
    case CorrespondenceChangeAddrScreenerPage      => userAnswers => navigateCorrespondenceChangeAddrScreenerPage(NormalMode)(userAnswers)
    case CorrespondenceAdditionalNameYesNoPage     => userAnswers => navigateCorrespondenceAdditionalNameYesNoPage(NormalMode)(userAnswers)
    case CorrespondenceContactNumberPage           => userAnswers => navigateCorrespondenceContactNumberPage(NormalMode)(userAnswers)
    case AddCorrespondenceFaxNumberPage            => userAnswers => navigateAddCorrespondenceFaxNumberPage(NormalMode)(userAnswers)
    case CorrespondenceFaxNumberPage               => userAnswers => navigateCorrespondenceFaxNumberPage(NormalMode)(userAnswers)
    case AddEmailAddressForCorrespondenceYesNoPage => userAnswers => navigateAddEmailAddressForCorrespondenceYesNoPage(NormalMode)(userAnswers)
    case RemoveCorrespondenceDetailsYesNoPage      => userAnswers => navigateRemoveCorrespondenceDetailsYesNoPage(userAnswers)
    case AddCorrespondenceAddressAdditionalInformationPage =>
      userAnswers => navigateAddCorrespondenceAddressAdditionalInformationPage(NormalMode)(userAnswers)
    case CorrespondenceUKAddrScreenerPage =>
      userAnswers => navigateCorrespondenceUKAddrScreenerPage(NormalMode)(userAnswers)
    case CorrespondenceEmailPage                 => _ => routes.CheckCorrespondenceDetailsController.onPageLoad()
    case RemoveCorrespondenceFaxNumberPage       => _ => routes.CheckCorrespondenceDetailsController.onPageLoad()
    case RemoveCorrespondenceEmailAddressPage    => _ => routes.CheckCorrespondenceDetailsController.onPageLoad()
    case CorrespondenceNamePage                  => userAnswers => navigateCorrespondenceNamePage(NormalMode)(userAnswers)
    case CorrespondenceAdditionalNamePage        => userAnswers => navigateCorrespondenceAdditionalNamePage(NormalMode)(userAnswers)
    case CorrespondenceAdditionalInformationPage => userAnswers => navigateCorrespondenceAdditionalInformationPage(NormalMode)(userAnswers)
    case RemoveCorrAddressAddInfoPage            => _ => routes.CheckCorrespondenceDetailsController.onPageLoad()
    case CorrespondenceAddressUkPage             => userAnswers => navigateCorrespondenceAddressUkPage(NormalMode)(userAnswers)
    case CorrespondenceAddressNonUkPage          => userAnswers => navigateCorrespondenceAddressNonUkPage(NormalMode)(userAnswers)
    case _                                       => _ => routes.IndexController.onPageLoad()
  }

  private val checkRouteMap: Page => UserAnswers => Call = { _ => _ =>
    routes.CheckYourAnswersController.onPageLoad()
  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = {
    mode match {
      case NormalMode =>
        normalRoutes(page)(userAnswers)
      case CheckMode =>
        checkRouteMap(page)(userAnswers)
    }
  }

  private def navigateCorrespondenceNamePage(mode: Mode)(answers: UserAnswers): Call =
    answers.get(AddCorrespondingDetailsYesNoPage) match {
      case Some(true) => routes.CorrespondenceAdditionalNameYesNoController.onPageLoad()
      case _          => routes.CheckCorrespondenceDetailsController.onPageLoad()
    }

  private def navigateCorrespondenceAdditionalNamePage(mode: Mode)(answers: UserAnswers): Call =
    answers.get(AddCorrespondingDetailsYesNoPage) match {
      case Some(true) => routes.CorrespondenceUKAddrScreenerController.onPageLoad()
      case _          => routes.CheckCorrespondenceDetailsController.onPageLoad()
    }

  private def navigateCorrespondenceAddressUkPage(mode: Mode)(answers: UserAnswers): Call =
    answers.get(AddCorrespondingDetailsYesNoPage) match {
      case Some(true) => routes.CorrespondenceAddrInfoScreenerController.onPageLoad()
      case _          => routes.CheckCorrespondenceDetailsController.onPageLoad()
    }

  private def navigateCorrespondenceAddressNonUkPage(mode: Mode)(answers: UserAnswers): Call =
    answers.get(AddCorrespondingDetailsYesNoPage) match {
      case Some(true) => routes.CorrespondenceAddrInfoScreenerController.onPageLoad()
      case _          => routes.CheckCorrespondenceDetailsController.onPageLoad()
    }

  private def navigateCorrespondenceAdditionalInformationPage(mode: Mode)(answers: UserAnswers): Call =
    answers.get(AddCorrespondingDetailsYesNoPage) match {
      case Some(true) => routes.CorrespondenceContactNumberController.onPageLoad()
      case _          => routes.CheckCorrespondenceDetailsController.onPageLoad()
    }

  private def navigateCorrespondenceContactNumberPage(mode: Mode)(answers: UserAnswers): Call =
    answers.get(AddCorrespondingDetailsYesNoPage) match {
      case Some(true) => routes.FaxNumberForCorrespondenceYesNoController.onPageLoad()
      case _          => routes.CheckCorrespondenceDetailsController.onPageLoad()
    }

  private def navigateCorrespondenceFaxNumberPage(mode: Mode)(answers: UserAnswers): Call =
    answers.get(AddCorrespondingDetailsYesNoPage) match {
      case Some(true) => routes.AddEmailAddressForCorrespondenceYesNoController.onPageLoad()
      case _          => routes.CheckCorrespondenceDetailsController.onPageLoad()
    }

  private def navigateAddAssociatedRegistrationNumberPage(mode: Mode)(answers: UserAnswers): Call =
    answers
      .get(AddAssociatedRegistrationNumberPage)
      .map {
        case false => routes.CheckTradingDetailsController.onPageLoad()
        case true  => routes.AssociatedRegNumberController.onPageLoad(mode)
      }
      .getOrElse(routes.SystemErrorController.onPageLoad())

  private def addPreviousRegistrationNumberRoute(mode: Mode)(userAnswers: UserAnswers): Call =
    userAnswers
      .get(AddPreviousRegistrationNumberPage)
      .map {
        case false => routes.CheckTradingDetailsController.onPageLoad()
        case true  => routes.PreviousRegistrationNumberController.onPageLoad()
      }
      .getOrElse(routes.SystemErrorController.onPageLoad())

  private def navigateCorrespondenceAdditionalNameYesNoPage(mode: Mode)(userAnswers: UserAnswers): Call =
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

  private def navigateAddCorrespondenceAddressAdditionalInformationPage(mode: Mode)(answers: UserAnswers): Call =
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

  private def navigateCorrespondenceUKAddrScreenerPage(mode: Mode)(userAnswers: UserAnswers): Call =
    userAnswers
      .get(CorrespondenceUKAddrScreenerPage)
      .map {
        case false => routes.CorrespondenceNonUKAddressController.onPageLoad()
        case true  => routes.CorrespondenceUKAddressController.onPageLoad()
      }
      .getOrElse(routes.SystemErrorController.onPageLoad())

  private def navigateAddCorrespondingDetailsYesNoPage(mode: Mode)(userAnswers: UserAnswers): Call =
    userAnswers
      .get(AddCorrespondingDetailsYesNoPage)
      .map {
        case true  => routes.CorrespondenceNameController.onPageLoad()
        case false => routes.ChangeRegistrationDetailsController.onPageLoad()
      }
      .getOrElse(routes.SystemErrorController.onPageLoad())

  private def navigateCorrespondenceChangeAddrScreenerPage(mode: Mode)(userAnswers: UserAnswers): Call = {

    val isUkAddress =
      userAnswers.get(CorrespondenceAddressUkPage).isDefined

    userAnswers
      .get(CorrespondenceChangeAddrScreenerPage)
      .map {
        case true if isUkAddress =>
          routes.CorrespondenceNonUKAddressController.onPageLoad()

        case false if isUkAddress =>
          routes.CorrespondenceUKAddressController.onPageLoad()

        case true =>
          routes.CorrespondenceUKAddressController.onPageLoad()

        case false =>
          routes.CorrespondenceNonUKAddressController.onPageLoad()
      }
      .getOrElse(routes.SystemErrorController.onPageLoad())
  }

  private def navigateAddCorrespondenceFaxNumberPage(mode: Mode)(userAnswers: UserAnswers): Call =
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

  private def navigateAddEmailAddressForCorrespondenceYesNoPage(mode: Mode)(userAnswers: UserAnswers): Call =
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

  private def navigateRemoveAssociatedRegNumberPage(mode: Mode)(answers: UserAnswers): Call =
    answers
      .get(AssociatedRegistrationNumbersPage)
      .filter(_.nonEmpty)
      .map(_ => routes.AssociatedRegistrationNumbersListController.onPageLoad())
      .getOrElse(routes.CheckTradingDetailsController.onPageLoad())

  private def navigateRemovePreviousRegNumberPage(mode: Mode)(answers: UserAnswers): Call =
    answers
      .get(PreviousRegistrationNumbersListPage)
      .filter(_.nonEmpty)
      .map(_ => routes.RemovePreviousRegNumberController.onPageLoad())
      .getOrElse(routes.CheckTradingDetailsController.onPageLoad())

  private def navigateRemoveCorrespondenceDetailsYesNoPage(answers: UserAnswers): Call =
    answers
      .get(RemoveCorrespondenceDetailsYesNoPage)
      .map {
        case false => routes.CheckCorrespondenceDetailsController.onPageLoad()
        case true  => routes.ChangeRegistrationDetailsController.onPageLoad()
      }
      .getOrElse(routes.SystemErrorController.onPageLoad())

}
