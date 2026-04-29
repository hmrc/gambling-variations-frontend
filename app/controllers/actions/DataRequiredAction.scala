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

import connectors.GamblingConnector
import controllers.routes
import models.requests.{DataRequest, OptionalDataRequest}
import models.{BusinessName, UserAnswers}
import pages.{BusinessNamePage, BusinessTypePage, TradingNamePage}
import play.api.Logging
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, Result}
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class DataRequiredActionImpl @Inject() (
  val sessionRepository: SessionRepository,
  val gamblingConnector: GamblingConnector
)(implicit val executionContext: ExecutionContext)
    extends DataRequiredAction
    with Logging {

  override protected def refine[A](request: OptionalDataRequest[A]): Future[Either[Result, DataRequest[A]]] = {
    request.userAnswers match {
      case None =>
        logger.info(s"User Answers not found. Populating User Answers to id ${request.mgdRegNum}")

        given HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

        gamblingConnector.getBusinessName(request.mgdRegNum) flatMap { businessName =>

          UserAnswers(request.mgdRegNum).set(BusinessTypePage, businessName.businessType).flatMap { userAnswers =>

            populateUserAnswers(userAnswers, businessName).map { ua =>
              logger.info("User Answers not found. Saving User Answers")
              sessionRepository.set(ua) map {
                case true =>
                  logger.info("User Answers saved.")
                  Right(DataRequest(request.request, request.mgdRegNum, ua))
                case false =>
                  logger.info("User Answers failed.")
                  Left(Redirect(routes.SystemErrorController.onPageLoad()))
              }
            }
          } getOrElse Future.successful(Left(Redirect(routes.SystemErrorController.onPageLoad())))
        }
      case Some(data) =>
        logger.info(s"User Answers found with id ${data.id}")
        Future.successful(Right(DataRequest(request.request, request.mgdRegNum, data)))
    }
  }

  private def populateUserAnswers(answers: UserAnswers, businessName: BusinessName): Try[UserAnswers] = {
    List(
      BusinessNamePage -> businessName.businessName,
      TradingNamePage  -> businessName.tradingName
    ).foldLeft(Try(answers)) {
      case (tryUa, (page, Some(value))) => tryUa.flatMap(_.set(page, value))
      case (tryUa, (_, None))           => tryUa
    }
  }
}

trait DataRequiredAction extends ActionRefiner[OptionalDataRequest, DataRequest]
