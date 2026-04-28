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
import models.requests.{DataRequest, OptionalDataRequest}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.Result
import play.api.test.FakeRequest
import repositories.SessionRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DataRequiredActionSpec extends SpecBase with MockitoSugar {

  class Harness(sessionRepository: SessionRepository) extends DataRequiredActionImpl(sessionRepository) {
    def callRefine[A](request: OptionalDataRequest[A]): Future[Either[Result, DataRequest[A]]] = refine(request)
  }

  val mgdRegNum = "GAM0000000001"

  "Data Required Action" - {

    "when there is no data in the cache" - {

      "must populate sessionRepository" in {

        val sessionRepository = mock[SessionRepository]
        when(sessionRepository.set(any())) thenReturn Future(true)
        val action = new Harness(sessionRepository)

        val result = action.callRefine(OptionalDataRequest(FakeRequest(), mgdRegNum, None)).futureValue

        result.isRight mustBe true
        verify(sessionRepository, times(1)).set(any())
      }
    }
  }
}
