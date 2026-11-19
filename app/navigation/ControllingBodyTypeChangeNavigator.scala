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

import controllers.controllingbody.routes as controllingBodyRoutes
import models.BusinessType.{LimitedLiabilityPartnership, Partnership}
import models.{BusinessType, UserAnswers}
import pages.Page
import pages.controllingbody.{ControllingBodyBusinessNamePage, ControllingBodyBusinessTypePage, ControllingBodyCrnPage, ControllingBodyDateOfIncorporationPage, ControllingBodyPreviousBusinessTypePage, ControllingBodyUtrPage}
import play.api.mvc.Call

/** The mini-flows that follow when the user changes the type of an existing controlling body.
  *
  * They take precedence over the normal routes for as long as the type differs from the one the controlling body had, and only for the pages listed
  * here. Every other page keeps its normal route.
  */
object ControllingBodyTypeChangeNavigator {

  def typeChangeRoutes(answers: UserAnswers): PartialFunction[Page, Call] =
    (answers.get(ControllingBodyPreviousBusinessTypePage), answers.get(ControllingBodyBusinessTypePage)) match {
      case (Some(previous), Some(LimitedLiabilityPartnership)) if previous != LimitedLiabilityPartnership => changeToLlpRoutes(previous)
      case _                                                                                              => PartialFunction.empty
    }

  // CB-BN-LLP, CB-DIN and CB-CR, then CB-UTR only when the previous type was a partnership, and then CB-CYA.
  private def changeToLlpRoutes(previous: BusinessType): PartialFunction[Page, Call] = {
    case ControllingBodyBusinessNamePage =>
      controllingBodyRoutes.DateOfIncorporationController.onPageLoad()
    case ControllingBodyDateOfIncorporationPage =>
      controllingBodyRoutes.ControllingBodyCompanyRegistrationNumberController.onPageLoad()
    case ControllingBodyCrnPage if previous == Partnership =>
      controllingBodyRoutes.ControllingBodyUTRController.onPageLoad()
    case ControllingBodyCrnPage =>
      controllingBodyRoutes.CheckControllingBodyDetailsController.onPageLoad()
    case ControllingBodyUtrPage if previous == Partnership =>
      controllingBodyRoutes.CheckControllingBodyDetailsController.onPageLoad()
  }
}
