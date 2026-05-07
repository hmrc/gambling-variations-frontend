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

package repositories

import models.{ApiBusinessType, BusinessDetails, BusinessDetailsDAO}
import org.mongodb.scala.model.Filters
import org.scalactic.source.Position
import org.scalatest.OptionValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.slf4j.MDC
import uk.gov.hmrc.mdc.MdcExecutionContext
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import java.time.{Clock, Instant, LocalDate, ZoneId}
import scala.concurrent.{ExecutionContext, Future}

class BusinessDetailsCacheRepositorySpec
    extends AnyFreeSpec
    with Matchers
    with DefaultPlayMongoRepositorySupport[BusinessDetailsDAO]
    with ScalaFutures
    with IntegrationPatience
    with OptionValues {

  // Deterministic fixed clock for tests
  private val instant = Instant.parse("2026-05-07T10:15:30.00Z")
  private val stubClock: Clock = Clock.fixed(instant, ZoneId.of("UTC"))

  // Use MDC-aware ExecutionContext
  implicit val ec: ExecutionContext = MdcExecutionContext()

  protected override val repository: BusinessDetailsCacheRepository =
    new BusinessDetailsCacheRepository(mongoComponent, stubClock)

  private val sampleBusinessDetails = BusinessDetails(
    mgdRegNumber          = "MGD123",
    businessType          = Some(ApiBusinessType.SoleProprietor),
    currentlyRegistered   = 1,
    groupReg              = false,
    dateOfRegistration    = Some(LocalDate.of(2020, 1, 1)),
    businessPartnerNumber = Some("BP001"),
    systemDate            = LocalDate.of(2026, 5, 7)
  )

  ".cacheBusinessDetails" - {

    "must insert a new BusinessDetailsDAO when none exists" in {
      val result = repository.cacheBusinessDetails(sampleBusinessDetails).futureValue
      result mustBe true

      val inserted = find(Filters.equal("_id", sampleBusinessDetails.mgdRegNumber)).futureValue.headOption
      inserted mustBe defined
      inserted.get.id mustBe sampleBusinessDetails.mgdRegNumber
      inserted.get.lastUpdated mustBe instant
      inserted.get.businessDetails mustBe sampleBusinessDetails
    }

    "must upsert an existing BusinessDetailsDAO" in {
      repository.cacheBusinessDetails(sampleBusinessDetails).futureValue

      val updatedDetails = sampleBusinessDetails.copy(currentlyRegistered = 2)

      val result = repository.cacheBusinessDetails(updatedDetails).futureValue
      result mustBe true

      val found = find(Filters.equal("_id", updatedDetails.mgdRegNumber)).futureValue.headOption.get
      found.businessDetails.currentlyRegistered mustBe 2
      found.lastUpdated mustBe instant
    }

    mustPreserveMdc(repository.cacheBusinessDetails(sampleBusinessDetails))
  }

  ".getBusinessDetails" - {

    "must return None when no record exists" in {
      repository.getBusinessDetails("NON_EXISTENT_ID").futureValue mustBe None
    }

    "must return the BusinessDetails when a record exists" in {
      repository.cacheBusinessDetails(sampleBusinessDetails).futureValue

      val result = repository.getBusinessDetails(sampleBusinessDetails.mgdRegNumber).futureValue
      result mustBe Some(sampleBusinessDetails)
    }

    mustPreserveMdc(repository.getBusinessDetails(sampleBusinessDetails.mgdRegNumber))
  }

  /** MDC-safe helper to ensure MDC is preserved across Futures */
  private def mustPreserveMdc[A](f: => Future[A])(implicit pos: Position): Unit =
    "must preserve MDC" in {
      MDC.put("test", "foo")
      val capturedMdc = Option(MDC.getCopyOfContextMap).orNull

      val resultWithMdc = f.map { r =>
        if (capturedMdc != null) MDC.setContextMap(capturedMdc)
        r
      }

      resultWithMdc.futureValue
      MDC.get("test") mustEqual "foo"
    }
}
