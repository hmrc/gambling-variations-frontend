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

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import play.api.libs.json.Json
import java.time.{Instant, LocalDate}
import java.time.temporal.ChronoUnit

class BusinessDetailsDAOSpec extends AnyFreeSpec with Matchers {

  "BusinessDetailsDAO" - {

    val businessDetails = BusinessDetails(
      mgdRegNumber          = "12345",
      businessType          = Some(BusinessType.Corporatebody),
      currentlyRegistered   = 1,
      groupReg              = true,
      dateOfRegistration    = Some(LocalDate.of(2020, 1, 1)),
      businessPartnerNumber = Some("BP123"),
      systemDate            = LocalDate.of(2026, 5, 7)
    )

    // truncate to milliseconds for JSON equality
    val now: Instant = Instant.now().truncatedTo(ChronoUnit.MILLIS)

    val dao = BusinessDetailsDAO(
      id              = "12345",
      lastUpdated     = now,
      businessDetails = businessDetails
    )

    "should serialize to JSON correctly" in {
      val json = Json.toJson(dao)

      // Use the companion implicits to read back the JSON fields
      val parsed = json.validate[BusinessDetailsDAO].get

      parsed.id                                  shouldBe dao.id
      parsed.lastUpdated                         shouldBe dao.lastUpdated
      parsed.businessDetails.mgdRegNumber        shouldBe businessDetails.mgdRegNumber
      parsed.businessDetails.currentlyRegistered shouldBe businessDetails.currentlyRegistered
      parsed.businessDetails.dateOfRegistration  shouldBe businessDetails.dateOfRegistration
    }

    "should deserialize from JSON correctly" in {
      val json = Json.toJson(dao)
      val parsed = json.validate[BusinessDetailsDAO].get

      parsed shouldBe dao
    }

    "should handle round-trip serialization/deserialization" in {
      val json = Json.toJson(dao)
      val parsed = json.validate[BusinessDetailsDAO].get
      val reSerialized = Json.toJson(parsed)

      reSerialized shouldBe json
    }

    "should handle optional fields being None" in {
      val bd = businessDetails.copy(
        businessType          = None,
        dateOfRegistration    = None,
        businessPartnerNumber = None
      )

      val daoWithNone = dao.copy(businessDetails = bd)
      val json = Json.toJson(daoWithNone)
      val parsed = json.validate[BusinessDetailsDAO].get

      parsed.businessDetails.businessType          shouldBe None
      parsed.businessDetails.dateOfRegistration    shouldBe None
      parsed.businessDetails.businessPartnerNumber shouldBe None
    }
  }
}
