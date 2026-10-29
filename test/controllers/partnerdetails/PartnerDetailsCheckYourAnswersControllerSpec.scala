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
import models.BusinessType.Soleproprietor
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.partnerdetails.{PartnerDetailsAddPartnerCompletedPage, PartnerDetailsBusinessNamePage}
import play.api.Application
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import viewmodels.checkAnswers.partnerdetails.CheckPartnerDetailsViewModel
import views.html.partnerdetails.PartnerDetailsCheckYourAnswersView

import scala.concurrent.Future

class PartnerDetailsCheckYourAnswersControllerSpec extends SpecBase with MockitoSugar with PartnerDetailsHelper {

  private def checkYourAnswersRoute(index: String): String =
    controllers.partnerdetails.routes.PartnerDetailsCheckYourAnswersController
      .onPageLoad(index)
      .url

  lazy val checkYourAnswersRouteExistingPartners: String = checkYourAnswersRoute(businessNumber1)
  lazy val checkYourAnswersRouteNewPartners: String = checkYourAnswersRoute(newPartnersIndex1.toString)

  val userAnswersExistingPartners: UserAnswers = UserAnswers(mgdRegNumber, cleanedDataExistingPartners())
  val userAnswersNewPartners: UserAnswers = UserAnswers(mgdRegNumber, cleanedDataNewPartners())

  def continueRoute(index: String): String =
    controllers.partnerdetails.routes.PartnerDetailsCheckYourAnswersController
      .onContinue(index)
      .url

  val incompleteNewPartnerAnswers: UserAnswers =
    UserAnswers(mgdRegNumber, Json.obj())
      .set(PartnerDetailsBusinessNamePage(newPartnersIndex1), "Partner Ltd")
      .success
      .value

  def applicationWithRepository(answers: Option[UserAnswers], repository: SessionRepository): Application =
    applicationBuilder(userAnswers = answers)
      .overrides(bind[SessionRepository].toInstance(repository))
      .build()

  "PartnerDetailsCheckYourAnswers Controller" - {

    "existing partners" - {

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

    "newPartners" - {

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

      "must redirect to the first missing mandatory page and not save when a new partner has a gap" in {
        /*
         * Small example
         * We select business type, we skip the name, and we get to the end of the journey.
         * When we click on continue we should head back to PartnerDetailsChangeBusinessNameController
         * */

        val repository = mock[SessionRepository]
        when(repository.set(any[UserAnswers])).thenReturn(Future.successful(true))

        val completeAnswers = UserAnswers(mgdRegNumber, cleanedDataNewPartnersRouting(businessType = 1))
        val application = applicationWithRepository(Some(completeAnswers), repository)

        running(application) {
          val request = FakeRequest(POST, continueRoute(newPartnersIndex1.toString))

          val result = route(application, request).value

          redirectLocation(result).value mustEqual controllers.partnerdetails.routes.PartnerDetailsChangeBusinessNameController
            .onPageLoad(newPartnersIndex1.toString, Soleproprietor, NormalMode)
            .url

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

    "invalid index" - {

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
