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
import play.api.libs.json.{JsArray, JsObject}

object PartnerUtils {

  def parseIndex(index: String, mode: Mode): BusinessNumberOrIndex =
    if mode == NormalMode then index.toInt
    else index

  def getExistingPartnersBusinessNumbers(userAnswers: UserAnswers): Seq[String] = (userAnswers.data \ "partners")
    .asOpt[JsObject]
    .fold(Seq.empty[String])(_.fields.map(_._1))
    // Note: compiler says its redundant, but you cannot remove it without compiler mixing up scala.collection.immutable.Seq and scala.collection.Seq
    .toSeq

  def findIndexForNewPartner(userAnswers: UserAnswers): Int = {
    val newPartnersSize = getNewPartnersSize(userAnswers)
    val newPartnerExistingIndex = (0 to newPartnersSize).collectFirst {
      case index: Int if userAnswers.get(PartnerDetailsAddPartnerCompletedPage(index)).contains(false) =>
        index
    }
    newPartnerExistingIndex getOrElse 0
  }

  private def getNewPartnersSize(userAnswers: UserAnswers): Int =
    (userAnswers.data \ "newPartners").validate[JsArray].map(_.value.size).getOrElse(0)

}
