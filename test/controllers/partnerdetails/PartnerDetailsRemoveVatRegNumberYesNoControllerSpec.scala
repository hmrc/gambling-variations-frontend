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
import forms.partnerdetails.PartnerDetailsRemoveVatRegNumberYesNoFormProvider
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.partnerdetails.{PartnerDetailsAddPartnerCompletedPage, PartnerDetailsRemoveVatRegNumberYesNoPage}
import pages.partnerdetails.PartnerDetailsVrnPage
import play.api.data.Form
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.partnerdetails.PartnerDetailsRemoveVatRegNumberYesNoView

import scala.concurrent.Future

//TODO
//class PartnerDetailsRemoveVatRegNumberYesNoControllerSpec extends SpecBase with MockitoSugar with PartnerDetailsHelper {
//
//  private val formProvider = new PartnerDetailsRemoveVatRegNumberYesNoFormProvider()
//  val form: Form[Boolean] = formProvider()
//
//  private lazy val removeVrnRoute =
//    controllers.partnerdetails.routes.PartnerDetailsRemoveVatRegNumberYesNoController.onPageLoad(newPartnersIndex1.toString).url
//
//  private val userAnswersWithVrn: UserAnswers =
//    UserAnswers(mgdRegNumber, cleanedDataExistingPartners(vrn = Some(testVRN)))
//      .set(PartnerDetailsAddPartnerCompletedPage(newPartnersIndex1), false)
//      .success
//      .value
//      .set(PartnerDetailsVrnPage(newPartnersIndex1), testVRN)
//      .success
//      .value
//
//  "PartnerDetailsRemoveVatRegNumberYesNo Controller" - {
//
//    "onPageLoad" - {
//
//      "must return OK and the correct view for a GET when VRN exists in UserAnswers" in {
//
//        val application = applicationBuilder(userAnswers = Some(userAnswersWithVrn)).build()
//
//        running(application) {
//          val request = FakeRequest(GET, removeVrnRoute)
//          val result = route(application, request).value
//          val view = application.injector.instanceOf[PartnerDetailsRemoveVatRegNumberYesNoView]
//
//          status(result) mustEqual OK
//          contentAsString(result) mustEqual view(form, newPartnersIndex1.toString, NormalMode, testVRN)(request, messages(application)).toString
//        }
//      }
//
//      "must populate the view correctly on a GET when the page has previously been answered" in {
//
//        val userAnswers = userAnswersWithVrn
//          .set(PartnerDetailsRemoveVatRegNumberYesNoPage(newPartnersIndex1), true)
//          .success
//          .value
//
//        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
//
//        running(application) {
//          val request = FakeRequest(GET, removeVrnRoute)
//          val view = application.injector.instanceOf[PartnerDetailsRemoveVatRegNumberYesNoView]
//          val result = route(application, request).value
//
//          status(result) mustEqual OK
//          contentAsString(result) mustEqual view(form.fill(true), newPartnersIndex1.toString, NormalMode, testVRN)(request,
//                                                                                                                   messages(application)
//                                                                                                                  ).toString
//        }
//      }
//
//      "must redirect to SystemErrorController for a GET if no VRN is found in UserAnswers" in {
//
//        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
//
//        running(application) {
//          val request = FakeRequest(GET, removeVrnRoute)
//          val result = route(application, request).value
//
//          status(result) mustEqual SEE_OTHER
//          redirectLocation(result).value mustEqual controllers.routes.SystemErrorController.onPageLoad().url
//        }
//      }
//
//      "must redirect to SystemErrorController for a GET if no existing data is found" in {
//
//        val application = applicationBuilder(userAnswers = None).build()
//
//        running(application) {
//          val request = FakeRequest(GET, removeVrnRoute)
//          val result = route(application, request).value
//
//          status(result) mustEqual SEE_OTHER
//          redirectLocation(result).value mustEqual controllers.routes.SystemErrorController
//            .onPageLoad()
//            .url // TODO: think this passes by accident not design!
//        }
//      }
//    }
//
//    "onSubmit" - {
//
//      "must remove VRN, save updated answers, and redirect to the next page when Yes is selected" in {
//        val mockSessionRepository = mock[SessionRepository]
//        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
//
//        val application = applicationBuilder(userAnswers = Some(userAnswersWithVrn))
//          .overrides(
//            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
//            bind[SessionRepository].toInstance(mockSessionRepository)
//          )
//          .build()
//
//        running(application) {
//          val request = FakeRequest(POST, removeVrnRoute)
//            .withFormUrlEncodedBody(("value", "true"))
//
//          val result = route(application, request).value
//
//          status(result) mustEqual SEE_OTHER
//          redirectLocation(result).value mustEqual onwardRoute.url
//
//          val expectedAnswers = userAnswersWithVrn
//            .remove(PartnerDetailsVrnPage(newPartnersIndex1))
//            .flatMap(_.set(PartnerDetailsRemoveVatRegNumberYesNoPage(newPartnersIndex1), true))
//            .success
//            .value
//
//          verify(mockSessionRepository).set(expectedAnswers)
//        }
//      }
//
//      "must retain VRN, save answers, and redirect to the next page when No is selected" in {
//
//        val mockSessionRepository = mock[SessionRepository]
//        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
//
//        val application = applicationBuilder(userAnswers = Some(userAnswersWithVrn))
//          .overrides(
//            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
//            bind[SessionRepository].toInstance(mockSessionRepository)
//          )
//          .build()
//
//        running(application) {
//          val request = FakeRequest(POST, removeVrnRoute)
//            .withFormUrlEncodedBody(("value", "false"))
//
//          val result = route(application, request).value
//
//          status(result) mustEqual SEE_OTHER
//          redirectLocation(result).value mustEqual onwardRoute.url
//
//          val expectedAnswers = userAnswersWithVrn
//            .set(PartnerDetailsRemoveVatRegNumberYesNoPage(newPartnersIndex1), false)
//            .success
//            .value
//
//          verify(mockSessionRepository).set(expectedAnswers)
//        }
//      }
//
//      "must return a Bad Request and errors when invalid data is submitted" in {
//
//        val application = applicationBuilder(userAnswers = Some(userAnswersWithVrn)).build()
//
//        running(application) {
//          val request = FakeRequest(POST, removeVrnRoute)
//            .withFormUrlEncodedBody(("value", ""))
//
//          val boundForm = form.bind(Map("value" -> ""))
//          val view = application.injector.instanceOf[PartnerDetailsRemoveVatRegNumberYesNoView]
//
//          val result = route(application, request).value
//
//          status(result) mustEqual BAD_REQUEST
//          contentAsString(result) mustEqual view(boundForm, newPartnersIndex1.toString, NormalMode, testVRN)(request, messages(application)).toString
//        }
//      }
//
//      "must redirect to SystemErrorController for a POST if no existing data is found" in {
//
//        val application = applicationBuilder(userAnswers = None).build()
//
//        running(application) {
//          val request = FakeRequest(POST, removeVrnRoute)
//            .withFormUrlEncodedBody(("value", "true"))
//
//          val result = route(application, request).value
//
//          status(result) mustEqual SEE_OTHER
//          redirectLocation(result).value mustEqual controllers.routes.SystemErrorController.onPageLoad().url
//        }
//      }
//    }
//  }
//}
