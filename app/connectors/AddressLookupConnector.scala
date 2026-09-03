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
import models.addresslookup.AddressLookupConfigSettings
import play.api.Logging
import play.api.http.HeaderNames
import play.api.http.Status.ACCEPTED
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{Json, Reads, __}
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

  private implicit val confirmedAddressReads: Reads[Address] = (
    (__ \ "address" \ "lines").readNullable[Seq[String]].map(_.getOrElse(Seq.empty)) and
      (__ \ "address" \ "postcode").readNullable[String] and
      (__ \ "address" \ "country" \ "code").readNullable[String]
  ) { (lines, postcode, country) =>
    Address(
      address1 = lines.headOption.getOrElse(""),
      address2 = lines.lift(1),
      address3 = lines.lift(2),
      address4 = lines.lift(3),
      postcode = postcode,
      country  = country
    )
  }

  def initJourney(configSettings: AddressLookupConfigSettings)(implicit hc: HeaderCarrier): Future[String] = {
    logger.info(s"[AddressLookup]: Initialising journey using endpoint ${config.addressLookupFrontendBaseUrl}/api/init")

    httpClient
      .post(url"${config.addressLookupFrontendBaseUrl}/api/init")
      .withBody(Json.toJson(configSettings))
      .execute[HttpResponse]
      .recoverWith { case exception =>
        logger.error(
          s"[AddressLookup]: Failed to initialise journey using endpoint ${config.addressLookupFrontendBaseUrl}/api/init",
          exception
        )
        Future.failed(exception)
      }
      .map { response =>
        logger.info(
          s"[AddressLookup]: Initialisation response status=${response.status}, " +
            s"locationHeaderPresent=${response.header(HeaderNames.LOCATION).isDefined}"
        )

        response.status match {
          case ACCEPTED =>
            response.header(HeaderNames.LOCATION) match {
              case Some(locationURL) => locationURL
              case None =>
                logger.warn("[AddressLookup]: No Location Header returned from Address Lookup")
                throw new RuntimeException("[AddressLookup]: No Location Header returned from Address Lookup")
            }
          case status =>
            val message = s"[AddressLookup]: Unexpected response, status $status returned"
            logger.error(message)
            throw new RuntimeException(message)
        }
      }
  }

  def retrieveAddress(
    id: String
  )(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Address] = {
    val fullUrl = s"${config.retrieveAddressUrl}?id=$id"

    logger.info(s"[AddressLookup]: Retrieving confirmed address using endpoint ${config.retrieveAddressUrl}")

    httpClient
      .get(url"$fullUrl")
      .execute[Address]
      .map { address =>
        logger.info("[AddressLookup]: Successfully retrieved confirmed address")
        address
      }
      .recoverWith { case exception =>
        logger.error(
          s"[AddressLookup]: Failed to retrieve confirmed address using endpoint ${config.retrieveAddressUrl}",
          exception
        )
        Future.failed(exception)
      }
  }

}
