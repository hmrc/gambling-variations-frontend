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

import play.api.i18n.{Messages, MessagesApi}
import play.api.libs.json.Json
import base.SpecBase
import models.UserAnswers
import org.scalatest.matchers.must.Matchers
import pages.PreviousRegistrationNumbersPage
import play.api.test.FakeRequest
import uk.gov.hmrc.govukfrontend.views.Aliases.Text

class PreviousRegNumberViewModelSpec extends SpecBase with Matchers {

  "PreviousRegNumberViewModel" - {

    "must populate correct view" in {
      val data = Json.obj(
        "mgdTradeDetailsSection" -> Json.obj("mgdRegNum" -> mgdRegNum),
        "previousRegistrationNumbers" -> Json.arr(
          "XHM00000199",
          "ZIU00001218"
        ),
        "unsubmittedPreviousRegNumbers" -> Json.arr(
          "GTT28881666"
        )
      )
      val baseUserAnswers =
        UserAnswers(userAnswersId, data)

      val previousRegNumbers = baseUserAnswers.get(PreviousRegistrationNumbersPage)

      val application = applicationBuilder(userAnswers = Some(baseUserAnswers)).build()

      val messagesApi = application.injector.instanceOf[MessagesApi]

      implicit val messages: Messages = messagesApi.preferred(FakeRequest())

      val submitted = PreviousRegNumberViewModel(previousRegNumbers).summaryList

      submitted.head.key.content mustEqual Text("XHM00000199")
      submitted(1).key.content mustEqual Text("ZIU00001218")
    }
  }
}
