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
import controllers.partnerdetails.routes.PartnerDetailsBusinessTypeController
import forms.partnerdetails.PartnerDetailsBusinessTypeFormProvider
import models.BusinessType.{Corporatebody, Soleproprietor}
import models.{BusinessType, CheckMode, NormalMode, SoleProprietorName, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.partnerdetails.{PartnerDetailsAddPartnerCompletedPage, PartnerDetailsBusinessTypePage, PartnerDetailsDateOfBirthPage, PartnerDetailsSoleProprietorPage}
import play.api.data.Form
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.partnerdetails.PartnerDetailsBusinessTypeView

import scala.concurrent.Future

class PartnerDetailsBusinessTypeControllerSpec extends SpecBase with MockitoSugar with PartnerDetailsHelper {

  val form: Form[BusinessType] = (new PartnerDetailsBusinessTypeFormProvider())()

  lazy val partnerDetailsBusinessTypeRouteExistingPartners: String =
    PartnerDetailsBusinessTypeController.onPageLoad(businessNumber1, CheckMode).url

  lazy val partnerDetailsBusinessTypeRouteNewPartners: String =
    PartnerDetailsBusinessTypeController.onPageLoad(newPartnersIndex1.toString, NormalMode).url

  val validUserAnswersExistingPartners: UserAnswers =
    userAnswersPartnerDetailsExistingPartners

  val validUserAnswersNewPartners: UserAnswers =
    userAnswersPartnerDetailsNewPartners
      .set(PartnerDetailsAddPartnerCompletedPage(newPartnersIndex1), false)
      .success
      .value

  "going back to change a new partner's business type" - {

    val soleProprietorName = SoleProprietorName("Mr", "Sam", None, "Smith")
    val dateOfBirth = java.time.LocalDate.of(1980, 1, 31)

    def soleProprietorAnswers: UserAnswers =
      validUserAnswersNewPartners
        .set(PartnerDetailsBusinessTypePage(newPartnersIndex1), Soleproprietor)
        .flatMap(_.set(PartnerDetailsSoleProprietorPage(newPartnersIndex1), soleProprietorName))
        .flatMap(_.set(PartnerDetailsDateOfBirthPage(newPartnersIndex1), dateOfBirth))
        .success
        .value

    def savedAnswersAfterSubmitting(businessType: BusinessType): UserAnswers = {
      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      val application =
        applicationBuilder(userAnswers = Some(soleProprietorAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, PartnerDetailsBusinessTypeController.onSubmit(newPartnersIndex1.toString, NormalMode).url)
            .withFormUrlEncodedBody(("value", businessType.toString))

        status(route(application, request).value) mustEqual SEE_OTHER
      }

      val captor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      verify(mockSessionRepository).set(captor.capture())
      captor.getValue
    }

    "must remove the sole proprietor's name and date of birth from the saved answers when another business type is selected" in {
      val saved = savedAnswersAfterSubmitting(Corporatebody)

      saved.get(PartnerDetailsBusinessTypePage(newPartnersIndex1)) mustBe Some(Corporatebody)
      saved.get(PartnerDetailsSoleProprietorPage(newPartnersIndex1)) mustBe None
      saved.get(PartnerDetailsDateOfBirthPage(newPartnersIndex1)) mustBe None
    }

    "must keep the sole proprietor's name and date of birth in the saved answers when sole proprietor is selected again" in {
      val saved = savedAnswersAfterSubmitting(Soleproprietor)

      saved.get(PartnerDetailsSoleProprietorPage(newPartnersIndex1)) mustBe Some(soleProprietorName)
      saved.get(PartnerDetailsDateOfBirthPage(newPartnersIndex1)) mustBe Some(dateOfBirth)
    }
  }

  "newPartners" - {
    "PartnerDetailsBusinessType Controller" - {

      "onPageLoad" - {

        "must populate the view correctly on a GET when the question has previously been answered" in {

          val application = applicationBuilder(userAnswers = Some(validUserAnswersNewPartners)).build()

          running(application) {
            val request = FakeRequest(GET, partnerDetailsBusinessTypeRouteNewPartners)

            val result = route(application, request).value

            val view = application.injector.instanceOf[PartnerDetailsBusinessTypeView]

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(form.fill(Soleproprietor), newPartnersIndex1.toString, NormalMode)(request,
                                                                                                                      messages(application)
                                                                                                                     ).toString
          }
        }

        "must return OK and the correct view for a GET when no previous data exists" in {

          val userAnswers = validUserAnswersExistingPartners
            .remove(PartnerDetailsBusinessTypePage(newPartnersIndex1))
            .success
            .value

          val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

          running(application) {
            val request = FakeRequest(GET, partnerDetailsBusinessTypeRouteNewPartners)

            val view = application.injector.instanceOf[PartnerDetailsBusinessTypeView]

            val result = route(application, request).value

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(form, newPartnersIndex1.toString, NormalMode)(request, messages(application)).toString
          }
        }

        "must redirect to SystemError for a GET if no existing data is found" in {

          val application = applicationBuilder(userAnswers = None).build()

          running(application) {
            val request = FakeRequest(GET, partnerDetailsBusinessTypeRouteNewPartners)

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
            applicationBuilder(userAnswers = Some(validUserAnswersNewPartners))
              .overrides(
                bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()

          running(application) {
            val request =
              FakeRequest(POST, PartnerDetailsBusinessTypeController.onSubmit(newPartnersIndex1.toString, NormalMode).url)
                .withFormUrlEncodedBody(("value", Corporatebody.toString))

            val result = route(application, request).value

            val expectedAnswers = validUserAnswersNewPartners
              .set(PartnerDetailsBusinessTypePage(newPartnersIndex1), Corporatebody)
              .success
              .value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual onwardRoute.url
            verify(mockSessionRepository).set(expectedAnswers)
          }
        }

        "must return BAD_REQUEST and errors when invalid data is submitted" in {

          val mockSessionRepository = mock[SessionRepository]

          val application = applicationBuilder(userAnswers = Some(validUserAnswersNewPartners))
            .overrides(
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

          running(application) {
            val request =
              FakeRequest(POST, PartnerDetailsBusinessTypeController.onSubmit(newPartnersIndex1.toString, NormalMode).url)
                .withFormUrlEncodedBody(("value", ""))

            val boundForm = form.bind(Map("value" -> ""))

            val view = application.injector.instanceOf[PartnerDetailsBusinessTypeView]

            val result = route(application, request).value

            status(result) mustEqual BAD_REQUEST
            contentAsString(result) mustEqual view(boundForm, newPartnersIndex1.toString, NormalMode)(request, messages(application)).toString
            verify(mockSessionRepository, never()).set(any())
          }
        }

        "must redirect to SystemError for a POST if no existing data is found" in {

          val application = applicationBuilder(userAnswers = None).build()

          running(application) {
            val request =
              FakeRequest(POST, PartnerDetailsBusinessTypeController.onSubmit(newPartnersIndex1.toString, NormalMode).url)
                .withFormUrlEncodedBody(("value", Corporatebody.toString))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.routes.SystemErrorController.onPageLoad().url
          }
        }
      }
    }
  }

  "partners" - {

    "PartnerDetailsBusinessType Controller" - {

      "onPageLoad" - {

        "must populate the view correctly on a GET when the question has previously been answered" in {

          val application = applicationBuilder(userAnswers = Some(validUserAnswersExistingPartners)).build()

          running(application) {
            val request = FakeRequest(GET, partnerDetailsBusinessTypeRouteExistingPartners)

            val result = route(application, request).value

            val view = application.injector.instanceOf[PartnerDetailsBusinessTypeView]

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(form.fill(Soleproprietor), businessNumber1, CheckMode)(request, messages(application)).toString
          }
        }

        "must return OK and the correct view for a GET when no previous data exists" in {

          val userAnswers = validUserAnswersExistingPartners
            .remove(PartnerDetailsBusinessTypePage(businessNumber1))
            .success
            .value

          val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

          running(application) {
            val request = FakeRequest(GET, partnerDetailsBusinessTypeRouteExistingPartners)

            val view = application.injector.instanceOf[PartnerDetailsBusinessTypeView]

            val result = route(application, request).value

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(form, businessNumber1, CheckMode)(request, messages(application)).toString
          }
        }

        "must redirect to SystemError for a GET if no existing data is found" in {

          val application = applicationBuilder(userAnswers = None).build()

          running(application) {
            val request = FakeRequest(GET, partnerDetailsBusinessTypeRouteExistingPartners)

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
            applicationBuilder(userAnswers = Some(validUserAnswersExistingPartners))
              .overrides(
                bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
                bind[SessionRepository].toInstance(mockSessionRepository)
              )
              .build()

          running(application) {
            val request =
              FakeRequest(POST, PartnerDetailsBusinessTypeController.onSubmit(businessNumber1, CheckMode).url)
                .withFormUrlEncodedBody(("value", Corporatebody.toString))

            val result = route(application, request).value

            val expectedAnswers = validUserAnswersExistingPartners
              .set(PartnerDetailsBusinessTypePage(businessNumber1), Corporatebody)
              .success
              .value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual onwardRoute.url
            verify(mockSessionRepository).set(expectedAnswers)
          }
        }

        "must return BAD_REQUEST and errors when invalid data is submitted" in {

          val mockSessionRepository = mock[SessionRepository]

          val application = applicationBuilder(userAnswers = Some(validUserAnswersExistingPartners))
            .overrides(
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

          running(application) {
            val request =
              FakeRequest(POST, PartnerDetailsBusinessTypeController.onSubmit(businessNumber1, CheckMode).url)
                .withFormUrlEncodedBody(("value", ""))

            val boundForm = form.bind(Map("value" -> ""))

            val view = application.injector.instanceOf[PartnerDetailsBusinessTypeView]

            val result = route(application, request).value

            status(result) mustEqual BAD_REQUEST
            contentAsString(result) mustEqual view(boundForm, businessNumber1, CheckMode)(request, messages(application)).toString
            verify(mockSessionRepository, never()).set(any())
          }
        }

        "must redirect to SystemError for a POST if no existing data is found" in {

          val application = applicationBuilder(userAnswers = None).build()

          running(application) {
            val request =
              FakeRequest(POST, PartnerDetailsBusinessTypeController.onSubmit(businessNumber1, CheckMode).url)
                .withFormUrlEncodedBody(("value", Corporatebody.toString))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.routes.SystemErrorController.onPageLoad().url
          }
        }
      }
    }

  }
}
