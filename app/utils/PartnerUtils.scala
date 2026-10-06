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

import models.{BusinessType, Mode, NormalMode, UserAnswers}
import pages.BusinessNumberOrIndex
import pages.partnerdetails.{PartnerDetailsAddPartnerCompletedPage, PartnerDetailsBusinessNamePage, PartnerDetailsBusinessTypePage, PartnerDetailsMgdRegNumberPage, PartnerDetailsSoleProprietorPage, PartnerDetailsTradingNamePage}
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

  def getCompletedNewPartners(userAnswers: UserAnswers): Seq[Int] = (0 to getNewPartnersSize(userAnswers))
    .map(index => userAnswers.get(PartnerDetailsAddPartnerCompletedPage(index)))
    .zipWithIndex
    .collect { case (Some(true), i) =>
      i
    }

  // TODO name etc
  // TODO need test for that
  def getName(businessNumberOrIndex: BusinessNumberOrIndex, userAnswers: UserAnswers): Option[String] = for {
    businessType <- userAnswers.get(PartnerDetailsBusinessTypePage(businessNumberOrIndex))
    name <- businessType match {
              case BusinessType.Soleproprietor =>
                userAnswers
                  .get(PartnerDetailsSoleProprietorPage(businessNumberOrIndex))
                  .map(_.fullName)
                  .orElse(userAnswers.get(PartnerDetailsMgdRegNumberPage(businessNumberOrIndex)))
              case _ =>
                userAnswers
                  .get(PartnerDetailsBusinessNamePage(businessNumberOrIndex))
                  .orElse(userAnswers.get(PartnerDetailsTradingNamePage(businessNumberOrIndex)))
                  .orElse(userAnswers.get(PartnerDetailsMgdRegNumberPage(businessNumberOrIndex)))
            }
  } yield name

  private def getNewPartnersSize(userAnswers: UserAnswers): Int =
    (userAnswers.data \ "newPartners").validate[JsArray].map(_.value.size).getOrElse(0)

}
