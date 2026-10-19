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

import base.SpecBase
import controllers.partnerdetails.PartnerDetailsHelper
import controllers.partnerdetails.routes as partnerRoutes
import controllers.routes
import models.*
import models.BusinessType.Corporatebody
import pages.partnerdetails.*
import pages.{BusinessNumberOrIndex, Page, QuestionPage}
import play.api.libs.json.Json
import play.api.mvc.Call
import utils.PartnerUtils
import viewmodels.checkAnswers.partnerdetails.PartnerMandatoryDetails

class PartnerDetailsNavigatorSpec extends SpecBase with PartnerDetailsHelper {

  private val navigator = new Navigator
  private val emptyAnswers = UserAnswers("id")

  private val existing: BusinessNumberOrIndex = businessNumber1
  private val newPartner: BusinessNumberOrIndex = newPartnersIndex1
  private val bothTypes = Seq("existing partner" -> existing, "new partner" -> newPartner)

  private val savedNewPartner: UserAnswers =
    emptyAnswers.set(PartnerDetailsAddPartnerCompletedPage(newPartnersIndex1), true).success.value

  private def key(index: BusinessNumberOrIndex): String = index.toString
  private def mode(index: BusinessNumberOrIndex): Mode = PartnerUtils.modeFor(index)

  private def cya(index: BusinessNumberOrIndex): Call =
    partnerRoutes.PartnerDetailsCheckYourAnswersController.onPageLoad(key(index))

  // Journey order belongs to PartnerMandatoryDetails; here we only check the navigator defers to it
  private def nextInAddJourney(answers: UserAnswers, index: BusinessNumberOrIndex): Call =
    PartnerMandatoryDetails.firstMissing(answers, index).map(_.call).getOrElse(cya(index))

  private def next(page: Page, index: BusinessNumberOrIndex, answers: UserAnswers = emptyAnswers): Call =
    navigator.nextPage(page, mode(index), answers)

  private val addYesNoPages: Seq[(String, BusinessNumberOrIndex => QuestionPage[Boolean], BusinessNumberOrIndex => Call)] = Seq(
    ("trading name", PartnerDetailsAddTradingNameYesNoPage(_), i => partnerRoutes.PartnerDetailsTradingNameController.onPageLoad(key(i), mode(i))),
    ("national insurance number",
     PartnerDetailsAddNationalInsuranceNumberYesNoPage(_),
     i => partnerRoutes.PartnerDetailsAddNationalInsuranceNumberController.onPageLoad(key(i), mode(i))
    ),
    ("VAT registration number",
     PartnerDetailsVatRegistrationNumberYesNoPage(_),
     i => partnerRoutes.PartnerDetailsVatRegistrationNumberController.onPageLoad(key(i), mode(i))
    ),
    ("additional address info",
     PartnerDetailsAdditionalAddressInfoYesNoPage(_),
     i => partnerRoutes.PartnerDetailsAdditionalAddressInfoController.onPageLoad(key(i), mode(i))
    ),
    ("fax number",
     PartnerDetailsAddFaxNumberYesNoPage(_),
     i => partnerRoutes.PartnerDetailsChangePartnerFaxNumberController.onPageLoad(key(i), mode(i))
    ),
    ("email address", PartnerDetailsAddEmailAddressYesNoPage(_), i => partnerRoutes.PartnerDetailsEmailAddressController.onPageLoad(key(i), mode(i)))
  )

  private val valuePages: Seq[(String, BusinessNumberOrIndex => Page)] = Seq(
    "business name"               -> (PartnerDetailsBusinessNamePage(_)),
    "sole proprietor"             -> (PartnerDetailsSoleProprietorPage(_)),
    "date of birth"               -> (PartnerDetailsDateOfBirthPage(_)),
    "trading name"                -> (PartnerDetailsTradingNamePage(_)),
    "NINO"                        -> (PartnerDetailsNinoPage(_)),
    "CRN"                         -> (PartnerDetailsCrnPage(_)),
    "UTR"                         -> (PartnerDetailsUtrPage(_)),
    "VRN"                         -> (PartnerDetailsVrnPage(_)),
    "country of incorporation"    -> (PartnerDetailsCountryOfIncorporationPage(_)),
    "foreign corporate reference" -> (PartnerDetailsForeignCorporateReferencePage(_)),
    "additional address info"     -> (PartnerDetailsAdditionalAddressInfoPage(_)),
    "contact number"              -> (PartnerDetailsContactNumberPage(_)),
    "email address"               -> (PartnerDetailsCorrespondenceEmailAddressPage(_))
  )

  private val removePages: Seq[(String, BusinessNumberOrIndex => Page)] = Seq(
    "trading name"            -> (PartnerDetailsRemovePartnerTradingNameYesNoPage(_)),
    "NINO"                    -> (PartnerDetailsRemoveNationalInsuranceNumberYesNoPage(_)),
    "VAT registration number" -> (PartnerDetailsRemoveVatRegNumberYesNoPage(_)),
    "email address"           -> (PartnerDetailsRemoveEmailAddressYesNoPage(_)),
    "fax number"              -> (PartnerDetailsRemoveFaxNumberYesNoPage(_)),
    "additional address info" -> (PartnerDetailsRemoveAdditionalInfoForPartnerAddressYesNoPage(_))
  )

  "Partner details navigation" - {

    "should route the same way in NormalMode and CheckMode, since the partner type comes from the index" in {
      val page = PartnerDetailsTradingNamePage(existing)

      navigator.nextPage(page, NormalMode, emptyAnswers) mustBe navigator.nextPage(page, CheckMode, emptyAnswers)
    }

    "business type" - {

      "should route to the business name page for the chosen type" in {
        val answers = emptyAnswers.copy(data = Json.obj("newPartners" -> Json.arr(Json.obj("partnerDetailsBusinessType" -> 2))))

        next(PartnerDetailsBusinessTypePage(newPartner), newPartner, answers) mustBe
          partnerRoutes.PartnerDetailsChangeBusinessNameController.onPageLoad(key(newPartner), Corporatebody, mode(newPartner))
      }

      "should route to SystemError when unanswered" in {
        next(PartnerDetailsBusinessTypePage(newPartner), newPartner) mustBe routes.SystemErrorController.onPageLoad()
      }
    }

    "is business incorporated in the UK" - {

      bothTypes.foreach { case (label, index) =>
        s"should route to date of incorporation when yes, for an $label" in {
          val answers = emptyAnswers.set(PartnerDetailsIsBusinessIncorporatedUkPage(index), true).success.value

          next(PartnerDetailsIsBusinessIncorporatedUkPage(index), index, answers) mustBe
            partnerRoutes.PartnerDetailsDateOfIncorporationController.onPageLoad(key(index), mode(index))
        }

        s"should route to country of incorporation when no, for an $label" in {
          val answers = emptyAnswers.set(PartnerDetailsIsBusinessIncorporatedUkPage(index), false).success.value

          next(PartnerDetailsIsBusinessIncorporatedUkPage(index), index, answers) mustBe
            partnerRoutes.PartnerDetailsAddCountryOfIncorporationController.onPageLoad(key(index), mode(index))
        }
      }

      "should route to SystemError when unanswered" in {
        next(PartnerDetailsIsBusinessIncorporatedUkPage(newPartner), newPartner) mustBe routes.SystemErrorController.onPageLoad()
      }
    }

    "add X yes/no questions" - {

      addYesNoPages.foreach { case (name, page, ifYes) =>
        s"add $name" - {

          bothTypes.foreach { case (label, index) =>
            s"should route to the $name page when yes, for an $label" in {
              val answers = emptyAnswers.set(page(index), true).success.value

              next(page(index), index, answers) mustBe ifYes(index)
            }
          }

          "should return to check your answers when no, for an existing partner" in {
            val answers = emptyAnswers.set(page(existing), false).success.value

            next(page(existing), existing, answers) mustBe cya(existing)
          }

          "should return to check your answers when no, for a saved new partner" in {
            val answers = savedNewPartner.set(page(newPartner), false).success.value

            next(page(newPartner), newPartner, answers) mustBe cya(newPartner)
          }

          "should carry on through the add journey when no, for an unsaved new partner" in {
            val answers = emptyAnswers.set(page(newPartner), false).success.value

            next(page(newPartner), newPartner, answers) mustBe nextInAddJourney(answers, newPartner)
          }

          "should route to SystemError when unanswered" in {
            next(page(newPartner), newPartner) mustBe routes.SystemErrorController.onPageLoad()
          }
        }
      }
    }

    "value pages" - {

      valuePages.foreach { case (name, page) =>
        s"$name" - {

          "should return to check your answers for an existing partner" in {
            next(page(existing), existing) mustBe cya(existing)
          }

          "should return to check your answers for a saved new partner" in {
            next(page(newPartner), newPartner, savedNewPartner) mustBe cya(newPartner)
          }

          "should carry on through the add journey for an unsaved new partner" in {
            next(page(newPartner), newPartner) mustBe nextInAddJourney(emptyAnswers, newPartner)
          }
        }
      }
    }

    "remove questions" - {

      removePages.foreach { case (name, page) =>
        bothTypes.foreach { case (label, index) =>
          s"remove $name should return to check your answers, for an $label" in {
            next(page(index), index) mustBe cya(index)
          }
        }
      }
    }

    "remove partner" - {

      for {
        (label, index) <- bothTypes
        answer         <- Seq(true, false)
      } s"should return to the partner list when the answer is $answer, for an $label" in {
        val answers = emptyAnswers.set(PartnerDetailsRemovePartnerYesNoPage(index), answer).success.value

        next(PartnerDetailsRemovePartnerYesNoPage(index), index, answers) mustBe
          partnerRoutes.PartnerDetailsController.onPageLoad(None)
      }

      "should route to SystemError when unanswered" in {
        next(PartnerDetailsRemovePartnerYesNoPage(newPartner), newPartner) mustBe routes.SystemErrorController.onPageLoad()
      }
    }
  }
}
