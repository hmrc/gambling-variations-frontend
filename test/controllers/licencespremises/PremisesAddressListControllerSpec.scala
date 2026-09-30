/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers.licencespremises

import base.SpecBase
import controllers.routes
import forms.licencespremises.PremisesAddressListFormProvider
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.licencespremises.{AddPremisesAddressPage, PremisesDetailsPage}
import play.api.inject.bind
import play.api.libs.json.Json
import repositories.SessionRepository
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import views.html.licencespremises.PremisesAddressListView

import java.time.LocalDate
import scala.concurrent.Future

class PremisesAddressListControllerSpec extends SpecBase with MockitoSugar {

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

  private val uaNoPremises = UserAnswers(
    id = userAnswersId,
    data = Json.obj(
      "licencesPremisesSection" -> Json.obj(
        "mgdRegNum"       -> "XGM000001761",
        "premisesDetails" -> Json.obj("totalRows" -> 1000, "premises" -> None)
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

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any[UserAnswers]))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(
            POST,
            controllers.licencespremises.routes.PremisesAddressListController.onSubmit().url
          )
            .withFormUrlEncodedBody("addPremisesAddress" -> "true")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
      }
    }
    "must redirect to AccessDenied when premises empty" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any[UserAnswers]))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(uaNoPremises))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, premisesAddressListRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.AccessDeniedController.onPageLoad().url

      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .build()

      running(application) {
        val request =
          FakeRequest(
            POST,
            controllers.licencespremises.routes.PremisesAddressListController.onSubmit().url
          ).withFormUrlEncodedBody("value" -> "")

        val boundForm = form.bind(Map("value" -> ""))

        val result = route(application, request).value

        val view =
          application.injector.instanceOf[PremisesAddressListView]

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual
          view(boundForm, NormalMode, addressList, maxPremises)(
            request,
            messages(application)
          ).toString
      }
    }

    "must redirect to System Error for a GET if no data" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, premisesAddressListRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PremisesAddressListView]

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.SystemErrorController.onPageLoad().url
      }
    }

    "must redirect to System Error for a POST if no data is found" in {

      val application =
        applicationBuilder(userAnswers = None)
          .build()

      running(application) {
        val request =
          FakeRequest(
            POST,
            controllers.licencespremises.routes.PremisesAddressListController.onSubmit().url
          )
            .withFormUrlEncodedBody("addPremisesAddress" -> "true")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.routes.SystemErrorController.onPageLoad().url
      }
    }

  }
}
