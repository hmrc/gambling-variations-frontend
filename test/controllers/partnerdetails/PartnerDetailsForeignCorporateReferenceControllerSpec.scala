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
import forms.partner.PartnerDetailsForeignCorporateReferenceFormProvider
import models.BusinessType.{Corporatebody, Soleproprietor}
import models.{NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.partnerdetails.PartnerDetailsAddPartnerCompletedPage
import pages.partnerdetails.{PartnerDetailsBusinessTypePage, PartnerDetailsForeignCorporateReferencePage, PartnerDetailsIsBusinessIncorporatedUkPage}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.partnerdetails.PartnerDetailsForeignCorporateReferenceView

import scala.concurrent.Future

//TODO so far this spec has test for ADD PARTNER, NOT CHANGE PARTNER
class PartnerDetailsForeignCorporateReferenceControllerSpec extends SpecBase with MockitoSugar with PartnerDetailsHelper {

  val formProvider = new PartnerDetailsForeignCorporateReferenceFormProvider()
  val form = formProvider()

  lazy val partnerDetailsForeignCorporateReferenceRoute =
    controllers.partnerdetails.routes.PartnerDetailsForeignCorporateReferenceController
      .onPageLoad(newPartnersIndex1.toString, NormalMode)
      .url // TODO casting int

  def validUserAnswers(fcr: Option[String] = None): UserAnswers = UserAnswers(mgdRegNumber, cleanedDataNewPartners(fcr = fcr))
  val userAnswersWithNoFcr: UserAnswers = validUserAnswers()
  val userAnswersWithFcr: UserAnswers = validUserAnswers(Some(testForeignCorpRef))

  "PartnerDetailsForeignCorporateReference Controller" - {

    "must return OK and the correct view for a GET" in {
      val userAnswersForGet: UserAnswers =
        userAnswersWithNoFcr
          .set(PartnerDetailsAddPartnerCompletedPage(newPartnersIndex1), false)
          .success
          .value
          .set(PartnerDetailsBusinessTypePage(newPartnersIndex1), Corporatebody)
          .success
          .value
          .set(PartnerDetailsIsBusinessIncorporatedUkPage(newPartnersIndex1), false)
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(userAnswersForGet)).build()

      running(application) {
        val request = FakeRequest(GET, partnerDetailsForeignCorporateReferenceRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PartnerDetailsForeignCorporateReferenceView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, newPartnersIndex1.toString, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly when the question has previously been answered" in {

      val userAnswersWithFcr: UserAnswers =
        validUserAnswers(Some(testForeignCorpRef))
          .set(PartnerDetailsAddPartnerCompletedPage(newPartnersIndex1), false)
          .success
          .value
          .set(PartnerDetailsBusinessTypePage(newPartnersIndex1), Corporatebody)
          .success
          .value
          .set(PartnerDetailsIsBusinessIncorporatedUkPage(newPartnersIndex1), false)
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(userAnswersWithFcr)).build()

      running(application) {
        val request = FakeRequest(GET, partnerDetailsForeignCorporateReferenceRoute)

        val view = application.injector.instanceOf[PartnerDetailsForeignCorporateReferenceView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(testForeignCorpRef), newPartnersIndex1.toString, NormalMode)(request,
                                                                                                                      messages(application)
                                                                                                                     ).toString
      }
    }

    "must redirect to SystemError when business type is not Corporatebody" in {

      val userAnswersForGet: UserAnswers =
        userAnswersWithNoFcr
          .set(PartnerDetailsAddPartnerCompletedPage(newPartnersIndex1), false)
          .success
          .value
          .set(PartnerDetailsBusinessTypePage(newPartnersIndex1), Soleproprietor)
          .success
          .value
          .set(PartnerDetailsIsBusinessIncorporatedUkPage(newPartnersIndex1), false)
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(userAnswersForGet)).build()

      running(application) {
        val request = FakeRequest(GET, partnerDetailsForeignCorporateReferenceRoute)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.SystemErrorController.onPageLoad().url
      }
    }

    "must redirect to SystemError when the business is incorporated in the UK" in {

      val userAnswersForGet: UserAnswers =
        userAnswersWithNoFcr
          .set(PartnerDetailsAddPartnerCompletedPage(newPartnersIndex1), false)
          .success
          .value
          .set(PartnerDetailsBusinessTypePage(newPartnersIndex1), Corporatebody)
          .success
          .value
          .set(PartnerDetailsIsBusinessIncorporatedUkPage(newPartnersIndex1), true)
          .success
          .value

      val application = applicationBuilder(userAnswers = Some(userAnswersForGet)).build()

      running(application) {
        val request = FakeRequest(GET, partnerDetailsForeignCorporateReferenceRoute)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.SystemErrorController.onPageLoad().url
      }
    }

    "must redirect to SystemError for a GET when incorporatedInUK has not been answered" in {

      val userAnswersForGet: UserAnswers =
        userAnswersWithNoFcr
          .set(PartnerDetailsAddPartnerCompletedPage(newPartnersIndex1), false)
          .success
          .value
          .set(PartnerDetailsBusinessTypePage(newPartnersIndex1), Corporatebody)
          .success
          .value
      // PartnerDetailsIsBusinessIncorporatedUkPage intentionally left unset -> None

      val application = applicationBuilder(userAnswers = Some(userAnswersForGet)).build()

      running(application) {
        val request = FakeRequest(GET, partnerDetailsForeignCorporateReferenceRoute)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.SystemErrorController.onPageLoad().url
      }
    }

    "must redirect to SystemError when business type has not been answered" in {

      val userAnswersForGet: UserAnswers =
        userAnswersWithNoFcr
          .set(PartnerDetailsAddPartnerCompletedPage(newPartnersIndex1), false)
          .success
          .value
          .set(PartnerDetailsIsBusinessIncorporatedUkPage(newPartnersIndex1), false)
          .success
          .value
      // PartnerDetailsBusinessTypePage intentionally left unset -> None

      val application = applicationBuilder(userAnswers = Some(userAnswersForGet)).build()

      running(application) {
        val request = FakeRequest(GET, partnerDetailsForeignCorporateReferenceRoute)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.SystemErrorController.onPageLoad().url
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val userAnswersForSubmit: UserAnswers =
        userAnswersWithNoFcr.set(PartnerDetailsAddPartnerCompletedPage(newPartnersIndex1), false).success.value

      val application =
        applicationBuilder(userAnswers = Some(userAnswersForSubmit))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {

        val expectedAnswers =
          userAnswersForSubmit.set(PartnerDetailsForeignCorporateReferencePage(newPartnersIndex1), testForeignCorpRef).success.value

        val request =
          FakeRequest(POST, partnerDetailsForeignCorporateReferenceRoute)
            .withFormUrlEncodedBody(("value", testForeignCorpRef))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
        verify(mockSessionRepository).set(expectedAnswers)
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val badData = "£$%^"
      val application = applicationBuilder(userAnswers = Some(userAnswersWithNoFcr)).build()

      running(application) {
        val request =
          FakeRequest(POST, partnerDetailsForeignCorporateReferenceRoute)
            .withFormUrlEncodedBody(("value", badData))

        val boundForm = form.bind(Map("value" -> badData))

        val view = application.injector.instanceOf[PartnerDetailsForeignCorporateReferenceView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, newPartnersIndex1.toString, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to SystemError for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, partnerDetailsForeignCorporateReferenceRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.SystemErrorController.onPageLoad().url
      }
    }

    "must redirect to SystemError for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, partnerDetailsForeignCorporateReferenceRoute)
            .withFormUrlEncodedBody(("value", "unknownRef"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.SystemErrorController.onPageLoad().url
      }
    }
  }
}
