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

import controllers.partnerdetails.routes as partnerRoutes
import controllers.routes
import models.{Mode, UserAnswers}
import pages.partnerdetails.*
import pages.{BusinessNumberOrIndex, Page, QuestionPage}
import play.api.mvc.Call
import utils.PartnerUtils
import viewmodels.checkAnswers.partnerdetails.PartnerMandatoryDetails

/** Partner details routing.
  *
  * The partner type is encoded in the index (and the mode it was parsed from):
  *   - existing partner: String business number (CheckMode)
  *   - new partner: Int index into newPartners (NormalMode)
  *
  * After an answer is saved, the user returns to check your answers if the partner is existing, or is a new partner that has already been saved.
  * Otherwise, they carry on through the add journey.
  */
object PartnerDetailsNavigator {

  // Defined first: the vals below reference it, and object vals initialise top to bottom
  private val partnerDetailsRoutes: PartialFunction[Page, UserAnswers => Call] = {

    // --- Answers that change which pages apply: always follow the branch ---

    case PartnerDetailsBusinessTypePage(index) =>
      answers =>
        answers
          .get(PartnerDetailsBusinessTypePage(index))
          .fold(systemError)(bt => partnerRoutes.PartnerDetailsChangeBusinessNameController.onPageLoad(key(index), bt, mode(index)))

    case PartnerDetailsIsBusinessIncorporatedUkPage(index) =>
      answers =>
        answers.get(PartnerDetailsIsBusinessIncorporatedUkPage(index)) match {
          case Some(true)  => partnerRoutes.PartnerDetailsDateOfIncorporationController.onPageLoad(key(index), mode(index))
          case Some(false) => partnerRoutes.PartnerDetailsAddCountryOfIncorporationController.onPageLoad(key(index), mode(index))
          case None        => systemError
        }

    // --- Optional "add X?" questions: yes goes to X, no carries on ---

    case PartnerDetailsAddTradingNameYesNoPage(index) =>
      addYesNo(index,
               PartnerDetailsAddTradingNameYesNoPage(index),
               partnerRoutes.PartnerDetailsTradingNameController.onPageLoad(key(index), mode(index))
              )

    case PartnerDetailsAddNationalInsuranceNumberYesNoPage(index) =>
      addYesNo(
        index,
        PartnerDetailsAddNationalInsuranceNumberYesNoPage(index),
        partnerRoutes.PartnerDetailsAddNationalInsuranceNumberController.onPageLoad(key(index), mode(index))
      )

    case PartnerDetailsVatRegistrationNumberYesNoPage(index) =>
      addYesNo(
        index,
        PartnerDetailsVatRegistrationNumberYesNoPage(index),
        partnerRoutes.PartnerDetailsVatRegistrationNumberController.onPageLoad(key(index), mode(index))
      )

    case PartnerDetailsAdditionalAddressInfoYesNoPage(index) =>
      addYesNo(
        index,
        PartnerDetailsAdditionalAddressInfoYesNoPage(index),
        partnerRoutes.PartnerDetailsAdditionalAddressInfoController.onPageLoad(key(index), mode(index))
      )

    case PartnerDetailsAddFaxNumberYesNoPage(index) =>
      addYesNo(index,
               PartnerDetailsAddFaxNumberYesNoPage(index),
               partnerRoutes.PartnerDetailsChangePartnerFaxNumberController.onPageLoad(key(index), mode(index))
              )

    case PartnerDetailsAddEmailAddressYesNoPage(index) =>
      addYesNo(index,
               PartnerDetailsAddEmailAddressYesNoPage(index),
               partnerRoutes.PartnerDetailsEmailAddressController.onPageLoad(key(index), mode(index))
              )

    // --- Value pages: carry on ---

    case PartnerDetailsTradingNamePage(index)                => carryOn(index)
    case PartnerDetailsNinoPage(index)                       => carryOn(index)
    case PartnerDetailsUtrPage(index)                        => carryOn(index)
    case PartnerDetailsVrnPage(index)                        => carryOn(index)
    case PartnerDetailsCountryOfIncorporationPage(index)     => carryOn(index)
    case PartnerDetailsForeignCorporateReferencePage(index)  => carryOn(index)
    case PartnerDetailsAdditionalAddressInfoPage(index)      => carryOn(index)
    case PartnerDetailsContactNumberPage(index)              => carryOn(index)
    case PartnerDetailsCorrespondenceEmailAddressPage(index) => carryOn(index)

    // --- Remove questions: only reachable from check your answers ---

    case PartnerDetailsRemovePartnerTradingNameYesNoPage(index)              => _ => checkYourAnswers(index)
    case PartnerDetailsRemoveNationalInsuranceNumberYesNoPage(index)         => _ => checkYourAnswers(index)
    case PartnerDetailsRemoveVatRegNumberYesNoPage(index)                    => _ => checkYourAnswers(index)
    case PartnerDetailsRemoveEmailAddressYesNoPage(index)                    => _ => checkYourAnswers(index)
    case PartnerDetailsRemoveFaxNumberYesNoPage(index)                       => _ => checkYourAnswers(index)
    case PartnerDetailsRemoveAdditionalInfoForPartnerAddressYesNoPage(index) => _ => checkYourAnswers(index)
  }

  /** The partner type comes from the index, so both modes share the same routes. */
  val normalRoutes: PartialFunction[Page, UserAnswers => Call] = partnerDetailsRoutes
  val checkRoutes: PartialFunction[Page, UserAnswers => Call] = partnerDetailsRoutes

  /** Existing partners and saved new partners go back to check your answers. */
  private def returnsToCheckYourAnswers(index: BusinessNumberOrIndex, answers: UserAnswers): Boolean =
    index match {
      case _: String => true // existing partner
      case i: Int => answers.get(PartnerDetailsAddPartnerCompletedPage(i)).contains(true) // saved new partner
    }

  private def carryOn(index: BusinessNumberOrIndex)(answers: UserAnswers): Call =
    if (returnsToCheckYourAnswers(index, answers)) checkYourAnswers(index)
    else nextInAddJourney(index, answers)

  /** TODO: replace with the designed journey order.
   * For now: the first unanswered mandatory page, or check your answers when there is none.
   * Skips optional "add X?" questions the user hasn't reached yet.
   */
  private def nextInAddJourney(index: BusinessNumberOrIndex, answers: UserAnswers): Call =
    PartnerMandatoryDetails.firstMissing(answers, index).map(_.call).getOrElse(checkYourAnswers(index))

  private def addYesNo(index: BusinessNumberOrIndex, page: QuestionPage[Boolean], ifYes: => Call): UserAnswers => Call =
    answers =>
      answers.get(page) match {
        case Some(true) => ifYes
        case Some(false) => carryOn(index)(answers)
        case None => systemError
      }

  // --- Helpers ---

  private def key(index: BusinessNumberOrIndex): String = index.toString

  private def mode(index: BusinessNumberOrIndex): Mode = PartnerUtils.modeFor(index)

  private def checkYourAnswers(index: BusinessNumberOrIndex): Call =
    partnerRoutes.PartnerDetailsCheckYourAnswersController.onPageLoad(key(index))

  private val systemError: Call = routes.SystemErrorController.onPageLoad()
}
