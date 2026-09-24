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
import forms.partnerdetails.PartnerDetailsRemoveNationalInsuranceNumberYesNoFormProvider
import models.{CheckMode, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.partnerdetails.{PartnerDetailsAddPartnerCompletedPage, PartnerDetailsNinoPage, PartnerDetailsRemoveNationalInsuranceNumberYesNoPage}
import play.api.data.Form
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.partnerdetails.PartnerDetailsRemoveNationalInsuranceNumberYesNoView

import scala.concurrent.Future

//TODO done I think
class PartnerDetailsRemoveNationalInsuranceNumberYesNoControllerSpec extends SpecBase with MockitoSugar with PartnerDetailsHelper {

  val formProvider = new PartnerDetailsRemoveNationalInsuranceNumberYesNoFormProvider()
  val form: Form[Boolean] = formProvider()

  lazy val partnerDetailsRemoveNinoRouteNewPartners: String =
    controllers.partnerdetails.routes.PartnerDetailsRemoveNationalInsuranceNumberYesNoController
      .onPageLoad(newPartnersIndex1.toString, NormalMode)
      .url

  lazy val partnerDetailsRemoveNinoRouteExistingUsers: String =
    controllers.partnerdetails.routes.PartnerDetailsRemoveNationalInsuranceNumberYesNoController.onPageLoad(businessNumber1, CheckMode).url

  val validUserAnswersNewPartners: UserAnswers =
    UserAnswers(mgdRegNumber, cleanedDataNewPartners(nino = Some(testNino)))
      .set(PartnerDetailsAddPartnerCompletedPage(newPartnersIndex1), false)
      .success
      .value
      .set(PartnerDetailsNinoPage(newPartnersIndex1), testNino)
      .success
      .value

  val validUserAnswersExistingUsers: UserAnswers =
    UserAnswers(mgdRegNumber, cleanedDataExistingPartners(nino = Some(testNino)))
      .set(PartnerDetailsAddPartnerCompletedPage(newPartnersIndex1), false)
      .success
      .value
      .set(PartnerDetailsNinoPage(businessNumber1), testNino)
      .success
      .value

  "partners" - {


    "PartnerDetailsRemoveNationalInsuranceNumberYesNo Controller" - {

      "onPageLoad" - {

        "must return OK and the correct view for a GET when no previous answer exists" in {

          val application = applicationBuilder(userAnswers = Some(validUserAnswersExistingUsers)).build()

          running(application) {
            val request = FakeRequest(GET, partnerDetailsRemoveNinoRouteExistingUsers)

            val result = route(application, request).value

            val view = application.injector.instanceOf[PartnerDetailsRemoveNationalInsuranceNumberYesNoView]

            status(result) mustBe OK
            contentAsString(result) mustBe view(form, businessNumber1, CheckMode, testNino)(request, messages(application)).toString
          }
        }

        "must populate the view correctly on a GET when the question has previously been answered" in {

          val userAnswers = validUserAnswersExistingUsers
            .set(PartnerDetailsRemoveNationalInsuranceNumberYesNoPage(businessNumber1), true)
            .success
            .value

          val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

          running(application) {
            val request = FakeRequest(GET, partnerDetailsRemoveNinoRouteExistingUsers)

            val view = application.injector.instanceOf[PartnerDetailsRemoveNationalInsuranceNumberYesNoView]

            val result = route(application, request).value

            status(result) mustBe OK
            contentAsString(result) mustBe view(form.fill(true), businessNumber1, CheckMode, testNino)(request,
              messages(application)
            ).toString
          }
        }

        "must redirect to SystemErrorController for a GET if PartnerDetailsNinoPage is missing" in {

          val userAnswersNoNino = emptyUserAnswers

          val application = applicationBuilder(userAnswers = Some(userAnswersNoNino)).build()

          running(application) {
            val request = FakeRequest(GET, partnerDetailsRemoveNinoRouteExistingUsers)

            val result = route(application, request).value

            status(result) mustBe SEE_OTHER
            redirectLocation(result).value mustBe controllers.routes.SystemErrorController.onPageLoad().url
          }
        }

        "must redirect to SystemErrorController for a GET if no existing data is found" in {

          val application = applicationBuilder(userAnswers = None).build()

          running(application) {
            val request = FakeRequest(GET, partnerDetailsRemoveNinoRouteExistingUsers)

            val result = route(application, request).value

            status(result) mustBe SEE_OTHER
            redirectLocation(result).value mustBe controllers.routes.SystemErrorController.onPageLoad().url
          }
        }
      }

      "onSubmit" - {

        "must remove NINO, update UserAnswers and redirect when 'Yes' (true) is submitted" in {

          val mockSessionRepository = mock[SessionRepository]

          when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

          val application =
            applicationBuilder(userAnswers = Some(validUserAnswersExistingUsers))
              .overrides(
                bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()

          running(application) {
            val request =
              FakeRequest(POST, partnerDetailsRemoveNinoRouteExistingUsers)
                .withFormUrlEncodedBody(("value", "true"))

            val result = route(application, request).value

            val expectedAnswers = validUserAnswersExistingUsers
              .remove(PartnerDetailsNinoPage(businessNumber1))
              .success
              .value
              .set(PartnerDetailsRemoveNationalInsuranceNumberYesNoPage(businessNumber1), true)
              .success
              .value

            status(result) mustBe SEE_OTHER
            redirectLocation(result).value mustBe onwardRoute.url
            verify(mockSessionRepository).set(expectedAnswers)
          }
        }

        "must NOT remove NINO, update UserAnswers and redirect when 'No' (false) is submitted" in {

          val mockSessionRepository = mock[SessionRepository]

          when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

          val application =
            applicationBuilder(userAnswers = Some(validUserAnswersExistingUsers))
              .overrides(
                bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()

          running(application) {
            val request =
              FakeRequest(POST, partnerDetailsRemoveNinoRouteExistingUsers)
                .withFormUrlEncodedBody(("value", "false"))

            val result = route(application, request).value

            val expectedAnswers = validUserAnswersExistingUsers
              .set(PartnerDetailsRemoveNationalInsuranceNumberYesNoPage(businessNumber1), false)
              .success
              .value

            status(result) mustBe SEE_OTHER
            redirectLocation(result).value mustBe onwardRoute.url
            verify(mockSessionRepository).set(expectedAnswers)
          }
        }

        "must return a Bad Request and errors when invalid data is submitted" in {

          val mockSessionRepository = mock[SessionRepository]

          val application = applicationBuilder(userAnswers = Some(validUserAnswersExistingUsers))
            .overrides(
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

          running(application) {
            val request =
              FakeRequest(POST, partnerDetailsRemoveNinoRouteExistingUsers)
                .withFormUrlEncodedBody(("value", ""))

            val boundForm = form.bind(Map("value" -> ""))

            val view = application.injector.instanceOf[PartnerDetailsRemoveNationalInsuranceNumberYesNoView]

            val result = route(application, request).value

            status(result) mustBe BAD_REQUEST
            contentAsString(result) mustBe view(boundForm, businessNumber1, CheckMode, testNino)(request, messages(application)).toString
            verify(mockSessionRepository, never()).set(any())
          }
        }

        "must return Bad Request and stay on the same page with form errors when submitted with no selection" in {

          val mockSessionRepository = mock[SessionRepository]

          val application = applicationBuilder(userAnswers = Some(validUserAnswersExistingUsers))
            .overrides(
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

          running(application) {
            val request =
              FakeRequest(POST, partnerDetailsRemoveNinoRouteExistingUsers)
                .withFormUrlEncodedBody(("value", ""))

            val boundForm = form.bind(Map("value" -> ""))

            val view = application.injector.instanceOf[PartnerDetailsRemoveNationalInsuranceNumberYesNoView]

            val result = route(application, request).value

            status(result) mustBe BAD_REQUEST
            contentAsString(result) mustBe view(boundForm, businessNumber1, CheckMode, testNino)(request, messages(application)).toString
            verify(mockSessionRepository, never()).set(any())
          }
        }
      }
    }
  }

  "newPartners" - {

    "PartnerDetailsRemoveNationalInsuranceNumberYesNo Controller" - {

      "onPageLoad" - {

        "must return OK and the correct view for a GET when no previous answer exists" in {

          val application = applicationBuilder(userAnswers = Some(validUserAnswersNewPartners)).build()

          running(application) {
            val request = FakeRequest(GET, partnerDetailsRemoveNinoRouteNewPartners)

            val result = route(application, request).value

            val view = application.injector.instanceOf[PartnerDetailsRemoveNationalInsuranceNumberYesNoView]

            status(result) mustBe OK
            contentAsString(result) mustBe view(form, newPartnersIndex1.toString, NormalMode, testNino)(request, messages(application)).toString
          }
        }

        "must populate the view correctly on a GET when the question has previously been answered" in {

          val userAnswers = validUserAnswersNewPartners
            .set(PartnerDetailsRemoveNationalInsuranceNumberYesNoPage(newPartnersIndex1), true)
            .success
            .value

          val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

          running(application) {
            val request = FakeRequest(GET, partnerDetailsRemoveNinoRouteNewPartners)

            val view = application.injector.instanceOf[PartnerDetailsRemoveNationalInsuranceNumberYesNoView]

            val result = route(application, request).value

            status(result) mustBe OK
            contentAsString(result) mustBe view(form.fill(true), newPartnersIndex1.toString, NormalMode, testNino)(request,
                                                                                                                   messages(application)
                                                                                                                  ).toString
          }
        }

        "must redirect to SystemErrorController for a GET if PartnerDetailsNinoPage is missing" in {

          val userAnswersNoNino = emptyUserAnswers

          val application = applicationBuilder(userAnswers = Some(userAnswersNoNino)).build()

          running(application) {
            val request = FakeRequest(GET, partnerDetailsRemoveNinoRouteNewPartners)

            val result = route(application, request).value

            status(result) mustBe SEE_OTHER
            redirectLocation(result).value mustBe controllers.routes.SystemErrorController.onPageLoad().url
          }
        }

        "must redirect to SystemErrorController for a GET if no existing data is found" in {

          val application = applicationBuilder(userAnswers = None).build()

          running(application) {
            val request = FakeRequest(GET, partnerDetailsRemoveNinoRouteNewPartners)

            val result = route(application, request).value

            status(result) mustBe SEE_OTHER
            redirectLocation(result).value mustBe controllers.routes.SystemErrorController.onPageLoad().url
          }
        }
      }

      "onSubmit" - {

        "must remove NINO, update UserAnswers and redirect when 'Yes' (true) is submitted" in {

          val mockSessionRepository = mock[SessionRepository]

          when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

          val application =
            applicationBuilder(userAnswers = Some(validUserAnswersNewPartners))
              .overrides(
                bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()

          running(application) {
            val request =
              FakeRequest(POST, partnerDetailsRemoveNinoRouteNewPartners)
                .withFormUrlEncodedBody(("value", "true"))

            val result = route(application, request).value

            val expectedAnswers = validUserAnswersNewPartners
              .remove(PartnerDetailsNinoPage(newPartnersIndex1))
              .success
              .value
              .set(PartnerDetailsRemoveNationalInsuranceNumberYesNoPage(newPartnersIndex1), true)
              .success
              .value

            status(result) mustBe SEE_OTHER
            redirectLocation(result).value mustBe onwardRoute.url
            verify(mockSessionRepository).set(expectedAnswers)
          }
        }

        "must NOT remove NINO, update UserAnswers and redirect when 'No' (false) is submitted" in {

          val mockSessionRepository = mock[SessionRepository]

          when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

          val application =
            applicationBuilder(userAnswers = Some(validUserAnswersNewPartners))
              .overrides(
                bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()

          running(application) {
            val request =
              FakeRequest(POST, partnerDetailsRemoveNinoRouteNewPartners)
                .withFormUrlEncodedBody(("value", "false"))

            val result = route(application, request).value

            val expectedAnswers = validUserAnswersNewPartners
              .set(PartnerDetailsRemoveNationalInsuranceNumberYesNoPage(newPartnersIndex1), false)
              .success
              .value

            status(result) mustBe SEE_OTHER
            redirectLocation(result).value mustBe onwardRoute.url
            verify(mockSessionRepository).set(expectedAnswers)
          }
        }

        "must return a Bad Request and errors when invalid data is submitted" in {

          val mockSessionRepository = mock[SessionRepository]

          val application = applicationBuilder(userAnswers = Some(validUserAnswersNewPartners))
            .overrides(
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

          running(application) {
            val request =
              FakeRequest(POST, partnerDetailsRemoveNinoRouteNewPartners)
                .withFormUrlEncodedBody(("value", ""))

            val boundForm = form.bind(Map("value" -> ""))

            val view = application.injector.instanceOf[PartnerDetailsRemoveNationalInsuranceNumberYesNoView]

            val result = route(application, request).value

            status(result) mustBe BAD_REQUEST
            contentAsString(result) mustBe view(boundForm, newPartnersIndex1.toString, NormalMode, testNino)(request, messages(application)).toString
            verify(mockSessionRepository, never()).set(any())
          }
        }

        "must return Bad Request and stay on the same page with form errors when submitted with no selection" in {

          val mockSessionRepository = mock[SessionRepository]

          val application = applicationBuilder(userAnswers = Some(validUserAnswersNewPartners))
            .overrides(
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

          running(application) {
            val request =
              FakeRequest(POST, partnerDetailsRemoveNinoRouteNewPartners)
                .withFormUrlEncodedBody(("value", ""))

            val boundForm = form.bind(Map("value" -> ""))

            val view = application.injector.instanceOf[PartnerDetailsRemoveNationalInsuranceNumberYesNoView]

            val result = route(application, request).value

            status(result) mustBe BAD_REQUEST
            contentAsString(result) mustBe view(boundForm, newPartnersIndex1.toString, NormalMode, testNino)(request, messages(application)).toString
            verify(mockSessionRepository, never()).set(any())
          }
        }
      }
    }

  }
}
