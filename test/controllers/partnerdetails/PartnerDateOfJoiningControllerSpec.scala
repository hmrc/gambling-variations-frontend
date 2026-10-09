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

package controllers.partnerdetails

import base.SpecBase
import connectors.GamblingConnector
import controllers.routes
import forms.partnerdetails.PartnerDateOfJoiningFormProvider
import models.{BusinessDetails, BusinessType, CheckMode, NormalMode}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.partnerdetails.*
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import utils.DateTimeFormats.{dateTimeFormat, dateTimeHintFormat}
import views.html.partnerdetails.PartnerDateOfJoiningView

import java.time.{Clock, LocalDate, ZoneOffset}
import scala.concurrent.Future

class PartnerDateOfJoiningControllerSpec extends SpecBase with MockitoSugar with PartnerDetailsHelper {

  private implicit val messages: Messages =
    stubMessages()

  private val clock: Clock =
    Clock.systemUTC()

  private val registrationDate: LocalDate =
    LocalDate.of(2020, 1, 1)

  private val validAnswer: LocalDate =
    LocalDate.now(ZoneOffset.UTC)

  private val formProvider =
    new PartnerDateOfJoiningFormProvider(clock)

  private val form =
    formProvider(registrationDate)

  lazy val partnerDateOfJoiningRouteExistingPartners: String =
    controllers.partnerdetails.routes.PartnerDateOfJoiningController
      .onPageLoad(
        businessNumber1,
        CheckMode
      )
      .url

  lazy val partnerDateOfJoiningRouteNewPartners: String =
    controllers.partnerdetails.routes.PartnerDateOfJoiningController
      .onPageLoad(
        newPartnersIndex1.toString,
        NormalMode
      )
      .url

  private val businessDetails: BusinessDetails =
    BusinessDetails(
      mgdRegNumber          = mgdRegNum,
      businessType          = Some(BusinessType.Corporatebody),
      currentlyRegistered   = 1,
      groupReg              = false,
      dateOfRegistration    = Some(registrationDate),
      businessPartnerNumber = Some("XB1234567890"),
      systemDate            = LocalDate.of(2026, 1, 1)
    )

  private val partnerDetailsUserAnswersExistingPartners =
    userAnswersPartnerDetailsMinimalValidData
      .set(
        PartnerDetailsMgdRegNumberPage(businessNumber1),
        mgdRegNum
      )
      .success
      .value
      .set(
        PartnerDetailsBusinessTypePage(businessNumber1),
        BusinessType.Corporatebody
      )
      .success
      .value

  private val partnerDetailsUserAnswersNewPartners =
    userAnswersPartnerDetailsMinimalValidData
      .set(
        PartnerDetailsMgdRegNumberPage(newPartnersIndex1),
        mgdRegNum
      )
      .success
      .value
      .set(
        PartnerDetailsAddPartnerCompletedPage(newPartnersIndex1),
        false
      )
      .success
      .value
      .set(
        PartnerDetailsBusinessTypePage(newPartnersIndex1),
        BusinessType.Corporatebody
      )
      .success
      .value

  private def mockGamblingConnector(
    details: BusinessDetails = businessDetails
  ): GamblingConnector = {

    val connector =
      mock[GamblingConnector]

    when(
      connector.getBusinessDetails(any[String])(any[HeaderCarrier])
    ).thenReturn(
      Future.successful(details)
    )

    connector
  }

  private def expectedRegistrationDate(
    date: LocalDate
  ): String =
    date.format(dateTimeFormat()(messages.lang))

  private def expectedLatestDate(
    date: LocalDate
  ): String =
    calculateLatestDate(date).format(dateTimeHintFormat)

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

  "newPartners" - {

    "PartnerDateOfJoining Controller" - {

      "must return OK and the correct view for a GET" in {

        val mockConnector =
          mockGamblingConnector()

        val application =
          applicationBuilder(
            userAnswers = Some(
              partnerDetailsUserAnswersNewPartners
            )
          )
            .overrides(
              bind[GamblingConnector]
                .toInstance(mockConnector)
            )
            .build()

        running(application) {

          val request =
            FakeRequest(
              GET,
              partnerDateOfJoiningRouteNewPartners
            )

          val result =
            route(application, request).value

          val view =
            application.injector
              .instanceOf[PartnerDateOfJoiningView]

          status(result) mustEqual OK

          val expected =
            view(
              form,
              newPartnersIndex1.toString,
              NormalMode,
              expectedRegistrationDate(registrationDate),
              expectedLatestDate(registrationDate)
            )(
              request,
              messages(application)
            ).toString

          assert(
            contentAsString(result) == expected
          )

          verify(mockConnector)
            .getBusinessDetails(any[String])(any[HeaderCarrier])
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val existingAnswer =
          validAnswer

        val userAnswers =
          partnerDetailsUserAnswersNewPartners
            .set(
              PartnerDetailsDateOfJoiningPage(
                newPartnersIndex1
              ),
              existingAnswer
            )
            .success
            .value

        val mockConnector =
          mockGamblingConnector()

        val application =
          applicationBuilder(
            userAnswers = Some(userAnswers)
          )
            .overrides(
              bind[GamblingConnector]
                .toInstance(mockConnector)
            )
            .build()

        running(application) {

          val request =
            FakeRequest(
              GET,
              partnerDateOfJoiningRouteNewPartners
            )

          val result =
            route(application, request).value

          val view =
            application.injector
              .instanceOf[PartnerDateOfJoiningView]

          status(result) mustEqual OK

          val expected =
            view(
              form.fill(existingAnswer),
              newPartnersIndex1.toString,
              NormalMode,
              expectedRegistrationDate(registrationDate),
              expectedLatestDate(registrationDate)
            )(
              request,
              messages(application)
            ).toString

          assert(
            contentAsString(result) == expected
          )
        }
      }

      "must redirect to the next page when valid data is submitted" in {

        val mockSessionRepository =
          mock[SessionRepository]

        when(
          mockSessionRepository.set(any())
        ).thenReturn(
          Future.successful(true)
        )

        val mockConnector =
          mockGamblingConnector()

        val application =
          applicationBuilder(
            userAnswers = Some(
              partnerDetailsUserAnswersNewPartners
            )
          )
            .overrides(
              bind[GamblingConnector]
                .toInstance(mockConnector),
              bind[Navigator]
                .toInstance(
                  new FakeNavigator(onwardRoute)
                ),
              bind[SessionRepository]
                .toInstance(mockSessionRepository)
            )
            .build()

        running(application) {

          val request =
            FakeRequest(
              POST,
              partnerDateOfJoiningRouteNewPartners
            ).withFormUrlEncodedBody(
              "value.day"   -> validAnswer.getDayOfMonth.toString,
              "value.month" -> validAnswer.getMonthValue.toString,
              "value.year"  -> validAnswer.getYear.toString
            )

          val result =
            route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            onwardRoute.url
        }
      }

      "must save the submitted date against the new partner index" in {

        val mockSessionRepository =
          mock[SessionRepository]

        when(
          mockSessionRepository.set(any())
        ).thenReturn(
          Future.successful(true)
        )

        val mockConnector =
          mockGamblingConnector()

        val application =
          applicationBuilder(
            userAnswers = Some(
              partnerDetailsUserAnswersNewPartners
            )
          )
            .overrides(
              bind[GamblingConnector]
                .toInstance(mockConnector),
              bind[Navigator]
                .toInstance(
                  new FakeNavigator(onwardRoute)
                ),
              bind[SessionRepository]
                .toInstance(mockSessionRepository)
            )
            .build()

        running(application) {

          val request =
            FakeRequest(
              POST,
              partnerDateOfJoiningRouteNewPartners
            ).withFormUrlEncodedBody(
              "value.day"   -> validAnswer.getDayOfMonth.toString,
              "value.month" -> validAnswer.getMonthValue.toString,
              "value.year"  -> validAnswer.getYear.toString
            )

          val result =
            route(application, request).value

          status(result) mustEqual SEE_OTHER

          val expectedAnswers =
            partnerDetailsUserAnswersNewPartners
              .set(
                PartnerDetailsDateOfJoiningPage(
                  newPartnersIndex1
                ),
                validAnswer
              )
              .success
              .value

          verify(mockSessionRepository)
            .set(expectedAnswers)
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {

        val mockConnector =
          mockGamblingConnector()

        val application =
          applicationBuilder(
            userAnswers = Some(
              partnerDetailsUserAnswersNewPartners
            )
          )
            .overrides(
              bind[GamblingConnector]
                .toInstance(mockConnector)
            )
            .build()

        running(application) {

          val request =
            FakeRequest(
              POST,
              partnerDateOfJoiningRouteNewPartners
            ).withFormUrlEncodedBody(
              "value.day"   -> "31",
              "value.month" -> "2",
              "value.year"  -> "2026"
            )

          val boundForm =
            form.bind(
              Map(
                "value.day"   -> "31",
                "value.month" -> "2",
                "value.year"  -> "2026"
              )
            )

          val view =
            application.injector
              .instanceOf[PartnerDateOfJoiningView]

          val result =
            route(application, request).value

          status(result) mustEqual BAD_REQUEST

          val expected =
            view(
              boundForm,
              newPartnersIndex1.toString,
              NormalMode,
              expectedRegistrationDate(registrationDate),
              expectedLatestDate(registrationDate)
            )(
              request,
              messages(application)
            ).toString

          assert(
            contentAsString(result) == expected
          )
        }
      }

      "must calculate the latest date from today when the registration date is before today" in {

        val mockConnector =
          mockGamblingConnector()

        val application =
          applicationBuilder(
            userAnswers = Some(
              partnerDetailsUserAnswersNewPartners
            )
          )
            .overrides(
              bind[GamblingConnector]
                .toInstance(mockConnector)
            )
            .build()

        running(application) {

          val request =
            FakeRequest(
              GET,
              partnerDateOfJoiningRouteNewPartners
            )

          val result =
            route(application, request).value

          status(result) mustEqual OK

          val expectedLatest =
            LocalDate.now().plusDays(15)

          assert(
            contentAsString(result).contains(
              expectedLatest.format(dateTimeHintFormat)
            )
          )
        }
      }

      "must calculate the latest date from the registration date when it is in the future" in {

        val futureRegistrationDate =
          LocalDate.now().plusDays(30)

        val futureBusinessDetails =
          businessDetails.copy(
            dateOfRegistration = Some(futureRegistrationDate)
          )

        val mockConnector =
          mockGamblingConnector(
            futureBusinessDetails
          )

        val application =
          applicationBuilder(
            userAnswers = Some(
              partnerDetailsUserAnswersNewPartners
            )
          )
            .overrides(
              bind[GamblingConnector]
                .toInstance(mockConnector)
            )
            .build()

        running(application) {

          val request =
            FakeRequest(
              GET,
              partnerDateOfJoiningRouteNewPartners
            )

          val result =
            route(application, request).value

          status(result) mustEqual OK

          val view =
            application.injector
              .instanceOf[PartnerDateOfJoiningView]

          val futureForm =
            new PartnerDateOfJoiningFormProvider(clock)
              .apply(futureRegistrationDate)

          val expected =
            view(
              futureForm,
              newPartnersIndex1.toString,
              NormalMode,
              expectedRegistrationDate(futureRegistrationDate),
              expectedLatestDate(futureRegistrationDate)
            )(
              request,
              messages(application)
            ).toString

          assert(
            contentAsString(result) == expected
          )
        }
      }

      "must propagate the exception when the backend does not return a registration date" in {

        val mockConnector =
          mockGamblingConnector(
            businessDetails.copy(
              dateOfRegistration = None
            )
          )

        val application =
          applicationBuilder(
            userAnswers = Some(
              partnerDetailsUserAnswersNewPartners
            )
          )
            .overrides(
              bind[GamblingConnector]
                .toInstance(mockConnector)
            )
            .build()

        running(application) {

          val result =
            route(
              application,
              FakeRequest(
                GET,
                partnerDateOfJoiningRouteNewPartners
              )
            ).value

          val exception =
            intercept[RuntimeException] {
              await(result)
            }

          exception.getMessage mustEqual
            "No registration date found"
        }
      }

      "must propagate connector failure for GET" in {

        val mockConnector =
          mock[GamblingConnector]

        when(
          mockConnector.getBusinessDetails(any[String])(any[HeaderCarrier])
        ).thenReturn(
          Future.failed(
            new RuntimeException(
              "Unable to retrieve business details"
            )
          )
        )

        val application =
          applicationBuilder(
            userAnswers = Some(
              partnerDetailsUserAnswersNewPartners
            )
          )
            .overrides(
              bind[GamblingConnector]
                .toInstance(mockConnector)
            )
            .build()

        running(application) {

          val result =
            route(
              application,
              FakeRequest(
                GET,
                partnerDateOfJoiningRouteNewPartners
              )
            ).value

          val exception =
            intercept[RuntimeException] {
              await(result)
            }

          exception.getMessage mustEqual
            "Unable to retrieve business details"
        }
      }

      "must propagate connector failure for POST" in {

        val mockConnector =
          mock[GamblingConnector]

        when(
          mockConnector.getBusinessDetails(any[String])(any[HeaderCarrier])
        ).thenReturn(
          Future.failed(
            new RuntimeException(
              "Unable to retrieve business details"
            )
          )
        )

        val application =
          applicationBuilder(
            userAnswers = Some(
              partnerDetailsUserAnswersNewPartners
            )
          )
            .overrides(
              bind[GamblingConnector]
                .toInstance(mockConnector)
            )
            .build()

        running(application) {

          val request =
            FakeRequest(
              POST,
              partnerDateOfJoiningRouteNewPartners
            ).withFormUrlEncodedBody(
              "value.day"   -> validAnswer.getDayOfMonth.toString,
              "value.month" -> validAnswer.getMonthValue.toString,
              "value.year"  -> validAnswer.getYear.toString
            )

          val result =
            route(application, request).value

          val exception =
            intercept[RuntimeException] {
              await(result)
            }

          exception.getMessage mustEqual
            "Unable to retrieve business details"
        }
      }

      "must redirect to SystemError for a GET if no existing data is found" in {

        val application =
          applicationBuilder(
            userAnswers = None
          ).build()

        running(application) {

          val request =
            FakeRequest(
              GET,
              partnerDateOfJoiningRouteNewPartners
            )

          val result =
            route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            routes.SystemErrorController
              .onPageLoad()
              .url
        }
      }

      "must redirect to SystemError for a POST if no existing data is found" in {

        val application =
          applicationBuilder(
            userAnswers = None
          ).build()

        running(application) {

          val request =
            FakeRequest(
              POST,
              partnerDateOfJoiningRouteNewPartners
            ).withFormUrlEncodedBody(
              "value.day"   -> validAnswer.getDayOfMonth.toString,
              "value.month" -> validAnswer.getMonthValue.toString,
              "value.year"  -> validAnswer.getYear.toString
            )

          val result =
            route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            routes.SystemErrorController
              .onPageLoad()
              .url
        }
      }
    }
  }

  "partners" - {

    "PartnerDateOfJoining Controller" - {

      "must return OK and the correct view for a GET" in {

        val mockConnector =
          mockGamblingConnector()

        val application =
          applicationBuilder(
            userAnswers = Some(
              partnerDetailsUserAnswersExistingPartners
            )
          )
            .overrides(
              bind[GamblingConnector]
                .toInstance(mockConnector)
            )
            .build()

        running(application) {

          val request =
            FakeRequest(
              GET,
              partnerDateOfJoiningRouteExistingPartners
            )

          val result =
            route(application, request).value

          val view =
            application.injector
              .instanceOf[PartnerDateOfJoiningView]

          status(result) mustEqual OK

          val expected =
            view(
              form,
              businessNumber1,
              CheckMode,
              expectedRegistrationDate(registrationDate),
              expectedLatestDate(registrationDate)
            )(
              request,
              messages(application)
            ).toString

          assert(
            contentAsString(result) == expected
          )

          verify(mockConnector)
            .getBusinessDetails(any[String])(any[HeaderCarrier])
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val existingAnswer =
          validAnswer

        val userAnswers =
          partnerDetailsUserAnswersExistingPartners
            .set(
              PartnerDetailsDateOfJoiningPage(
                businessNumber1
              ),
              existingAnswer
            )
            .success
            .value

        val mockConnector =
          mockGamblingConnector()

        val application =
          applicationBuilder(
            userAnswers = Some(userAnswers)
          )
            .overrides(
              bind[GamblingConnector]
                .toInstance(mockConnector)
            )
            .build()

        running(application) {

          val request =
            FakeRequest(
              GET,
              partnerDateOfJoiningRouteExistingPartners
            )

          val result =
            route(application, request).value

          val view =
            application.injector
              .instanceOf[PartnerDateOfJoiningView]

          status(result) mustEqual OK

          val expected =
            view(
              form.fill(existingAnswer),
              businessNumber1,
              CheckMode,
              expectedRegistrationDate(registrationDate),
              expectedLatestDate(registrationDate)
            )(
              request,
              messages(application)
            ).toString

          assert(
            contentAsString(result) == expected
          )
        }
      }

      "must redirect to the next page when valid data is submitted" in {

        val mockSessionRepository =
          mock[SessionRepository]

        when(
          mockSessionRepository.set(any())
        ).thenReturn(
          Future.successful(true)
        )

        val mockConnector =
          mockGamblingConnector()

        val application =
          applicationBuilder(
            userAnswers = Some(
              partnerDetailsUserAnswersExistingPartners
            )
          )
            .overrides(
              bind[GamblingConnector]
                .toInstance(mockConnector),
              bind[Navigator]
                .toInstance(
                  new FakeNavigator(onwardRoute)
                ),
              bind[SessionRepository]
                .toInstance(mockSessionRepository)
            )
            .build()

        running(application) {

          val request =
            FakeRequest(
              POST,
              partnerDateOfJoiningRouteExistingPartners
            ).withFormUrlEncodedBody(
              "value.day"   -> validAnswer.getDayOfMonth.toString,
              "value.month" -> validAnswer.getMonthValue.toString,
              "value.year"  -> validAnswer.getYear.toString
            )

          val result =
            route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            onwardRoute.url
        }
      }

      "must save the submitted date against the existing partner index" in {

        val mockSessionRepository =
          mock[SessionRepository]

        when(
          mockSessionRepository.set(any())
        ).thenReturn(
          Future.successful(true)
        )

        val mockConnector =
          mockGamblingConnector()

        val application =
          applicationBuilder(
            userAnswers = Some(
              partnerDetailsUserAnswersExistingPartners
            )
          )
            .overrides(
              bind[GamblingConnector]
                .toInstance(mockConnector),
              bind[Navigator]
                .toInstance(
                  new FakeNavigator(onwardRoute)
                ),
              bind[SessionRepository]
                .toInstance(mockSessionRepository)
            )
            .build()

        running(application) {

          val request =
            FakeRequest(
              POST,
              partnerDateOfJoiningRouteExistingPartners
            ).withFormUrlEncodedBody(
              "value.day"   -> validAnswer.getDayOfMonth.toString,
              "value.month" -> validAnswer.getMonthValue.toString,
              "value.year"  -> validAnswer.getYear.toString
            )

          val result =
            route(application, request).value

          status(result) mustEqual SEE_OTHER

          val expectedAnswers =
            partnerDetailsUserAnswersExistingPartners
              .set(
                PartnerDetailsDateOfJoiningPage(
                  businessNumber1
                ),
                validAnswer
              )
              .success
              .value

          verify(mockSessionRepository)
            .set(expectedAnswers)
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {

        val mockConnector =
          mockGamblingConnector()

        val application =
          applicationBuilder(
            userAnswers = Some(
              partnerDetailsUserAnswersExistingPartners
            )
          )
            .overrides(
              bind[GamblingConnector]
                .toInstance(mockConnector)
            )
            .build()

        running(application) {

          val request =
            FakeRequest(
              POST,
              partnerDateOfJoiningRouteExistingPartners
            ).withFormUrlEncodedBody(
              "value.day"   -> "31",
              "value.month" -> "2",
              "value.year"  -> "2026"
            )

          val boundForm =
            form.bind(
              Map(
                "value.day"   -> "31",
                "value.month" -> "2",
                "value.year"  -> "2026"
              )
            )

          val view =
            application.injector
              .instanceOf[PartnerDateOfJoiningView]

          val result =
            route(application, request).value

          status(result) mustEqual BAD_REQUEST

          val expected =
            view(
              boundForm,
              businessNumber1,
              CheckMode,
              expectedRegistrationDate(registrationDate),
              expectedLatestDate(registrationDate)
            )(
              request,
              messages(application)
            ).toString

          assert(
            contentAsString(result) == expected
          )
        }
      }

      "must propagate the exception when the backend does not return a registration date" in {

        val mockConnector =
          mockGamblingConnector(
            businessDetails.copy(
              dateOfRegistration = None
            )
          )

        val application =
          applicationBuilder(
            userAnswers = Some(
              partnerDetailsUserAnswersExistingPartners
            )
          )
            .overrides(
              bind[GamblingConnector]
                .toInstance(mockConnector)
            )
            .build()

        running(application) {

          val result =
            route(
              application,
              FakeRequest(
                GET,
                partnerDateOfJoiningRouteExistingPartners
              )
            ).value

          val exception =
            intercept[RuntimeException] {
              await(result)
            }

          exception.getMessage mustEqual
            "No registration date found"
        }
      }

      "must propagate connector failure for GET" in {

        val mockConnector =
          mock[GamblingConnector]

        when(
          mockConnector.getBusinessDetails(any[String])(any[HeaderCarrier])
        ).thenReturn(
          Future.failed(
            new RuntimeException(
              "Unable to retrieve business details"
            )
          )
        )

        val application =
          applicationBuilder(
            userAnswers = Some(
              partnerDetailsUserAnswersExistingPartners
            )
          )
            .overrides(
              bind[GamblingConnector]
                .toInstance(mockConnector)
            )
            .build()

        running(application) {

          val result =
            route(
              application,
              FakeRequest(
                GET,
                partnerDateOfJoiningRouteExistingPartners
              )
            ).value

          val exception =
            intercept[RuntimeException] {
              await(result)
            }

          exception.getMessage mustEqual
            "Unable to retrieve business details"
        }
      }

      "must propagate connector failure for POST" in {

        val mockConnector =
          mock[GamblingConnector]

        when(
          mockConnector.getBusinessDetails(any[String])(any[HeaderCarrier])
        ).thenReturn(
          Future.failed(
            new RuntimeException(
              "Unable to retrieve business details"
            )
          )
        )

        val application =
          applicationBuilder(
            userAnswers = Some(
              partnerDetailsUserAnswersExistingPartners
            )
          )
            .overrides(
              bind[GamblingConnector]
                .toInstance(mockConnector)
            )
            .build()

        running(application) {

          val request =
            FakeRequest(
              POST,
              partnerDateOfJoiningRouteExistingPartners
            ).withFormUrlEncodedBody(
              "value.day"   -> validAnswer.getDayOfMonth.toString,
              "value.month" -> validAnswer.getMonthValue.toString,
              "value.year"  -> validAnswer.getYear.toString
            )

          val result =
            route(application, request).value

          val exception =
            intercept[RuntimeException] {
              await(result)
            }

          exception.getMessage mustEqual
            "Unable to retrieve business details"
        }
      }

      "must redirect to SystemError for a GET if no existing data is found" in {

        val application =
          applicationBuilder(
            userAnswers = None
          ).build()

        running(application) {

          val request =
            FakeRequest(
              GET,
              partnerDateOfJoiningRouteExistingPartners
            )

          val result =
            route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            routes.SystemErrorController
              .onPageLoad()
              .url
        }
      }

      "must redirect to SystemError for a POST if no existing data is found" in {

        val application =
          applicationBuilder(
            userAnswers = None
          ).build()

        running(application) {

          val request =
            FakeRequest(
              POST,
              partnerDateOfJoiningRouteExistingPartners
            ).withFormUrlEncodedBody(
              "value.day"   -> validAnswer.getDayOfMonth.toString,
              "value.month" -> validAnswer.getMonthValue.toString,
              "value.year"  -> validAnswer.getYear.toString
            )

          val result =
            route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            routes.SystemErrorController
              .onPageLoad()
              .url
        }
      }
    }
  }
}
