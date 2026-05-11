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
import models.{BusinessDetails, BusinessType}
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.BusinessNameChangesPage
import play.api.inject.bind
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.BusinessDetailsService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import java.time.LocalDate
import scala.concurrent.Future

class ChangeRegistrationDetailsControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  private val mockBusinessDetailsService: BusinessDetailsService =
    mock[BusinessDetailsService]

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockBusinessDetailsService)
  }

  private def applicationWithMocks(userAnswers: Option[models.UserAnswers]) =
    applicationBuilder(userAnswers = userAnswers)
      .overrides(
        bind[BusinessDetailsService].toInstance(mockBusinessDetailsService)
      )
      .build()

  private def buildBusinessDetails(
    mgdRegNumber: String,
    groupReg: Boolean,
    businessType: Option[BusinessType]
  ): BusinessDetails =
    BusinessDetails(
      mgdRegNumber          = mgdRegNumber,
      businessType          = businessType,
      currentlyRegistered   = 1,
      groupReg              = groupReg,
      dateOfRegistration    = Some(LocalDate.of(2026, 1, 1)),
      businessPartnerNumber = None,
      systemDate            = LocalDate.of(2026, 1, 1)
    )

  "ChangeRegistrationDetailsController" - {

    "must return OK and render view" in {

      val userAnswers =
        emptyUserAnswers.set(BusinessNameChangesPage, true).success.value

      val application = applicationWithMocks(Some(userAnswers))

      running(application) {
        implicit val request =
          FakeRequest(GET, routes.ChangeRegistrationDetailsController.onPageLoad().url)

        given HeaderCarrier =
          HeaderCarrierConverter.fromRequestAndSession(request, request.session)

        given Request[?] = request

        var capturedMgdReg: String = ""

        when(
          mockBusinessDetailsService.retrieveBusinessDetails(anyString())(
            any[HeaderCarrier],
            any[Request[?]]
          )
        ).thenAnswer { invocation =>
          val mgd = invocation.getArgument[String](0)
          capturedMgdReg = mgd

          Future.successful(
            buildBusinessDetails(
              mgdRegNumber = mgd,
              groupReg     = false,
              businessType = Some(BusinessType.Partnership)
            )
          )
        }

        val result = route(application, request).value

        status(result) mustEqual OK

        verify(mockBusinessDetailsService).retrieveBusinessDetails(anyString())(
          any[HeaderCarrier],
          any[Request[?]]
        )
      }
    }

    "must redirect to SystemErrorController when service fails" in {

      val application = applicationWithMocks(Some(emptyUserAnswers))

      running(application) {
        implicit val request =
          FakeRequest(GET, routes.ChangeRegistrationDetailsController.onPageLoad().url)

        given HeaderCarrier =
          HeaderCarrierConverter.fromRequestAndSession(request, request.session)

        given Request[?] = request

        when(
          mockBusinessDetailsService.retrieveBusinessDetails(anyString())(
            any[HeaderCarrier],
            any[Request[?]]
          )
        ).thenReturn(Future.failed(new RuntimeException("boom")))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          routes.SystemErrorController.onPageLoad().url
      }
    }
  }
}
