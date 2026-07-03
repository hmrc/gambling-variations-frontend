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

package viewmodels.checkAnswers.tradingdetails

import base.SpecBase
import controllers.routes
import models.UserAnswers
import org.scalatest.matchers.must.Matchers
import pages.UnsubmittedPreviousRegNumbersPage
import play.api.i18n.{Messages, MessagesApi}
import play.api.libs.json.Json
import play.api.test.FakeRequest
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import viewmodels.checkAnswers.tradingdetails.UnsubmittedPrevRegNumbersViewModel

class UnsubmittedPrevRegNumbersViewModelSpec extends SpecBase with Matchers {

  "UnsubmittedRegNumberViewModel" - {

    "must populate correct view" in {
      val data = Json.obj(
        "mgdTradeDetailsSection" -> Json.obj(
          "mgdRegNum" -> mgdRegNum,
          "previousRegNumbersSection" -> Json.obj(
            "previousRegistrationNumbers" -> Json.arr(
              "XHM00000199",
              "ZIU00001218"
            ),
            "unsubmittedPreviousRegNumbers" -> Json.arr(
              "GTT28881666"
            ),
            "updated" -> true
          )
        )
      )
      val baseUserAnswers =
        UserAnswers(userAnswersId, data)

      val unsubmittedPreviousRegNumbers = baseUserAnswers.get(UnsubmittedPreviousRegNumbersPage)

      val application = applicationBuilder(userAnswers = Some(baseUserAnswers)).build()

      val messagesApi = application.injector.instanceOf[MessagesApi]

      implicit val messages: Messages = messagesApi.preferred(FakeRequest())

      val unsubmitted = UnsubmittedPrevRegNumbersViewModel(unsubmittedPreviousRegNumbers).summaryList

      unsubmitted.head.key.content mustEqual Text("GTT28881666")
      unsubmitted.head.actions.get.items(1).href mustEqual routes.PreviousRegistrationNumbersListController
        .onRedirect(prevRegNumber = "GTT28881666")
        .url
    }
  }
}
