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
import controllers.partner.PartnerUtils.getIndex
import forms.partner.PartnerDateOfJoiningFormProvider
import models.{BusinessDetails, BusinessType, NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.partner.PartnerDetailsAddPartnerCompletedPage
import pages.partnerdetails.{PartnerDetailsDateOfJoiningPage, PartnerDetailsPage}
import play.api.Application
import play.api.i18n.{Lang, Messages}
import play.api.inject.bind
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import utils.DateTimeFormats.{dateTimeFormat, dateTimeHintFormat}
import views.html.partner.PartnerDateOfJoiningView

import java.time.LocalDate
import scala.concurrent.Future

class PartnerDateOfJoiningControllerSpec extends SpecBase with MockitoSugar {

  private val registrationDate: LocalDate =
    LocalDate.of(2020, 1, 1)

  private val validAnswer: LocalDate =
    LocalDate.now().plusDays(1)

  private val getRoute: String =
    controllers.partner.routes.PartnerDateOfJoiningController
      .onPageLoad()
      .url

  private val postRoute: String =
    controllers.partner.routes.PartnerDateOfJoiningController
      .onSubmit()
      .url

  override val emptyUserAnswers: UserAnswers =
    UserAnswers(userAnswersId)

  private val partnerDetailsUserAnswers: UserAnswers =
    emptyUserAnswers
      .set(
        PartnerDetailsPage(0),
        userAnswersId
      )
      .success
      .value
      .set(
        PartnerDetailsAddPartnerCompletedPage,
        false
      )
      .success
      .value

  private val index: Int =
    partnerDetailsUserAnswers.getIndex

  private val businessDetails: BusinessDetails =
    BusinessDetails(
      mgdRegNumber          = userAnswersId,
      businessType          = Some(BusinessType.Unincorporatedbody),
      currentlyRegistered   = 1,
      groupReg              = false,
      dateOfRegistration    = Some(registrationDate),
      businessPartnerNumber = Some("XB1234567890"),
      systemDate            = LocalDate.of(2026, 1, 1)
    )

  private def controller(
    application: Application
  ): PartnerDateOfJoiningController =
    application.injector
      .instanceOf[PartnerDateOfJoiningController]

  private def form(
    application: Application,
    dateOfRegistration: LocalDate = registrationDate
  )(implicit messages: Messages) =
    application.injector
      .instanceOf[PartnerDateOfJoiningFormProvider]
      .apply(dateOfRegistration)

  private def view(
    application: Application
  ): PartnerDateOfJoiningView =
    application.injector
      .instanceOf[PartnerDateOfJoiningView]

  private def getRequest(): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(
      GET,
      getRoute
    )

  private def postRequest(
    answer: LocalDate = validAnswer
  ): FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest(
      POST,
      postRoute
    ).withFormUrlEncodedBody(
      "value.day"   -> answer.getDayOfMonth.toString,
      "value.month" -> answer.getMonthValue.toString,
      "value.year"  -> answer.getYear.toString
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

  private def mockConnectorFailing(
    message: String
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
      Future.failed(
        new RuntimeException(message)
      )
    )

    mockGamblingConnector
  }

  private def mockConnectorFailingToGetPartnerDetails(): GamblingConnector = {

    val mockGamblingConnector =
      mock[GamblingConnector]

    when(
      mockGamblingConnector
        .getPartnersDetails(
          any[String]
        )(
          any[HeaderCarrier]
        )
    ).thenReturn(
      Future.failed(
        new RuntimeException(
          "Unable to retrieve partner details"
        )
      )
    )

    mockGamblingConnector
  }

  private def buildApplication(
    answers: Option[UserAnswers],
    mockGamblingConnector: GamblingConnector,
    mockSessionRepository: SessionRepository
  ): Application =
    applicationBuilder(
      userAnswers = answers
    )
      .overrides(
        bind[GamblingConnector]
          .toInstance(mockGamblingConnector),
        bind[SessionRepository]
          .toInstance(mockSessionRepository)
      )
      .build()

  private def calculateLatestDate(
    dateOfRegistration: LocalDate
  ): LocalDate = {

    val currentDate =
      LocalDate.now()

    if (dateOfRegistration.isAfter(currentDate)) {
      dateOfRegistration.plusDays(15)
    } else {
      currentDate.plusDays(15)
    }
  }

  "PartnerDateOfJoining Controller" - {

    "must return OK and the correct view for a GET" in {

      val mockGamblingConnector =
        mockConnectorReturning(businessDetails)

      val mockSessionRepository =
        mock[SessionRepository]

      val application =
        buildApplication(
          Some(partnerDetailsUserAnswers),
          mockGamblingConnector,
          mockSessionRepository
        )

      running(application) {

        implicit val msgs: Messages =
          messages(application)

        implicit val lang: Lang =
          msgs.lang

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
            calculateLatestDate(registrationDate)
              .format(dateTimeHintFormat)
          )(
            request,
            msgs
          ).toString

        verify(mockGamblingConnector)
          .getBusinessDetails(
            any[String]
          )(
            any[HeaderCarrier]
          )
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val existingAnswer =
        LocalDate.now().plusDays(2)

      val userAnswers =
        partnerDetailsUserAnswers
          .set(
            PartnerDetailsDateOfJoiningPage(index),
            existingAnswer
          )
          .success
          .value

      val mockGamblingConnector =
        mockConnectorReturning(businessDetails)

      val mockSessionRepository =
        mock[SessionRepository]

      val application =
        buildApplication(
          Some(userAnswers),
          mockGamblingConnector,
          mockSessionRepository
        )

      running(application) {

        implicit val msgs: Messages =
          messages(application)

        implicit val lang: Lang =
          msgs.lang

        val request =
          getRequest()

        val result =
          controller(application)
            .onPageLoad(NormalMode)
            .apply(request)

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(application)(
            form(application).fill(existingAnswer),
            NormalMode,
            registrationDate.format(
              dateTimeFormat()
            ),
            calculateLatestDate(registrationDate)
              .format(dateTimeHintFormat)
          )(
            request,
            msgs
          ).toString
      }
    }

    "must calculate the latest date from today when the registration date is before today" in {

      val mockGamblingConnector =
        mockConnectorReturning(businessDetails)

      val mockSessionRepository =
        mock[SessionRepository]

      val application =
        buildApplication(
          Some(partnerDetailsUserAnswers),
          mockGamblingConnector,
          mockSessionRepository
        )

      running(application) {

        implicit val msgs: Messages =
          messages(application)

        implicit val lang: Lang =
          msgs.lang

        val expectedLatestDate =
          LocalDate.now().plusDays(15)

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
            expectedLatestDate.format(
              dateTimeHintFormat
            )
          )(
            request,
            msgs
          ).toString
      }
    }

    "must calculate the latest date from the registration date when it is in the future" in {

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

      val mockSessionRepository =
        mock[SessionRepository]

      val application =
        buildApplication(
          Some(partnerDetailsUserAnswers),
          mockGamblingConnector,
          mockSessionRepository
        )

      running(application) {

        implicit val msgs: Messages =
          messages(application)

        implicit val lang: Lang =
          msgs.lang

        val expectedLatestDate =
          futureRegistrationDate.plusDays(15)

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
            expectedLatestDate.format(
              dateTimeHintFormat
            )
          )(
            request,
            msgs
          ).toString
      }
    }

    "must redirect back to the date of joining page when valid data is submitted" in {

      val mockGamblingConnector =
        mockConnectorReturning(businessDetails)

      val mockSessionRepository =
        mock[SessionRepository]

      when(
        mockSessionRepository.set(
          any[UserAnswers]
        )
      ).thenReturn(
        Future.successful(true)
      )

      val application =
        buildApplication(
          Some(partnerDetailsUserAnswers),
          mockGamblingConnector,
          mockSessionRepository
        )

      running(application) {

        val result =
          controller(application)
            .onSubmit(NormalMode)
            .apply(postRequest())

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          controllers.partner.routes.PartnerDateOfJoiningController
            .onPageLoad()
            .url

        verify(mockSessionRepository)
          .set(any[UserAnswers])
      }
    }

    "must save the submitted date against the selected partner index" in {

      val mockGamblingConnector =
        mockConnectorReturning(businessDetails)

      val mockSessionRepository =
        mock[SessionRepository]

      when(
        mockSessionRepository.set(
          any[UserAnswers]
        )
      ).thenReturn(
        Future.successful(true)
      )

      val application =
        buildApplication(
          Some(partnerDetailsUserAnswers),
          mockGamblingConnector,
          mockSessionRepository
        )

      running(application) {

        val expectedUserAnswers =
          partnerDetailsUserAnswers
            .set(
              PartnerDetailsDateOfJoiningPage(index),
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

    "must return a Bad Request and errors when an invalid calendar date is submitted" in {

      val mockGamblingConnector =
        mockConnectorReturning(businessDetails)

      val mockSessionRepository =
        mock[SessionRepository]

      val application =
        buildApplication(
          Some(partnerDetailsUserAnswers),
          mockGamblingConnector,
          mockSessionRepository
        )

      running(application) {

        implicit val msgs: Messages =
          messages(application)

        implicit val lang: Lang =
          msgs.lang

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
            "value.day"   -> "31",
            "value.month" -> "2",
            "value.year"  -> "2026"
          )

        val boundForm =
          form(application).bind(invalidData)

        val result =
          controller(application)
            .onSubmit(NormalMode)
            .apply(request)

        status(result) mustEqual BAD_REQUEST

        boundForm.hasErrors mustEqual true

        contentAsString(result) mustEqual
          view(application)(
            boundForm,
            NormalMode,
            registrationDate.format(
              dateTimeFormat()
            ),
            calculateLatestDate(registrationDate)
              .format(dateTimeHintFormat)
          )(
            request,
            msgs
          ).toString

        verify(
          mockSessionRepository,
          never()
        ).set(
          any[UserAnswers]
        )
      }
    }

    "must return a Bad Request and errors when no date is submitted" in {

      val mockGamblingConnector =
        mockConnectorReturning(businessDetails)

      val mockSessionRepository =
        mock[SessionRepository]

      val application =
        buildApplication(
          Some(partnerDetailsUserAnswers),
          mockGamblingConnector,
          mockSessionRepository
        )

      running(application) {

        implicit val msgs: Messages =
          messages(application)

        implicit val lang: Lang =
          msgs.lang

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
            "value.day"   -> "",
            "value.month" -> "",
            "value.year"  -> ""
          )

        val boundForm =
          form(application).bind(emptyData)

        val result =
          controller(application)
            .onSubmit(NormalMode)
            .apply(request)

        status(result) mustEqual BAD_REQUEST

        boundForm.hasErrors mustEqual true

        contentAsString(result) mustEqual
          view(application)(
            boundForm,
            NormalMode,
            registrationDate.format(
              dateTimeFormat()
            ),
            calculateLatestDate(registrationDate)
              .format(dateTimeHintFormat)
          )(
            request,
            msgs
          ).toString

        verify(
          mockSessionRepository,
          never()
        ).set(
          any[UserAnswers]
        )
      }
    }

    "must propagate an exception when the backend does not return a registration date for GET" in {

      val mockGamblingConnector =
        mockConnectorReturning(
          businessDetails.copy(
            dateOfRegistration = None
          )
        )

      val mockSessionRepository =
        mock[SessionRepository]

      val application =
        buildApplication(
          Some(partnerDetailsUserAnswers),
          mockGamblingConnector,
          mockSessionRepository
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

    "must propagate an exception when the backend does not return a registration date for POST" in {

      val mockGamblingConnector =
        mockConnectorReturning(
          businessDetails.copy(
            dateOfRegistration = None
          )
        )

      val mockSessionRepository =
        mock[SessionRepository]

      val application =
        buildApplication(
          Some(partnerDetailsUserAnswers),
          mockGamblingConnector,
          mockSessionRepository
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
          "No registration date found"
      }
    }

    "must propagate connector failure for GET" in {

      val mockGamblingConnector =
        mockConnectorFailing(
          "Unable to retrieve business details"
        )

      val mockSessionRepository =
        mock[SessionRepository]

      val application =
        buildApplication(
          Some(partnerDetailsUserAnswers),
          mockGamblingConnector,
          mockSessionRepository
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

    "must propagate connector failure for POST" in {

      val mockGamblingConnector =
        mockConnectorFailing(
          "Unable to retrieve business details"
        )

      val mockSessionRepository =
        mock[SessionRepository]

      val application =
        buildApplication(
          Some(partnerDetailsUserAnswers),
          mockGamblingConnector,
          mockSessionRepository
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

      val mockGamblingConnector =
        mockConnectorFailingToGetPartnerDetails()

      val mockSessionRepository =
        mock[SessionRepository]

      val application =
        buildApplication(
          None,
          mockGamblingConnector,
          mockSessionRepository
        )

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

      val mockGamblingConnector =
        mockConnectorFailingToGetPartnerDetails()

      val mockSessionRepository =
        mock[SessionRepository]

      val application =
        buildApplication(
          None,
          mockGamblingConnector,
          mockSessionRepository
        )

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
