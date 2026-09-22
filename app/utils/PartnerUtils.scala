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

package utils

import models.{Mode, NormalMode, UserAnswers}
import pages.BusinessNumberOrIndex
import pages.partnerdetails.{PartnerDetailsAddPartnerCompletedPage, PartnerDetailsBusinessPartnerNumberPage}
import play.api.libs.json.JsArray

object PartnerUtils {

  def parseIndex(index: String, mode: Mode): BusinessNumberOrIndex =
    if mode == NormalMode then index.toInt
    else index

  def validateNormalMode(index: String, mode: Mode): Either[Exception, Int] =
    if mode == NormalMode then Right(index.toInt)
    else Left(RuntimeException("TODO, this route only allows NormalMode"))

  // Like the top one, but actually validate some things
  def parseIndexOpt(index: String, mode: Mode, userAnswers: UserAnswers): Option[BusinessNumberOrIndex] = {
    if mode == NormalMode then {
      val newPartnersSize = getNewPartnersSize(userAnswers)
      if (newPartnersSize > index.toInt) || (newPartnersSize == 0 && index.toInt == 0) then Some(index.toInt)
      else None
    } else {
      userAnswers.get(PartnerDetailsBusinessPartnerNumberPage(index)).fold(None)(_ => Some(index))
    }
  }

  def parseIndex(index: String, mode: Mode, userAnswers: UserAnswers): BusinessNumberOrIndex =
    if mode == NormalMode then {
      val isCorrect = userAnswers.get(PartnerDetailsAddPartnerCompletedPage(index.toInt)).contains(false)
      if isCorrect then index.toInt
      else findIndexForNewPartner(userAnswers)
    } else index

  def isIndexCorrect(index: String, mode: Mode, userAnswers: UserAnswers): Boolean =
    if mode != NormalMode then false
    else userAnswers.get(PartnerDetailsAddPartnerCompletedPage(index.toInt)).contains(false)

  // TODO find index where its not complete
  def findIndexForNewPartner(userAnswers: UserAnswers): Int = {
    val newPartnersSize = getNewPartnersSize(userAnswers)
    val newPartnerExistingIndex = (0 to newPartnersSize).collectFirst {
      case index: Int if userAnswers.get(PartnerDetailsAddPartnerCompletedPage(index)).contains(false) =>
        index
    }
    newPartnerExistingIndex getOrElse newPartnersSize // TODO look into it, changed to size so it will "append" if it doesnt find anything
  }

  private def getNewPartnersSize(userAnswers: UserAnswers): Int =
    (userAnswers.data \ "newPartners").validate[JsArray].map(_.value.size).getOrElse(0)

}
