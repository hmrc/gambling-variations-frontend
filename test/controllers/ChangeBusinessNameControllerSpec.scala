package controllers

import base.SpecBase
import forms.ChangeBusinessNameFormProvider
import models.{BusinessType, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.{BusinessNamePage, BusinessTypePage}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.ChangeBusinessNameView

import scala.concurrent.Future

class ChangeBusinessNameControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute: Call = Call("GET", "/foo")

  val formProvider = new ChangeBusinessNameFormProvider()

  val businessType: BusinessType = BusinessType.values.head
  val businessName = "Test Business"

  val form = formProvider(businessType)

  val data = Json.obj(
    BusinessTypePage.toString -> businessType.code,
    BusinessNamePage.toString -> businessName
  )

  lazy val changeBusinessNameRoute =
    routes.ChangeBusinessNameController.onPageLoad().url

  "ChangeBusinessName Controller" - {

    "must return OK and the correct view for a GET" in {

      val application =
        applicationBuilder(userAnswers = Some(UserAnswers("id", data))).build()

      running(application) {
        val request = FakeRequest(GET, changeBusinessNameRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ChangeBusinessNameView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form.fill(businessName), NormalMode, "What is the sole trader’s name?")(request, messages(application)).toString
      }
    }

    "must redirect" - {

      "to Check Business Name when no Business Name or Business Type exists" - {

        "when GET" in {

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

          running(application) {
            val request = FakeRequest(GET, changeBusinessNameRoute)

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual routes.CheckBusinessNameController.onPageLoad().url
          }
        }

        "when POST" in {

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

          running(application) {
            val request =
              FakeRequest(POST, changeBusinessNameRoute)
                .withFormUrlEncodedBody(("value", "New Name"))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual routes.CheckBusinessNameController.onPageLoad().url
          }
        }
      }

      "to the next page when valid data is submitted" in {

        val mockSessionRepository = mock[SessionRepository]
        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(UserAnswers("id", data)))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, changeBusinessNameRoute)
              .withFormUrlEncodedBody(("value", "Updated Business Name"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(UserAnswers("id", data))).build()

      running(application) {
        val request =
          FakeRequest(POST, changeBusinessNameRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[ChangeBusinessNameView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual
          view(boundForm, NormalMode, "What is the sole trader’s name?")(request, messages(application)).toString
      }
    }
  }
}
