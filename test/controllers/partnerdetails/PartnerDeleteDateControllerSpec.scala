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

package controllers.partner

import base.SpecBase
import connectors.GamblingConnector
import forms.partner.PartnerDeleteDateFormProvider
import models.{BusinessDetails, BusinessType, NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.partnerdetails.{ChosenPartnerToRemovePage, PartnerDetailsBusinessNamePage, PartnerDetailsDateOfLeavingPage, PartnerDetailsPage, PartnerDetailsTradingNamePage}
import play.api.Application
import play.api.i18n.{Lang, Messages}
import play.api.inject.bind
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import utils.DateTimeFormats.*
import views.html.partner.PartnerDeleteDateView

import java.time.{Clock, LocalDate, ZoneOffset}
import scala.concurrent.Future

class PartnerDeleteDateControllerSpec extends SpecBase with MockitoSugar {

  private implicit val msgs: Messages =
    stubMessages()

  private val today: LocalDate =
    LocalDate.of(2026, 9, 5)

  private val clock: Clock =
    Clock.fixed(
      today
        .atStartOfDay(ZoneOffset.UTC)
        .toInstant,
      ZoneOffset.UTC
    )

  private val partnerIndex: Int =
    0

  private val tradingName: String =
    "Test Trading Name"

  private val businessName: String =
    "Test Business Ltd"

  private val registrationDate: LocalDate =
    LocalDate.of(2020, 1, 1)

  private val validAnswer: LocalDate =
    today.plusDays(1)

  private val latestFormDate: LocalDate =
    today.plusDays(14)

  /*
   * The current controller uses LocalDate.now()
   * and adds 15 days for the value passed to the view.
   *
   * The form provider uses the injected Clock
   * and adds 14 days for validation.
   */
  private def latestControllerDisplayDate: LocalDate =
    LocalDate.now().plusDays(15)

  private val getRoute: String =
    controllers.partner.routes.PartnerDeleteDateController
      .onPageLoad()
      .url

  private val postRoute: String =
    controllers.partner.routes.PartnerDeleteDateController
      .onSubmit()
      .url

  override val emptyUserAnswers: UserAnswers =
    UserAnswers(userAnswersId)

  private val userAnswersWithTradingName: UserAnswers =
    emptyUserAnswers
      .set(
        PartnerDetailsPage(partnerIndex),
        userAnswersId
      )
      .success
      .value
      .set(
        ChosenPartnerToRemovePage,
        partnerIndex
      )
      .success
      .value
      .set(
        PartnerDetailsTradingNamePage(partnerIndex),
        tradingName
      )
      .success
      .value

  private val businessDetails: BusinessDetails =
    BusinessDetails(
      mgdRegNumber = userAnswersId,
      businessType = Some(
        BusinessType.Unincorporatedbody
      ),
      currentlyRegistered = 1,
      groupReg            = false,
      dateOfRegistration = Some(
        registrationDate
      ),
      businessPartnerNumber = Some(
        "XB1234567890"
      ),
      systemDate = LocalDate.of(
        2026,
        1,
        1
      )
    )

  private def controller(
    application: Application
  ): PartnerDeleteDateController =
    application.injector
      .instanceOf[
        PartnerDeleteDateController
      ]

  private def form(
    application: Application,
    dateOfRegistration: LocalDate = registrationDate
  ) =
    application.injector
      .instanceOf[
        PartnerDeleteDateFormProvider
      ]
      .apply(dateOfRegistration)

  private def view(
    application: Application
  ): PartnerDeleteDateView =
    application.injector
      .instanceOf[
        PartnerDeleteDateView
      ]

  private def getRequest(): FakeRequest[
    AnyContentAsEmpty.type
  ] =
    FakeRequest(
      GET,
      getRoute
    )

  private def postRequest(
    answer: LocalDate = validAnswer
  ): FakeRequest[
    AnyContentAsFormUrlEncoded
  ] =
    FakeRequest(
      POST,
      postRoute
    ).withFormUrlEncodedBody(
      "value.day" ->
        answer.getDayOfMonth.toString,
      "value.month" ->
        answer.getMonthValue.toString,
      "value.year" ->
        answer.getYear.toString
    )

  private def formData(
    answer: LocalDate
  ): Map[String, String] =
    Map(
      "value.day" ->
        answer.getDayOfMonth.toString,
      "value.month" ->
        answer.getMonthValue.toString,
      "value.year" ->
        answer.getYear.toString
    )

  private def mockConnectorReturning(
    details: BusinessDetails
  ): GamblingConnector = {

    val mockGamblingConnector =
      mock[GamblingConnector]

    when(
      mockGamblingConnector
        .getBusinessDetails(
          any[String]
        )(
          any[HeaderCarrier]
        )
    ).thenReturn(
      Future.successful(details)
    )

    mockGamblingConnector
  }

  private def buildApplication(
    userAnswers: UserAnswers,
    mockGamblingConnector: GamblingConnector,
    mockSessionRepository: Option[SessionRepository] = None
  ): Application = {

    val builder =
      applicationBuilder(
        userAnswers = Some(userAnswers)
      ).overrides(
        bind[Clock]
          .toInstance(clock),
        bind[GamblingConnector]
          .toInstance(
            mockGamblingConnector
          )
      )

    mockSessionRepository
      .fold(builder) { repository =>
        builder.overrides(
          bind[SessionRepository]
            .toInstance(repository)
        )
      }
      .build()
  }

  "PartnerDeleteDate Controller" - {

    "must return OK and the correct view for a GET" in {

      val mockGamblingConnector =
        mockConnectorReturning(
          businessDetails
        )

      val application =
        buildApplication(
          userAnswersWithTradingName,
          mockGamblingConnector
        )

      running(application) {

        implicit val lang: Lang =
          messages(application).lang

        val request =
          getRequest()

        val result =
          controller(application)
            .onPageLoad(NormalMode)
            .apply(request)

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(application)(
            form(application),
            NormalMode,
            registrationDate.format(
              dateTimeFormat()
            ),
            latestControllerDisplayDate
              .format(
                dateTimeHintFormat
              ),
            tradingName
          )(
            request,
            messages(application)
          ).toString

        verify(mockGamblingConnector)
          .getBusinessDetails(
            any[String]
          )(
            any[HeaderCarrier]
          )
      }
    }

    "must populate the view correctly on a GET when the date has previously been answered" in {

      val existingAnswer =
        today.plusDays(2)

      val userAnswers =
        userAnswersWithTradingName
          .set(
            PartnerDetailsDateOfLeavingPage(
              partnerIndex
            ),
            existingAnswer
          )
          .success
          .value

      val mockGamblingConnector =
        mockConnectorReturning(
          businessDetails
        )

      val application =
        buildApplication(
          userAnswers,
          mockGamblingConnector
        )

      running(application) {

        implicit val lang: Lang =
          messages(application).lang

        val request =
          getRequest()

        val result =
          controller(application)
            .onPageLoad(NormalMode)
            .apply(request)

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(application)(
            form(application)
              .fill(existingAnswer),
            NormalMode,
            registrationDate.format(
              dateTimeFormat()
            ),
            latestControllerDisplayDate
              .format(
                dateTimeHintFormat
              ),
            tradingName
          )(
            request,
            messages(application)
          ).toString
      }
    }

    "must use the trading name when both trading name and business name are available" in {

      val userAnswers =
        userAnswersWithTradingName
          .set(
            PartnerDetailsBusinessNamePage(
              partnerIndex
            ),
            businessName
          )
          .success
          .value

      val mockGamblingConnector =
        mockConnectorReturning(
          businessDetails
        )

      val application =
        buildApplication(
          userAnswers,
          mockGamblingConnector
        )

      running(application) {

        implicit val lang: Lang =
          messages(application).lang

        val request =
          getRequest()

        val result =
          controller(application)
            .onPageLoad(NormalMode)
            .apply(request)

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(application)(
            form(application),
            NormalMode,
            registrationDate.format(
              dateTimeFormat()
            ),
            latestControllerDisplayDate
              .format(
                dateTimeHintFormat
              ),
            tradingName
          )(
            request,
            messages(application)
          ).toString
      }
    }

    "must use the business name when the trading name is missing" in {

      val userAnswers =
        emptyUserAnswers
          .set(
            PartnerDetailsPage(
              partnerIndex
            ),
            userAnswersId
          )
          .success
          .value
          .set(
            ChosenPartnerToRemovePage,
            partnerIndex
          )
          .success
          .value
          .set(
            PartnerDetailsBusinessNamePage(
              partnerIndex
            ),
            businessName
          )
          .success
          .value

      val mockGamblingConnector =
        mockConnectorReturning(
          businessDetails
        )

      val application =
        buildApplication(
          userAnswers,
          mockGamblingConnector
        )

      running(application) {

        implicit val lang: Lang =
          messages(application).lang

        val request =
          getRequest()

        val result =
          controller(application)
            .onPageLoad(NormalMode)
            .apply(request)

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(application)(
            form(application),
            NormalMode,
            registrationDate.format(
              dateTimeFormat()
            ),
            latestControllerDisplayDate
              .format(
                dateTimeHintFormat
              ),
            businessName
          )(
            request,
            messages(application)
          ).toString
      }
    }

    "must use the MGD registration number when the trading name and business name are missing" in {

      val userAnswers =
        emptyUserAnswers
          .set(
            PartnerDetailsPage(
              partnerIndex
            ),
            userAnswersId
          )
          .success
          .value
          .set(
            ChosenPartnerToRemovePage,
            partnerIndex
          )
          .success
          .value

      val mockGamblingConnector =
        mockConnectorReturning(
          businessDetails
        )

      val application =
        buildApplication(
          userAnswers,
          mockGamblingConnector
        )

      running(application) {

        implicit val lang: Lang =
          messages(application).lang

        val request =
          getRequest()

        val result =
          controller(application)
            .onPageLoad(NormalMode)
            .apply(request)

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(application)(
            form(application),
            NormalMode,
            registrationDate.format(
              dateTimeFormat()
            ),
            latestControllerDisplayDate
              .format(
                dateTimeHintFormat
              ),
            userAnswersId
          )(
            request,
            messages(application)
          ).toString
      }
    }

    "must calculate the displayed latest date from today when registration date is before today" in {

      val mockGamblingConnector =
        mockConnectorReturning(
          businessDetails
        )

      val application =
        buildApplication(
          userAnswersWithTradingName,
          mockGamblingConnector
        )

      running(application) {

        implicit val lang: Lang =
          messages(application).lang

        val request =
          getRequest()

        val result =
          controller(application)
            .onPageLoad(NormalMode)
            .apply(request)

        status(result) mustEqual OK

        contentAsString(result) must include(
          latestControllerDisplayDate
            .format(dateTimeHintFormat)
        )
      }
    }

    "must calculate the displayed latest date from the registration date when registration date is in the future" in {

      val futureRegistrationDate =
        LocalDate.now().plusDays(30)

      val futureBusinessDetails =
        businessDetails.copy(
          dateOfRegistration = Some(
            futureRegistrationDate
          )
        )

      val mockGamblingConnector =
        mockConnectorReturning(
          futureBusinessDetails
        )

      val application =
        buildApplication(
          userAnswersWithTradingName,
          mockGamblingConnector
        )

      running(application) {

        implicit val lang: Lang =
          messages(application).lang

        val latestDisplayDate =
          futureRegistrationDate
            .plusDays(15)

        val request =
          getRequest()

        val result =
          controller(application)
            .onPageLoad(NormalMode)
            .apply(request)

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(application)(
            form(
              application,
              futureRegistrationDate
            ),
            NormalMode,
            futureRegistrationDate.format(
              dateTimeFormat()
            ),
            latestDisplayDate.format(
              dateTimeHintFormat
            ),
            tradingName
          )(
            request,
            messages(application)
          ).toString
      }
    }

    "must redirect to the partner details page when valid data is submitted" in {

      val mockSessionRepository =
        mock[SessionRepository]

      val mockGamblingConnector =
        mockConnectorReturning(
          businessDetails
        )

      when(
        mockSessionRepository.set(
          any[UserAnswers]
        )
      ).thenReturn(
        Future.successful(true)
      )

      val application =
        buildApplication(
          userAnswersWithTradingName,
          mockGamblingConnector,
          Some(mockSessionRepository)
        )

      running(application) {

        val request =
          postRequest(validAnswer)

        val result =
          controller(application)
            .onSubmit(NormalMode)
            .apply(request)

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.partner.routes.PartnerCheckConfirmRemoveDateController.onPageLoad().url

        verify(mockSessionRepository)
          .set(any[UserAnswers])
      }
    }

    "must save the submitted date against the selected partner index" in {

      val mockSessionRepository =
        mock[SessionRepository]

      val mockGamblingConnector =
        mockConnectorReturning(
          businessDetails
        )

      when(
        mockSessionRepository.set(
          any[UserAnswers]
        )
      ).thenReturn(
        Future.successful(true)
      )

      val application =
        buildApplication(
          userAnswersWithTradingName,
          mockGamblingConnector,
          Some(mockSessionRepository)
        )

      running(application) {

        val expectedUserAnswers =
          userAnswersWithTradingName
            .set(
              PartnerDetailsDateOfLeavingPage(
                partnerIndex
              ),
              validAnswer
            )
            .success
            .value

        val result =
          controller(application)
            .onSubmit(NormalMode)
            .apply(
              postRequest(validAnswer)
            )

        status(result) mustEqual SEE_OTHER

        verify(mockSessionRepository)
          .set(expectedUserAnswers)
      }
    }

    "must accept the latest permitted date" in {

      val mockSessionRepository =
        mock[SessionRepository]

      val mockGamblingConnector =
        mockConnectorReturning(
          businessDetails
        )

      when(
        mockSessionRepository.set(
          any[UserAnswers]
        )
      ).thenReturn(
        Future.successful(true)
      )

      val application =
        buildApplication(
          userAnswersWithTradingName,
          mockGamblingConnector,
          Some(mockSessionRepository)
        )

      running(application) {

        val result =
          controller(application)
            .onSubmit(NormalMode)
            .apply(
              postRequest(
                latestFormDate
              )
            )

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.partner.routes.PartnerCheckConfirmRemoveDateController.onPageLoad().url

        val expectedUserAnswers =
          userAnswersWithTradingName
            .set(
              PartnerDetailsDateOfLeavingPage(
                partnerIndex
              ),
              latestFormDate
            )
            .success
            .value

        verify(mockSessionRepository)
          .set(expectedUserAnswers)
      }
    }

    "must return a Bad Request when the submitted date is after the latest permitted date" in {

      val invalidAnswer =
        latestFormDate.plusDays(1)

      val mockSessionRepository =
        mock[SessionRepository]

      val mockGamblingConnector =
        mockConnectorReturning(
          businessDetails
        )

      val application =
        buildApplication(
          userAnswersWithTradingName,
          mockGamblingConnector,
          Some(mockSessionRepository)
        )

      running(application) {

        implicit val lang: Lang =
          messages(application).lang

        val request =
          postRequest(invalidAnswer)

        val boundForm =
          form(application)
            .bind(
              formData(invalidAnswer)
            )

        val result =
          controller(application)
            .onSubmit(NormalMode)
            .apply(request)

        status(result) mustEqual BAD_REQUEST

        boundForm.errors
          .map(_.message) must contain(
          "partnerDeleteDate.error.afterLatestDate"
        )

        contentAsString(result) mustEqual
          view(application)(
            boundForm,
            NormalMode,
            registrationDate.format(
              dateTimeFormat()
            ),
            latestControllerDisplayDate
              .format(
                dateTimeHintFormat
              ),
            tradingName
          )(
            request,
            messages(application)
          ).toString

        verify(
          mockSessionRepository,
          never()
        ).set(any[UserAnswers])
      }
    }

    "must return a Bad Request and the invalid date error when an invalid calendar date is submitted" in {

      val mockSessionRepository =
        mock[SessionRepository]

      val mockGamblingConnector =
        mockConnectorReturning(
          businessDetails
        )

      val application =
        buildApplication(
          userAnswersWithTradingName,
          mockGamblingConnector,
          Some(mockSessionRepository)
        )

      running(application) {

        implicit val lang: Lang =
          messages(application).lang

        val invalidData =
          Map(
            "value.day"   -> "31",
            "value.month" -> "2",
            "value.year"  -> "2026"
          )

        val request =
          FakeRequest(
            POST,
            postRoute
          ).withFormUrlEncodedBody(
            invalidData.toSeq*
          )

        val boundForm =
          form(application)
            .bind(invalidData)

        val result =
          controller(application)
            .onSubmit(NormalMode)
            .apply(request)

        status(result) mustEqual BAD_REQUEST

        boundForm.errors
          .map(_.message) must contain(
          "partnerDeleteDate.error.invalid"
        )

        contentAsString(result) mustEqual
          view(application)(
            boundForm,
            NormalMode,
            registrationDate.format(
              dateTimeFormat()
            ),
            latestControllerDisplayDate
              .format(
                dateTimeHintFormat
              ),
            tradingName
          )(
            request,
            messages(application)
          ).toString

        verify(
          mockSessionRepository,
          never()
        ).set(any[UserAnswers])
      }
    }

    "must return a Bad Request and the required error when no date is submitted" in {

      val mockSessionRepository =
        mock[SessionRepository]

      val mockGamblingConnector =
        mockConnectorReturning(
          businessDetails
        )

      val application =
        buildApplication(
          userAnswersWithTradingName,
          mockGamblingConnector,
          Some(mockSessionRepository)
        )

      running(application) {

        implicit val lang: Lang =
          messages(application).lang

        val emptyData =
          Map(
            "value.day"   -> "",
            "value.month" -> "",
            "value.year"  -> ""
          )

        val request =
          FakeRequest(
            POST,
            postRoute
          ).withFormUrlEncodedBody(
            emptyData.toSeq*
          )

        val boundForm =
          form(application)
            .bind(emptyData)

        val result =
          controller(application)
            .onSubmit(NormalMode)
            .apply(request)

        status(result) mustEqual BAD_REQUEST

        boundForm.errors
          .map(_.message) must contain(
          "partnerDeleteDate.error.required.all"
        )

        contentAsString(result) mustEqual
          view(application)(
            boundForm,
            NormalMode,
            registrationDate.format(
              dateTimeFormat()
            ),
            latestControllerDisplayDate
              .format(
                dateTimeHintFormat
              ),
            tradingName
          )(
            request,
            messages(application)
          ).toString

        verify(
          mockSessionRepository,
          never()
        ).set(any[UserAnswers])
      }
    }

    "must propagate an exception when the backend does not return a registration date" in {

      val mockGamblingConnector =
        mockConnectorReturning(
          businessDetails.copy(
            dateOfRegistration = None
          )
        )

      val application =
        buildApplication(
          userAnswersWithTradingName,
          mockGamblingConnector
        )

      running(application) {

        val result =
          controller(application)
            .onPageLoad(NormalMode)
            .apply(getRequest())

        val exception =
          intercept[RuntimeException] {
            await(result)
          }

        exception.getMessage mustEqual
          "No registration date found"
      }
    }

    "must propagate an exception on GET when no partner has been selected for removal" in {

      val userAnswers =
        emptyUserAnswers
          .set(
            PartnerDetailsPage(
              partnerIndex
            ),
            userAnswersId
          )
          .success
          .value

      val mockGamblingConnector =
        mockConnectorReturning(
          businessDetails
        )

      val application =
        buildApplication(
          userAnswers,
          mockGamblingConnector
        )

      running(application) {

        val result =
          controller(application)
            .onPageLoad(NormalMode)
            .apply(getRequest())

        val exception =
          intercept[RuntimeException] {
            await(result)
          }

        exception.getMessage mustEqual
          "No selected partner for removal"
      }
    }

    "must propagate an exception on POST when no partner has been selected for removal" in {

      val userAnswers =
        emptyUserAnswers
          .set(
            PartnerDetailsPage(
              partnerIndex
            ),
            userAnswersId
          )
          .success
          .value

      val mockGamblingConnector =
        mockConnectorReturning(
          businessDetails
        )

      val application =
        buildApplication(
          userAnswers,
          mockGamblingConnector
        )

      running(application) {

        val result =
          controller(application)
            .onSubmit(NormalMode)
            .apply(postRequest())

        val exception =
          intercept[RuntimeException] {
            await(result)
          }

        exception.getMessage mustEqual
          "No selected partner for removal"
      }
    }

    "must propagate the connector failure on GET" in {

      val mockGamblingConnector =
        mock[GamblingConnector]

      when(
        mockGamblingConnector
          .getBusinessDetails(
            any[String]
          )(
            any[HeaderCarrier]
          )
      ).thenReturn(
        Future.failed(
          new RuntimeException(
            "Unable to retrieve business details"
          )
        )
      )

      val application =
        buildApplication(
          userAnswersWithTradingName,
          mockGamblingConnector
        )

      running(application) {

        val result =
          controller(application)
            .onPageLoad(NormalMode)
            .apply(getRequest())

        val exception =
          intercept[RuntimeException] {
            await(result)
          }

        exception.getMessage mustEqual
          "Unable to retrieve business details"
      }
    }

    "must propagate the connector failure on POST" in {

      val mockGamblingConnector =
        mock[GamblingConnector]

      when(
        mockGamblingConnector
          .getBusinessDetails(
            any[String]
          )(
            any[HeaderCarrier]
          )
      ).thenReturn(
        Future.failed(
          new RuntimeException(
            "Unable to retrieve business details"
          )
        )
      )

      val application =
        buildApplication(
          userAnswersWithTradingName,
          mockGamblingConnector
        )

      running(application) {

        val result =
          controller(application)
            .onSubmit(NormalMode)
            .apply(postRequest())

        val exception =
          intercept[RuntimeException] {
            await(result)
          }

        exception.getMessage mustEqual
          "Unable to retrieve business details"
      }
    }

    "must redirect to SystemError for a GET when no existing data can be loaded" in {

      val application =
        applicationBuilder(
          userAnswers = None
        )
          .overrides(
            bind[Clock]
              .toInstance(clock)
          )
          .build()

      running(application) {

        val result =
          controller(application)
            .onPageLoad(NormalMode)
            .apply(getRequest())

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.routes.SystemErrorController
            .onPageLoad()
            .url
      }
    }

    "must redirect to SystemError for a POST when no existing data can be loaded" in {

      val application =
        applicationBuilder(
          userAnswers = None
        )
          .overrides(
            bind[Clock]
              .toInstance(clock)
          )
          .build()

      running(application) {

        val result =
          controller(application)
            .onSubmit(NormalMode)
            .apply(postRequest())

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.routes.SystemErrorController
            .onPageLoad()
            .url
      }
    }
  }
}
