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
import connectors.GamblingConnector
import models.*
import pages.*
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import org.mockito.Mockito.*
import org.mockito.ArgumentMatchers.*
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import scala.concurrent.Future
import java.time.LocalDate

class CheckTradingDetailsControllerSpec extends SpecBase with MockitoSugar {

  private val businessDetails =
    BusinessDetails(
      mgdRegNumber          = mgdRegNum,
      businessType          = None,
      currentlyRegistered   = 1,
      groupReg              = false,
      dateOfRegistration    = None,
      businessPartnerNumber = None,
      systemDate            = LocalDate.of(2026, 1, 1)
    )

  private val filledUserAnswers =
    emptyUserAnswers
      .set(MgdTradeDetailsSectionPage, "MGD999999")
      .success
      .value
      .set(GroupMemberPage, false)
      .success
      .value
      .set(BusinessTypePage, BusinessType.Soleproprietor)
      .success
      .value
      .set(BusinessTradeClassPage, BusinessTradeClass.Casino)
      .success
      .value
      .set(IsSeasonalBusinessPage, true)
      .success
      .value
      .set(PreviousRegistrationNumbersListPage, Seq("MGD123", "MGD456"))
      .success
      .value
      .set(AssociatedRegistrationNumbersPage, Seq("ASS789"))
      .success
      .value

  "CheckTradingDetailsController" - {

    "must return OK for a GET request" in {

      val mockConnector = mock[GamblingConnector]

      when(mockConnector.getBusinessDetails(any())(any()))
        .thenReturn(Future.successful(businessDetails))

      val application =
        applicationBuilder(userAnswers = Some(filledUserAnswers))
          .overrides(bind[GamblingConnector].toInstance(mockConnector))
          .build()

      running(application) {

        val request =
          FakeRequest(GET, routes.CheckTradingDetailsController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual OK
      }
    }

    "must show trade class section when user is NOT a group member" in {

      val mockConnector = mock[GamblingConnector]

      when(mockConnector.getBusinessDetails(any())(any()))
        .thenReturn(Future.successful(businessDetails))

      val application =
        applicationBuilder(userAnswers = Some(filledUserAnswers))
          .overrides(bind[GamblingConnector].toInstance(mockConnector))
          .build()

      running(application) {

        val request =
          FakeRequest(GET, routes.CheckTradingDetailsController.onPageLoad().url)

        val content =
          contentAsString(route(application, request).value)

        content must include("Trade class")
        content must include("Casino")
      }
    }

    "must show all sections when user is NOT a group member" in {

      val mockConnector = mock[GamblingConnector]

      when(mockConnector.getBusinessDetails(any())(any()))
        .thenReturn(Future.successful(businessDetails))

      val application =
        applicationBuilder(userAnswers = Some(filledUserAnswers))
          .overrides(bind[GamblingConnector].toInstance(mockConnector))
          .build()

      running(application) {

        val request =
          FakeRequest(GET, routes.CheckTradingDetailsController.onPageLoad().url)

        val content =
          contentAsString(route(application, request).value)

        content must include("Trade class")
        content must include("Seasonal business")
        content must include("Yes")

        content must include("Previous MGD registration numbers")
        content must include("MGD123")
        content must include("MGD456")

        content must include("Associated MGD registration numbers")
        content must include("ASS789")
      }
    }

    "must hide trade class and MGD registration sections when user IS a group member" in {

      val groupMemberAnswers =
        filledUserAnswers
          .set(GroupMemberPage, true)
          .success
          .value

      val mockConnector = mock[GamblingConnector]

      when(mockConnector.getBusinessDetails(any())(any()))
        .thenReturn(Future.successful(businessDetails.copy(groupReg = true)))

      val application =
        applicationBuilder(userAnswers = Some(groupMemberAnswers))
          .overrides(bind[GamblingConnector].toInstance(mockConnector))
          .build()

      running(application) {

        val request =
          FakeRequest(GET, routes.CheckTradingDetailsController.onPageLoad().url)

        val content =
          contentAsString(route(application, request).value)

        content must not include "Trade class"
        content must not include "Casino"
        content must not include "Previous MGD registration numbers"
        content must not include "Associated MGD registration numbers"

        content must include("Seasonal business")
      }
    }

    "onContinue" - {

      "must redirect to BusinessTradeClass when trade class is missing" in {
        val mockConnector = mock[GamblingConnector]

        when(mockConnector.getBusinessDetails(any())(any()))
          .thenReturn(Future.successful(businessDetails))

        val mgdDetails = MgdTradeDetails(
          mgdRegNumber                     = "MGD999999",
          isBusinessSeasonal               = Some(true),
          businessTradeClass               = Some(BusinessTradeClass.Casino),
          businessActivityDesc             = null,
          previousMgdRegistrationNumbers   = Some(Seq("MGD123")),
          associatedMgdRegistrationNumbers = Some(Seq("ASS456")),
          systemDate                       = Some(LocalDate.of(2026, 1, 1))
        )

        when(mockConnector.getMgdTradeDetails(any[String])(any()))
          .thenReturn(Future.successful(mgdDetails))

        val ua =
          emptyUserAnswers
            .set(MgdTradeDetailsSectionPage, "MGD999999")
            .success
            .value

        val application =
          applicationBuilder(userAnswers = Some(ua))
            .overrides(bind[GamblingConnector].toInstance(mockConnector))
            .build()

        running(application) {
          val request = FakeRequest(POST, routes.CheckTradingDetailsController.onContinue().url)
          val result = route(application, request).value

          redirectLocation(result).value mustBe
            routes.BusinessTradeClassController.onPageLoad(NormalMode).url
        }
      }

      "must redirect to OtherTradeClass when trade class is Other and description is missing" in {
        val mockConnector = mock[GamblingConnector]

        when(mockConnector.getBusinessDetails(any())(any()))
          .thenReturn(Future.successful(businessDetails))

        val mgdDetails = MgdTradeDetails(
          mgdRegNumber                     = "MGD999999",
          isBusinessSeasonal               = Some(false),
          businessTradeClass               = Some(BusinessTradeClass.Other),
          businessActivityDesc             = null,
          previousMgdRegistrationNumbers   = Some(Seq("MGD123")),
          associatedMgdRegistrationNumbers = Some(Seq("ASS456")),
          systemDate                       = Some(LocalDate.of(2026, 1, 1))
        )

        when(mockConnector.getMgdTradeDetails(any[String])(any()))
          .thenReturn(Future.successful(mgdDetails))

        val ua =
          emptyUserAnswers
            .set(MgdTradeDetailsSectionPage, "MGD999999")
            .success
            .value
            .set(BusinessTradeClassPage, BusinessTradeClass.Other)
            .success
            .value
            .set(SeasonalBusinessPage, true)
            .success
            .value

        val application =
          applicationBuilder(userAnswers = Some(ua))
            .overrides(bind[GamblingConnector].toInstance(mockConnector))
            .build()

        running(application) {
          val request = FakeRequest(POST, routes.CheckTradingDetailsController.onContinue().url)
          val result = route(application, request).value

          redirectLocation(result).value mustBe
            routes.OtherTradeClassController.onPageLoad(NormalMode).url
        }
      }

      "must redirect to ChangeRegistrationDetails when all fields are present" in {
        val mockConnector = mock[GamblingConnector]

        when(mockConnector.getBusinessDetails(any())(any()))
          .thenReturn(Future.successful(businessDetails))

        val mgdDetails = MgdTradeDetails(
          mgdRegNumber                     = "MGD999999",
          isBusinessSeasonal               = Some(true),
          businessTradeClass               = Some(BusinessTradeClass.Other),
          businessActivityDesc             = Some("Arcade"),
          previousMgdRegistrationNumbers   = Some(Seq("MGD123")),
          associatedMgdRegistrationNumbers = Some(Seq("ASS456")),
          systemDate                       = Some(LocalDate.of(2026, 1, 1))
        )

        when(mockConnector.getMgdTradeDetails(any[String])(any()))
          .thenReturn(Future.successful(mgdDetails))

        val ua =
          emptyUserAnswers
            .set(MgdTradeDetailsSectionPage, "MGD999999")
            .success
            .value
            .set(BusinessTradeClassPage, BusinessTradeClass.Casino)
            .success
            .value
            .set(SeasonalBusinessPage, true)
            .success
            .value

        val application =
          applicationBuilder(userAnswers = Some(ua))
            .overrides(bind[GamblingConnector].toInstance(mockConnector))
            .build()

        running(application) {
          val request = FakeRequest(POST, routes.CheckTradingDetailsController.onContinue().url)
          val result = route(application, request).value

          redirectLocation(result).value mustBe
            routes.ChangeRegistrationDetailsController.onPageLoad().url
        }
      }
    }

  }
}
