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
import forms.licencespremises.RemovePremisesAddressFormProvider
import models.licencespremises.{PremisesDetails, PremisesDetailsResponse}
import models.{Address, NormalMode, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.licencespremises.*
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.licencespremises.RemovePremisesAddressView

import java.time.LocalDate
import scala.concurrent.Future

class RemovePremisesAddressControllerSpec extends SpecBase with MockitoSugar {

  private val formProvider = new RemovePremisesAddressFormProvider()
  private val form = formProvider()

  private val chosenAddress: Address = Address(
    address1 = "Flat 1",
    address2 = Some("10 Market Calle"),
    address3 = Some("Madrid"),
    address4 = None,
    postcode = Some("28085"),
    country  = None
  )

  val premisesDetailsResponse = PremisesDetailsResponse(
    totalRows = Some(2),
    premises = Seq(
      PremisesDetails(
        mgdRegNumber = "MGD123456",
        address1     = Some("Flat 1"),
        address2     = Some("10 Market Calle"),
        address3     = Some("Madrid"),
        address4     = None,
        postcode     = Some("28085"),
        systemDate   = Some(LocalDate.of(2026, 9, 21))
      ),
      PremisesDetails(
        mgdRegNumber = "MGD789012",
        address1     = Some("Flat 2"),
        address2     = Some("20 Market Calle"),
        address3     = Some("Madrid"),
        address4     = None,
        postcode     = Some("28086"),
        systemDate   = Some(LocalDate.of(2026, 9, 21))
      )
    )
  )

  private lazy val removePremisesAddressRoute =
    routes.RemovePremisesAddressController.onPageLoad().url

  private val userAnswersWithChosenAddress: UserAnswers =
    emptyUserAnswers
      .set(LicencesPremisesSectionPage, userAnswersId)
      .success
      .value
      .set(ChosenPremisesAddressPage, chosenAddress)
      .success
      .value
      .set(PremisesDetailsPage, premisesDetailsResponse)
      .success
      .value

  private val userAnswersWithoutChosenAddress: UserAnswers =
    emptyUserAnswers
      .set(LicencesPremisesSectionPage, userAnswersId)
      .success
      .value
      .set(PremisesDetailsPage, premisesDetailsResponse)
      .success
      .value

  "RemovePremisesAddressController Controller" - {

    "must return OK and the correct view for a GET" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithChosenAddress))
          .build()

      running(application) {
        val request = FakeRequest(GET, removePremisesAddressRoute)

        val result = route(application, request).value

        val view =
          application.injector.instanceOf[RemovePremisesAddressView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(form, NormalMode, chosenAddress)(
            request,
            messages(application)
          ).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any[UserAnswers]))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithChosenAddress))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(
            POST,
            routes.RemovePremisesAddressController.onSubmit().url
          )
            .withFormUrlEncodedBody("value" -> "true")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithChosenAddress))
          .build()

      running(application) {
        val request =
          FakeRequest(
            POST,
            routes.RemovePremisesAddressController.onSubmit().url
          )
            .withFormUrlEncodedBody("value" -> "")

        val boundForm = form.bind(Map("value" -> ""))

        val result = route(application, request).value

        val view =
          application.injector.instanceOf[RemovePremisesAddressView]

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual
          view(boundForm, NormalMode, chosenAddress)(
            request,
            messages(application)
          ).toString
      }
    }

    "must redirect to System Error page for a POST when ChosenAddress is missing" in {

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithoutChosenAddress))
          .build()

      running(application) {
        val request =
          FakeRequest(
            POST,
            routes.RemovePremisesAddressController.onSubmit().url
          )
            .withFormUrlEncodedBody("value" -> "true")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.routes.SystemErrorController.onPageLoad().url
      }
    }

    "must redirect to System Error for a GET if no existing data is found" in {

      val application =
        applicationBuilder(userAnswers = None)
          .build()

      running(application) {
        val request = FakeRequest(GET, removePremisesAddressRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.routes.SystemErrorController.onPageLoad().url
      }
    }

    "must redirect to System Error for a POST if no existing data is found" in {

      val application =
        applicationBuilder(userAnswers = None)
          .build()

      running(application) {
        val request =
          FakeRequest(
            POST,
            routes.RemovePremisesAddressController.onSubmit().url
          )
            .withFormUrlEncodedBody("value" -> "true")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.routes.SystemErrorController.onPageLoad().url
      }
    }

    "must update UserAnswers correctly when true is submitted" in {

      val mockSessionRepository = mock[SessionRepository]
      val savedAnswersCaptor =
        ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockSessionRepository.set(any[UserAnswers]))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithChosenAddress))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(
            POST,
            routes.RemovePremisesAddressController.onSubmit().url
          )
            .withFormUrlEncodedBody("value" -> "true")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        verify(mockSessionRepository).set(savedAnswersCaptor.capture())

        val savedAnswers = savedAnswersCaptor.getValue

        savedAnswers.get(RemovePremisesAddressPage).value mustEqual true
        savedAnswers.get(LicencesPremisesDetailsSubmittedPage).value mustEqual true
        savedAnswers.get(LicencesPremisesDetailsChangesPage).value mustEqual true
        savedAnswers.get(ChosenPremisesAddressPage) mustEqual None
      }
    }

    "must update UserAnswers correctly when false is submitted" in {

      val mockSessionRepository = mock[SessionRepository]
      val savedAnswersCaptor =
        ArgumentCaptor.forClass(classOf[UserAnswers])

      when(mockSessionRepository.set(any[UserAnswers]))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(userAnswersWithChosenAddress))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(
            POST,
            routes.RemovePremisesAddressController.onSubmit().url
          )
            .withFormUrlEncodedBody("value" -> "false")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        verify(mockSessionRepository).set(savedAnswersCaptor.capture())

        val savedAnswers = savedAnswersCaptor.getValue

        savedAnswers.get(RemovePremisesAddressPage).value mustEqual false
        savedAnswers.get(ChosenPremisesAddressPage) mustEqual None
      }
    }
  }
}
