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
import controllers.partner.PartnerUtils.getIndex
import controllers.routes
import forms.partner.PartnerSoleProprietorDobFormProvider
import models.{BusinessType, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.partner.PartnerDetailsAddPartnerCompletedPage
import pages.partnerdetails.{PartnerDetailsBusinessTypePage, PartnerDetailsDateOfBirthPage, PartnerDetailsPage}
import play.api.Application
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded, Call}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.partner.PartnerSoleProprietorDobView

import java.time.{Clock, LocalDate, ZoneOffset}
import scala.concurrent.Future

class PartnerSoleProprietorDobControllerSpec extends SpecBase with MockitoSugar {

  private implicit val msgs: Messages =
    stubMessages()

  private val today: LocalDate =
    LocalDate.of(2026, 9, 5)

  private val clock: Clock =
    Clock.fixed(
      today.atStartOfDay(ZoneOffset.UTC).toInstant,
      ZoneOffset.UTC
    )

  private val validAnswer: LocalDate =
    today.minusYears(30)

  private val onwardRoute: Call =
    Call("GET", "/foo")

  private val getRoute: String =
    controllers.partner.routes.PartnerSoleProprietorDobController
      .onPageLoad()
      .url

  private val postRoute: String =
    controllers.partner.routes.PartnerSoleProprietorDobController
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
      .set(
        PartnerDetailsBusinessTypePage(0),
        BusinessType.Soleproprietor
      )
      .success
      .value

  private val index: Int =
    partnerDetailsUserAnswers.getIndex

  private def controller(
    application: Application
  ): PartnerSoleProprietorDobController =
    application.injector
      .instanceOf[PartnerSoleProprietorDobController]

  private def form(
    application: Application
  ) =
    application.injector
      .instanceOf[PartnerSoleProprietorDobFormProvider]
      .apply()

  private def view(
    application: Application
  ): PartnerSoleProprietorDobView =
    application.injector
      .instanceOf[PartnerSoleProprietorDobView]

  private def getRequest(): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, getRoute)

  private def postRequest(
    answer: LocalDate = validAnswer
  ): FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest(POST, postRoute)
      .withFormUrlEncodedBody(
        "value.day"   -> answer.getDayOfMonth.toString,
        "value.month" -> answer.getMonthValue.toString,
        "value.year"  -> answer.getYear.toString
      )

  "PartnerSoleProprietorDob Controller" - {

    "must return OK and the correct view for a GET for a sole proprietor" in {

      val application =
        applicationBuilder(
          userAnswers = Some(partnerDetailsUserAnswers)
        )
          .overrides(
            bind[Clock].toInstance(clock)
          )
          .build()

      running(application) {

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
            NormalMode
          )(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers =
        partnerDetailsUserAnswers
          .set(
            PartnerDetailsDateOfBirthPage(index),
            validAnswer
          )
          .success
          .value

      val application =
        applicationBuilder(
          userAnswers = Some(userAnswers)
        )
          .overrides(
            bind[Clock].toInstance(clock)
          )
          .build()

      running(application) {

        val request =
          getRequest()

        val result =
          controller(application)
            .onPageLoad(NormalMode)
            .apply(request)

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(application)(
            form(application).fill(validAnswer),
            NormalMode
          )(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository =
        mock[SessionRepository]

      when(mockSessionRepository.set(any[UserAnswers]))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(
          userAnswers = Some(partnerDetailsUserAnswers)
        )
          .overrides(
            bind[Clock].toInstance(clock),
            bind[Navigator].toInstance(
              new FakeNavigator(onwardRoute)
            ),
            bind[SessionRepository].toInstance(
              mockSessionRepository
            )
          )
          .build()

      running(application) {

        val request =
          postRequest()

        val result =
          controller(application)
            .onSubmit(NormalMode)
            .apply(request)

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          onwardRoute.url

        verify(mockSessionRepository)
          .set(any[UserAnswers])
      }
    }

    "must return a Bad Request and errors when an invalid date is submitted" in {

      val application =
        applicationBuilder(
          userAnswers = Some(partnerDetailsUserAnswers)
        )
          .overrides(
            bind[Clock].toInstance(clock)
          )
          .build()

      running(application) {

        val request =
          FakeRequest(POST, postRoute)
            .withFormUrlEncodedBody(
              "value.day"   -> "31",
              "value.month" -> "2",
              "value.year"  -> "2020"
            )

        val boundForm =
          form(application)
            .bind(
              Map(
                "value.day"   -> "31",
                "value.month" -> "2",
                "value.year"  -> "2020"
              )
            )

        val result =
          controller(application)
            .onSubmit(NormalMode)
            .apply(request)

        status(result) mustEqual BAD_REQUEST

        boundForm.errors
          .map(_.message) must contain(
          "partnerSoleProprietorDob.error.invalid"
        )

        contentAsString(result) mustEqual
          view(application)(
            boundForm,
            NormalMode
          )(request, messages(application)).toString
      }
    }

    "must accept any date in past" in {

      val earliestValidAnswer =
        today.minusYears(130)

      val mockSessionRepository =
        mock[SessionRepository]

      when(mockSessionRepository.set(any[UserAnswers]))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(
          userAnswers = Some(partnerDetailsUserAnswers)
        )
          .overrides(
            bind[Clock].toInstance(clock),
            bind[Navigator].toInstance(
              new FakeNavigator(onwardRoute)
            ),
            bind[SessionRepository].toInstance(
              mockSessionRepository
            )
          )
          .build()

      running(application) {

        val request =
          postRequest(earliestValidAnswer)

        val result =
          controller(application)
            .onSubmit(NormalMode)
            .apply(request)

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          onwardRoute.url
      }
    }

    "must return a Bad Request when today's date is submitted" in {

      val application =
        applicationBuilder(
          userAnswers = Some(partnerDetailsUserAnswers)
        )
          .overrides(
            bind[Clock].toInstance(clock)
          )
          .build()

      running(application) {

        val request =
          postRequest(today)

        val boundForm =
          form(application)
            .bind(
              Map(
                "value.day" ->
                  today.getDayOfMonth.toString,
                "value.month" ->
                  today.getMonthValue.toString,
                "value.year" ->
                  today.getYear.toString
              )
            )

        val result =
          controller(application)
            .onSubmit(NormalMode)
            .apply(request)

        status(result) mustEqual BAD_REQUEST

        boundForm.errors
          .map(_.message) must contain(
          "partnerSoleProprietorDob.error.afterLatestDate"
        )

        contentAsString(result) mustEqual
          view(application)(
            boundForm,
            NormalMode
          )(request, messages(application)).toString
      }
    }

    "must accept yesterday's date" in {

      val latestValidAnswer =
        today.minusDays(1)

      val mockSessionRepository =
        mock[SessionRepository]

      when(mockSessionRepository.set(any[UserAnswers]))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(
          userAnswers = Some(partnerDetailsUserAnswers)
        )
          .overrides(
            bind[Clock].toInstance(clock),
            bind[Navigator].toInstance(
              new FakeNavigator(onwardRoute)
            ),
            bind[SessionRepository].toInstance(
              mockSessionRepository
            )
          )
          .build()

      running(application) {

        val request =
          postRequest(latestValidAnswer)

        val result =
          controller(application)
            .onSubmit(NormalMode)
            .apply(request)

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          onwardRoute.url
      }
    }

    "must redirect to SystemError when the business type is not sole proprietor" in {

      val userAnswers =
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
          .set(
            PartnerDetailsBusinessTypePage(0),
            BusinessType.Corporatebody
          )
          .success
          .value

      val application =
        applicationBuilder(
          userAnswers = Some(userAnswers)
        )
          .overrides(
            bind[Clock].toInstance(clock)
          )
          .build()

      running(application) {

        val result =
          controller(application)
            .onPageLoad(NormalMode)
            .apply(getRequest())

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.SystemErrorController.onPageLoad().url
      }
    }

    "must redirect to SystemError when the business type is missing" in {

      val userAnswers =
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

      val application =
        applicationBuilder(
          userAnswers = Some(userAnswers)
        )
          .overrides(
            bind[Clock].toInstance(clock)
          )
          .build()

      running(application) {

        val result =
          controller(application)
            .onPageLoad(NormalMode)
            .apply(getRequest())

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.SystemErrorController.onPageLoad().url
      }
    }

    "must redirect to SystemError for a GET when no existing data can be loaded" in {

      val application =
        applicationBuilder(
          userAnswers = None
        )
          .overrides(
            bind[Clock].toInstance(clock)
          )
          .build()

      running(application) {

        val result =
          controller(application)
            .onPageLoad(NormalMode)
            .apply(getRequest())

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.SystemErrorController.onPageLoad().url
      }
    }

    "must redirect to SystemError for a POST when no existing data can be loaded" in {

      val application =
        applicationBuilder(
          userAnswers = None
        )
          .overrides(
            bind[Clock].toInstance(clock)
          )
          .build()

      running(application) {

        val result =
          controller(application)
            .onSubmit(NormalMode)
            .apply(postRequest())

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.SystemErrorController.onPageLoad().url
      }
    }
  }
}
