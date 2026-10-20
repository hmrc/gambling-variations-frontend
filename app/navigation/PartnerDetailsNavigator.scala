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
import models.{BusinessType, NormalMode, UserAnswers}
import pages.partnerdetails.*
import pages.{BusinessNumberOrIndex, Page}
import play.api.mvc.Call

object PartnerDetailsNavigator {

  private val partnerDetailsRoutes: PartialFunction[Page, UserAnswers => Call] = {
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
    case PartnerDetailsBusinessNamePage(index) =>
      _ => controllers.partnerdetails.routes.PartnerDetailsAddTradingNameYesNoController.onPageLoad(index.toString)
    case PartnerDetailsSoleProprietorPage(index) =>
      _ => controllers.partnerdetails.routes.PartnerDetailsSoleProprietorDobController.onPageLoad(index.toString, NormalMode)
    case PartnerDetailsDateOfBirthPage(index) =>
      _ => controllers.partnerdetails.routes.PartnerDetailsAddTradingNameYesNoController.onPageLoad(index.toString)
    case PartnerDetailsTradingNamePage(index) =>
      _ => controllers.partnerdetails.routes.PartnerDateOfJoiningController.onPageLoad(index.toString, NormalMode)
    case PartnerDetailsAddTradingNameYesNoPage(index) =>
      userAnswers => navigatePartnerAddTradingNameYesNoPage(index)(userAnswers)
    case PartnerDetailsRemoveVatRegNumberYesNoPage(index) =>
      _ => controllers.partnerdetails.routes.PartnerDetailsRemoveVatRegNumberYesNoController.onPageLoad(index.toString, NormalMode)
    case PartnerDetailsBusinessTypePage(index) =>
      userAnswers => navigatePartnerDetailsBusinessTypePage(index)(userAnswers)
    case PartnerDetailsUtrPage(index) =>
      userAnswers => navigatePartnerDetailsUTRPage(index)(userAnswers)
    case PartnerDetailsCrnPage(index) =>
      _ => controllers.partnerdetails.routes.PartnerDetailsAddUTRController.onPageLoad(index.toString, NormalMode)
    case PartnerDetailsForeignCorporateReferencePage(index) =>
      userAnswers => controllers.partnerdetails.routes.PartnerDetailsForeignCorporateReferenceController.onPageLoad(index.toString, NormalMode)
    case PartnerDetailsCorrespondenceEmailAddressPage(index) =>
      userAnswers => controllers.partnerdetails.routes.PartnerDetailsEmailAddressController.onPageLoad(index.toString, NormalMode)
    case PartnerDetailsCountryOfIncorporationPage(index) =>
      userAnswers => navigatePartnerDetailsCountryOfIncorporationPage(index)(userAnswers) // change it
    case PartnerDetailsRemovePartnerYesNoPage(index) =>
      userAnswers => navigatePartnerDetailsRemovePartnerPage(index)(userAnswers)
    case PartnerDetailsRemoveAdditionalInfoForPartnerAddressYesNoPage(index) =>
      userAnswers => navigateRemoveAdditionalInfoForPartnerAddressYesNoPage(index)(userAnswers)
    case PartnerDetailsIsBusinessIncorporatedUkPage(index) =>
      userAnswers => navigatePartnerDetailsIsBusinessIncorporatedUkPage(index, userAnswers)
    case PartnerDetailsDateOfIncorporationPage(index) =>
      userAnswers => navigatePartnerDetailsDateOfIncorporationPage(index, userAnswers)
    case PartnerDetailsDateOfJoiningPage(index) =>
      userAnswers => navigatePartnerDetailsDateOfJoiningPage(index, userAnswers)
  }

  /** The partner type comes from the index, so both modes share the same routes. */
  val partnerDetailsNormalRoutes: PartialFunction[Page, UserAnswers => Call] = partnerDetailsRoutes
  val partnerDetailsCheckRoutes: PartialFunction[Page, UserAnswers => Call] = partnerDetailsRoutes

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
          controllers.partnerdetails.routes.PartnerDetailsAddUTRController.onPageLoad(index.toString, NormalMode)
        case true =>
          controllers.partnerdetails.routes.PartnerDetailsAddNationalInsuranceNumberController.onPageLoad(index.toString, NormalMode)
      }
      .getOrElse(routes.SystemErrorController.onPageLoad())

  private def navigatePartnerAddTradingNameYesNoPage(index: BusinessNumberOrIndex)(answers: UserAnswers): Call =
    answers
      .get(PartnerDetailsAddTradingNameYesNoPage(index))
      .map {
        case false =>
          controllers.partnerdetails.routes.PartnerDateOfJoiningController.onPageLoad(index.toString, NormalMode)
        case true =>
          controllers.partnerdetails.routes.PartnerDetailsTradingNameController.onPageLoad(index.toString, NormalMode)
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
        controllers.partnerdetails.routes.PartnerDetailsForeignCorporateReferenceController.onPageLoad(index.toString, NormalMode)
      )

  private def navigatePartnerDetailsRemovePartnerPage(index: BusinessNumberOrIndex)(answers: UserAnswers): Call =
    answers
      .get(PartnerDetailsRemovePartnerYesNoPage(index))
      .fold(routes.SystemErrorController.onPageLoad()) { wantToRemove =>
        if (wantToRemove) {
          index match {
            case businessNumber: String =>
              controllers.partnerdetails.routes.PartnerDetailsController.onPageLoad(None)
            case newPartnerIndex: Int =>
              controllers.partnerdetails.routes.PartnerDetailsController.onPageLoad(None)
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
        case true =>
          controllers.partnerdetails.routes.PartnerDetailsVatRegistrationNumberYesNoController.onPageLoad(index.toString)
      }
      .getOrElse(routes.SystemErrorController.onPageLoad())
  }

  private def navigatePartnerDetailsIsBusinessIncorporatedUkPage(index: BusinessNumberOrIndex, userAnswers: UserAnswers): Call = {
    userAnswers
      .get(PartnerDetailsIsBusinessIncorporatedUkPage(index))
      .map {
        case true =>
          controllers.partnerdetails.routes.PartnerDetailsDateOfIncorporationController.onPageLoad(index.toString, NormalMode)
        case false =>
          controllers.partnerdetails.routes.PartnerDetailsAddCountryOfIncorporationController.onPageLoad(index.toString, NormalMode)
      }
      .getOrElse(routes.SystemErrorController.onPageLoad())
  }

  private def navigatePartnerDetailsDateOfJoiningPage(index: BusinessNumberOrIndex, userAnswers: UserAnswers): Call = {
    userAnswers
      .get(PartnerDetailsBusinessTypePage(index))
      .map {
        case BusinessType.Soleproprietor =>
          controllers.partnerdetails.routes.PartnerDetailsAddNationalInsuranceNumberController.onPageLoad(index.toString, NormalMode)
        case BusinessType.Corporatebody =>
          controllers.partnerdetails.routes.PartnerDetailsIsBusinessIncorporatedUkController.onPageLoad(index.toString, NormalMode)
        case BusinessType.Unincorporatedbody =>
          controllers.partnerdetails.routes.PartnerDetailsAddUTRController.onPageLoad(index.toString, NormalMode)
        case BusinessType.Partnership =>
          controllers.partnerdetails.routes.PartnerDetailsVatRegistrationNumberYesNoController.onPageLoad(index.toString)
        case BusinessType.LimitedLiabilityPartnership =>
          controllers.partnerdetails.routes.PartnerDetailsDateOfIncorporationController.onPageLoad(index.toString, NormalMode)
      }
      .getOrElse(routes.SystemErrorController.onPageLoad())
  }

  private def navigatePartnerDetailsDateOfIncorporationPage(index: BusinessNumberOrIndex, userAnswers: UserAnswers): Call = {
    lazy val incorporatedInUk = userAnswers.get(PartnerDetailsIsBusinessIncorporatedUkPage(index)).contains(true)
    val dateOfIncorporation = controllers.partnerdetails.routes.PartnerDetailsDateOfIncorporationController.onPageLoad(index.toString, NormalMode)

    userAnswers.get(PartnerDetailsBusinessTypePage(index)) match {
      case Some(BusinessType.Corporatebody) if incorporatedInUk => dateOfIncorporation
      case Some(BusinessType.LimitedLiabilityPartnership)       => dateOfIncorporation
      case _                                                    => routes.SystemErrorController.onPageLoad()
    }
  }
}
