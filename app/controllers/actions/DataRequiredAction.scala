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

import controllers.routes
import models.BusinessType.Soleproprietor
import models.UserAnswers
import models.requests.{DataRequest, OptionalDataRequest}
import pages.{BusinessNamePage, BusinessTypePage, TradingNamePage}
import play.api.Logging
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, Result}
import repositories.SessionRepository

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DataRequiredActionImpl @Inject() (
  val sessionRepository: SessionRepository
)(implicit val executionContext: ExecutionContext)
    extends DataRequiredAction
    with Logging {

  override protected def refine[A](request: OptionalDataRequest[A]): Future[Either[Result, DataRequest[A]]] = {
    request.userAnswers match {
      case None =>
        logger.info(s"User Answers not found. Populating User Answers to id ${request.mgdRegNum}")
        UserAnswers(request.mgdRegNum)
          .set(TradingNamePage, "Trader One")
          .flatMap(_.set(BusinessNamePage, "Business One"))
          .flatMap(_.set(BusinessTypePage, Soleproprietor))
          .map { ua =>
            logger.info("User Answers not found. Saving User Answers")
            sessionRepository.set(ua) map {
              case true =>
                logger.info("User Answers saved.")
                Right(DataRequest(request.request, request.mgdRegNum, ua))
              case false =>
                logger.info("User Answers failed.")
                Left(Redirect(routes.SystemErrorController.onPageLoad()))
            }
          } getOrElse Future.successful(Left(Redirect(routes.SystemErrorController.onPageLoad())))
      case Some(data) =>
        logger.info(s"User Answers found with id ${data.id}")
        Future.successful(Right(DataRequest(request.request, request.mgdRegNum, data)))
    }
  }
}

trait DataRequiredAction extends ActionRefiner[OptionalDataRequest, DataRequest]
