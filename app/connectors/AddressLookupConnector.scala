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

import config.FrontendAppConfig
import models.Address
import play.api.Logging
import play.api.http.HeaderNames
import play.api.http.Status.ACCEPTED
import play.api.libs.json.Json
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import uk.gov.hmrc.http.*
import uk.gov.hmrc.http.client.HttpClientV2

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AddressLookupConnector @Inject() (
  config: FrontendAppConfig,
  implicit val httpClient: HttpClientV2
)(implicit ec: ExecutionContext)
    extends HttpReadsInstances
    with Logging {

  def initJourney()(implicit hc: HeaderCarrier): Future[String] = {
    httpClient
      .post(url"${config.addressLookupFrontendBaseUrl}/api/init")
      .withBody(Json.obj())
      .execute[HttpResponse]
      .map { response =>
        response.status match {
          case ACCEPTED =>
            response.header(HeaderNames.LOCATION) match {
              case Some(locationURL) => locationURL
              case None =>
                logger.warn("[AddressLookup]: No Location Header returned from Address Lookup")
                throw new RuntimeException("[AddressLookup]: No Location Header returned from Address Lookup")
            }
          case status =>
            logger.error("[AddressLookup]: Unexpected response, status $status returned")
            throw new RuntimeException("[AddressLookup]: Unexpected response, status $status returned")
        }
      }
  }

  def retrieveAddress(
    id: String
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Address] = {
    val fullUrl = s"${config.retrieveAddressUrl}?id=$id"
    httpClient.get(url"$fullUrl").execute[Address]
  }

}
