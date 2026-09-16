package controllers

import base.SpecBase
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.PremisesAddressListView

class PremisesAddressListControllerSpec extends SpecBase {

  "PremisesAddressList Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.PremisesAddressListController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PremisesAddressListView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, messages(application)).toString
      }
    }
  }
}
