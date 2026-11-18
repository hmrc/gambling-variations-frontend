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

package services

import connectors.GamblingConnector
import models.ClientListStatus
import play.api.Logging
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

enum AgentClientAuthResult:
  case Authorised, NotAuthorised, NotReady, Failed

@Singleton
class AgentClientAuthService @Inject() (connector: GamblingConnector)(implicit ec: ExecutionContext) extends Logging {

  // This service is MGD-only
  private val regime = "mgd"

  def authoriseClient(regNumber: String)(using hc: HeaderCarrier): Future[AgentClientAuthResult] =
    connector
      .startClientListRetrieval(regime)
      .flatMap {
        case ClientListStatus.Succeeded =>
          logger.info(s"client list retrieval succeeded for agent authorisation regime=$regime")
          connector.hasClient(regime, regNumber).map { hasClient =>
            logger.info(s"hasClient check for agent authorisation regime=$regime hasClient=$hasClient")
            if (hasClient) AgentClientAuthResult.Authorised else AgentClientAuthResult.NotAuthorised
          }
        case status @ (ClientListStatus.InProgress | ClientListStatus.InitiateDownload) =>
          logger.warn(s"client list not ready for agent authorisation regime=$regime status=${status.asString}")
          Future.successful(AgentClientAuthResult.NotReady)
        case ClientListStatus.Failed =>
          logger.warn(s"client list retrieval failed for agent authorisation regime=$regime")
          Future.successful(AgentClientAuthResult.Failed)
      }
      .recover { case NonFatal(e) =>
        logger.error(s"failed to authorise agent client regime=$regime", e)
        AgentClientAuthResult.Failed
      }
}
