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
    case RemoveTradeNamePage                   => _ => routes.CheckBusinessNameController.onPageLoad()
    case BusinessNamePage                      => _ => routes.CheckBusinessNameController.onPageLoad()
    case SoleProprietorPage                    => _ => routes.ChangeBusinessNameController.onPageLoad(Soleproprietor, NormalMode)
    case TradingNamePage                       => _ => routes.CheckBusinessNameController.onPageLoad()
    case BusinessFaxNumberPage                 => _ => routes.CheckContactDetailsController.onPageLoad()
    case RemoveFaxNumberPage                   => _ => routes.CheckContactDetailsController.onPageLoad()
    case RemoveEmailAddressPage                => _ => routes.CheckContactDetailsController.onPageLoad()
    case BusinessContactNumberPage             => _ => routes.CheckContactDetailsController.onPageLoad()
    case BusinessEmailAddressPage              => _ => routes.CheckContactDetailsController.onPageLoad()
    case BusinessTradeClassPage                => _ => routes.CheckTradingDetailsController.onPageLoad()
    case IsSeasonalBusinessPage                => _ => routes.CheckTradingDetailsController.onPageLoad()
    case OtherTradeClassPage                   => _ => routes.CheckTradingDetailsController.onPageLoad()
    case AddPreviousRegistrationNumberPage     => userAnswers => addPreviousRegistrationNumberRoute(NormalMode)(userAnswers)
    case PreviousRegNumberPage                 => _ => routes.PreviousRegistrationNumberController.onPageLoad(NormalMode)
    case PreviousRegistrationNumbersListPage   => _ => routes.PreviousRegistrationNumbersListController.onPageLoad(NormalMode)
    case RemovePreviousRegNumberPage           => _ => routes.PreviousRegistrationNumbersListController.onPageLoad(NormalMode)
    case AddAssociatedRegistrationNumberPage   => userAnswers => navigateAddAssociatedRegistrationNumberPage(NormalMode)(userAnswers)
    case AssociatedRegNumberPage               => _ => routes.AssociatedRegistrationNumbersListController.onPageLoad(NormalMode)
    case AssociatedRegistrationNumbersPage     => _ => routes.AssociatedRegistrationNumbersListController.onPageLoad(NormalMode)
    case RemoveAssociatedRegNumberPage         => userAnswers => navigateRemoveAssociatedRegNumberPage(NormalMode)(userAnswers)
    case AddCorrespondingDetailsYesNoPage      => userAnswers => navigateAddCorrespondingDetailsYesNoPage(NormalMode)(userAnswers)
    case CorrespondenceAdditionalNameYesNoPage => userAnswers => navigateCorrespondenceAdditionalNameYesNoPage(NormalMode)(userAnswers)
    case CorrespondenceContactNumberPage       => _ => routes.CheckCorrespondenceDetailsController.onPageLoad()
    case CorrespondenceFaxNumberPage           => _ => routes.CheckCorrespondenceDetailsController.onPageLoad()
    case CorrespondenceEmailPage               => _ => routes.CheckCorrespondenceDetailsController.onPageLoad()
    case RemoveCorrespondenceFaxNumberPage     => _ => routes.CheckCorrespondenceDetailsController.onPageLoad()
    case RemoveCorrespondenceEmailAddressPage  => _ => routes.CheckCorrespondenceDetailsController.onPageLoad()
    case CorrespondenceNamePage                => _ => routes.CheckCorrespondenceDetailsController.onPageLoad()
    case CorrespondenceAdditionalNamePage               => _ => routes.CheckCorrespondenceDetailsController.onPageLoad()
    case RemoveCorrAddressAddInfoPage          => _ => routes.CheckCorrespondenceDetailsController.onPageLoad()
    case _                                     => _ => routes.IndexController.onPageLoad()
  }

  private val checkRouteMap: Page => UserAnswers => Call = {
    case RemoveTradeNamePage                 => _ => routes.CheckBusinessNameController.onPageLoad()
    case BusinessNamePage                    => _ => routes.CheckBusinessNameController.onPageLoad()
    case SoleProprietorPage                  => _ => routes.ChangeBusinessNameController.onPageLoad(Soleproprietor, CheckMode)
    case BusinessEmailAddressPage            => _ => routes.BusinessEmailAddressController.onPageLoad(CheckMode)
    case IsSeasonalBusinessPage              => _ => routes.CheckTradingDetailsController.onPageLoad()
    case OtherTradeClassPage                 => _ => routes.CheckTradingDetailsController.onPageLoad()
    case BusinessTradeClassPage              => _ => routes.CheckTradingDetailsController.onPageLoad()
    case AddPreviousRegistrationNumberPage   => userAnswers => addPreviousRegistrationNumberRoute(CheckMode)(userAnswers)
    case PreviousRegNumberPage               => _ => routes.PreviousRegistrationNumberController.onPageLoad(CheckMode)
    case PreviousRegistrationNumbersListPage => _ => routes.PreviousRegistrationNumbersListController.onPageLoad(CheckMode)
    case RemovePreviousRegNumberPage         => userAnswers => navigateRemovePreviousRegNumberPage(CheckMode)(userAnswers)
    case AddAssociatedRegistrationNumberPage => userAnswers => navigateAddAssociatedRegistrationNumberPage(CheckMode)(userAnswers)
    case AssociatedRegNumberPage             => _ => routes.AssociatedRegistrationNumbersListController.onPageLoad(CheckMode)
    case AssociatedRegistrationNumbersPage   => _ => routes.AssociatedRegistrationNumbersListController.onPageLoad(CheckMode)
    case RemoveAssociatedRegNumberPage       => userAnswers => navigateRemoveAssociatedRegNumberPage(CheckMode)(userAnswers)
    case CorrespondenceFaxNumberPage         => _ => routes.CorrespondenceFaxNumberController.onPageLoad(CheckMode)
    case RemoveCorrespondenceFaxNumberPage   => _ => routes.RemoveCorrespondenceFaxNumberController.onPageLoad(CheckMode)
    case CorrespondenceNamePage              => _ => routes.CheckCorrespondenceDetailsController.onPageLoad()
    case CorrespondenceAdditionalNamePage              => _ => routes.CheckCorrespondenceDetailsController.onPageLoad()
    case RemoveCorrAddressAddInfoPage        => _ => routes.CheckCorrespondenceDetailsController.onPageLoad()
    case _                                   => _ => routes.CheckYourAnswersController.onPageLoad()
  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = {
    mode match {
      case NormalMode =>
        normalRoutes(page)(userAnswers)
      case CheckMode =>
        checkRouteMap(page)(userAnswers)
    }
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
        case true  => routes.PreviousRegistrationNumberController.onPageLoad(mode)
      }
      .getOrElse(routes.SystemErrorController.onPageLoad())

  private def navigateCorrespondenceAdditionalNameYesNoPage(mode: Mode)(userAnswers: UserAnswers): Call =
    userAnswers
      .get(CorrespondenceAdditionalNameYesNoPage)
      .map {
        case false => routes.IndexController.onPageLoad() // change it
        case true  => routes.IndexController.onPageLoad()
      }
      .getOrElse(routes.SystemErrorController.onPageLoad())

  private def navigateAddCorrespondingDetailsYesNoPage(mode: Mode)(userAnswers: UserAnswers): Call =
    userAnswers
      .get(AddCorrespondingDetailsYesNoPage)
      .map {
        case true  => routes.CorrespondenceNameController.onPageLoad(mode)
        case false => routes.ChangeRegistrationDetailsController.onPageLoad()
      }
      .getOrElse(routes.SystemErrorController.onPageLoad())

  private def navigateRemoveAssociatedRegNumberPage(mode: Mode)(answers: UserAnswers): Call =
    answers
      .get(AssociatedRegistrationNumbersPage)
      .filter(_.nonEmpty)
      .map(_ => routes.AssociatedRegistrationNumbersListController.onPageLoad(mode))
      .getOrElse(routes.CheckTradingDetailsController.onPageLoad())

  private def navigateRemovePreviousRegNumberPage(mode: Mode)(answers: UserAnswers): Call =
    answers
      .get(PreviousRegistrationNumbersListPage)
      .filter(_.nonEmpty)
      .map(_ => routes.RemovePreviousRegNumberController.onPageLoad(mode))
      .getOrElse(routes.CheckTradingDetailsController.onPageLoad())

}
