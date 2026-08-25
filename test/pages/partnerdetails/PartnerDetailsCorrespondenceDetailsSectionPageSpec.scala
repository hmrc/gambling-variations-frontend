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

import models.{Address, ContactNumber, CorrespondenceDetails}
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{JsPath, Json}

class PartnerDetailsCorrespondenceDetailsSectionPageSpec extends PlaySpec {

  val Index = 0

  "PartnerDetailsCorrespondenceDetailsSectionPage" must {

    "have the correct path" in {
      PartnerDetailsCorrespondenceDetailsSectionPage(
        Index
      ).path mustEqual (JsPath \ "partners" \ Index \ "partnerDetailsCorrespondenceDetailsSection")
    }

    "have the correct toString value" in {

      PartnerDetailsCorrespondenceDetailsSectionPage(Index).toString mustEqual "partnerDetailsCorrespondenceDetailsSection"
    }

    "be able to read and write PartnerDetailsCorrespondenceDetailsSectionPage values with correct index" in {

      val value1 = CorrespondenceDetails(
        mgdRegNumber = "111",
        nameLine1    = Some("111"),
        nameLine2    = Some("111"),
        correspondenceAddress = Some(
          Address(
            address1 = "111",
            address2 = Some("111"),
            address3 = Some("111"),
            address4 = Some("111"),
            postcode = Some("111"),
            country  = Some("111")
          )
        ),
        additionalInformation = Some("111"),
        iomOrCiFlag           = Some("111"),
        contactNumber = Some(
          ContactNumber(
            phoneNumber       = Some("111"),
            mobilePhoneNumber = Some("111")
          )
        ),
        faxNumber = Some("111"),
        emailAddr = Some("111")
      )

      val value1AsJson = Json.obj(
        "mgdRegNumber"      -> "111",
        "nameLine1"         -> "111",
        "nameLine2"         -> "111",
        "address1"          -> "111",
        "address2"          -> "111",
        "address3"          -> "111",
        "address4"          -> "111",
        "postcode"          -> "111",
        "country"           -> "111",
        "adi"               -> "111",
        "iomOrCiFlag"       -> "111",
        "phoneNumber"       -> "111",
        "mobilePhoneNumber" -> "111",
        "faxNumber"         -> "111",
        "emailAddr"         -> "111"
      )

      val value2 = CorrespondenceDetails(
        mgdRegNumber = "222",
        nameLine1    = Some("222"),
        nameLine2    = Some("222"),
        correspondenceAddress = Some(
          Address(
            address1 = "222",
            address2 = Some("222"),
            address3 = Some("222"),
            address4 = Some("222"),
            postcode = Some("222"),
            country  = Some("222")
          )
        ),
        additionalInformation = Some("222"),
        iomOrCiFlag           = Some("222"),
        contactNumber = Some(
          ContactNumber(
            phoneNumber       = Some("222"),
            mobilePhoneNumber = Some("222")
          )
        ),
        faxNumber = Some("222"),
        emailAddr = Some("222")
      )

      val value2AsJson = Json.obj(
        "mgdRegNumber"      -> "222",
        "nameLine1"         -> "222",
        "nameLine2"         -> "222",
        "address1"          -> "222",
        "address2"          -> "222",
        "address3"          -> "222",
        "address4"          -> "222",
        "postcode"          -> "222",
        "country"           -> "222",
        "adi"               -> "222",
        "iomOrCiFlag"       -> "222",
        "phoneNumber"       -> "222",
        "mobilePhoneNumber" -> "222",
        "faxNumber"         -> "222",
        "emailAddr"         -> "222"
      )

      val json = Json.obj(
        "partners" -> Json.arr(
          Json.obj(
            PartnerDetailsCorrespondenceDetailsSectionPage(Index).toString -> value1AsJson
          ),
          Json.obj(
            PartnerDetailsCorrespondenceDetailsSectionPage(Index + 1).toString -> value2AsJson
          )
        )
      )


      PartnerDetailsCorrespondenceDetailsSectionPage(Index).path
        .asSingleJson(json)
        .validate[CorrespondenceDetails]
        .get mustEqual value1

      PartnerDetailsCorrespondenceDetailsSectionPage(Index + 1).path
        .asSingleJson(json)
        .validate[CorrespondenceDetails]
        .get mustEqual value2
    }

  }
}
