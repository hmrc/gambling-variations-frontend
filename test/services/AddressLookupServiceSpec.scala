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
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class AddressLookupServiceSpec extends SpecBase with MockitoSugar {

  implicit val ec: ExecutionContext = ExecutionContext.global
  implicit private val hc: HeaderCarrier = HeaderCarrier()
  private val app = applicationBuilder().build()
  implicit private val messages: Messages = this.messages(app)

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
      val config = configCaptor.getValue

      config.version mustBe 2
      config.options.continueUrl mustBe
        "http://localhost:9000/gambling-variations/change-registration-details/correspondence-details/address-lookup/callback"
      config.options.homeNavHref mustBe "http://gambling-variations/home"
      config.options.accessibilityFooterUrl mustBe
        "http://localhost:12346/accessibility-statement/gambling-variations-frontend"
      config.options.deskProServiceName mustBe "gambling-variations-frontend"
      config.options.ukMode mustBe true
      config.options.selectPageConfig.showSearchAgainLink mustBe true
      config.options.timeoutConfig mustBe models.addresslookup.TimeoutConfig(
        timeoutAmount       = 900,
        timeoutUrl          = "http://localhost:9000/gambling-variations/there-is-a-problem",
        timeoutKeepAliveUrl = "http://localhost:9000/gambling-variations/refresh-session"
      )

      config.labels.en.lookupPageLabels.title mustBe "Find the correspondence address"
      config.labels.en.lookupPageLabels.heading mustBe "Find the correspondence address"
      config.labels.en.lookupPageLabels.postcodeLabel mustBe "Postcode"
      config.labels.en.lookupPageLabels.filterLabel mustBe "Property name or number (optional)"
      config.labels.en.lookupPageLabels.submitLabel mustBe "Find address"
      config.labels.en.lookupPageLabels.manualAddressLinkText mustBe "Enter the address manually"
      config.labels.en.confirmPageLabels.heading mustBe "Review and confirm"
      config.labels.en.confirmPageLabels.changeLinkText mustBe "Change"
      config.labels.en.confirmPageLabels.submitLabel mustBe "Confirm address"
      config.labels.en.editPageLabels.townLabel mustBe "County"
      config.options.manualAddressEntryConfig.maxLengthErrorMessages.en.addressLine1 mustBe
        "Address line 1 must be 35 characters or fewer"
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
