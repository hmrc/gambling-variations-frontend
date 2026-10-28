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
import connectors.GamblingConnector
import models.controllingbody.ControlBodyDetails
import forms.controllingbody.ControllingBodyTradingNameFormProvider
import models.{BusinessType, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, verifyNoInteractions, when}
import org.mockito.ArgumentCaptor
import org.scalatestplus.mockito.MockitoSugar
import pages.controllingbody.{ControllingBodyBusinessTypePage, ControllingBodySectionPage, ControllingBodyTradingNamePage}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.controllingbody.ControllingBodyTradingNameView

import scala.concurrent.Future

class ControllingBodyTradingNameControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/trading-name")

  val formProvider = new ControllingBodyTradingNameFormProvider()
  val form = formProvider()

  lazy val controllingBodyTradingNameRoute = routes.ControllingBodyTradingNameController.onPageLoad().url

  private def loadedAnswers(bodyType: BusinessType = BusinessType.Soleproprietor): UserAnswers =
    emptyUserAnswers
      .set(ControllingBodySectionPage, userAnswersId)
      .success
      .value
      .set(ControllingBodyBusinessTypePage, bodyType)
      .success
      .value

  "ControllingBodyTradingName Controller" - {

    "render the registration-details caption and page title" in {
      val application = applicationBuilder(userAnswers = Some(loadedAnswers())).build()
      running(application) {
        val result = route(application, FakeRequest(GET, controllingBodyTradingNameRoute)).value
        status(result) mustEqual OK
        val document = org.jsoup.Jsoup.parse(contentAsString(result))
        document.select("span.govuk-caption-l").text() mustEqual "Change registration details"
        document.title() must include("Change registration details")
      }
    }

    Seq(
      "" -> "Enter the controlling body’s trading name",
      "Trading@Name" -> "The controlling body’s trading name must only include letters a to z, numbers 0 to 9, apostrophes, hyphens, slashes or spaces",
      ("A" * 101) -> "The controlling body’s trading name must be 100 characters or less"
    ).foreach { case (value, expectedError) =>
      s"render the exact error copy: $expectedError" in {
        val application = applicationBuilder(userAnswers = Some(loadedAnswers())).build()
        running(application) {
          val request = FakeRequest(POST, controllingBodyTradingNameRoute)
            .withFormUrlEncodedBody("controllingBodyTradingName" -> value)
          val result = route(application, request).value
          status(result) mustEqual BAD_REQUEST
          val document = org.jsoup.Jsoup.parse(contentAsString(result))
          document.select(".govuk-error-summary__list a").text() mustEqual expectedError
          document.select(".govuk-error-message").text() must include(expectedError)
        }
      }
    }

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(loadedAnswers())).build()

      running(application) {
        val request = FakeRequest(GET, controllingBodyTradingNameRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ControllingBodyTradingNameView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, BusinessType.Soleproprietor)(request, messages(application)).toString
      }
    }

    "must render the form with its POST route" in {
      val application = applicationBuilder(userAnswers = Some(loadedAnswers())).build()
      running(application) {
        val url = routes.ControllingBodyTradingNameController.onPageLoad().url
        val request = FakeRequest(GET, url)
        val result = route(application, request).value
        status(result) mustEqual OK
        val doc = org.jsoup.Jsoup.parse(contentAsString(result))
        doc.select("form").attr("action") mustEqual routes.ControllingBodyTradingNameController.onSubmit().url
        val invalid = route(application, FakeRequest(POST, url).withFormUrlEncodedBody("controllingBodyTradingName" -> "")).value
        status(invalid) mustEqual BAD_REQUEST
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = loadedAnswers().set(ControllingBodyTradingNamePage, "answer").success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllingBodyTradingNameRoute)

        val view = application.injector.instanceOf[ControllingBodyTradingNameView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill("answer"), NormalMode, BusinessType.Soleproprietor)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(loadedAnswers()))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, controllingBodyTradingNameRoute)
            .withFormUrlEncodedBody(("controllingBodyTradingName", "answer"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(loadedAnswers())).build()

      running(application) {
        val request =
          FakeRequest(POST, controllingBodyTradingNameRoute)
            .withFormUrlEncodedBody(("controllingBodyTradingName", ""))

        val boundForm = form.bind(Map("controllingBodyTradingName" -> ""))

        val view = application.injector.instanceOf[ControllingBodyTradingNameView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, BusinessType.Soleproprietor)(request, messages(application)).toString
      }
    }

    BusinessType.values.foreach { bodyType =>
      s"show the hint only for a sole proprietor on GET and validation errors for $bodyType" in {
        val answers = UserAnswers(loadedAnswers(bodyType).id, loadedAnswers(bodyType).data ++ Json.obj("businessType" -> 1))
        val repository = mock[SessionRepository]
        when(repository.set(any())).thenReturn(Future.successful(true))
        val application = applicationBuilder(userAnswers = Some(answers))
          .overrides(bind[SessionRepository].toInstance(repository), bind[Navigator].toInstance(new FakeNavigator(onwardRoute)))
          .build()
        running(application) {
          Seq(GET, POST).foreach { method =>
            val request = FakeRequest(method, controllingBodyTradingNameRoute)
              .withFormUrlEncodedBody("controllingBodyTradingName" -> "")
            val result = route(application, request).value
            status(result) mustEqual (request.method match { case GET => OK; case _ => BAD_REQUEST })
            val doc = org.jsoup.Jsoup.parse(contentAsString(result))
            doc.select("#controllingBodyTradingName-hint").size() mustEqual
              (bodyType match { case BusinessType.Soleproprietor => 1; case _ => 0 })
            doc.select("#controllingBodyTradingName").attr("autocomplete") mustEqual "organization"
          }
          val submitted = route(application,
                                FakeRequest(POST, controllingBodyTradingNameRoute)
                                  .withFormUrlEncodedBody("controllingBodyTradingName" -> "Valid Trading Name")
                               ).value
          status(submitted) mustEqual SEE_OTHER
          redirectLocation(submitted).value mustEqual onwardRoute.url
        }
      }
    }

    Seq(GET, POST).foreach { method =>
      Seq(
        "missing type"           -> Json.obj("mgdRegNum" -> userAnswersId),
        "invalid type"           -> Json.obj("mgdRegNum" -> userAnswersId, "typeOfControllingBody" -> 99),
        "empty registration"     -> Json.obj("mgdRegNum" -> "", "typeOfControllingBody" -> 1),
        "different registration" -> Json.obj("mgdRegNum" -> "another-registration", "typeOfControllingBody" -> 1)
      ).foreach { case (scenario, details) =>
        s"reject $method with cached $scenario without saving a trading name" in {
          val repository = mock[SessionRepository]
          val navigator = mock[Navigator]
          val answers = UserAnswers(userAnswersId, Json.obj("controllingBodyDetails" -> details, "businessType" -> 1))
          val application = applicationBuilder(userAnswers = Some(answers))
            .overrides(bind[SessionRepository].toInstance(repository), bind[Navigator].toInstance(navigator))
            .build()
          running(application) {
            val result = route(application,
                               FakeRequest(method, controllingBodyTradingNameRoute)
                                 .withFormUrlEncodedBody("controllingBodyTradingName" -> "Should not save")
                              ).value
            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.routes.SystemErrorController.onPageLoad().url
            verifyNoInteractions(repository, navigator)
          }
        }
      }

      Seq(
        "unknown registration" -> Future.successful(Json.obj("mgdRegNumber" -> "").as[ControlBodyDetails]),
        "backend failure"      -> Future.failed[ControlBodyDetails](new RuntimeException("Not found"))
      ).foreach { case (scenario, backendResult) =>
        s"reject $method for $scenario when there is no cache" in {
          val connector = mock[GamblingConnector]
          val repository = mock[SessionRepository]
          val navigator = mock[Navigator]
          when(connector.getControlBodyDetails(any())(any())).thenReturn(backendResult)
          when(repository.set(any())).thenReturn(Future.successful(true))
          val application = applicationBuilder(userAnswers = None)
            .overrides(
              bind[GamblingConnector].toInstance(connector),
              bind[SessionRepository].toInstance(repository),
              bind[Navigator].toInstance(navigator)
            )
            .build()
          running(application) {
            val result = route(application,
                               FakeRequest(method, controllingBodyTradingNameRoute)
                                 .withFormUrlEncodedBody("controllingBodyTradingName" -> "Should not save")
                              ).value
            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.routes.SystemErrorController.onPageLoad().url
            verify(connector).getControlBodyDetails(org.mockito.ArgumentMatchers.eq(userAnswersId))(any())
            verifyNoInteractions(navigator)
          }
        }
      }
    }

    "fetch and prefill valid controlling-body details when there is no cache" in {
      val connector = mock[GamblingConnector]
      val repository = mock[SessionRepository]
      val details =
        Json.obj("mgdRegNumber" -> userAnswersId, "typeOfControllingBody" -> 1, "tradingName" -> "Existing Trading Name").as[ControlBodyDetails]
      when(connector.getControlBodyDetails(any())(any())).thenReturn(Future.successful(details))
      when(repository.set(any())).thenReturn(Future.successful(true))
      val application = applicationBuilder(userAnswers = None)
        .overrides(
          bind[GamblingConnector].toInstance(connector),
          bind[SessionRepository].toInstance(repository)
        )
        .build()
      running(application) {
        val result = route(application, FakeRequest(GET, controllingBodyTradingNameRoute)).value
        status(result) mustEqual OK
        val doc = org.jsoup.Jsoup.parse(contentAsString(result))
        doc.select("#controllingBodyTradingName").`val`() mustEqual "Existing Trading Name"
        doc.select("#controllingBodyTradingName-hint").size() mustEqual 1
        verify(connector).getControlBodyDetails(org.mockito.ArgumentMatchers.eq(userAnswersId))(any())
        val saved = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(repository).set(saved.capture())
        saved.getValue.get(ControllingBodyTradingNamePage).value mustEqual "Existing Trading Name"
      }
    }
  }
}
