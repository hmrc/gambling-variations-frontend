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
import models.UserAnswers
import org.scalatestplus.mockito.MockitoSugar
import pages.partnerdetails.PartnerDetailsAddPartnerCompletedPage
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import viewmodels.checkAnswers.partnerdetails.CheckPartnerDetailsViewModel
import views.html.partnerdetails.PartnerDetailsCheckYourAnswersView

class PartnerDetailsCheckYourAnswersControllerSpec extends SpecBase with MockitoSugar with PartnerDetailsHelper {

  private def checkYourAnswersRoute(index: String): String =
    controllers.partnerdetails.routes.PartnerDetailsCheckYourAnswersController
      .onPageLoad(index)
      .url

  lazy val checkYourAnswersRouteExistingPartners: String = checkYourAnswersRoute(businessNumber1)
  lazy val checkYourAnswersRouteNewPartners: String = checkYourAnswersRoute(newPartnersIndex1.toString)

  val userAnswersExistingPartners: UserAnswers = UserAnswers(mgdRegNumber, cleanedDataExistingPartners())
  val userAnswersNewPartners: UserAnswers = UserAnswers(mgdRegNumber, cleanedDataNewPartners())

  "partners" - {
    "PartnerDetailsCheckYourAnswers Controller" - {

      "must return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(userAnswersExistingPartners)).build()

        running(application) {
          val request = FakeRequest(GET, checkYourAnswersRouteExistingPartners)

          val result = route(application, request).value

          val view = application.injector.instanceOf[PartnerDetailsCheckYourAnswersView]

          val expectedViewModel =
            CheckPartnerDetailsViewModel.from(userAnswersExistingPartners, businessNumber1, isNewPartnerFlow = false, isSubmitted = None)

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(expectedViewModel)(request, messages(application)).toString
        }
      }

      "must redirect to SystemError for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, checkYourAnswersRouteExistingPartners)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.SystemErrorController.onPageLoad().url
        }
      }
    }
  }

  "newPartners" - {
    "PartnerDetailsCheckYourAnswers Controller" - {

      "must return OK and the correct view for a GET when the partner has not been saved" in {

        val userAnswersForGet: UserAnswers =
          userAnswersNewPartners
            .set(PartnerDetailsAddPartnerCompletedPage(newPartnersIndex1), false)
            .success
            .value

        val application = applicationBuilder(userAnswers = Some(userAnswersForGet)).build()

        running(application) {
          val request = FakeRequest(GET, checkYourAnswersRouteNewPartners)

          val result = route(application, request).value

          val view = application.injector.instanceOf[PartnerDetailsCheckYourAnswersView]

          val expectedViewModel =
            CheckPartnerDetailsViewModel.from(userAnswersForGet, newPartnersIndex1, isNewPartnerFlow = true, isSubmitted = Some(false))

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(expectedViewModel)(request, messages(application)).toString
        }
      }

      "must return OK and the correct view for a GET when the partner has been saved" in {

        val userAnswersForGet: UserAnswers =
          userAnswersNewPartners
            .set(PartnerDetailsAddPartnerCompletedPage(newPartnersIndex1), true)
            .success
            .value

        val application = applicationBuilder(userAnswers = Some(userAnswersForGet)).build()

        running(application) {
          val request = FakeRequest(GET, checkYourAnswersRouteNewPartners)

          val result = route(application, request).value

          val view = application.injector.instanceOf[PartnerDetailsCheckYourAnswersView]

          val expectedViewModel =
            CheckPartnerDetailsViewModel.from(userAnswersForGet, newPartnersIndex1, isNewPartnerFlow = true, isSubmitted = Some(true))

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(expectedViewModel)(request, messages(application)).toString
        }
      }

      "must redirect to SystemError for a GET if no existing data is found" in {

        val application = applicationBuilder(userAnswers = None).build()

        running(application) {
          val request = FakeRequest(GET, checkYourAnswersRouteNewPartners)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.SystemErrorController.onPageLoad().url
        }
      }
    }
  }

  "invalid index" - {
    "PartnerDetailsCheckYourAnswers Controller" - {

      "must redirect to SystemError for a negative index" in {

        val application = applicationBuilder(userAnswers = Some(userAnswersNewPartners)).build()

        running(application) {
          val request = FakeRequest(GET, checkYourAnswersRoute("-1"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.SystemErrorController.onPageLoad().url
        }
      }

      "must redirect to SystemError for a reference with invalid characters" in {

        val application = applicationBuilder(userAnswers = Some(userAnswersExistingPartners)).build()

        running(application) {
          val request = FakeRequest(GET, checkYourAnswersRoute("BPN-0001"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.SystemErrorController.onPageLoad().url
        }
      }
    }
  }
}
