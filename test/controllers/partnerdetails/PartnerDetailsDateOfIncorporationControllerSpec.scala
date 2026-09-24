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
import forms.partnerdetails.PartnerDateOfIncorporationFormProvider
import models.{BusinessType, CheckMode, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.partnerdetails.{PartnerDetailsAddPartnerCompletedPage, PartnerDetailsBusinessTypePage, PartnerDetailsDateOfIncorporation, PartnerDetailsIsBusinessIncorporatedUkPage, PartnerDetailsMgdRegNumberPage}
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded, Call}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.partnerdetails.PartnerDetailsDateOfIncorporationView

import java.time.{LocalDate, ZoneOffset}
import scala.concurrent.Future

//TODO i think its done
class PartnerDetailsDateOfIncorporationControllerSpec extends SpecBase with MockitoSugar with PartnerDetailsHelper {

  private implicit val messages: Messages = stubMessages()

  private val formProvider = new PartnerDateOfIncorporationFormProvider()
  private val form = formProvider()

  private val validAnswer = LocalDate.now(ZoneOffset.UTC)

  lazy val partnerDetailsDateOfIncorporationRouteExistingPartners: String =
    controllers.partnerdetails.routes.PartnerDetailsDateOfIncorporationController
      .onPageLoad(businessNumber1, CheckMode)
      .url

  lazy val partnerDetailsDateOfIncorporationRouteNewPartners: String =
    controllers.partnerdetails.routes.PartnerDetailsDateOfIncorporationController
      .onPageLoad(newPartnersIndex1.toString, NormalMode)
      .url

  private val partnerDetailsUserAnswersExistingPartners =
    userAnswersPartnerDetailsMinimalValidData
      .set(PartnerDetailsMgdRegNumberPage(businessNumber1), mgdRegNum)
      .success
      .value
      .set(
        PartnerDetailsBusinessTypePage(businessNumber1),
        BusinessType.Corporatebody
      )
      .success
      .value
      .set(
        PartnerDetailsIsBusinessIncorporatedUkPage(businessNumber1),
        true
      )
      .success
      .value

  private val partnerDetailsUserAnswersNewPartners =
    userAnswersPartnerDetailsMinimalValidData
      .set(PartnerDetailsMgdRegNumberPage(newPartnersIndex1), mgdRegNum)
      .success
      .value
      .set(PartnerDetailsAddPartnerCompletedPage(newPartnersIndex1), false) 
      .success
      .value
      .set(
        PartnerDetailsBusinessTypePage(newPartnersIndex1),
        BusinessType.Corporatebody
      )
      .success
      .value
      .set(
        PartnerDetailsIsBusinessIncorporatedUkPage(newPartnersIndex1),
        true
      )
      .success
      .value

  "newPartners" - {

    "PartnerDateOfIncorporation Controller" - {

      "must return OK and the correct view for a GET" in {
        val application =
          applicationBuilder(
            userAnswers = Some(partnerDetailsUserAnswersNewPartners)
          ).build()

        running(application) {
          val request = FakeRequest(GET, partnerDetailsDateOfIncorporationRouteNewPartners)

          val result =
            route(application, request).value

          val view =
            application.injector.instanceOf[PartnerDetailsDateOfIncorporationView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(
              form,
              newPartnersIndex1.toString,
              NormalMode
            )(request, messages(application)).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val userAnswers =
          partnerDetailsUserAnswersNewPartners
            .set(
              PartnerDetailsDateOfIncorporation(newPartnersIndex1),
              validAnswer
            )
            .success
            .value

        val application =
          applicationBuilder(
            userAnswers = Some(userAnswers)
          ).build()

        running(application) {
          val request = FakeRequest(GET, partnerDetailsDateOfIncorporationRouteNewPartners)

          val result =
            route(application, request).value

          val view =
            application.injector.instanceOf[PartnerDetailsDateOfIncorporationView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(
              form.fill(validAnswer),
              newPartnersIndex1.toString,
              NormalMode
            )(request, messages(application)).toString
        }
      }

      "must redirect to the next page when valid data is submitted" in {

        val mockSessionRepository =
          mock[SessionRepository]

        when(mockSessionRepository.set(any()))
          .thenReturn(Future.successful(true))

        val application =
          applicationBuilder(
            userAnswers = Some(partnerDetailsUserAnswersNewPartners)
          )
            .overrides(
              bind[Navigator].toInstance(
                new FakeNavigator(onwardRoute)
              ),
              bind[SessionRepository].toInstance(
                mockSessionRepository
              )
            )
            .build()

        running(application) {
          val request = FakeRequest(POST, partnerDetailsDateOfIncorporationRouteNewPartners)
            .withFormUrlEncodedBody(
              "value.day"   -> validAnswer.getDayOfMonth.toString,
              "value.month" -> validAnswer.getMonthValue.toString,
              "value.year"  -> validAnswer.getYear.toString
            )

          val result =
            route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {

        val application =
          applicationBuilder(
            userAnswers = Some(partnerDetailsUserAnswersNewPartners)
          ).build()

        val request =
          FakeRequest(POST, partnerDetailsDateOfIncorporationRouteNewPartners)
            .withFormUrlEncodedBody(
              "value" -> "invalid value"
            )

        running(application) {

          val boundForm =
            form.bind(
              Map("value" -> "invalid value")
            )

          val view =
            application.injector.instanceOf[PartnerDetailsDateOfIncorporationView]

          val result =
            route(application, request).value

          status(result) mustEqual BAD_REQUEST

          contentAsString(result) mustEqual
            view(
              boundForm,
              newPartnersIndex1.toString,
              NormalMode
            )(request, messages(application)).toString
        }
      }

      "must return OK for an LLP" in {

        val userAnswers =
          userAnswersPartnerDetailsMinimalValidData
            .set(
              PartnerDetailsMgdRegNumberPage(newPartnersIndex1),
              mgdRegNum
            )
            .success
            .value
            .set(
              PartnerDetailsAddPartnerCompletedPage(newPartnersIndex1), 
              false
            )
            .success
            .value
            .set(
              PartnerDetailsBusinessTypePage(newPartnersIndex1),
              BusinessType.LimitedLiabilityPartnership
            )
            .success
            .value

        val application =
          applicationBuilder(
            userAnswers = Some(userAnswers)
          ).build()

        running(application) {
          val request = FakeRequest(GET, partnerDetailsDateOfIncorporationRouteNewPartners)

          val result =
            route(application, request).value

          status(result) mustEqual OK
        }
      }

      "must redirect to SystemError when a corporate body is not incorporated in the UK" in {

        val userAnswers =
          userAnswersPartnerDetailsMinimalValidData
            .set(
              PartnerDetailsMgdRegNumberPage(newPartnersIndex1),
              mgdRegNum
            )
            .success
            .value
            .set(
              PartnerDetailsAddPartnerCompletedPage(newPartnersIndex1),
              false
            )
            .success
            .value
            .set(
              PartnerDetailsBusinessTypePage(newPartnersIndex1),
              BusinessType.Corporatebody
            )
            .success
            .value
            .set(
              PartnerDetailsIsBusinessIncorporatedUkPage(newPartnersIndex1),
              false
            )
            .success
            .value

        val application =
          applicationBuilder(
            userAnswers = Some(userAnswers)
          ).build()

        running(application) {
          val request = FakeRequest(GET, partnerDetailsDateOfIncorporationRouteNewPartners)

          val result =
            route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            routes.SystemErrorController.onPageLoad().url
        }
      }

      "must redirect to SystemError when the business type is missing" in {

        val userAnswers =
          userAnswersPartnerDetailsMinimalValidData
            .set(
              PartnerDetailsMgdRegNumberPage(newPartnersIndex1),
              mgdRegNum
            )
            .success
            .value
            .set(
              PartnerDetailsAddPartnerCompletedPage(newPartnersIndex1), 
              false
            )
            .success
            .value

        val application =
          applicationBuilder(
            userAnswers = Some(userAnswers)
          ).build()

        running(application) {
          val request = FakeRequest(GET, partnerDetailsDateOfIncorporationRouteNewPartners)

          val result =
            route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            routes.SystemErrorController.onPageLoad().url
        }
      }

      "must redirect to SystemError when a corporate body has no incorporated in UK answer" in {

        val userAnswers =
          emptyUserAnswers
            .set(
              PartnerDetailsMgdRegNumberPage(newPartnersIndex1),
              mgdRegNum
            )
            .success
            .value
            .set(
              PartnerDetailsAddPartnerCompletedPage(newPartnersIndex1), 
              false
            )
            .success
            .value
            .set(
              PartnerDetailsBusinessTypePage(newPartnersIndex1),
              BusinessType.Corporatebody
            )
            .success
            .value

        val application =
          applicationBuilder(
            userAnswers = Some(userAnswers)
          ).build()

        running(application) {
          val request = FakeRequest(GET, partnerDetailsDateOfIncorporationRouteNewPartners)

          val result =
            route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            routes.SystemErrorController.onPageLoad().url
        }
      }

      "must redirect to SystemError for a GET if no existing data is found" in {

        val application =
          applicationBuilder(
            userAnswers = None
          ).build()

        running(application) {
          val request = FakeRequest(GET, partnerDetailsDateOfIncorporationRouteNewPartners)

          val result =
            route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            routes.SystemErrorController.onPageLoad().url
        }
      }

      "must redirect to SystemError for a POST if no existing data is found" in {

        val application =
          applicationBuilder(
            userAnswers = None
          ).build()

        running(application) {
          val request = FakeRequest(POST, partnerDetailsDateOfIncorporationRouteNewPartners)
            .withFormUrlEncodedBody(
              "value.day"   -> validAnswer.getDayOfMonth.toString,
              "value.month" -> validAnswer.getMonthValue.toString,
              "value.year"  -> validAnswer.getYear.toString
            )

          val result =
            route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            routes.SystemErrorController.onPageLoad().url
        }
      }
    }
  }

  "partners" - {

    "PartnerDateOfIncorporation Controller" - {

      "must return OK and the correct view for a GET" in {
        val application =
          applicationBuilder(
            userAnswers = Some(partnerDetailsUserAnswersExistingPartners)
          ).build()

        running(application) {
          val request = FakeRequest(GET, partnerDetailsDateOfIncorporationRouteExistingPartners)

          val result =
            route(application, request).value

          val view =
            application.injector.instanceOf[PartnerDetailsDateOfIncorporationView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(
              form,
              businessNumber1,
              CheckMode
            )(request, messages(application)).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val userAnswers =
          partnerDetailsUserAnswersExistingPartners
            .set(
              PartnerDetailsDateOfIncorporation(businessNumber1),
              validAnswer
            )
            .success
            .value

        val application =
          applicationBuilder(
            userAnswers = Some(userAnswers)
          ).build()

        running(application) {
          val request = FakeRequest(GET, partnerDetailsDateOfIncorporationRouteExistingPartners)

          val result =
            route(application, request).value

          val view =
            application.injector.instanceOf[PartnerDetailsDateOfIncorporationView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(
              form.fill(validAnswer),
              businessNumber1,
              CheckMode
            )(request, messages(application)).toString
        }
      }

      "must redirect to the next page when valid data is submitted" in {

        val mockSessionRepository =
          mock[SessionRepository]

        when(mockSessionRepository.set(any()))
          .thenReturn(Future.successful(true))

        val application =
          applicationBuilder(
            userAnswers = Some(partnerDetailsUserAnswersExistingPartners)
          )
            .overrides(
              bind[Navigator].toInstance(
                new FakeNavigator(onwardRoute)
              ),
              bind[SessionRepository].toInstance(
                mockSessionRepository
              )
            )
            .build()

        running(application) {
          val request = FakeRequest(POST, partnerDetailsDateOfIncorporationRouteExistingPartners)
            .withFormUrlEncodedBody(
              "value.day"   -> validAnswer.getDayOfMonth.toString,
              "value.month" -> validAnswer.getMonthValue.toString,
              "value.year"  -> validAnswer.getYear.toString
            )

          val result =
            route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {

        val application =
          applicationBuilder(
            userAnswers = Some(partnerDetailsUserAnswersExistingPartners)
          ).build()

        val request =
          FakeRequest(POST, partnerDetailsDateOfIncorporationRouteExistingPartners)
            .withFormUrlEncodedBody(
              "value" -> "invalid value"
            )

        running(application) {

          val boundForm =
            form.bind(
              Map("value" -> "invalid value")
            )

          val view =
            application.injector.instanceOf[PartnerDetailsDateOfIncorporationView]

          val result =
            route(application, request).value

          status(result) mustEqual BAD_REQUEST

          contentAsString(result) mustEqual
            view(
              boundForm,
              businessNumber1,
              CheckMode
            )(request, messages(application)).toString
        }
      }

      "must return OK for an LLP" in {

        val userAnswers =
          userAnswersPartnerDetailsMinimalValidData
            .set(
              PartnerDetailsMgdRegNumberPage(businessNumber1),
              mgdRegNum
            )
            .success
            .value
            .set(
              PartnerDetailsBusinessTypePage(businessNumber1),
              BusinessType.LimitedLiabilityPartnership
            )
            .success
            .value

        val application =
          applicationBuilder(
            userAnswers = Some(userAnswers)
          ).build()

        running(application) {
          val request = FakeRequest(GET, partnerDetailsDateOfIncorporationRouteExistingPartners)

          val result =
            route(application, request).value

          status(result) mustEqual OK
        }
      }

      "must redirect to SystemError when a corporate body is not incorporated in the UK" in {

        val userAnswers =
          userAnswersPartnerDetailsMinimalValidData
            .set(
              PartnerDetailsMgdRegNumberPage(businessNumber1),
              mgdRegNum
            )
            .success
            .value
            .set(
              PartnerDetailsBusinessTypePage(businessNumber1),
              BusinessType.Corporatebody
            )
            .success
            .value
            .set(
              PartnerDetailsIsBusinessIncorporatedUkPage(businessNumber1),
              false
            )
            .success
            .value

        val application =
          applicationBuilder(
            userAnswers = Some(userAnswers)
          ).build()

        running(application) {
          val request = FakeRequest(GET, partnerDetailsDateOfIncorporationRouteExistingPartners)

          val result =
            route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            routes.SystemErrorController.onPageLoad().url
        }
      }

      "must redirect to SystemError when the business type is missing" in {

        val userAnswers =
          userAnswersPartnerDetailsMinimalValidData
            .set(
              PartnerDetailsMgdRegNumberPage(businessNumber1),
              mgdRegNum
            )
            .success
            .value

        val application =
          applicationBuilder(
            userAnswers = Some(userAnswers)
          ).build()

        running(application) {
          val request = FakeRequest(GET, partnerDetailsDateOfIncorporationRouteExistingPartners)

          val result =
            route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            routes.SystemErrorController.onPageLoad().url
        }
      }

      "must redirect to SystemError when a corporate body has no incorporated in UK answer" in {

        val userAnswers =
          emptyUserAnswers
            .set(
              PartnerDetailsMgdRegNumberPage(businessNumber1),
              mgdRegNum
            )
            .success
            .value
            .set(
              PartnerDetailsBusinessTypePage(businessNumber1),
              BusinessType.Corporatebody
            )
            .success
            .value

        val application =
          applicationBuilder(
            userAnswers = Some(userAnswers)
          ).build()

        running(application) {
          val request = FakeRequest(GET, partnerDetailsDateOfIncorporationRouteExistingPartners)

          val result =
            route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            routes.SystemErrorController.onPageLoad().url
        }
      }

      "must redirect to SystemError for a GET if no existing data is found" in {

        val application =
          applicationBuilder(
            userAnswers = None
          ).build()

        running(application) {
          val request = FakeRequest(GET, partnerDetailsDateOfIncorporationRouteExistingPartners)

          val result =
            route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            routes.SystemErrorController.onPageLoad().url
        }
      }

      "must redirect to SystemError for a POST if no existing data is found" in {

        val application =
          applicationBuilder(
            userAnswers = None
          ).build()

        running(application) {
          val request = FakeRequest(POST, partnerDetailsDateOfIncorporationRouteExistingPartners)
            .withFormUrlEncodedBody(
              "value.day"   -> validAnswer.getDayOfMonth.toString,
              "value.month" -> validAnswer.getMonthValue.toString,
              "value.year"  -> validAnswer.getYear.toString
            )

          val result =
            route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            routes.SystemErrorController.onPageLoad().url
        }
      }
    }

  }
}
