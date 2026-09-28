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

package controllers.licencespremises

import base.SpecBase
import models.UserAnswers
import models.licencespremises.LicencesAndPremisesRadioOptions.ByPost
import pages.licencespremises.*
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import viewmodels.checkAnswers.licencespremises.CheckLicencesAndPremisesViewModel
import views.html.licencespremises.CheckLicenceAndPremisesView

class CheckLicencesAndPremisesControllerSpec extends SpecBase {

  private lazy val checkLicencesAndPremisesRoute = routes.CheckLicencesAndPremisesController.onPageLoad().url

  private val userAnswers: UserAnswers =
    emptyUserAnswers
      .set(LicencesPremisesSectionPage, mgdRegNum)
      .success
      .value
      .set(LicenceNumberPage, "123-456789-A-123456-789")
      .success
      .value
      .set(LicencePremisesNotCoveredPage, "1")
      .success
      .value
      .set(LicencesPremisesPage, ByPost)
      .success
      .value

  "CheckLicencesAndPremises Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, checkLicencesAndPremisesRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckLicenceAndPremisesView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(CheckLicencesAndPremisesViewModel.from(userAnswers))(request, messages(application)).toString
      }
    }
  }
}
