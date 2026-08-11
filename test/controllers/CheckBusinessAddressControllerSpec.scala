package controllers

import base.SpecBase
import models.{Address, NormalMode, UserAnswers}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import play.api.libs.json.Json
import views.html.BusinessAddressView

class CheckBusinessAddressControllerSpec extends SpecBase {





  "CheckBusinessAddress Controller" - {

    "must return OK and the correct view for a GET when address is present" in {

      val addressOnlyUa: UserAnswers = UserAnswers("id", Json.obj(
        "businessAddressSection" -> Json.obj("mgdRegNum" -> "XMY1000002",
          "businessAddressUk" -> Address(
            "abc",
            Some("abc"),
            Some("abc"),
            Some("abc"),
            Some("abc"),
            Some("abc")
            )
          )
        )
      )

      val application = applicationBuilder(userAnswers = Some(addressOnlyUa)).build()

      running(application) {
        val request = FakeRequest(GET, routes.CheckBusinessAddressController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[BusinessAddressView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(ua = addressOnlyUa, mode = NormalMode, showChangeMessage = false)(request, messages(application)).toString
      }
    }

    "must redirect when address is not present" in {

      val regOnly = Json.obj(
        "businessAddressSection" -> Json.obj(
          "mgdRegNum" -> "XMY1000001",
        )
      )

      val basicAnswers = UserAnswers("id", regOnly)


      val application = applicationBuilder(userAnswers = Some(basicAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.CheckBusinessAddressController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[BusinessAddressView]

        status(result) mustEqual SEE_OTHER
      }
    }
    "must return SystemErrorController when Business Address section is empty" in {

      val data = Json.obj(
        "businessContactDetailsSection" -> Json.obj())

      val userAnswers = UserAnswers("id-number", data)

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(GET, routes.CheckBusinessAddressController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          routes.SystemErrorController.onPageLoad().url
      }
    }
  }
}
