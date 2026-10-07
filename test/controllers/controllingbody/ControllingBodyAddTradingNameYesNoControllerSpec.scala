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
import models.BusinessType
import navigation.{FakeNavigator, Navigator}
import org.jsoup.Jsoup
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, verifyNoInteractions, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.controllingbody.{ControllingBodyAddTradingNameYesNoPage, ControllingBodyBusinessTypePage, ControllingBodyDetailsLoadedPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository

import scala.concurrent.Future

class ControllingBodyAddTradingNameYesNoControllerSpec extends SpecBase with MockitoSugar {
  private val answers = emptyUserAnswers
    .set(ControllingBodyBusinessTypePage, BusinessType.Partnership)
    .success
    .value
    .set(ControllingBodyDetailsLoadedPage, true)
    .success
    .value
  private val url = routes.ControllingBodyAddTradingNameYesNoController.onPageLoad().url

  "ControllingBodyAddTradingNameYesNoController" - {
    "render the specified title, caption, radios, Back and Continue" in {
      val application = applicationBuilder(Some(answers)).build()
      running(application) {
        val result = route(application, FakeRequest(GET, url)).value
        status(result) mustBe OK
        val doc = Jsoup.parse(contentAsString(result))
        doc.title() mustBe "Do you want to add a trading name for the controlling body? - Change registration details - Manage your gambling tax - GOV.UK"
        doc.select("h1").text() mustBe "Do you want to add a trading name for the controlling body?"
        doc.select(".govuk-caption-l").text() mustBe "Change registration details"
        doc.select("input[type=radio]").size() mustBe 2
        doc.select("input[checked]").size() mustBe 0
        doc.select(".govuk-back-link").text() mustBe "Back"
        doc.select("button.govuk-button").text() mustBe "Continue"
        doc.select("form").attr("action") mustBe routes.ControllingBodyAddTradingNameYesNoController.onSubmit().url
      }
    }

    Seq(true, false).foreach { choice =>
      s"prefill a previously selected answer of $choice" in {
        val application = applicationBuilder(Some(answers.set(ControllingBodyAddTradingNameYesNoPage, choice).success.value)).build()
        running(application) {
          val result = route(application, FakeRequest(GET, url)).value
          status(result) mustBe OK
          Jsoup.parse(contentAsString(result)).select("input[checked]").attr("value") mustBe choice.toString
        }
      }

      s"save the answer $choice before continuing" in {
        val repository = mock[SessionRepository]
        when(repository.set(any())) thenReturn Future.successful(true)
        val next = Call("GET", "/next-controlling-body-step")
        val application = applicationBuilder(Some(answers))
          .overrides(bind[SessionRepository].toInstance(repository), bind[Navigator].toInstance(new FakeNavigator(next)))
          .build()
        running(application) {
          val result = route(application, FakeRequest(POST, url).withFormUrlEncodedBody("value" -> choice.toString)).value
          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe next.url
          val captor = ArgumentCaptor.forClass(classOf[models.UserAnswers])
          verify(repository).set(captor.capture())
          captor.getValue mustBe answers.set(ControllingBodyAddTradingNameYesNoPage, choice).success.value
        }
      }
    }

    "display the required-choice error without saving" in {
      val repository = mock[SessionRepository]
      val application = applicationBuilder(Some(answers)).overrides(bind[SessionRepository].toInstance(repository)).build()
      running(application) {
        val result = route(application, FakeRequest(POST, url).withFormUrlEncodedBody("value" -> "")).value
        status(result) mustBe BAD_REQUEST
        val doc = Jsoup.parse(contentAsString(result))
        doc.select(".govuk-error-summary").text() must include("Select yes if you want to add a trading name for the controlling body")
        verifyNoInteractions(repository)
      }
    }

    "redirect to the service error when saving fails" in {
      val repository = mock[SessionRepository]
      when(repository.set(any())) thenReturn Future.successful(false)
      val application = applicationBuilder(Some(answers)).overrides(bind[SessionRepository].toInstance(repository)).build()
      running(application) {
        val result = route(application, FakeRequest(POST, url).withFormUrlEncodedBody("value" -> "true")).value
        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe controllers.routes.SystemErrorController.onPageLoad().url
      }
    }
  }
}
