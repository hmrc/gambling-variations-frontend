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
import models.UserAnswers
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import viewmodels.checkAnswers.partner.CheckPartnerDetailsViewModel
import views.html.partner.PartnerDetailsCheckYourAnswersView

class PartnerDetailsCheckYourAnswersControllerSpec extends SpecBase with PartnerDetailsHelper {

  "PartnerDetailsCheckYourAnswers Controller" - {

    // TODO: Interim solutions

    // index -> will be refactored with the indexing ticket
    // is new partner -> to use flag!
    val isNewPartner: Option[Boolean] = Some(true)
    // is submitted -> to use flag!
    val isSubmitted: Boolean = true

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val validUserAnswers: UserAnswers =
        UserAnswers(mgdRegNumber, cleanedData())

      implicit val msg: Messages = messages(application)
      val model = CheckPartnerDetailsViewModel.from(validUserAnswers, index, isNewPartner, isSubmitted)

      running(application) {
        val request = FakeRequest(GET, routes.PartnerDetailsCheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PartnerDetailsCheckYourAnswersView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(model)(request, messages(application)).toString
      }
    }
  }
}
