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
import models.*
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

  private val addressLookupConfig = AddressLookupConfigSettings(
    options = AddressLookupConfigOptions(
      continueUrl            = "http://localhost:9000/continue",
      homeNavHref            = "http://localhost:9000/home",
      signOutHref            = "http://localhost:9000/sign-out",
      accessibilityFooterUrl = "http://localhost:9000/accessibility",
      deskProServiceName     = "gambling-variations-frontend",
      allowedCountryCodes    = Seq("GB"),
      selectPageConfig       = SelectPageConfig(30, showSearchLinkAgain = true, showNoneOfTheseOption = false),
      confirmPageConfig = ConfirmPageConfig(
        showChangeLink        = true,
        showSubHeadingAndInfo = false,
        showSearchAgainLink   = true,
        showConfirmChangeText = false
      ),
      manualAddressEntryConfig = ManualAddressEntryConfig(
        line1MaxLength  = 35,
        line2MaxLength  = 35,
        line3MaxLength  = 35,
        townMaxLength   = 35,
        mandatoryFields = Map("addressLine1" -> true),
        maxLengthErrorMessages = MaxLengthErrorMessages(
          en = ManualAddressEntryLineContent("line1", "line2", "line3", "town"),
          cy = ManualAddressEntryLineContent("line1", "line2", "line3", "town")
        )
      )
    ),
    labels = {
      val editPageLabels = EditPageLabels(
        title         = "Enter address",
        heading       = "Enter address",
        line1Label    = "Address line 1",
        line2Label    = "Address line 2",
        line3Label    = "Address line 3",
        townLabel     = "Town or city",
        postcodeLabel = Some("Postcode"),
        countryLabel  = Some("Country"),
        submitLabel   = Some("Continue")
      )

      val labels = AddressLookupLabelContent(
        appLevelLabels = AppLevelLabels("Manage your gambling variation"),
        selectPageLabels = SelectPageLabels(
          title               = "Select address",
          heading             = "Select address",
          headingWithPostcode = "Select address for {0}",
          proposalListLabel   = "Select an address",
          submitLabel         = "Continue",
          searchAgainLinkText = "Search again"
        ),
        lookupPageLabels = LookupPageLabels(
          title                      = "Find address",
          heading                    = "Find address",
          afterHeadingText           = "We will use this address to contact you.",
          filterLabel                = "Property name or number",
          postcodeLabel              = "Postcode",
          submitLabel                = "Find address",
          noResultsFoundMessage      = "No addresses found",
          resultLimitExceededMessage = "Too many addresses found"
        ),
        confirmPageLabels = ConfirmPageLabels(
          title               = "Confirm address",
          heading             = "Confirm address",
          searchAgainLinkText = "Search again",
          confirmChangeText   = "By confirming this change, you agree that the information is correct."
        ),
        editPageLabels = editPageLabels,
        international  = International(editPageLabels)
      )

      AddressLookupLabels(en = labels, cy = labels)
    }
  )

  "AddressLookupConnector" should {

    ".initJourney() method" should {

      "for a successful response" must {
        "return a Location for callback" in {

          stubFor(
            post(urlEqualTo("/api/init"))
              .withRequestBody(
                equalToJson(
                  Json.toJson(addressLookupConfig).toString()
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
            connector.initJourney(addressLookupConfig).futureValue

          actualResult mustBe expectedResult

        }
      }

      "for an error response" must {

        "return a Left(Invalid) when no location returns" in {

          stubFor(
            post(urlEqualTo("/api/init"))
              .withRequestBody(
                equalToJson(
                  Json.toJson(addressLookupConfig).toString()
                )
              )
              .willReturn(
                aResponse()
                  .withStatus(ACCEPTED)
              )
          )

          recoverToSucceededIf[RuntimeException] {
            connector.initJourney(addressLookupConfig)
          }
        }

        "return a Left(DefaultedUnexpectedFailure) when unexpected response" in {

          stubFor(
            post(urlEqualTo("/api/init"))
              .withRequestBody(
                equalToJson(
                  Json.toJson(addressLookupConfig).toString()
                )
              )
              .willReturn(
                aResponse()
                  .withHeader(HeaderNames.LOCATION, "/foo")
                  .withStatus(BAD_REQUEST)
              )
          )

          recoverToSucceededIf[RuntimeException] {
            connector.initJourney(addressLookupConfig)
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
