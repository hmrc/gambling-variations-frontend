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

package models

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.*

class BusinessTradeClassSpec extends AnyWordSpec with Matchers {

  "BusinessTradeClass.fromCode" should {

    "return correct BusinessTradeClass for valid codes" in {
      BusinessTradeClass.fromCode(1) shouldBe Some(BusinessTradeClass.Amusementorgamingmachinesupplier)
      BusinessTradeClass.fromCode(2) shouldBe Some(BusinessTradeClass.Adultgamingcentre)
      BusinessTradeClass.fromCode(3) shouldBe Some(BusinessTradeClass.Familyentertainmentcentre)
      BusinessTradeClass.fromCode(4) shouldBe Some(BusinessTradeClass.BookmakerOrBettingActivities)
      BusinessTradeClass.fromCode(5) shouldBe Some(BusinessTradeClass.Bingopromoter)
      BusinessTradeClass.fromCode(6) shouldBe Some(BusinessTradeClass.Casino)
      BusinessTradeClass.fromCode(7) shouldBe Some(BusinessTradeClass.Publichouse)
      BusinessTradeClass.fromCode(8) shouldBe Some(BusinessTradeClass.Club)
      BusinessTradeClass.fromCode(9) shouldBe Some(BusinessTradeClass.Other)
    }

    "return None for invalid codes" in {
      BusinessTradeClass.fromCode(0)  shouldBe None
      BusinessTradeClass.fromCode(16) shouldBe None
      BusinessTradeClass.fromCode(-1) shouldBe None
    }
  }

  "BusinessTradeClass Reads" should {

    "deserialize valid codes to BusinessTradeClass" in {
      Json.fromJson[BusinessTradeClass](JsNumber(1)) shouldBe JsSuccess(BusinessTradeClass.Amusementorgamingmachinesupplier)
      Json.fromJson[BusinessTradeClass](JsNumber(2)) shouldBe JsSuccess(BusinessTradeClass.Adultgamingcentre)
      Json.fromJson[BusinessTradeClass](JsNumber(3)) shouldBe JsSuccess(BusinessTradeClass.Familyentertainmentcentre)
      Json.fromJson[BusinessTradeClass](JsNumber(4)) shouldBe JsSuccess(BusinessTradeClass.BookmakerOrBettingActivities)
      Json.fromJson[BusinessTradeClass](JsNumber(5)) shouldBe JsSuccess(BusinessTradeClass.Bingopromoter)
      Json.fromJson[BusinessTradeClass](JsNumber(6)) shouldBe JsSuccess(BusinessTradeClass.Casino)
      Json.fromJson[BusinessTradeClass](JsNumber(7)) shouldBe JsSuccess(BusinessTradeClass.Publichouse)
      Json.fromJson[BusinessTradeClass](JsNumber(8)) shouldBe JsSuccess(BusinessTradeClass.Club)
      Json.fromJson[BusinessTradeClass](JsNumber(9)) shouldBe JsSuccess(BusinessTradeClass.Other)
    }

    "fail for invalid codes" in {
      val result = Json.fromJson[BusinessTradeClass](JsNumber(99))

      result.isError shouldBe true
      result match {
        case JsError(errors) =>
          errors.head._2.head.message shouldBe "Invalid Business Trade Class"
        case _ => fail("Expected JsError")
      }
    }

    "fail for non-numeric JSON" in {
      Json.fromJson[BusinessTradeClass](JsString("abc")).isError shouldBe true
      Json.fromJson[BusinessTradeClass](JsBoolean(true)).isError shouldBe true
    }
  }

  "BusinessTradeClass Writes" should {

    "serialize BusinessTradeClass to numeric JSON code" in {
      Json.toJson[BusinessTradeClass](BusinessTradeClass.Amusementorgamingmachinesupplier) shouldBe JsNumber(1)
      Json.toJson[BusinessTradeClass](BusinessTradeClass.Adultgamingcentre)                shouldBe JsNumber(2)
      Json.toJson[BusinessTradeClass](BusinessTradeClass.Familyentertainmentcentre)        shouldBe JsNumber(3)
      Json.toJson[BusinessTradeClass](BusinessTradeClass.BookmakerOrBettingActivities)     shouldBe JsNumber(4)
      Json.toJson[BusinessTradeClass](BusinessTradeClass.Bingopromoter)                    shouldBe JsNumber(5)
      Json.toJson[BusinessTradeClass](BusinessTradeClass.Casino)                           shouldBe JsNumber(6)
      Json.toJson[BusinessTradeClass](BusinessTradeClass.Publichouse)                      shouldBe JsNumber(7)
      Json.toJson[BusinessTradeClass](BusinessTradeClass.Club)                             shouldBe JsNumber(8)
      Json.toJson[BusinessTradeClass](BusinessTradeClass.Other)                            shouldBe JsNumber(9)
    }
  }

  "BusinessTradeClass Format" should {

    "round-trip correctly (write then read)" in
      BusinessTradeClass.values.foreach { bt =>
        val json = Json.toJson[BusinessTradeClass](bt)
        val result = json.validate[BusinessTradeClass]

        result shouldBe JsSuccess(bt)
      }
  }
}
