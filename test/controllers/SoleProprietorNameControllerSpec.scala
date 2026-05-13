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

package controllers

import base.SpecBase
import forms.SoleProprietorNameFormProvider
import models.{NormalMode, SoleProprietorName, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.SoleProprietorPage
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.SoleProprietorNameView

import scala.concurrent.Future

class SoleProprietorNameControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute: Call = Call("GET", "/foo")

  private val formProvider = new SoleProprietorNameFormProvider()
  private val form = formProvider()

  private val validData = Map(
    "title"      -> "Mr",
    "firstName"  -> "John",
    "middleName" -> "A",
    "lastName"   -> "Doe"
  )

  private val model = SoleProprietorName(
    title      = "Mr",
    firstName  = "John",
    middleName = Some("A"),
    lastName   = "Doe"
  )

  private val populatedAnswers = UserAnswers(
    userAnswersId,
    Json.obj(
      SoleProprietorPage.toString -> Json.toJson(model)
    )
  )

  "SoleProprietorNameController" - {

    // ============================
    // GET
    // ============================
    "onPageLoad" - {

      "return OK and render empty form when no existing answer" in {

        val application = applicationBuilder(userAnswers = Some(UserAnswers(userAnswersId))).build()

        running(application) {
          val request = FakeRequest(GET, routes.SoleProprietorNameController.onPageLoad(NormalMode).url)
          val controller = application.injector.instanceOf[SoleProprietorNameController]
          val result = call(controller.onPageLoad(NormalMode), request)
          val view = application.injector.instanceOf[SoleProprietorNameView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
        }
      }

      "populate form when data exists" in {

        val application = applicationBuilder(userAnswers = Some(populatedAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, routes.SoleProprietorNameController.onPageLoad(NormalMode).url)
          val controller = application.injector.instanceOf[SoleProprietorNameController]
          val result = call(controller.onPageLoad(NormalMode), request)
          val view = application.injector.instanceOf[SoleProprietorNameView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form.fill(model), NormalMode)(request, messages(application)).toString
        }
      }

    }

    // ============================
    // POST
    // ============================
    "onSubmit" - {

      "redirect on valid submission" in {

        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any()))
          .thenReturn(Future.successful(true))

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, routes.SoleProprietorNameController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody(validData.toSeq*)
          val controller = application.injector.instanceOf[SoleProprietorNameController]

          val result = call(controller.onSubmit(NormalMode), request)

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      "return BAD_REQUEST when invalid data submitted" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request =
            FakeRequest(POST, routes.SoleProprietorNameController.onSubmit(NormalMode).url)
              .withFormUrlEncodedBody(
                "title"      -> "",
                "firstName"  -> "",
                "middleName" -> "",
                "lastName"   -> ""
              )

          val controller = application.injector.instanceOf[SoleProprietorNameController]
          val boundForm = form.bind(
            Map(
              "title"      -> "",
              "firstName"  -> "",
              "middleName" -> "",
              "lastName"   -> ""
            )
          )
          val view = application.injector.instanceOf[SoleProprietorNameView]
          val result = call(controller.onSubmit(NormalMode), request)

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
        }
      }

    }
  }
}
