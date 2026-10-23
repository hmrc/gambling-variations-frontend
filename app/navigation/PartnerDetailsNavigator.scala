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
import models.{BusinessType, Mode, UserAnswers}
import pages.partnerdetails.*
import pages.{BusinessNumberOrIndex, Page, QuestionPage}
import play.api.mvc.Call
import utils.PartnerUtils
import viewmodels.checkAnswers.partnerdetails.PartnerMandatoryDetails
import viewmodels.checkAnswers.partnerdetails.PartnerMandatoryDetails.firstMissing

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
      addYesNo(
        index,
        PartnerDetailsAddTradingNameYesNoPage(index),
        ifYes = partnerRoutes.PartnerDetailsTradingNameController.onPageLoad(key(index), mode(index)),
        ifNo  = to(partnerRoutes.PartnerDateOfJoiningController.onPageLoad(key(index), mode(index)))
      )

    case PartnerDetailsAddNationalInsuranceNumberYesNoPage(index) =>
      addYesNo(
        index,
        PartnerDetailsAddNationalInsuranceNumberYesNoPage(index),
        ifYes = partnerRoutes.PartnerDetailsAddNationalInsuranceNumberController.onPageLoad(key(index), mode(index)),
        ifNo  = to(partnerRoutes.PartnerDetailsAddUTRController.onPageLoad(key(index), mode(index)))
      )

    case PartnerDetailsVatRegistrationNumberYesNoPage(index) =>
      addYesNo(
        index,
        PartnerDetailsVatRegistrationNumberYesNoPage(index),
        ifYes = partnerRoutes.PartnerDetailsVatRegistrationNumberController.onPageLoad(key(index), mode(index)),
        // TODO -> this should go to address lookup
        ifNo = to(partnerRoutes.PartnerDetailsAdditionalAddressInfoController.onPageLoad(key(index), mode(index)))
      )

    case PartnerDetailsAdditionalAddressInfoYesNoPage(index) =>
      addYesNo(
        index,
        PartnerDetailsAdditionalAddressInfoYesNoPage(index),
        ifYes = partnerRoutes.PartnerDetailsAdditionalAddressInfoController.onPageLoad(key(index), mode(index)),
        ifNo  = to(partnerRoutes.PartnerDetailsContactDetailsController.onPageLoad(key(index), mode(index)))
      )

    case PartnerDetailsAddFaxNumberYesNoPage(index) =>
      addYesNo(
        index,
        PartnerDetailsAddFaxNumberYesNoPage(index),
        ifYes = partnerRoutes.PartnerDetailsChangePartnerFaxNumberController.onPageLoad(key(index), mode(index)),
        ifNo  = to(partnerRoutes.PartnerDetailsAddEmailAddressYesNoPageController.onPageLoad(key(index)))
      )

    case PartnerDetailsAddEmailAddressYesNoPage(index) =>
      addYesNo(
        index,
        PartnerDetailsAddEmailAddressYesNoPage(index),
        ifYes = partnerRoutes.PartnerDetailsEmailAddressController.onPageLoad(key(index), mode(index)),
        ifNo  = to(checkYourAnswers(index))
      )

    // --- Value pages: carry on to the next page in the journey ---

    case PartnerDetailsBusinessNamePage(index) =>
      carryOn(index, to(partnerRoutes.PartnerDetailsAddTradingNameYesNoController.onPageLoad(key(index))))
    case PartnerDetailsSoleProprietorPage(index) =>
      carryOn(index, to(partnerRoutes.PartnerDetailsSoleProprietorDobController.onPageLoad(key(index), mode(index))))
    case PartnerDetailsDateOfBirthPage(index) =>
      carryOn(index, to(partnerRoutes.PartnerDetailsAddTradingNameYesNoController.onPageLoad(key(index))))
    case PartnerDetailsTradingNamePage(index) =>
      carryOn(index, to(partnerRoutes.PartnerDateOfJoiningController.onPageLoad(key(index), mode(index))))
    case PartnerDetailsDateOfJoiningPage(index) =>
      carryOn(index, afterDateOfJoining(index))
    case PartnerDetailsDateOfIncorporationPage(index) =>
      carryOn(index, afterDateOfIncorporation(index))
    case PartnerDetailsNinoPage(index) =>
      carryOn(index, to(partnerRoutes.PartnerDetailsAddUTRController.onPageLoad(key(index), mode(index))))
    case PartnerDetailsCrnPage(index) =>
      carryOn(index, to(partnerRoutes.PartnerDetailsAddUTRController.onPageLoad(key(index), mode(index))))
    case PartnerDetailsCountryOfIncorporationPage(index) =>
      carryOn(index, to(partnerRoutes.PartnerDetailsForeignCorporateReferenceController.onPageLoad(key(index), mode(index))))
    case PartnerDetailsForeignCorporateReferencePage(index) =>
      carryOn(index, to(partnerRoutes.PartnerDetailsAddUTRController.onPageLoad(key(index), mode(index))))
    case PartnerDetailsUtrPage(index) =>
      carryOn(index, to(partnerRoutes.PartnerDetailsVatRegistrationNumberYesNoController.onPageLoad(key(index))))
    case PartnerDetailsVrnPage(index) => // TODO -> this should go to address lookup
      carryOn(index, to(partnerRoutes.PartnerDetailsAdditionalAddressInfoYesNoController.onPageLoad(key(index))))
    case PartnerDetailsAdditionalAddressInfoPage(index) =>
      carryOn(index, to(partnerRoutes.PartnerDetailsContactDetailsController.onPageLoad(key(index), mode(index))))
    case PartnerDetailsContactNumberPage(index) =>
      carryOn(index, to(partnerRoutes.PartnerDetailsAddFaxNumberYesNoController.onPageLoad(key(index))))
    case PartnerDetailsCorrespondenceFaxNumberPage(index) =>
      carryOn(index, to(partnerRoutes.PartnerDetailsAddEmailAddressYesNoPageController.onPageLoad(key(index))))
    case PartnerDetailsCorrespondenceEmailAddressPage(index) =>
      carryOn(index, nextMissing(index))

    // --- Remove questions: only reachable from check your answers ---

    case PartnerDetailsRemovePartnerTradingNameYesNoPage(index)              => _ => checkYourAnswers(index)
    case PartnerDetailsRemoveNationalInsuranceNumberYesNoPage(index)         => _ => checkYourAnswers(index)
    case PartnerDetailsRemoveVatRegNumberYesNoPage(index)                    => _ => checkYourAnswers(index)
    case PartnerDetailsRemoveEmailAddressYesNoPage(index)                    => _ => checkYourAnswers(index)
    case PartnerDetailsRemoveFaxNumberYesNoPage(index)                       => _ => checkYourAnswers(index)
    case PartnerDetailsRemoveAdditionalInfoForPartnerAddressYesNoPage(index) => _ => checkYourAnswers(index)

    // --- Remove partner: reached from the partner list, so either answer returns there ---

    case PartnerDetailsRemovePartnerYesNoPage(index) =>
      answers =>
        answers
          .get(PartnerDetailsRemovePartnerYesNoPage(index))
          .fold(systemError)(_ => partnerList)
  }

  /** The partner type comes from the index, so both modes share the same routes. */
  val partnerDetailsNormalRoutes: PartialFunction[Page, UserAnswers => Call] = partnerDetailsRoutes
  val partnerDetailsCheckRoutes: PartialFunction[Page, UserAnswers => Call] = partnerDetailsRoutes

  // --- Journey branches ---

  private def afterDateOfJoining(index: BusinessNumberOrIndex): UserAnswers => Call =
    answers =>
      answers.get(PartnerDetailsBusinessTypePage(index)) match {
        case Some(BusinessType.Soleproprietor) =>
          partnerRoutes.PartnerDetailsAddNationalInsuranceNumberYesNoController.onPageLoad(key(index))
        case Some(BusinessType.Corporatebody) =>
          partnerRoutes.PartnerDetailsIsBusinessIncorporatedUkController.onPageLoad(key(index), mode(index))
        case Some(BusinessType.Unincorporatedbody) =>
          partnerRoutes.PartnerDetailsAddUTRController.onPageLoad(key(index), mode(index))
        case Some(BusinessType.Partnership) =>
          partnerRoutes.PartnerDetailsVatRegistrationNumberYesNoController.onPageLoad(key(index))
        case Some(BusinessType.LimitedLiabilityPartnership) =>
          partnerRoutes.PartnerDetailsDateOfIncorporationController.onPageLoad(key(index), mode(index))
        case None => systemError
      }

  private def afterDateOfIncorporation(index: BusinessNumberOrIndex): UserAnswers => Call =
    answers => {
      lazy val incorporatedInUk = answers.get(PartnerDetailsIsBusinessIncorporatedUkPage(index)).contains(true)
      val crn = partnerRoutes.PartnerDetailsCompanyRegistrationNumberController.onPageLoad(key(index), mode(index))

      answers.get(PartnerDetailsBusinessTypePage(index)) match {
        case Some(BusinessType.Corporatebody) if incorporatedInUk => crn
        case Some(BusinessType.LimitedLiabilityPartnership)       => crn
        case _                                                    => systemError
      }
    }

  // --- Journey mechanics ---

  /** Existing partners and saved new partners go back to check your answers. */
  private def returnsToCheckYourAnswers(index: BusinessNumberOrIndex, answers: UserAnswers): Boolean =
    index match {
      case _: String => true // existing partner
      case i: Int    => answers.get(PartnerDetailsHasAnsweredAllQuestionsPage(i)).contains(true) // new partner that has reached CYA
    }

  private def carryOn(index: BusinessNumberOrIndex, next: UserAnswers => Call): UserAnswers => Call =
    answers =>
      if (returnsToCheckYourAnswers(index, answers)) checkYourAnswers(index)
      else next(answers)

  private def addYesNo(
    index: BusinessNumberOrIndex,
    page: QuestionPage[Boolean],
    ifYes: => Call,
    ifNo: UserAnswers => Call
  ): UserAnswers => Call =
    answers =>
      answers.get(page) match {
        case Some(true)  => ifYes
        case Some(false) => carryOn(index, ifNo)(answers)
        case None        => systemError
      }

  private def to(call: => Call): UserAnswers => Call = _ => call

  /** Fallback where the journey order isn't defined yet: first unanswered mandatory page, or check your answers. */
  private def nextMissing(index: BusinessNumberOrIndex): UserAnswers => Call =
    answers => firstMissing(answers, index).map(_.call).getOrElse(checkYourAnswers(index))

  // --- Helpers ---

  private def key(index: BusinessNumberOrIndex): String = index.toString

  private def mode(index: BusinessNumberOrIndex): Mode = PartnerUtils.modeFor(index)

  private def checkYourAnswers(index: BusinessNumberOrIndex): Call =
    partnerRoutes.PartnerDetailsCheckYourAnswersController.onPageLoad(key(index))

  private val partnerList: Call = partnerRoutes.PartnerDetailsController.onPageLoad(None)

  private val systemError: Call = routes.SystemErrorController.onPageLoad()
}
