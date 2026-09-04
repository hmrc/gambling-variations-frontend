package controllers.licencespremises

import base.SpecBase
import forms.licencespremises.OtherLicencesAndPermitsGBFormProvider
import models.licencespremises.OtherLicencesAndPermitsGB
import models.licencespremises.OtherLicencesAndPermitsGB.*
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
import viewmodels.OtherLicencesAndPermitsViewModel
import views.html.licencespremises.OtherLicencesAndPermitsGBView

import scala.concurrent.Future

class OtherLicencesAndPermitsGBControllerSpec extends SpecBase with MockitoSugar {

  def onwardRoute = Call("GET", "/foo")

  lazy val otherLicencesAndPermitsGBRoute: String =
    controllers.licencespremises.routes.OtherLicencesAndPermitsGBController.onPageLoad().url

  val formProvider = new OtherLicencesAndPermitsGBFormProvider()
  val form = formProvider()
  val blankAnswers = UserAnswers(userAnswersId, Json.obj("licencesPremisesSection" -> Json.obj("mgdRegNum" -> "XGM000001761")))

  private val userAnswers = UserAnswers(
    userAnswersId,
    Json.obj(
      "licencesPremisesSection" -> Json.obj(
        "mgdRegNum"           -> "XGM000001761",
        "clubGaming"          -> "1",
        "clubMachine"         -> "0",
        "clubPremises"        -> "0",
        "familyEntertainment" -> "1",
        "localAuthority"      -> "0",
        "onPremises"          -> "1",
        "prizeGaming"         -> "0"
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
        val viewModel = OtherLicencesAndPermitsViewModel(form)(messages(application))
        status(result) mustEqual OK

        contentAsString(result) mustBe view(form, NormalMode, viewModel)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, otherLicencesAndPermitsGBRoute)
        val view = application.injector.instanceOf[OtherLicencesAndPermitsGBView]

        val result = route(application, request).value
        val preparedForm = form.fill(getSelectedLicencesAndPermits(userAnswers))
        val checkboxes = OtherLicencesAndPermitsViewModel(preparedForm)(messages(application))
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(preparedForm, NormalMode, checkboxes)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(blankAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()
      val set = getSelectedLicencesAndPermits(userAnswers)

      running(application) {
        val request =
          FakeRequest(POST, otherLicencesAndPermitsGBRoute)
            .withFormUrlEncodedBody(form.fill(set).data.toSeq*)

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
            .withFormUrlEncodedBody(("x", "invalid value"))

        val boundForm = form.bind(Map("x" -> "invalid value"))

        val view = application.injector.instanceOf[OtherLicencesAndPermitsGBView]

        val result = route(application, request).value
        val checkboxes = OtherLicencesAndPermitsViewModel(boundForm)(messages(application))

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, checkboxes)(request, messages(application)).toString
      }
    }
  }
}
