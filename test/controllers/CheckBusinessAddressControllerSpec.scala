package controllers

import base.SpecBase
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.CheckBusinessAddressView

class CheckBusinessAddressControllerSpec extends SpecBase {

  "CheckBusinessAddress Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.CheckBusinessAddressController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckBusinessAddressView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(request, messages(application)).toString
      }
    }
  }
}
