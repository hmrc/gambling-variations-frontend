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

package viewmodels.checkAnswers.partnerdetails

import controllers.partnerdetails.*
import models.BusinessType.*
import models.{Address, BusinessType, UserAnswers}
import pages.partnerdetails.*
import pages.{BusinessNumberOrIndex, QuestionPage}
import play.api.libs.json.Reads
import play.api.mvc.Call
import queries.Settable
import utils.PartnerUtils

import scala.util.Try

object PartnerMandatoryDetails {

  /** A mandatory answer: the page it's stored on, where to send the user, and how to tell it's answered. */
  final case class Field(page: Settable[?], call: Call, isAnswered: UserAnswers => Boolean)

  object Field {
    def of[A: Reads](page: QuestionPage[A], call: Call): Field =
      Field(page, call, ua => ua.get(page).isDefined)
  }

  // --- Public API ---

  /** Mandatory fields for the partner's current answers, in journey order. */
  def required(userAnswers: UserAnswers, index: BusinessNumberOrIndex): Seq[Field] = {
    val mode = PartnerUtils.modeFor(index)

    val businessTypeField =
      Field.of(PartnerDetailsBusinessTypePage(index), routes.PartnerDetailsBusinessTypeController.onPageLoad(urlIndex(index), mode))

    val businessFields =
      userAnswers.get(PartnerDetailsBusinessTypePage(index)).fold(Seq.empty[Field])(businessFieldsFor(userAnswers, index, _))

    (businessTypeField +: businessFields) ++ commonFields(index)
  }

  /** The first unanswered mandatory field, used to redirect on continue. */
  def firstMissing(userAnswers: UserAnswers, index: BusinessNumberOrIndex): Option[Field] =
    required(userAnswers, index).find(field => !field.isAnswered(userAnswers))

  def isMissing(userAnswers: UserAnswers, index: BusinessNumberOrIndex): Boolean =
    firstMissing(userAnswers, index).isDefined

  /** Removes answers to conditional pages that don't apply to the partner's current business type / incorporation. */
  def cleanUp(userAnswers: UserAnswers, index: BusinessNumberOrIndex): Try[UserAnswers] = {
    val keep = required(userAnswers, index).map(_.page).toSet

    conditionalPages(index)
      .filterNot(keep.contains)
      .foldLeft(Try(userAnswers))((acc, page) => acc.flatMap(_.remove(page)))
  }

  // --- Rules ---

  /** Pages are keyed by the typed index; routes only take the string form. */
  private def urlIndex(index: BusinessNumberOrIndex): String = index.toString

  private def businessFieldsFor(userAnswers: UserAnswers, index: BusinessNumberOrIndex, bt: BusinessType): Seq[Field] = {
    val key = urlIndex(index)
    val mode = PartnerUtils.modeFor(index)

    val businessName = Field.of(
      PartnerDetailsBusinessNamePage(index),
      routes.PartnerDetailsChangeBusinessNameController.onPageLoad(key, businessType = bt, mode)
    )
    val utr = Field.of(PartnerDetailsUtrPage(index), routes.PartnerDetailsAddUTRController.onPageLoad(key, mode))

    val ukIncorporation = Seq(
      Field.of(PartnerDetailsDateOfIncorporation(index), routes.PartnerDetailsDateOfIncorporationController.onPageLoad(key, mode)),
      Field.of(PartnerDetailsCrnPage(index), routes.PartnerDetailsForeignCorporateReferenceController.onPageLoad(key, mode)) // TODO: CRN page
    )

    val nonUkIncorporation = Seq(
      Field.of(
        PartnerDetailsCountryOfIncorporationPage(index),
        routes.PartnerDetailsAddCountryOfIncorporationController.onPageLoad(key, mode)
      ),
      Field.of(
        PartnerDetailsForeignCorporateReferencePage(index),
        routes.PartnerDetailsForeignCorporateReferenceController.onPageLoad(key, mode)
      )
    )

    bt match {
      case Soleproprietor =>
        Seq(
          Field.of(
            PartnerDetailsSoleProprietorPage(index),
            routes.PartnerDetailsChangeBusinessNameController.onPageLoad(key, businessType = Soleproprietor, mode)
          ),
          Field.of(PartnerDetailsDateOfBirthPage(index), routes.PartnerDetailsSoleProprietorDobController.onPageLoad(key, mode)),
          utr
        )

      case Corporatebody =>
        val isIncorporatedInUk =
          Field.of(
            PartnerDetailsIsBusinessIncorporatedUkPage(index),
            routes.PartnerDetailsIsBusinessIncorporatedUkController.onPageLoad(key, mode)
          )

        val incorporation = userAnswers.get(PartnerDetailsIsBusinessIncorporatedUkPage(index)) match {
          case Some(true)  => ukIncorporation
          case Some(false) => nonUkIncorporation
          case None        => Nil
        }

        Seq(businessName, utr, isIncorporatedInUk) ++ incorporation

      case LimitedLiabilityPartnership => Seq(businessName, utr) ++ ukIncorporation
      case Unincorporatedbody          => Seq(businessName, utr)
      case Partnership                 => Seq(businessName)
    }
  }

  private def commonFields(index: BusinessNumberOrIndex): Seq[Field] = {
    val key = urlIndex(index)
    val mode = PartnerUtils.modeFor(index)

    Seq(
      Field.of(
        PartnerDetailsDateOfJoiningPage(index),
        routes.PartnerDateOfJoiningController.onPageLoad(key, mode)
      ),
      Field(
        PartnerDetailsNewCorrespondenceDetailsSectionPage(index),
        routes.PartnerDetailsBusinessTypeController.onPageLoad(key, mode), // TODO: no address page controller yet
        ua => correspondenceAddress(ua, index).isDefined
      ),
      Field.of(PartnerDetailsContactNumberPage(index), routes.PartnerDetailsContactDetailsController.onPageLoad(key, mode))
    )
  }

  /** Every page that only applies to some business types / incorporation answers. */
  private def conditionalPages(index: BusinessNumberOrIndex): Seq[Settable[?]] = Seq(
    PartnerDetailsSoleProprietorPage(index),
    PartnerDetailsDateOfBirthPage(index),
    PartnerDetailsBusinessNamePage(index),
    PartnerDetailsUtrPage(index),
    PartnerDetailsIsBusinessIncorporatedUkPage(index),
    PartnerDetailsDateOfIncorporation(index),
    PartnerDetailsCrnPage(index),
    PartnerDetailsCountryOfIncorporationPage(index),
    PartnerDetailsForeignCorporateReferencePage(index)
  )

  private def correspondenceAddress(userAnswers: UserAnswers, index: BusinessNumberOrIndex): Option[Address] =
    userAnswers.get(PartnerDetailsNewCorrespondenceDetailsSectionPage(index)).flatMap(_.correspondenceAddress)
}
