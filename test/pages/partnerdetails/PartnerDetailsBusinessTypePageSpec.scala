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
import models.{BusinessType, SoleProprietorName, UserAnswers}
import org.scalatestplus.play.PlaySpec
import pages.BusinessNumberOrIndex
import play.api.libs.json.{JsPath, Json}

import java.time.LocalDate

class PartnerDetailsBusinessTypePageSpec extends PlaySpec with PartnerDetailsHelper {

  private val soleProprietorName = SoleProprietorName("Mr", "John", None, "Doe")
  private val dateOfBirth = LocalDate.of(1980, 1, 31)

  private def soleProprietorAnswers(index: BusinessNumberOrIndex): UserAnswers =
    UserAnswers("id")
      .set(PartnerDetailsBusinessTypePage(index), BusinessType.Soleproprietor)
      .flatMap(_.set(PartnerDetailsSoleProprietorPage(index), soleProprietorName))
      .flatMap(_.set(PartnerDetailsDateOfBirthPage(index), dateOfBirth))
      .flatMap(_.set(PartnerDetailsTradingNamePage(index), "Homer Simpson Inc"))
      .get

  private def cleanupBehaviour(index: BusinessNumberOrIndex, otherIndex: BusinessNumberOrIndex): Unit = {

    "remove the sole proprietor's name and date of birth when the business type changes from sole proprietor" in {
      Seq(
        BusinessType.Corporatebody,
        BusinessType.Unincorporatedbody,
        BusinessType.Partnership,
        BusinessType.LimitedLiabilityPartnership
      ).foreach { businessType =>
        val result = soleProprietorAnswers(index).set(PartnerDetailsBusinessTypePage(index), businessType).get

        result.get(PartnerDetailsBusinessTypePage(index)) mustBe Some(businessType)
        result.get(PartnerDetailsSoleProprietorPage(index)) mustBe None
        result.get(PartnerDetailsDateOfBirthPage(index)) mustBe None
      }
    }

    "keep the sole proprietor's name and date of birth when sole proprietor is selected again" in {
      val result = soleProprietorAnswers(index).set(PartnerDetailsBusinessTypePage(index), BusinessType.Soleproprietor).get

      result.get(PartnerDetailsSoleProprietorPage(index)) mustBe Some(soleProprietorName)
      result.get(PartnerDetailsDateOfBirthPage(index)) mustBe Some(dateOfBirth)
    }

    "keep the partner's other answers when the business type changes" in {
      val result = soleProprietorAnswers(index).set(PartnerDetailsBusinessTypePage(index), BusinessType.Corporatebody).get

      result.get(PartnerDetailsTradingNamePage(index)) mustBe Some("Homer Simpson Inc")
    }

    "leave another partner's sole proprietor answers untouched" in {
      val answers =
        soleProprietorAnswers(index)
          .set(PartnerDetailsBusinessTypePage(otherIndex), BusinessType.Soleproprietor)
          .flatMap(_.set(PartnerDetailsSoleProprietorPage(otherIndex), soleProprietorName))
          .flatMap(_.set(PartnerDetailsDateOfBirthPage(otherIndex), dateOfBirth))
          .get

      val result = answers.set(PartnerDetailsBusinessTypePage(index), BusinessType.Corporatebody).get

      result.get(PartnerDetailsSoleProprietorPage(index)) mustBe None
      result.get(PartnerDetailsSoleProprietorPage(otherIndex)) mustBe Some(soleProprietorName)
      result.get(PartnerDetailsDateOfBirthPage(otherIndex)) mustBe Some(dateOfBirth)
    }

    "succeed when there are no sole proprietor answers to remove" in {
      val result = UserAnswers("id").set(PartnerDetailsBusinessTypePage(index), BusinessType.Corporatebody).get

      result.get(PartnerDetailsBusinessTypePage(index)) mustBe Some(BusinessType.Corporatebody)
    }
  }
  "partners" must {
    "PartnerDetailsBusinessTypePage" must {

      behave like cleanupBehaviour(businessNumber1, businessNumber2)

      "have the correct path" in {
        PartnerDetailsBusinessTypePage(businessNumber1).path mustEqual (JsPath \ "partners" \ businessNumber1 \ "partnerDetailsBusinessType")
      }

      "have the correct toString value" in {
        PartnerDetailsBusinessTypePage(businessNumber1).toString mustEqual "partnerDetailsBusinessType"
      }

      "be able to read and write PartnerDetailsBusinessTypePage values with correct index" in {

        val value1 = BusinessType.Soleproprietor
        val value2 = BusinessType.Unincorporatedbody

        val json = Json.obj(
          "partners" -> Json.obj(
            businessNumber1 -> Json.obj(
              PartnerDetailsBusinessTypePage(businessNumber1).toString -> Json.toJson(value1.code)
            ),
            businessNumber2 -> Json.obj(
              PartnerDetailsBusinessTypePage(businessNumber2).toString -> Json.toJson(value2.code)
            )
          )
        )

        PartnerDetailsBusinessTypePage(businessNumber1).path
          .asSingleJson(json)
          .validate[BusinessType]
          .get mustEqual value1

        PartnerDetailsBusinessTypePage(businessNumber2).path
          .asSingleJson(json)
          .validate[BusinessType]
          .get mustEqual value2
      }

    }
  }

  "newPartners" must {
    "PartnerDetailsBusinessTypePage" must {

      behave like cleanupBehaviour(newPartnersIndex1, newPartnersIndex2)

      "have the correct path" in {
        PartnerDetailsBusinessTypePage(newPartnersIndex1).path mustEqual (JsPath \ "newPartners" \ newPartnersIndex1 \ "partnerDetailsBusinessType")
      }

      "have the correct toString value" in {
        PartnerDetailsBusinessTypePage(newPartnersIndex1).toString mustEqual "partnerDetailsBusinessType"
      }

      "be able to read and write PartnerDetailsBusinessTypePage values with correct index" in {

        val value1 = BusinessType.Soleproprietor
        val value2 = BusinessType.Unincorporatedbody

        val json = Json.obj(
          "newPartners" -> Json.arr(
            Json.obj(
              PartnerDetailsBusinessTypePage(newPartnersIndex1).toString -> Json.toJson(value1.code)
            ),
            Json.obj(
              PartnerDetailsBusinessTypePage(newPartnersIndex2).toString -> Json.toJson(value2.code)
            )
          )
        )

        PartnerDetailsBusinessTypePage(newPartnersIndex1).path
          .asSingleJson(json)
          .validate[BusinessType]
          .get mustEqual value1

        PartnerDetailsBusinessTypePage(newPartnersIndex2).path
          .asSingleJson(json)
          .validate[BusinessType]
          .get mustEqual value2
      }

    }
  }

}
