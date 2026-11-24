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

package controllers.cancellation

import base.SpecBase
import forms.cancellation.CancelRegistrationDateFormProvider
import models.{BusinessType, RegistrationStatus, UserAnswers}
import org.scalatestplus.mockito.MockitoSugar
import pages.cancellation.{CancelRegistrationDatePage, RegistrationCancellationDeclarationPage}
import pages.{BusinessTypePage, DateOfRegistrationPage, GroupMemberPage, RegistrationStatusPage}
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.cancellation.{CancelRegistrationDateView, RegistrationCancellationLockedView, RegistrationCancellationWarningView}

import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.{Clock, Instant, LocalDate, ZoneOffset}

class CancellationEntrypointDeciderSpec extends SpecBase with MockitoSugar {

  private val gamblingUrl = "http://localhost:10400/gambling/"

  private val dateFormatter =
    DateTimeFormatter.ofPattern("d MMMM yyyy")

  private implicit val messages: Messages =
    stubMessages()

  private val fixedInstant =
    Instant.parse("2026-10-21T12:00:00Z")

  private val fixedClock =
    Clock.fixed(fixedInstant, ZoneOffset.UTC)

  private val currentDate =
    LocalDate.of(2026, 10, 21)

  private val registrationDate =
    LocalDate.of(2007, 3, 20)

  private val dateOfCancellation: LocalDate =
    LocalDate.of(2022, 1, 15)

  private val maximumValidDate =
    currentDate.plusDays(14)

  private val firstInvalidDate =
    currentDate.plusDays(15)

  private val middleOfDateRange = {
    val numberOfDays =
      ChronoUnit.DAYS.between(
        registrationDate,
        firstInvalidDate
      )

    registrationDate.plusDays(numberOfDays / 2)
  }

  def onwardRoute: Call = Call("GET", "/foo")

  private val messageKey: String = "cancelGroupRegistrationWarning"
  private val isGroupFlag: Boolean = true

  override val emptyUserAnswers: UserAnswers = super.emptyUserAnswers
    .setOriginal(BusinessTypePage, BusinessType.Soleproprietor)
    .success
    .value
    .setOriginal(GroupMemberPage, isGroupFlag)
    .success
    .value
    .setOriginal(DateOfRegistrationPage, registrationDate)
    .success
    .value
    .setOriginal(CancelRegistrationDatePage, dateOfCancellation)
    .success
    .value

  private val userAnswersHmrcCancelled = emptyUserAnswers.setOriginal(RegistrationStatusPage, RegistrationStatus.Cancelled).success.value
  private val userAnswersUserCancelled = emptyUserAnswers.setOriginal(RegistrationCancellationDeclarationPage, true).success.value
  private val userAnswersBothCancelled = emptyUserAnswers
    .setOriginal(RegistrationStatusPage, RegistrationStatus.Cancelled)
    .success
    .value
    .setOriginal(RegistrationCancellationDeclarationPage, true)
    .success
    .value

  private val userAnswersNoCancel = emptyUserAnswers
    .setOriginal(RegistrationCancellationDeclarationPage, false)
    .success
    .value
    .setOriginal(RegistrationStatusPage, RegistrationStatus.Registered)
    .success
    .value

  lazy val cancelRegistrationDateRoute: String =
    controllers.cancellation.routes.CancelRegistrationDateController.onPageLoad().url

  lazy val registrationCancellationLockedRoute: String =
    controllers.cancellation.routes.RegistrationCancellationLockedController.onPageLoad().url

  lazy val registrationCancellationDeclarationRoute: String =
    controllers.cancellation.routes.RegistrationCancellationWarningController.onPageLoad().url

  private val formProvider = new CancelRegistrationDateFormProvider()

  private def form = formProvider(
    registrationDate,
    maximumValidDate
  )

  "DateController" - {

    "must return OK and the correct view for a GET" ignore {
      val application = applicationBuilder(userAnswers = Some(userAnswersNoCancel))
        .overrides(
          bind[Clock]
            .toInstance(fixedClock)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.cancellation.routes.CancelRegistrationDateController.onPageLoad().url)

        val result = route(application, request).value

        val view =
          application.injector
            .instanceOf[CancelRegistrationDateView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(
            form,
            registrationDate,
            firstInvalidDate,
            middleOfDateRange,
            messageKey,
            isGroupFlag
          )(
            request,
            messages(application)
          ).toString
      }

    }

    "must redirect and return OK and the correct view for a GET if hmrc has already cancelled" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersHmrcCancelled))
        .overrides(
          bind[Clock]
            .toInstance(fixedClock)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.cancellation.routes.CancelRegistrationDateController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.cancellation.routes.RegistrationCancellationLockedController.onPageLoad().url
      }
    }

    "must redirect and return OK and the correct view for a GET if user has already cancelled" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersUserCancelled))
        .overrides(
          bind[Clock]
            .toInstance(fixedClock)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.cancellation.routes.CancelRegistrationDateController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.cancellation.routes.RegistrationCancellationWarningController.onPageLoad().url
      }
    }

    "must redirect and return OK and the correct view for a GET if hmrc and user have already cancelled" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersBothCancelled))
        .overrides(
          bind[Clock]
            .toInstance(fixedClock)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.cancellation.routes.CancelRegistrationDateController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.cancellation.routes.RegistrationCancellationLockedController.onPageLoad().url
      }
    }

  }

  "WarningController" - {

    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersUserCancelled))
        .overrides(
          bind[Clock]
            .toInstance(fixedClock)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.cancellation.routes.RegistrationCancellationWarningController.onPageLoad().url)

        val result = route(application, request).value

        val view =
          application.injector
            .instanceOf[RegistrationCancellationWarningView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(
            form,
            dateFormatter.format(dateOfCancellation),
            messageKey
          )(
            request,
            messages(application)
          ).toString
      }

    }

    "must redirect and return OK and the correct view for a GET if hmrc has already cancelled" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersHmrcCancelled))
        .overrides(
          bind[Clock]
            .toInstance(fixedClock)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.cancellation.routes.RegistrationCancellationWarningController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.cancellation.routes.RegistrationCancellationLockedController.onPageLoad().url
      }
    }

    "must redirect and return OK and the correct view for a GET if user has did not cancel" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersNoCancel))
        .overrides(
          bind[Clock]
            .toInstance(fixedClock)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.cancellation.routes.RegistrationCancellationWarningController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.cancellation.routes.CancelRegistrationDateController.onPageLoad().url
      }
    }

    "must redirect and return OK and the correct view for a GET if hmrc and user have already cancelled" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersBothCancelled))
        .overrides(
          bind[Clock]
            .toInstance(fixedClock)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.cancellation.routes.RegistrationCancellationWarningController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.cancellation.routes.RegistrationCancellationLockedController.onPageLoad().url
      }
    }

  }

  "LockedController" - {

    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersHmrcCancelled))
        .overrides(
          bind[Clock]
            .toInstance(fixedClock)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.cancellation.routes.RegistrationCancellationLockedController.onPageLoad().url)

        val result = route(application, request).value

        val view =
          application.injector
            .instanceOf[RegistrationCancellationLockedView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(gamblingUrl, "msg")(request, messages(application)).toString
      }

    }

    "must redirect and return OK and the correct view for a GET if the user did not cancel" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersNoCancel))
        .overrides(
          bind[Clock]
            .toInstance(fixedClock)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.cancellation.routes.RegistrationCancellationLockedController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.cancellation.routes.CancelRegistrationDateController.onPageLoad().url
      }
    }

    "must redirect and return OK and the correct view for a GET if user has already cancelled" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersUserCancelled))
        .overrides(
          bind[Clock]
            .toInstance(fixedClock)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.cancellation.routes.RegistrationCancellationLockedController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.cancellation.routes.RegistrationCancellationWarningController.onPageLoad().url
      }
    }

    "must not redirect and return OK and the correct view for a GET if hmrc and user have already cancelled" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersBothCancelled))
        .overrides(
          bind[Clock]
            .toInstance(fixedClock)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.cancellation.routes.RegistrationCancellationLockedController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual OK
      }
    }

  }

}
