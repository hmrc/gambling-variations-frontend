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
import config.FrontendAppConfig
import models.{BusinessDetails, BusinessType}
import org.mockito.ArgumentMatchers.{any, anyString}
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.BusinessNameChangesPage
import play.api.inject.bind
import play.api.i18n.Messages
import play.api.mvc.Request
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.BusinessDetailsService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import viewmodels.{NoChange, ReadyToSubmit, TaskListItem, TaskStatus}
import views.html.ChangeRegistrationDetailsView

import java.time.LocalDate
import scala.concurrent.Future

class ChangeRegistrationDetailsControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  private val mockBusinessDetailsService: BusinessDetailsService = mock[BusinessDetailsService]

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

  // Mirrors your controller's buildTaskList() logic exactly
  private def expectedTasks(
    isGroupMember: Boolean,
    isPartnership: Boolean,
    businessNameChanged: Boolean
  )(implicit messages: Messages): Seq[TaskListItem] = {

    val licencesChanged = false // same as controller
    val premisesExists = false
    val premisesTriggered = licencesChanged

    def status(flag: Boolean): TaskStatus =
      if (flag) ReadyToSubmit else NoChange

    Seq(
      if (!isGroupMember)
        Some(
          TaskListItem(
            messages("changeRegistrationDetails.businessName"),
            routes.CheckBusinessNameController.onPageLoad().url,
            status(businessNameChanged)
          )
        )
      else None,
      if (!isGroupMember)
        Some(
          TaskListItem(
            messages("changeRegistrationDetails.businessAddress"),
            routes.IndexController.onPageLoad().url,
            NoChange
          )
        )
      else None,
      if (!isGroupMember)
        Some(
          TaskListItem(
            messages("changeRegistrationDetails.businessContactDetails"),
            routes.IndexController.onPageLoad().url,
            NoChange
          )
        )
      else None,
      Some(
        TaskListItem(
          messages("changeRegistrationDetails.correspondenceDetails"),
          routes.IndexController.onPageLoad().url,
          NoChange
        )
      ),
      Some(
        TaskListItem(
          messages("changeRegistrationDetails.tradingDetails"),
          routes.IndexController.onPageLoad().url,
          NoChange
        )
      ),
      Some(
        TaskListItem(
          messages("changeRegistrationDetails.returnPeriod"),
          routes.IndexController.onPageLoad().url,
          NoChange
        )
      ),
      if (isPartnership)
        Some(
          TaskListItem(
            messages("changeRegistrationDetails.partnerDetails"),
            routes.IndexController.onPageLoad().url,
            NoChange
          )
        )
      else None,
      if (isGroupMember)
        Some(
          TaskListItem(
            messages("changeRegistrationDetails.groupMemberDetails"),
            "group-member-details",
            NoChange
          )
        )
      else None,
      if (isGroupMember)
        Some(
          TaskListItem(
            messages("changeRegistrationDetails.controllingBodyDetails"),
            "group-member-details",
            NoChange
          )
        )
      else None,
      if (isGroupMember)
        Some(
          TaskListItem(
            messages("changeRegistrationDetails.disbandMGDGroup"),
            "group-member-details",
            NoChange
          )
        )
      else None,
      if (!isGroupMember)
        Some(
          TaskListItem(
            messages("changeRegistrationDetails.premises"),
            routes.IndexController.onPageLoad().url,
            status(licencesChanged)
          )
        )
      else None,
      if (!isGroupMember)
        Some(
          TaskListItem(
            messages("changeRegistrationDetails.licences"),
            routes.IndexController.onPageLoad().url,
            status(licencesChanged)
          )
        )
      else None,
      if (!isGroupMember && premisesTriggered)
        Some(
          TaskListItem(
            messages("changeRegistrationDetails.premises"),
            "premises",
            if (premisesExists) NoChange else viewmodels.NotStarted
          )
        )
      else None
    ).flatten
  }

  "ChangeRegistrationDetailsController" - {

    "must return OK and the correct view for a GET" - {

      "when non-group member AND partnership AND business name changed (canStart = true)" in {

        val userAnswers =
          emptyUserAnswers.set(BusinessNameChangesPage, true).success.value

        val application = applicationWithMocks(Some(userAnswers))

        running(application) {
          implicit val request =
            FakeRequest(GET, routes.ChangeRegistrationDetailsController.onPageLoad().url)

          given HeaderCarrier =
            HeaderCarrierConverter.fromRequestAndSession(request, request.session)

          // If your service signature includes `given Request[?]`, keep this:
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

          val view = application.injector.instanceOf[ChangeRegistrationDetailsView]
          val appConfig = application.injector.instanceOf[FrontendAppConfig]
          implicit val msgs: Messages = messages(application)

          val tasks = expectedTasks(isGroupMember = false, isPartnership = true, businessNameChanged = true)
          val canStart = tasks.exists(_.status == ReadyToSubmit)

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(
            capturedMgdReg,
            appConfig.gamblingManagementHomeUrl,
            tasks,
            canStart
          )(request, msgs).toString

          verify(mockBusinessDetailsService).retrieveBusinessDetails(anyString())(
            any[HeaderCarrier],
            any[Request[?]]
          )
        }
      }

      "when group member (non-group tasks hidden; group tasks shown; canStart = false)" in {

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
                groupReg     = true,
                businessType = None
              )
            )
          }

          val result = route(application, request).value

          val view = application.injector.instanceOf[ChangeRegistrationDetailsView]
          val appConfig = application.injector.instanceOf[FrontendAppConfig]
          implicit val msgs: Messages = messages(application)

          val tasks = expectedTasks(isGroupMember = true, isPartnership = false, businessNameChanged = true)
          val canStart = tasks.exists(_.status == ReadyToSubmit)

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(
            capturedMgdReg,
            appConfig.gamblingManagementHomeUrl,
            tasks,
            canStart
          )(request, msgs).toString

          verify(mockBusinessDetailsService).retrieveBusinessDetails(anyString())(
            any[HeaderCarrier],
            any[Request[?]]
          )
        }
      }
    }

    "must redirect to SystemErrorController when BusinessDetailsService fails" in {

      val userAnswers =
        emptyUserAnswers.set(BusinessNameChangesPage, false).success.value

      val application = applicationWithMocks(Some(userAnswers))

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
        redirectLocation(result).value mustEqual routes.SystemErrorController.onPageLoad().url
      }
    }
  }
}
