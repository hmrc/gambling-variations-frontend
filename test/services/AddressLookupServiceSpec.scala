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

import base.SpecBase
import connectors.AddressLookupConnector
import models.Address
import models.addresslookup.AddressLookupConfigSettings
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class AddressLookupServiceSpec extends SpecBase with MockitoSugar {

  implicit val ec: ExecutionContext = ExecutionContext.global
  implicit private val hc: HeaderCarrier = HeaderCarrier()
  implicit private val messages: Messages = stubMessages()

  private val mockConnector = mock[AddressLookupConnector]
  private val service = new AddressLookupService(mockConnector, testFrontendAppConfig)

  ".initJourney" - {

    "must initialise an Address Lookup journey with a callback URL and return the on-ramp URL" in {

      val configCaptor = ArgumentCaptor.forClass(classOf[AddressLookupConfigSettings])
      val onRampUrl = "/address-lookup/on-ramp"

      when(mockConnector.initJourney(any[AddressLookupConfigSettings])(any[HeaderCarrier]))
        .thenReturn(Future.successful(onRampUrl))

      val result = service.initJourney().futureValue

      result mustBe onRampUrl

      verify(mockConnector).initJourney(configCaptor.capture())(any[HeaderCarrier])
      configCaptor.getValue.options.continueUrl mustBe
        "http://localhost:9000/change-registration-details/correspondence-details/address-lookup/callback"
      configCaptor.getValue.options.ukMode mustBe true
    }
  }

  ".retrieveAddress" - {

    "must return an address from the connector" in {

      val address = Address("1 Test Street", Some("Testtown"), None, None, Some("AA1 1AA"), Some("GB"))

      when(mockConnector.retrieveAddress(any[String])(any[HeaderCarrier], any[ExecutionContext]))
        .thenReturn(Future.successful(address))

      service.retrieveAddress("journey-id").futureValue mustBe address
    }
  }
}
