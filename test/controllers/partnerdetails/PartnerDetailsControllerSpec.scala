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
import forms.partnerdetails.AddAnotherPartnerFormProvider
import models.{BusinessType, CheckMode, NormalMode, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.partnerdetails.*
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import viewmodels.checkAnswers.partnerdetails.PartnerDetailsViewModel
import views.html.partnerdetails.PartnerDetailsView

import scala.concurrent.Future

class PartnerDetailsControllerSpec extends SpecBase with MockitoSugar with PartnerDetailsHelper {

  private val existingPartners = (0 to 100).map(e => (e.toString, f"XYZ Consulting-$e%03d"))

  private val paginationHelper = PaginationHelper(1)
  private val paginationHelperMaxSizePage1 = PaginationHelper(100, 1, existingPartners)
  private val paginationHelperMaxSizePage2 = PaginationHelper(100, 2, existingPartners)

  private val formProvider =
    new AddAnotherPartnerFormProvider()

  private val addAnotherPartnerForm =
    formProvider("partnerDetails.addAnotherPartner.error.required")

  private lazy val partnerDetailsRoute =
    controllers.partnerdetails.routes.PartnerDetailsController.onPageLoad(None).url

  private lazy val onSubmitRoute =
    controllers.partnerdetails.routes.PartnerDetailsController.onSubmit(None).url

  private lazy val onPartnerDetailsRoute =
    controllers.partnerdetails.routes.PartnerDetailsController
      .onPartnerDetails(businessNumber1, CheckMode)
      .url

  private lazy val onRemoveRoute =
    controllers.partnerdetails.routes.PartnerDetailsController
      .onRemove(businessNumber1, CheckMode)
      .url

  private val userAnswersWithPartner =
    emptyUserAnswers
      .set(PartnerDetailsMgdRegNumberPage(businessNumber1), "XWM00000001762")
      .success
      .value
      .set(PartnerDetailsTradingNamePage(businessNumber1), "XYZ Consulting")
      .success
      .value
      .set(PartnerDetailsBusinessNamePage(businessNumber1), "XYZ Consulting Ltd")
      .success
      .value
      .set(PartnerDetailsBusinessTypePage(businessNumber1), BusinessType.Partnership)
      .success
      .value

  private val userAnswersWithPartnerMaxSize = (1 to 100).foldLeft(emptyUserAnswers)((userAnswers, businessNumber) => {
    userAnswers
      .set(PartnerDetailsMgdRegNumberPage(businessNumber.toString), f"XWM00000001762-$businessNumber%03d")
      .success
      .value
      .set(PartnerDetailsTradingNamePage(businessNumber.toString), f"XYZ Consulting-$businessNumber%03d")
      .success
      .value
      .set(PartnerDetailsBusinessNamePage(businessNumber.toString), f"XYZ Consulting Ltd-$businessNumber%03d")
      .success
      .value
      .set(PartnerDetailsBusinessTypePage(businessNumber.toString), BusinessType.Partnership)
      .success
      .value
  })

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
                userAnswersWithPartner,
                paginationHelper.partnerDetailsBusinessNumberList
              ),
              paginationHelper.paginatedViewModel,
              paginationHelper.page,
              paginationHelper.from,
              paginationHelper.to,
              paginationHelper.totalRecords
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
                userAnswers,
                paginationHelper.partnerDetailsBusinessNumberList
              ),
              paginationHelper.paginatedViewModel,
              paginationHelper.page,
              paginationHelper.from,
              paginationHelper.to,
              paginationHelper.totalRecords
            )(
              request,
              messages(application)
            ).toString
        }
      }

      "must populate existing partners table with correct pagination" in {

        val application =
          applicationBuilder(
            userAnswers = Some(userAnswersWithPartnerMaxSize)
          ).build()

        running(application) {

          val request =
            FakeRequest(
              GET,
              partnerDetailsRoute + "?page=2"
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
                userAnswersWithPartnerMaxSize,
                paginationHelperMaxSizePage2.partnerDetailsBusinessNumberList.slice(10, 20)
              ),
              paginationHelperMaxSizePage2.paginatedViewModel,
              paginationHelperMaxSizePage2.page,
              paginationHelperMaxSizePage2.from,
              paginationHelperMaxSizePage2.to,
              paginationHelperMaxSizePage2.totalRecords
            )(
              request,
              messages(application)
            ).toString
        }
      }

      "must populate existing partners table with correct pagination with page one when page param is missing" in {

        val application =
          applicationBuilder(
            userAnswers = Some(userAnswersWithPartnerMaxSize)
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
                userAnswersWithPartnerMaxSize,
                paginationHelperMaxSizePage1.partnerDetailsBusinessNumberList.slice(0, 10)
              ),
              paginationHelperMaxSizePage1.paginatedViewModel,
              paginationHelperMaxSizePage1.page,
              paginationHelperMaxSizePage1.from,
              paginationHelperMaxSizePage1.to,
              paginationHelperMaxSizePage1.totalRecords
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
            controllers.partnerdetails.routes.PartnerDetailsBusinessTypeController.onPageLoad(0.toString, NormalMode).url
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
                userAnswersWithPartner,
                paginationHelper.partnerDetailsBusinessNumberList
              ),
              paginationHelper.paginatedViewModel,
              paginationHelper.page,
              paginationHelper.from,
              paginationHelper.to,
              paginationHelper.totalRecords
            )(
              request,
              messages(application)
            ).toString
        }
      }

      "must return a Bad Request and errors when invalid data is submitted with correct pagination page" in {
        val application =
          applicationBuilder(
            userAnswers = Some(userAnswersWithPartnerMaxSize)
          ).build()

        running(application) {

          val request =
            FakeRequest(
              POST,
              onSubmitRoute + "?page=2"
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
                userAnswersWithPartnerMaxSize,
                paginationHelperMaxSizePage2.partnerDetailsBusinessNumberList.slice(10, 20)
              ),
              paginationHelperMaxSizePage2.paginatedViewModel,
              paginationHelperMaxSizePage2.page,
              paginationHelperMaxSizePage2.from,
              paginationHelperMaxSizePage2.to,
              paginationHelperMaxSizePage2.totalRecords
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
            controllers.partnerdetails.routes.PartnerDetailsController.onPageLoad(None).url
        }
      }
    }

    "onRemove" - {

      "must save the selected partner and redirect to the partner delete date page" in {

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
              GET,
              onRemoveRoute
            )

          val result =
            route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            controllers.partnerdetails.routes.PartnerDetailsDeleteDateController
              .onPageLoad()
              .url

          val capturedAnswers =
            ArgumentCaptor.forClass(classOf[UserAnswers])

          verify(mockSessionRepository).set(capturedAnswers.capture())

          capturedAnswers.getValue
            .get(PartnerDetailsChosenPartnerToRemovePage) mustEqual Some(businessNumber1)
        }
      }

    }
  }

  private def viewModel(
    application: play.api.Application,
    userAnswers: UserAnswers,
    paginatedPartnerDetailsBusinessNumber: Seq[String]
  ): PartnerDetailsViewModel = {

    val frontendAppConfig =
      application.injector
        .instanceOf[config.FrontendAppConfig]

    implicit val appMessages =
      messages(application)

    PartnerDetailsViewModel.from(
      paginatedPartnerDetailsBusinessNumber,
      todayDate,
      userAnswers,
      frontendAppConfig
    )
  }
}
