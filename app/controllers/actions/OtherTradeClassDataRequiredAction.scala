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
import models.requests.{DataRequest, OptionalDataRequest}
import play.api.Logging
import play.api.mvc.{ActionRefiner, Result}
import play.api.mvc.Results.Redirect

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class OtherTradeClassDataRequiredActionImpl @Inject() (
)(implicit val executionContext: ExecutionContext)
    extends OtherTradeClassDataRequiredAction
    with Logging {

  override protected def refine[A](
    request: OptionalDataRequest[A]
  ): Future[Either[Result, DataRequest[A]]] = {

    request.userAnswers match {

      case None =>
        logger.warn(s"No UserAnswers found for id ${request.mgdRegNum}. Redirecting to SystemError.")
        Future.successful(Left(Redirect(routes.SystemErrorController.onPageLoad())))

      case Some(answers) =>
        logger.info(s"UserAnswers found for id ${answers.id}. Proceeding.")
        Future.successful(Right(DataRequest(request.request, request.mgdRegNum, answers)))
    }
  }
}

trait OtherTradeClassDataRequiredAction extends ActionRefiner[OptionalDataRequest, DataRequest]
