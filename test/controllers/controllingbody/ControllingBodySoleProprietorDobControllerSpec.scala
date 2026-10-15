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

package controllers.controllingbody

import base.SpecBase
import controllers.routes
import forms.controllingbody.ControllingBodySoleProprietorDobFormProvider
import models.{BusinessType, NormalMode, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.controllingbody.{ControllingBodyBusinessTypePage, ControllingBodyChangesPage, ControllingBodyDateOfBirthPage, ControllingBodySectionPage, ControllingBodySubmittedPage}
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.controllingbody.ControllingBodySoleProprietorDobView

import java.time.{Clock, LocalDate, ZoneOffset}
import scala.concurrent.Future

class ControllingBodySoleProprietorDobControllerSpec extends SpecBase with MockitoSugar {

  private val today: LocalDate = LocalDate.of(2026, 9, 5)

  private val clock: Clock = Clock.fixed(today.atStartOfDay(ZoneOffset.UTC).toInstant, ZoneOffset.UTC)

  private val validAnswer: LocalDate = today.minusYears(30)

  private lazy val getRoute: String = controllers.controllingbody.routes.ControllingBodySoleProprietorDobController.onPageLoad().url

  private lazy val postRoute: String = controllers.controllingbody.routes.ControllingBodySoleProprietorDobController.onSubmit().url

  private val userAnswersForSoleProprietor: UserAnswers =
    emptyUserAnswers
      .set(ControllingBodySectionPage, userAnswersId)
      .success
      .value
      .set(ControllingBodyBusinessTypePage, BusinessType.Soleproprietor)
      .success
      .value

  private val userAnswersWithDob: UserAnswers =
    userAnswersForSoleProprietor
      .set(ControllingBodyDateOfBirthPage, LocalDate.of(1998, 3, 2))
      .success
      .value

  private val userAnswersForOtherBusinessType: UserAnswers =
    userAnswersForSoleProprietor
      .set(ControllingBodyBusinessTypePage, BusinessType.Corporatebody)
      .success
      .value

  private val validFormData: Map[String, String] =
    Map("value.day"   -> validAnswer.getDayOfMonth.toString,
        "value.month" -> validAnswer.getMonthValue.toString,
        "value.year"  -> validAnswer.getYear.toString
       )

  private def form(application: play.api.Application)(implicit msgs: Messages) =
    application.injector.instanceOf[ControllingBodySoleProprietorDobFormProvider].apply()

  private def view(application: play.api.Application) = application.injector.instanceOf[ControllingBodySoleProprietorDobView]

  private def getRequest() = FakeRequest(GET, getRoute)

  private def postRequest(answer: LocalDate = validAnswer) =
    FakeRequest(POST, postRoute)
      .withFormUrlEncodedBody("value.day"   -> answer.getDayOfMonth.toString,
                              "value.month" -> answer.getMonthValue.toString,
                              "value.year"  -> answer.getYear.toString
                             )

  "ControllingBodySoleProprietorDobController" - {

    "must return OK and the correct view for a GET" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswersForSoleProprietor))
          .overrides(bind[Clock].toInstance(clock))
          .build()

      running(application) {
        implicit val request = getRequest()
        implicit val msgs: Messages = messages(application)

        val result = route(application, request).value

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(application)(form(application), NormalMode)(request, msgs).toString
      }
    }

    "must populate the form when the date of birth has previously been answered" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithDob))
          .overrides(bind[Clock].toInstance(clock))
          .build()

      running(application) {
        implicit val request = getRequest()
        implicit val msgs: Messages = messages(application)

        val result = route(application, request).value

        val existingDob = LocalDate.of(1998, 3, 2)

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(application)(form(application).fill(existingDob), NormalMode)(request, msgs).toString
      }
    }

    "must redirect to System Error when the business type is not sole proprietor" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswersForOtherBusinessType))
          .overrides(bind[Clock].toInstance(clock))
          .build()

      running(application) {
        val result = route(application, getRequest()).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.SystemErrorController.onPageLoad().url
      }
    }

    "must redirect to System Error when the business type is missing" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[Clock].toInstance(clock))
          .build()

      running(application) {
        val result = route(application, getRequest()).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.SystemErrorController.onPageLoad().url
      }
    }

    "must redirect to System Error when user answers cannot be loaded on GET" in {

      val application =
        applicationBuilder(userAnswers = None)
          .overrides(bind[Clock].toInstance(clock))
          .build()

      running(application) {
        val result = route(application, getRequest()).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.SystemErrorController.onPageLoad().url
      }
    }

    "must redirect to System Error when user answers cannot be loaded on POST" in {

      val application =
        applicationBuilder(userAnswers = None)
          .overrides(bind[Clock].toInstance(clock))
          .build()

      running(application) {
        val result = route(application, postRequest()).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.SystemErrorController.onPageLoad().url
      }
    }

    "must return Bad Request when an invalid date is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswersForSoleProprietor))
          .overrides(bind[Clock].toInstance(clock))
          .build()

      running(application) {
        val invalidData = Map("value.day" -> "31", "value.month" -> "2", "value.year" -> "2020")

        val request: FakeRequest[play.api.mvc.AnyContentAsFormUrlEncoded] =
          FakeRequest(POST, postRoute).withFormUrlEncodedBody(invalidData.toSeq*)

        implicit val msgs: Messages = messages(application)

        val boundForm = form(application).bind(invalidData)

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        boundForm.errors.map(_.message) must
          contain("controllingBodySoleProprietorDob.error.invalid")

        contentAsString(result) mustEqual
          view(application)(boundForm, NormalMode)(request, msgs).toString
      }
    }
    "must return Bad Request when today's date is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswersForSoleProprietor))
          .overrides(bind[Clock].toInstance(clock))
          .build()

      running(application) {
        implicit val request = postRequest(today)

        implicit val msgs: Messages = messages(application)

        val boundForm = form(application).bind(
          Map("value.day" -> today.getDayOfMonth.toString, "value.month" -> today.getMonthValue.toString, "value.year" -> today.getYear.toString)
        )

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST

        boundForm.errors.map(_.message) must
          contain("controllingBodySoleProprietorDob.error.afterLatestDate")

        contentAsString(result) mustEqual
          view(application)(boundForm, NormalMode)(request, msgs).toString
      }
    }

    "must accept a date in the past" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any[UserAnswers])).thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(userAnswersForSoleProprietor))
          .overrides(bind[Clock].toInstance(clock), bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val result = route(application, postRequest()).value

        status(result) mustEqual SEE_OTHER

        verify(mockSessionRepository).set(any[UserAnswers])
      }
    }

    "must accept yesterday's date" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any[UserAnswers])).thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(userAnswersForSoleProprietor))
          .overrides(bind[Clock].toInstance(clock), bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val result = route(application, postRequest(today.minusDays(1))).value

        status(result) mustEqual SEE_OTHER

      }
    }

    "must update UserAnswers correctly when a new date of birth is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      val savedAnswersCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockSessionRepository.set(any[UserAnswers])).thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(userAnswersForSoleProprietor))
          .overrides(bind[Clock].toInstance(clock), bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val result = route(application, postRequest()).value

        status(result) mustEqual SEE_OTHER

        verify(mockSessionRepository).set(savedAnswersCaptor.capture())

        val savedAnswers = savedAnswersCaptor.getValue

        savedAnswers.get(ControllingBodyDateOfBirthPage).value mustEqual validAnswer
        savedAnswers.get(ControllingBodySubmittedPage).value mustEqual true
        savedAnswers.get(ControllingBodyChangesPage).value mustEqual true
      }
    }

    "must set the changes flag when the date of birth has changed" in {

      val mockSessionRepository = mock[SessionRepository]

      val savedAnswersCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockSessionRepository.set(any[UserAnswers])).thenReturn(Future.successful(true))

      val previousDob = LocalDate.of(1998, 3, 2)

      val answersWithPreviousDob = userAnswersForSoleProprietor
        .set(ControllingBodyDateOfBirthPage, previousDob)
        .success
        .value

      val application =
        applicationBuilder(userAnswers = Some(answersWithPreviousDob))
          .overrides(bind[Clock].toInstance(clock), bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val result = route(application, postRequest()).value

        status(result) mustEqual SEE_OTHER

        verify(mockSessionRepository).set(savedAnswersCaptor.capture())

        val savedAnswers = savedAnswersCaptor.getValue

        savedAnswers.get(ControllingBodyDateOfBirthPage).value mustEqual validAnswer
        savedAnswers.get(ControllingBodySubmittedPage).value mustEqual true
        savedAnswers.get(ControllingBodyChangesPage).value mustEqual true
      }
    }
  }
}
