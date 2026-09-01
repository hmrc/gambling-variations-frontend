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

import models.ContactNumber
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{JsPath, Json}

class PartnerDetailsContactNumberPageSpec extends PlaySpec {

  val Index = 0

  "PartnerDetailsContactNumberPage" must {

    "have the correct path" in {
      PartnerDetailsContactNumberPage(
        Index
      ).path mustEqual (JsPath \ "partners" \ Index \ "partnerDetailsCorrespondenceDetailsSection" \ "contactNumber")
    }

    "have the correct toString value" in {

      PartnerDetailsContactNumberPage(Index).toString mustEqual "contactNumber"
    }

    "be able to read and write PartnerDetailsContactNumberPage values with correct index" in {

      val value1 = ContactNumber(phoneNumber = Some("111"), mobilePhoneNumber = Some("111"))
      val value2 = ContactNumber(phoneNumber = Some("222"), mobilePhoneNumber = Some("222"))

      val json = Json.obj(
        "partners" -> Json.arr(
          Json.obj(
            PartnerDetailsCorrespondenceDetailsSectionPage(Index).toString -> Json.obj(
              PartnerDetailsContactNumberPage(Index).toString -> Json.toJson(value1)
            )
          ),
          Json.obj(
            PartnerDetailsCorrespondenceDetailsSectionPage(Index + 1).toString -> Json.obj(
              PartnerDetailsContactNumberPage(Index + 1).toString -> Json.toJson(value2)
            )
          )
        )
      )

      PartnerDetailsContactNumberPage(Index).path
        .asSingleJson(json)
        .validate[ContactNumber]
        .get mustEqual value1

      PartnerDetailsContactNumberPage(Index + 1).path
        .asSingleJson(json)
        .validate[ContactNumber]
        .get mustEqual value2
    }

  }
}
