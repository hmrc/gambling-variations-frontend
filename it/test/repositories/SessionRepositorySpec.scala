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

import config.FrontendAppConfig
import models.UserAnswers
import org.mockito.Mockito.when
import org.mongodb.scala.model.Filters
import org.scalactic.source.Position
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, TryValues}
import org.scalatestplus.mockito.MockitoSugar
import org.slf4j.MDC
import pages.contactdetails.{BusinessContactDetailsSection, BusinessContactDetailsSubmittedPage, BusinessEmailAddressPage, BusinessFaxNumberPage}
import play.api.libs.json.Json
import uk.gov.hmrc.mdc.MdcExecutionContext
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import java.time.temporal.ChronoUnit
import java.time.{Clock, Instant, ZoneId}
import scala.concurrent.{ExecutionContext, ExecutionContextExecutorService, Future}

class SessionRepositorySpec
    extends AnyFreeSpec
    with Matchers
    with DefaultPlayMongoRepositorySupport[UserAnswers]
    with ScalaFutures
    with IntegrationPatience
    with OptionValues
    with TryValues
    with MockitoSugar {

  private val instant = Instant.now.truncatedTo(ChronoUnit.MILLIS)
  private val stubClock: Clock = Clock.fixed(instant, ZoneId.systemDefault)

  private val userAnswers = UserAnswers("id", Json.obj("foo" -> "bar"), Instant.ofEpochSecond(1))

  private val mockAppConfig = mock[FrontendAppConfig]
  when(mockAppConfig.cacheTtl) thenReturn 1L

  implicit val productionLikeTestMdcExecutionContext: ExecutionContext = MdcExecutionContext()

  protected override val repository: SessionRepository = new SessionRepository(
    mongoComponent = mongoComponent,
    appConfig      = mockAppConfig,
    clock          = stubClock
  )

  ".set" - {

    "must set the last updated time on the supplied user answers to `now`, and save them" in {

      val expectedResult = userAnswers copy (lastUpdated = instant)

      val setResult = repository.set(userAnswers).futureValue
      val updatedRecord = find(Filters.equal("_id", userAnswers.id)).futureValue.headOption.value

      updatedRecord mustEqual expectedResult
    }

    mustPreserveMdc(repository.set(userAnswers))
  }

  ".get" - {

    "must migrate legacy controlling body answers and retain edits on the next save" in {
      val legacy = UserAnswers(
        userAnswers.id,
        Json.obj(
          "controllingBodyDetailsSection" -> Json.obj("typeOfControllingBody" -> "partnership", "businessName" -> "Original"),
          "controllingBodyDetails"        -> Json.obj("businessName" -> "Edited", "loaded" -> true)
        )
      )
      insert(legacy).futureValue

      val migrated = repository.get(legacy.id).futureValue.value
      migrated.data mustBe Json.obj("controllingBodyDetails" -> Json.obj("typeOfControllingBody" -> 4, "businessName" -> "Edited", "loaded" -> true))
      repository.set(migrated).futureValue mustBe true
      repository.get(legacy.id).futureValue.value.data mustBe migrated.data
    }

    "must keep a tombstone, a flag and the time a section was loaded through the round trip" in {
      val answers = UserAnswers("id")
        .withSectionLoaded(BusinessContactDetailsSection, instant)
        .setOriginal(BusinessFaxNumberPage, "01632 960001")
        .flatMap(_.setOriginal(BusinessEmailAddressPage, "info@example.com"))
        .flatMap(_.set(BusinessEmailAddressPage, "contact@example.com"))
        .flatMap(_.remove(BusinessFaxNumberPage))
        .flatMap(_.set(BusinessContactDetailsSubmittedPage, true))
        .success
        .value

      repository.set(answers).futureValue mustBe true

      val result = repository.get(answers.id).futureValue.value
      result.original mustBe answers.original
      result.modified mustBe answers.modified
      result.flags mustBe answers.flags
      result.get(BusinessFaxNumberPage) mustBe None
    }

    "must keep a removed controlling body VAT number absent after saving and reading again" in {
      import pages.controllingbody.{ControllingBodyRemoveVATRegistrationNumberPage, ControllingBodyVrnPage}

      val original = UserAnswers(userAnswers.id, Json.obj(
        "controllingBodyDetails" -> Json.obj("mgdRegNum" -> "XGM00000001761", "vrn" -> "123456789")
      ))
      repository.set(original).futureValue mustBe true
      val loaded = repository.get(original.id).futureValue.value
      val removed = loaded.remove(ControllingBodyVrnPage).get
        .set(ControllingBodyRemoveVATRegistrationNumberPage, true).get
      repository.set(removed).futureValue mustBe true

      val reloaded = repository.get(original.id).futureValue.value
      reloaded.get(ControllingBodyVrnPage) mustBe None
      reloaded.get(ControllingBodyRemoveVATRegistrationNumberPage) mustBe Some(true)
      (reloaded.data \ "controllingBodyDetails" \ "mgdRegNum").as[String] mustBe "XGM00000001761"
    }

    "when there is a record for this id" - {

      "must update the lastUpdated time and get the record" in {

        insert(userAnswers).futureValue

        val result = repository.get(userAnswers.id).futureValue
        val expectedResult = userAnswers copy (lastUpdated = instant)

        result.value mustEqual expectedResult
      }
    }

    "when there is no record for this id" - {

      "must return None" in {

        repository.get("id that does not exist").futureValue must not be defined
      }
    }

    mustPreserveMdc(repository.get(userAnswers.id))
  }

  ".clear" - {

    "must remove a record" in {

      insert(userAnswers).futureValue

      val result = repository.clear(userAnswers.id).futureValue

      repository.get(userAnswers.id).futureValue must not be defined
    }

    "must return true when there is no record to remove" in {
      val result = repository.clear("id that does not exist").futureValue

      result mustEqual true
    }

    mustPreserveMdc(repository.clear(userAnswers.id))
  }

  ".keepAlive" - {

    "when there is a record for this id" - {

      "must update its lastUpdated to `now` and return true" in {

        insert(userAnswers).futureValue

        val result = repository.keepAlive(userAnswers.id).futureValue

        val expectedUpdatedAnswers = userAnswers copy (lastUpdated = instant)

        val updatedAnswers = find(Filters.equal("_id", userAnswers.id)).futureValue.headOption.value
        updatedAnswers mustEqual expectedUpdatedAnswers
      }
    }

    "when there is no record for this id" - {

      "must return true" in {

        repository.keepAlive("id that does not exist").futureValue mustEqual true
      }
    }

    mustPreserveMdc(repository.keepAlive(userAnswers.id))
  }

  private def mustPreserveMdc[A](f: => Future[A])(implicit pos: Position): Unit =
    "must preserve MDC" in {

      MDC.put("test", "foo")

      (f.map { _ =>
        Option(MDC.get("test"))
      }.futureValue) mustEqual Some("foo")
    }
}
