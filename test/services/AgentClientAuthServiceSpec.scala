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
import models.ClientListStatus.*
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{never, verify, when}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AgentClientAuthServiceSpec extends AnyFreeSpec with Matchers with ScalaFutures with IntegrationPatience with MockitoSugar {

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  private val regime = "mgd"
  private val regNumber = "XGM00003122200"

  private def newService(): (GamblingConnector, AgentClientAuthService) = {
    val connector = mock[GamblingConnector]
    (connector, new AgentClientAuthService(connector))
  }

  "authoriseClient" - {

    "returns Authorised when the client list is ready and the agent holds the client" in {
      val (connector, service) = newService()
      when(connector.startClientListRetrieval(eqTo(regime))(using any[HeaderCarrier]())).thenReturn(Future.successful(Succeeded))
      when(connector.hasClient(eqTo(regime), eqTo(regNumber))(using any[HeaderCarrier]())).thenReturn(Future.successful(true))

      service.authoriseClient(regNumber).futureValue mustBe AgentClientAuthResult.Authorised
    }

    "returns NotAuthorised when the client list is ready but the agent does not hold the client" in {
      val (connector, service) = newService()
      when(connector.startClientListRetrieval(eqTo(regime))(using any[HeaderCarrier]())).thenReturn(Future.successful(Succeeded))
      when(connector.hasClient(eqTo(regime), eqTo(regNumber))(using any[HeaderCarrier]())).thenReturn(Future.successful(false))

      service.authoriseClient(regNumber).futureValue mustBe AgentClientAuthResult.NotAuthorised
    }

    "returns NotReady (without checking hasClient) when the retrieval is still in progress" in {
      val (connector, service) = newService()
      when(connector.startClientListRetrieval(eqTo(regime))(using any[HeaderCarrier]())).thenReturn(Future.successful(InProgress))

      service.authoriseClient(regNumber).futureValue mustBe AgentClientAuthResult.NotReady
      verify(connector, never).hasClient(any[String](), any[String]())(using any[HeaderCarrier]())
    }

    "returns NotReady (without checking hasClient) when the backend still needs to initiate the download" in {
      val (connector, service) = newService()
      when(connector.startClientListRetrieval(eqTo(regime))(using any[HeaderCarrier]())).thenReturn(Future.successful(InitiateDownload))

      service.authoriseClient(regNumber).futureValue mustBe AgentClientAuthResult.NotReady
      verify(connector, never).hasClient(any[String](), any[String]())(using any[HeaderCarrier]())
    }

    "returns Failed (without checking hasClient) when the retrieval fails" in {
      val (connector, service) = newService()
      when(connector.startClientListRetrieval(eqTo(regime))(using any[HeaderCarrier]())).thenReturn(Future.successful(Failed))

      service.authoriseClient(regNumber).futureValue mustBe AgentClientAuthResult.Failed
      verify(connector, never).hasClient(any[String](), any[String]())(using any[HeaderCarrier]())
    }

    "returns Failed when a downstream call errors" in {
      val (connector, service) = newService()
      when(connector.startClientListRetrieval(eqTo(regime))(using any[HeaderCarrier]()))
        .thenReturn(Future.failed(new RuntimeException("backend down")))

      service.authoriseClient(regNumber).futureValue mustBe AgentClientAuthResult.Failed
    }
  }
}
