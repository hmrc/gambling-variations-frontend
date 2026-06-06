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

package controllers.actions

import base.SpecBase
import connectors.GamblingConnector
import models.BusinessType.{Partnership, Soleproprietor}
import models.requests.{DataRequest, OptionalDataRequest}
import models.{BusinessContactDetails, BusinessNameDetails, BusinessTradeClass, MgdTradeDetails, SoleProprietorNameDetails, UserAnswers}
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.libs.json.Json
import play.api.mvc.Results.*
import play.api.mvc.{AnyContent, Result}
import play.api.test.FakeRequest
import repositories.SessionRepository
import uk.gov.hmrc.http.UpstreamErrorResponse

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DataRequiredActionSpec extends SpecBase with MockitoSugar {

  class Harness() extends DataRequiredActionImpl() {
    def callRefine[A](request: OptionalDataRequest[A]): Future[Either[Result, DataRequest[A]]] = refine(request)
  }

  "Data Required Action" - {

    "when there is no User Answers in the cache" - {

      "return the request with a populated User Answers" in {

        val request = FakeRequest()
        val action = new Harness()

        val result: Either[Result, DataRequest[AnyContent]] =
          action.callRefine(OptionalDataRequest(request, mgdRegNum, None)).futureValue

        val expected = DataRequest(request, mgdRegNum, UserAnswers(mgdRegNum, Json.obj()))

        result.map { req =>
          req.request mustBe expected.request
          req.userAnswers.data mustBe expected.userAnswers.data
          req.userAnswers.id mustBe expected.userAnswers.id
        }
      }

    }

    "when there are User Answers in the cache" - {

      "return the request with a populated User Answers without call to backend" in {

        val request = FakeRequest()
        val action = new Harness()

        val data = Json.obj(
          "businessName" -> "Test Business Ltd",
          "tradingName"  -> "Test Trader Ltd"
        )

        val result: Either[Result, DataRequest[AnyContent]] =
          action.callRefine(OptionalDataRequest(request, mgdRegNum, Some(UserAnswers(mgdRegNum, data)))).futureValue

        val expected = DataRequest(request, mgdRegNum, UserAnswers(mgdRegNum, data))

        result.map { req =>
          req.request mustBe expected.request
          req.userAnswers.data mustBe expected.userAnswers.data
          req.userAnswers.id mustBe expected.userAnswers.id
        }

      }

    }

  }
}
