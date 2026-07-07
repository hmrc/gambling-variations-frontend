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

package utils

import base.SpecBase
import models.UserAnswers
import models.requests.DataRequest
import pages.QuestionPage
import play.api.libs.json.{JsPath, Json, OFormat, Reads}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.*

import scala.concurrent.ExecutionContext

class FlagsUtilSpec extends SpecBase {
  case class TestAnswer(value: String)

  object TestAnswer {
    implicit val format: OFormat[TestAnswer] = Json.format[TestAnswer]
  }

  case object TestRefPage extends QuestionPage[TestAnswer] {
    override def path: JsPath = JsPath \ "testRefPage"
  }

  case object TestSubmittedOnlyPage extends QuestionPage[Boolean] {
    override def path: JsPath = JsPath \ "testSubmittedOnlyPage"
  }

  case object TestChangesPage extends QuestionPage[Boolean] {
    override def path: JsPath = JsPath \ "testChangesPage"
  }

  private def dataConstructor(ref: TestAnswer, subm: Boolean, change: Boolean): UserAnswers = {
    UserAnswers(userAnswersId,
                Json.obj(
                  "testRefPage"           -> ref,
                  "testSubmittedOnlyPage" -> subm,
                  "testChangesPage"       -> change
                )
               )
  }

  "FlagsUtil" - {
    "checkFlag" - {
      "must return true for submittedOnly but not changed data" in {
        val userAnswers = dataConstructor(TestAnswer("foobar"), subm = true, change = false)
        FlagsUtil.checkFlag(userAnswers, TestChangesPage, TestSubmittedOnlyPage) mustEqual true

      }

      "must return true for changed data without a submittedOnly flag" in {
        val userAnswers = dataConstructor(TestAnswer("foobar"), subm = false, change = true)

        FlagsUtil.checkFlag(userAnswers, TestChangesPage, TestSubmittedOnlyPage) mustEqual true

      }

      "must return false for two false flags" in {
        val userAnswers = dataConstructor(TestAnswer("foobar"), subm = false, change = false)

        FlagsUtil.checkFlag(userAnswers, TestChangesPage, TestSubmittedOnlyPage) mustEqual false

      }
    }

    "checkIfChanged" - {

      "must return false when data hasn't changed" in {
        val ua = dataConstructor(TestAnswer("foobar"), subm = false, change = false)
        val value = TestAnswer("foobar")
        value mustEqual ua.get(TestRefPage).getOrElse(None)
        val application = applicationBuilder(userAnswers = Some(ua)).build()

        running(application) {
          implicit val request: DataRequest[AnyContentAsEmpty.type] = DataRequest(FakeRequest(), userAnswersId, ua)
          implicit val ec: ExecutionContext = application.injector.instanceOf[ExecutionContext]
          FlagsUtil.checkIfChanged(value, ua, TestRefPage, TestChangesPage) mustEqual false
        }
      }

      "must return true when change was made earlier in user journey" in {
        val userAnswers = dataConstructor(TestAnswer("foobar"), subm = false, change = true)
        val value = TestAnswer("foobar")
        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          implicit val request: DataRequest[AnyContentAsEmpty.type] = DataRequest(FakeRequest(), userAnswersId, userAnswers)
          implicit val ec: ExecutionContext = application.injector.instanceOf[ExecutionContext]

          FlagsUtil.checkIfChanged(value, userAnswers, TestRefPage, TestChangesPage) mustEqual true
        }
      }

      "must return true when new data submitted and both flags false" in {
        val userAnswers = dataConstructor(TestAnswer("foobar"), subm = false, change = false)
        val value = Some(TestAnswer("newbar"))
        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          implicit val request: DataRequest[AnyContentAsEmpty.type] = DataRequest(FakeRequest(), userAnswersId, userAnswers)
          implicit val ec: ExecutionContext = application.injector.instanceOf[ExecutionContext]

          FlagsUtil.checkIfChanged(value, userAnswers, TestRefPage, TestChangesPage) mustEqual true
        }
      }

      "must return true when new data submitted and both flags true" in {
        val userAnswers = dataConstructor(TestAnswer("foobar"), subm = true, change = true)
        val value = TestAnswer("newbar")
        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          implicit val request: DataRequest[AnyContentAsEmpty.type] = DataRequest(FakeRequest(), userAnswersId, userAnswers)
          implicit val ec: ExecutionContext = application.injector.instanceOf[ExecutionContext]

          FlagsUtil.checkIfChanged(value, userAnswers, TestRefPage, TestChangesPage) mustEqual true
        }
      }
    }
  }
}
