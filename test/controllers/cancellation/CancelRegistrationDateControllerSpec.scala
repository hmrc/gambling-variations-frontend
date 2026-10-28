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
import connectors.GamblingConnector
import controllers.actions.{BusinessDetailsDataRequiredAction, BusinessDetailsDataRequiredActionImpl}
import controllers.routes
import forms.cancellation.CancelRegistrationDateFormProvider
import models.{BusinessDetails, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.cancellation.CancelRegistrationDatePage
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded, Call}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import views.html.cancellation.CancelRegistrationDateView

import java.time.temporal.ChronoUnit
import java.time.{Clock, Instant, LocalDate, ZoneOffset}
import scala.concurrent.Future

class CancelRegistrationDateControllerSpec extends SpecBase with MockitoSugar {

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

  private val validAnswer =
    LocalDate.of(2017, 1, 27)

  private val formProvider =
    new CancelRegistrationDateFormProvider()

  private def form =
    formProvider(
      registrationDate,
      maximumValidDate
    )

  private val onwardRoute =
    Call("GET", "/foo")

  private lazy val cancelRegistrationDateRoute =
    controllers.cancellation.routes.CancelRegistrationDateController
      .onPageLoad(NormalMode)
      .url

  override val emptyUserAnswers =
    UserAnswers(userAnswersId)

  private def getRequest(): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(
      GET,
      cancelRegistrationDateRoute
    )

  private def postRequest(
    answer: LocalDate = validAnswer
  ): FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest(
      POST,
      cancelRegistrationDateRoute
    ).withFormUrlEncodedBody(
      "value.day"   -> answer.getDayOfMonth.toString,
      "value.month" -> answer.getMonthValue.toString,
      "value.year"  -> answer.getYear.toString
    )

  private def buildApplication(
    userAnswers: Option[UserAnswers],
    businessDetails: BusinessDetails,
    sessionRepository: SessionRepository = mock[SessionRepository],
    navigator: Navigator = new FakeNavigator(onwardRoute)
  ) = {
    val gamblingConnector =
      mock[GamblingConnector]

    when(
      gamblingConnector.getBusinessDetails(any[String])(
        any[HeaderCarrier]
      )
    ).thenReturn(
      Future.successful(businessDetails)
    )

    when(
      sessionRepository.set(any[UserAnswers])
    ).thenReturn(
      Future.successful(true)
    )

    applicationBuilder(userAnswers = userAnswers)
      .overrides(
        bind[GamblingConnector]
          .toInstance(gamblingConnector),
        bind[SessionRepository]
          .toInstance(sessionRepository),
        bind[Navigator]
          .toInstance(navigator),
        bind[Clock]
          .toInstance(fixedClock),
        bind[BusinessDetailsDataRequiredAction]
          .to[BusinessDetailsDataRequiredActionImpl]
      )
      .build()
  }

  private def businessDetails(
    dateOfRegistration: Option[LocalDate]
  ): BusinessDetails =
    BusinessDetails(
      mgdRegNumber          = "XMGD00000000001",
      businessType          = None,
      currentlyRegistered   = 1,
      groupReg              = false,
      dateOfRegistration    = dateOfRegistration,
      businessPartnerNumber = None,
      systemDate            = currentDate
    )

  private def businessDetailsWithRegistrationDate: BusinessDetails =
    businessDetails(Some(registrationDate))

  private def businessDetailsWithoutRegistrationDate: BusinessDetails =
    businessDetails(None)

  "CancelRegistrationDateController" - {

    "onPageLoad" - {

      "must return OK and the correct view for a GET" in {
        val application =
          buildApplication(
            userAnswers     = Some(emptyUserAnswers),
            businessDetails = businessDetailsWithRegistrationDate
          )

        running(application) {
          val request =
            getRequest()

          val result =
            route(application, request).value

          val view =
            application.injector
              .instanceOf[CancelRegistrationDateView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(
              form,
              NormalMode,
              registrationDate,
              firstInvalidDate,
              middleOfDateRange
            )(
              request,
              messages(application)
            ).toString
        }
      }

      "must populate the view when the question has previously been answered" in {
        val userAnswers =
          UserAnswers(userAnswersId)
            .set(
              CancelRegistrationDatePage,
              validAnswer
            )
            .success
            .value

        val application =
          buildApplication(
            userAnswers     = Some(userAnswers),
            businessDetails = businessDetailsWithRegistrationDate
          )

        running(application) {
          val request =
            getRequest()

          val result =
            route(application, request).value

          val view =
            application.injector
              .instanceOf[CancelRegistrationDateView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(
              form.fill(validAnswer),
              NormalMode,
              registrationDate,
              firstInvalidDate,
              middleOfDateRange
            )(
              request,
              messages(application)
            ).toString
        }
      }

      "must redirect to SystemError when the business details do not contain a registration date" in {
        val application =
          buildApplication(
            userAnswers     = Some(emptyUserAnswers),
            businessDetails = businessDetailsWithoutRegistrationDate
          )

        running(application) {
          val result =
            route(application, getRequest()).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            routes.SystemErrorController
              .onPageLoad()
              .url
        }
      }

      "must return OK when no existing user answers are found" in {
        val application =
          buildApplication(
            userAnswers     = None,
            businessDetails = businessDetailsWithRegistrationDate
          )

        running(application) {
          val result =
            route(application, getRequest()).value

          status(result) mustEqual OK
        }
      }
    }

    "onSubmit" - {

      "must save the answer and redirect to the next page when valid data is submitted" in {
        val mockSessionRepository =
          mock[SessionRepository]

        when(
          mockSessionRepository.set(any[UserAnswers])
        ).thenReturn(
          Future.successful(true)
        )

        val application =
          buildApplication(
            userAnswers       = Some(emptyUserAnswers),
            businessDetails   = businessDetailsWithRegistrationDate,
            sessionRepository = mockSessionRepository
          )

        running(application) {
          val result =
            route(application, postRequest()).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            onwardRoute.url

          verify(mockSessionRepository).set(
            org.mockito.ArgumentMatchers.argThat[UserAnswers] { answers =>
              answers.get(CancelRegistrationDatePage).contains(validAnswer)
            }
          )
        }
      }

      "must accept the registration date because the lower boundary is inclusive" in {
        val mockSessionRepository =
          mock[SessionRepository]

        when(
          mockSessionRepository.set(any[UserAnswers])
        ).thenReturn(
          Future.successful(true)
        )

        val application =
          buildApplication(
            userAnswers       = Some(emptyUserAnswers),
            businessDetails   = businessDetailsWithRegistrationDate,
            sessionRepository = mockSessionRepository
          )

        running(application) {
          val result =
            route(
              application,
              postRequest(registrationDate)
            ).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            onwardRoute.url
        }
      }

      "must accept today plus 14 days because the upper boundary is inclusive" in {
        val mockSessionRepository =
          mock[SessionRepository]

        when(
          mockSessionRepository.set(any[UserAnswers])
        ).thenReturn(
          Future.successful(true)
        )

        val application =
          buildApplication(
            userAnswers       = Some(emptyUserAnswers),
            businessDetails   = businessDetailsWithRegistrationDate,
            sessionRepository = mockSessionRepository
          )

        running(application) {
          val result =
            route(
              application,
              postRequest(maximumValidDate)
            ).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            onwardRoute.url
        }
      }

      "must return BadRequest when a date before registration is submitted" in {
        val application =
          buildApplication(
            userAnswers     = Some(emptyUserAnswers),
            businessDetails = businessDetailsWithRegistrationDate
          )

        running(application) {
          val invalidDate =
            registrationDate.minusDays(1)

          val request =
            postRequest(invalidDate)

          val boundForm =
            form.bind(
              Map(
                "value.day"   -> invalidDate.getDayOfMonth.toString,
                "value.month" -> invalidDate.getMonthValue.toString,
                "value.year"  -> invalidDate.getYear.toString
              )
            )

          val result =
            route(application, request).value

          val view =
            application.injector
              .instanceOf[CancelRegistrationDateView]

          status(result) mustEqual BAD_REQUEST

          contentAsString(result) mustEqual
            view(
              boundForm,
              NormalMode,
              registrationDate,
              firstInvalidDate,
              middleOfDateRange
            )(
              request,
              messages(application)
            ).toString
        }
      }

      "must return BadRequest for today plus 15 days because it is the first invalid date" in {
        val application =
          buildApplication(
            userAnswers     = Some(emptyUserAnswers),
            businessDetails = businessDetailsWithRegistrationDate
          )

        running(application) {
          val request =
            postRequest(firstInvalidDate)

          val boundForm =
            form.bind(
              Map(
                "value.day"   -> firstInvalidDate.getDayOfMonth.toString,
                "value.month" -> firstInvalidDate.getMonthValue.toString,
                "value.year"  -> firstInvalidDate.getYear.toString
              )
            )

          val result =
            route(application, request).value

          val view =
            application.injector
              .instanceOf[CancelRegistrationDateView]

          status(result) mustEqual BAD_REQUEST

          contentAsString(result) mustEqual
            view(
              boundForm,
              NormalMode,
              registrationDate,
              firstInvalidDate,
              middleOfDateRange
            )(
              request,
              messages(application)
            ).toString
        }
      }

      "must return BadRequest and the correct view when invalid data is submitted" in {
        val application =
          buildApplication(
            userAnswers     = Some(emptyUserAnswers),
            businessDetails = businessDetailsWithRegistrationDate
          )

        val request =
          FakeRequest(
            POST,
            cancelRegistrationDateRoute
          ).withFormUrlEncodedBody(
            "value.day"   -> "not-a-day",
            "value.month" -> "10",
            "value.year"  -> "2026"
          )

        running(application) {
          val boundForm =
            form.bind(
              Map(
                "value.day"   -> "not-a-day",
                "value.month" -> "10",
                "value.year"  -> "2026"
              )
            )

          val view =
            application.injector
              .instanceOf[CancelRegistrationDateView]

          val result =
            route(application, request).value

          status(result) mustEqual BAD_REQUEST

          contentAsString(result) mustEqual
            view(
              boundForm,
              NormalMode,
              registrationDate,
              firstInvalidDate,
              middleOfDateRange
            )(
              request,
              messages(application)
            ).toString
        }
      }

      "must redirect to SystemError when the business details do not contain a registration date" in {
        val application =
          buildApplication(
            userAnswers     = Some(emptyUserAnswers),
            businessDetails = businessDetailsWithoutRegistrationDate
          )

        running(application) {
          val result =
            route(application, postRequest()).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            routes.SystemErrorController
              .onPageLoad()
              .url
        }
      }

      "must save the answer and redirect to the next page when no existing user answers are found" in {
        val mockSessionRepository =
          mock[SessionRepository]

        when(
          mockSessionRepository.set(any[UserAnswers])
        ).thenReturn(
          Future.successful(true)
        )

        val application =
          buildApplication(
            userAnswers       = None,
            businessDetails   = businessDetailsWithRegistrationDate,
            sessionRepository = mockSessionRepository
          )

        running(application) {
          val result =
            route(application, postRequest()).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            onwardRoute.url

          verify(mockSessionRepository).set(
            org.mockito.ArgumentMatchers.argThat[UserAnswers] { answers =>
              answers.get(CancelRegistrationDatePage).contains(validAnswer)
            }
          )
        }
      }
    }
  }
}
