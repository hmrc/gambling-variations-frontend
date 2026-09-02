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

package controllers.partner

import base.SpecBase
import utils.PartnerUtils.addNewPartnerIndex
import controllers.partner.routes.PartnerDetailsBusinessTypeController
import forms.partner.PartnerDetailsBusinessTypeFormProvider
import models.BusinessType.Corporatebody
import models.{BusinessType, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.partnerdetails.PartnerDetailsBusinessTypePage
import play.api.data.Form
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.partner.PartnerDetailsBusinessTypeView

import scala.concurrent.Future

class PartnerDetailsBusinessTypeControllerSpec extends SpecBase with MockitoSugar with PartnerDetailsHelper {

  val form: Form[BusinessType] = (new PartnerDetailsBusinessTypeFormProvider())()

  lazy val partnerDetailsBusinessTypeRoute: String =
    PartnerDetailsBusinessTypeController.onPageLoad().url

  val validUserAnswers: UserAnswers = UserAnswers(mgdRegNumber, cleanedData())

  private val expectedIndex: Int = validUserAnswers.addNewPartnerIndex()

  "PartnerDetailsBusinessType Controller" - {

    "onPageLoad" - {

      "must return OK and the correct view for a GET when no previous data exists" in {

        val application = applicationBuilder(userAnswers = Some(validUserAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, partnerDetailsBusinessTypeRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[PartnerDetailsBusinessTypeView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" ignore {

        // TODO: this has to be fixed with the indexing ticket
        val targetIndex = validUserAnswers.addNewPartnerIndex()

        val userAnswers = validUserAnswers
          .set(PartnerDetailsBusinessTypePage(targetIndex), Corporatebody)
          .success
          .value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, partnerDetailsBusinessTypeRoute)

          val view = application.injector.instanceOf[PartnerDetailsBusinessTypeView]

          val result = route(application, request).value

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(form.fill(Corporatebody), NormalMode)(request, messages(application)).toString
        }
      }

      "must redirect to SystemError for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, partnerDetailsBusinessTypeRoute)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.SystemErrorController.onPageLoad().url
        }
      }
    }

    "onSubmit" - {

      "must update UserAnswers and redirect to the next page when valid data is submitted" in {

        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        val application =
          applicationBuilder(userAnswers = Some(validUserAnswers))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, PartnerDetailsBusinessTypeController.onSubmit().url)
              .withFormUrlEncodedBody(("value", Corporatebody.toString))

          val result = route(application, request).value

          val expectedAnswers = validUserAnswers
            .set(PartnerDetailsBusinessTypePage(expectedIndex), Corporatebody)
            .success
            .value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
          verify(mockSessionRepository).set(expectedAnswers)
        }
      }

      "must return BAD_REQUEST and errors when invalid data is submitted" in {

        val mockSessionRepository = mock[SessionRepository]

        val application = applicationBuilder(userAnswers = Some(validUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

        running(application) {
          val request =
            FakeRequest(POST, PartnerDetailsBusinessTypeController.onSubmit().url)
              .withFormUrlEncodedBody(("value", ""))

          val boundForm = form.bind(Map("value" -> ""))

          val view = application.injector.instanceOf[PartnerDetailsBusinessTypeView]

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST
          contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
          verify(mockSessionRepository, never()).set(any())
        }
      }

      "must redirect to SystemError for a POST if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request =
            FakeRequest(POST, PartnerDetailsBusinessTypeController.onSubmit().url)
              .withFormUrlEncodedBody(("value", Corporatebody.toString))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.SystemErrorController.onPageLoad().url
        }
      }
    }
  }
}
