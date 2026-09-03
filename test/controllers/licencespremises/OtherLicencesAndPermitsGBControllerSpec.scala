package controllers.licencespremises

import base.SpecBase
import forms.licencespremises.OtherLicencesAndPermitsGBFormProvider
import models.licencespremises.OtherLicencesAndPermitsGB
import models.licencespremises.OtherLicencesAndPermitsGB.*
import viewmodels.OtherLicencesAndPermitsViewModel.*
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.licencespremises.OtherLicencesAndPermitsGBView

import scala.concurrent.Future

class OtherLicencesAndPermitsGBControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  lazy val otherLicencesAndPermitsGBRoute: String =
    controllers.licencespremises.routes.OtherLicencesAndPermitsGBController.onPageLoad().url

  val formProvider = new OtherLicencesAndPermitsGBFormProvider()
  val form = formProvider()
  val blankAnswers = UserAnswers(userAnswersId,
    Json.obj(
      "licencesPremisesSection" -> Json.obj(
        "mgdRegNumber" -> "XGM000001761"))
    )

  val userAnswers = UserAnswers(
    userAnswersId,
    Json.obj(
      "licencesPremisesSection" -> Json.obj(
        "mgdRegNumber" -> "XGM000001761",
        "clubGaming" -> "1",
        "clubLicence" -> "0",
        "clubPremises" -> "1",
        "familyEntertainment" -> "0",
        "localAuthority" -> "1",
        "onPremises" -> "0",
        "prizeGaming" -> "1"
      )
    )
  )

  "OtherLicencesAndPermitsGB Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(blankAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, otherLicencesAndPermitsGBRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[OtherLicencesAndPermitsGBView]
        val preparedForm = form.fill(getSelectedLicencesAndPermits(blankAnswers)(messages(application)))
        val checkboxes = otherLPCheckboxItems(preparedForm)(messages(application))

        status(result) mustEqual OK

        contentAsString(result) mustBe view(form, NormalMode, checkboxes)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(
        "userAnswersId",
        Json.obj(
          "licencesPremisesSection" -> Json.obj(
            "mgdRegNumber"        -> "XGM000001761",
            "clubGaming"          -> "1",
            "clubLicence"         -> "0",
            "clubPremises"        -> "1",
            "familyEntertainment" -> "0",
            "localAuthority"      -> "1",
            "onPremises"          -> "0",
            "prizeGaming"         -> "1"
          )
        )
      )

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, otherLicencesAndPermitsGBRoute)
        val view = application.injector.instanceOf[OtherLicencesAndPermitsGBView]

        val result = route(application, request).value
        val preparedForm = form.fill(getSelectedLicencesAndPermits(userAnswers)(messages(application)))
        val checkboxes = otherLPCheckboxItems(preparedForm)(messages(application))
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(preparedForm, NormalMode, checkboxes)(request, messages(application)).toString
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
        val request =
          FakeRequest(POST, otherLicencesAndPermitsGBRoute)
            .withFormUrlEncodedBody(("permitsGB[]", clubMachine.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(blankAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, otherLicencesAndPermitsGBRoute)
            .withFormUrlEncodedBody(("permitsGB[]", "invalid value"))

        val boundForm = form.bind(Map("permitsGB[]" -> "invalid value"))

        val view = application.injector.instanceOf[OtherLicencesAndPermitsGBView]

        val result = route(application, request).value
        val checkboxes = otherLPCheckboxItems(boundForm)(messages(application))

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, checkboxes)(request, messages(application)).toString
      }
    }

    "must return Bad Request if no option selected" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, otherLicencesAndPermitsGBRoute)
            .withFormUrlEncodedBody("" -> "")

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
      }
    }
  }
}
