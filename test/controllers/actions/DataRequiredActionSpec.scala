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
import models.MgdCertificate
import models.requests.{DataRequest, OptionalDataRequest}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.Result
import play.api.test.FakeRequest
import repositories.SessionRepository

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DataRequiredActionSpec extends SpecBase with MockitoSugar {

  class Harness(sessionRepository: SessionRepository, gamblingConnector: GamblingConnector)
      extends DataRequiredActionImpl(sessionRepository, gamblingConnector) {
    def callRefine[A](request: OptionalDataRequest[A]): Future[Either[Result, DataRequest[A]]] = refine(request)
  }

  val mgdRegNum = "GAM0000000001"

  "Data Required Action" - {

    "when there is no data in the cache" - {

      "must populate sessionRepository" in {

        val sessionRepository = mock[SessionRepository]
        val gamblingConnector = mock[GamblingConnector]
        when(sessionRepository.set(any())) thenReturn Future(true)
        when(gamblingConnector.getCertificate(any())(any())) thenReturn Future(certificate)
        val action = new Harness(sessionRepository, gamblingConnector)

        val result = action.callRefine(OptionalDataRequest(FakeRequest(), mgdRegNum, None)).futureValue

        result.isRight mustBe true
        verify(sessionRepository, times(1)).set(any())
      }
    }

    def certificate: MgdCertificate =
      MgdCertificate(
        mgdRegNumber         = "MGD123",
        registrationDate     = Some(LocalDate.parse("2026-01-01")),
        individualName       = Some("John Doe"),
        businessName         = Some("Test Business Ltd"),
        tradingName          = None,
        repMemName           = None,
        busAddrLine1         = Some("Line 1"),
        busAddrLine2         = Some("Line 2"),
        busAddrLine3         = None,
        busAddrLine4         = None,
        busPostcode          = Some("AB1 2CD"),
        busCountry           = None,
        busAdi               = None,
        repMemLine1          = None,
        repMemLine2          = None,
        repMemLine3          = None,
        repMemLine4          = None,
        repMemPostcode       = None,
        repMemAdi            = None,
        typeOfBusiness       = Some("Corporate Body"), // important: matches controller
        businessTradeClass   = Some(1),
        noOfPartners         = None,
        groupReg             = "N",
        noOfGroupMems        = None,
        dateCertIssued       = Some(LocalDate.parse("2026-01-02")),
        partMembers          = Seq.empty,
        groupMembers         = Seq.empty,
        returnPeriodEndDates = Seq.empty
      )
  }
}
