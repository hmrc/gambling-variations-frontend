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
import play.api.libs.json.Json

class CorrespondenceDetailsSpec extends AnyWordSpec with Matchers {

  "CorrespondenceDetails" should {

    "write JSON correctly" in {

      val model =
        CorrespondenceDetails(
          mgdRegNumber          = "MGD123456",
          nameLine1             = Some("Test Name"),
          nameLine2             = Some("Line 2"),
          correspondenceAddress = None,
          additionalInformation = Some("Additional information"),
          iomOrCiFlag           = Some(true),
          contactNumber         = None,
          faxNumber             = Some("12345"),
          emailAddr             = Some("test@test.com")
        )

      val json =
        Json.toJson(model)

      (json \ "mgdRegNumber").as[String] shouldBe "MGD123456"
      (json \ "iomOrCiFlag").as[String]  shouldBe "Y"
    }

    "convert empty nameLine2 to None" in {

      val json =
        Json.obj(
          "mgdRegNumber" -> "MGD123456",
          "nameLine1"    -> "Test Name",
          "nameLine2"    -> ""
        )

      val result =
        json.validate[CorrespondenceDetails]

      result.isSuccess     shouldBe true
      result.get.nameLine2 shouldBe None
    }

    "read JSON correctly" in {

      val json =
        Json.obj(
          "mgdRegNumber" -> "MGD123456",
          "nameLine1"    -> "Test Name",
          "nameLine2"    -> "Line 2",
          "adi"          -> "Additional information",
          "iomOrCiFlag"  -> "0",
          "faxNumber"    -> "12345",
          "emailAddr"    -> "test@test.com"
        )

      val result =
        json.validate[CorrespondenceDetails]

      result.isSuccess shouldBe true

      result.get.additionalInformation shouldBe
        Some("Additional information")

      result.get.iomOrCiFlag shouldBe
        Some(false)
    }

    "convert empty adi to None" in {

      val json =
        Json.obj(
          "mgdRegNumber" -> "MGD123456",
          "adi"          -> ""
        )

      val result =
        json.validate[CorrespondenceDetails]

      result.isSuccess                 shouldBe true
      result.get.additionalInformation shouldBe None
    }

    "convert empty faxNumber to None" in {

      val json =
        Json.obj(
          "mgdRegNumber" -> "MGD123456",
          "faxNumber"    -> ""
        )

      val result =
        json.validate[CorrespondenceDetails]

      result.isSuccess     shouldBe true
      result.get.faxNumber shouldBe None
    }

    "convert empty emailAddr to None" in {

      val json =
        Json.obj(
          "mgdRegNumber" -> "MGD123456",
          "emailAddr"    -> ""
        )

      val result =
        json.validate[CorrespondenceDetails]

      result.isSuccess     shouldBe true
      result.get.emailAddr shouldBe None
    }

    "set correspondenceAddress to None when address is absent" in {

      val json =
        Json.obj(
          "mgdRegNumber" -> "MGD123456"
        )

      val result =
        json.validate[CorrespondenceDetails]

      result.isSuccess                 shouldBe true
      result.get.correspondenceAddress shouldBe None
    }

    "set contactNumber to None when contact details are absent" in {

      val json =
        Json.obj(
          "mgdRegNumber" -> "MGD123456"
        )

      val result =
        json.validate[CorrespondenceDetails]

      result.isSuccess         shouldBe true
      result.get.contactNumber shouldBe None
    }

    "retain iomOrCiFlag when present" in {

      val json = {
        Json.obj(
          "mgdRegNumber" -> "MGD123456",
          "iomOrCiFlag"  -> "1"
        )

      }

      val result =
        json.validate[CorrespondenceDetails]

      result.isSuccess       shouldBe true
      result.get.iomOrCiFlag shouldBe Some(true)
    }

    "set iomOrCiFlag to None when not parsable" in {

      val json1 = {
        Json.obj(
          "mgdRegNumber" -> "MGD123456",
          "iomOrCiFlag"  -> "incorrect-flag"
        )
      }

      val json2 = {
        Json.obj(
          "mgdRegNumber" -> "MGD123456",
          "iomOrCiFlag"  -> null
        )
      }

      val json3 = {
        Json.obj(
          "mgdRegNumber" -> "MGD123456"
        )
      }

      val result1 =
        json1.validate[CorrespondenceDetails]

      val result2 =
        json2.validate[CorrespondenceDetails]

      val result3 =
        json3.validate[CorrespondenceDetails]

      result1.isSuccess       shouldBe true
      result1.get.iomOrCiFlag shouldBe None

      result2.isSuccess       shouldBe true
      result2.get.iomOrCiFlag shouldBe None

      result3.isSuccess       shouldBe true
      result3.get.iomOrCiFlag shouldBe None
    }

    "retain nameLine1 when present" in {

      val json =
        Json.obj(
          "mgdRegNumber" -> "MGD123456",
          "nameLine1"    -> "Name Line 1"
        )

      val result =
        json.validate[CorrespondenceDetails]

      result.isSuccess     shouldBe true
      result.get.nameLine1 shouldBe Some("Name Line 1")
    }

  }
}
