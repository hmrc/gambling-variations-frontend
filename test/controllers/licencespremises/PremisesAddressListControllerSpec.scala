package controllers.licencespremises

import base.SpecBase
import controllers.routes
import forms.licencespremises.PremisesAddressListFormProvider
import models.{NormalMode, UserAnswers}
import pages.licencespremises.{AddPremisesAddressPage, PremisesDetailsPage}
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.licencespremises.PremisesAddressListView

import java.time.LocalDate

class PremisesAddressListControllerSpec extends SpecBase {

  private lazy val premisesAddressListRoute =
    controllers.licencespremises.routes.PremisesAddressListController.onPageLoad().url
  private val formProvider = new PremisesAddressListFormProvider
  private val form = formProvider()
  private val userAnswers = UserAnswers(
    id = userAnswersId,
    data = Json.obj(
      "licencesPremisesSection" -> Json.obj(
        "mgdRegNum" -> "XGM000001761",
        "premisesDetails" -> Json.obj(
          "totalRows" -> 1000,
          "premises" -> Json.arr(
            Json.obj(
              "mgdRegNumber" -> "XGM000001761",
              "address1"     -> "123 Road",
              "address2"     -> "Avenue",
              "address3"     -> "London",
              "postcode"     -> "E8 1EA",
              "systemDate"   -> LocalDate.now()
            ),
            Json.obj(
              "mgdRegNumber" -> "XGM000001761",
              "address1"     -> "456 Road",
              "address2"     -> "Avenue",
              "address3"     -> "London",
              "postcode"     -> "E8 2EA",
              "systemDate"   -> LocalDate.now()
            ),
            Json.obj(
              "mgdRegNumber" -> "XGM000001761",
              "address1"     -> "789 Road",
              "address2"     -> "Avenue",
              "address3"     -> "London",
              "postcode"     -> "E8 3EA",
              "systemDate"   -> LocalDate.now()
            )
          )
        )
      )
    )
  )

  private val preparedFormWithAnswers =
    userAnswers
      .get(AddPremisesAddressPage)
      .fold(form)(form.fill)
  private val addressList = userAnswers.get(PremisesDetailsPage).fold(Seq.empty)(list => list.premises)
  private val maxPremises = 100

  "PremisesAddressList Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, premisesAddressListRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PremisesAddressListView]

        status(result) mustEqual OK
        contentAsString(result) mustBe view(preparedFormWithAnswers, NormalMode, addressList, maxPremises)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, premisesAddressListRoute)

        val view = application.injector.instanceOf[PremisesAddressListView]

        val result = route(application, request).value
        println(premisesAddressListRoute)
        status(result) mustEqual OK
        contentAsString(result) mustEqual view(preparedFormWithAnswers, NormalMode, addressList, maxPremises)(request, messages(application)).toString
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
