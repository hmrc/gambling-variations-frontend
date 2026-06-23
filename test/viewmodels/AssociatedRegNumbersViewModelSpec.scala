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

package viewmodels

import controllers.routes
import play.api.i18n.{Messages, MessagesApi}
import play.api.libs.json.Json
import base.SpecBase
import models.UserAnswers
import org.scalatest.matchers.must.Matchers
import pages.AssociatedRegistrationNumbersPage
import play.api.test.FakeRequest
import uk.gov.hmrc.govukfrontend.views.Aliases.Text

class AssociatedRegNumbersViewModelSpec extends SpecBase with Matchers {

  "AssociatedRegNumberViewModel" - {

    "must populate correct view" in {
      val data = Json.obj(
        "mgdTradeDetailsSection" -> Json.obj("mgdRegNum" -> mgdRegNum),
        "associatedRegistrationNumbers" -> Json.arr(
          "XHM00000199",
          "ZIU00001218",
          "GTT28881666"
        )
      )
      val baseUserAnswers =
        UserAnswers(userAnswersId, data)

      val associatedRegNumbers = baseUserAnswers.get(AssociatedRegistrationNumbersPage)

      val application = applicationBuilder(userAnswers = Some(baseUserAnswers)).build()

      val messagesApi = application.injector.instanceOf[MessagesApi]

      implicit val messages: Messages = messagesApi.preferred(FakeRequest())

      val result = AssociatedRegNumbersViewModel(associatedRegNumbers).summaryList

      result.head.key.content mustEqual Text("XHM00000199")
      result(1).key.content mustEqual Text("ZIU00001218")
      result(2).key.content mustEqual Text("GTT28881666")
      result.head.actions.get.items(1).href mustEqual routes.AssociatedRegistrationNumbersController.onRedirect(assocRegNumber = "XHM00000199").url
    }
  }
}
