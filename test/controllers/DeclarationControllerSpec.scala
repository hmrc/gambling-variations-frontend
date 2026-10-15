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
import connectors.GamblingConnector
import forms.DeclarationSubmittedFormProvider
import models.{BusinessDetails, BusinessType}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import pages.{BusinessTypePage, DeclarationPage, GroupMemberPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import views.html.DeclarationView

import java.time.LocalDate
import scala.concurrent.Future

class DeclarationControllerSpec extends SpecBase {

  "Declaration Controller" - {

    "must return OK and the correct view for a GET when isGroupMember is true" in {

      val userAnswers = emptyUserAnswers
        .set(GroupMemberPage, true)
        .success
        .value
        .set(BusinessTypePage, BusinessType.Partnership)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
      val formProvider = new DeclarationSubmittedFormProvider()
      val form = formProvider()

      running(application) {
        val request = FakeRequest(GET, routes.DeclarationController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[DeclarationView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, true)(request, messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET when isGroupMember is false" in {
      val userAnswers = emptyUserAnswers
        .set(GroupMemberPage, false)
        .success
        .value
        .set(BusinessTypePage, BusinessType.Soleproprietor)
        .success
        .value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
      val formProvider = new DeclarationSubmittedFormProvider()
      val form = formProvider()

      running(application) {
        val request = FakeRequest(GET, routes.DeclarationController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[DeclarationView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, false)(request, messages(application)).toString
      }
    }

    "must redirect view for a GET when isGroupMember is None" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.DeclarationController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
      }
    }

  }
}
