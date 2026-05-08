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

package services

import connectors.GamblingConnector
import models.{BusinessDetails, BusinessType}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.concurrent.ScalaFutures
import org.mockito.Mockito.*
import org.mockito.ArgumentMatchers.*
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import repositories.BusinessDetailsCacheRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.mdc.MdcExecutionContext

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class BusinessDetailsServiceSpec extends AnyFreeSpec with Matchers with MockitoSugar with ScalaFutures {

  // Use MDC-preserving ExecutionContext
  implicit val ec: ExecutionContext = MdcExecutionContext()

  // Implicits required by the service
  implicit private val hc: HeaderCarrier = HeaderCarrier()
  implicit private val request: FakeRequest[AnyContent] = FakeRequest()

  // Mocks
  private val mockConnector = mock[GamblingConnector]
  private val mockRepo = mock[BusinessDetailsCacheRepository]

  private val service = new BusinessDetailsService(mockConnector, mockRepo)

  // Sample data
  private val sampleDetails = BusinessDetails(
    mgdRegNumber          = "MGD123",
    businessType          = Some(BusinessType.Soleproprietor),
    currentlyRegistered   = 1,
    groupReg              = false,
    dateOfRegistration    = Some(LocalDate.of(2020, 1, 1)),
    businessPartnerNumber = Some("BP001"),
    systemDate            = LocalDate.of(2026, 5, 7)
  )

  ".retrieveBusinessDetails" - {

    "must return cached BusinessDetails if available in repository" in {
      reset(mockRepo, mockConnector) // clear previous calls

      when(mockRepo.getBusinessDetails("MGD123"))
        .thenReturn(Future.successful(Some(sampleDetails)))

      val result = service.retrieveBusinessDetails("MGD123").futureValue

      result mustBe sampleDetails
      verify(mockRepo, times(1)).getBusinessDetails("MGD123")
      verifyNoMoreInteractions(mockConnector) // connector should not be called
    }

    "must fetch from connector and cache if not in repository" in {
      reset(mockRepo, mockConnector) // ensure no leftover invocations

      when(mockRepo.getBusinessDetails("MGD123"))
        .thenReturn(Future.successful(None))
      when(mockConnector.getBusinessDetails("MGD123")(hc))
        .thenReturn(Future.successful(sampleDetails))
      when(mockRepo.cacheBusinessDetails(sampleDetails))
        .thenReturn(Future.successful(true))

      val result = service.retrieveBusinessDetails("MGD123").futureValue

      result mustBe sampleDetails
      verify(mockRepo, times(1)).getBusinessDetails("MGD123")
      verify(mockConnector, times(1)).getBusinessDetails("MGD123")(hc)
      verify(mockRepo, times(1)).cacheBusinessDetails(sampleDetails)
    }
  }
}
