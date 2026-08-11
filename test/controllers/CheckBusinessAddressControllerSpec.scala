package controllers

import base.SpecBase
import models.{Address, NormalMode, UserAnswers}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import play.api.libs.json.Json
import views.html.BusinessAddressView

class CheckBusinessAddressControllerSpec extends SpecBase {

  private val regOnly = Json.obj(
    "businessAddressSection" -> Json.obj(
      "mgdRegNum" -> "XMY1000001",
    )
  )

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

  private val basicAnswers = UserAnswers("id", regOnly)

  "CheckBusinessAddress Controller" - {

    "must return OK and the correct view for a GET when address is present" in {

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

      val application = applicationBuilder(userAnswers = Some(basicAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.CheckBusinessAddressController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[BusinessAddressView]

        status(result) mustEqual SEE_OTHER
      }
    }
  }
}
