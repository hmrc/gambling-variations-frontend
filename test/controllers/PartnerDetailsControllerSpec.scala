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

package controllers

import base.SpecBase
import forms.partner.AddAnotherPartnerFormProvider
import models.UserAnswers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.partner.PartnerDetailsAddAnotherPartnerYesNoPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import viewmodels.checkAnswers.partner.PartnerDetailsViewModel
import views.html.partner.PartnerDetailsView

import scala.concurrent.Future

class PartnerDetailsControllerSpec extends SpecBase with MockitoSugar {

  private val formProvider =
    new AddAnotherPartnerFormProvider()

  private val addPartnerForm =
    formProvider("partnerDetails.addPartner.error.required")

  private val addAnotherPartnerForm =
    formProvider("partnerDetails.addAnotherPartner.error.required")

  private lazy val partnerDetailsRoute =
    controllers.partner.routes.PartnerDetailsController.onPageLoad.url

  private lazy val onSubmitRoute =
    controllers.partner.routes.PartnerDetailsController.onSubmit.url

  private lazy val onPartnerDetailsRoute =
    controllers.partner.routes.PartnerDetailsController
      .onPartnerDetails(0)
      .url

  private lazy val onRemoveRoute =
    controllers.partner.routes.PartnerDetailsController
      .onRemove(0)
      .url

  import pages.partnerdetails.*

  private val userAnswersWithPartner =
    emptyUserAnswers
      .set(PartnerDetailsPage(0), "XWM00000001762")
      .success
      .value
      .set(PartnerDetailsTradingNamePage(0), "XYZ Consulting")
      .success
      .value
      .set(PartnerDetailsBusinessNamePage(0), "XYZ Consulting Ltd")
      .success
      .value

  private lazy val onContinueRoute =
    controllers.partner.routes.PartnerDetailsController.onContinue.url

  "PartnerDetails Controller" - {

    "onPageLoad" - {

      "must return OK and the correct view for a GET" in {

        val application =
          applicationBuilder(
            userAnswers = Some(userAnswersWithPartner)
          ).build()

        running(application) {

          val request =
            FakeRequest(
              GET,
              partnerDetailsRoute
            )

          val result =
            route(application, request).value

          val view =
            application.injector
              .instanceOf[PartnerDetailsView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(
              addAnotherPartnerForm,
              viewModel(
                application,
                userAnswersWithPartner
              )
            )(
              request,
              messages(application)
            ).toString
        }
      }

      "must populate the form when the question has previously been answered" in {

        val userAnswers =
          userAnswersWithPartner
            .set(
              PartnerDetailsAddAnotherPartnerYesNoPage,
              true
            )
            .success
            .value

        val application =
          applicationBuilder(
            userAnswers = Some(userAnswers)
          ).build()

        running(application) {

          val request =
            FakeRequest(
              GET,
              partnerDetailsRoute
            )

          val result =
            route(application, request).value

          val view =
            application.injector
              .instanceOf[PartnerDetailsView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(
              addAnotherPartnerForm.fill(true),
              viewModel(
                application,
                userAnswers
              )
            )(
              request,
              messages(application)
            ).toString
        }
      }
    }

    "onSubmit" - {

      "must redirect to the partner details page when Yes is submitted" in {

        val mockSessionRepository =
          mock[SessionRepository]

        when(
          mockSessionRepository.set(any[UserAnswers])
        ).thenReturn(
          Future.successful(true)
        )

        val application =
          applicationBuilder(
            userAnswers = Some(userAnswersWithPartner)
          )
            .overrides(
              bind[SessionRepository]
                .toInstance(mockSessionRepository)
            )
            .build()

        running(application) {

          val request =
            FakeRequest(
              POST,
              onSubmitRoute
            ).withFormUrlEncodedBody(
              "value" -> "true"
            )

          val result =
            route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            controllers.partner.routes.PartnerDetailsBusinessTypeController.onPageLoad().url
        }
      }

      "must redirect to Change Registration Details when No is submitted" in {

        val mockSessionRepository =
          mock[SessionRepository]

        when(
          mockSessionRepository.set(any[UserAnswers])
        ).thenReturn(
          Future.successful(true)
        )

        val application =
          applicationBuilder(
            userAnswers = Some(userAnswersWithPartner)
          )
            .overrides(
              bind[SessionRepository]
                .toInstance(mockSessionRepository)
            )
            .build()

        running(application) {

          val request =
            FakeRequest(
              POST,
              onSubmitRoute
            ).withFormUrlEncodedBody(
              "value" -> "false"
            )

          val result =
            route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            controllers.routes.ChangeRegistrationDetailsController
              .onPageLoad()
              .url
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {

        val application =
          applicationBuilder(
            userAnswers = Some(userAnswersWithPartner)
          ).build()

        running(application) {

          val request =
            FakeRequest(
              POST,
              onSubmitRoute
            ).withFormUrlEncodedBody(
              "value" -> ""
            )

          val boundForm =
            addAnotherPartnerForm.bind(
              Map(
                "value" -> ""
              )
            )

          val result =
            route(application, request).value

          val view =
            application.injector
              .instanceOf[PartnerDetailsView]

          status(result) mustEqual BAD_REQUEST

          contentAsString(result) mustEqual
            view(
              boundForm,
              viewModel(
                application,
                userAnswersWithPartner
              )
            )(
              request,
              messages(application)
            ).toString
        }
      }

    }

    "onPartnerDetails" - {

      "must redirect to the partner details page" in {

        val application =
          applicationBuilder(
            userAnswers = Some(userAnswersWithPartner)
          ).build()

        running(application) {

          val request =
            FakeRequest(
              GET,
              onPartnerDetailsRoute
            )

          val result =
            route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            controllers.partner.routes.PartnerDetailsController.onPageLoad.url
        }
      }
    }

    "onRemove" - {

      "must redirect to the partner details page" in {

        val application =
          applicationBuilder(
            userAnswers = Some(userAnswersWithPartner)
          ).build()

        running(application) {

          val request =
            FakeRequest(
              GET,
              onRemoveRoute
            )

          val result =
            route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            controllers.partner.routes.PartnerDetailsController.onPageLoad.url
        }
      }
    }

    "onContinue" - {

      "must redirect to Change Registration Details" in {

        val application =
          applicationBuilder(
            userAnswers = Some(userAnswersWithPartner)
          ).build()

        running(application) {

          val request =
            FakeRequest(
              GET,
              onContinueRoute
            )

          val result =
            route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            controllers.routes.ChangeRegistrationDetailsController
              .onPageLoad()
              .url
        }
      }
    }
  }

  private def viewModel(
    application: play.api.Application,
    userAnswers: UserAnswers
  ): PartnerDetailsViewModel = {

    val frontendAppConfig =
      application.injector
        .instanceOf[config.FrontendAppConfig]

    implicit val appMessages =
      messages(application)

    PartnerDetailsViewModel.from(
      userAnswers,
      frontendAppConfig
    )
  }
}
