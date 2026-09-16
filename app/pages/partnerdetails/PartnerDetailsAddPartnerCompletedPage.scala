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

package pages.partnerdetails

import pages.{BusinessNumberOrIndex, QuestionPage}
import play.api.libs.json.JsPath

//TODO should be indexed I think? Guessing
//TODO very important, that'd be the flag to indicate if we finished creating new partner details
// TODO changed obj to class

//TODO prolly doesn't need string key if its for new partners only

//TODO I think this might not need index?
case class PartnerDetailsAddPartnerCompletedPage(businessNumberOrIndex: BusinessNumberOrIndex) extends QuestionPage[Boolean] {

  override def path: JsPath = businessNumberOrIndex match {
    case key: String => JsPath \ "partners" \ key \ toString
    case index: Int  => JsPath \ "newPartners" \ index \ toString
  }

  override def toString: String = "partnerDetailsAddPartnerCompleted" // TODO
}
object PartnerDetailsAddPartnerCompletedPage {
  def apply(index: Int) = new PartnerDetailsAddPartnerCompletedPage(index)
  def apply(partnerDetailsBusinessNumber: String) = new PartnerDetailsAddPartnerCompletedPage(partnerDetailsBusinessNumber)
}
