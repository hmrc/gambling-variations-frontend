/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 */

package controllers

import base.SpecBase
import forms.SoleProprietorNameFormProvider
import models.{NormalMode, SoleProprietorName, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.SoleProprietorNamePage
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.SoleProprietorNameFormView

import scala.concurrent.Future

class SoleProprietorNameControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute: Call = Call("GET", "/foo")

  private val formProvider = new SoleProprietorNameFormProvider()
  private val form = formProvider()

  private val routeUrl =
    routes.SoleProprietorNameController.onPageLoad(NormalMode).url

  // ------------------------------------------
  // Test Data
  // ------------------------------------------

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
      SoleProprietorNamePage.toString -> Json.toJson(model)
    )
  )

  // ------------------------------------------
  // Tests
  // ------------------------------------------

  "SoleProprietorNameController" - {

    // ============================
    // GET
    // ============================

    "onPageLoad" - {

      "return OK and render empty form when no existing answer" in {

        val application = applicationBuilder(userAnswers = Some(validUserAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, routeUrl)

          val result = route(application, request).value

          status(result) mustEqual OK

          val content = contentAsString(result)

          content must include("""name="title"""")
          content must include("""name="firstName"""")
          content must include("""name="lastName"""")
        }
      }

      "populate form when data exists" in {

        val application = applicationBuilder(userAnswers = Some(populatedAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, routeUrl)

          val result = route(application, request).value
          val content = contentAsString(result)

          status(result) mustEqual OK

          content must include("""value="Mr"""")
          content must include("""value="John"""")
          content must include("""value="A"""")
          content must include("""value="Doe"""")
        }
      }

      "redirect to Journey Recovery when no UserAnswers" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, routeUrl)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            routes.JourneyRecoveryController.onPageLoad().url
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
          applicationBuilder(userAnswers = Some(validUserAnswers))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, routeUrl)
              .withFormUrlEncodedBody(validData.toSeq*)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      "return BAD_REQUEST when invalid data submitted" in {

        val application = applicationBuilder(userAnswers = Some(validUserAnswers)).build()

        running(application) {
          val request =
            FakeRequest(POST, routeUrl)
              .withFormUrlEncodedBody(
                "title"      -> "",
                "firstName"  -> "",
                "middleName" -> "",
                "lastName"   -> ""
              )

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST

          val content = contentAsString(result)

          content must include("soleProprietorNameForm.error.title.required")
          content must include("soleProprietorNameForm.error.firstName.required")
          content must include("soleProprietorNameForm.error.lastName.required")
        }
      }

      "redirect to Journey Recovery when no UserAnswers" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request =
            FakeRequest(POST, routeUrl)
              .withFormUrlEncodedBody(validData.toSeq*)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual
            routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
}
