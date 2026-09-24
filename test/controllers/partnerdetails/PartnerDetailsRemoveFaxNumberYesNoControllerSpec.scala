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
import controllers.partnerdetails.routes.PartnerDetailsRemoveFaxNumberYesNoController
import controllers.routes
import forms.partnerdetails.PartnerDetailsRemoveFaxNumberYesNoFormProvider
import models.{CheckMode, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import pages.partnerdetails.{PartnerDetailsAddPartnerCompletedPage, PartnerDetailsCorrespondenceFaxNumberPage, PartnerDetailsRemoveFaxNumberYesNoPage}
import play.api.data.Form
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.partnerdetails.PartnerDetailsRemoveFaxNumberYesNoView

import scala.concurrent.Future

//TODO done
class PartnerDetailsRemoveFaxNumberYesNoControllerSpec extends SpecBase with MockitoSugar with PartnerDetailsHelper {

  private val formProvider = new PartnerDetailsRemoveFaxNumberYesNoFormProvider()
  val form: Form[Boolean] = formProvider()

  lazy val partnerDetailsRemoveFaxNumberYesNoRouteNewPartners: String =
    PartnerDetailsRemoveFaxNumberYesNoController.onPageLoad(newPartnersIndex1.toString, NormalMode).url

  lazy val partnerDetailsRemoveFaxNumberYesNoRouteExistingPartners: String =
    PartnerDetailsRemoveFaxNumberYesNoController.onPageLoad(businessNumber1, CheckMode).url

  private val validUserAnswersNewPartners: UserAnswers =
    UserAnswers(mgdRegNumber, cleanedDataExistingPartners(Some(testFaxNumber)))
      .set(PartnerDetailsAddPartnerCompletedPage(newPartnersIndex1), false)
      .success
      .value
      .set(PartnerDetailsCorrespondenceFaxNumberPage(newPartnersIndex1), testFaxNumber)
      .success
      .value

  private val validUserAnswersExistingPartners: UserAnswers =
    UserAnswers(mgdRegNumber, cleanedDataNewPartners(Some(testFaxNumber)))
      .set(PartnerDetailsCorrespondenceFaxNumberPage(businessNumber1), testFaxNumber)
      .success
      .value

  "partners" - {

    "PartnerDetailsRemoveFaxNumberYesNo Controller" - {

      "onPageLoad" - {

        "must return OK and the correct view for a GET when fax number exists in UserAnswers" in {
          val application = applicationBuilder(userAnswers = Some(validUserAnswersExistingPartners)).build()

          running(application) {
            val request = FakeRequest(GET, partnerDetailsRemoveFaxNumberYesNoRouteExistingPartners)
            val result = route(application, request).value
            val view = application.injector.instanceOf[PartnerDetailsRemoveFaxNumberYesNoView]

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(form, businessNumber1, CheckMode, testFaxNumber)(request, messages(application)).toString
          }
        }

        "must populate the view correctly on a GET when the question has previously been answered" in {
          val userAnswers =
            validUserAnswersExistingPartners
              .set(PartnerDetailsRemoveFaxNumberYesNoPage(businessNumber1), true)
              .success
              .value

          val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

          running(application) {
            val request = FakeRequest(GET, partnerDetailsRemoveFaxNumberYesNoRouteExistingPartners)
            val result = route(application, request).value
            val view = application.injector.instanceOf[PartnerDetailsRemoveFaxNumberYesNoView]

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(form.fill(true), businessNumber1, CheckMode, testFaxNumber)(request,
                                                                                                               messages(application)
                                                                                                              ).toString
          }
        }

        "must redirect to JourneyRecovery on a GET when correspondence details exist but faxNumber is None" in {
          val userAnswers = UserAnswers(mgdRegNumber, cleanedDataExistingPartners(None))

          val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

          running(application) {
            val request = FakeRequest(GET, partnerDetailsRemoveFaxNumberYesNoRouteExistingPartners)
            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
          }
        }

        "must redirect to SystemError when no user answers are found" in {
          val application = applicationBuilder(userAnswers = None).build()

          running(application) {
            val request = FakeRequest(GET, partnerDetailsRemoveFaxNumberYesNoRouteExistingPartners)
            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.routes.SystemErrorController.onPageLoad().url
          }
        }
      }

      "onSubmit" - {

        "must remove the fax number and redirect to next page when 'Yes' (true) is submitted" in {
          val mockSessionRepository = mock[SessionRepository]
          when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

          val userAnswers = validUserAnswersExistingPartners

          val application =
            applicationBuilder(userAnswers = Some(userAnswers))
              .overrides(
                bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()

          running(application) {
            val request = FakeRequest(POST, partnerDetailsRemoveFaxNumberYesNoRouteExistingPartners)
              .withFormUrlEncodedBody(("value", "true"))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual onwardRoute.url
          }
        }

        "must retain the fax number and redirect when 'No' (false) is submitted" in {
          val mockSessionRepository = mock[SessionRepository]
          when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

          val userAnswers = validUserAnswersExistingPartners

          val application =
            applicationBuilder(userAnswers = Some(userAnswers))
              .overrides(
                bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()

          running(application) {
            val request =
              FakeRequest(POST, partnerDetailsRemoveFaxNumberYesNoRouteExistingPartners)
                .withFormUrlEncodedBody(("value", "false"))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual onwardRoute.url
          }
        }

        "must return BAD_REQUEST and errors when invalid data is submitted" in {
          val userAnswers = validUserAnswersExistingPartners

          val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

          running(application) {
            val request =
              FakeRequest(POST, partnerDetailsRemoveFaxNumberYesNoRouteExistingPartners)
                .withFormUrlEncodedBody(("value", ""))

            val boundForm = form.bind(Map("value" -> ""))
            val view = application.injector.instanceOf[PartnerDetailsRemoveFaxNumberYesNoView]
            val result = route(application, request).value

            status(result) mustEqual BAD_REQUEST
            contentAsString(result) mustEqual view(boundForm, businessNumber1, CheckMode, testFaxNumber)(request, messages(application)).toString
          }
        }

        "must redirect to 'there is a problem' error page when submitting 'Yes' (true) but correspondence details section is missing" in {
          val userAnswers = emptyUserAnswers

          val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

          running(application) {
            val request = FakeRequest(POST, partnerDetailsRemoveFaxNumberYesNoRouteExistingPartners)
              .withFormUrlEncodedBody(("value", "true"))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual "/gambling-variations/there-is-a-problem-with-the-service"
          }
        }
      }
    }

  }

  "newPartners" - {

    "PartnerDetailsRemoveFaxNumberYesNo Controller" - {

      "onPageLoad" - {

        "must return OK and the correct view for a GET when fax number exists in UserAnswers" in {
          val application = applicationBuilder(userAnswers = Some(validUserAnswersNewPartners)).build()

          running(application) {
            val request = FakeRequest(GET, partnerDetailsRemoveFaxNumberYesNoRouteNewPartners)
            val result = route(application, request).value
            val view = application.injector.instanceOf[PartnerDetailsRemoveFaxNumberYesNoView]

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(form, newPartnersIndex1.toString, NormalMode, testFaxNumber)(request,
                                                                                                                messages(application)
                                                                                                               ).toString
          }
        }

        "must populate the view correctly on a GET when the question has previously been answered" in {
          val userAnswers =
            validUserAnswersNewPartners
              .set(PartnerDetailsRemoveFaxNumberYesNoPage(newPartnersIndex1), true)
              .success
              .value

          val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

          running(application) {
            val request = FakeRequest(GET, partnerDetailsRemoveFaxNumberYesNoRouteNewPartners)
            val result = route(application, request).value
            val view = application.injector.instanceOf[PartnerDetailsRemoveFaxNumberYesNoView]

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(form.fill(true), newPartnersIndex1.toString, NormalMode, testFaxNumber)(request,
                                                                                                                           messages(application)
                                                                                                                          ).toString
          }
        }

        "must redirect to JourneyRecovery on a GET when correspondence details exist but faxNumber is None" in {
          val userAnswers = UserAnswers(mgdRegNumber, cleanedDataNewPartners(None))

          val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

          running(application) {
            val request = FakeRequest(GET, partnerDetailsRemoveFaxNumberYesNoRouteNewPartners)
            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
          }
        }

        "must redirect to SystemError when no user answers are found" in {
          val application = applicationBuilder(userAnswers = None).build()

          running(application) {
            val request = FakeRequest(GET, partnerDetailsRemoveFaxNumberYesNoRouteNewPartners)
            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.routes.SystemErrorController.onPageLoad().url
          }
        }
      }

      "onSubmit" - {

        "must remove the fax number and redirect to next page when 'Yes' (true) is submitted" in {
          val mockSessionRepository = mock[SessionRepository]
          when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

          val userAnswers = validUserAnswersNewPartners

          val application =
            applicationBuilder(userAnswers = Some(userAnswers))
              .overrides(
                bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()

          running(application) {
            val request = FakeRequest(POST, partnerDetailsRemoveFaxNumberYesNoRouteNewPartners)
              .withFormUrlEncodedBody(("value", "true"))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual onwardRoute.url
          }
        }

        "must retain the fax number and redirect when 'No' (false) is submitted" in {
          val mockSessionRepository = mock[SessionRepository]
          when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

          val userAnswers = validUserAnswersNewPartners

          val application =
            applicationBuilder(userAnswers = Some(userAnswers))
              .overrides(
                bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()

          running(application) {
            val request =
              FakeRequest(POST, partnerDetailsRemoveFaxNumberYesNoRouteNewPartners)
                .withFormUrlEncodedBody(("value", "false"))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual onwardRoute.url
          }
        }

        "must return BAD_REQUEST and errors when invalid data is submitted" in {
          val userAnswers = validUserAnswersNewPartners

          val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

          running(application) {
            val request =
              FakeRequest(POST, partnerDetailsRemoveFaxNumberYesNoRouteNewPartners)
                .withFormUrlEncodedBody(("value", ""))

            val boundForm = form.bind(Map("value" -> ""))
            val view = application.injector.instanceOf[PartnerDetailsRemoveFaxNumberYesNoView]
            val result = route(application, request).value

            status(result) mustEqual BAD_REQUEST
            contentAsString(result) mustEqual view(boundForm, newPartnersIndex1.toString, NormalMode, testFaxNumber)(request,
                                                                                                                     messages(application)
                                                                                                                    ).toString
          }
        }

        "must redirect to 'there is a problem' error page when submitting 'Yes' (true) but correspondence details section is missing" in {
          val userAnswers = emptyUserAnswers

          val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

          running(application) {
            val request = FakeRequest(POST, partnerDetailsRemoveFaxNumberYesNoRouteNewPartners)
              .withFormUrlEncodedBody(("value", "true"))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual "/gambling-variations/there-is-a-problem-with-the-service"
          }
        }
      }
    }

  }
}
