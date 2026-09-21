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
import models.ContactNumber
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{JsPath, Json}

class PartnerDetailsContactNumberPageSpec extends PlaySpec with PartnerDetailsHelper {

  "partners" must {
    "PartnerDetailsContactNumberPage" must {

      "have the correct path" in {
        PartnerDetailsContactNumberPage(
          businessNumber1
        ).path mustEqual (JsPath \ "partners" \ businessNumber1 \ "partnerDetailsCorrespondenceDetailsSection" \ "contactNumber")
      }

      "have the correct toString value" in {

        PartnerDetailsContactNumberPage(businessNumber1).toString mustEqual "contactNumber"
      }

      "be able to read and write PartnerDetailsContactNumberPage values with correct index" in {

        val value1 = ContactNumber(phoneNumber = Some("111"), mobilePhoneNumber = Some("111"))
        val value2 = ContactNumber(phoneNumber = Some("222"), mobilePhoneNumber = Some("222"))

        val json = Json.obj(
          "partners" -> Json.obj(
            businessNumber1 ->
              Json.obj(
                PartnerDetailsCorrespondenceDetailsSectionPage(businessNumber1).toString -> Json.obj(
                  PartnerDetailsContactNumberPage(businessNumber1).toString -> Json.toJson(value1)
                )
              ),
            businessNumber2 ->
              Json.obj(
                PartnerDetailsCorrespondenceDetailsSectionPage(businessNumber2).toString -> Json.obj(
                  PartnerDetailsContactNumberPage(businessNumber2).toString -> Json.toJson(value2)
                )
              )
          )
        )

        PartnerDetailsContactNumberPage(businessNumber1).path
          .asSingleJson(json)
          .validate[ContactNumber]
          .get mustEqual value1

        PartnerDetailsContactNumberPage(businessNumber2).path
          .asSingleJson(json)
          .validate[ContactNumber]
          .get mustEqual value2
      }

    }
  }

  "newPartners" must {
    "PartnerDetailsContactNumberPage" must {

      "have the correct path" in {
        PartnerDetailsContactNumberPage(
          newPartnersIndex1
        ).path mustEqual (JsPath \ "newPartners" \ newPartnersIndex1 \ "partnerDetailsCorrespondenceDetailsSection" \ "contactNumber")
      }

      "have the correct toString value" in {

        PartnerDetailsContactNumberPage(newPartnersIndex1).toString mustEqual "contactNumber"
      }

      "be able to read and write PartnerDetailsContactNumberPage values with correct index" in {

        val value1 = ContactNumber(phoneNumber = Some("111"), mobilePhoneNumber = Some("111"))
        val value2 = ContactNumber(phoneNumber = Some("222"), mobilePhoneNumber = Some("222"))

        val json = Json.obj(
          "newPartners" -> Json.arr(
            Json.obj(
              PartnerDetailsCorrespondenceDetailsSectionPage(newPartnersIndex1).toString -> Json.obj(
                PartnerDetailsContactNumberPage(newPartnersIndex1).toString -> Json.toJson(value1)
              )
            ),
            Json.obj(
              PartnerDetailsCorrespondenceDetailsSectionPage(newPartnersIndex2).toString -> Json.obj(
                PartnerDetailsContactNumberPage(newPartnersIndex2).toString -> Json.toJson(value2)
              )
            )
          )
        )

        PartnerDetailsContactNumberPage(newPartnersIndex1).path
          .asSingleJson(json)
          .validate[ContactNumber]
          .get mustEqual value1

        PartnerDetailsContactNumberPage(newPartnersIndex2).path
          .asSingleJson(json)
          .validate[ContactNumber]
          .get mustEqual value2
      }

    }
  }

}
