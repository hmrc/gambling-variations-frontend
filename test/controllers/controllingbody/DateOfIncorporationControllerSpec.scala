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
import forms.controllingbody.DateOfIncorporationFormProvider
import models.UserAnswers
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.controllingbody.{ControllingBodyChangesPage, ControllingBodyDateOfIncorporationPage, ControllingBodySubmittedPage}
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded, Call}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.controllingbody.DateOfIncorporationView

import java.time.{LocalDate, ZoneOffset}
import scala.concurrent.Future

class DateOfIncorporationControllerSpec extends SpecBase with MockitoSugar {

  private implicit val messages: Messages = stubMessages()

  private val formProvider = new DateOfIncorporationFormProvider()
  private def form = formProvider()

  def onwardRoute = Call("GET", "/date-of-incorporation")

  val validAnswer = LocalDate.now(ZoneOffset.UTC)

  lazy val dateOfIncorporationRoute = controllers.controllingbody.routes.DateOfIncorporationController.onPageLoad().url

  override val emptyUserAnswers = UserAnswers(userAnswersId)

  def getRequest(): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, dateOfIncorporationRoute)

  def postRequest(): FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest(POST, dateOfIncorporationRoute)
      .withFormUrlEncodedBody(
        "dateOfIncorporation.day"   -> validAnswer.getDayOfMonth.toString,
        "dateOfIncorporation.month" -> validAnswer.getMonthValue.toString,
        "dateOfIncorporation.year"  -> validAnswer.getYear.toString
      )

  "DateOfIncorporation Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val result = route(application, getRequest()).value

        val view = application.injector.instanceOf[DateOfIncorporationView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form)(getRequest(), messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(ControllingBodyDateOfIncorporationPage, validAnswer).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val view = application.injector.instanceOf[DateOfIncorporationView]

        val result = route(application, getRequest()).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(validAnswer))(getRequest(), messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val result = route(application, postRequest()).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request =
        FakeRequest(POST, dateOfIncorporationRoute)
          .withFormUrlEncodedBody("dateOfIncorporation.day" -> "31", "dateOfIncorporation.month" -> "4", "dateOfIncorporation.year" -> "2007")

      running(application) {
        val boundForm = form.bind(Map("dateOfIncorporation.day" -> "31", "dateOfIncorporation.month" -> "4", "dateOfIncorporation.year" -> "2007"))

        val view = application.injector.instanceOf[DateOfIncorporationView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm)(request, messages(application)).toString
      }
    }

    "must render an empty form for a GET if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val result = route(application, getRequest()).value
        val view = application.injector.instanceOf[DateOfIncorporationView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form)(getRequest(), messages(application)).toString
      }
    }

    "must save the date and redirect for a POST if no existing data is found" in {
      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application = applicationBuilder(userAnswers = None)
        .overrides(
          bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
          bind[SessionRepository].toInstance(mockSessionRepository)
        )
        .build()

      running(application) {
        val result = route(application, postRequest()).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
        val savedAnswers = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(savedAnswers.capture())
        savedAnswers.getValue.id mustEqual userAnswersId
        savedAnswers.getValue.get(ControllingBodyDateOfIncorporationPage).value mustEqual validAnswer
      }
    }

    Seq(
      ("a new date", None, false, true),
      ("a changed date", Some(LocalDate.of(2007, 3, 27)), false, true),
      ("an unchanged date", Some(validAnswer), false, false),
      ("an unchanged date with previous changes", Some(validAnswer), true, true)
    ).foreach { case (description, previousDate, previouslyChanged, expectedChanged) =>
      s"set the submitted and change flags correctly for $description" in {
        val initialAnswers = previousDate
          .fold(emptyUserAnswers) { date =>
            emptyUserAnswers.set(ControllingBodyDateOfIncorporationPage, date).success.value
          }
          .set(ControllingBodyChangesPage, previouslyChanged)
          .success
          .value
          .set(ControllingBodySubmittedPage, false)
          .success
          .value
        val mockSessionRepository = mock[SessionRepository]
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
        val application = applicationBuilder(userAnswers = Some(initialAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val result = route(application, postRequest()).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
          val savedAnswers = ArgumentCaptor.forClass(classOf[UserAnswers])
          verify(mockSessionRepository).set(savedAnswers.capture())
          savedAnswers.getValue.get(ControllingBodyDateOfIncorporationPage).value mustEqual validAnswer
          savedAnswers.getValue.get(ControllingBodySubmittedPage).value mustEqual true
          savedAnswers.getValue.get(ControllingBodyChangesPage).value mustEqual expectedChanged
        }
      }
    }
  }
}
