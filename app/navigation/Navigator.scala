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
    case RemoveTradeNamePage             => _ => routes.CheckBusinessNameController.onPageLoad()
    case BusinessNamePage                => _ => routes.CheckBusinessNameController.onPageLoad()
    case SoleProprietorPage              => _ => routes.ChangeBusinessNameController.onPageLoad(Soleproprietor, NormalMode)
    case TradingNamePage                 => _ => routes.CheckBusinessNameController.onPageLoad()
    case FaxNumberPage                   => _ => routes.CheckContactDetailsController.onPageLoad()
    case RemoveFaxNumberPage             => _ => routes.CheckContactDetailsController.onPageLoad()
    case RemoveEmailAddressPage          => _ => routes.CheckContactDetailsController.onPageLoad()
    case BusinessContactNumberPage       => _ => routes.CheckContactDetailsController.onPageLoad()
    case BusinessEmailAddressPage        => _ => routes.CheckContactDetailsController.onPageLoad()
    case BusinessTradeClassPage          => _ => routes.BusinessTradeClassController.onPageLoad(NormalMode)
    case IsSeasonalBusinessPage          => _ => routes.SeasonalBusinessController.onPageLoad(NormalMode)
    case CorrespondenceContactNumberPage => _ => routes.SeasonalBusinessController.onPageLoad(NormalMode)
    case _                               => _ => routes.IndexController.onPageLoad()
  }

  private val checkRouteMap: Page => UserAnswers => Call = {
    case RemoveTradeNamePage      => _ => routes.CheckBusinessNameController.onPageLoad()
    case BusinessNamePage         => _ => routes.CheckBusinessNameController.onPageLoad()
    case SoleProprietorPage       => _ => routes.ChangeBusinessNameController.onPageLoad(Soleproprietor, CheckMode)
    case BusinessEmailAddressPage => _ => routes.ChangeEmailAddressController.onPageLoad(CheckMode)
    case IsSeasonalBusinessPage   => _ => routes.SeasonalBusinessController.onPageLoad(CheckMode)
    case _                        => _ => routes.CheckYourAnswersController.onPageLoad()
  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = {
    mode match {
      case NormalMode =>
        normalRoutes(page)(userAnswers)
      case CheckMode =>
        checkRouteMap(page)(userAnswers)
    }
  }
}
