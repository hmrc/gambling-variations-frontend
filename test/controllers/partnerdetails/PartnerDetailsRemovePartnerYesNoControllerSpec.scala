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

package controllers.partnerdetails

import base.SpecBase
import controllers.routes
import forms.partnerdetails.PartnerDetailsRemovePartnerYesNoFormProvider
import models.{CheckMode, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.partnerdetails.{PartnerDetailsBusinessNamePage, PartnerDetailsChosenPartnerToRemovePage, PartnerDetailsRemovePartnerYesNoPage, PartnerDetailsSoleProprietorPage}
import play.api.data.Form
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.partnerdetails.PartnerDetailsRemovePartnerYesNoView

import scala.concurrent.Future

class PartnerDetailsRemovePartnerYesNoControllerSpec extends SpecBase with MockitoSugar with PartnerDetailsHelper {

  val formProvider = new PartnerDetailsRemovePartnerYesNoFormProvider()
  val form: Form[Boolean] = formProvider()

  val testBusinessName = "Partner Company Ltd"

  lazy val removePartnerYesNoRouteExistingPartners: String =
    controllers.partnerdetails.routes.PartnerDetailsRemovePartnerYesNoController
      .onPageLoad(businessNumber1, CheckMode)
      .url

  lazy val removePartnerYesNoRouteNewPartners: String =
    controllers.partnerdetails.routes.PartnerDetailsRemovePartnerYesNoController
      .onPageLoad(newPartnersIndex1.toString, NormalMode)
      .url

  lazy val deleteDateRoute: String =
    controllers.partnerdetails.routes.PartnerDetailsDeleteDateController.onPageLoad().url

  val userAnswersExistingPartners: UserAnswers =
    UserAnswers(mgdRegNumber, cleanedDataExistingPartners())
      .set(PartnerDetailsBusinessNamePage(businessNumber1), testBusinessName)
      .success
      .value

  val userAnswersNewPartners: UserAnswers =
    UserAnswers(mgdRegNumber, cleanedDataNewPartners())
      .set(PartnerDetailsBusinessNamePage(newPartnersIndex1), testBusinessName)
      .success
      .value

  "partners" - {
    "PartnerDetailsRemovePartnerYesNo Controller" - {

      "must return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(userAnswersExistingPartners)).build()

        running(application) {
          val request = FakeRequest(GET, removePartnerYesNoRouteExistingPartners)

          val result = route(application, request).value

          val view = application.injector.instanceOf[PartnerDetailsRemovePartnerYesNoView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, businessNumber1, NormalMode, testBusinessName)(request, messages(application)).toString
        }
      }

      "must populate the view correctly when the question has previously been answered" in {

        val userAnswersAnswered: UserAnswers =
          userAnswersExistingPartners
            .set(PartnerDetailsRemovePartnerYesNoPage(businessNumber1), true)
            .success
            .value

        val application = applicationBuilder(userAnswers = Some(userAnswersAnswered)).build()

        running(application) {
          val request = FakeRequest(GET, removePartnerYesNoRouteExistingPartners)

          val view = application.injector.instanceOf[PartnerDetailsRemovePartnerYesNoView]

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form.fill(true), businessNumber1, NormalMode, testBusinessName)(request,
                                                                                                                 messages(application)
                                                                                                                ).toString
        }
      }

      "must use the sole proprietor's name when there is no business name" in {

        val userAnswersSoleProprietor: UserAnswers = {
          val withoutBusinessName =
            userAnswersExistingPartners.remove(PartnerDetailsBusinessNamePage(businessNumber1)).success.value

          withoutBusinessName.copy(
            data = withoutBusinessName.data.deepMerge(
              Json.obj(
                "partners" -> Json.obj(
                  businessNumber1 -> Json.obj(
                    "partnerDetailsSoleProprietor" -> Json.obj(
                      "title"     -> "Ms",
                      "firstName" -> "PartnerFirst1",
                      "lastName"  -> "PartnerLast1"
                    )
                  )
                )
              )
            )
          )
        }

        val expectedName =
          userAnswersSoleProprietor.get(PartnerDetailsSoleProprietorPage(businessNumber1)).value.fullName

        val application = applicationBuilder(userAnswers = Some(userAnswersSoleProprietor)).build()

        running(application) {
          val request = FakeRequest(GET, removePartnerYesNoRouteExistingPartners)

          val result = route(application, request).value

          val view = application.injector.instanceOf[PartnerDetailsRemovePartnerYesNoView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, businessNumber1, NormalMode, expectedName)(request, messages(application)).toString
        }
      }

      "must redirect to SystemError for a GET when the partner has no name" in {

        val userAnswersNoName: UserAnswers =
          userAnswersExistingPartners.remove(PartnerDetailsBusinessNamePage(businessNumber1)).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswersNoName)).build()

        running(application) {
          val request = FakeRequest(GET, removePartnerYesNoRouteExistingPartners)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.SystemErrorController.onPageLoad().url
        }
      }

      "must save the answer and chosen partner, and redirect to the delete date page when yes is submitted" in {

        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(userAnswersExistingPartners))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {

          val expectedAnswers =
            userAnswersExistingPartners
              .set(PartnerDetailsRemovePartnerYesNoPage(businessNumber1), true)
              .success
              .value
              .set(PartnerDetailsChosenPartnerToRemovePage, businessNumber1)
              .success
              .value

          val request =
            FakeRequest(POST, removePartnerYesNoRouteExistingPartners)
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual deleteDateRoute
          verify(mockSessionRepository).set(expectedAnswers)
        }
      }

      "must redirect to the next page when no is submitted" in {

        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(userAnswersExistingPartners))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, removePartnerYesNoRouteExistingPartners)
              .withFormUrlEncodedBody(("value", "false"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {

        val application = applicationBuilder(userAnswers = Some(userAnswersExistingPartners)).build()

        running(application) {
          val request =
            FakeRequest(POST, removePartnerYesNoRouteExistingPartners)
              .withFormUrlEncodedBody(("value", ""))

          val boundForm = form.bind(Map("value" -> ""))

          val view = application.injector.instanceOf[PartnerDetailsRemovePartnerYesNoView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, businessNumber1, NormalMode, testBusinessName)(request, messages(application)).toString
        }
      }

      "must redirect to SystemError for a POST when the partner has no name" in {

        val userAnswersNoName: UserAnswers =
          userAnswersExistingPartners.remove(PartnerDetailsBusinessNamePage(businessNumber1)).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswersNoName)).build()

        running(application) {
          val request =
            FakeRequest(POST, removePartnerYesNoRouteExistingPartners)
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.SystemErrorController.onPageLoad().url
        }
      }

      "must redirect to SystemError for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, removePartnerYesNoRouteExistingPartners)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.SystemErrorController.onPageLoad().url
        }
      }

      "must redirect to SystemError for a POST if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request =
            FakeRequest(POST, removePartnerYesNoRouteExistingPartners)
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.SystemErrorController.onPageLoad().url
        }
      }
    }
  }

  // might not need this
  "newPartners" - {
    "PartnerDetailsRemovePartnerYesNo Controller" - {

      "must return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(userAnswersNewPartners)).build()

        running(application) {
          val request = FakeRequest(GET, removePartnerYesNoRouteNewPartners)

          val result = route(application, request).value

          val view = application.injector.instanceOf[PartnerDetailsRemovePartnerYesNoView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, newPartnersIndex1.toString, NormalMode, testBusinessName)(request,
                                                                                                                 messages(application)
                                                                                                                ).toString
        }
      }

      "must save the answer and chosen partner, and redirect to the delete date page when yes is submitted" in {

        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(userAnswersNewPartners))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {

          val expectedAnswers =
            userAnswersNewPartners
              .set(PartnerDetailsRemovePartnerYesNoPage(newPartnersIndex1), true)
              .success
              .value
              .set(PartnerDetailsChosenPartnerToRemovePage, newPartnersIndex1.toString)
              .success
              .value

          val request =
            FakeRequest(POST, removePartnerYesNoRouteNewPartners)
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual deleteDateRoute
          verify(mockSessionRepository).set(expectedAnswers)
        }
      }

      "must redirect to SystemError for a GET when the partner has no name" in {

        val userAnswersNoName: UserAnswers =
          userAnswersNewPartners.remove(PartnerDetailsBusinessNamePage(newPartnersIndex1)).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswersNoName)).build()

        running(application) {
          val request = FakeRequest(GET, removePartnerYesNoRouteNewPartners)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.SystemErrorController.onPageLoad().url
        }
      }
    }
  }
}
