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
import forms.FaxNumberFormProvider
import models.{CheckMode, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.partnerdetails.{PartnerDetailsAddPartnerCompletedPage, PartnerDetailsChangePartnerFaxNumberPage, PartnerDetailsCorrespondenceFaxNumberPage}
import play.api.data.Form
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.partnerdetails.PartnerDetailsChangeFaxNumberView

import scala.concurrent.Future

class PartnerDetailsChangePartnerFaxNumberControllerSpec extends SpecBase with MockitoSugar with PartnerDetailsHelper {

  val form: Form[String] = (new FaxNumberFormProvider())("partnerDetailsFaxNumber")

  lazy val changePartnerFaxNumberRoute: String =
    controllers.partnerdetails.routes.PartnerDetailsChangePartnerFaxNumberController.onPageLoad(businessNumber1, CheckMode).url

  val userAnswersWithNoFax: UserAnswers =
    UserAnswers(mgdRegNumber, cleanedData())
      .set(PartnerDetailsAddPartnerCompletedPage(newPartnersIndex1), false) // TODO passingNewPartnerIndex1 instead (no string)
      .success
      .value

  val userAnswersWithFax: UserAnswers =
    UserAnswers(mgdRegNumber, cleanedData(faxNumber = Some(testFaxNumber)))
      .set(PartnerDetailsAddPartnerCompletedPage(newPartnersIndex1), false) // TODO passingNewPartnerIndex1 instead (no string)
      .success
      .value

  "ChangePartnerFaxNumber Controller" - {

    "onPageLoad" - {

      "must return OK and the correct view for a GET when no previous data exists" in {

        val application = applicationBuilder(userAnswers = Some(userAnswersWithNoFax)).build()

        running(application) {
          val request = FakeRequest(GET, changePartnerFaxNumberRoute)

          val result = route(application, request).value

          val view = application.injector.instanceOf[PartnerDetailsChangeFaxNumberView]

          status(result) mustBe OK
          contentAsString(result) mustBe view(form, businessNumber1, CheckMode)(request, messages(application)).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val application = applicationBuilder(userAnswers = Some(userAnswersWithFax)).build()

        running(application) {
          val request = FakeRequest(GET, changePartnerFaxNumberRoute)

          val view = application.injector.instanceOf[PartnerDetailsChangeFaxNumberView]

          val result = route(application, request).value

          status(result) mustBe OK
          contentAsString(result) mustBe view(form.fill(testFaxNumber), businessNumber1, CheckMode)(request, messages(application)).toString
        }
      }

      "must redirect to SystemErrorController for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, changePartnerFaxNumberRoute)

          val result = route(application, request).value

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe controllers.routes.SystemErrorController.onPageLoad().url
        }
      }
    }

    "onSubmit" - {

      "must update UserAnswers and redirect to the next page when valid data is submitted" in {

        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        val application =
          applicationBuilder(userAnswers = Some(userAnswersWithNoFax))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, changePartnerFaxNumberRoute)
              .withFormUrlEncodedBody(("faxNumber", testFaxNumber))

          val result = route(application, request).value

          val expectedAnswers = userAnswersWithNoFax
            .set(PartnerDetailsCorrespondenceFaxNumberPage(index), testFaxNumber)
            .success
            .value
            .set(PartnerDetailsChangePartnerFaxNumberPage(index), value = true)
            .success
            .value

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe onwardRoute.url
          verify(mockSessionRepository).set(expectedAnswers)
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {

        val mockSessionRepository = mock[SessionRepository]

        val application = applicationBuilder(userAnswers = Some(userAnswersWithNoFax))
          .build()

        running(application) {
          val request =
            FakeRequest(POST, changePartnerFaxNumberRoute)
              .withFormUrlEncodedBody(("value", ""))

          val boundForm = form.bind(Map("value" -> ""))

          val view = application.injector.instanceOf[PartnerDetailsChangeFaxNumberView]

          val result = route(application, request).value

          status(result) mustBe BAD_REQUEST
          contentAsString(result) mustBe view(boundForm, businessNumber1, CheckMode)(request, messages(application)).toString
          verify(mockSessionRepository, never()).set(any())
        }
      }

      "must redirect to Journey Recovery for a POST if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request =
            FakeRequest(POST, changePartnerFaxNumberRoute)
              .withFormUrlEncodedBody(("value", testFaxNumber))

          val result = route(application, request).value

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe controllers.routes.SystemErrorController.onPageLoad().url
        }
      }
    }
  }
}
