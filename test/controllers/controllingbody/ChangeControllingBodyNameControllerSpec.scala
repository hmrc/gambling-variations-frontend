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
import models.{BusinessType, NormalMode, SoleProprietorName, UserAnswers}
import navigation.Navigator
import org.jsoup.Jsoup
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, verifyNoInteractions, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.controllingbody.{ControllingBodyBusinessNamePage, ControllingBodyBusinessTypePage, ControllingBodySectionPage, ControllingBodySoleProprietorPage}
import play.api.Application
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository

import scala.concurrent.Future

class ChangeControllingBodyNameControllerSpec extends SpecBase with MockitoSugar {

  private val onwardRoute = Call("GET", "/next-controlling-body-screen")
  private val businessTypes = BusinessType.values.filterNot(_ == BusinessType.Soleproprietor)
  private val proprietorName = SoleProprietorName("Mr", "Tom", Some("John"), "Smith")
  private val proprietorFields = Seq("title" -> "Mr", "firstName" -> "Tom", "middleName" -> "John", "lastName" -> "Smith")
  private val unrelatedData = Json.obj(
    "businessName" -> "Registered business",
    "partners"     -> Json.arr(Json.obj("partnerDetailsBusinessName" -> "Existing partner"))
  )

  private def answers(businessType: BusinessType): UserAnswers =
    UserAnswers(userAnswersId, unrelatedData)
      .set(ControllingBodyBusinessTypePage, businessType)
      .success
      .value
      .set(ControllingBodySectionPage, mgdRegNum)
      .success
      .value

  private def withApplication(userAnswers: Option[UserAnswers], saved: Boolean = true)(
    test: (Application, SessionRepository, Navigator) => Any
  ): Unit = {
    val repository = mock[SessionRepository]
    val connector = mock[GamblingConnector]
    when(connector.getControlBodyDetails(any())(any())) thenReturn Future.failed(new RuntimeException("Backend unavailable"))
    val navigator = mock[Navigator]
    when(repository.set(any())) thenReturn Future.successful(saved)
    when(navigator.nextPage(any(), any(), any())) thenReturn onwardRoute
    val application = applicationBuilder(userAnswers)
      .overrides(bind[SessionRepository].toInstance(repository), bind[Navigator].toInstance(navigator), bind[GamblingConnector].toInstance(connector))
      .build()

    running(application) {
      test(application, repository, navigator)
    }
  }

  "ChangeControllingBodyNameController" - {
    businessTypes.foreach { businessType =>
      s"for $businessType" - {
        "show the saved controlling body name with the correct heading and form action" in {
          val userAnswers = answers(businessType).set(ControllingBodyBusinessNamePage, "Controlling Body").success.value
          withApplication(Some(userAnswers)) { (application, repository, _) =>
            val result = route(application, FakeRequest(GET, routes.ChangeControllingBodyNameController.onPageLoad(businessType).url)).value
            status(result) mustEqual OK
            val doc = Jsoup.parse(contentAsString(result))
            doc.select("h1").text() mustEqual messages(application)(s"changeBusinessName.heading.$businessType")
            doc.select("#value").`val`() mustEqual "Controlling Body"
            doc.select("form").attr("action") mustEqual routes.ChangeControllingBodyNameController.onSubmit(businessType).url
            verifyNoInteractions(repository)
          }
        }

        "show a blank field when no name is cached" in {
          withApplication(Some(answers(businessType))) { (application, _, _) =>
            val result = route(application, FakeRequest(GET, routes.ChangeControllingBodyNameController.onPageLoad(businessType).url)).value
            status(result) mustEqual OK
            Jsoup.parse(contentAsString(result)).select("#value").`val`() mustEqual ""
          }
        }

        "save the trimmed name without changing registered business or partner data and continue" in {
          val userAnswers = answers(businessType).set(ControllingBodyBusinessNamePage, "Old Name").success.value
          withApplication(Some(userAnswers)) { (application, repository, navigator) =>
            val request = FakeRequest(POST, routes.ChangeControllingBodyNameController.onSubmit(businessType).url)
              .withFormUrlEncodedBody("value" -> "  Updated Name  ")
            val result = route(application, request).value
            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual onwardRoute.url
            val captor = ArgumentCaptor.forClass(classOf[UserAnswers])
            verify(repository).set(captor.capture())
            val savedAnswers = captor.getValue
            savedAnswers mustEqual userAnswers.set(ControllingBodyBusinessNamePage, "Updated Name").success.value
            verify(navigator).nextPage(ControllingBodyBusinessNamePage, NormalMode, savedAnswers)
          }
        }

        "accept a name at the maximum length" in {
          val length = if (businessType == BusinessType.Partnership) 35 else 160
          withApplication(Some(answers(businessType))) { (application, repository, _) =>
            val request = FakeRequest(POST, routes.ChangeControllingBodyNameController.onSubmit(businessType).url)
              .withFormUrlEncodedBody("value" -> ("A" * length))
            val result = route(application, request).value
            status(result) mustEqual SEE_OTHER
            val captor = ArgumentCaptor.forClass(classOf[UserAnswers])
            verify(repository).set(captor.capture())
            captor.getValue.get(ControllingBodyBusinessNamePage).value mustEqual ("A" * length)
          }
        }

        "display required, invalid-character and length errors without saving" in {
          val length = if (businessType == BusinessType.Partnership) 35 else 160
          withApplication(Some(answers(businessType))) { (application, repository, navigator) =>
            Seq("   " -> "required", "Name@" -> "invalid", "Name." -> "invalid", ("A" * (length + 1)) -> "length").foreach { case (value, error) =>
              val request = FakeRequest(POST, routes.ChangeControllingBodyNameController.onSubmit(businessType).url)
                .withFormUrlEncodedBody("value" -> value)
              val result = route(application, request).value
              status(result) mustEqual BAD_REQUEST
              val doc = Jsoup.parse(contentAsString(result))
              doc.select(".govuk-error-summary a").attr("href") mustEqual "#value"
              doc.select(".govuk-error-summary").text() must include(messages(application)(s"changeBusinessName.error.$error.$businessType"))
              doc.select("#value").`val`() mustEqual value
            }
            verifyNoInteractions(repository, navigator)
          }
        }

        "show a service error when the name cannot be saved" in {
          withApplication(Some(answers(businessType)), saved = false) { (application, _, navigator) =>
            val request = FakeRequest(POST, routes.ChangeControllingBodyNameController.onSubmit(businessType).url)
              .withFormUrlEncodedBody("value" -> "New Name")
            val result = route(application, request).value
            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.routes.SystemErrorController.onPageLoad().url
            verifyNoInteractions(navigator)
          }
        }
      }
    }

    "for a sole proprietor" - {
      "show all saved name fields" in {
        val userAnswers = answers(BusinessType.Soleproprietor).set(ControllingBodySoleProprietorPage, proprietorName).success.value
        withApplication(Some(userAnswers)) { (application, repository, _) =>
          val result =
            route(application, FakeRequest(GET, routes.ChangeControllingBodyNameController.onPageLoad(BusinessType.Soleproprietor).url)).value
          status(result) mustEqual OK
          val doc = Jsoup.parse(contentAsString(result))
          proprietorFields.foreach { case (field, value) => doc.select(s"#$field").`val`() mustEqual value }
          doc.select("h1").text() mustEqual messages(application)("soleProprietorName.heading")
          verifyNoInteractions(repository)
        }
      }

      "show blank fields when no name is cached" in {
        withApplication(Some(answers(BusinessType.Soleproprietor))) { (application, _, _) =>
          val result =
            route(application, FakeRequest(GET, routes.ChangeControllingBodyNameController.onPageLoad(BusinessType.Soleproprietor).url)).value
          status(result) mustEqual OK
          val doc = Jsoup.parse(contentAsString(result))
          proprietorFields.foreach { case (field, _) => doc.select(s"#$field").`val`() mustEqual "" }
        }
      }

      Seq(Some("John"), None).foreach { middleName =>
        s"save a name with middle name $middleName and continue without changing other details" in {
          val userAnswers = answers(BusinessType.Soleproprietor).set(ControllingBodySoleProprietorPage, proprietorName).success.value
          withApplication(Some(userAnswers)) { (application, repository, navigator) =>
            val request = FakeRequest(POST, routes.ChangeControllingBodyNameController.onSubmit(BusinessType.Soleproprietor).url)
              .withFormUrlEncodedBody("title" -> " Ms ", "firstName" -> " Jane ", "middleName" -> middleName.getOrElse("  "), "lastName" -> " Jones ")
            val result = route(application, request).value
            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual onwardRoute.url
            val captor = ArgumentCaptor.forClass(classOf[UserAnswers])
            verify(repository).set(captor.capture())
            val savedAnswers = captor.getValue
            savedAnswers mustEqual userAnswers
              .set(ControllingBodySoleProprietorPage, SoleProprietorName("Ms", "Jane", middleName, "Jones"))
              .success
              .value
            verify(navigator).nextPage(ControllingBodySoleProprietorPage, NormalMode, savedAnswers)
          }
        }
      }

      "display errors and preserve entered values without saving" in {
        withApplication(Some(answers(BusinessType.Soleproprietor))) { (application, repository, navigator) =>
          val request = FakeRequest(POST, routes.ChangeControllingBodyNameController.onSubmit(BusinessType.Soleproprietor).url)
            .withFormUrlEncodedBody("title" -> "", "firstName" -> "Jane@", "middleName" -> "Jane", "lastName" -> ("A" * 101))
          val result = route(application, request).value
          status(result) mustEqual BAD_REQUEST
          val doc = Jsoup.parse(contentAsString(result))
          Seq("title" -> "required", "firstName" -> "invalid", "lastName" -> "length").foreach { case (field, error) =>
            doc.select(s".govuk-error-summary a[href='#$field']").text() mustEqual messages(application)(s"soleProprietorName.error.$field.$error")
          }
          doc.select("#firstName").`val`() mustEqual "Jane@"
          doc.select("#middleName").`val`() mustEqual "Jane"
          verifyNoInteractions(repository, navigator)
        }
      }

      "show a service error when the name cannot be saved" in {
        withApplication(Some(answers(BusinessType.Soleproprietor)), saved = false) { (application, _, navigator) =>
          val request = FakeRequest(POST, routes.ChangeControllingBodyNameController.onSubmit(BusinessType.Soleproprietor).url)
            .withFormUrlEncodedBody(proprietorFields*)
          val result = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.SystemErrorController.onPageLoad().url
          verifyNoInteractions(navigator)
        }
      }
    }

    Seq(GET, POST).foreach { method =>
      Seq(
        "missing session"                    -> None,
        "missing business type"              -> Some(emptyUserAnswers.set(ControllingBodySectionPage, mgdRegNum).success.value),
        "different business type in the URL" -> Some(answers(BusinessType.Soleproprietor)),
        "invalid cached business type" -> Some(
          UserAnswers(userAnswersId, Json.obj("controllingBodyDetails" -> Json.obj("mgdRegNum" -> mgdRegNum, "typeOfControllingBody" -> 99)))
        )
      ).foreach { case (scenario, userAnswers) =>
        s"redirect $method to the service error without saving when there is $scenario" in {
          withApplication(userAnswers) { (application, repository, navigator) =>
            val request = FakeRequest(method, routes.ChangeControllingBodyNameController.onPageLoad(BusinessType.Partnership).url)
              .withFormUrlEncodedBody("value" -> "New Name")
            val result = route(application, request).value
            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.routes.SystemErrorController.onPageLoad().url
            verifyNoInteractions(repository, navigator)
          }
        }
      }
    }
  }
}
