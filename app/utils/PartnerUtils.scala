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
import pages.partnerdetails.PartnerDetailsAddPartnerCompletedPage
import play.api.libs.json.{JsArray, JsObject, JsPath}

object PartnerUtils {

  def parseIndex(index: String, mode: Mode): BusinessNumberOrIndex =
    if mode == NormalMode then index.toInt
    else index

  // TODO error checking version to validate user is not fucking around with the index,
  // in fact, we wouldn't even need to pass it
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

    val newPartnersSize = (userAnswers.data \ "newPartners").validate[JsArray].map(_.value.size).getOrElse(0)

    val p = (0 to newPartnersSize).collectFirst {
      case index: Int if userAnswers.get(PartnerDetailsAddPartnerCompletedPage(index)).contains(false) =>
        index
    }

    p getOrElse newPartnersSize // TODO look into it, changed to size so it will "append" if it doesnt find anything
  }

  def getNewPartnersSize(userAnswers: UserAnswers): Int =
    (userAnswers.data \ "newPartners").validate[JsArray].map(_.value.size).getOrElse(0)

}
