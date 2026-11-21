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
import models.{BusinessType, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, verifyNoInteractions, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.controllingbody.{ControllingBodyBusinessTypePage, ControllingBodyChangesPage, ControllingBodyDateOfIncorporationPage, ControllingBodySubmittedPage}
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

  private val validUserAnswers = emptyUserAnswers.set(ControllingBodyBusinessTypePage, BusinessType.Corporatebody).success.value

  def getRequest(): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, dateOfIncorporationRoute)

  def postRequest(): FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest(POST, dateOfIncorporationRoute)
      .withFormUrlEncodedBody(
        "value.day"   -> validAnswer.getDayOfMonth.toString,
        "value.month" -> validAnswer.getMonthValue.toString,
        "value.year"  -> validAnswer.getYear.toString
      )

  "DateOfIncorporation Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(validUserAnswers)).build()

      running(application) {
        val result = route(application, getRequest()).value

        val view = application.injector.instanceOf[DateOfIncorporationView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form)(getRequest(), messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = validUserAnswers.set(ControllingBodyDateOfIncorporationPage, validAnswer).success.value

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
        applicationBuilder(userAnswers = Some(validUserAnswers))
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

    Seq(BusinessType.Corporatebody, BusinessType.LimitedLiabilityPartnership).foreach { businessType =>
      s"must return OK for a GET when the business type is $businessType" in {
        val answers = emptyUserAnswers.set(ControllingBodyBusinessTypePage, businessType).success.value
        val application = applicationBuilder(userAnswers = Some(answers)).build()

        running(application) {
          val result = route(application, getRequest()).value
          val view = application.injector.instanceOf[DateOfIncorporationView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form)(getRequest(), messages(application)).toString
        }
      }

      s"must redirect a new $businessType to company registration number when a valid date is submitted" in {
        val mockSessionRepository = mock[SessionRepository]
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val answers = emptyUserAnswers.set(ControllingBodyBusinessTypePage, businessType).success.value
        val application = applicationBuilder(userAnswers = Some(answers))
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val result = route(application, postRequest()).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.ControllingBodyCompanyRegistrationNumberController.onPageLoad().url
        }
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(validUserAnswers)).build()

      val request =
        FakeRequest(POST, dateOfIncorporationRoute)
          .withFormUrlEncodedBody("value.day" -> "31", "value.month" -> "4", "value.year" -> "2007")

      running(application) {
        val boundForm = form.bind(Map("value.day" -> "31", "value.month" -> "4", "value.year" -> "2007"))

        val view = application.injector.instanceOf[DateOfIncorporationView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm)(request, messages(application)).toString
      }
    }

    "must return a Bad Request with the invalid date error when invalid fields are submitted with a missing year" in {

      val application = applicationBuilder(userAnswers = Some(validUserAnswers)).build()

      val invalidData = Map("value.day" -> "$$", "value.month" -> "me", "value.year" -> "")

      val request =
        FakeRequest(POST, dateOfIncorporationRoute)
          .withFormUrlEncodedBody(invalidData.toSeq*)

      running(application) {
        val boundForm = formProvider()(messages(application)).bind(invalidData)

        val view = application.injector.instanceOf[DateOfIncorporationView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        boundForm.errors.map(_.message).distinct mustEqual Seq("dateOfIncorporation.error.invalid")
        contentAsString(result) mustEqual view(boundForm)(request, messages(application)).toString
      }
    }

    "must redirect to System Error for a GET when no session exists" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val result = route(application, getRequest()).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.SystemErrorController.onPageLoad().url
      }
    }

    "must redirect to System Error without saving for a POST when no session exists" in {
      val mockSessionRepository = mock[SessionRepository]
      val application = applicationBuilder(userAnswers = None)
        .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
        .build()

      running(application) {
        val result = route(application, postRequest()).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.SystemErrorController.onPageLoad().url
        verifyNoInteractions(mockSessionRepository)
      }
    }

    Seq(None, Some(BusinessType.Soleproprietor), Some(BusinessType.Partnership), Some(BusinessType.Unincorporatedbody))
      .foreach { businessType =>
        s"must reject GET and POST requests without saving when the business type is $businessType" in {
          val mockSessionRepository = mock[SessionRepository]
          val answers = emptyUserAnswers.setIfDefined(ControllingBodyBusinessTypePage, businessType).success.value
          val application = applicationBuilder(userAnswers = Some(answers))
            .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
            .build()

          running(application) {
            val invalidRequest = FakeRequest(POST, dateOfIncorporationRoute).withFormUrlEncodedBody("value.day" -> "invalid")
            Seq(
              route(application, getRequest()).value,
              route(application, postRequest()).value,
              route(application, invalidRequest).value
            ).foreach { result =>
              status(result) mustEqual SEE_OTHER
              redirectLocation(result).value mustEqual controllers.routes.SystemErrorController.onPageLoad().url
            }
            verifyNoInteractions(mockSessionRepository)
          }
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
          .fold(validUserAnswers) { date =>
            validUserAnswers.set(ControllingBodyDateOfIncorporationPage, date).success.value
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
