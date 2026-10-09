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
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{JsObject, JsPath, Json}

class PartnerDetailsCrnPageSpec extends PlaySpec with PartnerDetailsHelper {

  "partners" must {
    "PartnerDetailsCrnPage" must {

      "have the correct path" in {
        PartnerDetailsCrnPage(businessNumber1).path mustEqual (JsPath \ "partners" \ businessNumber1 \ "partnerDetailsCrn")
      }

      "have the correct toString value" in {

        PartnerDetailsCrnPage(businessNumber1).toString mustEqual "partnerDetailsCrn"
      }

      "read PartnerDetailsCrnPage values for the correct business number" in {
        val json = Json.obj(
          "partners" -> Json.obj(
            businessNumber1 -> Json.obj("partnerDetailsCrn" -> "SC123456"),
            businessNumber2 -> Json.obj("partnerDetailsCrn" -> "OC303030")
          )
        )

        PartnerDetailsCrnPage(businessNumber1).path.asSingleJson(json).validate[String].get mustEqual
          "SC123456"
        PartnerDetailsCrnPage(businessNumber2).path.asSingleJson(json).validate[String].get mustEqual
          "OC303030"
      }

      "write PartnerDetailsCrnPage values under the business number" in {
        val answers = UserAnswers("id").set(PartnerDetailsCrnPage(businessNumber1), "SC123456").success.value

        (answers.data \ "partners" \ businessNumber1 \ "partnerDetailsCrn").as[String] mustEqual "SC123456"
        answers.get(PartnerDetailsCrnPage(businessNumber1)) mustBe Some("SC123456")
      }

      "write the CRN without changing the partner's other answers" in {
        val before = UserAnswers("id", cleanedDataExistingPartners())
        val after = before.set(PartnerDetailsCrnPage(businessNumber1), "SC123456").success.value

        after.get(PartnerDetailsCrnPage(businessNumber1)) mustBe Some("SC123456")
        (after.data \ "partners" \ businessNumber1).as[JsObject] - "partnerDetailsCrn" mustEqual
          (before.data \ "partners" \ businessNumber1).as[JsObject] - "partnerDetailsCrn"
      }

    }
  }

  "newPartners" must {
    "PartnerDetailsCrnPage" must {

      "have the correct path" in {
        PartnerDetailsCrnPage(newPartnersIndex1).path mustEqual (JsPath \ "newPartners" \ newPartnersIndex1 \ "partnerDetailsCrn")
      }

      "have the correct toString value" in {

        PartnerDetailsCrnPage(newPartnersIndex1).toString mustEqual "partnerDetailsCrn"
      }

      "read PartnerDetailsCrnPage values at the correct index" in {
        val json = Json.obj(
          "newPartners" -> Json.arr(
            Json.obj("partnerDetailsCrn" -> "SC123456"),
            Json.obj("partnerDetailsCrn" -> "OC303030")
          )
        )

        PartnerDetailsCrnPage(newPartnersIndex1).path.asSingleJson(json).validate[String].get mustEqual
          "SC123456"
        PartnerDetailsCrnPage(newPartnersIndex2).path.asSingleJson(json).validate[String].get mustEqual
          "OC303030"
      }

      "write PartnerDetailsCrnPage values at the index" in {
        val answers = UserAnswers("id").set(PartnerDetailsCrnPage(newPartnersIndex1), "SC123456").success.value

        (answers.data \ "newPartners" \ newPartnersIndex1 \ "partnerDetailsCrn").as[String] mustEqual
          "SC123456"
        answers.get(PartnerDetailsCrnPage(newPartnersIndex1)) mustBe Some("SC123456")
      }

      "write the CRN for the second new partner without changing the first" in {
        val before = UserAnswers("id", cleanedDataNewPartners())
        val after = before.set(PartnerDetailsCrnPage(newPartnersIndex2), "OC303030").success.value

        after.get(PartnerDetailsCrnPage(newPartnersIndex2)) mustBe Some("OC303030")
        (after.data \ "newPartners" \ newPartnersIndex1) mustEqual (before.data \ "newPartners" \
          newPartnersIndex1)
      }

      "fail to write at an index past the end of the newPartners array" in {
        UserAnswers("id").set(PartnerDetailsCrnPage(newPartnersIndex2), "OC303030").isFailure mustBe true
      }
    }
  }

}
