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
import controllers.partner.PartnerUtils.getIndex
import controllers.routes
import forms.partner.PartnerDateOfIncorporationFormProvider
import models.{BusinessType, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.partner.{PartnerDateOfIncorporationPage, PartnerDetailsAddPartnerCompletedPage}
import pages.partnerdetails.{PartnerDetailsBusinessTypePage, PartnerDetailsIsBusinessIncorporatedUkPage, PartnerDetailsPage}
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded, Call}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.partner.PartnerDateOfIncorporationView

import java.time.{LocalDate, ZoneOffset}
import scala.concurrent.Future

class PartnerDateOfIncorporationControllerSpec extends SpecBase with MockitoSugar {

  private implicit val messages: Messages = stubMessages()

  private val formProvider = new PartnerDateOfIncorporationFormProvider()
  private val form = formProvider()

  private val onwardRoute = Call("GET", "/foo")

  private val validAnswer = LocalDate.now(ZoneOffset.UTC)

  private val getRoute =
    controllers.partner.routes.PartnerDateOfIncorporationController
      .onPageLoad()
      .url

  private val postRoute =
    controllers.partner.routes.PartnerDateOfIncorporationController
      .onSubmit()
      .url

  override val emptyUserAnswers = UserAnswers(userAnswersId)

  private val partnerDetailsUserAnswers =
    emptyUserAnswers
      .set(PartnerDetailsPage(0), userAnswersId)
      .success
      .value
      .set(PartnerDetailsAddPartnerCompletedPage, false)
      .success
      .value
      .set(
        PartnerDetailsBusinessTypePage(0),
        BusinessType.Corporatebody
      )
      .success
      .value
      .set(
        PartnerDetailsIsBusinessIncorporatedUkPage(0),
        true
      )
      .success
      .value

  private val index: Int =
    partnerDetailsUserAnswers.getIndex

  private def getRequest(): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, getRoute)

  private def postRequest(): FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest(POST, postRoute)
      .withFormUrlEncodedBody(
        "value.day"   -> validAnswer.getDayOfMonth.toString,
        "value.month" -> validAnswer.getMonthValue.toString,
        "value.year"  -> validAnswer.getYear.toString
      )

  "PartnerDateOfIncorporation Controller" - {

    "must return OK and the correct view for a GET" in {

      val application =
        applicationBuilder(
          userAnswers = Some(partnerDetailsUserAnswers)
        ).build()

      running(application) {

        val result =
          route(application, getRequest()).value

        val view =
          application.injector.instanceOf[PartnerDateOfIncorporationView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(
            form,
            NormalMode
          )(getRequest(), messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers =
        partnerDetailsUserAnswers
          .set(
            PartnerDateOfIncorporationPage(index),
            validAnswer
          )
          .success
          .value

      val application =
        applicationBuilder(
          userAnswers = Some(userAnswers)
        ).build()

      running(application) {

        val result =
          route(application, getRequest()).value

        val view =
          application.injector.instanceOf[PartnerDateOfIncorporationView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(
            form.fill(validAnswer),
            NormalMode
          )(getRequest(), messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository =
        mock[SessionRepository]

      when(mockSessionRepository.set(any()))
        .thenReturn(Future.successful(true))

      val application =
        applicationBuilder(
          userAnswers = Some(partnerDetailsUserAnswers)
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

        val result =
          route(application, postRequest()).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application =
        applicationBuilder(
          userAnswers = Some(partnerDetailsUserAnswers)
        ).build()

      val request =
        FakeRequest(POST, postRoute)
          .withFormUrlEncodedBody(
            "value" -> "invalid value"
          )

      running(application) {

        val boundForm =
          form.bind(
            Map("value" -> "invalid value")
          )

        val view =
          application.injector.instanceOf[PartnerDateOfIncorporationView]

        val result =
          route(application, request).value

        status(result) mustEqual BAD_REQUEST

        contentAsString(result) mustEqual
          view(
            boundForm,
            NormalMode
          )(request, messages(application)).toString
      }
    }

    "must return OK for an LLP" in {

      val userAnswers =
        emptyUserAnswers
          .set(
            PartnerDetailsPage(0),
            userAnswersId
          )
          .success
          .value
          .set(
            PartnerDetailsAddPartnerCompletedPage,
            false
          )
          .success
          .value
          .set(
            PartnerDetailsBusinessTypePage(0),
            BusinessType.LimitedLiabilityPartnership
          )
          .success
          .value

      val application =
        applicationBuilder(
          userAnswers = Some(userAnswers)
        ).build()

      running(application) {

        val result =
          route(application, getRequest()).value

        status(result) mustEqual OK
      }
    }

    "must redirect to SystemError when a corporate body is not incorporated in the UK" in {

      val userAnswers =
        emptyUserAnswers
          .set(
            PartnerDetailsPage(0),
            userAnswersId
          )
          .success
          .value
          .set(
            PartnerDetailsAddPartnerCompletedPage,
            false
          )
          .success
          .value
          .set(
            PartnerDetailsBusinessTypePage(0),
            BusinessType.Corporatebody
          )
          .success
          .value
          .set(
            PartnerDetailsIsBusinessIncorporatedUkPage(0),
            false
          )
          .success
          .value

      val application =
        applicationBuilder(
          userAnswers = Some(userAnswers)
        ).build()

      running(application) {

        val result =
          route(application, getRequest()).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.SystemErrorController.onPageLoad().url
      }
    }

    "must redirect to SystemError when the business type is missing" in {

      val userAnswers =
        emptyUserAnswers
          .set(
            PartnerDetailsPage(0),
            userAnswersId
          )
          .success
          .value
          .set(
            PartnerDetailsAddPartnerCompletedPage,
            false
          )
          .success
          .value

      val application =
        applicationBuilder(
          userAnswers = Some(userAnswers)
        ).build()

      running(application) {

        val result =
          route(application, getRequest()).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.SystemErrorController.onPageLoad().url
      }
    }

    "must redirect to SystemError when a corporate body has no incorporated in UK answer" in {

      val userAnswers =
        emptyUserAnswers
          .set(
            PartnerDetailsPage(0),
            userAnswersId
          )
          .success
          .value
          .set(
            PartnerDetailsAddPartnerCompletedPage,
            false
          )
          .success
          .value
          .set(
            PartnerDetailsBusinessTypePage(0),
            BusinessType.Corporatebody
          )
          .success
          .value

      val application =
        applicationBuilder(
          userAnswers = Some(userAnswers)
        ).build()

      running(application) {

        val result =
          route(application, getRequest()).value

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

        val result =
          route(application, getRequest()).value

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

        val result =
          route(application, postRequest()).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual
          routes.SystemErrorController.onPageLoad().url
      }
    }
  }
}
