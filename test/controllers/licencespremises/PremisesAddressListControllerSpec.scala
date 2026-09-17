package controllers.licencespremises

import base.SpecBase
import controllers.routes
import forms.licencespremises.PremisesAddressListFormProvider
import models.{NormalMode, UserAnswers}
import play.api.Application
import play.api.data.Form
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.licencespremises.PremisesAddressListView

class PremisesAddressListControllerSpec extends SpecBase {
  private lazy val premisesAddressListRoute =
    routes.PremisesAddressListController.onPageLoad().url

  val formProvider = new PremisesAddressListFormProvider
  val form: Form[Boolean] = formProvider()

  "PremisesAddressList Controller" - {

    "must return OK and the correct view for a GET" in new Setup {
      val application: Application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, premisesAddressListRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PremisesAddressListView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, Seq.empty)(request, messages(application)).toString
      }
    }

    "must redirect to System Error if no data" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, premisesAddressListRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PremisesAddressListView]

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.SystemErrorController.onPageLoad().url
      }
    }
  }
}
