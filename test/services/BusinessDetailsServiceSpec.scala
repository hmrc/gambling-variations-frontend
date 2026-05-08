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
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class BusinessDetailsServiceSpec extends AnyFreeSpec with Matchers with MockitoSugar with ScalaFutures {

  implicit val ec: ExecutionContext = ExecutionContext.global

  implicit private val hc: HeaderCarrier = HeaderCarrier()
  implicit private val request: FakeRequest[AnyContent] = FakeRequest()

  private val mockConnector = mock[GamblingConnector]

  private val service = new BusinessDetailsService(mockConnector)

  private val sampleDetails = BusinessDetails(
    mgdRegNumber          = "MGD123",
    businessType          = Some(BusinessType.Soleproprietor),
    currentlyRegistered   = 1,
    groupReg              = false,
    dateOfRegistration    = Some(LocalDate.of(2020, 1, 1)),
    businessPartnerNumber = Some("BP001"),
    systemDate            = LocalDate.of(2026, 5, 7)
  )

  override implicit val patienceConfig: PatienceConfig =
    PatienceConfig(
      timeout = scaled(org.scalatest.time.Span(5, org.scalatest.time.Seconds))
    )

  ".retrieveBusinessDetails" - {

    "must return business details from connector" in {

      reset(mockConnector)

      when(mockConnector.getBusinessDetails(eqTo("MGD123"))(any[HeaderCarrier]))
        .thenReturn(Future.successful(sampleDetails))

      val result = service.retrieveBusinessDetails("MGD123").futureValue

      result mustBe sampleDetails

      verify(mockConnector, times(1))
        .getBusinessDetails(eqTo("MGD123"))(any[HeaderCarrier])
    }

    "must propagate connector failure" in {

      reset(mockConnector)

      val exception = new RuntimeException("connector failed")

      when(mockConnector.getBusinessDetails(eqTo("MGD123"))(any[HeaderCarrier]))
        .thenReturn(Future.failed(exception))

      val result = service.retrieveBusinessDetails("MGD123").failed.futureValue

      result mustBe exception

      verify(mockConnector, times(1))
        .getBusinessDetails(eqTo("MGD123"))(any[HeaderCarrier])
    }
  }
}
