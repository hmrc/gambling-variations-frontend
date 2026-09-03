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
import controllers.partner.PartnerUtils.getIndex
import forms.{ChangeBusinessNameFormProvider, SoleProprietorNameFormProvider}
import models.BusinessType.Partnership
import models.{BusinessType, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.partner.PartnerDetailsAddPartnerCompletedPage
import pages.partnerdetails.PartnerDetailsBusinessNamePage
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import views.html.{ChangePartnerDetailsBusinessNameView, ChangePartnerDetailsSoleProprietorNameView}

import scala.concurrent.Future

class ChangePartnerDetailsBusinessNameControllerSpec extends SpecBase with MockitoSugar {

  trait Setup(val businessType: BusinessType) {

    def onwardRoute: Call = Call("GET", "/foo")

    val formProvider = new ChangeBusinessNameFormProvider()
    val businessName = "Test Business"
    val form = formProvider(businessType)

    val businessData = Json.obj(
      "partners" -> Json.arr(
        Json.obj(
          "partnerDetailsMgdRegNumber" -> mgdRegNum,
          "partnerDetailsBusinessType" -> businessType.code,
          "partnerDetailsBusinessName" -> businessName
        )
      )
    )

    val userAnswers: UserAnswers =
      UserAnswers("id", businessData)
        .set(PartnerDetailsAddPartnerCompletedPage, false)
        .success
        .value

    val index: Int = userAnswers.getIndex

    val soleProprietorData = Json.obj(
      "partners" -> Json.arr(
        Json.obj(
          "partnerDetailsMgdRegNumber" -> mgdRegNum,
          "partnerDetailsBusinessType" -> businessType.code,
          "partnerDetailsSoleProprietor" -> Json.obj(
            "title"      -> "Mr",
            "firstName"  -> "Tom",
            "middleName" -> "Bob",
            "lastName"   -> "Smith"
          )
        )
      )
    )

    val soleProprietorUserAnswers: UserAnswers =
      UserAnswers("id", soleProprietorData)
        .set(PartnerDetailsAddPartnerCompletedPage, false)
        .success
        .value

    val noAnswers = UserAnswers(
      userAnswersId,
      Json.obj(
        "businessNameSection" -> Json.obj("mgdRegNum" -> mgdRegNum)
      )
    )

    lazy val changePartnerDetailsBusinessNameRoute =
      controllers.partner.routes.ChangePartnerDetailsBusinessNameController
        .onPageLoad(Partnership)
        .url
  }

  "ChangePartnerDetailsBusinessName Controller" - {

    "must return OK and the correct view for a GET" in new Setup(BusinessType.Partnership) {

      val application =
        applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, changePartnerDetailsBusinessNameRoute)

        val result = route(application, request).value

        val view =
          application.injector.instanceOf[ChangePartnerDetailsBusinessNameView]

        val headingKey = "changeBusinessName.heading.partnership"
        val titleKey = "changeBusinessName.title.partnership"

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(
            form.fill(businessName),
            NormalMode,
            Partnership,
            headingKey,
            titleKey
          )(request, messages(application)).toString
      }
    }

    "must return OK and sole proprietor view for a GET" in new Setup(BusinessType.Soleproprietor) {

      val application =
        applicationBuilder(userAnswers = Some(soleProprietorUserAnswers)).build()

      running(application) {

        val request =
          FakeRequest(
            GET,
            controllers.partner.routes.ChangePartnerDetailsBusinessNameController
              .onPageLoad(BusinessType.Soleproprietor)
              .url
          )

        val result = route(application, request).value

        status(result) mustEqual OK
      }
    }

    "must redirect" - {

      "to System Error when no Business Name or Business Type exists" - {

        "when GET" in new Setup(BusinessType.Partnership) {

          val application =
            applicationBuilder(userAnswers = Some(noAnswers)).build()

          running(application) {
            val request =
              FakeRequest(GET, changePartnerDetailsBusinessNameRoute)

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual
              routes.SystemErrorController.onPageLoad().url
          }
        }

        "when POST" in new Setup(BusinessType.Partnership) {

          val application =
            applicationBuilder(userAnswers = Some(noAnswers)).build()

          running(application) {
            val request =
              FakeRequest(POST, changePartnerDetailsBusinessNameRoute)
                .withFormUrlEncodedBody(("value", "New Name"))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual
              routes.SystemErrorController.onPageLoad().url
          }
        }
      }

      "to the next page when valid data is submitted" in new Setup(BusinessType.Partnership) {

        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, changePartnerDetailsBusinessNameRoute)
              .withFormUrlEncodedBody(("value", "Updated Business Name"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }
    }

    "must redirect to next page when valid sole proprietor data is submitted" in
      new Setup(BusinessType.Soleproprietor) {

        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(soleProprietorUserAnswers))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(
              POST,
              controllers.partner.routes.ChangePartnerDetailsBusinessNameController
                .onSubmit(BusinessType.Soleproprietor)
                .url
            )
              .withFormUrlEncodedBody(
                "title"      -> "Mr",
                "firstName"  -> "John",
                "middleName" -> "Bob",
                "lastName"   -> "Smith"
              )

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url
        }
      }

    "must return bad request when invalid sole proprietor data is submitted" in
      new Setup(BusinessType.Soleproprietor) {

        val application =
          applicationBuilder(userAnswers = Some(soleProprietorUserAnswers)).build()

        running(application) {

          val request =
            FakeRequest(
              POST,
              controllers.partner.routes.ChangePartnerDetailsBusinessNameController
                .onSubmit(BusinessType.Soleproprietor)
                .url
            )
              .withFormUrlEncodedBody(
                "title"      -> "",
                "firstName"  -> "",
                "middleName" -> "",
                "lastName"   -> ""
              )

          val result = route(application, request).value

          val view =
            application.injector.instanceOf[ChangePartnerDetailsSoleProprietorNameView]

          val boundForm =
            new SoleProprietorNameFormProvider()()
              .bind(
                Map(
                  "title"      -> "",
                  "firstName"  -> "",
                  "middleName" -> "",
                  "lastName"   -> ""
                )
              )

          status(result) mustEqual BAD_REQUEST

          contentAsString(result) mustEqual
            view(
              boundForm,
              NormalMode
            )(request, messages(application)).toString
        }
      }

    "must update data correctly when submitted in" in
      new Setup(BusinessType.Partnership) {

        val mockSessionRepository = mock[SessionRepository]
        val savedAnswersCaptor =
          ArgumentCaptor.forClass(classOf[UserAnswers])

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(
              bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
              bind[SessionRepository].toInstance(mockSessionRepository)
            )
            .build()

        running(application) {
          val request =
            FakeRequest(POST, changePartnerDetailsBusinessNameRoute)
              .withFormUrlEncodedBody(("value", "Updated Business Name"))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          verify(mockSessionRepository).set(savedAnswersCaptor.capture())

          savedAnswersCaptor.getValue
            .get(PartnerDetailsBusinessNamePage(index))
            .value mustEqual "Updated Business Name"
        }
      }

    "must return a Bad Request and errors when invalid data is submitted" in
      new Setup(BusinessType.Partnership) {

        val application =
          applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          val request =
            FakeRequest(POST, changePartnerDetailsBusinessNameRoute)
              .withFormUrlEncodedBody(("value", ""))

          val boundForm = form.bind(Map("value" -> ""))

          val view =
            application.injector.instanceOf[ChangePartnerDetailsBusinessNameView]

          val result = route(application, request).value

          val headingKey = "changeBusinessName.heading.partnership"
          val titleKey = "changeBusinessName.title.partnership"

          status(result) mustEqual BAD_REQUEST

          contentAsString(result) mustEqual
            view(
              boundForm,
              NormalMode,
              Partnership,
              headingKey,
              titleKey
            )(request, messages(application)).toString
        }
      }
  }
}
