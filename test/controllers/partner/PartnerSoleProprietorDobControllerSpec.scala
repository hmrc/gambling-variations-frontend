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

  private val formProvider =
    new PartnerSoleProprietorDobFormProvider(clock)

  private val form =
    formProvider()

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

  private def getRequest(): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, getRoute)

  private def postRequest(): FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest(POST, postRoute)
      .withFormUrlEncodedBody(
        "value.day"   -> validAnswer.getDayOfMonth.toString,
        "value.month" -> validAnswer.getMonthValue.toString,
        "value.year"  -> validAnswer.getYear.toString
      )

  "PartnerSoleProprietorDob Controller" - {

    "must return OK and the correct view for a GET for a sole proprietor" in {
      println("....................." + getRoute)
      val application =
        applicationBuilder(
          userAnswers = Some(partnerDetailsUserAnswers)
        )
          .overrides(
            bind[Clock].toInstance(clock)
          )
          .build()

      running(application) {

        val result =
          route(application, getRequest()).value

        val view =
          application.injector
            .instanceOf[PartnerSoleProprietorDobView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(
            form,
            NormalMode
          )(getRequest(), messages(application)).toString
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

        val result =
          route(application, getRequest()).value

        val view =
          application.injector
            .instanceOf[PartnerSoleProprietorDobView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(
            form.fill(validAnswer),
            NormalMode
          )(getRequest(), messages(application)).toString
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

        val result =
          route(application, postRequest()).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          onwardRoute.url

        verify(mockSessionRepository)
          .set(any[UserAnswers])
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application =
        applicationBuilder(
          userAnswers = Some(partnerDetailsUserAnswers)
        )
          .overrides(
            bind[Clock].toInstance(clock)
          )
          .build()

      val request =
        FakeRequest(POST, postRoute)
          .withFormUrlEncodedBody(
            "value.day"   -> "31",
            "value.month" -> "2",
            "value.year"  -> "2020"
          )

      running(application) {

        val boundForm =
          form.bind(
            Map(
              "value.day"   -> "31",
              "value.month" -> "2",
              "value.year"  -> "2020"
            )
          )

        val view =
          application.injector
            .instanceOf[PartnerSoleProprietorDobView]

        val result =
          route(application, request).value

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(
            boundForm,
            NormalMode
          )(request, messages(application)).toString
      }
    }

    "must return a Bad Request when the date is earlier than 120 years ago" in {

      val application =
        applicationBuilder(
          userAnswers = Some(partnerDetailsUserAnswers)
        )
          .overrides(
            bind[Clock].toInstance(clock)
          )
          .build()

      val invalidAnswer =
        today.minusYears(120).minusDays(1)

      val request =
        FakeRequest(POST, postRoute)
          .withFormUrlEncodedBody(
            "value.day"   -> invalidAnswer.getDayOfMonth.toString,
            "value.month" -> invalidAnswer.getMonthValue.toString,
            "value.year"  -> invalidAnswer.getYear.toString
          )

      running(application) {

        val boundForm =
          form.bind(
            Map(
              "value.day"   -> invalidAnswer.getDayOfMonth.toString,
              "value.month" -> invalidAnswer.getMonthValue.toString,
              "value.year"  -> invalidAnswer.getYear.toString
            )
          )

        val view =
          application.injector
            .instanceOf[PartnerSoleProprietorDobView]

        val result =
          route(application, request).value

        status(result) mustEqual BAD_REQUEST

        boundForm.errors
          .map(_.message) must contain(
          "partnerSoleProprietorDob.error.beforeEarliestDate"
        )

        contentAsString(result) mustEqual
          view(
            boundForm,
            NormalMode
          )(request, messages(application)).toString
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

      val request =
        FakeRequest(POST, postRoute)
          .withFormUrlEncodedBody(
            "value.day"   -> today.getDayOfMonth.toString,
            "value.month" -> today.getMonthValue.toString,
            "value.year"  -> today.getYear.toString
          )

      running(application) {

        val boundForm =
          form.bind(
            Map(
              "value.day"   -> today.getDayOfMonth.toString,
              "value.month" -> today.getMonthValue.toString,
              "value.year"  -> today.getYear.toString
            )
          )

        val view =
          application.injector
            .instanceOf[PartnerSoleProprietorDobView]

        val result =
          route(application, request).value

        status(result) mustEqual BAD_REQUEST

        boundForm.errors
          .map(_.message) must contain(
          "partnerSoleProprietorDob.error.afterLatestDate"
        )

        contentAsString(result) mustEqual
          view(
            boundForm,
            NormalMode
          )(request, messages(application)).toString
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
          route(application, getRequest()).value

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
          route(application, getRequest()).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.SystemErrorController.onPageLoad().url
      }
    }

    "must redirect to SystemError for a GET if no existing data is found" in {

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
          route(application, getRequest()).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.SystemErrorController.onPageLoad().url
      }
    }

    "must redirect to SystemError for a POST if no existing data is found" in {

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
          route(application, postRequest()).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.SystemErrorController.onPageLoad().url
      }
    }
  }
}
