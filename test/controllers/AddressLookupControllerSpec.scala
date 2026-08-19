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

package controllers

import base.SpecBase
import models.{Address, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.businessaddress.isleMOrChannelFlagPage
import pages.correspondencedetails.{CorrespondenceAddressUkPage, CorrespondenceDetailsSectionPage, CorrespondenceDetailsSubmittedPage}
import play.api.inject.bind
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.AddressLookupService
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

class AddressLookupControllerSpec extends SpecBase with MockitoSugar {

  private val addressLookupUrl = "/address-lookup/on-ramp"

  private val userAnswers =
    emptyUserAnswers
      .set(CorrespondenceDetailsSectionPage, mgdRegNum)
      .success
      .value

  "AddressLookup Controller" - {

    "must redirect to the Address Lookup on-ramp URL when initialising a journey" in {

      val mockAddressLookupService = mock[AddressLookupService]

      when(mockAddressLookupService.initJourney()(any[HeaderCarrier], any[Messages]))
        .thenReturn(Future.successful(addressLookupUrl))

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[AddressLookupService].toInstance(mockAddressLookupService))
          .build()

      running(application) {
        val request = FakeRequest(GET, routes.AddressLookupController.initialise().url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual addressLookupUrl
      }
    }

    "must retrieve and save the confirmed address when Address Lookup returns" in {

      val mockAddressLookupService = mock[AddressLookupService]
      val mockSessionRepository = mock[SessionRepository]
      val savedAnswersCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
      val address = Address("1 Test Street", Some("Testtown"), None, None, Some("JE1 1AA"), Some("GB"))

      when(mockAddressLookupService.retrieveAddress(any())(any()))
        .thenReturn(Future.successful(address))
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[AddressLookupService].toInstance(mockAddressLookupService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, routes.AddressLookupController.callback("journey-id").url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.CheckCorrespondenceDetailsController.onPageLoad().url

        verify(mockSessionRepository).set(savedAnswersCaptor.capture())
        savedAnswersCaptor.getValue.get(CorrespondenceAddressUkPage).value mustBe address
        savedAnswersCaptor.getValue.get(isleMOrChannelFlagPage).value mustBe "true"
        savedAnswersCaptor.getValue.get(CorrespondenceDetailsSubmittedPage).value mustBe true
      }
    }
  }
}
