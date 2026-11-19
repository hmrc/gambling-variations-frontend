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
import controllers.businesscontactdetails.LoadedPartnerDetails
import forms.partnerdetails.AddAnotherPartnerFormProvider
import models.{BusinessType, CheckMode, NormalMode, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{atLeastOnce, never, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.BusinessNumberOrIndex
import pages.partnerdetails.*
import play.api.Application
import play.api.inject.bind
import play.api.libs.json.JsObject
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import utils.PartnerUtils
import viewmodels.checkAnswers.partnerdetails.PartnerDetailsViewModel
import views.html.partnerdetails.PartnerDetailsView

import scala.concurrent.Future

class PartnerDetailsControllerSpec extends SpecBase with MockitoSugar with PartnerDetailsHelper {
  private val existingPartners =
    (1 to 100).map { index =>
      index.toString -> f"XYZ Consulting-$index%03d"
    }

  private lazy val paginationHelper =
    PaginationHelper(
      1,
      businessNumbers = Seq(
        businessNumber1 -> businessNumber1
      ).toIndexedSeq
    )

  private lazy val paginationHelperMaxSizePage1 =
    PaginationHelper(
      100,
      1,
      existingPartners
    )

  private lazy val paginationHelperMaxSizePage2 =
    PaginationHelper(
      100,
      2,
      existingPartners
    )

  private val formProvider =
    new AddAnotherPartnerFormProvider()

  private val addAnotherPartnerForm =
    formProvider(
      "partnerDetails.addAnotherPartner.error.required"
    )

  private lazy val partnerDetailsRoute =
    routes.PartnerDetailsController
      .onPageLoad(None)
      .url

  private lazy val onSubmitRoute =
    routes.PartnerDetailsController
      .onSubmit(None)
      .url

  private lazy val onPartnerDetailsRoute =
    routes.PartnerDetailsController
      .onPartnerDetails(
        businessNumber1,
        CheckMode
      )
      .url

  private lazy val onRemoveRoute =
    routes.PartnerDetailsController
      .onRemove(
        businessNumber1,
        CheckMode
      )
      .url

  override val emptyUserAnswers: UserAnswers =
    LoadedPartnerDetails(
      userAnswersId,
      JsObject.empty,
      ExistingPartnerDetailsSection,
      NewPartnerDetailsSection
    )

  private val userAnswersWithPartner =
    emptyUserAnswers
      .set(
        PartnerDetailsMgdRegNumberPage(
          businessNumber1
        ),
        "XWM00000001762"
      )
      .success
      .value
      .set(
        PartnerDetailsTradingNamePage(
          businessNumber1
        ),
        "XYZ Consulting"
      )
      .success
      .value
      .set(
        PartnerDetailsBusinessNamePage(
          businessNumber1
        ),
        "XYZ Consulting Ltd"
      )
      .success
      .value
      .set(
        PartnerDetailsBusinessTypePage(
          businessNumber1
        ),
        BusinessType.Partnership
      )
      .success
      .value

  private val userAnswersWithPartnerMaxSize =
    (1 to 100).foldLeft(emptyUserAnswers) { case (userAnswers, businessNumber) =>
      val partnerNumber =
        businessNumber.toString

      userAnswers
        .set(
          PartnerDetailsMgdRegNumberPage(
            partnerNumber
          ),
          f"XWM00000001762-$businessNumber%03d"
        )
        .success
        .value
        .set(
          PartnerDetailsTradingNamePage(
            partnerNumber
          ),
          f"XYZ Consulting-$businessNumber%03d"
        )
        .success
        .value
        .set(
          PartnerDetailsBusinessNamePage(
            partnerNumber
          ),
          f"XYZ Consulting Ltd-$businessNumber%03d"
        )
        .success
        .value
        .set(
          PartnerDetailsBusinessTypePage(
            partnerNumber
          ),
          BusinessType.Partnership
        )
        .success
        .value
    }

  "PartnerDetailsController" - {

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
            route(
              application,
              request
            ).value

          val view =
            application.injector
              .instanceOf[PartnerDetailsView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(
              addAnotherPartnerForm,
              viewModel(
                application             = application,
                userAnswers             = userAnswersWithPartner,
                paginatedPartnerNumbers = paginationHelper.partnerDetailsBusinessNumberList
              ),
              paginationHelper.paginatedViewModel,
              paginationHelper.page,
              paginationHelper.totalPages,
              paginationHelper.from,
              paginationHelper.to,
              paginationHelper.totalRecords
            )(
              request,
              messages(application)
            ).toString
        }
      }

      "must not populate the form when the question has previously been answered" in {

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
            route(
              application,
              request
            ).value

          val view =
            application.injector
              .instanceOf[PartnerDetailsView]

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(
              addAnotherPartnerForm,
              viewModel(
                application             = application,
                userAnswers             = userAnswers,
                paginatedPartnerNumbers = paginationHelper.partnerDetailsBusinessNumberList
              ),
              paginationHelper.paginatedViewModel,
              paginationHelper.page,
              paginationHelper.totalPages,
              paginationHelper.from,
              paginationHelper.to,
              paginationHelper.totalRecords
            )(
              request,
              messages(application)
            ).toString
        }
      }

      "must populate the existing partners table with page two" in {

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
            route(
              application,
              request
            ).value

          val view =
            application.injector
              .instanceOf[PartnerDetailsView]

          val pageTwoPartnerNumbers =
            paginationHelperMaxSizePage2.partnerDetailsBusinessNumberList
              .slice(10, 20)

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(
              addAnotherPartnerForm,
              viewModel(
                application             = application,
                userAnswers             = userAnswersWithPartnerMaxSize,
                paginatedPartnerNumbers = pageTwoPartnerNumbers
              ),
              paginationHelperMaxSizePage2.paginatedViewModel,
              paginationHelperMaxSizePage2.page,
              paginationHelperMaxSizePage2.totalPages,
              paginationHelperMaxSizePage2.from,
              paginationHelperMaxSizePage2.to,
              paginationHelperMaxSizePage2.totalRecords
            )(
              request,
              messages(application)
            ).toString
        }
      }

      "must use page one when the page parameter is missing" in {

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
            route(
              application,
              request
            ).value

          val view =
            application.injector
              .instanceOf[PartnerDetailsView]

          val pageOnePartnerNumbers =
            paginationHelperMaxSizePage1.partnerDetailsBusinessNumberList
              .slice(0, 10)

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(
              addAnotherPartnerForm,
              viewModel(
                application             = application,
                userAnswers             = userAnswersWithPartnerMaxSize,
                paginatedPartnerNumbers = pageOnePartnerNumbers
              ),
              paginationHelperMaxSizePage1.paginatedViewModel,
              paginationHelperMaxSizePage1.page,
              paginationHelperMaxSizePage1.totalPages,
              paginationHelperMaxSizePage1.from,
              paginationHelperMaxSizePage1.to,
              paginationHelperMaxSizePage1.totalRecords
            )(
              request,
              messages(application)
            ).toString
        }
      }

      "must hide the add option when 100 partners count towards the maximum" in {

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
            route(
              application,
              request
            ).value

          val expectedViewModel =
            viewModel(
              application = application,
              userAnswers = userAnswersWithPartnerMaxSize,
              paginatedPartnerNumbers = paginationHelperMaxSizePage1.partnerDetailsBusinessNumberList
                .slice(0, 10)
            )

          status(result) mustEqual OK

          expectedViewModel.partners must have size 10
          expectedViewModel.addAnotherPartner mustBe false
          expectedViewModel.showMaximumPartnersMessage mustBe true
        }
      }

      "must show the add option when one of 100 partners has a future leaving date" in {

        val answers =
          userAnswersWithPartnerMaxSize
            .set(
              PartnerDetailsDateOfLeavingPage("1"),
              todayDate.plusDays(10)
            )
            .success
            .value

        val application =
          applicationBuilder(
            userAnswers = Some(answers)
          ).build()

        running(application) {

          val request =
            FakeRequest(
              GET,
              partnerDetailsRoute
            )

          val result =
            route(
              application,
              request
            ).value

          val expectedViewModel =
            viewModel(
              application = application,
              userAnswers = answers,
              paginatedPartnerNumbers = paginationHelperMaxSizePage1.partnerDetailsBusinessNumberList
                .slice(0, 10)
            )

          status(result) mustEqual OK

          /*
           * 100 total minus one having a leaving date gives a
           * maximum-limit count of 99.
           */
          expectedViewModel.addAnotherPartner mustBe true
          expectedViewModel.showMaximumPartnersMessage mustBe false
        }
      }

      "must show the add option when one of 100 partners has a past leaving date" in {

        val answers =
          userAnswersWithPartnerMaxSize
            .set(
              PartnerDetailsDateOfLeavingPage("1"),
              todayDate.minusDays(10)
            )
            .success
            .value

        val application =
          applicationBuilder(
            userAnswers = Some(answers)
          ).build()

        running(application) {

          val request =
            FakeRequest(
              GET,
              partnerDetailsRoute
            )

          val result =
            route(
              application,
              request
            ).value

          val expectedViewModel =
            viewModel(
              application = application,
              userAnswers = answers,
              paginatedPartnerNumbers = paginationHelperMaxSizePage1.partnerDetailsBusinessNumberList
                .slice(0, 10)
            )

          status(result) mustEqual OK
          expectedViewModel.addAnotherPartner mustBe true
          expectedViewModel.showMaximumPartnersMessage mustBe false
        }
      }
    }

    "onSubmit" - {

      "must redirect to the new partner business type page when Yes is submitted below the maximum" in {

        val mockSessionRepository =
          mock[SessionRepository]

        when(
          mockSessionRepository.set(
            any[UserAnswers]
          )
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
            route(
              application,
              request
            ).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            routes.PartnerDetailsBusinessTypeController
              .onPageLoad(
                "0",
                NormalMode
              )
              .url

          verify(
            mockSessionRepository,
            atLeastOnce()
          ).set(any[UserAnswers])
        }
      }

      "must increment the new partner index for each completed new partner" in {

        val mockSessionRepository =
          mock[SessionRepository]

        when(
          mockSessionRepository.set(
            any[UserAnswers]
          )
        ).thenReturn(
          Future.successful(true)
        )

        val answersWithNewPartnerZero =
          userAnswersWithPartner
            .set(
              PartnerDetailsMgdRegNumberPage(0),
              "0"
            )
            .success
            .value
            .set(
              PartnerDetailsAddPartnerCompletedPage(0),
              true
            )
            .success
            .value

        val answersWithNewPartnerOne =
          answersWithNewPartnerZero
            .set(
              PartnerDetailsMgdRegNumberPage(1),
              "1"
            )
            .success
            .value
            .set(
              PartnerDetailsAddPartnerCompletedPage(1),
              true
            )
            .success
            .value

        val applications =
          Seq(
            userAnswersWithPartner    -> "0",
            answersWithNewPartnerZero -> "1",
            answersWithNewPartnerOne  -> "2"
          ).map { case (answers, expectedIndex) =>
            applicationBuilder(
              userAnswers = Some(answers)
            )
              .overrides(
                bind[SessionRepository]
                  .toInstance(mockSessionRepository)
              )
              .build() -> expectedIndex
          }

        applications.foreach { case (application, expectedIndex) =>
          running(application) {

            val request =
              FakeRequest(
                POST,
                onSubmitRoute
              ).withFormUrlEncodedBody(
                "value" -> "true"
              )

            val result =
              route(
                application,
                request
              ).value

            status(result) mustEqual SEE_OTHER

            redirectLocation(result).value mustEqual
              routes.PartnerDetailsBusinessTypeController
                .onPageLoad(
                  expectedIndex,
                  NormalMode
                )
                .url
          }
        }
      }

      "must block a Yes submission when the maximum partner count has been reached" in {

        val mockSessionRepository =
          mock[SessionRepository]

        val application =
          applicationBuilder(
            userAnswers = Some(userAnswersWithPartnerMaxSize)
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
            route(
              application,
              request
            ).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            routes.PartnerDetailsController
              .onPageLoad(Some(1))
              .url

          /*
           * The controller blocks the request before saving the
           * answer or creating partner 101.
           */
          verify(
            mockSessionRepository,
            never()
          ).set(any[UserAnswers])
        }
      }

      "must allow a Yes submission when one of 100 partners has a future leaving date" in {

        val mockSessionRepository =
          mock[SessionRepository]

        when(
          mockSessionRepository.set(
            any[UserAnswers]
          )
        ).thenReturn(
          Future.successful(true)
        )

        val answers =
          userAnswersWithPartnerMaxSize
            .set(
              PartnerDetailsDateOfLeavingPage("1"),
              todayDate.plusDays(10)
            )
            .success
            .value

        val application =
          applicationBuilder(
            userAnswers = Some(answers)
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
            route(
              application,
              request
            ).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            routes.PartnerDetailsBusinessTypeController
              .onPageLoad(
                "0",
                NormalMode
              )
              .url

          verify(
            mockSessionRepository,
            atLeastOnce()
          ).set(any[UserAnswers])
        }
      }

      "must allow a Yes submission when one of 100 partners has a past leaving date" in {

        val mockSessionRepository =
          mock[SessionRepository]

        when(
          mockSessionRepository.set(
            any[UserAnswers]
          )
        ).thenReturn(
          Future.successful(true)
        )

        val answers =
          userAnswersWithPartnerMaxSize
            .set(
              PartnerDetailsDateOfLeavingPage("1"),
              todayDate.minusDays(10)
            )
            .success
            .value

        val application =
          applicationBuilder(
            userAnswers = Some(answers)
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
            route(
              application,
              request
            ).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            routes.PartnerDetailsBusinessTypeController
              .onPageLoad(
                "0",
                NormalMode
              )
              .url
        }
      }

      "must redirect to Change Registration Details when No is submitted" in {

        val mockSessionRepository =
          mock[SessionRepository]

        when(
          mockSessionRepository.set(
            any[UserAnswers]
          )
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
            route(
              application,
              request
            ).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            controllers.routes.ChangeRegistrationDetailsController
              .onPageLoad()
              .url
        }
      }

      "must allow No to be submitted when the maximum partner count has been reached" in {

        val mockSessionRepository =
          mock[SessionRepository]

        when(
          mockSessionRepository.set(
            any[UserAnswers]
          )
        ).thenReturn(
          Future.successful(true)
        )

        val application =
          applicationBuilder(
            userAnswers = Some(userAnswersWithPartnerMaxSize)
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
            route(
              application,
              request
            ).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            controllers.routes.ChangeRegistrationDetailsController
              .onPageLoad()
              .url

          verify(
            mockSessionRepository,
            atLeastOnce()
          ).set(any[UserAnswers])
        }
      }

      "must return Bad Request with errors when invalid data is submitted" in {

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
            route(
              application,
              request
            ).value

          val view =
            application.injector
              .instanceOf[PartnerDetailsView]

          status(result) mustEqual BAD_REQUEST

          contentAsString(result) mustEqual
            view(
              boundForm,
              viewModel(
                application             = application,
                userAnswers             = userAnswersWithPartner,
                paginatedPartnerNumbers = paginationHelper.partnerDetailsBusinessNumberList
              ),
              paginationHelper.paginatedViewModel,
              paginationHelper.page,
              paginationHelper.totalPages,
              paginationHelper.from,
              paginationHelper.to,
              paginationHelper.totalRecords
            )(
              request,
              messages(application)
            ).toString
        }
      }

      "must return Bad Request with the correct page when invalid data is submitted" in {

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
            route(
              application,
              request
            ).value

          val view =
            application.injector
              .instanceOf[PartnerDetailsView]

          val pageTwoPartnerNumbers =
            paginationHelperMaxSizePage2.partnerDetailsBusinessNumberList
              .slice(10, 20)

          status(result) mustEqual BAD_REQUEST

          contentAsString(result) mustEqual
            view(
              boundForm,
              viewModel(
                application             = application,
                userAnswers             = userAnswersWithPartnerMaxSize,
                paginatedPartnerNumbers = pageTwoPartnerNumbers
              ),
              paginationHelperMaxSizePage2.paginatedViewModel,
              paginationHelperMaxSizePage2.page,
              paginationHelperMaxSizePage2.totalPages,
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
            route(
              application,
              request
            ).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            routes.PartnerDetailsController
              .onPageLoad(None)
              .url
        }
      }
    }

    "onRemove" - {

      "must save the selected partner and redirect to the delete date page in CheckMode" in {

        val mockSessionRepository =
          mock[SessionRepository]

        when(
          mockSessionRepository.set(
            any[UserAnswers]
          )
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
            route(
              application,
              request
            ).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            routes.PartnerDetailsDeleteDateController
              .onPageLoad()
              .url

          val capturedAnswers =
            ArgumentCaptor.forClass(
              classOf[UserAnswers]
            )

          verify(
            mockSessionRepository
          ).set(
            capturedAnswers.capture()
          )

          capturedAnswers.getValue
            .get(
              PartnerDetailsChosenPartnerToRemovePage
            ) mustEqual Some(businessNumber1)
        }
      }
    }
  }

  private def viewModel(
    application: Application,
    userAnswers: UserAnswers,
    paginatedPartnerNumbers: Seq[BusinessNumberOrIndex]
  ): PartnerDetailsViewModel = {

    val frontendAppConfig =
      application.injector
        .instanceOf[config.FrontendAppConfig]

    implicit val appMessages =
      messages(application)

    val allPartnerNumbers =
      getAllPartnerNumbers(userAnswers)

    PartnerDetailsViewModel.from(
      paginatedPartnerNumbers = paginatedPartnerNumbers,
      allPartnerNumbers       = allPartnerNumbers,
      todayDate               = todayDate,
      userAnswers             = userAnswers,
      frontendAppConfig       = frontendAppConfig
    )
  }

  private def getAllPartnerNumbers(
    userAnswers: UserAnswers
  ): Seq[BusinessNumberOrIndex] = {

    val completedNewPartners: Seq[BusinessNumberOrIndex] =
      PartnerUtils.getCompletedNewPartners(
        userAnswers
      )

    val existingPartnerNumbers: Seq[BusinessNumberOrIndex] =
      PartnerUtils
        .getExistingPartnersBusinessNumbers(
          userAnswers
        )
        .filter { partnerNumber =>
          userAnswers
            .get(
              PartnerDetailsMgdRegNumberPage(
                partnerNumber
              )
            )
            .isDefined
        }
        .toSeq

    completedNewPartners ++
      existingPartnerNumbers
  }
}
