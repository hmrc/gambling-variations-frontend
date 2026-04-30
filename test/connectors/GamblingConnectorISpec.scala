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

package connectors

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import models.{MgdCertificate, MgdCertificateError}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsArray, Json}
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}

import java.time.LocalDate
import scala.concurrent.ExecutionContext

class GamblingConnectorISpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

  given ExecutionContext = ExecutionContext.global
  given HeaderCarrier = HeaderCarrier()

  private val wireMockServer = new WireMockServer(0)

  override def beforeAll(): Unit = {
    wireMockServer.start()
    configureFor("localhost", wireMockServer.port())
  }

  override def afterAll(): Unit =
    wireMockServer.stop()

  private lazy val app =
    new GuiceApplicationBuilder()
      .configure(
        "microservice.services.gambling.protocol" -> "http",
        "microservice.services.gambling.host"     -> "localhost",
        "microservice.services.gambling.port"     -> wireMockServer.port()
      )
      .build()

  private lazy val connector =
    app.injector.instanceOf[GamblingConnector]

  private val mgdRegNumber = "XWM00000001762"

  "GamblingConnector.getCertificate" should {

    "return certificate when backend returns 200" in {

      val responseJson =
        Json.obj(
          "mgdRegNumber"         -> mgdRegNumber,
          "groupReg"             -> "N",
          "businessName"         -> "Test Business Ltd",
          "tradingName"          -> "Test Trader Ltd",
          "groupMembers"         -> JsArray(),
          "partMembers"          -> JsArray(),
          "returnPeriodEndDates" -> JsArray()
        )

      wireMockServer.stubFor(
        get(urlEqualTo(s"/gambling/certificate/mgd/$mgdRegNumber"))
          .willReturn(okJson(responseJson.toString()))
      )

      connector.getCertificate(mgdRegNumber).futureValue mustBe certificate
    }

    "return NotFound when backend returns UpstreamErrorResponse" in {

      wireMockServer.stubFor(
        get(urlEqualTo(s"/gambling/certificate/mgd/$mgdRegNumber"))
          .willReturn(aResponse().withStatus(404))
      )

      recoverToSucceededIf[UpstreamErrorResponse] {
        connector.getCertificate(mgdRegNumber)
      }
    }

    "return Left(UnexpectedError) when backend returns 500" in {

      wireMockServer.stubFor(
        get(urlEqualTo(s"/gambling/certificate/mgd/$mgdRegNumber"))
          .willReturn(serverError())
      )

      recoverToSucceededIf[UpstreamErrorResponse] {
        connector.getCertificate(mgdRegNumber)
      }
    }

    def certificate: MgdCertificate =
      MgdCertificate(
        mgdRegNumber         = mgdRegNumber,
        registrationDate     = None,
        individualName       = None,
        businessName         = Some("Test Business Ltd"),
        tradingName          = Some("Test Trader Ltd"),
        repMemName           = None,
        busAddrLine1         = None,
        busAddrLine2         = None,
        busAddrLine3         = None,
        busAddrLine4         = None,
        busPostcode          = None,
        busCountry           = None,
        busAdi               = None,
        repMemLine1          = None,
        repMemLine2          = None,
        repMemLine3          = None,
        repMemLine4          = None,
        repMemPostcode       = None,
        repMemAdi            = None,
        typeOfBusiness       = None, // important: matches controller
        businessTradeClass   = None,
        noOfPartners         = None,
        groupReg             = "N",
        noOfGroupMems        = None,
        dateCertIssued       = None,
        partMembers          = Seq.empty,
        groupMembers         = Seq.empty,
        returnPeriodEndDates = Seq.empty
      )
  }
}
