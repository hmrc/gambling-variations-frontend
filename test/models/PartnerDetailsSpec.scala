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

import java.time.LocalDate

class PartnerDetailsSpec extends AnyWordSpec with Matchers {
  "PartnerDetails" should {

    "read a complete address" in {

      val json = Json.obj(
        "partners" -> Json.arr(
          Json.obj(
            "mgdRegNumber"           -> "XWM00000001762",
            "dateOfJoining"          -> "2022-01-15",
            "dateOfLeaving"          -> "2028-12-31",
            "solePropTitle"          -> "Mr",
            "solePropFirstName"      -> "John",
            "solePropMiddleName"     -> "Michael",
            "solePropLastName"       -> "Doe",
            "businessName"           -> "XYZ Consulting Ltd",
            "tradingName"            -> "XYZ Consulting",
            "dateOfBirth"            -> "1985-06-20",
            "nino"                   -> "AB123456C",
            "utr"                    -> "1234567890",
            "vrn"                    -> "GB123456789",
            "crn"                    -> "09876543",
            "dateOfIncorporation"    -> "2020-03-01",
            "countryOfIncorporation" -> "GB",
            "foreignCorporateRef"    -> "FCR-987654",
            "address1"               -> "123 High Street",
            "address2"               -> "Suite 4",
            "address3"               -> "Business Park",
            "address4"               -> "London",
            "postcode"               -> "SW1A 1AA",
            "country"                -> "GB",
            "adi"                    -> "ADI123456",
            "iomOrCiFlag"            -> "N",
            "phoneNumber"            -> "02071234567",
            "mobilePhoneNumber"      -> "07700123456",
            "faxNumber"              -> "02071234568",
            "emailAddress"           -> "john.doe@example.com",
            "isFutureLeaveDate"      -> 0,
            "isFutureJoinDate"       -> 0,
            "businessType"           -> 2
          )
        ),
        "systemDate" -> "2026-07-30"
      )

      val result =
        json.validate[PartnersDetails]

      result.isSuccess                                shouldBe true
      result.get.partners.size                        shouldBe 1
      result.get.partners.head.mgdRegNumber           shouldBe "XWM00000001762"
      result.get.partners.head.dateOfJoining          shouldBe Some(LocalDate.of(2022, 1, 15))
      result.get.partners.head.dateOfLeaving          shouldBe Some(LocalDate.of(2028, 12, 31))
      result.get.partners.head.solePropTitle          shouldBe Some("Mr")
      result.get.partners.head.solePropFirstName      shouldBe Some("John")
      result.get.partners.head.solePropMiddleName     shouldBe Some("Michael")
      result.get.partners.head.solePropLastName       shouldBe Some("Doe")
      result.get.partners.head.businessName           shouldBe Some("XYZ Consulting Ltd")
      result.get.partners.head.tradingName            shouldBe Some("XYZ Consulting")
      result.get.partners.head.dateOfBirth            shouldBe Some(LocalDate.of(1985, 6, 20))
      result.get.partners.head.nino                   shouldBe Some("AB123456C")
      result.get.partners.head.utr                    shouldBe Some("1234567890")
      result.get.partners.head.vrn                    shouldBe Some("GB123456789")
      result.get.partners.head.crn                    shouldBe Some("09876543")
      result.get.partners.head.dateOfIncorporation    shouldBe Some(LocalDate.of(2020, 3, 1))
      result.get.partners.head.countryOfIncorporation shouldBe Some("GB")
      result.get.partners.head.foreignCorporateRef    shouldBe Some("FCR-987654")
      result.get.partners.head.address1               shouldBe Some("123 High Street")
      result.get.partners.head.address2               shouldBe Some("Suite 4")
      result.get.partners.head.address3               shouldBe Some("Business Park")
      result.get.partners.head.address4               shouldBe Some("London")
      result.get.partners.head.postcode               shouldBe Some("SW1A 1AA")
      result.get.partners.head.country                shouldBe Some("GB")
      result.get.partners.head.adi                    shouldBe Some("ADI123456")
      result.get.partners.head.iomOrCiFlag            shouldBe Some("N")
      result.get.partners.head.phoneNumber            shouldBe Some("02071234567")
      result.get.partners.head.mobilePhoneNumber      shouldBe Some("07700123456")
      result.get.partners.head.faxNumber              shouldBe Some("02071234568")
      result.get.partners.head.emailAddr              shouldBe Some("john.doe@example.com")
      result.get.partners.head.isFutureJoinDate       shouldBe Some(0)
      result.get.partners.head.isFutureLeaveDate      shouldBe Some(0)
      result.get.partners.head.businessType           shouldBe Some(2)
      result.get.systemDate                           shouldBe Some(LocalDate.of(2026, 7, 30))
    }
  }
}
