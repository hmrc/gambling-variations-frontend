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
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.{get, getRequestedFor, okJson, urlEqualTo}
import controllers.actions.{AuthorisedAction, FakeAuthorisedAction}
import models.BusinessType.*
import models.UserAnswers
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.mockito.invocation.InvocationOnMock
import org.scalatestplus.mockito.MockitoSugar
import pages.controllingbody.{ControllingBodyBusinessNamePage, ControllingBodySectionPage}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository

import java.util.concurrent.atomic.AtomicReference
import scala.concurrent.Future

class ControllingBodyNameJourneySpec extends SpecBase with MockitoSugar {

  "The controlling body name journey" - {
    Seq(
      Partnership                 -> "New & Partners",
      Corporatebody               -> "New Company",
      Unincorporatedbody          -> "New Association",
      LimitedLiabilityPartnership -> "New LLP"
    ).foreach { case (businessType, updatedName) =>
      s"load I1.36, save $businessType, reach the trading-name screener and retain the edit on return" in {
        val backend = new WireMockServer(0)
        backend.start()
        try {
          val endpoint = s"/gambling/controlling-body-details/mgd/$userAnswersId"
          backend.stubFor(
            get(urlEqualTo(endpoint)).willReturn(
              okJson(
                Json
                  .obj(
                    "mgdRegNumber"          -> userAnswersId,
                    "typeOfControllingBody" -> businessType.code,
                    "businessName"          -> "Original Name"
                  )
                  .toString
              )
            )
          )

          val cached = new AtomicReference[Option[UserAnswers]](None)
          val repository = mock[SessionRepository]
          when(repository.get(any())).thenAnswer((_: InvocationOnMock) => Future.successful(cached.get()))
          when(repository.set(any())).thenAnswer((invocation: InvocationOnMock) => {
            cached.set(Some(invocation.getArgument[UserAnswers](0)))
            Future.successful(true)
          })

          val application = new GuiceApplicationBuilder()
            .configure(
              "microservice.services.gambling.protocol" -> "http",
              "microservice.services.gambling.host"     -> "localhost",
              "microservice.services.gambling.port"     -> backend.port()
            )
            .overrides(bind[AuthorisedAction].to[FakeAuthorisedAction], bind[SessionRepository].toInstance(repository))
            .build()

          running(application) {
            val nameUrl = routes.ChangeControllingBodyNameController.onPageLoad(businessType).url
            val initial = route(application, FakeRequest(GET, nameUrl)).value
            status(initial) mustBe OK
            Jsoup.parse(contentAsString(initial)).select("#value").`val`() mustBe "Original Name"
            cached.get().value.get(ControllingBodySectionPage).value mustBe userAnswersId

            val invalid = route(application, FakeRequest(POST, nameUrl).withFormUrlEncodedBody("value" -> "New.Name")).value
            status(invalid) mustBe BAD_REQUEST
            cached.get().value.get(ControllingBodyBusinessNamePage).value mustBe "Original Name"

            val submitted = route(application, FakeRequest(POST, nameUrl).withFormUrlEncodedBody("value" -> updatedName)).value
            status(submitted) mustBe SEE_OTHER
            redirectLocation(submitted).value mustBe routes.ControllingBodyAddTradingNameYesNoController.onPageLoad().url
            cached.get().value.get(ControllingBodyBusinessNamePage).value mustBe updatedName

            val next = route(application, FakeRequest(GET, redirectLocation(submitted).value)).value
            status(next) mustBe OK
            val nextDoc = Jsoup.parse(contentAsString(next))
            nextDoc.select("h1").text() mustBe "Do you want to add a trading name for the controlling body?"
            nextDoc.select("input[type=radio]").size() mustBe 2

            val revisited = route(application, FakeRequest(GET, nameUrl)).value
            status(revisited) mustBe OK
            Jsoup.parse(contentAsString(revisited)).select("#value").`val`() mustBe updatedName
            backend.verify(1, getRequestedFor(urlEqualTo(endpoint)))
          }
        } finally backend.stop()
      }
    }
  }
}
