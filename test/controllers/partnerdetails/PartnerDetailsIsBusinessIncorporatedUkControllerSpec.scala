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
import forms.partnerdetails.PartnerDetailsIsBusinessIncorporatedUkFormProvider
import models.{BusinessType, CheckMode, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.partnerdetails.{PartnerDetailsAddPartnerCompletedPage, PartnerDetailsBusinessTypePage, PartnerDetailsIsBusinessIncorporatedUkPage}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.partnerdetails.PartnerDetailsIsBusinessIncorporatedUkView

import scala.concurrent.Future

//TODO kinda done
class PartnerDetailsIsBusinessIncorporatedUkControllerSpec extends SpecBase with MockitoSugar with PartnerDetailsHelper {

  val formProvider = new PartnerDetailsIsBusinessIncorporatedUkFormProvider()
  val form = formProvider()

  private val userAnswersExistingPartners =
    userAnswersPartnerDetailsExistingPartners
      .set(PartnerDetailsBusinessTypePage(businessNumber1), BusinessType.Corporatebody)
      .success
      .value
      .set(PartnerDetailsIsBusinessIncorporatedUkPage(businessNumber1), true)
      .success
      .value

  private val emptyUserAnswersExistingPartners =
    userAnswersPartnerDetailsMinimalValidData

  private val userAnswersNewPartners =
    userAnswersPartnerDetailsNewPartners
      .set(PartnerDetailsBusinessTypePage(newPartnersIndex1), BusinessType.Corporatebody)
      .success
      .value
      .set(PartnerDetailsIsBusinessIncorporatedUkPage(newPartnersIndex1), true)
      .success
      .value
      .set(PartnerDetailsAddPartnerCompletedPage(newPartnersIndex1), false)
      .success
      .value

  private val emptyUserAnswersNewPartners =
    userAnswersPartnerDetailsMinimalValidData
      .set(PartnerDetailsAddPartnerCompletedPage(newPartnersIndex1), false)
      .success
      .value

  lazy val partnerDetailsIsBusinessIncorporatedUkRouteExistingPartners =
    controllers.partnerdetails.routes.PartnerDetailsIsBusinessIncorporatedUkController.onPageLoad(businessNumber1, CheckMode).url

  lazy val partnerDetailsIsBusinessIncorporatedUkRouteNewPartners =
    controllers.partnerdetails.routes.PartnerDetailsIsBusinessIncorporatedUkController.onPageLoad(newPartnersIndex1.toString, NormalMode).url

  "newPartners" - {

    "PartnerDetailsIsBusinessIncorporatedUk Controller" - {

      "must return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswersNewPartners)).build()

        running(application) {
          val request = FakeRequest(GET, partnerDetailsIsBusinessIncorporatedUkRouteNewPartners)

          val result = route(application, request).value

          val view = application.injector.instanceOf[PartnerDetailsIsBusinessIncorporatedUkView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, newPartnersIndex1.toString, NormalMode)(request, messages(application)).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val application = applicationBuilder(userAnswers = Some(userAnswersExistingPartners)).build()

        running(application) {
          val request = FakeRequest(GET, partnerDetailsIsBusinessIncorporatedUkRouteExistingPartners)

          val view = application.injector.instanceOf[PartnerDetailsIsBusinessIncorporatedUkView]

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form.fill(true), businessNumber1, CheckMode)(request, messages(application)).toString
        }
      }

      "must redirect to the next page when valid data is submitted" in {

        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswersExistingPartners))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, partnerDetailsIsBusinessIncorporatedUkRouteExistingPartners)
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswersExistingPartners)).build()

        running(application) {
          val request =
            FakeRequest(POST, partnerDetailsIsBusinessIncorporatedUkRouteExistingPartners)
              .withFormUrlEncodedBody(("value", ""))

          val boundForm = form.bind(Map("value" -> ""))

          val view = application.injector.instanceOf[PartnerDetailsIsBusinessIncorporatedUkView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, businessNumber1, CheckMode)(request, messages(application)).toString
        }
      }

      "must redirect to SystemError for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, partnerDetailsIsBusinessIncorporatedUkRouteExistingPartners)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.SystemErrorController.onPageLoad().url
        }
      }

      "must redirect to SystemError for a POST if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request =
            FakeRequest(POST, partnerDetailsIsBusinessIncorporatedUkRouteExistingPartners)
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.SystemErrorController.onPageLoad().url
        }
      }
    }

  }

  "partners" - {

    "PartnerDetailsIsBusinessIncorporatedUk Controller" - {

      "must return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswersExistingPartners)).build()

        running(application) {
          val request = FakeRequest(GET, partnerDetailsIsBusinessIncorporatedUkRouteExistingPartners)

          val result = route(application, request).value

          val view = application.injector.instanceOf[PartnerDetailsIsBusinessIncorporatedUkView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, businessNumber1, CheckMode)(request, messages(application)).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val application = applicationBuilder(userAnswers = Some(userAnswersExistingPartners)).build()

        running(application) {
          val request = FakeRequest(GET, partnerDetailsIsBusinessIncorporatedUkRouteExistingPartners)

          val view = application.injector.instanceOf[PartnerDetailsIsBusinessIncorporatedUkView]

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form.fill(true), businessNumber1, CheckMode)(request, messages(application)).toString
        }
      }

      "must redirect to the next page when valid data is submitted" in {

        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswersExistingPartners))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, partnerDetailsIsBusinessIncorporatedUkRouteExistingPartners)
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswersExistingPartners)).build()

        running(application) {
          val request =
            FakeRequest(POST, partnerDetailsIsBusinessIncorporatedUkRouteExistingPartners)
              .withFormUrlEncodedBody(("value", ""))

          val boundForm = form.bind(Map("value" -> ""))

          val view = application.injector.instanceOf[PartnerDetailsIsBusinessIncorporatedUkView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, businessNumber1, CheckMode)(request, messages(application)).toString
        }
      }

      "must redirect to SystemError for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, partnerDetailsIsBusinessIncorporatedUkRouteExistingPartners)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.SystemErrorController.onPageLoad().url
        }
      }

      "must redirect to SystemError for a POST if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request =
            FakeRequest(POST, partnerDetailsIsBusinessIncorporatedUkRouteExistingPartners)
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.SystemErrorController.onPageLoad().url
        }
      }
    }

  }
}
