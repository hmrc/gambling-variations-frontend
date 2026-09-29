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
import models.{Address, BusinessType, NormalMode, UserAnswers}
import pages.QuestionPage
import pages.partnerdetails.*
import play.api.libs.json.Reads
import play.api.mvc.Call
import queries.Settable

import scala.util.Try

object PartnerMandatoryFields {

  /** A mandatory answer: the page it's stored on, where to send the user, and how to tell it's answered. */
  final case class Field(page: Settable[?], call: Call, isAnswered: UserAnswers => Boolean)

  object Field {
    def of[A: Reads](page: QuestionPage[A], call: Call): Field =
      Field(page, call, ua => ua.get(page).isDefined)
  }

  // --- Public API ---

  /** Mandatory fields for the partner's current answers, in journey order. */
  def required(userAnswers: UserAnswers, index: String): Seq[Field] = {
    val businessTypeField = Field.of(PartnerDetailsBusinessTypePage(index), routes.PartnerDetailsBusinessTypeController.onPageLoad(index, NormalMode))
    val businessFields = userAnswers.get(PartnerDetailsBusinessTypePage(index)).fold(Seq.empty[Field])(businessFieldsFor(userAnswers, index, _))

    (businessTypeField +: businessFields) ++ commonFields(index)
  }

  /** The first unanswered mandatory field, used to redirect on continue. */
  private def firstMissing(userAnswers: UserAnswers, index: String): Option[Field] =
    required(userAnswers, index).find(field => !field.isAnswered(userAnswers))

  def isMissing(userAnswers: UserAnswers, index: String): Boolean =
    firstMissing(userAnswers, index).isDefined

  /** Removes answers to conditional pages that don't apply to the partner's current business type / incorporation. */
  def cleanUp(userAnswers: UserAnswers, index: String): Try[UserAnswers] = {
    val keep = required(userAnswers, index).map(_.page).toSet

    conditionalPages(index)
      .filterNot(keep.contains)
      .foldLeft(Try(userAnswers))((acc, page) => acc.flatMap(_.remove(page)))
  }

  // --- Rules ---

  private def businessFieldsFor(userAnswers: UserAnswers, index: String, bt: BusinessType): Seq[Field] = {

    val businessName = Field.of(
      PartnerDetailsBusinessNamePage(index),
      routes.PartnerDetailsChangeBusinessNameController.onPageLoad(index, businessType = bt, NormalMode)
    )
    val utr = Field.of(PartnerDetailsUtrPage(index), routes.PartnerDetailsAddUTRController.onPageLoad(index, NormalMode))

    val ukIncorporation = Seq(
      Field.of(PartnerDetailsDateOfIncorporation(index), routes.PartnerDetailsDateOfIncorporationController.onPageLoad(index, NormalMode)),
      Field.of(PartnerDetailsCrnPage(index), routes.PartnerDetailsForeignCorporateReferenceController.onPageLoad(index, NormalMode)) // TODO: CRN page
    )

    val nonUkIncorporation = Seq(
      Field.of(PartnerDetailsCountryOfIncorporation(index),
               routes.PartnerDetailsIsBusinessIncorporatedUkController.onPageLoad(index, NormalMode)
              ), // TODO: country page
      Field.of(PartnerDetailsForeignCorporateReferencePage(index),
               routes.PartnerDetailsForeignCorporateReferenceController.onPageLoad(index, NormalMode)
              )
    )

    bt match {
      case Soleproprietor =>
        Seq(
          Field.of(PartnerDetailsSoleProprietorPage(index),
                   routes.PartnerDetailsChangeBusinessNameController.onPageLoad(index, businessType = Soleproprietor, NormalMode)
                  ),
          Field.of(PartnerDetailsDateOfBirthPage(index), routes.PartnerDetailsSoleProprietorDobController.onPageLoad(index, NormalMode)),
          utr
        )

      case Corporatebody =>
        val isIncorporatedInUk =
          Field.of(PartnerDetailsIsBusinessIncorporatedUkPage(index),
                   routes.PartnerDetailsIsBusinessIncorporatedUkController.onPageLoad(index, NormalMode)
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

  private def commonFields(index: String): Seq[Field] = Seq(
    Field.of(PartnerDetailsDateOfJoiningPage(index),
             routes.PartnerDetailsSoleProprietorDobController.onPageLoad(index, NormalMode)
            ), // TODO: date-of-joining page
    Field(
      PartnerDetailsNewCorrespondenceDetailsSectionPage(index),
      routes.PartnerDetailsBusinessTypeController.onPageLoad(index, NormalMode), // TODO: address page
      ua => correspondenceAddress(ua, index).isDefined
    ),
    Field.of(PartnerDetailsContactNumberPage(index), routes.PartnerDetailsContactDetailsController.onPageLoad(index, NormalMode))
  )

  /** Every page that only applies to some business types / incorporation answers. */
  private def conditionalPages(index: String): Seq[Settable[_]] = Seq(
    PartnerDetailsSoleProprietorPage(index),
    PartnerDetailsDateOfBirthPage(index),
    PartnerDetailsBusinessNamePage(index),
    PartnerDetailsUtrPage(index),
    PartnerDetailsIsBusinessIncorporatedUkPage(index),
    PartnerDetailsDateOfIncorporation(index),
    PartnerDetailsCrnPage(index),
    PartnerDetailsCountryOfIncorporation(index),
    PartnerDetailsForeignCorporateReferencePage(index)
  )

  private def correspondenceAddress(userAnswers: UserAnswers, index: String): Option[Address] =
    userAnswers.get(PartnerDetailsNewCorrespondenceDetailsSectionPage(index)).flatMap(_.correspondenceAddress)
}
