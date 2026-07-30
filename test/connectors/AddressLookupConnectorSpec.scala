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
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.*
import models.Address
import org.scalactic.Prettifier.default
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import play.api.http.HeaderNames
import play.api.http.Status.*
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class AddressLookupConnectorSpec extends AsyncWordSpec with Matchers with BeforeAndAfterAll {

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
        "microservice.services.address-lookup-frontend.protocol" -> "http",
        "microservice.services.address-lookup-frontend.host"     -> "localhost",
        "microservice.services.address-lookup-frontend.port"     -> wireMockServer.port()
      )
      .build()

  private lazy val connector =
    app.injector.instanceOf[AddressLookupConnector]

  val address = Address(
    "add1",
    Some("add2"),
    None,
    None,
    None,
    Some("UK")
  )

  "AddressLookupConnector" should {

    ".initJourney() method" should {

      "for a successful response" must {
        "return a Location for callback" in {

          stubFor(
            post(urlEqualTo("/api/init"))
              .withRequestBody(
                equalToJson(
                  Json.obj().toString()
                )
              )
              .willReturn(
                aResponse()
                  .withHeader(HeaderNames.LOCATION, "/foo")
                  .withStatus(ACCEPTED)
              )
          )

          val expectedResult = "/foo"
          val actualResult =
            connector.initJourney().futureValue

          actualResult mustBe expectedResult

        }
      }

      "for an error response" must {

        "return a Left(Invalid) when no location returns" in {

          stubFor(
            post(urlEqualTo("/api/init"))
              .withRequestBody(
                equalToJson(
                  Json.obj().toString()
                )
              )
              .willReturn(
                aResponse()
                  .withStatus(ACCEPTED)
              )
          )

          recoverToSucceededIf[RuntimeException] {
            connector.initJourney()
          }
        }

        "return a Left(DefaultedUnexpectedFailure) when unexpected response" in {

          stubFor(
            post(urlEqualTo("/api/init"))
              .withRequestBody(
                equalToJson(
                  Json.obj().toString()
                )
              )
              .willReturn(
                aResponse()
                  .withHeader(HeaderNames.LOCATION, "/foo")
                  .withStatus(BAD_REQUEST)
              )
          )

          recoverToSucceededIf[RuntimeException] {
            connector.initJourney()
          }
        }

      }

    }

    "retrieveAddress" should {
      "retrieveAddress" in {
        val address = Address("address_one", None, None, None, None, None)
        val json = Json.toJson(address).toString
        val id = "test-id"

        wireMockServer.stubFor(
          get(urlEqualTo(s"/api/v2/confirmed?id=$id"))
            .willReturn(okJson(json))
        )

        connector.retrieveAddress(id).map(_ mustBe address)
      }
    }

  }

}
