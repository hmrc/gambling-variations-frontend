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

import controllers.partnerdetails.PartnerDetailsHelper
import models.UserAnswers
import org.scalatestplus.play.PlaySpec
import pages.BusinessNumberOrIndex
import play.api.libs.json.JsPath

class PartnerDetailsAddTradingNameYesNoPageSpec extends PlaySpec with PartnerDetailsHelper {

  private def answersWithTradingName(index: BusinessNumberOrIndex): UserAnswers =
    UserAnswers("id")
      .set(PartnerDetailsAddTradingNameYesNoPage(index), true)
      .flatMap(_.set(PartnerDetailsTradingNamePage(index), "Homer Simpson Inc"))
      .get

  private def cleanupBehaviour(index: BusinessNumberOrIndex, otherIndex: BusinessNumberOrIndex): Unit = {

    "remove the trading name when the answer changes to No" in {
      val result = answersWithTradingName(index).set(PartnerDetailsAddTradingNameYesNoPage(index), false).get

      result.get(PartnerDetailsAddTradingNameYesNoPage(index)) mustBe Some(false)
      result.get(PartnerDetailsTradingNamePage(index)) mustBe None
    }

    "keep the trading name when the answer is Yes" in {
      val result = answersWithTradingName(index).set(PartnerDetailsAddTradingNameYesNoPage(index), true).get

      result.get(PartnerDetailsTradingNamePage(index)) mustBe Some("Homer Simpson Inc")
    }

    "leave another partner's trading name untouched" in {
      val answers = answersWithTradingName(index).set(PartnerDetailsTradingNamePage(otherIndex), "Other Inc").get

      val result = answers.set(PartnerDetailsAddTradingNameYesNoPage(index), false).get

      result.get(PartnerDetailsTradingNamePage(index)) mustBe None
      result.get(PartnerDetailsTradingNamePage(otherIndex)) mustBe Some("Other Inc")
    }

    "succeed when there is no trading name to remove" in {
      val result = UserAnswers("id").set(PartnerDetailsAddTradingNameYesNoPage(index), false).get

      result.get(PartnerDetailsAddTradingNameYesNoPage(index)) mustBe Some(false)
    }
  }
  "PartnerDetailsAddTradingNameYesNoPage" must {

    "partners" must {

      behave like cleanupBehaviour(businessNumber1, businessNumber2)

      "have the correct path" in {
        PartnerDetailsAddTradingNameYesNoPage(businessNumber1).path mustEqual
          (JsPath \ "partners" \ businessNumber1 \ "partnerDetailsAddTradingNameYesNo")
      }

      "have the correct string representation" in {
        PartnerDetailsAddTradingNameYesNoPage(businessNumber1).toString mustEqual "partnerDetailsAddTradingNameYesNo"
      }
    }

    "newPartners" must {

      behave like cleanupBehaviour(newPartnersIndex1, newPartnersIndex2)

      "have the correct path" in {
        PartnerDetailsAddTradingNameYesNoPage(newPartnersIndex1).path mustEqual
          (JsPath \ "newPartners" \ newPartnersIndex1 \ "partnerDetailsAddTradingNameYesNo")
      }

      "have the correct string representation" in {
        PartnerDetailsAddTradingNameYesNoPage(newPartnersIndex1).toString mustEqual "partnerDetailsAddTradingNameYesNo"
      }
    }
  }
}
