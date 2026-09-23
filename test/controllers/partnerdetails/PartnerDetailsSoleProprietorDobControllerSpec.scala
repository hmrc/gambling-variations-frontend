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
import forms.partnerdetails.PartnerSoleProprietorDobFormProvider
import models.{BusinessType, CheckMode, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.partnerdetails.{PartnerDetailsBusinessTypePage, PartnerDetailsDateOfBirthPage, PartnerDetailsMgdRegNumberPage}
import play.api.Application
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded, Call}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.partnerdetails.PartnerDetailsSoleProprietorDobView

import java.time.{Clock, LocalDate, ZoneOffset}
import scala.concurrent.Future

//TODO done?
class PartnerDetailsSoleProprietorDobControllerSpec extends SpecBase with MockitoSugar with PartnerDetailsHelper {

  private implicit val msgs: Messages =
    stubMessages()

  private val today: LocalDate =
    LocalDate.of(2026, 9, 5)

  private val clock: Clock =
    Clock.fixed(
      today.atStartOfDay(ZoneOffset.UTC).toInstant,
      ZoneOffset.UTC
    )

  private val validAnswer: LocalDate =
    today.minusYears(30)

  private val getRoute: String =
    controllers.partnerdetails.routes.PartnerDetailsSoleProprietorDobController
      .onPageLoad(businessNumber1, CheckMode)
      .url

  private val postRoute: String =
    controllers.partnerdetails.routes.PartnerDetailsSoleProprietorDobController
      .onSubmit(businessNumber1, CheckMode)
      .url

  override val emptyUserAnswers: UserAnswers =
    UserAnswers(userAnswersId)

  private val partnerDetailsUserAnswersExistingUsers: UserAnswers =
    emptyUserAnswers
      .set(
        PartnerDetailsMgdRegNumberPage(businessNumber1),
        userAnswersId
      )
      .success
      .value
      .set(
        PartnerDetailsBusinessTypePage(businessNumber1),
        BusinessType.Soleproprietor
      )
      .success
      .value

  private val partnerDetailsUserAnswersNewPartners: UserAnswers = userAnswersPartnerDetailsMinimalValidData
    .set(
      PartnerDetailsMgdRegNumberPage(newPartnersIndex1),
      userAnswersId
    )
    .success
    .value
    .set(
      PartnerDetailsBusinessTypePage(newPartnersIndex1),
      BusinessType.Soleproprietor
    )
    .success
    .value

  private def controller(
    application: Application
  ): PartnerDetailsSoleProprietorDobController =
    application.injector
      .instanceOf[PartnerDetailsSoleProprietorDobController]

  private def form(
    application: Application
  ) =
    application.injector
      .instanceOf[PartnerSoleProprietorDobFormProvider]
      .apply()

  private def view(
    application: Application
  ): PartnerDetailsSoleProprietorDobView =
    application.injector
      .instanceOf[PartnerDetailsSoleProprietorDobView]

  private def getRequest(): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, getRoute)

  private def postRequest(
    answer: LocalDate = validAnswer
  ): FakeRequest[AnyContentAsFormUrlEncoded] =
    FakeRequest(POST, postRoute)
      .withFormUrlEncodedBody(
        "value.day"   -> answer.getDayOfMonth.toString,
        "value.month" -> answer.getMonthValue.toString,
        "value.year"  -> answer.getYear.toString
      )

  "newPartners" - {
    "PartnerSoleProprietorDob Controller" - {

      "must return OK and the correct view for a GET for a sole proprietor" in {

        val application =
          applicationBuilder(
            userAnswers = Some(partnerDetailsUserAnswersNewPartners)
          )
            .overrides(
              bind[Clock].toInstance(clock)
            )
            .build()

        running(application) {

          val request =
            getRequest()

          val result =
            controller(application)
              .onPageLoad(newPartnersIndex1.toString, NormalMode)
              .apply(request)

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(application)(
              form(application),
              newPartnersIndex1.toString,
              NormalMode
            )(request, messages(application)).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val userAnswers =
          partnerDetailsUserAnswersNewPartners
            .set(
              PartnerDetailsDateOfBirthPage(newPartnersIndex1),
              validAnswer
            )
            .success
            .value

        val application =
          applicationBuilder(
            userAnswers = Some(userAnswers)
          )
            .overrides(
              bind[Clock].toInstance(clock)
            )
            .build()

        running(application) {

          val request =
            getRequest()

          val result =
            controller(application)
              .onPageLoad(newPartnersIndex1.toString, NormalMode)
              .apply(request)

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(application)(
              form(application).fill(validAnswer),
              newPartnersIndex1.toString,
              NormalMode
            )(request, messages(application)).toString
        }
      }

      "must redirect to the next page when valid data is submitted" in {

        val mockSessionRepository =
          mock[SessionRepository]

        when(mockSessionRepository.set(any[UserAnswers]))
          .thenReturn(Future.successful(true))

        val application =
          applicationBuilder(
            userAnswers = Some(partnerDetailsUserAnswersNewPartners)
          )
            .overrides(
              bind[Clock].toInstance(clock),
              bind[Navigator].toInstance(
                new FakeNavigator(onwardRoute)
              ),
              bind[SessionRepository].toInstance(
                mockSessionRepository
              )
            )
            .build()

        running(application) {

          val request =
            postRequest()

          val result =
            controller(application)
              .onSubmit(newPartnersIndex1.toString, NormalMode)
              .apply(request)

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            onwardRoute.url

          verify(mockSessionRepository)
            .set(any[UserAnswers])
        }
      }

      "must return a Bad Request and errors when an invalid date is submitted" in {

        val application =
          applicationBuilder(
            userAnswers = Some(partnerDetailsUserAnswersNewPartners)
          )
            .overrides(
              bind[Clock].toInstance(clock)
            )
            .build()

        running(application) {

          val request =
            FakeRequest(POST, postRoute)
              .withFormUrlEncodedBody(
                "value.day"   -> "31",
                "value.month" -> "2",
                "value.year"  -> "2020"
              )

          val boundForm =
            form(application)
              .bind(
                Map(
                  "value.day"   -> "31",
                  "value.month" -> "2",
                  "value.year"  -> "2020"
                )
              )

          val result =
            controller(application)
              .onSubmit(newPartnersIndex1.toString, NormalMode)
              .apply(request)

          status(result) mustEqual BAD_REQUEST

          boundForm.errors
            .map(_.message) must contain(
            "partnerSoleProprietorDob.error.invalid"
          )

          contentAsString(result) mustEqual
            view(application)(
              boundForm,
              newPartnersIndex1.toString,
              NormalMode
            )(request, messages(application)).toString
        }
      }

      "must accept any date in past" in {

        val earliestValidAnswer =
          today.minusYears(130)

        val mockSessionRepository =
          mock[SessionRepository]

        when(mockSessionRepository.set(any[UserAnswers]))
          .thenReturn(Future.successful(true))

        val application =
          applicationBuilder(
            userAnswers = Some(partnerDetailsUserAnswersNewPartners)
          )
            .overrides(
              bind[Clock].toInstance(clock),
              bind[Navigator].toInstance(
                new FakeNavigator(onwardRoute)
              ),
              bind[SessionRepository].toInstance(
                mockSessionRepository
              )
            )
            .build()

        running(application) {

          val request =
            postRequest(earliestValidAnswer)

          val result =
            controller(application)
              .onSubmit(newPartnersIndex1.toString, NormalMode)
              .apply(request)

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            onwardRoute.url
        }
      }

      "must return a Bad Request when today's date is submitted" in {

        val application =
          applicationBuilder(
            userAnswers = Some(partnerDetailsUserAnswersNewPartners)
          )
            .overrides(
              bind[Clock].toInstance(clock)
            )
            .build()

        running(application) {

          val request =
            postRequest(today)

          val boundForm =
            form(application)
              .bind(
                Map(
                  "value.day" ->
                    today.getDayOfMonth.toString,
                  "value.month" ->
                    today.getMonthValue.toString,
                  "value.year" ->
                    today.getYear.toString
                )
              )

          val result =
            controller(application)
              .onSubmit(newPartnersIndex1.toString, NormalMode)
              .apply(request)

          status(result) mustEqual BAD_REQUEST

          boundForm.errors
            .map(_.message) must contain(
            "partnerSoleProprietorDob.error.afterLatestDate"
          )

          contentAsString(result) mustEqual
            view(application)(
              boundForm,
              newPartnersIndex1.toString,
              NormalMode
            )(request, messages(application)).toString
        }
      }

      "must accept yesterday's date" in {

        val latestValidAnswer =
          today.minusDays(1)

        val mockSessionRepository =
          mock[SessionRepository]

        when(mockSessionRepository.set(any[UserAnswers]))
          .thenReturn(Future.successful(true))

        val application =
          applicationBuilder(
            userAnswers = Some(partnerDetailsUserAnswersNewPartners)
          )
            .overrides(
              bind[Clock].toInstance(clock),
              bind[Navigator].toInstance(
                new FakeNavigator(onwardRoute)
              ),
              bind[SessionRepository].toInstance(
                mockSessionRepository
              )
            )
            .build()

        running(application) {

          val request =
            postRequest(latestValidAnswer)

          val result =
            controller(application)
              .onSubmit(newPartnersIndex1.toString, NormalMode)
              .apply(request)

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            onwardRoute.url
        }
      }

      "must redirect to SystemError when the business type is not sole proprietor" in {

        val userAnswers =
          emptyUserAnswers
            .set(
              PartnerDetailsMgdRegNumberPage(newPartnersIndex1),
              userAnswersId
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
          )
            .overrides(
              bind[Clock].toInstance(clock)
            )
            .build()

        running(application) {

          val result =
            controller(application)
              .onPageLoad(newPartnersIndex1.toString, NormalMode)
              .apply(getRequest())

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            routes.SystemErrorController.onPageLoad().url
        }
      }

      "must redirect to SystemError when the business type is missing" in {

        val userAnswers =
          emptyUserAnswers
            .set(
              PartnerDetailsMgdRegNumberPage(newPartnersIndex1),
              userAnswersId
            )
            .success
            .value

        val application =
          applicationBuilder(
            userAnswers = Some(userAnswers)
          )
            .overrides(
              bind[Clock].toInstance(clock)
            )
            .build()

        running(application) {

          val result =
            controller(application)
              .onPageLoad(newPartnersIndex1.toString, NormalMode)
              .apply(getRequest())

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            routes.SystemErrorController.onPageLoad().url
        }
      }

      "must redirect to SystemError for a GET when no existing data can be loaded" in {

        val application =
          applicationBuilder(
            userAnswers = None
          )
            .overrides(
              bind[Clock].toInstance(clock)
            )
            .build()

        running(application) {

          val result =
            controller(application)
              .onPageLoad(newPartnersIndex1.toString, NormalMode)
              .apply(getRequest())

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            routes.SystemErrorController.onPageLoad().url
        }
      }

      "must redirect to SystemError for a POST when no existing data can be loaded" in {

        val application =
          applicationBuilder(
            userAnswers = None
          )
            .overrides(
              bind[Clock].toInstance(clock)
            )
            .build()

        running(application) {

          val result =
            controller(application)
              .onSubmit(newPartnersIndex1.toString, NormalMode)
              .apply(postRequest())

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            routes.SystemErrorController.onPageLoad().url
        }
      }
    }

  }

  "partners" - {

    "PartnerSoleProprietorDob Controller" - {

      "must return OK and the correct view for a GET for a sole proprietor" in {

        val application =
          applicationBuilder(
            userAnswers = Some(partnerDetailsUserAnswersExistingUsers)
          )
            .overrides(
              bind[Clock].toInstance(clock)
            )
            .build()

        running(application) {

          val request =
            getRequest()

          val result =
            controller(application)
              .onPageLoad(businessNumber1, CheckMode)
              .apply(request)

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(application)(
              form(application),
              businessNumber1,
              CheckMode
            )(request, messages(application)).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val userAnswers =
          partnerDetailsUserAnswersExistingUsers
            .set(
              PartnerDetailsDateOfBirthPage(businessNumber1),
              validAnswer
            )
            .success
            .value

        val application =
          applicationBuilder(
            userAnswers = Some(userAnswers)
          )
            .overrides(
              bind[Clock].toInstance(clock)
            )
            .build()

        running(application) {

          val request =
            getRequest()

          val result =
            controller(application)
              .onPageLoad(businessNumber1, CheckMode)
              .apply(request)

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(application)(
              form(application).fill(validAnswer),
              businessNumber1,
              CheckMode
            )(request, messages(application)).toString
        }
      }

      "must redirect to the next page when valid data is submitted" in {

        val mockSessionRepository =
          mock[SessionRepository]

        when(mockSessionRepository.set(any[UserAnswers]))
          .thenReturn(Future.successful(true))

        val application =
          applicationBuilder(
            userAnswers = Some(partnerDetailsUserAnswersExistingUsers)
          )
            .overrides(
              bind[Clock].toInstance(clock),
              bind[Navigator].toInstance(
                new FakeNavigator(onwardRoute)
              ),
              bind[SessionRepository].toInstance(
                mockSessionRepository
              )
            )
            .build()

        running(application) {

          val request =
            postRequest()

          val result =
            controller(application)
              .onSubmit(businessNumber1, CheckMode)
              .apply(request)

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            onwardRoute.url

          verify(mockSessionRepository)
            .set(any[UserAnswers])
        }
      }

      "must return a Bad Request and errors when an invalid date is submitted" in {

        val application =
          applicationBuilder(
            userAnswers = Some(partnerDetailsUserAnswersExistingUsers)
          )
            .overrides(
              bind[Clock].toInstance(clock)
            )
            .build()

        running(application) {

          val request =
            FakeRequest(POST, postRoute)
              .withFormUrlEncodedBody(
                "value.day"   -> "31",
                "value.month" -> "2",
                "value.year"  -> "2020"
              )

          val boundForm =
            form(application)
              .bind(
                Map(
                  "value.day"   -> "31",
                  "value.month" -> "2",
                  "value.year"  -> "2020"
                )
              )

          val result =
            controller(application)
              .onSubmit(businessNumber1, CheckMode)
              .apply(request)

          status(result) mustEqual BAD_REQUEST

          boundForm.errors
            .map(_.message) must contain(
            "partnerSoleProprietorDob.error.invalid"
          )

          contentAsString(result) mustEqual
            view(application)(
              boundForm,
              businessNumber1,
              CheckMode
            )(request, messages(application)).toString
        }
      }

      "must accept any date in past" in {

        val earliestValidAnswer =
          today.minusYears(130)

        val mockSessionRepository =
          mock[SessionRepository]

        when(mockSessionRepository.set(any[UserAnswers]))
          .thenReturn(Future.successful(true))

        val application =
          applicationBuilder(
            userAnswers = Some(partnerDetailsUserAnswersExistingUsers)
          )
            .overrides(
              bind[Clock].toInstance(clock),
              bind[Navigator].toInstance(
                new FakeNavigator(onwardRoute)
              ),
              bind[SessionRepository].toInstance(
                mockSessionRepository
              )
            )
            .build()

        running(application) {

          val request =
            postRequest(earliestValidAnswer)

          val result =
            controller(application)
              .onSubmit(businessNumber1, CheckMode)
              .apply(request)

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            onwardRoute.url
        }
      }

      "must return a Bad Request when today's date is submitted" in {

        val application =
          applicationBuilder(
            userAnswers = Some(partnerDetailsUserAnswersExistingUsers)
          )
            .overrides(
              bind[Clock].toInstance(clock)
            )
            .build()

        running(application) {

          val request =
            postRequest(today)

          val boundForm =
            form(application)
              .bind(
                Map(
                  "value.day" ->
                    today.getDayOfMonth.toString,
                  "value.month" ->
                    today.getMonthValue.toString,
                  "value.year" ->
                    today.getYear.toString
                )
              )

          val result =
            controller(application)
              .onSubmit(businessNumber1, CheckMode)
              .apply(request)

          status(result) mustEqual BAD_REQUEST

          boundForm.errors
            .map(_.message) must contain(
            "partnerSoleProprietorDob.error.afterLatestDate"
          )

          contentAsString(result) mustEqual
            view(application)(
              boundForm,
              businessNumber1,
              CheckMode
            )(request, messages(application)).toString
        }
      }

      "must accept yesterday's date" in {

        val latestValidAnswer =
          today.minusDays(1)

        val mockSessionRepository =
          mock[SessionRepository]

        when(mockSessionRepository.set(any[UserAnswers]))
          .thenReturn(Future.successful(true))

        val application =
          applicationBuilder(
            userAnswers = Some(partnerDetailsUserAnswersExistingUsers)
          )
            .overrides(
              bind[Clock].toInstance(clock),
              bind[Navigator].toInstance(
                new FakeNavigator(onwardRoute)
              ),
              bind[SessionRepository].toInstance(
                mockSessionRepository
              )
            )
            .build()

        running(application) {

          val request =
            postRequest(latestValidAnswer)

          val result =
            controller(application)
              .onSubmit(businessNumber1, CheckMode)
              .apply(request)

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            onwardRoute.url
        }
      }

      "must redirect to SystemError when the business type is not sole proprietor" in {

        val userAnswers =
          emptyUserAnswers
            .set(
              PartnerDetailsMgdRegNumberPage(businessNumber1),
              userAnswersId
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
          )
            .overrides(
              bind[Clock].toInstance(clock)
            )
            .build()

        running(application) {

          val result =
            controller(application)
              .onPageLoad(businessNumber1, CheckMode)
              .apply(getRequest())

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            routes.SystemErrorController.onPageLoad().url
        }
      }

      "must redirect to SystemError when the business type is missing" in {

        val userAnswers =
          emptyUserAnswers
            .set(
              PartnerDetailsMgdRegNumberPage(businessNumber1),
              userAnswersId
            )
            .success
            .value

        val application =
          applicationBuilder(
            userAnswers = Some(userAnswers)
          )
            .overrides(
              bind[Clock].toInstance(clock)
            )
            .build()

        running(application) {

          val result =
            controller(application)
              .onPageLoad(businessNumber1, CheckMode)
              .apply(getRequest())

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            routes.SystemErrorController.onPageLoad().url
        }
      }

      "must redirect to SystemError for a GET when no existing data can be loaded" in {

        val application =
          applicationBuilder(
            userAnswers = None
          )
            .overrides(
              bind[Clock].toInstance(clock)
            )
            .build()

        running(application) {

          val result =
            controller(application)
              .onPageLoad(businessNumber1, CheckMode)
              .apply(getRequest())

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            routes.SystemErrorController.onPageLoad().url
        }
      }

      "must redirect to SystemError for a POST when no existing data can be loaded" in {

        val application =
          applicationBuilder(
            userAnswers = None
          )
            .overrides(
              bind[Clock].toInstance(clock)
            )
            .build()

        running(application) {

          val result =
            controller(application)
              .onSubmit(businessNumber1, CheckMode)
              .apply(postRequest())

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual
            routes.SystemErrorController.onPageLoad().url
        }
      }
    }

  }

}
