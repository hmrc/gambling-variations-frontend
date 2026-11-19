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
import models.BusinessType.{Corporatebody, LimitedLiabilityPartnership, Partnership, Soleproprietor, Unincorporatedbody}
import models.{BusinessType, UserAnswers}
import pages.Page
import pages.controllingbody.*
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
      case (Some(previous), Some(Unincorporatedbody)) if previous != Unincorporatedbody => changeToUnincorporatedBodyRoutes(previous)
      case (Some(previous), Some(Soleproprietor)) if previous != Soleproprietor         => changeToSoleproprietorRoutes(previous, answers)
      case (Some(previous), Some(Corporatebody)) if previous != Corporatebody           => changeToCorporateBodyRoutes(previous, answers)
      case _                                                                            => PartialFunction.empty
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

  // CB-BN-UB, then CB-UTR only when the previous type was a partnership, and then CB-CYA. From the other types the
  // normal route already takes CB-BN-UB to CB-CYA.
  private def changeToUnincorporatedBodyRoutes(previous: BusinessType): PartialFunction[Page, Call] = {
    case ControllingBodyBusinessNamePage if previous == Partnership =>
      controllingBodyRoutes.ControllingBodyUTRController.onPageLoad()
    case ControllingBodyUtrPage if previous == Partnership =>
      controllingBodyRoutes.CheckControllingBodyDetailsController.onPageLoad()
  }

  // CB-NI-SC, then CB-NI when Yes is selected.
  // When No is selected, navigate to CB-UTR if the previous type was a partnership,
  // otherwise navigate to CB-CYA.
  // CB-NI navigates to CB-UTR only when the previous type was a partnership,
  // otherwise navigate to CB-CYA.
  // CB-UTR navigates to CB-CYA.
  private def changeToSoleproprietorRoutes(previous: BusinessType, answers: UserAnswers): PartialFunction[Page, Call] = {
    case ControllingBodyDateOfBirthPage =>
      controllingBodyRoutes.ControllingBodyAddNINumberYesNoController.onPageLoad()
    case ControllingBodyAddNINumberYesNoPage if answers.get(ControllingBodyAddNINumberYesNoPage).contains(true) =>
      controllingBodyRoutes.ControllingBodyNINumberController.onPageLoad()
    case ControllingBodyAddNINumberYesNoPage if answers.get(ControllingBodyAddNINumberYesNoPage).contains(false) && previous == Partnership =>
      controllingBodyRoutes.ControllingBodyUTRController.onPageLoad()
    case ControllingBodyAddNINumberYesNoPage =>
      controllingBodyRoutes.CheckControllingBodyDetailsController.onPageLoad()
    case ControllingBodyNinoPage if previous == Partnership =>
      controllingBodyRoutes.ControllingBodyUTRController.onPageLoad()
    case ControllingBodyNinoPage =>
      controllingBodyRoutes.CheckControllingBodyDetailsController.onPageLoad()
    case ControllingBodyUtrPage if previous == Partnership =>
      controllingBodyRoutes.CheckControllingBodyDetailsController.onPageLoad()
  }

  // CB-BN-CB and CB-IN, then CB-DIN and CB-CR for a business incorporated in the UK, or CB-COU and CB-FOR for one
  // incorporated abroad. After that CB-UTR only when the previous type was a partnership, and then CB-CYA.
  private def changeToCorporateBodyRoutes(previous: BusinessType, answers: UserAnswers): PartialFunction[Page, Call] = {
    case ControllingBodyBusinessNamePage =>
      controllingBodyRoutes.ControllingBodyBusinessUKIncorporatedController.onPageLoad()
    case ControllingBodyIsUkIncorporatedPage if answers.get(ControllingBodyIsUkIncorporatedPage).contains("1") =>
      controllingBodyRoutes.DateOfIncorporationController.onPageLoad()
    case ControllingBodyIsUkIncorporatedPage if answers.get(ControllingBodyIsUkIncorporatedPage).contains("0") =>
      controllingBodyRoutes.ControllingBodyCountryOfIncorporationController.onPageLoad()
    case ControllingBodyDateOfIncorporationPage =>
      controllingBodyRoutes.ControllingBodyCompanyRegistrationNumberController.onPageLoad()
    case ControllingBodyCountryOfIncorporationPage =>
      controllingBodyRoutes.ControllingBodyForeignCorpRefController.onPageLoad()
    case ControllingBodyCrnPage | ControllingBodyForeignCorpRefPage if previous == Partnership =>
      controllingBodyRoutes.ControllingBodyUTRController.onPageLoad()
    case ControllingBodyCrnPage | ControllingBodyForeignCorpRefPage =>
      controllingBodyRoutes.CheckControllingBodyDetailsController.onPageLoad()
    case ControllingBodyUtrPage if previous == Partnership =>
      controllingBodyRoutes.CheckControllingBodyDetailsController.onPageLoad()
  }
}
